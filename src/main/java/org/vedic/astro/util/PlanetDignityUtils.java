package org.vedic.astro.util;

import java.util.List;

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

    /**
     * Determines Bhadhakastana (Obstacle House) from Lagna Sign Modality:
     * Movable (Chara: 1, 4, 7, 10) -> 11th House
     * Fixed (Sthira: 2, 5, 8, 11) -> 9th House
     * Dual (Dwisvabhava: 3, 6, 9, 12) -> 7th House
     */
    public static int getBhadhakaHouse(int lagnaSign) {
        return switch (lagnaSign) {
            case 1, 4, 7, 10 -> 11;
            case 2, 5, 8, 11 -> 9;
            case 3, 6, 9, 12 -> 7;
            default -> 11;
        };
    }

    /**
     * Checks if absolute longitude is within 1 degree (01°00'00") of the 3 Water-Fire Gandanta junctions:
     * 1. Revati (Pisces) -> Aswini (Aries) [359° - 360° / 0° - 1°]
     * 2. Aslesha (Cancer) -> Magha (Leo) [119° - 121°]
     * 3. Jyeshtha (Scorpio) -> Moola (Sagittarius) [239° - 241°]
     */
    public static boolean isGandanta(double absLong) {
        double norm = ((absLong % 360.0) + 360.0) % 360.0;
        // 1. Pisces-Aries junction (0° / 360°)
        if (norm >= 359.0 || norm <= 1.0) return true;
        // 2. Cancer-Leo junction (120°)
        if (norm >= 119.0 && norm <= 121.0) return true;
        // 3. Scorpio-Sagittarius junction (240°)
        if (norm >= 239.0 && norm <= 241.0) return true;
        return false;
    }

    /**
     * Resolves Graha Yuddha (Planetary War) between two non-luminary planets in same sign within 1 degree.
     * Winner: Lower absolute degree longitude.
     * Defeated: Higher absolute degree longitude.
     * Exception: Venus always defeats Mars regardless of degree.
     */
    public static boolean isYuddhaDefeated(String planet1, double p1AbsLong, String planet2, double p2AbsLong) {
        String p1 = planet1.toLowerCase();
        String p2 = planet2.toLowerCase();
        // Luminaries and nodes do not engage in planetary war
        List<String> validWarPlanets = List.of("mars", "mercury", "jupiter", "venus", "saturn");
        if (!validWarPlanets.contains(p1) || !validWarPlanets.contains(p2)) return false;

        // Must be in the exact same 30° sign
        int s1 = (int)(p1AbsLong / 30.0) + 1;
        int s2 = (int)(p2AbsLong / 30.0) + 1;
        if (s1 != s2) return false;

        // Must be within 1.0 degree
        double diff = Math.abs(p1AbsLong - p2AbsLong);
        if (diff > 1.0) return false;

        // Special Venus vs Mars exception
        if ("venus".equals(p1) && "mars".equals(p2)) return false; // Venus wins
        if ("mars".equals(p1) && "venus".equals(p2)) return true;  // Mars loses to Venus

        // General rule: lower longitude wins, higher longitude is defeated
        double degInSign1 = p1AbsLong % 30.0;
        double degInSign2 = p2AbsLong % 30.0;
        return degInSign1 > degInSign2;
    }

    /**
     * Evaluates Retrograde (Vakra) dignity overrides:
     * 1. Debilitated + Retrograde = Uchcha-Sama Bala (Debilitation cancelled, functions like Exalted).
     * 2. Exalted + Retrograde = Functions like neutral/weak.
     */
    public static String getEffectiveDignityWithVakra(String planet, int sign, boolean isRetrograde) {
        boolean exalted = isExalted(planet, sign);
        boolean debilitated = isDebilitated(planet, sign);
        boolean ownSign = isOwnSign(planet, sign);

        if (debilitated && isRetrograde) {
            return "UCHCHA_SAMA_VAKRA"; // Debilitation cancelled by retrogradation
        }
        if (exalted && isRetrograde) {
            return "WEAKENED_EXALTED_VAKRA"; // Exaltation strength reduced by retrogradation
        }
        if (exalted) return "EXALTED";
        if (debilitated) return "DEBILITATED";
        if (ownSign) return "OWN_SIGN";
        return "NEUTRAL";
    }
}
