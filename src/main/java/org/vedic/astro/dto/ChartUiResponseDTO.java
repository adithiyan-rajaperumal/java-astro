package org.vedic.astro.dto;

import lombok.Builder;
import lombok.Data;
import org.vedic.astro.model.DasaPeriod;

import java.util.List;

@Data
@Builder
public class ChartUiResponseDTO {

    private String name;
    private String dateOfBirth;   // <-- Added actual input date tracking
    private String timeOfBirth;   // <-- Added actual input time tracking
    private String localMeanTime; // <-- Holds the astronomical computed LMT
    private ChartResponseDTO.BirthProfile birthProfile;
    private List<ChartResponseDTO.PositionDetail> d1Chart;
    private List<ChartResponseDTO.PositionDetail> d9Chart;
    private List<DasaPeriod> currentDasaTimeline;
    // Core Panchangam Element Block
    private String thithi;
    private String yogam;
    private String karanam;
}
