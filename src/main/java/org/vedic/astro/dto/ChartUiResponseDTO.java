package org.vedic.astro.dto;

import lombok.Builder;
import lombok.Data;
import org.vedic.astro.model.DasaPeriod;

import java.util.List;

@Data
@Builder
public class ChartUiResponseDTO {

    private String name;
    private String localMeanTime;
    private ChartResponseDTO.BirthProfile birthProfile;
    private List<ChartResponseDTO.PositionDetail> d1Chart;
    private List<ChartResponseDTO.PositionDetail> d9Chart;
    private List<DasaPeriod> currentDasaTimeline;
}
