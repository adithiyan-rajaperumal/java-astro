package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyBalanDTO {
    private boolean enabled;
    private String message;
    private String targetDate;
    private String rasi;
    private String nakshatra;
    private String runningDasaBhukthi;
    private boolean chandrashtama;
    private String dailyNarrative;
    private String generalOutlook;
    private String careerWork;
    private String financeWealth;
    private String healthVitality;
    private String relationshipFamily;
    private String luckyColor;
    private String luckyNumber;
    private String favorableDirection;
    private String bestTimeWindow;
    private String dailyRemedy;
    private PredictionResponseDTO.TokenUsage tokenUsage;
}
