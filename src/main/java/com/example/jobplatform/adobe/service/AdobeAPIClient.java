package com.example.jobplatform.adobe.service;

import com.example.jobplatform.adobe.util.AdobeAuthenticationManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AdobeAPIClient {

    @Autowired
    private AdobeAuthenticationManager authManager;

    @Value("${adobe.pdfservices.api.url:https://pdf-services-api.adobe.io}")
    private String adobeApiUrl;

    @Value("${adobe.client.timeout:30000}")
    private int clientTimeout;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int MAX_POLL_ATTEMPTS = 120; // 120 × 5 s = 10 minutes
    private static final int POLL_WAIT_MS = 5000;

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    public UploadResponse uploadPDF(byte[] pdfBytes, String filename) {
        try {
            log.info("Uploading PDF to Adobe ({} bytes, file: {})", pdfBytes.length, filename);

            String bearerToken = authManager.getBearerToken();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(adobeApiUrl + "/pdf/operation/createpdfoperation"))
                .timeout(Duration.ofMillis(clientTimeout))
                .header("Authorization", "Bearer " + bearerToken)
                .header("Content-Type", "application/octet-stream")
                .POST(HttpRequest.BodyPublishers.ofByteArray(pdfBytes))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            log.debug("Adobe upload responded with HTTP {}", response.statusCode());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                log.error("Adobe API upload error (HTTP {}): {}", response.statusCode(), response.body());
                if (response.statusCode() == 401) {
                    authManager.refreshToken();
                }
                throw new RuntimeException("Adobe PDF upload failed: HTTP " + response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(response.body(), Map.class);

            String assetID = (String) map.get("assetID");
            String uploadURL = (String) map.get("uploadURL");
            String pollURL = (String) map.get("pollURL");

            log.info("PDF uploaded successfully. Asset ID: {}...",
                assetID.substring(0, Math.min(20, assetID.length())));

            return new UploadResponse(assetID, uploadURL, pollURL);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload PDF to Adobe", e);
            throw new RuntimeException("PDF upload failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Poll
    // -------------------------------------------------------------------------

    public void pollForCompletion(String assetID, String pollURL) {
        log.info("Polling Adobe for processing status (max {} attempts)", MAX_POLL_ATTEMPTS);

        try {
            String bearerToken = authManager.getBearerToken();
            HttpClient httpClient = HttpClient.newHttpClient();

            for (int attempt = 1; attempt <= MAX_POLL_ATTEMPTS; attempt++) {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(pollURL))
                    .timeout(Duration.ofMillis(clientTimeout))
                    .header("Authorization", "Bearer " + bearerToken)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new RuntimeException("Status check failed: HTTP " + response.statusCode());
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(response.body(), Map.class);
                String status = (String) map.get("status");

                log.debug("Poll attempt {}/{}: status = {}", attempt, MAX_POLL_ATTEMPTS, status);

                if ("done".equals(status)) {
                    log.info("PDF processing completed after {} poll attempt(s)", attempt);
                    return;
                }

                if ("failed".equals(status)) {
                    throw new RuntimeException("Adobe PDF processing failed (status=failed)");
                }

                if (attempt < MAX_POLL_ATTEMPTS) {
                    Thread.sleep(POLL_WAIT_MS);
                }
            }

            throw new RuntimeException("PDF processing timed out after 10 minutes");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Polling interrupted", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to poll Adobe API", e);
            throw new RuntimeException("Polling failed: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------
    // Download
    // -------------------------------------------------------------------------

    public String downloadExtractedText(String assetID) {
        try {
            log.info("Downloading extracted text from Adobe for asset {}", assetID);

            String bearerToken = authManager.getBearerToken();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(adobeApiUrl + "/pdf/output/" + assetID))
                .timeout(Duration.ofMillis(clientTimeout))
                .header("Authorization", "Bearer " + bearerToken)
                .GET()
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            log.debug("Adobe download responded with HTTP {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("Download failed (HTTP {}): {}", response.statusCode(), response.body());
                throw new RuntimeException("Download failed: HTTP " + response.statusCode());
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(response.body(), Map.class);
            String text = combineTextElements(map);

            log.info("Extracted {} characters from Adobe response", text.length());
            return text;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download extracted text from Adobe", e);
            throw new RuntimeException("Download failed: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private String combineTextElements(Map<String, Object> responseMap) {
        Object elementsObj = responseMap.get("elements");
        if (!(elementsObj instanceof List)) {
            log.warn("No 'elements' array found in Adobe response");
            return "";
        }

        List<Object> elements = (List<Object>) elementsObj;
        log.debug("Found {} text elements in Adobe response", elements.size());

        StringBuilder sb = new StringBuilder();
        for (Object element : elements) {
            if (element instanceof Map) {
                String text = (String) ((Map<String, Object>) element).get("Text");
                if (text != null && !text.isBlank()) {
                    if (!sb.isEmpty()) {
                        sb.append("\n");
                    }
                    sb.append(text);
                }
            }
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Inner result type
    // -------------------------------------------------------------------------

    public static class UploadResponse {
        public final String assetID;
        public final String uploadURL;
        public final String pollURL;

        public UploadResponse(String assetID, String uploadURL, String pollURL) {
            this.assetID = assetID;
            this.uploadURL = uploadURL;
            this.pollURL = pollURL;
        }
    }
}