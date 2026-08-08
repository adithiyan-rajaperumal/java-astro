package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShadbalaDTO {
    private Map<String, PlanetaryStrength> planetStrengths;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlanetaryStrength {
        private double sthanaBala;
        private double digBala;
        private double kalaBala;
        private double cheshtaBala;
        private double drigBala;
        private double totalShadbalaRupas;
        private String strengthCategory;
    }
}
