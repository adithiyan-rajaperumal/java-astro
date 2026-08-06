package org.vedic.astro.util;

public class TamilCalendarUtils {

    public record TamilDate(String yearName, String monthName, int monthIndex, int day, double dayFraction) {}

    // Array of 12 Tamil Month Names
    public static final String[] TAMIL_MONTHS = {
            "Chithirai", "Vaikasi", "Aani", "Aadi", "Avani", "Purattasi",
            "Aippasi", "Karthigai", "Margazhi", "Thai", "Maasi", "Panguni"
    };

    // Vararuchi Month Span Days
    public static final double[] SURYA_VAKYA_MONTH_DAYS = {
            30.93, 31.41, 31.62, 31.47, 31.02, 30.45,
            29.93, 29.54, 29.41, 29.57, 29.98, 30.49
    };

    /**
     * Maps Gregorian Date to Tamil Solar Month and Day.
     * Uses Chithirai 1 (Mesha Sankranti) anchor offset.
     */
    public static TamilDate getTamilDate(int year, int month, int day, double hourFraction) {
        boolean isLeap = java.time.Year.isLeap(year);
        int[] daysBeforeMonth = isLeap
                ? new int[] { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 }
                : new int[] { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };

        int gregDayOfYear = daysBeforeMonth[month - 1] + day;
        int chithirai1DayOfYear = isLeap ? 105 : 104; // April 14

        int daysFromChithirai1 = (gregDayOfYear >= chithirai1DayOfYear)
                ? (gregDayOfYear - chithirai1DayOfYear)
                : (gregDayOfYear + (isLeap ? 366 : 365) - chithirai1DayOfYear);

        double totalDays = daysFromChithirai1 + (hourFraction / 24.0);

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

        return new TamilDate("Samvatsara", TAMIL_MONTHS[solarMonthIdx], solarMonthIdx, tamilDay, dayOffsetInMonth);
    }
}
