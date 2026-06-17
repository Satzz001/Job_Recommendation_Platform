package com.example.jobplatform.controller;

import com.example.jobplatform.enums.InteractionType;
import com.example.jobplatform.dto.JobResponseDTO;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.service.JobSkillService;
import com.example.jobplatform.service.UserJobInteractionService;
import com.example.jobplatform.service.UserSkillService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserSkillService userSkillService;
    private final JobSkillService jobSkillService;
    private final UserJobInteractionService userJobInteractionService;
    private final JobService jobService;

    public UserController(UserSkillService userSkillService,
                          JobSkillService jobSkillService,
                          UserJobInteractionService userJobInteractionService,
                          JobService jobService) {
        this.userSkillService = userSkillService;
        this.jobSkillService = jobSkillService;
        this.userJobInteractionService = userJobInteractionService;
        this.jobService = jobService;
    }

    @PostMapping("/{userId}/skills/{skillId}")
    public String addSkillToUser(@PathVariable Long userId,
                                 @PathVariable Long skillId) {

        userSkillService.addSkillToUser(userId, skillId);
        return "Skill added to user";
    }

    @GetMapping("/{userId}/skills")
    public List<Skill> getUserSkills(@PathVariable Long userId) {
        return userSkillService.getSkillsByUser(userId);
    }

    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<Page<JobResponseDTO>> recommendJobs(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(jobService.getRecommendations(userId, pageable));
    }

    @PostMapping("/{userId}/jobs/{jobId}/interaction")
    public String interact(@PathVariable Long userId,
                           @PathVariable Long jobId,
                           @RequestParam InteractionType type) {

        userJobInteractionService.interact(userId, jobId, type);
        return "Interaction saved";
    }
}