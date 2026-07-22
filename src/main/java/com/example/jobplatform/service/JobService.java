package com.example.jobplatform.service;

import com.example.jobplatform.dto.JobRequestDTO;
import com.example.jobplatform.dto.JobResponseDTO;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.JobSkill;
import com.example.jobplatform.entity.UserJobInteraction;
import com.example.jobplatform.entity.UserProfile;
import com.example.jobplatform.enums.ImportanceLevel;
import com.example.jobplatform.enums.InteractionType;
import com.example.jobplatform.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSkillRepository userSkillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final UserJobInteractionRepository userJobInteractionRepository;
    private final SkillSimilarityService skillSimilarityService;

    public JobService(JobRepository jobRepository,
                      UserProfileRepository userProfileRepository,
                      UserSkillRepository userSkillRepository,
                      JobSkillRepository jobSkillRepository,
                      UserJobInteractionRepository userJobInteractionRepository,
                      SkillSimilarityService skillSimilarityService) {
        this.jobRepository = jobRepository;
        this.userProfileRepository = userProfileRepository;
        this.userSkillRepository = userSkillRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.userJobInteractionRepository = userJobInteractionRepository;
        this.skillSimilarityService = skillSimilarityService;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public JobResponseDTO saveJob(JobRequestDTO dto) {

        Job job = toEntity(dto);
        Job saved =  jobRepository.save(job);
        return toResponse(saved);
    }

    public List<JobResponseDTO> getAllJobs() {
        List<Job> jobs =  jobRepository.findAll();

        return jobs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Page<JobResponseDTO> getRecommendations(Long userId, Pageable pageable) {
        // 1. Get user skills
        List<Long> userSkillIds = userSkillRepository.findByUserId(userId)
                .stream().map(us -> us.getSkill().getId()).toList();

        if (userSkillIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2. Get user profile for location/jobType preferences
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);

        // 3. Expand skill IDs with semantic matches, then find candidate jobs
        List<Long> expandedSkillIds = skillSimilarityService.expandSkillIds(userSkillIds);
        List<Job> candidates = jobSkillRepository.findBySkillIdIn(expandedSkillIds)
                .stream().map(JobSkill::getJob).distinct().collect(Collectors.toList());

        // Filter out jobs the user has already applied to
        List<Long> appliedJobIds = userJobInteractionRepository.findByUserIdAndInteractionType(userId, InteractionType.APPLIED)
                .stream().map(interaction -> interaction.getJob().getId()).toList();

        candidates.removeIf(job -> appliedJobIds.contains(job.getId()));

        // 4. Batch load all data needed for scoring — 3 queries total
        List<Long> candidateJobIds = candidates.stream().map(Job::getId).toList();

        if (candidateJobIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, List<JobSkill>> jobSkillsMap = jobSkillRepository.findByJobIdIn(candidateJobIds)
                .stream().collect(groupingBy(js -> js.getJob().getId()));

        Set<Long> expandedSkillSet = new HashSet<>(expandedSkillIds);

        Map<Long, Double> similarityMap = jobRepository.getSemanticSimilarities(userId, candidateJobIds)
                .stream().collect(Collectors.toMap(
                        r -> r.getJobId(),
                        r -> r.getSimilarity()
                ));

        // 5. Score each job
        List<JobResponseDTO> scoredJobs = candidates.stream().map(job -> {
                    List<JobSkill> jobSkills = jobSkillsMap.getOrDefault(job.getId(), List.of());
                    double score = calculateScore(job, jobSkills, userSkillIds, expandedSkillSet, profile, similarityMap.get(job.getId()));
                    return toResponseWithScore(job, score);
                }).sorted((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()))
                .toList();

        // 5. Apply pagination manually (since we sorted in memory)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), scoredJobs.size());
        return new PageImpl<>(scoredJobs.subList(start, end), pageable, scoredJobs.size());
    }

    private double calculateScore(Job job, List<JobSkill> jobSkills, List<Long> userSkillIds,
                                   Set<Long> expandedSkillSet, UserProfile profile, Double similarity) {
        double score = 0;

        // Skill Match (50%) — MUST_HAVE weight 1.0, NICE_TO_HAVE weight 0.4
        if (!jobSkills.isEmpty()) {
            double totalWeight = 0.0;
            double matchedWeight = 0.0;
            for (JobSkill js : jobSkills) {
                double weight = js.getImportance() == ImportanceLevel.MUST_HAVE ? 1.0 : 0.4;
                totalWeight += weight;

                Long jobSkillId = js.getSkill().getId();
                if (userSkillIds.contains(jobSkillId)) {
                    matchedWeight += weight;
                } else if (expandedSkillSet.contains(jobSkillId)) {
                    matchedWeight += weight * 0.85;
                }
            }
            score += (matchedWeight / totalWeight) * 50;
        }

        // Description–Summary Semantic Match (20%)
        if (similarity != null) {
            score += similarity * 20;
        }

        // Preference Match (20%) + Experience (10%)
        if (profile != null) {
            if (job.getLocation() != null && job.getLocation().equalsIgnoreCase(profile.getPreferredLocation())) {
                score += 10;
            }
            if (job.getJobType() != null && job.getJobType() == profile.getJobType()) {
                score += 10;
            }
            if (job.getExperienceLevel() != null && job.getExperienceLevel() == profile.getExperience()) {
                score += 10;
            }
        }

        return score;
    }

    private JobResponseDTO toResponseWithScore(Job job, double score) {
        JobResponseDTO dto = toResponse(job);
        dto.setMatchScore(score);
        return dto;
    }

    private Job toEntity(JobRequestDTO dto) {
        Job job = new Job();
        job.setTitle(dto.getTitle());
        job.setCompanyName(dto.getCompanyName());
        job.setDescription(dto.getDescription());
        job.setLocation(dto.getLocation());
        job.setJobType(dto.getJobType());
        job.setExperienceLevel(dto.getExperienceLevel());
        job.setApplyUrl(dto.getApplyUrl());
        job.setSource(dto.getSource());
        return job;
    }

    public JobResponseDTO toResponse(Job job) {
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(job.getId());
        dto.setTitle(job.getTitle());
        dto.setCompanyName(job.getCompanyName());
        dto.setLocation(job.getLocation());
        dto.setJobType(job.getJobType());
        dto.setExperienceLevel(job.getExperienceLevel());
        dto.setDescription(job.getDescription());
        dto.setApplyUrl(job.getApplyUrl());
        return dto;
    }
}