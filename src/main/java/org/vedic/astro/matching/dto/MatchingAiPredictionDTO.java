package org.vedic.astro.matching.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vedic.astro.dto.PredictionResponseDTO.TokenUsage;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchingAiPredictionDTO {
    private boolean enabled;
    private String message;
    private String overallVerdict; // EXCELLENT, VERY_GOOD, GOOD, AVERAGE, NOT_RECOMMENDED
    private double compatibilityPercentage; // 0 - 100
    private String executiveSummary;
    private TokenUsage tokenUsage;

    // Compatibility Domain Breakdowns
    private DomainAnalysis emotionalMentalHarmony;
    private DomainAnalysis healthLongevityNadi;
    private DomainAnalysis careerFinancialSynergy;
    private DomainAnalysis progenyFamilyLineage;
    private DomainAnalysis doshaPapasamyaParity;

    private List<String> keyStrengths;
    private List<String> growthAreasAndCautions;
    private List<String> authenticVedicRemedies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DomainAnalysis {
        private String title;
        private String scoreOrStatus; // e.g. "90% (High Harmony)"
        private String analysis;
        private String astrologicalBasis;
    }
}
