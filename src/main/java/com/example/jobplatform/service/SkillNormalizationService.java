package com.example.jobplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SkillNormalizationService {

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.normalization.model:meta-llama/llama-3.1-8b-instruct:free}")
    private String model;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PROMPT_TEMPLATE =
        "You are a skill name normalizer for a job platform.\n" +
        "Normalize the following skill name to its most common, official form.\n\n" +
        "Rules:\n" +
        "- Use the most widely recognized capitalization (e.g. \"JavaScript\" not \"javascript\")\n" +
        "- Use the full name, not abbreviations (e.g. \"Machine Learning\" not \"ML\")\n" +
        "- Use the official product name (e.g. \"Node.js\" not \"nodejs\" or \"node\")\n" +
        "- Return ONLY the normalized skill name — no explanation, no punctuation, no quotes\n\n" +
        "Skill to normalize: ";

    public String normalize(String rawSkillName) {
        if (rawSkillName == null || rawSkillName.isBlank()) return rawSkillName;

        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "user", "content", PROMPT_TEMPLATE + rawSkillName)
                )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost:8080")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            HttpResponse<String> httpResponse = HttpClient.newHttpClient()
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                log.warn("Skill normalization failed (HTTP {}), using raw name: {}", httpResponse.statusCode(), rawSkillName);
                return rawSkillName;
            }

            JsonNode root = objectMapper.readTree(httpResponse.body());
            String normalized = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText()
                .trim();

            log.debug("Normalized '{}' → '{}'", rawSkillName, normalized);
            return normalized.isBlank() ? rawSkillName : normalized;

        } catch (Exception e) {
            log.warn("Skill normalization error for '{}', using raw name: {}", rawSkillName, e.getMessage());
            return rawSkillName;
        }
    }
}