package com.example.jobplatform.repository;

import com.example.jobplatform.entity.UserJobInteraction;
import com.example.jobplatform.enums.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserJobInteractionRepository extends JpaRepository<UserJobInteraction, Long> {
    List<UserJobInteraction> findByUserIdAndInteractionType(Long userId, InteractionType interactionType);
}