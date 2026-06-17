package com.example.jobplatform.service;

import com.example.jobplatform.dto.resume.ParsedResumeDTO;
import com.example.jobplatform.dto.resume.ResumeParsingResponse;
import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.entity.User;
import com.example.jobplatform.entity.UserProfile;
import com.example.jobplatform.entity.UserSkill;
import com.example.jobplatform.enums.ExperienceLevel;
import com.example.jobplatform.enums.JobType;
import com.example.jobplatform.repository.UserProfileRepository;
import com.example.jobplatform.repository.UserRepository;
import com.example.jobplatform.repository.UserSkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class ResumeParsingService {

    private final OpenRouterService openRouterService;
    private final GeminiEmbeddingService geminiEmbeddingService;
    private final SkillNormalizationService skillNormalizationService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final SkillService skillService;
    private final UserSkillRepository userSkillRepository;
    // @Lazy breaks the circular dependency: Spring creates a proxy for self-invocation so @Transactional is honoured
    private final ResumeParsingService self;

    public ResumeParsingService(OpenRouterService openRouterService,
                                GeminiEmbeddingService geminiEmbeddingService,
                                SkillNormalizationService skillNormalizationService,
                                UserRepository userRepository,
                                UserProfileRepository userProfileRepository,
                                SkillService skillService,
                                UserSkillRepository userSkillRepository,
                                @Lazy ResumeParsingService self) {
        this.openRouterService = openRouterService;
        this.geminiEmbeddingService = geminiEmbeddingService;
        this.skillNormalizationService = skillNormalizationService;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.skillService = skillService;
        this.userSkillRepository = userSkillRepository;
        this.self = self;
    }

    public ResumeParsingResponse parseAndSave(Long userId, String rawText) {
        log.info("Starting AI resume parsing for userId={}", userId);

        // --- AI phase: all network calls, no DB connection held ---
        ParsedResumeDTO parsed = openRouterService.parse(rawText);

        String summaryEmbedding = null;
        if (parsed.getSummary() != null && !parsed.getSummary().isBlank()) {
            summaryEmbedding = geminiEmbeddingService.generateEmbedding(parsed.getSummary());
        }

        // Pre-normalize every skill name via AI before opening any transaction
        List<String> aiSkills = parsed.getSkills() != null ? parsed.getSkills() : Collections.emptyList();
        List<String> normalizedSkillNames = new ArrayList<>();
        for (String skillName : aiSkills) {
            if (skillName == null || skillName.isBlank()) continue;
            normalizedSkillNames.add(skillNormalizationService.normalize(skillName));
        }

        // --- DB phase: short transaction, zero AI calls inside ---
        return self.saveResults(userId, parsed, summaryEmbedding, normalizedSkillNames);
    }

    @Transactional
    public ResumeParsingResponse saveResults(Long userId, ParsedResumeDTO parsed,
                                              String summaryEmbedding, List<String> normalizedSkillNames) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new com.example.jobplatform.exception.ResourceNotFoundException("User not found with id: " + userId));

        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseGet(() -> {
                log.info("No existing UserProfile for userId={} — creating new one", userId);
                UserProfile newProfile = new UserProfile();
                newProfile.setUser(user);
                return newProfile;
            });

        List<String> fieldsUpdated = new ArrayList<>();

        if (parsed.getName() != null) {
            user.setName(parsed.getName());
            fieldsUpdated.add("name");
            log.debug("Updating user name to: {}", parsed.getName());
        }
        userRepository.save(user);

        ExperienceLevel validExperience = toValidExperience(parsed.getExperience());
        if (validExperience != null)               { profile.setExperience(validExperience);                      fieldsUpdated.add("experience"); }
        if (parsed.getPreferredLocation() != null) { profile.setPreferredLocation(parsed.getPreferredLocation()); fieldsUpdated.add("preferredLocation"); }
        JobType validJobType = toValidJobType(parsed.getJobType());
        if (validJobType != null)                  { profile.setJobType(validJobType);                            fieldsUpdated.add("jobType"); }
        if (parsed.getSummary() != null)           { profile.setSummary(parsed.getSummary());                     fieldsUpdated.add("summary"); }
        if (parsed.getCollege() != null)           { profile.setCollege(parsed.getCollege());                     fieldsUpdated.add("college"); }
        if (parsed.getDegree() != null)            { profile.setDegree(parsed.getDegree());                       fieldsUpdated.add("degree"); }
        if (parsed.getGraduationYear() != null)    { profile.setGraduationYear(parsed.getGraduationYear());       fieldsUpdated.add("graduationYear"); }
        userProfileRepository.save(profile);

        if (summaryEmbedding != null) {
            userProfileRepository.updateSummaryEmbedding(userId, summaryEmbedding);
            log.debug("Summary embedding saved for userId={}", userId);
        }

        List<String> skillsAdded = new ArrayList<>();
        for (String normalizedName : normalizedSkillNames) {
            // findOrCreateNormalized: no AI call, pure DB find-or-insert
            Skill skill = skillService.findOrCreateNormalized(normalizedName);
            if (!userSkillRepository.existsByUserAndSkill(user, skill)) {
                UserSkill userSkill = new UserSkill();
                userSkill.setUser(user);
                userSkill.setSkill(skill);
                userSkillRepository.save(userSkill);
                skillsAdded.add(normalizedName);
                log.debug("Added skill '{}' to userId={}", normalizedName, userId);
            } else {
                log.debug("Skill '{}' already exists for userId={} — skipping", normalizedName, userId);
            }
        }

        log.info("Resume saved for userId={}: {} fields updated, {} skills added",
            userId, fieldsUpdated.size(), skillsAdded.size());

        List<String> missingFields = parsed.getMissingFields() != null
            ? parsed.getMissingFields()
            : Collections.emptyList();

        String message = missingFields.isEmpty()
            ? "Resume parsed successfully"
            : "Resume parsed with some missing fields";

        return new ResumeParsingResponse(true, message, fieldsUpdated, missingFields, skillsAdded, skillsAdded.size());
    }

    private static ExperienceLevel toValidExperience(String raw) {
        if (raw == null) return null;
        for (ExperienceLevel level : ExperienceLevel.values()) {
            if (level.name().equalsIgnoreCase(raw.trim())) return level;
        }
        log.warn("AI returned invalid experience value: '{}' — skipping", raw);
        return null;
    }

    private static JobType toValidJobType(String raw) {
        if (raw == null) return null;
        for (JobType type : JobType.values()) {
            if (type.name().equalsIgnoreCase(raw.trim())) return type;
        }
        log.warn("AI returned invalid jobType value: '{}' — skipping", raw);
        return null;
    }
}
