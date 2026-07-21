package org.vedic.astro.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.vedic.astro.model.DasaPeriod;

@Data
@Builder
public class ComprehensiveReportDTO {
    private String name;
    private String dateOfBirth;
    private String timeOfBirth;
    private String localMeanTime;
    private double latitude;
    private double longitude;
    private String resolvedTimezone;

    // Core Panchangam Element Block
    private String thithi;
    private String yogam;
    private String karanam;

    private ChartResponseDTO.BirthProfile birthProfile;
    private List<ChartResponseDTO.PositionDetail> birthPlanetaryPositions;
    private Map<String, List<ChartResponseDTO.PositionDetail>> vargaChartsMap;
    private List<DasaPeriod> vimshottariTimeline;
    private ShadbalaDTO shadbalaStrengths;
    private DiagnosticsDTO structuralDiagnostics;
}
