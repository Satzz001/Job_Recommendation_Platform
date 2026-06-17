package com.example.jobplatform.adobe.util;

import com.example.jobplatform.adobe.config.AdobeCredentialsProvider;
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
import java.util.Map;

@Slf4j
@Component
public class AdobeAuthenticationManager {

    @Autowired
    private AdobeCredentialsProvider credentialsProvider;

    @Value("${adobe.client.timeout:30000}")
    private int clientTimeout;

    private String cachedBearerToken;
    private long tokenExpirationTime;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public String getBearerToken() {
        if (isTokenValid()) {
            log.debug("Reusing cached Bearer token ({} min left)",
                (tokenExpirationTime - System.currentTimeMillis()) / 60000);
            return cachedBearerToken;
        }

        log.info("Fetching new Bearer token from Adobe IMS (OAuth client credentials)");
        try {
            String bearerToken = exchangeClientCredentialsForBearerToken();

            this.cachedBearerToken = bearerToken;
            log.info("Bearer token obtained and cached");
            return bearerToken;

        } catch (Exception e) {
            log.error("Adobe authentication failed", e);
            throw new RuntimeException("Adobe authentication failed: " + e.getMessage(), e);
        }
    }

    public void refreshToken() {
        log.info("Clearing cached Bearer token to force refresh");
        this.cachedBearerToken = null;
        this.tokenExpirationTime = 0;
    }

    private String exchangeClientCredentialsForBearerToken() throws Exception {
        String imsUrl = credentialsProvider.getImsUrl();
        String clientId = credentialsProvider.getClientId();
        String clientSecret = credentialsProvider.getClientSecret();
        String scope = credentialsProvider.getScope();

        String body = "client_id=" + clientId +
                      "&client_secret=" + clientSecret +
                      "&grant_type=client_credentials" +
                      "&scope=" + scope;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(imsUrl + "/ims/token/v3"))
            .timeout(Duration.ofMillis(clientTimeout))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build();

        log.debug("Calling Adobe IMS: {}/ims/token/v3", imsUrl);

        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString());

        log.debug("Adobe IMS responded with HTTP {}", response.statusCode());

        if (response.statusCode() != 200) {
            log.error("Adobe IMS error (HTTP {}): {}", response.statusCode(), response.body());
            throw new RuntimeException("Adobe IMS authentication failed: HTTP " + response.statusCode());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
        String accessToken = (String) responseMap.get("access_token");

        if (accessToken == null || accessToken.isBlank()) {
            throw new RuntimeException("Adobe IMS response missing access_token field");
        }

        updateTokenExpiration(responseMap);
        return accessToken;
    }

    private void updateTokenExpiration(Map<String, Object> responseMap) {
        Object expiresIn = responseMap.get("expires_in");
        long now = System.currentTimeMillis();

        if (expiresIn instanceof Number) {
            long expiresInSeconds = ((Number) expiresIn).longValue();
            long bufferSeconds = Math.min(300, Math.max(30, expiresInSeconds / 10));
            this.tokenExpirationTime = now + ((expiresInSeconds - bufferSeconds) * 1000);
            return;
        }

        // Fallback: cache for 55 minutes when expiry is missing
        this.tokenExpirationTime = now + (55 * 60 * 1000);
    }

    private boolean isTokenValid() {
        return cachedBearerToken != null
            && !cachedBearerToken.isBlank()
            && System.currentTimeMillis() < tokenExpirationTime;
    }
}