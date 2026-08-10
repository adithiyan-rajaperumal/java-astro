package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChartResponseDTO {
    private String name;
    private String dateOfBirth;
    private String localMeanTime;
    private double julianDayUT;
    private BirthProfile birthProfile;
    private List<PositionDetail> d1Chart;
    private List<PositionDetail> d9Chart;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BirthProfile {
        private String lagna;
        private String rashi;
        private String nakshatra;
        private int nakshatraPada;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PositionDetail {
        private String planetKey;
        private String displayName;
        private int signNumber;
        private String rashiName;
        private double degreeInSign;
        private String formattedDegree;
    }
}
