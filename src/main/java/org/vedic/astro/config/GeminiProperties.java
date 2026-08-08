package org.vedic.astro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey = "";
    private boolean enabled = true;
    private String model = "gemini-1.5-flash";

    public boolean isFeatureEnabled() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }
}
