package com.example.jobplatform.dto;

import com.example.jobplatform.enums.ExperienceLevel;
import com.example.jobplatform.enums.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JobRequestDTO {

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotBlank(message = "Company name must not be blank")
    private String companyName;

    @NotBlank(message = "Description must not be blank")
    @Size(max = 2000, message = "Description must be <= 2000 characters")
    private String description;

    @NotBlank(message = "Location must not be blank")
    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    @NotNull(message = "Experience level is required")
    private ExperienceLevel experienceLevel;

    @NotBlank(message = "Apply URL must not be blank")
    private String applyUrl;

    @NotBlank(message = "Source must not be blank")
    private String source;


}