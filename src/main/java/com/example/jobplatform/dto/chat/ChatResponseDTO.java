package com.example.jobplatform.dto.chat;

import com.example.jobplatform.dto.JobResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatResponseDTO {

    private String answer;
    private List<JobResponseDTO> sourceJobs;
}
