package org.vedic.astro.util;

public class ZodiacUtils {
    private static final String[] ENGLISH_SIGNS = {
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    };

    private static final String[] VEDIC_RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Karka", "Simha", "Kanya",
            "Tula", "Vrischika", "Dhanu", "Makara", "Kumbha", "Meena"
    };

    private static final String[] NAKSHATRAS = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra",
            "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
            "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
            "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
            "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    };

    public static String getSignName(int signNumber) {
        return ENGLISH_SIGNS[signNumber - 1];
    }

    public static String getVedicRashi(int signNumber) {
        return VEDIC_RASHIS[signNumber - 1];
    }

    public static int getNakshatraNumber(double absoluteLongitude) {
        double boundedLong = absoluteLongitude % 360.0;
        int index = (int) (boundedLong / (360.0 / 27.0)) + 1;
        return index > 27 ? 27 : index;
    }

    public static String getNakshatraName(double absoluteLongitude) {
        int index = getNakshatraNumber(absoluteLongitude) - 1;
        return NAKSHATRAS[index >= 27 ? 26 : index];
    }

    public static int getNakshatraPada(double absoluteLongitude) {
        double boundedLong = absoluteLongitude % 360.0;
        double nakshatraArc = 360.0 / 27.0;
        double positionInNakshatra = boundedLong % nakshatraArc;
        double padaArc = nakshatraArc / 4.0;
        int pada = (int) (positionInNakshatra / padaArc) + 1;
        return pada > 4 ? 4 : pada;
    }

    public static String formatDMS(double degreeInSign) {
        int degrees = (int) degreeInSign;
        double minutesRaw = (degreeInSign - degrees) * 60.0;
        int minutes = (int) minutesRaw;

        // Returns standard readable formatting: 13°45'
        return String.format("%02d°%02d'", degrees, minutes);
    }
}
