package com.example.jobplatform.service;

import com.example.jobplatform.dto.resume.ParsedResumeDTO;
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

/**
 * Sends extracted resume text to OpenRouter AI and deserializes the JSON
 * response into a ParsedResumeDTO.
 *
 * Uses java.net.http.HttpClient (same as AdobeAPIClient — no extra dependency).
 */
@Slf4j
@Service
public class OpenRouterService {

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.model}")
    private String model;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    // Instructions to the AI — field names match ParsedResumeDTO exactly so
    // Jackson can deserialize the response with zero manual mapping.
    private static final String SYSTEM_PROMPT =
        "You are a resume parser. Extract information from the resume text provided by the user.\n\n" +
        "Return ONLY a valid JSON object with exactly these fields. No extra text. No markdown. No explanation.\n\n" +
          "{\n" +
        "  \"name\": \"full name string or null\",\n" +
        "  \"email\": \"email string or null\",\n" +
        "  \"phone\": \"phone string or null\",\n" +
        "  \"skills\": [\"array of technical skill name strings\"],\n" +
        "  \"companies\": [\"array of company name strings\"],\n" +
        "  \"totalYearsExperience\": \"number or null\",\n" +
        "  \"experience\": \"one of FRESHER, JUNIOR, MID, SENIOR — or null\",\n" +
        "  \"preferredLocation\": \"city or location string or null\",\n" +
        "  \"jobType\": \"one of REMOTE, ONSITE, HYBRID — never null, default REMOTE\",\n" +
        "  \"summary\": \"2-3 sentence professional summary synthesized from the resume — NEVER null, always generate from available information\",\n" +
        "  \"college\": \"institution/university name or null\",\n" +
        "  \"degree\": \"degree and field e.g. B.Tech Computer Science or null\",\n" +
        "  \"graduationYear\": \"4-digit year as integer or null\",\n" +
        "  \"missingFields\": [\"names of fields you could not find\"]\n" +
        "}\n\n" +
        "Rules:\n" +
        "- experience: FRESHER = 0-1 yrs, JUNIOR = 1-3 yrs, MID = 3-6 yrs, SENIOR = 6+ yrs\n" +
        "- skills: technical skills only (Java, MySQL, Docker etc) — no soft skills\n" +
        "- jobType: infer from context; if unclear or not mentioned, always default to REMOTE\n" +
        "- summary: ALWAYS generate — never null — write in third person from available resume info, highlight key skills and experience level\n" +
        "- graduationYear: integer only — no quotes, no text\n" +
        "- missingFields: include field name if value is null\n" +
        "- null means not found — never use empty string";

    /**
     * Sends the raw resume text to OpenRouter and parses the AI's JSON reply
     * into a ParsedResumeDTO.
     *
     * @param resumeText plain text extracted from the uploaded PDF
     * @return ParsedResumeDTO with whatever fields the AI could find
     * @throws RuntimeException if the HTTP call fails or the response isn't valid JSON
     */
    public ParsedResumeDTO parse(String resumeText) {
        log.info("Sending resume text to OpenRouter (model: {}, {} chars)", model, resumeText.length());
        String aiContent = callChatCompletion(SYSTEM_PROMPT, resumeText);

        try {
            // Deserialize the AI's JSON string directly into ParsedResumeDTO.
            // @JsonIgnoreProperties(ignoreUnknown = true) on the DTO handles any extra fields.
            ParsedResumeDTO dto = objectMapper.readValue(aiContent, ParsedResumeDTO.class);
            log.info("OpenRouter parsing complete — skills found: {}, experience: {}",
                dto.getSkills() != null ? dto.getSkills().size() : 0, dto.getExperience());
            return dto;
        } catch (Exception e) {
            log.error("Failed to parse OpenRouter response as ParsedResumeDTO", e);
            throw new RuntimeException("Failed to call OpenRouter API: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a system+user message pair to OpenRouter and returns the raw
     * text of the AI's reply (no JSON deserialization). Used for free-form
     * chat completions such as the job-search chat assistant.
     *
     * @param systemPrompt instructions establishing the AI's role/constraints
     * @param userMessage  the user-turn content
     * @return the AI's raw reply text
     * @throws RuntimeException if the HTTP call fails
     */
    public String chat(String systemPrompt, String userMessage) {
        return callChatCompletion(systemPrompt, userMessage);
    }

    private String callChatCompletion(String systemPrompt, String userMessage) {
        try {
            // Build the chat request body as a Java Map, then serialize to JSON
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user",   "content", userMessage)
                )
            );

            String requestJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type",  "application/json")
                // HTTP-Referer helps OpenRouter identify the app (optional but recommended)
                .header("HTTP-Referer",  "http://localhost:8080")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

            HttpResponse<String> httpResponse = httpClient
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());

            log.debug("OpenRouter responded with HTTP {}", httpResponse.statusCode());

            if (httpResponse.statusCode() != 200) {
                log.error("OpenRouter error (HTTP {}): {}", httpResponse.statusCode(), httpResponse.body());
                throw new RuntimeException("OpenRouter call failed due to an upstream error");
            }

            // Pull out choices[0].message.content — this is the AI's reply text
            JsonNode root = objectMapper.readTree(httpResponse.body());
            String aiContent = root
                .path("choices").get(0)
                .path("message")
                .path("content")
                .asText();

            log.debug("AI response content: {}", aiContent);
            return aiContent;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenRouter call failed", e);
            throw new RuntimeException("Failed to call OpenRouter API: " + e.getMessage(), e);
        }
    }
}
