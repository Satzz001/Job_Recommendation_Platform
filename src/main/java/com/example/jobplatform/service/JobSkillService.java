package com.example.jobplatform.service;

import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.JobSkill;
import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.enums.ImportanceLevel;
import com.example.jobplatform.exception.ResourceNotFoundException;
import com.example.jobplatform.repository.JobRepository;
import com.example.jobplatform.repository.JobSkillRepository;
import com.example.jobplatform.repository.SkillRepository;
import com.example.jobplatform.repository.UserSkillRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JobSkillService {

    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final UserSkillRepository userSkillRepository;

    public JobSkillService(JobRepository jobRepository,
                           SkillRepository skillRepository,
                           JobSkillRepository jobSkillRepository,
                           UserSkillRepository userSkillRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
        this.jobSkillRepository = jobSkillRepository;
        this.userSkillRepository = userSkillRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public JobSkill addSkillToJob(Long jobId, Long skillId) {
        return addSkillToJob(jobId, skillId, ImportanceLevel.NICE_TO_HAVE);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public JobSkill addSkillToJob(Long jobId, Long skillId, ImportanceLevel importance) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with id " + jobId + " not found"));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill with id " + skillId + " not found"));

        JobSkill jobSkill = new JobSkill();
        jobSkill.setJob(job);
        jobSkill.setSkill(skill);
        jobSkill.setImportance(importance);

        return jobSkillRepository.save(jobSkill);
    }

    public List<Skill> getSkillsByJob(Long jobId) {
        List<JobSkill> jobSkills = jobSkillRepository.findByJobId(jobId);
        return jobSkills.stream()
                .map(JobSkill::getSkill)
                .collect(Collectors.toList());
    }
}
