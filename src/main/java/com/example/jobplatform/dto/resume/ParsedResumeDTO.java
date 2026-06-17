package com.example.jobplatform.dto.resume;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedResumeDTO {

    // maps to User.name
    private String name;

    // maps to User.email — service skips if null (keeps existing)
    private String email;

    // informational only — no DB column
    private String phone;

    // maps to user_skills table via skills.name
    private List<String> skills;

    // informational — no DB column, used to build context
    private List<String> companies;

    // used to derive experience field — no direct DB column
    private Double totalYearsExperience;

    // maps to UserProfile.experience — one of: FRESHER, JUNIOR, MID, SENIOR
    private String experience;

    // maps to UserProfile.preferredLocation
    private String preferredLocation;

    // maps to UserProfile.jobType — one of: REMOTE, ONSITE, HYBRID
    private String jobType;

    // maps to UserProfile.summary
    private String summary;

    // maps to UserProfile.college
    private String college;

    // maps to UserProfile.degree
    private String degree;

    // maps to UserProfile.graduationYear
    private Integer graduationYear;

    // AI lists field names it could not find
    private List<String> missingFields;
}