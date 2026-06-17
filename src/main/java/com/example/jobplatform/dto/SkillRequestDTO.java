package com.example.jobplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequestDTO {

    @NotBlank(message = "Skill name must not be blank")
    private String name;


}