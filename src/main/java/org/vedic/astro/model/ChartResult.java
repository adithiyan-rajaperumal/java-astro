package org.vedic.astro.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartResult {
    private String name;
    private String localMeanTime;
    private String dateOfBirth;
    private String timeOfBirth;
    private double julianDayUT;
    private Map<String, PlanetaryPosition> d1Positions;
    private Map<String, PlanetaryPosition> d9Positions;
}
