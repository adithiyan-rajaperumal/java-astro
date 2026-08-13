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

        List<String> candidates = List.of(lord1, lord9, lord5);
        String selectedPlanet = lord1;
        String selectionRationale = "லக்னாதிபதி பலம் பெற முதன்மை ரத்தினம் தேர்ந்தெடுக்கப்பட்டது.";

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
                selectionRationale = planet + " திரிகோணாதிபதியாக (1, 5, 9) கேந்திர-திரிகோண சுப ஸ்தானத்தில் அமர்ந்துள்ளதால் யோககாரக ரத்தினமாக பரிந்துரைக்கப்படுகிறது.";
                break;
            }
        }

        return buildGemologyResult(selectedPlanet, selectionRationale);
    }

    public static GemologyResult buildGemologyResult(String planet, String rationale) {
        String p = planet != null ? planet.trim().toLowerCase() : "sun";
        return switch (p) {
            case "sun", "surya" -> new GemologyResult(
                    "Ruby", "மாணிக்கம் (Ruby)",
                    "Red Garnet / Spinel (சிவப்பு கார்னெட்)",
                    "Sun (சூரியன்)",
                    "Gold / Copper (தங்கம் / தாமிரம்)",
                    "Ring Finger (மோதிர விரல்)",
                    "Sunday Sunrise (ஞாயிறு அதிகாலை)",
                    List.of("Blue Sapphire (நீலம்)", "Diamond (வைரம்)", "Hessonite (கோமேதகம்)"),
                    rationale
            );
            case "moon", "chandra" -> new GemologyResult(
                    "Natural Pearl", "முத்து (Natural Pearl)",
                    "Moonstone (சந்திரகாந்த கல்)",
                    "Moon (சந்திரன்)",
                    "Silver (வெள்ளி)",
                    "Little Finger (சுண்டு விரல்)",
                    "Monday Sunrise (திங்கள் அதிகாலை)",
                    List.of("Blue Sapphire (நீலம்)", "Hessonite (கோமேதகம்)", "Cat's Eye (வைடூரியம்)"),
                    rationale
            );
            case "mars", "kuja", "sevvai", "mangal" -> new GemologyResult(
                    "Red Coral", "செம்பவளம் (Red Coral)",
                    "Carnelian (கார்னீலியன்)",
                    "Mars (செவ்வாய்)",
                    "Gold / Copper (தங்கம் / தாமிரம்)",
                    "Ring Finger (மோதிர விரல்)",
                    "Tuesday Sunrise (செவ்வாய் அதிகாலை)",
                    List.of("Emerald (மரகதம்)", "Diamond (வைரம்)", "Blue Sapphire (நீலம்)"),
                    rationale
            );
            case "mercury", "budha" -> new GemologyResult(
                    "Emerald", "மரகதம் (Emerald)",
                    "Peridot / Green Tourmaline (பெரிடாட்)",
                    "Mercury (புதன்)",
                    "Gold / Silver (தங்கம் / வெள்ளி)",
                    "Little Finger (சுண்டு விரல்)",
                    "Wednesday Sunrise (புதன் அதிகாலை)",
                    List.of("Natural Pearl (முத்து)", "Red Coral (பவளம்)"),
                    rationale
            );
            case "jupiter", "guru" -> new GemologyResult(
                    "Yellow Sapphire", "கனக புஷ்பராகம் (Yellow Sapphire)",
                    "Yellow Topaz / Citrine (புஷ்பராக உபரத்தினம்)",
                    "Jupiter (குரு)",
                    "Gold (தங்கம்)",
                    "Index Finger (ஆள்காட்டி விரல்)",
                    "Thursday Sunrise (வியாழன் அதிகாலை)",
                    List.of("Diamond (வைரம்)", "Blue Sapphire (நீலம்)"),
                    rationale
            );
            case "venus", "shukra" -> new GemologyResult(
                    "Diamond", "வைரம் (Diamond)",
                    "White Sapphire / Zircon (வெள்ளை ஜிர்கான்)",
                    "Venus (சுக்கிரன்)",
                    "Platinum / Silver / White Gold (பிளாட்டினம் / வெள்ளி)",
                    "Middle / Little Finger (நடுவிரல் / சுண்டு விரல்)",
                    "Friday Sunrise (வெள்ளி அதிகாலை)",
                    List.of("Ruby (மாணிக்கம்)", "Pearl (முத்து)", "Red Coral (பவளம்)", "Yellow Sapphire (புஷ்பராகம்)"),
                    rationale
            );
            case "saturn", "shani" -> new GemologyResult(
                    "Blue Sapphire", "நீலக்கல் (Blue Sapphire)",
                    "Amethyst / Iolite (அமெதிஸ்ட் / காக்கைப் பொன்)",
                    "Saturn (சனி)",
                    "Silver / Iron (வெள்ளி / இரும்பு)",
                    "Middle Finger (நடுவிரல்)",
                    "Saturday Evening (சனி அந்தி சாயும் வேளை)",
                    List.of("Ruby (மாணிக்கம்)", "Pearl (முத்து)", "Red Coral (பவளம்)"),
                    rationale
            );
            case "rahu" -> new GemologyResult(
                    "Hessonite Garnet", "கோமேதகம் (Hessonite Garnet)",
                    "Spessartite (ஸ்பெஸ்ஸார்டைட்)",
                    "Rahu (ராகு)",
                    "Silver (வெள்ளி)",
                    "Middle Finger (நடுவிரல்)",
                    "Saturday Night / Rahu Kalam (சனிக்கிழமை / ராகு காலம்)",
                    List.of("Ruby (மாணிக்கம்)", "Pearl (முத்து)", "Yellow Sapphire (புஷ்பராகம்)"),
                    rationale
            );
            case "ketu" -> new GemologyResult(
                    "Cat's Eye", "வைடூரியம் (Cat's Eye)",
                    "Chrysoberyl (கிரைசோபெரில்)",
                    "Ketu (கேது)",
                    "Silver (வெள்ளி)",
                    "Ring Finger (மோதிர விரல்)",
                    "Tuesday Evening (செவ்வாய் மாலை வேளை)",
                    List.of("Ruby (மாணிக்கம்)", "Pearl (முத்து)", "Diamond (வைரம்)"),
                    rationale
            );
            default -> defaultGemologyResult();
        };
    }

    private static GemologyResult defaultGemologyResult() {
        return new GemologyResult(
                "Red Coral", "செம்பவளம் (Red Coral)",
                "Carnelian", "Mars (செவ்வாய்)",
                "Gold / Copper", "Ring Finger",
                "Tuesday Sunrise",
                List.of("Emerald", "Diamond", "Blue Sapphire"),
                "யோககாரக கிரக பலம் கூட்ட பரிந்துரைக்கப்பட்டது."
        );
    }
}
