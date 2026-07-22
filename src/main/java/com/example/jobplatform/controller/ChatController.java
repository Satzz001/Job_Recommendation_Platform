package com.example.jobplatform.controller;

import com.example.jobplatform.dto.chat.ChatRequestDTO;
import com.example.jobplatform.dto.chat.ChatResponseDTO;
import com.example.jobplatform.service.JobChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/{userId}/chat")
public class ChatController {

    private final JobChatService jobChatService;

    public ChatController(JobChatService jobChatService) {
        this.jobChatService = jobChatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponseDTO> chat(@PathVariable Long userId,
                                                @Valid @RequestBody ChatRequestDTO request) {
        return ResponseEntity.ok(jobChatService.answer(userId, request.getMessage()));
    }
}
