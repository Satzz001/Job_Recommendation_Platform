package com.example.jobplatform.service;

import com.example.jobplatform.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillSimilarityService {

    private final SkillRepository skillRepository;

    // 0.70 = 70% cosine similarity required — tune higher for stricter, lower for more lenient matching
    private static final double SIMILARITY_THRESHOLD = 0.70;

    public SkillSimilarityService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Long> expandSkillIds(List<Long> userSkillIds) {
        List<Long> similarIds = skillRepository.findAllSimilarSkillIds(userSkillIds, SIMILARITY_THRESHOLD);
        if (similarIds.isEmpty()) {
            return userSkillIds;
        }
        List<Long> expanded = new ArrayList<>(userSkillIds);
        expanded.addAll(similarIds);
        return expanded;
    }

    public boolean hasSemanticMatch(Long jobSkillId, List<Long> userSkillIds) {
        if (userSkillIds == null || userSkillIds.isEmpty()) {
            return false;
        }
        if (userSkillIds.contains(jobSkillId)) {
            return false;
        }
        List<Long> similarIds = skillRepository.findSimilarSkillIds(
            jobSkillId,
            userSkillIds,
            SIMILARITY_THRESHOLD
        );
        return !similarIds.isEmpty();
    }
}
