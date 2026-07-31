package com.example.jobplatform.service;

import com.example.jobplatform.enums.InteractionType;
import com.example.jobplatform.entity.Job;
import com.example.jobplatform.entity.User;
import com.example.jobplatform.entity.UserJobInteraction;
import com.example.jobplatform.exception.ResourceNotFoundException;
import com.example.jobplatform.repository.JobRepository;
import com.example.jobplatform.repository.UserJobInteractionRepository;
import com.example.jobplatform.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserJobInteractionService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserJobInteractionRepository interactionRepository;

    public UserJobInteractionService(UserRepository userRepository,
                                     JobRepository jobRepository,
                                     UserJobInteractionRepository interactionRepository) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.interactionRepository = interactionRepository;
    }

    public UserJobInteraction interact(Long userId, Long jobId, InteractionType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job with id " + jobId + " not found"));

        UserJobInteraction interaction = interactionRepository.findByUserIdAndJobId(userId, jobId)
                .orElseGet(UserJobInteraction::new);
        interaction.setUser(user);
        interaction.setJob(job);
        interaction.setInteractionType(type);

        return interactionRepository.save(interaction);
    }
}
