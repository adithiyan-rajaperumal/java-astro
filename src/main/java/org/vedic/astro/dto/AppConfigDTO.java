package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigDTO {
    private boolean aiPredictionsEnabled;
    private boolean lifePredictionsEnabled;
    private boolean dailyBalanEnabled;
    private boolean pdfPredictionsEnabled;
    private String geminiModel;
    private double temperature;
    private int thinkingBudget;
}
