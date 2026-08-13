package org.vedic.astro.util;

import org.vedic.astro.model.PlanetaryPosition;

import java.util.*;

/**
 * Structural Astrological Anchors & Directional Alignment Engine based on classical Brihat Parasara Hora Shastra.
 */
public class StructuralAnchorsUtils {

    private static final String[] RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Kataka", "Simha", "Kanya",
            "Tula", "Vrishchika", "Dhanus", "Makara", "Kumbha", "Meena"
    };

    public record LuckyDayResult(
            String vedicWeekdayName,
            String rulingPlanet,
            String luckySignifications
    ) {}

    public record AuspiciousDirectionsResult(
            String permanentVastuDirection,
            String travelDirection,
            String lagnaCompassZone,
            String moonCompassZone
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
            double julianDay) {

        // 1. Vedic Lucky Weekday (Vara)
        int varaIdx = ((int) Math.floor(julianDay + 0.5) % 7) + 1;
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
        String moonSignName = RASHIS[moonSign - 1];

        PlanetaryPosition llPos = d1 != null ? d1.get(lagnaLord) : null;
        int pakaLagnaSign = llPos != null ? llPos.getSignNumber() : lagnaSign;
        String pakaLagnaName = RASHIS[pakaLagnaSign - 1];
        int llHouse = ((pakaLagnaSign - lagnaSign + 12) % 12) + 1;

        boolean llStrong = (llHouse == 1 || llHouse == 4 || llHouse == 7 || llHouse == 10 || llHouse == 5 || llHouse == 9);
        String vitalityStatus = "லக்னம்: " + lagnaSignName + " | பாக லக்னம் (லக்னாதிபதி அமர்ந்த ராசி): " + pakaLagnaName + " (" + llHouse +
                "-ஆம் பாவகம்). " + (llStrong ? "சரீர பலமும் நோய் எதிர்ப்பு ஆற்றலும் மிக நன்று (Strong Vitality)." : "உடல்நலத்தில் சீரான விழிப்புணர்வு தேவை.");

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
        String arudhaLagnaText = alSignName + " (" + finalAlHouse + "-ஆம் பாவகம்) - சமூக அந்தஸ்து & தொழில் வெற்றி நங்கூரம் (Arudha Lagna - AL)";

        // Mind Anchor (Mati Karaka / Moon Dispositor)
        String moonLord = PlanetDignityUtils.getSignLord(moonSign);
        PlanetaryPosition mlPos = d1 != null ? d1.get(moonLord) : null;
        int mlHouse = mlPos != null ? ((mlPos.getSignNumber() - lagnaSign + 12) % 12) + 1 : 1;
        boolean mlStrong = (mlHouse == 1 || mlHouse == 4 || mlHouse == 7 || mlHouse == 10 || mlHouse == 5 || mlHouse == 9);
        String mindResilience = "ராசிநாதன் " + moonLord + " (" + mlHouse + "-ஆம் பாவகம்) - " +
                (mlStrong ? "மன உறுதி மற்றும் சவால்களை வெல்லும் மனோபலம் (High Resilience)." : "தியானம் மற்றும் வழிபாட்டின் மூலம் மன அமைதி காக்கவும்.");

        // Karma Anchor (10th/11th House Prosperity Engine)
        int tenthSign = ((lagnaSign + 10 - 1 - 1) % 12) + 1;
        String tenthLord = PlanetDignityUtils.getSignLord(tenthSign);
        String karmaAnchor = "10-ஆம் பாவகமான " + RASHIS[tenthSign - 1] + " (அதிபதி " + tenthLord + ") & 11-ஆம் லாப பாவகம் - தொழில் & பொருளாதார யோக நங்கூரம் (SAV Karma Anchor).";

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
            case 1 -> new LuckyDayResult("Sunday (ஞாயிற்றுக்கிழமை)", "Sun (சூரியன்)", "ஆளுமை, அரசு உதவிகள், தலைமைப் பொறுப்புகள், புதிய தொடக்கங்கள்");
            case 2 -> new LuckyDayResult("Monday (திங்கட்கிழமை)", "Moon (சந்திரன்)", "மனத்தெளிவு, கலை ஈடுபாடு, மக்கள் தொடர்பு, பொது வர்த்தகம்");
            case 3 -> new LuckyDayResult("Tuesday (செவ்வாய்க்கிழமை)", "Mars (செவ்வாய்)", "வீரியம், தைரிய முயற்சிகள், பூமி/சொத்து ஒப்பந்தங்கள், தொழில்நுட்பம்");
            case 4 -> new LuckyDayResult("Wednesday (புதன்கிழமை)", "Mercury (புதன்)", "கல்வி, வணிகம், தகவல் தொடர்பு, ஆவணங்கள், முதலீடுகள்");
            case 5 -> new LuckyDayResult("Thursday (வியாழக்கிழமை)", "Jupiter (குரு)", "சுப காரியங்கள், ஆன்மீகம், பெரியோர் ஆசிகள், நிதி விரிவாக்கம்");
            case 6 -> new LuckyDayResult("Friday (வெள்ளிக்கிழமை)", "Venus (சுக்கிரன்)", "ஆடம்பரப் பொருட்கள், கலை, வாகனம், உறவுகள், மங்கலப் பணிகள்");
            case 7 -> new LuckyDayResult("Saturday (சனிக்கிழமை)", "Saturn (சனி)", "நீண்ட காலத் திட்டங்கள், அமைதி, தியானம், தொண்டுப் பணிகள்");
            default -> new LuckyDayResult("Thursday (வியாழக்கிழமை)", "Jupiter (குரு)", "சுப காரியங்கள், நிதி விரிவாக்கம்");
        };
    }

    public static String getElementDirection(int sign) {
        int mod = sign % 4;
        return switch (mod) {
            case 1 -> "East (கிழக்கு - அக்னி)";
            case 2 -> "South (தெற்கு - பூமி)";
            case 3 -> "West (மேற்கு - காற்று)";
            case 0 -> "North (வடக்கு - ஜலம்)";
            default -> "East (கிழக்கு)";
        };
    }

    public static String getPlanetaryDigbalaDirection(String planet) {
        if (planet == null) return "East";
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya", "mars", "kuja", "sevvai" -> "South (தெற்கு)";
            case "venus", "shukra", "moon", "chandra" -> "North (வடக்கு)";
            case "saturn", "shani" -> "West (மேற்கு)";
            case "jupiter", "guru", "mercury", "budha" -> "East / North-East (கிழக்கு / வடகிழக்கு)";
            default -> "East (கிழக்கு)";
        };
    }
}
