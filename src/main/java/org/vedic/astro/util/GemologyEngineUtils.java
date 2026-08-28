package org.vedic.astro.util;

import org.vedic.astro.model.PlanetaryPosition;

import java.util.*;

/**
 * Vedic Gemology Engine based on classical Ratna Shastra and Jataka Parijata.
 */
public class GemologyEngineUtils {

    public record GemologyResult(
            String primaryGemstone,
            String primaryGemstoneTamil,
            String secondarySubstitute,
            String rulingPlanet,
            String recommendedMetal,
            String recommendedFinger,
            String activationDayAndTiming,
            List<String> forbiddenCompanionGems,
            String astrologicalRationale
    ) {}

    public static String getYogakarakaPlanet(int lagnaSign) {
        return switch (lagnaSign) {
            case 2, 7 -> "Saturn"; // Taurus (9 & 10), Libra (4 & 5)
            case 4, 5 -> "Mars";   // Cancer (5 & 10), Leo (4 & 9)
            case 10, 11 -> "Venus"; // Capricorn (5 & 10), Aquarius (4 & 9)
            default -> null;
        };
    }

    public static GemologyResult calculateGemologyRecommendation(int lagnaSign, Map<String, PlanetaryPosition> d1) {
        if (d1 == null || d1.isEmpty()) {
            return defaultGemologyResult();
        }

        // Trikona Lords: 1st (Lagna), 5th, 9th
        int firstSign = lagnaSign;
        int fifthSign = ((lagnaSign - 1 + 4) % 12) + 1;
        int ninthSign = ((lagnaSign - 1 + 8) % 12) + 1;

        String lord1 = PlanetDignityUtils.getSignLord(firstSign);
        String lord5 = PlanetDignityUtils.getSignLord(fifthSign);
        String lord9 = PlanetDignityUtils.getSignLord(ninthSign);
        String yogakaraka = getYogakarakaPlanet(lagnaSign);

        List<String> candidates = new ArrayList<>();
        if (yogakaraka != null) candidates.add(yogakaraka);
        if (!candidates.contains(lord1)) candidates.add(lord1);
        if (!candidates.contains(lord9)) candidates.add(lord9);
        if (!candidates.contains(lord5)) candidates.add(lord5);

        String selectedPlanet = lord1;
        String selectionRationale = lord1 + " (Lagna Lord) strengthened as foundational primary gemstone.";

        for (String planet : candidates) {
            PlanetaryPosition pos = d1.get(planet);
            if (pos != null) {
                int house = ((pos.getSignNumber() - lagnaSign + 12) % 12) + 1;
                // Exclude Dusthana (6, 8, 12)
                if (house == 6 || house == 8 || house == 12) {
                    continue; // Skip dusthana placement
                }
                // Check if debilitated
                if (PlanetDignityUtils.isDebilitated(planet, pos.getSignNumber())) {
                    continue;
                }
                selectedPlanet = planet;
                if (planet.equalsIgnoreCase(yogakaraka)) {
                    selectionRationale = planet + " is the supreme Yogakaraka (governing Kendra & Trikona simultaneously), placed auspiciously in House " + house + ", recommended as the most potent life-enhancing gemstone.";
                } else {
                    selectionRationale = planet + " placed in auspicious House " + house + " as Trikona Lord (1, 5, 9) and recommended as primary beneficial gemstone.";
                }
                break;
            }
        }

        return buildGemologyResult(selectedPlanet, selectionRationale);
    }

    public static GemologyResult buildGemologyResult(String planet, String rationale) {
        String p = planet != null ? planet.trim().toLowerCase() : "sun";
        return switch (p) {
            case "sun", "surya" -> new GemologyResult(
                    "Ruby", "மாணிக்கம்",
                    "Red Garnet / Spinel",
                    "Sun",
                    "Gold / Copper",
                    "Ring Finger",
                    "Sunday Sunrise",
                    List.of("Blue Sapphire", "Diamond", "Hessonite"),
                    rationale
            );
            case "moon", "chandra" -> new GemologyResult(
                    "Natural Pearl", "முத்து",
                    "Moonstone",
                    "Moon",
                    "Silver",
                    "Little Finger",
                    "Monday Sunrise",
                    List.of("Blue Sapphire", "Hessonite", "Cat's Eye"),
                    rationale
            );
            case "mars", "kuja", "sevvai", "mangal" -> new GemologyResult(
                    "Red Coral", "செம்பவளம்",
                    "Carnelian",
                    "Mars",
                    "Gold / Copper",
                    "Ring Finger",
                    "Tuesday Sunrise",
                    List.of("Emerald", "Diamond", "Blue Sapphire"),
                    rationale
            );
            case "mercury", "budha" -> new GemologyResult(
                    "Emerald", "மரகதம்",
                    "Peridot / Green Tourmaline",
                    "Mercury",
                    "Gold / Silver",
                    "Little Finger",
                    "Wednesday Sunrise",
                    List.of("Natural Pearl", "Red Coral"),
                    rationale
            );
            case "jupiter", "guru" -> new GemologyResult(
                    "Yellow Sapphire", "கனக புஷ்பராகம்",
                    "Yellow Topaz / Citrine",
                    "Jupiter",
                    "Gold",
                    "Index Finger",
                    "Thursday Sunrise",
                    List.of("Diamond", "Blue Sapphire"),
                    rationale
            );
            case "venus", "shukra" -> new GemologyResult(
                    "Diamond", "வைரம்",
                    "White Sapphire / Zircon",
                    "Venus",
                    "Platinum / Silver / White Gold",
                    "Middle / Little Finger",
                    "Friday Sunrise",
                    List.of("Ruby", "Pearl", "Red Coral", "Yellow Sapphire"),
                    rationale
            );
            case "saturn", "shani" -> new GemologyResult(
                    "Blue Sapphire", "நீலக்கல்",
                    "Amethyst / Iolite",
                    "Saturn",
                    "Silver / Iron",
                    "Middle Finger",
                    "Saturday Evening",
                    List.of("Ruby", "Pearl", "Red Coral"),
                    rationale
            );
            case "rahu" -> new GemologyResult(
                    "Hessonite Garnet", "கோமேதகம்",
                    "Spessartite",
                    "Rahu",
                    "Silver",
                    "Middle Finger",
                    "Saturday Night / Rahu Kalam",
                    List.of("Ruby", "Pearl", "Red Coral", "Yellow Sapphire"),
                    rationale
            );
            case "ketu" -> new GemologyResult(
                    "Cat's Eye", "வைடூரியம்",
                    "Chrysoberyl",
                    "Ketu",
                    "Gold / Silver",
                    "Middle Finger",
                    "Tuesday Evening",
                    List.of("Ruby", "Pearl", "Emerald", "Diamond"),
                    rationale
            );
            default -> defaultGemologyResult();
        };
    }

    private static GemologyResult defaultGemologyResult() {
        return new GemologyResult(
                "Red Coral", "செம்பவளம் (Red Coral)",
                "Carnelian", "Mars",
                "Gold / Copper", "Ring Finger",
                "Tuesday Sunrise",
                List.of("Emerald", "Diamond", "Blue Sapphire"),
                "Mars placed in Kendra-Trikona auspicious house as Trikona Lord and recommended as Yogakaraka gemstone."
        );
    }
}
