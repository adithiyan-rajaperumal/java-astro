package org.vedic.astro.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vedic.astro.dto.ChartUiResponseDTO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchingResponseDTO {
    private double totalScore;
    private double maxScore;
    private double percentage;
    private String verdict;
    private List<KootaResultDTO> kootas;
    private List<String> warnings;
    private ChartUiResponseDTO boyProfile;
    private ChartUiResponseDTO girlProfile;
}
