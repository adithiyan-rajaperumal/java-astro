package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequestDTO {
    private BirthDetailsDTO birthDetails;
    private ChartUiResponseDTO chartData;
    private String language;
}
