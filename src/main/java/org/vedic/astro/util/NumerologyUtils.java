package org.vedic.astro.util;

import java.util.*;

/**
 * Numerology & Lucky Elements Calculation Engine based on Vedic Sankhya Shastra.
 */
public class NumerologyUtils {

    public record NumerologyResult(
            int radicalDriverNumber,
            String radicalRulingPlanet,
            int destinyConductorNumber,
            String destinyRulingPlanet,
            int astrologicalPlanetNumber,
            String astrologicalPlanetName,
            List<Integer> friendlyNumbers,
            List<Integer> neutralNumbers,
            List<Integer> enemyNumbers,
            String conflictResolutionNotes
    ) {}

    public record LuckyDatesResult(
            List<Integer> primaryLuckyDates,
            List<Integer> secondaryFriendlyDates,
            List<Integer> datesToAvoid,
            List<Integer> currentMonthChandrashtamaDates,
            String transitCautionNotes
    ) {}

    public static int getDigitalRoot(int n) {
        if (n <= 0) return 1;
        return 1 + ((n - 1) % 9);
    }

    public static String getPlanetForNumber(int num) {
        return switch (num) {
            case 1 -> "Sun";
            case 2 -> "Moon";
            case 3 -> "Jupiter";
            case 4 -> "Rahu";
            case 5 -> "Mercury";
            case 6 -> "Venus";
            case 7 -> "Ketu";
            case 8 -> "Saturn";
            case 9 -> "Mars";
            default -> "Sun";
        };
    }

    public static int getNumberForPlanet(String planet) {
        if (planet == null) return 1;
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya" -> 1;
            case "moon", "chandra" -> 2;
            case "jupiter", "guru" -> 3;
            case "rahu" -> 4;
            case "mercury", "budha" -> 5;
            case "venus", "shukra" -> 6;
            case "ketu" -> 7;
            case "saturn", "shani" -> 8;
            case "mars", "kuja", "sevvai", "mangal" -> 9;
            default -> 1;
        };
    }

    public static NumerologyResult calculateNumerology(int day, int month, int year, String lagnaLord) {
        int driver = getDigitalRoot(day);
        int conductor = getDigitalRoot(day + month + year);
        String driverPlanet = getPlanetForNumber(driver);
        String conductorPlanet = getPlanetForNumber(conductor);

        int astroPlanetNum = getNumberForPlanet(lagnaLord);
        String astroPlanetName = getPlanetForNumber(astroPlanetNum);

        List<Integer> friends = getFriendlyNumbers(driver);
        List<Integer> neutrals = getNeutralNumbers(driver);
        List<Integer> enemies = getEnemyNumbers(driver);

        String conflictNotes = null;
        if (enemies.contains(conductor)) {
            conflictNotes = "ஓட்டுநர் எண் " + driver + " (" + driverPlanet + ") மற்றும் நடத்துனர் எண் " + conductor + " (" + conductorPlanet +
                    ") பகையாக அமைவதால் நடுநிலை பால எண்களான 5 (புதன்) அல்லது 6 (சுக்கிரன்) பயன்படுத்தவும்.";
        }

        return new NumerologyResult(
                driver,
                driverPlanet,
                conductor,
                conductorPlanet,
                astroPlanetNum,
                astroPlanetName,
                friends,
                neutrals,
                enemies,
                conflictNotes
        );
    }

    public static List<Integer> getFriendlyNumbers(int num) {
        return switch (num) {
            case 1 -> List.of(1, 2, 3, 5, 9);
            case 2 -> List.of(1, 2, 3, 5);
            case 3 -> List.of(1, 2, 3, 9);
            case 4 -> List.of(1, 5, 6, 7);
            case 5 -> List.of(1, 5, 6, 8);
            case 6 -> List.of(5, 6, 8);
            case 7 -> List.of(1, 2, 3, 6, 7);
            case 8 -> List.of(5, 6);
            case 9 -> List.of(1, 3, 9);
            default -> List.of(1, 5);
        };
    }

    public static List<Integer> getNeutralNumbers(int num) {
        return switch (num) {
            case 1, 2 -> List.of(4, 7);
            case 3 -> List.of(7);
            case 4, 9 -> List.of(6, 7);
            case 5, 8 -> List.of(3, 7, 9);
            case 6 -> List.of(1, 4, 7, 9);
            case 7 -> List.of(4, 5);
            default -> List.of(3, 7);
        };
    }

    public static List<Integer> getEnemyNumbers(int num) {
        return switch (num) {
            case 1 -> List.of(8);
            case 2, 7 -> List.of(8, 9);
            case 3 -> List.of(5, 6, 8);
            case 4 -> List.of(2, 4, 8);
            case 5 -> List.of(2);
            case 6 -> List.of(3);
            case 8 -> List.of(1, 2, 4, 8);
            case 9 -> List.of(2, 5, 8);
            default -> List.of(8);
        };
    }

    public static LuckyDatesResult calculateLuckyDates(int driverNumber, int moonSign, List<Integer> chandrashtamaDays) {
        List<Integer> primary = switch (driverNumber) {
            case 1 -> List.of(1, 10, 19, 28);
            case 2 -> List.of(2, 11, 20, 29);
            case 3 -> List.of(3, 12, 21, 30);
            case 4 -> List.of(1, 5, 6, 14, 23);
            case 5 -> List.of(5, 14, 23);
            case 6 -> List.of(6, 15, 24);
            case 7 -> List.of(7, 16, 25);
            case 8 -> List.of(5, 6, 14, 23);
            case 9 -> List.of(9, 18, 27);
            default -> List.of(1, 10, 19, 28);
        };

        List<Integer> secondary = switch (driverNumber) {
            case 1 -> List.of(2, 3, 5, 9, 11, 12, 14, 21, 23, 27, 30);
            case 2 -> List.of(1, 3, 7, 10, 12, 19, 21, 28, 30);
            case 3 -> List.of(1, 2, 9, 10, 11, 18, 19, 27, 28);
            case 4 -> List.of(7, 10, 15, 19, 24, 28);
            case 5 -> List.of(1, 6, 10, 15, 19, 24, 28);
            case 6 -> List.of(5, 8, 14, 17, 23, 26);
            case 7 -> List.of(1, 2, 3, 10, 11, 12, 19, 20, 21, 28, 29);
            case 8 -> List.of(15, 24);
            case 9 -> List.of(1, 3, 10, 12, 19, 21, 28, 30);
            default -> List.of(2, 3, 5, 9);
        };

        List<Integer> avoid = switch (driverNumber) {
            case 1 -> List.of(8, 17, 26);
            case 2 -> List.of(8, 9, 17, 18, 26, 27);
            case 3 -> List.of(6, 15, 24);
            case 4 -> List.of(2, 4, 8, 13, 22, 31);
            case 5 -> List.of(2, 11, 20, 29);
            case 6 -> List.of(3, 12, 21, 30);
            case 7 -> List.of(8, 17, 26);
            case 8 -> List.of(1, 2, 4, 8, 10, 11, 13, 17, 22, 26, 31);
            case 9 -> List.of(2, 5, 8, 11, 14, 17, 20, 23, 26);
            default -> List.of(8, 17, 26);
        };

        List<Integer> chandraDays = chandrashtamaDays != null ? chandrashtamaDays : Collections.emptyList();
        String caution = chandraDays.isEmpty() ? null : "சந்திராஷ்டம தினங்களில் புதிய முதலீடுகள் மற்றும் முக்கிய பயணங்களைத் தவிர்க்கவும்.";

        return new LuckyDatesResult(
                primary,
                secondary,
                avoid,
                chandraDays,
                caution
        );
    }
}
