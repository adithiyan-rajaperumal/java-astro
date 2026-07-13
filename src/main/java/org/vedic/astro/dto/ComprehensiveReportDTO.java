package org.vedic.astro.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.vedic.astro.model.DasaPeriod;

@Data
@Builder
public class ComprehensiveReportDTO {
    private String name;
    private String dateOfBirth;
    private String timeOfBirth;
    private double latitude;
    private double longitude;
    private String resolvedTimezone;
    private ChartResponseDTO.BirthProfile birthProfile;
    private List<ChartResponseDTO.PositionDetail> birthPlanetaryPositions;
    private List<List<ChartResponseDTO.PositionDetail>> vargaChartsSuite;
    private List<DasaPeriod> vimshottariTimeline;
    private ShadbalaDTO shadbalaStrengths;
    private DiagnosticsDTO structuralDiagnostics;
}
