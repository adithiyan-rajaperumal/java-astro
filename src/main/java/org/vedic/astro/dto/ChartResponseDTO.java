package org.vedic.astro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
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
    public static class BirthProfile {
        private String lagna;
        private String rashi;
        private String nakshatra;
        private int nakshatraPada;
    }

    @Data
    @Builder
    public static class PositionDetail {
        private String planetKey;
        private String displayName;
        private int signNumber;
        private String rashiName;
        private double degreeInSign;
        private String formattedDegree;
    }
}
