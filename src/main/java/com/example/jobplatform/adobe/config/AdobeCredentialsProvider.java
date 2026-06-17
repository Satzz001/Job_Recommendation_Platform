package com.example.jobplatform.adobe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class AdobeCredentialsProvider {

    @Value("${adobe.credentials.path:adobe-credentials.json}")
    private String credentialsPath;

    @Value("${adobe.credentials.environment-variable-name:ADOBE_CREDENTIALS_JSON}")
    private String envVariableName;

    @Value("${adobe.oauth.scope:}")
    private String oauthScope;

    private Map<String, Object> credentialsCache;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> getCredentials() {
        if (credentialsCache != null) {
            return new HashMap<>(credentialsCache);
        }

        try {
            String envCredentials = System.getenv(envVariableName);
            System.out.println("power: ahhhhh" + envCredentials);
            if (envCredentials != null && !envCredentials.trim().isEmpty()) {
                log.info("Loading Adobe credentials from environment variable: {}", envVariableName);
                credentialsCache = objectMapper.readValue(envCredentials, Map.class);
                validateCredentials();
                return new HashMap<>(credentialsCache);
            }

            log.info("Loading Adobe credentials from file: {}", credentialsPath);
            ClassPathResource resource = new ClassPathResource(credentialsPath);

            if (resource.exists()) {
                try (InputStream is = resource.getInputStream()) {
                    credentialsCache = objectMapper.readValue(is, Map.class);
                    validateCredentials();
                    log.info("Adobe credentials loaded successfully from file");
                    return new HashMap<>(credentialsCache);
                }
            }

            throw new IllegalStateException(
                String.format(
                    "Adobe credentials not found. Either set env var '%s' or place adobe-credentials.json in src/main/resources/",
                    envVariableName
                )
            );

        } catch (Exception e) {
            log.error("Failed to load Adobe credentials", e);
            throw new RuntimeException("Failed to load Adobe credentials. Check logs for details.", e);
        }
    }

    private void validateCredentials() {
        String[] requiredFields = {"client_id", "client_secret", "organization_id", "technical_account_id"};
        for (String field : requiredFields) {
            if (!credentialsCache.containsKey(field) || credentialsCache.get(field) == null) {
                throw new IllegalStateException("Missing required Adobe credential field: " + field);
            }
        }
        log.info("All required Adobe credential fields validated");
    }

    public String getClientId() {
        return (String) getCredentials().get("client_id");
    }

    public String getClientSecret() {
        return (String) getCredentials().get("client_secret");
    }

    public String getOrganizationId() {
        return (String) getCredentials().get("organization_id");
    }

    public String getTechnicalAccountId() {
        return (String) getCredentials().get("technical_account_id");
    }

    public String getScope() {
        if (oauthScope != null && !oauthScope.isBlank()) {
            return oauthScope;
        }

        Object scopesValue = getCredentials().get("scopes");
        if (scopesValue == null) {
            scopesValue = getCredentials().get("scope");
        }

        if (scopesValue instanceof String && !((String) scopesValue).isBlank()) {
            return (String) scopesValue;
        }

        return "ent_documentservices_sdk";
    }

    public String getImsUrl() {
        Object value = getCredentials().get("ims_url");
        return value != null ? String.valueOf(value) : "https://ims-na1.adobelogin.com";
    }

    public String getPrivateKey() {
        return (String) getCredentials().get("private_key");
    }
}
