package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBalanRequestDTO {
    private BirthDetailsDTO birthDetails;
    private ChartUiResponseDTO chartData;
    private String targetDate;
    private String language;
    private boolean forceRefresh;
}
