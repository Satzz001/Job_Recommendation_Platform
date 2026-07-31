package com.example.jobplatform.repository;

import com.example.jobplatform.entity.UserJobInteraction;
import com.example.jobplatform.enums.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserJobInteractionRepository extends JpaRepository<UserJobInteraction, Long> {
    List<UserJobInteraction> findByUserIdAndInteractionType(Long userId, InteractionType interactionType);
    Optional<UserJobInteraction> findByUserIdAndJobId(Long userId, Long jobId);
}