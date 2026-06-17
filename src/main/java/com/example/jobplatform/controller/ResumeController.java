package com.example.jobplatform.controller;

import com.example.jobplatform.adobe.service.AdobePDFService;
import com.example.jobplatform.dto.resume.ResumeParsingResponse;
import com.example.jobplatform.service.ResumeParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private static final long RATE_LIMIT_COOLDOWN_MS = 60_000; // 1 minute per user
    private final Map<Long, Long> lastUploadTime = new ConcurrentHashMap<>();

    @Autowired
    private AdobePDFService adobePDFService;

    @Autowired
    private ResumeParsingService resumeParsingService;

    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(
            @RequestParam(name = "userId") Long userId,
            @RequestParam(name = "file") MultipartFile file) {

        long startTime = System.currentTimeMillis();

        try {
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid userId. Must be a positive integer.", System.currentTimeMillis()));
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("No file provided. Please upload a PDF resume.", System.currentTimeMillis()));
            }

            long now = System.currentTimeMillis();
            Long last = lastUploadTime.get(userId);
            if (last != null && (now - last) < RATE_LIMIT_COOLDOWN_MS) {
                long secondsLeft = (RATE_LIMIT_COOLDOWN_MS - (now - last)) / 1000;
                log.warn("Rate limit hit — userId: {}, retry in {}s", userId, secondsLeft);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErrorResponse(
                        "Too many requests. Wait " + secondsLeft + " seconds before uploading again.",
                        now
                    ));
            }
            lastUploadTime.put(userId, now);

            log.info("Resume upload request — userId: {}, file: {}, size: {} bytes",
                userId, file.getOriginalFilename(), file.getSize());

            // Step 1: Extract raw text from the uploaded PDF
            String extractedText = adobePDFService.extractTextFromPDF(file);
            log.info("Text extraction done — {} chars in {}ms",
                extractedText.length(), System.currentTimeMillis() - startTime);

            // Step 2: Send text to AI, parse result, save to DB, return summary
            ResumeParsingResponse response = resumeParsingService.parseAndSave(userId, extractedText);

            log.info("Full resume pipeline completed in {}ms for userId={}",
                System.currentTimeMillis() - startTime, userId);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.warn("File validation error: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage(), System.currentTimeMillis(), processingTime));

        } catch (IllegalStateException e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.warn("Service configuration error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(e.getMessage(), System.currentTimeMillis(), processingTime));

        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Error during resume parsing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                    "Failed to parse resume: " + e.getMessage(),
                    System.currentTimeMillis(), processingTime, e.getClass().getSimpleName()
                ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(new HealthCheckResponse("Resume service is running", System.currentTimeMillis()));
    }

    public static class ErrorResponse {
        @JsonProperty("success") public boolean success = false;
        @JsonProperty("error") public String error;
        @JsonProperty("timestamp") public long timestamp;
        @JsonProperty("processingTimeMs") public long processingTimeMs;
        @JsonProperty("errorType") public String errorType;

        public ErrorResponse(String error, long timestamp) {
            this.error = error; this.timestamp = timestamp;
        }
        public ErrorResponse(String error, long timestamp, long processingTimeMs) {
            this.error = error; this.timestamp = timestamp; this.processingTimeMs = processingTimeMs;
        }
        public ErrorResponse(String error, long timestamp, long processingTimeMs, String errorType) {
            this.error = error; this.timestamp = timestamp;
            this.processingTimeMs = processingTimeMs; this.errorType = errorType;
        }
    }

    public static class HealthCheckResponse {
        @JsonProperty("status") public String status;
        @JsonProperty("timestamp") public long timestamp;

        public HealthCheckResponse(String status, long timestamp) {
            this.status = status; this.timestamp = timestamp;
        }
    }
}
