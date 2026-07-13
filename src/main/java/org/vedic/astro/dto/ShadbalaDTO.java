package org.vedic.astro.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ShadbalaDTO {
    private Map<String, PlanetaryStrength> planetStrengths;

    @Data
    @Builder
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
