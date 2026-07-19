package org.vedic.astro.matching.model;

import lombok.Getter;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.matching.StrictnessLevel;
import org.vedic.astro.util.ZodiacUtils;

@Getter
public class MatchingContext {
    private final ChartResult boyChart;
    private final ChartResult girlChart;
    private final BirthDetailsDTO boyBirthDetails;
    private final BirthDetailsDTO girlBirthDetails;
    private final StrictnessLevel strictness;

    // Derived quick lookups
    private final int boyNakshatra;     // 1 to 27
    private final int girlNakshatra;    // 1 to 27
    private final int boyPada;          // 1 to 4
    private final int girlPada;         // 1 to 4
    private final int boyRashi;         // 1 to 12
    private final int girlRashi;        // 1 to 12
    private final int boyLagnaSign;     // 1 to 12
    private final int girlLagnaSign;    // 1 to 12

    public MatchingContext(ChartResult boyChart, ChartResult girlChart,
                           BirthDetailsDTO boyBirthDetails, BirthDetailsDTO girlBirthDetails,
                           StrictnessLevel strictness) {
        this.boyChart = boyChart;
        this.girlChart = girlChart;
        this.boyBirthDetails = boyBirthDetails;
        this.girlBirthDetails = girlBirthDetails;
        this.strictness = strictness != null ? strictness : StrictnessLevel.MODERATE;

        PlanetaryPosition boyMoon = boyChart.getD1Positions().get("Moon");
        PlanetaryPosition girlMoon = girlChart.getD1Positions().get("Moon");

        this.boyNakshatra = ZodiacUtils.getNakshatraNumber(boyMoon.getAbsoluteLongitude());
        this.girlNakshatra = ZodiacUtils.getNakshatraNumber(girlMoon.getAbsoluteLongitude());
        this.boyPada = boyMoon.getPada();
        this.girlPada = girlMoon.getPada();
        this.boyRashi = boyMoon.getSignNumber();
        this.girlRashi = girlMoon.getSignNumber();

        this.boyLagnaSign = boyChart.getD1Positions().get("Lagna").getSignNumber();
        this.girlLagnaSign = girlChart.getD1Positions().get("Lagna").getSignNumber();
    }
}
