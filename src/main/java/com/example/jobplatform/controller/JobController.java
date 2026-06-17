package com.example.jobplatform.controller;

import com.example.jobplatform.dto.JobRequestDTO;
import com.example.jobplatform.dto.JobResponseDTO;
// ...existing imports...
import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.service.JobService;
import com.example.jobplatform.service.JobSkillService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;
    private final JobSkillService jobSkillService;

    public JobController(JobService jobService,
                         JobSkillService jobSkillService) {
        this.jobService = jobService;
        this.jobSkillService = jobSkillService;
    }

    @PostMapping
    public JobResponseDTO createJob(@Valid @RequestBody JobRequestDTO job) {
        return jobService.saveJob(job);
    }

    @GetMapping
    public List<JobResponseDTO> getAllJobs() {

        return jobService.getAllJobs();
    }

    @PostMapping("/{jobId}/skills/{skillId}")
    public String addSkillToJob(@PathVariable Long jobId,
                                @PathVariable Long skillId) {

        jobSkillService.addSkillToJob(jobId, skillId);
        return "Skill added to job";
    }

    @GetMapping("/{jobId}/skills")
    public List<Skill> getSkillsByJob(@PathVariable Long jobId) {
        return jobSkillService.getSkillsByJob(jobId);
    }
}