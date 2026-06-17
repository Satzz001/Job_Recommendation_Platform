package com.example.jobplatform.entity;

import com.example.jobplatform.enums.ImportanceLevel;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "job_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "skill_id"}))
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "importance", nullable = false)
    private ImportanceLevel importance = ImportanceLevel.NICE_TO_HAVE;
}
