package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private TokenUsage tokenUsage;

    // 1. Core Psychological & Behavioral Profile
    private PersonalityAndBehavior personalityAndBehavior;

    // 2. Retrospective Past Life Turning Points Till Date (2-3 milestones)
    private List<RetrospectivePastMilestone> retrospectivePastMilestones;

    // 3. Autonomous AI Longevity & Active Yogas Analysis
    private AiLongevityAnalysis aiLongevityAnalysis;

    // 4. Single Unified Year-by-Year Narrative Stream
    private List<YearlyPrediction> yearlyPredictions;

    // Metadata
    private String forecastMode; // "TEN_YEARS" or "LIFETIME"
    private int startYear;
    private int endYear;
    private int startAge;
    private int endAge;
    private int totalForecastYears;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PersonalityAndBehavior {
        private String coreTemperament;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RetrospectivePastMilestone {
        private String approxPeriod;
        private String milestoneTitle;
        private String eventNarrative;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiLongevityAnalysis {
        private int calculatedAyulCeiling;
        private String classification; // Poornayu / Madhyayu / Alpayu
        private String primarySpanRationale;
        private List<AiYogaItem> activeYogasIdentified;
        private List<AiDoshaItem> activeDoshasIdentified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiYogaItem {
        private String yogaName;
        private String effect;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiDoshaItem {
        private String doshaName;
        private String remedialAdvice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YearlyPrediction {
        private int year;
        private int age;
        @JsonProperty("dasaBhukthi")
        @JsonAlias({"activeDasaBhukthi", "dasaBhukthi", "runningDasa", "dasa_bhukthi", "dasa", "dasaPeriod"})
        private String dasaBhukthi;
        @JsonProperty("annualNarrative")
        @JsonAlias({"annualNarrative", "narrative", "annual_narrative", "prediction", "forecast"})
        private String annualNarrative;
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
}
