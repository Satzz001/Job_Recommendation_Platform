package com.example.jobplatform.repository;

import com.example.jobplatform.entity.JobSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {

    List<JobSkill> findByJobId(Long jobId);

    List<JobSkill> findByJobIdIn(List<Long> jobIds);

    List<JobSkill> findBySkillIdIn(List<Long> skillIds);
}