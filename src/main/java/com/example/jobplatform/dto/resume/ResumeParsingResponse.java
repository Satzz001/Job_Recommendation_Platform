package com.example.jobplatform.dto.resume;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParsingResponse {

    private boolean success;

    private String message;

    // profile fields that were saved to DB
    private List<String> fieldsUpdated;

    // fields AI could not find in resume
    private List<String> fieldsMissing;

    // skill names added to user_skills table
    private List<String> skillsAdded;

    private int skillsCount;
}