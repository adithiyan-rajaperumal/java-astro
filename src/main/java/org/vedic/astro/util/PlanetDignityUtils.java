package org.vedic.astro.util;

public class PlanetDignityUtils {

    public static boolean isExalted(String planet, int sign) {
        return switch (planet) {
            case "Sun" -> sign == 1;
            case "Moon" -> sign == 2;
            case "Mars" -> sign == 10;
            case "Mercury" -> sign == 6;
            case "Jupiter" -> sign == 4;
            case "Venus" -> sign == 12;
            case "Saturn" -> sign == 7;
            default -> false;
        };
    }

    public static boolean isDebilitated(String planet, int sign) {
        return switch (planet) {
            case "Sun" -> sign == 7;
            case "Moon" -> sign == 8;
            case "Mars" -> sign == 4;
            case "Mercury" -> sign == 12;
            case "Jupiter" -> sign == 10;
            case "Venus" -> sign == 6;
            case "Saturn" -> sign == 1;
            default -> false;
        };
    }

    public static boolean isOwnSign(String planet, int sign) {
        return switch (planet) {
            case "Sun" -> sign == 5;
            case "Moon" -> sign == 4;
            case "Mars" -> sign == 1 || sign == 8;
            case "Mercury" -> sign == 3 || sign == 6;
            case "Jupiter" -> sign == 9 || sign == 12;
            case "Venus" -> sign == 2 || sign == 7;
            case "Saturn" -> sign == 10 || sign == 11;
            default -> false;
        };
    }

    public static int getHouseFromLagna(int planetSign, int lagnaSign) {
        return ((planetSign - lagnaSign + 12) % 12) + 1;
    }

    public static boolean isKendra(int house) {
        return house == 1 || house == 4 || house == 7 || house == 10;
    }

    public static boolean isTrikona(int house) {
        return house == 1 || house == 5 || house == 9;
    }

    public static boolean isUpachaya(int house) {
        return house == 3 || house == 6 || house == 10 || house == 11;
    }

    public static boolean isDusthana(int house) {
        return house == 6 || house == 8 || house == 12;
    }

    public static boolean isAspecting(String planet, int aspectingPlanetSign, int targetHouseSign) {
        int diff = getHouseFromLagna(targetHouseSign, aspectingPlanetSign);
        if (diff == 7) return true; // All planets aspect 7th
        return switch (planet) {
            case "Mars" -> diff == 4 || diff == 8;
            case "Jupiter" -> diff == 5 || diff == 9;
            case "Saturn" -> diff == 3 || diff == 10;
            default -> false;
        };
    }

    /**
     * Exact Combustion (Moudhya) Orbs per BPHS Master Specification:
     * - Moon: <= 12°
     * - Mars: <= 17°
     * - Mercury: <= 14° (Direct) / <= 12° (Retrograde)
     * - Jupiter: <= 11°
     * - Venus: <= 10° (Direct) / <= 8° (Retrograde)
     * - Saturn: <= 15°
     */
    public static boolean isCombust(String planet, double planetAbsLong, double sunAbsLong, boolean isRetrograde) {
        if ("Sun".equalsIgnoreCase(planet) || "Rahu".equalsIgnoreCase(planet) || "Ketu".equalsIgnoreCase(planet)) return false;
        double diff = Math.abs(planetAbsLong - sunAbsLong);
        if (diff > 180.0) diff = 360.0 - diff;

        double maxOrb = switch (planet.toLowerCase()) {
            case "moon" -> 12.0;
            case "mars" -> 17.0;
            case "mercury" -> isRetrograde ? 12.0 : 14.0;
            case "jupiter" -> 11.0;
            case "venus" -> isRetrograde ? 8.0 : 10.0;
            case "saturn" -> 15.0;
            default -> 12.0;
        };

        return diff <= maxOrb;
    }

    public static boolean isCombust(String planet, double planetAbsLong, double sunAbsLong) {
        return isCombust(planet, planetAbsLong, sunAbsLong, false);
    }

    public static int getExaltationSign(String planet) {
        return switch (planet) {
            case "Sun" -> 1;
            case "Moon" -> 2;
            case "Mars" -> 10;
            case "Mercury" -> 6;
            case "Jupiter" -> 4;
            case "Venus" -> 12;
            case "Saturn" -> 7;
            default -> 0;
        };
    }

    public static String getSignLord(int sign) {
        return switch (sign) {
            case 1, 8 -> "Mars";
            case 2, 7 -> "Venus";
            case 3, 6 -> "Mercury";
            case 4 -> "Moon";
            case 5 -> "Sun";
            case 9, 12 -> "Jupiter";
            case 10, 11 -> "Saturn";
            default -> "";
        };
    }
}
