package com.example.jobplatform.repository;

import com.example.jobplatform.entity.Skill;
import com.example.jobplatform.entity.User;
import com.example.jobplatform.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUserId(Long userId);

    // Used by ResumeParsingService to skip skills the user already has
    boolean existsByUserAndSkill(User user, Skill skill);
}