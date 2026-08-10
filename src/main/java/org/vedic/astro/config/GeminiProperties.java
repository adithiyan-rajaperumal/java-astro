package org.vedic.astro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey = "";
    private boolean enabled = true;
    private boolean lifePredictionsEnabled = true;
    private boolean dailyBalanEnabled = true;
    private boolean matchingEnabled = true;
    private boolean pdfPredictionsEnabled = true;
    private String model = "gemini-3.6-flash";
    private double temperature = 0.4;
    private int thinkingBudget = 1024;

    public String getResolvedApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "";
        }
        String trimmed = apiKey.trim();
        if (trimmed.startsWith("enc:")) {
            try {
                byte[] decoded = Base64.getDecoder().decode(trimmed.substring(4));
                return new String(decoded, StandardCharsets.UTF_8).trim();
            } catch (Exception e) {
                return trimmed;
            }
        }
        return trimmed;
    }

    public boolean isFeatureEnabled() {
        String key = getResolvedApiKey();
        return enabled && !key.isEmpty();
    }

    public boolean isLifePredictionsEnabled() {
        return isFeatureEnabled() && lifePredictionsEnabled;
    }

    public boolean isDailyBalanEnabled() {
        return isFeatureEnabled() && dailyBalanEnabled;
    }

    public boolean isMatchingEnabled() {
        return isFeatureEnabled() && matchingEnabled;
    }

    public boolean isPdfPredictionsEnabled() {
        return isFeatureEnabled() && pdfPredictionsEnabled;
    }
}
