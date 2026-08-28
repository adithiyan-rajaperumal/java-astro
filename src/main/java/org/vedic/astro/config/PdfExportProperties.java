package org.vedic.astro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "pdf")
public class PdfExportProperties {
    private boolean includeLifeAnchors = true;
    private boolean includeAyurdaya = true;
    private boolean includeYogasDoshams = true;
    private boolean includeAyurvedicHealth = true;
    private boolean includeAiPredictions = true;
}
