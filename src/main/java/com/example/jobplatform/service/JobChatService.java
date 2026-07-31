package com.example.jobplatform.service;

import com.example.jobplatform.dto.JobResponseDTO;
import com.example.jobplatform.dto.chat.ChatResponseDTO;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.UserProfile;
import com.example.jobplatform.repository.JobRepository;
import com.example.jobplatform.repository.UserProfileRepository;
import com.example.jobplatform.repository.UserSkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Completes the RAG loop for job search: embeds the user's free-text query,
 * retrieves the top semantically-similar jobs via pgvector, and asks an LLM
 * to generate a grounded answer from that retrieved context.
 */
@Slf4j
@Service
public class JobChatService {

    private static final int TOP_K = 5;
    // Query intent leads; the user's resume-derived profile nudges results toward their background.
    private static final double QUERY_WEIGHT = 0.65;
    private static final double PROFILE_WEIGHT = 1 - QUERY_WEIGHT;
    private static final int DESCRIPTION_SNIPPET_LENGTH = 450;

    private static final String SYSTEM_PROMPT =
        "You are a job search assistant. Answer the user's question using ONLY the job " +
        "listings provided in the context below. Do not invent or reference any job that " +
        "is not in the list. When you mention a job, cite its title and company name. " +
        "If the user has a listed skill/experience profile, factor it into how you explain " +
        "fit (e.g. skills they already have vs. what a job additionally requires). " +
        "If none of the listed jobs are a good match for the user's request, say so honestly " +
        "instead of forcing a recommendation. Be concise and helpful.";

    private static final String GENERAL_CHAT_SYSTEM_PROMPT =
        "You are the chat assistant embedded in a job platform. Respond naturally to whatever " +
        "the user says — greetings, small talk, career advice, interview prep, resume tips, or " +
        "any other question — the same way a helpful general-purpose assistant would. If the " +
        "user has a listed skill/experience profile and it's relevant, feel free to use it. " +
        "Do not invent specific job listings — if the user actually wants job recommendations, " +
        "tell them to ask you to find matching jobs. Be concise and conversational.";

    private static final String INTENT_CLASSIFIER_SYSTEM_PROMPT =
        "You are an intent classifier for a job platform chat assistant. Classify the user's " +
        "message into exactly one category:\n\n" +
        "- JOB_SEARCH: the user wants job recommendations, is describing the kind of role/company " +
        "they want, or is asking about specific job listings.\n" +
        "- GENERAL_ADVICE: anything else — greetings, small talk, career advice, interview tips, " +
        "resume/skill guidance, or any question that is not asking to be matched to job listings.\n\n" +
        "Respond with ONLY one word: JOB_SEARCH or GENERAL_ADVICE. No punctuation, no explanation.";

    private final GeminiEmbeddingService geminiEmbeddingService;
    private final JobRepository jobRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final OpenRouterService openRouterService;
    private final JobService jobService;

    public JobChatService(GeminiEmbeddingService geminiEmbeddingService,
                          JobRepository jobRepository,
                          UserProfileRepository userProfileRepository,
                          UserSkillRepository userSkillRepository,
                          OpenRouterService openRouterService,
                          JobService jobService) {
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.jobRepository = jobRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSkillRepository = userSkillRepository;
        this.openRouterService = openRouterService;
        this.jobService = jobService;
    }

    public ChatResponseDTO answer(Long userId, String userMessage) {
        if (!isJobSearchIntent(userMessage)) {
            return answerGeneralAdvice(userId, userMessage);
        }

        String queryEmbedding = geminiEmbeddingService.generateEmbedding(userMessage);
        if (queryEmbedding == null) {
            return new ChatResponseDTO(
                "Sorry, I couldn't process your request right now. Please try again shortly.",
                List.of());
        }

        String searchEmbedding = personalizeEmbedding(userId, queryEmbedding);

        List<JobRepository.JobSearchResult> results = jobRepository.findTopSimilarJobs(searchEmbedding, TOP_K);
        if (results.isEmpty()) {
            return new ChatResponseDTO(
                "I couldn't find any matching jobs in our listings right now.",
                List.of());
        }

        List<Long> orderedIds = results.stream().map(JobRepository.JobSearchResult::getId).toList();
        Map<Long, Double> similarityById = results.stream()
            .collect(Collectors.toMap(JobRepository.JobSearchResult::getId, JobRepository.JobSearchResult::getSimilarity));

        Map<Long, Job> jobsById = jobRepository.findAllById(orderedIds).stream()
            .collect(Collectors.toMap(Job::getId, j -> j));

        // findAllById does not guarantee input order — reorder to match the similarity ranking
        List<Job> orderedJobs = orderedIds.stream()
            .map(jobsById::get)
            .filter(Objects::nonNull)
            .toList();

        String userProfileLine = buildUserProfileLine(userId);
        String jobContext = buildJobContext(orderedJobs, similarityById);
        List<JobResponseDTO> sourceJobs = orderedJobs.stream().map(jobService::toResponse).toList();

        String answer;
        try {
            String userPrompt = userProfileLine + "User question: " + userMessage + "\n\n" + jobContext;
            answer = openRouterService.chat(SYSTEM_PROMPT, userPrompt);
        } catch (RuntimeException e) {
            log.error("Chat generation failed after successful retrieval", e);
            answer = "I found some matching jobs, but couldn't generate a summary right now.";
        }

        return new ChatResponseDTO(answer, sourceJobs);
    }

    /**
     * Classifies whether the user's message is asking for job recommendations
     * (needs the RAG retrieval flow) or general career advice (no retrieval needed).
     * Defaults to JOB_SEARCH on classification failure, preserving prior behavior.
     */
    private boolean isJobSearchIntent(String userMessage) {
        try {
            String verdict = openRouterService.chat(INTENT_CLASSIFIER_SYSTEM_PROMPT, userMessage)
                .trim()
                .toUpperCase();
            return !verdict.contains("GENERAL_ADVICE");
        } catch (RuntimeException e) {
            log.warn("Intent classification failed — defaulting to job search flow", e);
            return true;
        }
    }

    /**
     * Answers anything that isn't a job-search request — greetings, small talk, career
     * advice, etc. — directly, skipping vector retrieval since there's no job-listing
     * context needed to ground the answer.
     */
    private ChatResponseDTO answerGeneralAdvice(Long userId, String userMessage) {
        String userProfileLine = buildUserProfileLine(userId);
        String answer;
        try {
            String userPrompt = userProfileLine + "User question: " + userMessage;
            answer = openRouterService.chat(GENERAL_CHAT_SYSTEM_PROMPT, userPrompt);
        } catch (RuntimeException e) {
            log.error("General chat generation failed", e);
            answer = "Sorry, I couldn't process your request right now. Please try again shortly.";
        }
        return new ChatResponseDTO(answer, List.of());
    }

    /**
     * Blends the query embedding with the user's resume-derived summary embedding
     * so retrieval favors jobs matching both what they asked and who they are.
     * Falls back to the raw query embedding if the user has no summary embedding yet.
     */
    private String personalizeEmbedding(Long userId, String queryEmbedding) {
        String profileEmbedding = userProfileRepository.findSummaryEmbeddingByUserId(userId).orElse(null);
        if (profileEmbedding == null) {
            return queryEmbedding;
        }

        try {
            double[] query = parseVector(queryEmbedding);
            double[] profile = parseVector(profileEmbedding);
            double[] blended = new double[query.length];
            for (int i = 0; i < query.length; i++) {
                blended[i] = query[i] * QUERY_WEIGHT + profile[i] * PROFILE_WEIGHT;
            }
            return formatVector(blended);
        } catch (RuntimeException e) {
            log.error("Failed to blend query and profile embeddings for user {} — falling back to query-only search", userId, e);
            return queryEmbedding;
        }
    }

    private double[] parseVector(String vectorLiteral) {
        String trimmed = vectorLiteral.substring(1, vectorLiteral.length() - 1);
        String[] parts = trimmed.split(",");
        double[] values = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Double.parseDouble(parts[i]);
        }
        return values;
    }

    private String formatVector(double[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(values[i]);
        }
        return sb.append("]").toString();
    }

    private String buildUserProfileLine(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        List<String> skillNames = userSkillRepository.findByUserId(userId).stream()
            .map(us -> us.getSkill().getName())
            .toList();

        List<String> parts = new ArrayList<>();
        if (!skillNames.isEmpty()) {
            parts.add("skills=" + String.join(", ", skillNames));
        }
        if (profile != null) {
            if (profile.getExperience() != null) {
                parts.add("experience=" + profile.getExperience());
            }
            if (profile.getPreferredLocation() != null) {
                parts.add("preferred location=" + profile.getPreferredLocation());
            }
            if (profile.getJobType() != null) {
                parts.add("preferred job type=" + profile.getJobType());
            }
        }

        if (parts.isEmpty()) {
            return "";
        }
        return "User profile: " + String.join("; ", parts) + "\n\n";
    }

    private String buildJobContext(List<Job> jobs, Map<Long, Double> similarityById) {
        StringBuilder sb = new StringBuilder("Job listings:\n");
        int i = 1;
        for (Job job : jobs) {
            Double similarity = similarityById.get(job.getId());
            String description = job.getDescription() == null ? "" :
                job.getDescription().substring(0, Math.min(DESCRIPTION_SNIPPET_LENGTH, job.getDescription().length()));

            sb.append(i++).append(". ").append(job.getTitle())
                .append(" at ").append(job.getCompanyName())
                .append(" (location=").append(job.getLocation())
                .append(", type=").append(job.getJobType())
                .append(", experience=").append(job.getExperienceLevel())
                .append(similarity != null ? String.format(", relevance=%.0f%%", similarity * 100) : "")
                .append(")\n   ").append(description).append("\n");
        }
        // Note: job descriptions come from admin-created listings and are not sanitized
        // against prompt injection — acceptable for this portfolio-scope project.
        return sb.toString();
    }
}
