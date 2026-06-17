package com.example.jobplatform.controller;

import com.example.jobplatform.dto.SkillRequestDTO;
import com.example.jobplatform.dto.SkillResponseDTO;
// ...existing imports...
import com.example.jobplatform.service.SkillService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping
    public SkillResponseDTO createSkill(@Valid @RequestBody SkillRequestDTO skill) {
        return skillService.saveSkill(skill);
    }

    @GetMapping
    public List<SkillResponseDTO> getAllSkills() {
        return skillService.getAllSkills();
    }
}