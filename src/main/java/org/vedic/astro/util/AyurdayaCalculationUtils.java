package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.DasaPeriod;

import java.time.LocalDate;
import java.util.*;

/**
 * Deterministic 3-Principle Parashara & Jaimini Ayurdaya (Longevity Determination) Engine.
 * 
 * Evaluates:
 * 1. Jaimini 3-Pair Modality System (with Hora Lagna and Kakshya Vriddhi/Hrasa rules).
 * 2. Parashara & Shadbala Life-Force Strength (Ayur Bala and Deerghayu Yogas).
 * 3. Maraka & Badhaka Planetary Health Timeline & Remedial Guidelines.
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
            Map<String, Object> jaiminiThreePairs,
            Map<String, Object> parasharaAyurBala,
            Map<String, Object> marakaBadhakaTimeline,
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

    public static int calculateHoraLagnaSign(int sunSign, double sunDegree, double birthHour, double birthMinute) {
        // Hora Lagna (HL): 1 sign (30 deg) per hora (2.5 ghatis = 1 hour) elapsed from sunrise (~6:00 AM)
        double totalHours = birthHour + (birthMinute / 60.0);
        double hoursFromSunrise = totalHours - 6.0;
        if (hoursFromSunrise < 0) hoursFromSunrise += 24.0;

        double sunAbsLong = ((sunSign - 1) * 30.0) + sunDegree;
        double hlAbsLong = (sunAbsLong + (hoursFromSunrise * 30.0)) % 360.0;
        return ((int) (hlAbsLong / 30.0)) + 1;
    }

    public static AyurdayaProfile calculateAyurdaya(
            int lagnaSign,
            int moonSign,
            List<ChartResponseDTO.PositionDetail> d1Chart,
            List<DasaPeriod> dasaTimeline,
            int birthYear) {
        return calculateAyurdaya(lagnaSign, moonSign, d1Chart, dasaTimeline, birthYear, 12, 0);
    }

    public static AyurdayaProfile calculateAyurdaya(
            int lagnaSign,
            int moonSign,
            List<ChartResponseDTO.PositionDetail> d1Chart,
            List<DasaPeriod> dasaTimeline,
            int birthYear,
            int birthHour,
            int birthMinute) {

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
        ChartResponseDTO.PositionDetail sunPos = planetMap.get("SUN");
        ChartResponseDTO.PositionDetail jupiterPos = planetMap.get("JUPITER");
        ChartResponseDTO.PositionDetail venusPos = planetMap.get("VENUS");
        ChartResponseDTO.PositionDetail mercuryPos = planetMap.get("MERCURY");

        int lagnaLordSign = lagnaLordPos != null ? lagnaLordPos.getSignNumber() : lagnaSign;
        int eighthLordSign = eighthLordPos != null ? eighthLordPos.getSignNumber() : eighthSign;
        int saturnSign = saturnPos != null ? saturnPos.getSignNumber() : 7;
        int activeMoonSign = moonPos != null ? moonPos.getSignNumber() : moonSign;

        // Hora Lagna Calculation for Pair 3
        int sunSign = sunPos != null ? sunPos.getSignNumber() : 1;
        double sunDeg = sunPos != null ? sunPos.getDegreeInSign() : 15.0;
        int horaLagnaSign = calculateHoraLagnaSign(sunSign, sunDeg, birthHour, birthMinute);

        // 2. PRINCIPLE 1: Classical 3-Pair Jaimini Modality Evaluation
        // Pair 1: Lagna Lord & 8th Lord
        Modality mLL = getModality(lagnaLordSign);
        Modality m8L = getModality(eighthLordSign);
        String span1 = getModalitySpan(mLL, m8L);

        // Pair 2: Moon & Saturn (Ayushkaraka)
        Modality mMoon = getModality(activeMoonSign);
        Modality mSat = getModality(saturnSign);
        String span2 = getModalitySpan(mMoon, mSat);

        // Pair 3: Lagna & Hora Lagna (with Moon fallback)
        Modality mLagna = getModality(lagnaSign);
        Modality mHL = getModality(horaLagnaSign);
        String span3 = getModalitySpan(mLagna, mHL);

        // Majority Resolution across the 3 Jaimini pairs
        Map<String, Integer> votes = new HashMap<>();
        votes.put("Poornayu", 0);
        votes.put("Madhyayu", 0);
        votes.put("Alpayu", 0);
        votes.put(span1, votes.get(span1) + 1);
        votes.put(span2, votes.get(span2) + 1);
        votes.put(span3, votes.get(span3) + 1);

        String baseSpan;
        if (votes.get("Poornayu") >= 2) baseSpan = "Poornayu";
        else if (votes.get("Madhyayu") >= 2) baseSpan = "Madhyayu";
        else if (votes.get("Alpayu") >= 2) baseSpan = "Alpayu";
        else baseSpan = span1; // Pair 1 acts as anchor tie-breaker

        // Kakshya Vriddhi & Hrasa Adjustments
        List<String> adjustments = new ArrayList<>();
        int baseCeilingAge = switch (baseSpan) {
            case "Poornayu" -> 82;
            case "Madhyayu" -> 68;
            case "Alpayu" -> 50;
            default -> 75;
        };

        // Kakshya Vriddhi Factor A: Jupiter in Kendra (1,4,7,10), Trikona (5,9), or Exalted
        if (jupiterPos != null) {
            int jupHouse = ((jupiterPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            boolean jupStrong = PlanetDignityUtils.isOwnSign("Jupiter", jupiterPos.getSignNumber()) ||
                    PlanetDignityUtils.isExalted("Jupiter", jupiterPos.getSignNumber());
            if (jupHouse == 1 || jupHouse == 4 || jupHouse == 7 || jupHouse == 10 || jupHouse == 5 || jupHouse == 9 || jupStrong) {
                adjustments.add("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (+5 to +7 years).");
                baseCeilingAge += 6;
                if ("Alpayu".equals(baseSpan)) baseSpan = "Madhyayu";
                else if ("Madhyayu".equals(baseSpan) && baseCeilingAge >= 72) baseSpan = "Poornayu";
            }
        }

        // Kakshya Vriddhi Factor B: Ayushkaraka Saturn in Own Sign / Exalted
        if (saturnPos != null) {
            if (PlanetDignityUtils.isOwnSign("Saturn", saturnPos.getSignNumber()) || PlanetDignityUtils.isExalted("Saturn", saturnPos.getSignNumber())) {
                adjustments.add("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).");
                baseCeilingAge += 4;
            }
        }

        // Kakshya Vriddhi Factor C: Lagna Lord in Kendra / Trikona / Exalted
        if (lagnaLordPos != null) {
            int llHouse = ((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (PlanetDignityUtils.isExalted(lagnaLord, lagnaLordPos.getSignNumber()) || PlanetDignityUtils.isOwnSign(lagnaLord, lagnaLordPos.getSignNumber())) {
                adjustments.add("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).");
                baseCeilingAge += 4;
            } else if (llHouse == 6 || llHouse == 8 || llHouse == 12) {
                adjustments.add("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.");
                baseCeilingAge -= 2;
            }
        }

        // 3. PRINCIPLE 2: Parashara & Shadbala Life-Force (Ayur Bala)
        int kendraBeneficCount = 0;
        for (var benefic : List.of("JUPITER", "VENUS", "MERCURY")) {
            ChartResponseDTO.PositionDetail p = planetMap.get(benefic);
            if (p != null) {
                int h = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
                if (h == 1 || h == 4 || h == 7 || h == 10) kendraBeneficCount++;
            }
        }

        boolean lagnaLordStrong = (lagnaLordPos != null && (
                PlanetDignityUtils.isExalted(lagnaLord, lagnaLordPos.getSignNumber()) ||
                PlanetDignityUtils.isOwnSign(lagnaLord, lagnaLordPos.getSignNumber()) ||
                PlanetDignityUtils.isKendra(((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1) ||
                PlanetDignityUtils.isTrikona(((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1)
        ));

        String ayurBalaClassification;
        if (kendraBeneficCount >= 2 && lagnaLordStrong) {
            ayurBalaClassification = "High Resilience & Deerghayu Vitality";
        } else if (kendraBeneficCount >= 1 || lagnaLordStrong) {
            ayurBalaClassification = "Balanced Constitutional Vitality";
        } else {
            ayurBalaClassification = "Health-Cautious Vitality";
        }

        Map<String, Object> parasharaBalaMap = new LinkedHashMap<>();
        parasharaBalaMap.put("vitalityScore", ayurBalaClassification);
        parasharaBalaMap.put("kendraBeneficsCount", kendraBeneficCount);
        parasharaBalaMap.put("lagnaLordStrength", lagnaLordStrong ? "Strong / Dignified" : "Moderate");
        parasharaBalaMap.put("deerghayuYogaPresent", kendraBeneficCount >= 1 && lagnaLordStrong);

        // 4. PRINCIPLE 3: Maraka & Badhaka Timeline & Remedies
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
        String activeMarakaDasaInfo = "Ages " + (baseCeilingAge - 3) + " to " + (baseCeilingAge + 3) +
                " (~" + (targetYear - 3) + " - " + (targetYear + 3) + ") Maraka / Badhaka / Ashtamadhipati Dasa-Bhukti caution period.";

        if (dasaTimeline != null && !dasaTimeline.isEmpty()) {
            LocalDate targetDate = LocalDate.of(Math.max(1900, targetYear), 6, 15);
            for (DasaPeriod d : dasaTimeline) {
                if (d.getStartDate() != null && d.getEndDate() != null
                        && !targetDate.isBefore(d.getStartDate()) && !targetDate.isAfter(d.getEndDate())) {
                    activeMarakaDasaInfo = d.getPlanetName() + " Mahadasa period (around age " + (baseCeilingAge - 3) +
                            " - " + (baseCeilingAge + 3) + ") calls for health caution and mindful care.";
                    break;
                }
            }
        }

        Map<String, Object> marakaTimelineMap = new LinkedHashMap<>();
        marakaTimelineMap.put("marakaLords", marakaLord2 + " (2nd) & " + marakaLord7 + " (7th)");
        marakaTimelineMap.put("badhakaLord", badhakaLord + " (" + getModality(lagnaSign) + " Lagna)");
        marakaTimelineMap.put("criticalDasaWindow", activeMarakaDasaInfo);
        marakaTimelineMap.put("recommendedRemedies", "Maha Mrityunjaya Japa, Shiva Abhishekam, and Dhanvantari prayer");

        String lifespanRangeStr = (baseCeilingAge - 4) + " - " + (baseCeilingAge + 4) + " Years (~" + (targetYear - 4) + " - " + (targetYear + 4) + ")";

        // Assemble Jaimini 3-Pair Map
        Map<String, Object> threePairsMap = new LinkedHashMap<>();
        threePairsMap.put("pair1_lagnaLord_and_8thLord", Map.of(
                "planets", lagnaLord + " (" + mLL + ") & " + eighthLord + " (" + m8L + ")",
                "derivedSpan", span1
        ));
        threePairsMap.put("pair2_moon_and_saturn", Map.of(
                "planets", "Moon (" + mMoon + ") & Saturn (" + mSat + ")",
                "derivedSpan", span2
        ));
        threePairsMap.put("pair3_lagna_and_horaLagna", Map.of(
                "planets", "Lagna (" + mLagna + ") & Hora Lagna (" + mHL + ")",
                "derivedSpan", span3
        ));
        threePairsMap.put("majorityConsensus", baseSpan);

        String rationale = "Determined via Parashara & Jaimini Ayurdaya based on Lagna Lord (" + lagnaLord +
                "), 8th Lord (" + eighthLord + "), Moon, Saturn, and Hora Lagna modalities, refined with Kakshya Vriddhi and Shadbala life-force: " +
                baseSpan + ". " + String.join(" ", adjustments);

        return new AyurdayaProfile(
                baseSpan,
                baseCeilingAge,
                lifespanRangeStr,
                threePairsMap,
                threePairsMap,
                parasharaBalaMap,
                marakaTimelineMap,
                adjustments,
                activeMarakaDasaInfo,
                rationale
        );
    }
}
