package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.vedic.astro.model.DasaPeriod;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartUiResponseDTO {

    private String name;
    private String dateOfBirth;   // <-- Added actual input date tracking
    private String timeOfBirth;   // <-- Added actual input time tracking
    private String localMeanTime; // <-- Holds the astronomical computed LMT
    private double latitude;
    private double longitude;
    private String resolvedTimezone;
    private String ayanamsa;
    private String panchangamSystem;

    private ChartResponseDTO.BirthProfile birthProfile;
    private List<ChartResponseDTO.PositionDetail> d1Chart;
    private List<ChartResponseDTO.PositionDetail> d9Chart;
    private List<ChartResponseDTO.PositionDetail> bhavaChart;
    private List<DasaPeriod> currentDasaTimeline;
    private ShadbalaDTO shadbalaStrengths;
    private DiagnosticsDTO structuralDiagnostics;

    // Core Panchangam Element Block
    private String thithi;
    private String yogam;
    private String karanam;

    // Health & Ayurdaya Longevity Engine
    private org.vedic.astro.util.AyurvedicAstrologyUtils.AyurvedicHealthProfile ayurvedicHealth;
    private org.vedic.astro.util.AyurdayaCalculationUtils.AyurdayaProfile ayurdayaProfile;

    // Personal Elements, Deities & Life Anchors Engine
    private LifeAnchorsProfile lifeAnchors;

    // Feature Toggles
    private boolean lifeAnchorsEnabled;
    private boolean ayurdayaEnabled;

    // AI Prediction Capabilities
    private boolean aiPredictionsEnabled;
}
