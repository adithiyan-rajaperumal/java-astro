package org.vedic.astro.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanetaryPosition {
    private String name;
    private double absoluteLongitude;
    private int signNumber;
    private String signName;
    private String rashi;
    private String nakshatra;
    private int pada;
    private double degreeInSign;
    private double speed;
}
