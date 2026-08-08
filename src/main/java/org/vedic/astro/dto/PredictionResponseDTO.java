package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponseDTO {
    private boolean enabled;
    private String message;
    private String overallSummary;
    private List<AiYoga> aiYogas;
    private List<AiDosham> aiDoshams;
    private List<PastMilestone> pastMilestones;
    private List<YearlyPrediction> futurePredictions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiYoga {
        private String name;
        private String formingPlanets;
        private String impact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    public static class PastMilestone {
        private int year;
        private int age;
        private String dasaBhukthi;
        private String milestoneTitle;
        private String description;
        private String astrologicalFactor;
        private boolean verified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyPrediction {
        private int year;
        private int age;
        private String dasaBhukthi;
        private String careerFinance;
        private String healthVitality;
        private String familyMarriage;
        private String remediesGuidance;
    }
}
