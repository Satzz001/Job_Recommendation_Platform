package com.example.jobplatform.entity;

import com.example.jobplatform.enums.InteractionType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "user_job_interaction",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "job_id"})
)
public class UserJobInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @Enumerated(EnumType.STRING)
    private InteractionType interactionType;
}
