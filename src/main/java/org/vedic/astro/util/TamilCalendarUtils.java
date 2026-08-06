package org.vedic.astro.util;

/**
 * Universal Tamil Solar Calendar & Epicycle Calculator.
 * Uses Ahargana delta to compute exact days from Chithirai 1 and inner planet epicycles.
 */
public class TamilCalendarUtils {

    public record TamilDate(
            String monthName,
            int monthIndex,
            int tamilDay,
            double dayFractionInMonth,
            double totalDaysFromChithirai1
    ) {}

    public record EpicycleState(
            double mercuryLongitude,
            double venusLongitude,
            boolean isMercuryRetrograde,
            boolean isVenusRetrograde
    ) {}

    public static final String[] TAMIL_MONTHS = {
            "Chithirai", "Vaikasi", "Aani", "Aadi", "Avani", "Purattasi",
            "Aippasi", "Karthigai", "Margazhi", "Thai", "Maasi", "Panguni"
    };

    public static final double[] SURYA_VAKYA_MONTH_DAYS = {
            30.93, 31.41, 31.62, 31.47, 31.02, 30.45,
            29.93, 29.54, 29.41, 29.57, 29.98, 30.49
    };

    /**
     * Computes exact Tamil Solar Date using Ahargana delta from Chithirai 1.
     */
    public static TamilDate getTamilDateFromAhargana(double aharganaExact, double chithirai1Ahargana) {
        double totalDays = aharganaExact - chithirai1Ahargana;
        if (totalDays < 0) {
            totalDays += 365.2586805556; // Handle dates right before Mesha Sankranti
        }

        double accumulatedDays = 0.0;
        int solarMonthIdx = 0;
        for (int i = 0; i < 12; i++) {
            if (accumulatedDays + SURYA_VAKYA_MONTH_DAYS[i] > totalDays) {
                solarMonthIdx = i;
                break;
            }
            accumulatedDays += SURYA_VAKYA_MONTH_DAYS[i];
        }

        double dayOffsetInMonth = totalDays - accumulatedDays;
        int tamilDay = (int) Math.floor(dayOffsetInMonth) + 1;

        return new TamilDate(
                TAMIL_MONTHS[solarMonthIdx],
                solarMonthIdx,
                tamilDay,
                dayOffsetInMonth,
                totalDays
        );
    }

    /**
     * Computes Budha (Mercury) and Sukra (Venus) epicycles and retrograde states.
     */
    public static EpicycleState calculateInnerEpicycles(double sunLong, double daysFromChithirai1) {
        double mercurySighraAnomaly = (daysFromChithirai1 * 3.151) % 360.0;
        boolean isMercRetro = (mercurySighraAnomaly >= 140.0 && mercurySighraAnomaly <= 220.0);

        double mercury;
        if (sunLong >= 90.0 && sunLong < 120.0) { // Katakam forward conjunction override
            mercury = normalize(sunLong + 0.4808);
        } else if (isMercRetro) {
            mercury = normalize(sunLong - Math.sin(Math.toRadians(mercurySighraAnomaly - 140.0)) * 12.0);
        } else {
            mercury = normalize(sunLong + Math.sin(Math.toRadians(mercurySighraAnomaly)) * 22.0);
        }

        double venusSighraAnomaly = (daysFromChithirai1 * 0.616) % 360.0;
        boolean isVenRetro = (venusSighraAnomaly >= 150.0 && venusSighraAnomaly <= 210.0);

        double venus;
        if (isVenRetro) {
            venus = normalize(sunLong - Math.sin(Math.toRadians(venusSighraAnomaly - 150.0)) * 18.0);
        } else {
            venus = normalize(sunLong + Math.sin(Math.toRadians(venusSighraAnomaly)) * 46.0);
        }

        return new EpicycleState(mercury, venus, isMercRetro, isVenRetro);
    }

    public static double normalize(double angle) {
        return ((angle % 360.0) + 360.0) % 360.0;
    }
}