package org.vedic.astro.util;

import org.vedic.astro.model.PlanetaryPosition;

import java.util.Map;

/**
 * Structural Astrological Anchors calculation based on classical Jaimini Arudha Lagna,
 * Paka Lagna, and Sav Karma Anchors.
 */
public class StructuralAnchorsUtils {

    private static final String[] RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Kataka", "Simha", "Kanya",
            "Tula", "Vrishchika", "Dhanus", "Makara", "Kumbha", "Meena"
    };

    public record LuckyDayResult(
            String dayName,
            String rulingPlanet,
            String auspiciousActivities
    ) {}

    public record AuspiciousDirectionsResult(
            String permanentVastuDirection,
            String travelProsperityDirection,
            String lagnaElementDirection,
            String moonElementDirection
    ) {}

    public record StructuralAnchorsResult(
            String physicalVitalityAnchor,
            String arudhaLagna,
            String mindAnchorResilience,
            String karmaAnchorHouse
    ) {}

    public record StructuralBundle(
            LuckyDayResult luckyDay,
            AuspiciousDirectionsResult directions,
            StructuralAnchorsResult structuralAnchors
    ) {}

    public static StructuralBundle calculateStructuralAnchors(
            int lagnaSign,
            int moonSign,
            Map<String, PlanetaryPosition> d1,
            double julianDayUT) {

        // 1. Lucky Day of Week from Julian Day
        int dayOfWeek = (int) Math.floor((julianDayUT + 1.5) % 7);
        int varaIdx = (dayOfWeek == 0) ? 7 : dayOfWeek;
        LuckyDayResult luckyDay = getLuckyDayDetails(varaIdx);

        // 2. Auspicious Directions
        String lagnaDir = getElementDirection(lagnaSign);
        String moonDir = getElementDirection(moonSign);
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        String lagnaLordDigbala = getPlanetaryDigbalaDirection(lagnaLord);

        String permanentVastu = lagnaDir + " (" + lagnaLordDigbala + ")";
        String travelDir = moonDir + " (North-East / East)";

        AuspiciousDirectionsResult directions = new AuspiciousDirectionsResult(
                permanentVastu,
                travelDir,
                lagnaDir,
                moonDir
        );

        // 3. Structural Anchors
        String lagnaSignName = RASHIS[lagnaSign - 1];

        PlanetaryPosition llPos = d1 != null ? d1.get(lagnaLord) : null;
        int pakaLagnaSign = llPos != null ? llPos.getSignNumber() : lagnaSign;
        String pakaLagnaName = RASHIS[pakaLagnaSign - 1];
        int llHouse = ((pakaLagnaSign - lagnaSign + 12) % 12) + 1;

        boolean llStrong = (llHouse == 1 || llHouse == 4 || llHouse == 7 || llHouse == 10 || llHouse == 5 || llHouse == 9);
        String vitalityStatus = "Lagna: " + lagnaSignName + " | Paka Lagna (Lagna Lord sign): " + pakaLagnaName + " (House " + llHouse + "). " +
                (llStrong ? "Strong physical vitality and immune resilience." : "Consistent health awareness and routine care recommended.");

        // Arudha Lagna (AL)
        int dist = ((pakaLagnaSign - lagnaSign + 12) % 12);
        int rawAl = ((pakaLagnaSign + dist - 1) % 12) + 1;
        int alHouseFromLagna = ((rawAl - lagnaSign + 12) % 12) + 1;

        int finalAlSign = rawAl;
        // Jaimini exception rules: If AL falls in 1st or 7th, or 4th or 10th from Lagna, jump 10 houses forward
        if (alHouseFromLagna == 1 || alHouseFromLagna == 7 || alHouseFromLagna == 4 || alHouseFromLagna == 10) {
            finalAlSign = ((rawAl + 10 - 1 - 1) % 12) + 1;
        }
        String alSignName = RASHIS[finalAlSign - 1];
        int finalAlHouse = ((finalAlSign - lagnaSign + 12) % 12) + 1;
        String arudhaLagnaText = alSignName + " (House " + finalAlHouse + ") - Social Status & Professional Recognition Anchor (Arudha Lagna - AL)";

        // Mind Anchor (Mati Karaka / Moon Dispositor)
        String moonLord = PlanetDignityUtils.getSignLord(moonSign);
        PlanetaryPosition mlPos = d1 != null ? d1.get(moonLord) : null;
        int mlHouse = mlPos != null ? ((mlPos.getSignNumber() - lagnaSign + 12) % 12) + 1 : 1;
        boolean mlStrong = (mlHouse == 1 || mlHouse == 4 || mlHouse == 7 || mlHouse == 10 || mlHouse == 5 || mlHouse == 9);
        String mindResilience = "Moon Sign Lord " + moonLord + " (House " + mlHouse + ") - " +
                (mlStrong ? "Strong mental fortitude and high emotional resilience." : "Meditation, mindfulness, and spiritual focus recommended for inner peace.");

        // Karma Anchor (10th/11th House Prosperity Engine)
        int tenthSign = ((lagnaSign + 10 - 1 - 1) % 12) + 1;
        String tenthLord = PlanetDignityUtils.getSignLord(tenthSign);
        String karmaAnchor = "House 10: " + RASHIS[tenthSign - 1] + " (Lord: " + tenthLord + ") & House 11 (Gains) - Professional & Financial Prosperity Engine (SAV Karma Anchor).";

        StructuralAnchorsResult anchors = new StructuralAnchorsResult(
                vitalityStatus,
                arudhaLagnaText,
                mindResilience,
                karmaAnchor
        );

        return new StructuralBundle(luckyDay, directions, anchors);
    }

    public static LuckyDayResult getLuckyDayDetails(int varaIdx) {
        return switch (varaIdx) {
            case 1 -> new LuckyDayResult("Sunday", "Sun", "Leadership, government affairs, executive decisions, new beginnings");
            case 2 -> new LuckyDayResult("Monday", "Moon", "Mental clarity, creative pursuits, public relations, trade");
            case 3 -> new LuckyDayResult("Tuesday", "Mars", "Courageous initiatives, property deals, technical pursuits");
            case 4 -> new LuckyDayResult("Wednesday", "Mercury", "Education, commerce, communication, documentation, investments");
            case 5 -> new LuckyDayResult("Thursday", "Jupiter", "Auspicious activities, spirituality, elder blessings, financial expansion");
            case 6 -> new LuckyDayResult("Friday", "Venus", "Luxury, arts, vehicles, relationships, celebration");
            case 7 -> new LuckyDayResult("Saturday", "Saturn", "Long-term planning, disciplined work, meditation, service");
            default -> new LuckyDayResult("Thursday", "Jupiter", "Auspicious activities, financial expansion");
        };
    }

    public static String getElementDirection(int sign) {
        int mod = sign % 4;
        return switch (mod) {
            case 1 -> "East (Fire)";
            case 2 -> "South (Earth)";
            case 3 -> "West (Air)";
            case 0 -> "North (Water)";
            default -> "East";
        };
    }

    public static String getPlanetaryDigbalaDirection(String planet) {
        if (planet == null) return "East";
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya", "mars", "kuja", "sevvai" -> "South";
            case "venus", "shukra", "moon", "chandra" -> "North";
            case "saturn", "shani" -> "West";
            case "jupiter", "guru", "mercury", "budha" -> "East / North-East";
            default -> "East";
        };
    }
}
