package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionResponseDTO {
    private boolean enabled;
    private String message;
    private String overallSummary;
    private TokenUsage tokenUsage;
    private NativePersonality nativePersonality;
    private HealthAnalysis healthAnalysis;
    private List<AiYoga> aiYogas;
    private List<AiDosham> aiDoshams;
    private List<PastMilestone> pastMilestones;
    private List<YearlyPrediction> futurePredictions;
    private List<YearlyPrediction> lifetimePredictions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NativePersonality {
        private String coreTemperament;
        private List<String> keyStrengths;
        private List<String> vulnerabilitiesAndKarmicLessons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HealthAnalysis {
        private String ayurvedicConstitution;
        private List<String> organVulnerabilities;
        private String longevityVitalitySummary;
        private List<String> recommendedDietAndLifestyle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenUsage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private double estimatedCostUsd;
        private double estimatedCostInr;
        private String modelUsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiYoga {
        private String name;
        private String formingPlanets;
        private String impact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiDosham {
        private String name;
        private String status;
        private String nullificationFactor;
        private String remedy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PastMilestone {
        private int year;
        private int age;
        private String dasaBhukthi;
        private String milestoneTitle;
        private String nature; // POSITIVE, CHALLENGING, NEUTRAL
        private String description;
        private String astrologicalFactor;
        private boolean verified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YearlyPrediction {
        private int year;
        private int age;
        private String dasaBhukthi;
        private String personalMindset;
        private String careerProfession;
        private String careerFinance; // Backwards compatibility
        private String wealthFinance;
        private String healthVitality;
        private String marriageFamily;
        private String familyMarriage; // Backwards compatibility
        private String parentsKids;
        private String favorableVsCaution;
        private String remediesGuidance;
    }
}
