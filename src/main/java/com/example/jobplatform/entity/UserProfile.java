package com.example.jobplatform.entity;

import jakarta.persistence.*;
import lombok.Data;

import com.example.jobplatform.enums.ExperienceLevel;
import com.example.jobplatform.enums.JobType;


@Data
@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ExperienceLevel experience;

    private String preferredLocation;

    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private String college;

    private String degree;

    private Integer graduationYear;
}