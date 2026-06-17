package com.example.jobplatform.service;

import com.example.jobplatform.dto.SkillRequestDTO;
import com.example.jobplatform.dto.SkillResponseDTO;
import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.repository.SkillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SkillNormalizationService normalizationService;

    public SkillService(SkillRepository skillRepository, SkillNormalizationService normalizationService) {
        this.skillRepository = skillRepository;
        this.normalizationService = normalizationService;
    }

    public SkillResponseDTO saveSkill(SkillRequestDTO dto) {
        Skill skill = findOrCreate(dto.getName());
        return toResponse(skill);
    }

    public Skill findOrCreate(String rawSkillName) {
        String canonicalName = normalizationService.normalize(rawSkillName);
        return findOrCreateNormalized(canonicalName);
    }

    public Skill findOrCreateNormalized(String normalizedName) {
        return skillRepository.findByNameIgnoreCase(normalizedName)
            .orElseGet(() -> {
                Skill s = new Skill();
                s.setName(normalizedName);
                return skillRepository.save(s);
            });
    }

    public List<SkillResponseDTO> getAllSkills() {
        return skillRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private SkillResponseDTO toResponse(Skill skill) {
        SkillResponseDTO dto = new SkillResponseDTO();
        dto.setId(skill.getId());
        dto.setName(skill.getName());
        return dto;
    }
}
