package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.DasaPeriod;

import java.time.LocalDate;
import java.util.*;

/**
 * Deterministic 3-Principle Parashara & Jaimini Ayurdaya (Longevity Determination) Engine.
 * 
 * Evaluates:
 * 1. Jaimini 3-Pair Modality System (with Hora Lagna and Kakshya Vriddhi/Hrasa rules).
 * 2. Parashara & Shadbala Life-Force Strength (Sarira Bala, Jeeva Bala, Ayushkaraka Bala & Yogas).
 * 3. Maraka & Badhaka Planetary Health Timeline & Remedial Guidelines.
 */
public class AyurdayaCalculationUtils {

    public static final Map<String, Double> MIN_SHADBALA_RUPAS = Map.of(
            "Sun", 6.5,
            "Moon", 6.0,
            "Mars", 5.0,
            "Mercury", 7.0,
            "Jupiter", 6.5,
            "Venus", 5.5,
            "Saturn", 5.0
    );

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
        return calculateAyurdaya(lagnaSign, moonSign, d1Chart, dasaTimeline, birthYear, 12, 0, null);
    }

    public static AyurdayaProfile calculateAyurdaya(
            int lagnaSign,
            int moonSign,
            List<ChartResponseDTO.PositionDetail> d1Chart,
            List<DasaPeriod> dasaTimeline,
            int birthYear,
            int birthHour,
            int birthMinute) {
        return calculateAyurdaya(lagnaSign, moonSign, d1Chart, dasaTimeline, birthYear, birthHour, birthMinute, null);
    }

    public static AyurdayaProfile calculateAyurdaya(
            int lagnaSign,
            int moonSign,
            List<ChartResponseDTO.PositionDetail> d1Chart,
            List<DasaPeriod> dasaTimeline,
            int birthYear,
            int birthHour,
            int birthMinute,
            ShadbalaDTO shadbala) {

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

        // Majority Resolution & Classical Exceptions across the 3 Jaimini pairs
        Map<String, Integer> votes = new HashMap<>();
        votes.put("Poornayu", 0);
        votes.put("Madhyayu", 0);
        votes.put("Alpayu", 0);
        votes.put(span1, votes.get(span1) + 1);
        votes.put(span2, votes.get(span2) + 1);
        votes.put(span3, votes.get(span3) + 1);

        String baseSpan;
        List<String> adjustments = new ArrayList<>();

        int moonHouse = ((activeMoonSign - lagnaSign + 12) % 12) + 1;

        // Classical Jaimini Resolution Priority (BPHS Ch 45 & JUS 2.1.15-25):
        // 1. Primary Rule (Dwi-Samvada): If 2 or 3 pairs agree on the same span, that majority consensus prevails.
        if (votes.get("Poornayu") >= 2) {
            baseSpan = "Poornayu";
        } else if (votes.get("Madhyayu") >= 2) {
            baseSpan = "Madhyayu";
        } else if (votes.get("Alpayu") >= 2) {
            baseSpan = "Alpayu";
        } else {
            // 2. Ambiguity Tie-Breaker (All 3 pairs give 3 distinct spans: Poorna, Madhya, Alpa)
            if (moonHouse == 1 || moonHouse == 7) {
                // Classical Exception: Moon in 1st or 7th House decides tie (Jaimini Upadesha Sutra 2.1.23)
                baseSpan = span2;
                adjustments.add("All 3 pairs distinct with Moon in " + (moonHouse == 1 ? "Lagna (1st)" : "7th house") +
                        ": Moon-Saturn pair (Pair 2) decides tie-breaker (Jaimini Sutra 2.1.23).");
            } else {
                // Odd vs Even Lagna Tie-Breaker (Jaimini Sutra 2.1.24)
                boolean isOddLagna = (lagnaSign % 2 != 0);
                baseSpan = isOddLagna ? span3 : span1;
                adjustments.add("All 3 pairs indicate distinct spans: " +
                        (isOddLagna ? "Odd Lagna gives precedence to Lagna-Hora Lagna (Pair 3)."
                                : "Even Lagna gives precedence to Lagna Lord-8th Lord (Pair 1)."));
            }
        }

        // Classical Ayurdaya Baseline Compartments (Alpayu: 0-36, Madhyayu: 36-72, Poornayu: 72-108)
        int baseCeilingAge = switch (baseSpan) {
            case "Poornayu" -> 84;
            case "Madhyayu" -> 68;
            case "Alpayu" -> 34;
            default -> 72;
        };

        // Kakshya Vriddhi (Longevity Expansion - Jaimini Upadesha Sutras 2.1.26-30)
        // Factor A: Jupiter in Kendra (1,4,7,10), Trikona (5,9), or Exalted/Own Sign
        if (jupiterPos != null) {
            int jupHouse = ((jupiterPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            boolean jupStrong = PlanetDignityUtils.isOwnSign("Jupiter", jupiterPos.getSignNumber()) ||
                    PlanetDignityUtils.isExalted("Jupiter", jupiterPos.getSignNumber());
            if (jupHouse == 1 || jupHouse == 4 || jupHouse == 7 || jupHouse == 10 || jupHouse == 5 || jupHouse == 9 || jupStrong) {
                adjustments.add("Jupiter benefic Kendra/Trikona placement confers Kakshya Vriddhi (Longevity compartment elevation).");
                if ("Alpayu".equals(baseSpan)) {
                    baseSpan = "Madhyayu";
                    baseCeilingAge = 68;
                } else if ("Madhyayu".equals(baseSpan)) {
                    baseSpan = "Poornayu";
                    baseCeilingAge = 82;
                } else {
                    baseCeilingAge += 4;
                }
            }
        }

        // Factor B: Ayushkaraka Saturn in Own Sign / Exalted
        if (saturnPos != null) {
            if (PlanetDignityUtils.isOwnSign("Saturn", saturnPos.getSignNumber()) || PlanetDignityUtils.isExalted("Saturn", saturnPos.getSignNumber())) {
                adjustments.add("Ayushkaraka Saturn in Own/Exalted sign reinforces longevity (+4 years).");
                baseCeilingAge += 4;
            }
        }

        // Factor C: Lagna Lord in Kendra / Trikona / Exalted
        if (lagnaLordPos != null) {
            int llHouse = ((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (PlanetDignityUtils.isExalted(lagnaLord, lagnaLordPos.getSignNumber()) || PlanetDignityUtils.isOwnSign(lagnaLord, lagnaLordPos.getSignNumber())) {
                adjustments.add("Lagna Lord strong in own/exalted sign adds physical vitality (+4 years).");
                baseCeilingAge += 4;
            }
        }

        // Kakshya Hrasa (Longevity Reductions)
        // Factor 1: Ayushkaraka Saturn in Debility (in Aries)
        if (saturnPos != null && PlanetDignityUtils.isDebilitated("Saturn", saturnPos.getSignNumber())) {
            boolean saturnNeechaBhanga = hasNeechabhanga("Saturn", saturnPos.getSignNumber(), planetMap, lagnaSign, activeMoonSign);
            if (saturnNeechaBhanga) {
                adjustments.add("Ayushkaraka Saturn possesses Neecha Bhanga (cancellation of debility into longevity stability).");
                baseCeilingAge += 2;
            } else {
                adjustments.add("Ayushkaraka Saturn in debility applies Kakshya Hrasa reduction (-5 years).");
                baseCeilingAge -= 5;
                if ("Poornayu".equals(baseSpan) && baseCeilingAge < 74) baseSpan = "Madhyayu";
                else if ("Madhyayu".equals(baseSpan) && baseCeilingAge <= 42) baseSpan = "Alpayu";
            }
        }

        // Factor 2: Lagna Lord in Dusthana (6/8/12) and Debilitated
        if (lagnaLordPos != null) {
            int llHouse = ((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (llHouse == 6 || llHouse == 8 || llHouse == 12) {
                if (PlanetDignityUtils.isDebilitated(lagnaLord, lagnaLordPos.getSignNumber())) {
                    adjustments.add("Lagna Lord debilitated in Dusthana applies Kakshya Hrasa (-4 years).");
                    baseCeilingAge -= 4;
                } else {
                    adjustments.add("Lagna Lord in Dusthana (6/8/12) advises mindful health regimen.");
                    baseCeilingAge -= 2;
                }
            }
        }

        // Factor 3: Papakarthari Yoga on Lagna (12th and 2nd occupied by natural malefics)
        int h12Sign = ((lagnaSign + 12 - 1 - 1) % 12) + 1;
        int h2Sign = ((lagnaSign + 2 - 1 - 1) % 12) + 1;
        boolean h12Malefic = false;
        boolean h2Malefic = false;
        for (var mKey : List.of("SUN", "MARS", "SATURN", "RAHU", "KETU")) {
            var p = planetMap.get(mKey);
            if (p != null) {
                if (p.getSignNumber() == h12Sign) h12Malefic = true;
                if (p.getSignNumber() == h2Sign) h2Malefic = true;
            }
        }
        if (h12Malefic && h2Malefic) {
            adjustments.add("Lagna hemmed between malefics in 12th & 2nd (Papakarthari Yoga) cautions physical vitality (-3 years).");
            baseCeilingAge -= 3;
        }

        // 3. PRINCIPLE 2: Parashara & Shadbala Life-Force (Sarira, Jeeva & Ayushkaraka Bala)
        // A. Sarira Bala (Lagna Lord & Deha Constitution)
        double minLL = MIN_SHADBALA_RUPAS.getOrDefault(capitalize(lagnaLord), 5.5);
        double llRupas = getPlanetRupas(shadbala, lagnaLord, lagnaLordPos, lagnaSign);
        int llHouse = lagnaLordPos != null ? ((lagnaLordPos.getSignNumber() - lagnaSign + 12) % 12) + 1 : 1;
        double rawLLRatio = llRupas / Math.max(1.0, minLL);

        double dignityMultiplier = 1.0;
        if (lagnaLordPos != null) {
            if (PlanetDignityUtils.isExalted(lagnaLord, lagnaLordPos.getSignNumber())) dignityMultiplier += 0.20;
            else if (PlanetDignityUtils.isOwnSign(lagnaLord, lagnaLordPos.getSignNumber())) dignityMultiplier += 0.12;
            else if (PlanetDignityUtils.isDebilitated(lagnaLord, lagnaLordPos.getSignNumber())) dignityMultiplier -= 0.20;

            if (llHouse == 1 || llHouse == 4 || llHouse == 7 || llHouse == 10) dignityMultiplier += 0.10;
            else if (llHouse == 5 || llHouse == 9) dignityMultiplier += 0.08;
            else if (llHouse == 6 || llHouse == 8 || llHouse == 12) dignityMultiplier -= 0.15;
        }
        double sariraRatio = Math.max(0.40, Math.min(2.0, rawLLRatio * dignityMultiplier));

        String sariraStatus;
        if (sariraRatio >= 1.20) sariraStatus = "VERY_STRONG";
        else if (sariraRatio >= 1.00) sariraStatus = "STRONG";
        else if (sariraRatio >= 0.82) sariraStatus = "MODERATE";
        else sariraStatus = "CAUTIOUS";

        // B. Jeeva Bala (Jupiter as Jeeva Karaka & Moon as Prana/Mana Karaka)
        double jupRupas = getPlanetRupas(shadbala, "Jupiter", jupiterPos, lagnaSign);
        double moonRupas = getPlanetRupas(shadbala, "Moon", moonPos, lagnaSign);
        double jupRatio = jupRupas / 6.5;
        double moonRatio = moonRupas / 6.0;

        if (jupiterPos != null) {
            int jh = ((jupiterPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (jh == 1 || jh == 4 || jh == 7 || jh == 10 || jh == 5 || jh == 9) jupRatio *= 1.12;
            if (PlanetDignityUtils.isExalted("Jupiter", jupiterPos.getSignNumber()) || PlanetDignityUtils.isOwnSign("Jupiter", jupiterPos.getSignNumber())) jupRatio *= 1.10;
            else if (jh == 6 || jh == 8 || jh == 12) jupRatio *= 0.90;
        }
        if (moonPos != null && sunPos != null) {
            double moonAbs = ((moonPos.getSignNumber() - 1) * 30.0) + moonPos.getDegreeInSign();
            double sunAbs = ((sunPos.getSignNumber() - 1) * 30.0) + sunPos.getDegreeInSign();
            double elongation = (moonAbs - sunAbs + 720.0) % 360.0;
            if (elongation >= 120.0 && elongation <= 240.0) moonRatio *= 1.10; // Full / Bright Moon
        }

        double jeevaRatio = Math.max(0.40, Math.min(2.0, (0.60 * jupRatio) + (0.40 * moonRatio)));
        String jeevaStatus;
        if (jeevaRatio >= 1.18) jeevaStatus = "RADIANT";
        else if (jeevaRatio >= 1.00) jeevaStatus = "SOUND";
        else if (jeevaRatio >= 0.82) jeevaStatus = "BALANCED";
        else jeevaStatus = "CAUTIOUS";

        // C. Ayushkaraka Bala (Saturn Longevity Shield)
        double saturnRupas = getPlanetRupas(shadbala, "Saturn", saturnPos, lagnaSign);
        double saturnRatio = saturnRupas / 5.0;
        if (saturnPos != null) {
            int sh = ((saturnPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (PlanetDignityUtils.isExalted("Saturn", saturnPos.getSignNumber()) || PlanetDignityUtils.isOwnSign("Saturn", saturnPos.getSignNumber())) saturnRatio *= 1.15;
            if (sh == 3 || sh == 6 || sh == 11) saturnRatio *= 1.10; // Upachaya joy for Saturn
            else if (PlanetDignityUtils.isDebilitated("Saturn", saturnPos.getSignNumber())) saturnRatio *= 0.82;
        }
        saturnRatio = Math.max(0.40, Math.min(2.0, saturnRatio));

        String ayurBalaStatus;
        if (saturnRatio >= 1.15) ayurBalaStatus = "EXALTED_SHIELD";
        else if (saturnRatio >= 0.98) ayurBalaStatus = "SOUND_PROTECTION";
        else if (saturnRatio >= 0.80) ayurBalaStatus = "MODERATE_SHIELD";
        else ayurBalaStatus = "REMEDY_ADVISED";

        // D. Parashara Longevity Yogas (Kendra Benefics & Upachaya Malefics)
        int kendraBeneficCount = 0;
        for (var benefic : List.of("JUPITER", "VENUS", "MERCURY")) {
            ChartResponseDTO.PositionDetail p = planetMap.get(benefic);
            if (p != null) {
                int h = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
                if (h == 1 || h == 4 || h == 7 || h == 10) kendraBeneficCount++;
            }
        }
        if (moonPos != null) {
            int mh = ((moonPos.getSignNumber() - lagnaSign + 12) % 12) + 1;
            if (mh == 1 || mh == 4 || mh == 7 || mh == 10) {
                if (sunPos != null) {
                    double moonAbs = ((moonPos.getSignNumber() - 1) * 30.0) + moonPos.getDegreeInSign();
                    double sunAbs = ((sunPos.getSignNumber() - 1) * 30.0) + sunPos.getDegreeInSign();
                    double el = (moonAbs - sunAbs + 720.0) % 360.0;
                    if (el > 60.0 && el < 300.0) kendraBeneficCount++;
                }
            }
        }

        int upachayaMaleficCount = 0;
        for (var malefic : List.of("SUN", "MARS", "SATURN", "RAHU", "KETU")) {
            ChartResponseDTO.PositionDetail p = planetMap.get(malefic);
            if (p != null) {
                int h = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
                if (h == 3 || h == 6 || h == 11) upachayaMaleficCount++;
            }
        }

        double eighthLordRupas = getPlanetRupas(shadbala, eighthLord, eighthLordPos, lagnaSign);
        String lagnaVs8th;
        if (llRupas >= eighthLordRupas + 0.35) {
            lagnaVs8th = "Lagna Lord Stronger (High Immunity)";
        } else if (Math.abs(llRupas - eighthLordRupas) <= 0.35) {
            lagnaVs8th = "Equally Balanced";
        } else {
            lagnaVs8th = "8th Lord Dominant (Health Caution Advised)";
        }

        // Composite Vitality Index
        double yogaBonus = (kendraBeneficCount * 0.035) + (Math.min(3, upachayaMaleficCount) * 0.025)
                + (llRupas > eighthLordRupas ? 0.04 : 0.0);
        double compositeIndex = (0.40 * sariraRatio) + (0.35 * jeevaRatio) + (0.25 * saturnRatio) + yogaBonus;

        String ayurBalaClassification;
        if (compositeIndex >= 1.15) {
            ayurBalaClassification = "High Resilience & Deerghayu Vitality";
        } else if (compositeIndex >= 1.02) {
            ayurBalaClassification = "Robust Sarira & Jeeva Strength";
        } else if (compositeIndex >= 0.88) {
            ayurBalaClassification = "Balanced Constitutional Vitality";
        } else if (compositeIndex >= 0.76) {
            ayurBalaClassification = "Moderate Vitality (Mindful Regimen)";
        } else {
            ayurBalaClassification = "Health-Cautious Vitality";
        }

        boolean parasharaYogaPresent = (kendraBeneficCount >= 1 && sariraRatio >= 0.95)
                || (upachayaMaleficCount >= 2 && sariraRatio >= 0.90);

        Map<String, Object> sariraMap = new LinkedHashMap<>();
        sariraMap.put("rulingPlanet", lagnaLord);
        sariraMap.put("rupas", Math.round(llRupas * 100.0) / 100.0);
        sariraMap.put("minRequiredRupas", minLL);
        sariraMap.put("ratioPercentage", (int) Math.round(sariraRatio * 100));
        sariraMap.put("status", sariraStatus);
        sariraMap.put("housePlacement", llHouse);

        Map<String, Object> jeevaMap = new LinkedHashMap<>();
        jeevaMap.put("jupiterRupas", Math.round(jupRupas * 100.0) / 100.0);
        jeevaMap.put("moonRupas", Math.round(moonRupas * 100.0) / 100.0);
        jeevaMap.put("ratioPercentage", (int) Math.round(jeevaRatio * 100));
        jeevaMap.put("status", jeevaStatus);

        Map<String, Object> ayurMap = new LinkedHashMap<>();
        ayurMap.put("saturnRupas", Math.round(saturnRupas * 100.0) / 100.0);
        ayurMap.put("ratioPercentage", (int) Math.round(saturnRatio * 100));
        ayurMap.put("status", ayurBalaStatus);

        Map<String, Object> parasharaBalaMap = new LinkedHashMap<>();
        parasharaBalaMap.put("vitalityScore", ayurBalaClassification);
        parasharaBalaMap.put("compositeScoreValue", Math.round(compositeIndex * 100.0) / 100.0);
        parasharaBalaMap.put("sariraBala", sariraMap);
        parasharaBalaMap.put("jeevaBala", jeevaMap);
        parasharaBalaMap.put("ayurBala", ayurMap);
        parasharaBalaMap.put("kendraBeneficsCount", kendraBeneficCount);
        parasharaBalaMap.put("upachayaMaleficsCount", upachayaMaleficCount);
        parasharaBalaMap.put("lagnaLordStrength", sariraStatus);
        parasharaBalaMap.put("lagnaLordVs8thLord", lagnaVs8th);
        parasharaBalaMap.put("deerghayuYogaPresent", parasharaYogaPresent);

        // 4. PRINCIPLE 3: Classical Parashara & Jaimini Maraka & Badhaka Timeline & Remedies
        int marakaSign2 = ((lagnaSign + 2 - 1 - 1) % 12) + 1;
        int marakaSign7 = ((lagnaSign + 7 - 1 - 1) % 12) + 1;
        String marakaLord2 = PlanetDignityUtils.getSignLord(marakaSign2);
        String marakaLord7 = PlanetDignityUtils.getSignLord(marakaSign7);

        // Badhaka Sthana & Lord: 11th for Chara Lagna, 9th for Sthira Lagna, 7th for Dwisvabhava Lagna
        int badhakaSign = switch (getModality(lagnaSign)) {
            case CHARA -> ((lagnaSign + 11 - 1 - 1) % 12) + 1;
            case STHIRA -> ((lagnaSign + 9 - 1 - 1) % 12) + 1;
            case DWISVABHAVA -> ((lagnaSign + 7 - 1 - 1) % 12) + 1;
        };
        int badhakaHouseNumber = switch (getModality(lagnaSign)) {
            case CHARA -> 11;
            case STHIRA -> 9;
            case DWISVABHAVA -> 7;
        };
        String badhakaLord = PlanetDignityUtils.getSignLord(badhakaSign);

        // Identify Occupants in Maraka & Badhaka Houses (BPHS Sloka 44.3-5)
        List<String> marakaOccupants2 = new ArrayList<>();
        List<String> marakaOccupants7 = new ArrayList<>();
        List<String> badhakaOccupants = new ArrayList<>();
        List<String> marakaOccupantPlanets = new ArrayList<>();

        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                if (p.getPlanetKey() == null || "LAGNA".equalsIgnoreCase(p.getPlanetKey())) continue;
                String pName = capitalize(p.getPlanetKey());
                int h = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
                if (h == 2) {
                    marakaOccupants2.add(pName);
                    marakaOccupantPlanets.add(pName);
                } else if (h == 7) {
                    marakaOccupants7.add(pName);
                    marakaOccupantPlanets.add(pName);
                }
                if (h == badhakaHouseNumber) {
                    badhakaOccupants.add(pName);
                }
            }
        }

        // Lagna Lord Exemption / Dosha Nivritti Check
        String lagnaLordExemption = null;
        if (lagnaSign == 10) { // Capricorn: Saturn rules 1 & 2
            lagnaLordExemption = "Saturn rules both Lagna (1st) and 2nd house; Lagna lordship confers protective immunity from Maraka affliction.";
        } else if (lagnaSign == 1 || lagnaSign == 8) { // Aries/Scorpio: Mars rules 1 & 8/6
            lagnaLordExemption = "Mars rules Lagna; Lagna lordship neutralizes evil Ashtama/Roga dosha.";
        } else if (lagnaSign == 7 || lagnaSign == 2) { // Libra/Taurus: Venus rules 1 & 8/6
            lagnaLordExemption = "Venus rules Lagna; Lagna lordship mitigates Ashtamadhipatya/Roga affliction.";
        } else if (lagnaSign == 11) { // Aquarius: Saturn rules 1 & 12
            lagnaLordExemption = "Saturn rules Lagna and 12th; Lagna lordship grants natural constitutional resilience.";
        }

        // 22nd Drekkana Lord (Kharesha) Calculation
        ChartResponseDTO.PositionDetail lagnaPos = planetMap.get("LAGNA");
        double lagnaDeg = lagnaPos != null ? lagnaPos.getDegreeInSign() : 15.0;
        int eighthSignD1 = ((lagnaSign + 8 - 1 - 1) % 12) + 1;
        int khareshaSign;
        if (lagnaDeg < 10.0) {
            khareshaSign = eighthSignD1;
        } else if (lagnaDeg < 20.0) {
            khareshaSign = ((eighthSignD1 + 5 - 1 - 1) % 12) + 1;
        } else {
            khareshaSign = ((eighthSignD1 + 9 - 1 - 1) % 12) + 1;
        }
        String khareshaLord = PlanetDignityUtils.getSignLord(khareshaSign);

        // Micro-Timing: Exact Dasa & Bhukthi (Sub-period) Resolution at Longevity Transition Window
        int targetYear = birthYear + baseCeilingAge;
        String activeMahadasa = marakaLord2;
        String activeBhukthi = marakaLord7;
        String activeBhukthiRange = "";
        boolean isMarakaBhukthi = false;
        String activeMarakaPlanet = marakaLord2;

        if (dasaTimeline != null && !dasaTimeline.isEmpty()) {
            LocalDate targetDate = LocalDate.of(Math.max(1900, targetYear), 6, 15);
            for (DasaPeriod d : dasaTimeline) {
                if (d.getStartDate() != null && d.getEndDate() != null
                        && !targetDate.isBefore(d.getStartDate()) && !targetDate.isAfter(d.getEndDate())) {
                    activeMahadasa = d.getPlanetName();
                    activeMarakaPlanet = d.getPlanetName();

                    // Search for exact sub-period (Bhukthi)
                    if (d.getBhukthis() != null && !d.getBhukthis().isEmpty()) {
                        for (DasaPeriod.BhukthiPeriod b : d.getBhukthis()) {
                            if (b.getStartDate() != null && b.getEndDate() != null
                                    && !targetDate.isBefore(b.getStartDate()) && !targetDate.isAfter(b.getEndDate())) {
                                activeBhukthi = b.getPlanetName();
                                activeBhukthiRange = b.getStartDate().getYear() + "-" + String.format("%02d", b.getStartDate().getMonthValue())
                                        + " to " + b.getEndDate().getYear() + "-" + String.format("%02d", b.getEndDate().getMonthValue());

                                if (activeBhukthi.equalsIgnoreCase(marakaLord2) || activeBhukthi.equalsIgnoreCase(marakaLord7)
                                        || activeBhukthi.equalsIgnoreCase(badhakaLord) || activeBhukthi.equalsIgnoreCase(eighthLord)
                                        || activeBhukthi.equalsIgnoreCase(khareshaLord) || marakaOccupantPlanets.contains(capitalize(activeBhukthi))) {
                                    isMarakaBhukthi = true;
                                    activeMarakaPlanet = activeBhukthi;
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }

        String activeMarakaDasaInfo;
        if (!activeBhukthiRange.isEmpty()) {
            activeMarakaDasaInfo = activeMahadasa + " Mahadasa - " + activeBhukthi + " Bhukthi (" + activeBhukthiRange +
                    ", ~Age " + (baseCeilingAge - 2) + "-" + (baseCeilingAge + 2) + ") represents the primary classical Maraka/Badhaka cautionary period.";
        } else {
            activeMarakaDasaInfo = activeMahadasa + " Mahadasa period (around age " + (baseCeilingAge - 3) +
                    " - " + (baseCeilingAge + 3) + ") calls for health caution and mindful care.";
        }

        // Multi-Tiered Classical Vedic Remedies
        String badhakaRemedy = switch (getModality(lagnaSign)) {
            case CHARA -> "Kula Devata (Family Deity) Puja, temple ghee lamp, and Annadanam to alleviate 11th Badhaka obstacles.";
            case STHIRA -> "Dharma Devata / Pitru Puja, temple pilgrimage, and charity to appease 9th Badhaka.";
            case DWISVABHAVA -> "Sri Maha Vishnu & Shiva Sahasranama, couples charity to harmonize 7th Maraka-Badhaka force.";
        };

        String tailoredRemedy = getPlanetSpecificRemedy(activeMarakaPlanet);

        Map<String, Object> marakaTimelineMap = new LinkedHashMap<>();
        marakaTimelineMap.put("marakaLords", marakaLord2 + " (2nd) & " + marakaLord7 + " (7th)");
        marakaTimelineMap.put("marakaLord2", marakaLord2);
        marakaTimelineMap.put("marakaLord7", marakaLord7);
        marakaTimelineMap.put("marakaOccupants2", marakaOccupants2);
        marakaTimelineMap.put("marakaOccupants7", marakaOccupants7);
        marakaTimelineMap.put("badhakaLord", badhakaLord + " (" + badhakaHouseNumber + "th House - " + getModality(lagnaSign) + " Lagna)");
        marakaTimelineMap.put("badhakaOccupants", badhakaOccupants);
        marakaTimelineMap.put("khareshaLord", khareshaLord + " (22nd Drekkana Lord)");
        marakaTimelineMap.put("lagnaLordExemption", lagnaLordExemption);
        marakaTimelineMap.put("activeMahadasa", activeMahadasa);
        marakaTimelineMap.put("activeBhukthi", activeBhukthi);
        marakaTimelineMap.put("activeBhukthiRange", activeBhukthiRange);
        marakaTimelineMap.put("isMarakaBhukthi", isMarakaBhukthi);
        marakaTimelineMap.put("activeMarakaPlanet", activeMarakaPlanet);
        marakaTimelineMap.put("criticalDasaWindow", activeMarakaDasaInfo);
        marakaTimelineMap.put("badhakaRemedies", badhakaRemedy);
        marakaTimelineMap.put("tailoredDasaRemedies", tailoredRemedy);
        marakaTimelineMap.put("universalRemedies", "Maha Mrityunjaya Mantra Japa (108 times daily), Ayushya Homa, and Lord Dhanvantari Prayer");
        marakaTimelineMap.put("recommendedRemedies", tailoredRemedy + " Along with " + badhakaRemedy);

        String lifespanRangeStr = switch (baseSpan) {
            case "Poornayu" -> (baseCeilingAge - 5) + " - " + Math.min(105, baseCeilingAge + 8) + " Years (~" + (targetYear - 5) + " - " + (targetYear + 8) + ")";
            case "Madhyayu" -> Math.max(36, baseCeilingAge - 6) + " - " + Math.min(74, baseCeilingAge + 4) + " Years (~" + (targetYear - 6) + " - " + (targetYear + 4) + ")";
            case "Alpayu" -> "0 - " + Math.min(38, baseCeilingAge + 3) + " Years (~" + birthYear + " - " + (birthYear + Math.min(38, baseCeilingAge + 3)) + ")";
            default -> (baseCeilingAge - 4) + " - " + (baseCeilingAge + 4) + " Years (~" + (targetYear - 4) + " - " + (targetYear + 4) + ")";
        };

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

    private static double getPlanetRupas(
            ShadbalaDTO shadbala,
            String planet,
            ChartResponseDTO.PositionDetail pos,
            int lagnaSign) {
        if (planet == null) return 5.5;
        String normPlanet = capitalize(planet);
        if (shadbala != null && shadbala.getPlanetStrengths() != null) {
            var ps = shadbala.getPlanetStrengths().get(normPlanet);
            if (ps != null && ps.getTotalShadbalaRupas() > 0.0) {
                return ps.getTotalShadbalaRupas();
            }
        }
        // Fallback calculation from dignity & house if ShadbalaDTO not provided
        double minReq = MIN_SHADBALA_RUPAS.getOrDefault(normPlanet, 5.5);
        if (pos == null) return minReq;
        double rupas = minReq;
        int house = ((pos.getSignNumber() - lagnaSign + 12) % 12) + 1;
        if (PlanetDignityUtils.isExalted(normPlanet, pos.getSignNumber())) rupas += 1.8;
        else if (PlanetDignityUtils.isOwnSign(normPlanet, pos.getSignNumber())) rupas += 1.2;
        else if (PlanetDignityUtils.isDebilitated(normPlanet, pos.getSignNumber())) rupas -= 1.5;

        if (house == 1 || house == 4 || house == 7 || house == 10) rupas += 0.8;
        else if (house == 5 || house == 9) rupas += 0.6;
        else if (house == 6 || house == 8 || house == 12) rupas -= 0.6;

        return Math.max(2.5, Math.round(rupas * 100.0) / 100.0);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static String getPlanetSpecificRemedy(String planet) {
        if (planet == null) return "Maha Mrityunjaya Japa, Shiva Abhishekam, and Dhanvantari prayer";
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya" -> "Lord Shiva Puja, Aditya Hridaya Stotram, and Maha Mrityunjaya Mantra Japa.";
            case "moon", "chandra" -> "Goddess Parvati & Shiva Puja, Chandra Kavacham, and Amriteshwara Prayer.";
            case "mars", "kuja", "sevvai", "mangal" -> "Lord Subramanya / Kartikeya Puja, Dhanvantari Stotram, and Rudra Abhishekam.";
            case "mercury", "budha" -> "Sri Vishnu Sahasranama Parayanam, Sudarshana Ashtakam, and Tulasi Archana.";
            case "jupiter", "guru" -> "Lord Dakshinamurthy Puja, Guru Gayatri Japa, and Annadanam (food charity).";
            case "venus", "shukra" -> "Sri Maha Lakshmi Ashtakam, Sri Suktam Parayanam, and Gho (Cow) Puja.";
            case "saturn", "shani" -> "Maha Mrityunjaya Japa, Lord Shiva Milk & Til Abhishekam, and Hanuman Chalisa.";
            case "rahu" -> "Goddess Durga Puja, Rahu Kalam Ghee Lamp, and Maha Mrityunjaya Japa.";
            case "ketu" -> "Maha Ganapati Homa/Puja, Ganesha Atharvashirsha Parayanam, and Meditation.";
            default -> "Maha Mrityunjaya Japa, Shiva Abhishekam, and Dhanvantari prayer";
        };
    }

    private static boolean hasNeechabhanga(
            String planet,
            int sign,
            Map<String, ChartResponseDTO.PositionDetail> map,
            int lagnaSign,
            int moonSign) {
        if (planet == null || !PlanetDignityUtils.isDebilitated(planet, sign)) return false;
        String pKey = planet.toUpperCase();
        String dispositor = PlanetDignityUtils.getSignLord(sign);
        int exSign = PlanetDignityUtils.getExaltationSign(planet);
        String exLord = PlanetDignityUtils.getSignLord(exSign);

        var lordPos = map.get(dispositor.toUpperCase());
        var exLordPos = map.get(exLord.toUpperCase());

        // Law 1: Dispositor in Kendra from Lagna or Moon
        if (lordPos != null) {
            int hL = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), lagnaSign);
            int hM = PlanetDignityUtils.getHouseFromLagna(lordPos.getSignNumber(), moonSign);
            if (PlanetDignityUtils.isKendra(hL) || PlanetDignityUtils.isKendra(hM)) return true;
        }
        // Law 2: Exaltation lord in Kendra from Lagna or Moon, or Exalted
        if (exLordPos != null) {
            int hL = PlanetDignityUtils.getHouseFromLagna(exLordPos.getSignNumber(), lagnaSign);
            int hM = PlanetDignityUtils.getHouseFromLagna(exLordPos.getSignNumber(), moonSign);
            if (PlanetDignityUtils.isKendra(hL) || PlanetDignityUtils.isKendra(hM) || PlanetDignityUtils.isExalted(exLord, exLordPos.getSignNumber())) return true;
        }
        // Law 3: Exalted companion in same sign
        for (var entry : map.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(pKey) || entry.getKey().equalsIgnoreCase("LAGNA")) continue;
            if (entry.getValue().getSignNumber() == sign && PlanetDignityUtils.isExalted(entry.getKey(), entry.getValue().getSignNumber())) {
                return true;
            }
        }
        return false;
    }
}
