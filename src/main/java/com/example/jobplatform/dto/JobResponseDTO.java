package com.example.jobplatform.dto;

import com.example.jobplatform.enums.ExperienceLevel;
import com.example.jobplatform.enums.JobType;
import lombok.Data;

@Data
public class JobResponseDTO {

    private Long id;
    private String title;
    private String companyName;
    private String location;
    private JobType jobType;
    private ExperienceLevel experienceLevel;
    private double matchScore;
    private String description;
    private String applyUrl;

}