package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.DasaPeriod;

import java.time.LocalDate;
import java.util.*;

/**
 * Deterministic Parashara & Jaimini Ayurdaya (Longevity Determination) Engine
 * based on Brihat Parasara Hora Shastra (BPHS Chapters 44-45) and Jaimini Sutras.
 */
public class AyurdayaCalculationUtils {

    public enum Modality {
        CHARA,       // Movable: Aries 1, Cancer 4, Libra 7, Capricorn 10
        STHIRA,      // Fixed: Taurus 2, Leo 5, Scorpio 8, Aquarius 11
        DWISVABHAVA  // Dual: Gemini 3, Virgo 6, Sagittarius 9, Pisces 12
    }

    public record AyurdayaProfile(
            String longevityClassification,
            int estimatedLifespanCeiling,
            String lifespanRange,
            Map<String, Object> threePairsDetails,
            List<String> kakshyaAdjustments,
            String criticalMarakaWindow,
            String classicalRationale
    ) {}

    public static Modality getModality(int signNumber) {
        return switch (signNumber) {
            case 1, 4, 7, 10 -> Modality.CHARA;
            case 2, 5, 8, 11 -> Modality.STHIRA;
            case 3, 6, 9, 12 -> Modality.DWISVABHAVA;
            default -> Modality.CHARA;
        };
    }

    public static String getModalitySpan(Modality m1, Modality m2) {
        if (m1 == Modality.CHARA && m2 == Modality.CHARA) return "Poornayu";
        if (m1 == Modality.CHARA && m2 == Modality.STHIRA) return "Madhyayu";
        if (m1 == Modality.CHARA && m2 == Modality.DWISVABHAVA) return "Alpayu";

        if (m1 == Modality.STHIRA && m2 == Modality.CHARA) return "Madhyayu";
        if (m1 == Modality.STHIRA && m2 == Modality.STHIRA) return "Alpayu";
        if (m1 == Modality.STHIRA && m2 == Modality.DWISVABHAVA) return "Poornayu";

        if (m1 == Modality.DWISVABHAVA && m2 == Modality.CHARA) return "Alpayu";
        if (m1 == Modality.DWISVABHAVA && m2 == Modality.STHIRA) return "Poornayu";
        if (m1 == Modality.DWISVABHAVA && m2 == Modality.DWISVABHAVA) return "Madhyayu";

        return "Madhyayu";
    }

    public static AyurdayaProfile calculateAyurdaya(
            int lagnaSign,
            int moonSign,
            List<ChartResponseDTO.PositionDetail> d1Chart,
            List<DasaPeriod> dasaTimeline,
            int birthYear) {

        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                String key = p.getPlanetKey() != null ? p.getPlanetKey().toUpperCase() : "";
                planetMap.put(key, p);
            }
        }

        // 1. Identify Key Longevity Determinants
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        int eighthSign = ((lagnaSign + 7 - 1) % 12) + 1;
        String eighthLord = PlanetDignityUtils.getSignLord(eighthSign);

        ChartResponseDTO.PositionDetail lagnaLordPos = planetMap.get(lagnaLord.toUpperCase());
        ChartResponseDTO.PositionDetail eighthLordPos = planetMap.get(eighthLord.toUpperCase());
        ChartResponseDTO.PositionDetail saturnPos = planetMap.get("SATURN");
        ChartResponseDTO.PositionDetail moonPos = planetMap.get("MOON");
        ChartResponseDTO.PositionDetail jupiterPos = planetMap.get("JUPITER");
        ChartResponseDTO.PositionDetail venusPos = planetMap.get("VENUS");

        int lagnaLordSign = lagnaLordPos != null ? lagnaLordPos.getSignNumber() : lagnaSign;
        int eighthLordSign = eighthLordPos != null ? eighthLordPos.getSignNumber() : eighthSign;
        int saturnSign = saturnPos != null ? saturnPos.getSignNumber() : 7;
        int activeMoonSign = moonPos != null ? moonPos.getSignNumber() : moonSign;

        // 2. Classical 3-Pair Modality Evaluation
        // Pair 1: Lagna Lord & 8th Lord
        Modality mLL = getModality(lagnaLordSign);
        Modality m8L = getModality(eighthLordSign);
        String span1 = getModalitySpan(mLL, m8L);

        // Pair 2: Moon & Saturn (Ayushkaraka)
        Modality mMoon = getModality(activeMoonSign);
        Modality mSat = getModality(saturnSign);
        String span2 = getModalitySpan(mMoon, mSat);

        // Pair 3: Lagna & Moon (or Hora Lagna)
        Modality mLagna = getModality(lagnaSign);
        String span3 = getModalitySpan(mLagna, mMoon);

        // Majority Resolution
        Map<String, Integer> votes = new HashMap<>();
        votes.put("Poornayu", 0);
        votes.put("Madhyayu", 0);
        votes.put("Alpayu", 0);
        votes.put(span1, votes.get(span1) + 1);
        votes.put(span2, votes.get(span2) + 1);
        votes.put(span3, votes.get(span3) + 1);

        String baseSpan = "Poornayu";
        if (votes.get("Poornayu") >= 2) baseSpan = "Poornayu";
        else if (votes.get("Madhyayu") >= 2) baseSpan = "Madhyayu";
        else if (votes.get("Alpayu") >= 2) baseSpan = "Alpayu";
        else baseSpan = span1; // Pair 1 acts as anchor tie-breaker

        // 3. Kakshya Vriddhi & Hrasa Adjustments
        List<String> adjustments = new ArrayList<>();
        int baseCeilingAge = switch (baseSpan) {
            case "Poornayu" -> 80;
            case "Madhyayu" -> 60;
            case "Alpayu" -> 32;
            default -> 75;
        };

        // Kakshya Vriddhi Factor A: Jupiter in Kendra (1,4,7,10) or Trikona (5,9) or exalted
        if (jupiterPos != null) {
            int jupHouse = ((jupiterPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            boolean jupStrong = PlanetDignityUtils.isOwnSign("Jupiter", jupiterPos.getSignNumber()) ||
                    PlanetDignityUtils.isExalted("Jupiter", jupiterPos.getSignNumber());
            if (jupHouse == 1 || jupHouse == 4 || jupHouse == 7 || jupHouse == 10 || jupHouse == 5 || jupHouse == 9 || jupStrong) {
                adjustments.add("குருவின் சுப பார்வை / கேந்திர-திரிகோண பலம் (Jupiter benefic Kendra/Trikona placement adds Kakshya Vriddhi +4 to +6 years).");
                baseCeilingAge += 5;
                if ("Alpayu".equals(baseSpan)) baseSpan = "Madhyayu";
                else if ("Madhyayu".equals(baseSpan) && baseCeilingAge > 68) baseSpan = "Poornayu";
            }
        }

        // Kakshya Vriddhi Factor B: Ayushkaraka Saturn in Own Sign / Exalted or high dignity
        if (saturnPos != null) {
            if (PlanetDignityUtils.isOwnSign("Saturn", saturnPos.getSignNumber()) || PlanetDignityUtils.isExalted("Saturn", saturnPos.getSignNumber())) {
                adjustments.add("ஆயுள்காரகன் சனி சுவக்ஷேத்திரம் / உச்சம் பெற்று பலம் (Ayushkaraka Saturn in Own/Exalted sign reinforces life vitality +3 years).");
                baseCeilingAge += 3;
            }
        }

        // Kakshya Vriddhi Factor C: Lagna Lord in Kendra / Trikona / Exalted
        if (lagnaLordPos != null) {
            int llHouse = ((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (PlanetDignityUtils.isExalted(lagnaLord, lagnaLordPos.getSignNumber()) || PlanetDignityUtils.isOwnSign(lagnaLord, lagnaLordPos.getSignNumber())) {
                adjustments.add("லக்னாதிபதி ஆட்சி / உச்ச பலம் பெற்று சரீர பலத்தை கூட்டுகிறார் (Lagna Lord strong in own/exalted sign adds +3 years vitality).");
                baseCeilingAge += 3;
            } else if (llHouse == 6 || llHouse == 8 || llHouse == 12) {
                adjustments.add("லக்னாதிபதி மறைவு ஸ்தானத்தில் இருப்பது விழிப்புணர்வு தேவை (Lagna Lord in Dusthana 6/8/12 requires mindful health care).");
                baseCeilingAge -= 2;
            }
        }

        // 4. Maraka & Badhaka Timeline Alignment
        int marakaSign2 = ((lagnaSign + 2 - 1 - 1) % 12) + 1;
        int marakaSign7 = ((lagnaSign + 7 - 1 - 1) % 12) + 1;
        String marakaLord2 = PlanetDignityUtils.getSignLord(marakaSign2);
        String marakaLord7 = PlanetDignityUtils.getSignLord(marakaSign7);

        // Badhaka Lord: 11th for Chara Lagna, 9th for Sthira Lagna, 7th for Dwisvabhava Lagna
        int badhakaSign = switch (getModality(lagnaSign)) {
            case CHARA -> ((lagnaSign + 11 - 1 - 1) % 12) + 1;
            case STHIRA -> ((lagnaSign + 9 - 1 - 1) % 12) + 1;
            case DWISVABHAVA -> ((lagnaSign + 7 - 1 - 1) % 12) + 1;
        };
        String badhakaLord = PlanetDignityUtils.getSignLord(badhakaSign);

        int targetYear = birthYear + baseCeilingAge;
        String activeMarakaDasaInfo = "வயது " + (baseCeilingAge - 2) + " முதல் " + (baseCeilingAge + 2) + " வரை (சுமார் " + (targetYear - 2) + " - " + (targetYear + 2) + ") மாரக/பாதக/அஷ்டமாதிபதி திசா-புக்தி காலம்.";

        if (dasaTimeline != null && !dasaTimeline.isEmpty()) {
            LocalDate targetDate = LocalDate.of(targetYear, 6, 15);
            for (DasaPeriod d : dasaTimeline) {
                if (d.getStartDate() != null && d.getEndDate() != null
                        && !targetDate.isBefore(d.getStartDate()) && !targetDate.isAfter(d.getEndDate())) {
                    activeMarakaDasaInfo = d.getPlanetName() + " திசா காலத்தில் (சுமார் " + (baseCeilingAge - 2) + " - " + (baseCeilingAge + 2) + " வயது) ஆயுள் சவால்கள்.";
                    break;
                }
            }
        }

        String lifespanRangeStr = (baseCeilingAge - 3) + " - " + (baseCeilingAge + 2) + " வயது (~" + (targetYear - 3) + " - " + (targetYear + 2) + ")";

        Map<String, Object> threePairsMap = new LinkedHashMap<>();
        threePairsMap.put("pair1_lagnaLord_and_8thLord", Map.of(
                "planets", lagnaLord + " (" + mLL + ") & " + eighthLord + " (" + m8L + ")",
                "derivedSpan", span1
        ));
        threePairsMap.put("pair2_moon_and_saturn", Map.of(
                "planets", "Moon (" + mMoon + ") & Saturn (" + mSat + ")",
                "derivedSpan", span2
        ));
        threePairsMap.put("pair3_lagna_and_moon", Map.of(
                "planets", "Lagna (" + mLagna + ") & Moon (" + mMoon + ")",
                "derivedSpan", span3
        ));
        threePairsMap.put("majorityConsensus", baseSpan);

        String rationale = "பராசர-ஜெயமினி முறைப்படி லக்னாதிபதி (" + lagnaLord + "), 8-ஆம் அதிபதி (" + eighthLord + "), சந்திரன், மற்றும் ஆயுள்காரகன் சனி ஆகியவற்றின் சர/ஸ்திர/உபய ராசி அமைப்புகளின் அடிப்படையில் " +
                baseSpan + " என நிர்ணயிக்கப்பட்டுள்ளது. " + String.join(" ", adjustments);

        return new AyurdayaProfile(
                baseSpan,
                baseCeilingAge,
                lifespanRangeStr,
                threePairsMap,
                adjustments,
                activeMarakaDasaInfo,
                rationale
        );
    }
}
