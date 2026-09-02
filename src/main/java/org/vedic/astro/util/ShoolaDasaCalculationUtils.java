package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;

import java.util.*;

/**
 * Maharishi Jaimini's Classical Shoola Dasa (108-year sign-based Ayur Dasa) Engine.
 *
 * Core Features:
 * 1. Starting Sign Selection: Compares Lagna (1st house) vs 7th house based on planetary conjunctions,
 *    exaltation/own-sign dignities, and Jupiter/benefic influences.
 * 2. Progression Direction: Direct (Savya) for odd starting signs; Reverse (Apasavya) for even starting signs.
 * 3. 12 Mahadasas of exactly 9 years each (0 to 108 years).
 * 4. 12 Antardasas per Mahadasa of exactly 9 months each (0.75 years).
 * 5. Trishoola Signs: 1st, 5th, and 9th from 8th house (representing Lord Shiva's trident of destruction).
 * 6. Rudra Sign & Planet: Stronger of 2nd lord and 8th lord, and its occupied sign.
 * 7. Critical Window Alignment: Mapping native's calculated Ayurdaya ceiling age to Shoola Mahadasa/Antardasa.
 */
public class ShoolaDasaCalculationUtils {

    public record ShoolaPeriod(
            int periodIndex,
            int signNumber,
            String signName,
            int startYear,
            int endYear,
            int startAge,
            int endAge,
            boolean isTrishoola,
            boolean isRudra,
            String riskCategory,
            List<ShoolaAntardasaPeriod> antardasas
    ) {}

    public record ShoolaAntardasaPeriod(
            int subIndex,
            int signNumber,
            String signName,
            String startMonthYear,
            String endMonthYear
    ) {}

    public record ShoolaDasaReport(
            String startingSignName,
            int startingSignNumber,
            String startingSignReason,
            String progressionDirection,
            String rudraSignName,
            int rudraSignNumber,
            String rudraPlanetName,
            List<String> trishoolaSignNames,
            List<Integer> trishoolaSignNumbers,
            List<ShoolaPeriod> periods,
            String criticalShoolaWindow,
            String classicalRationale
    ) {}

    public static ShoolaDasaReport calculateShoolaDasa(
            int lagnaSign,
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            int birthYear,
            int targetLifespanAge) {

        Map<String, ChartResponseDTO.PositionDetail> normPlanetMap = normalizePlanetMap(planetMap);

        // 1. Determine Starting Sign (Lagna 1st house vs 7th house)
        int seventhSign = ((lagnaSign + 6 - 1) % 12) + 1;
        StartingSignEvaluation startEval = evaluateStartingSign(lagnaSign, seventhSign, normPlanetMap);
        int startingSign = startEval.chosenSign();
        String startingSignName = ZodiacUtils.getSignName(startingSign);
        String startingSignReason = startEval.reason();

        // 2. Progression Direction: Odd sign -> Direct (Savya), Even sign -> Reverse (Apasavya)
        boolean isDirect = (startingSign % 2 != 0);
        String progressionDirection = isDirect ? "Direct (Savya)" : "Reverse (Apasavya)";

        // 3. Trishoola Signs: 1st, 5th, and 9th from 8th house
        int eighthSign = ((lagnaSign + 8 - 2) % 12) + 1; // 8th house from Lagna
        int trishoola1 = eighthSign;
        int trishoola5 = ((eighthSign + 5 - 2) % 12) + 1;
        int trishoola9 = ((eighthSign + 9 - 2) % 12) + 1;
        List<Integer> trishoolaSignNumbers = List.of(trishoola1, trishoola5, trishoola9);
        List<String> trishoolaSignNames = trishoolaSignNumbers.stream()
                .map(ZodiacUtils::getSignName)
                .toList();

        // 4. Rudra Sign & Planet: Stronger of 2nd lord and 8th lord
        int secondSign = ((lagnaSign % 12) + 1);
        String secondLord = PlanetDignityUtils.getSignLord(secondSign);
        String eighthLord = PlanetDignityUtils.getSignLord(eighthSign);

        RudraEvaluation rudraEval = evaluateRudra(secondLord, eighthLord, eighthSign, normPlanetMap, lagnaSign);
        String rudraPlanetName = rudraEval.planetName();
        int rudraSignNumber = rudraEval.signNumber();
        String rudraSignName = ZodiacUtils.getSignName(rudraSignNumber);

        // 5. Build 12 x 9-Year Mahadasas and 9-Month Antardasas
        List<ShoolaPeriod> periods = new ArrayList<>(12);
        ShoolaPeriod criticalPeriod = null;

        for (int i = 0; i < 12; i++) {
            int periodIndex = i + 1;
            int signNum = isDirect
                    ? (((startingSign - 1 + i) % 12) + 1)
                    : (((startingSign - 1 - i + 120) % 12) + 1);
            String signName = ZodiacUtils.getSignName(signNum);
            int startAge = i * 9;
            int endAge = (i + 1) * 9;
            int startYear = birthYear + startAge;
            int endYear = birthYear + endAge;

            boolean isTrishoola = trishoolaSignNumbers.contains(signNum);
            boolean isRudra = (signNum == rudraSignNumber);

            String riskCategory;
            if (isTrishoola && isRudra) {
                riskCategory = "CRITICAL_TRISHOOLA_RUDRA";
            } else if (isTrishoola) {
                riskCategory = "HIGH_TRISHOOLA";
            } else if (isRudra) {
                riskCategory = "HIGH_RUDRA";
            } else {
                riskCategory = "MODERATE";
            }

            // Antardasas for this Mahadasa: 12 sub-periods of 9 months each
            boolean subDirect = (signNum % 2 != 0);
            List<ShoolaAntardasaPeriod> antardasas = new ArrayList<>(12);
            for (int j = 0; j < 12; j++) {
                int subIndex = j + 1;
                int subSign = subDirect
                        ? (((signNum - 1 + j) % 12) + 1)
                        : (((signNum - 1 - j + 120) % 12) + 1);
                String subSignName = ZodiacUtils.getSignName(subSign);

                int startTotalMonths = (startAge * 12) + (j * 9);
                int endTotalMonths = (startAge * 12) + ((j + 1) * 9);

                int sYear = birthYear + (startTotalMonths / 12);
                int sMonth = (startTotalMonths % 12) + 1;
                int eYear = birthYear + (endTotalMonths / 12);
                int eMonth = (endTotalMonths % 12) + 1;

                String startMonthYear = String.format("%02d/%04d", sMonth, sYear);
                String endMonthYear = String.format("%02d/%04d", eMonth, eYear);

                antardasas.add(new ShoolaAntardasaPeriod(subIndex, subSign, subSignName, startMonthYear, endMonthYear));
            }

            ShoolaPeriod period = new ShoolaPeriod(
                    periodIndex,
                    signNum,
                    signName,
                    startYear,
                    endYear,
                    startAge,
                    endAge,
                    isTrishoola,
                    isRudra,
                    riskCategory,
                    antardasas
            );
            periods.add(period);

            if (targetLifespanAge >= startAge && targetLifespanAge < endAge) {
                criticalPeriod = period;
            }
        }

        if (criticalPeriod == null && !periods.isEmpty()) {
            criticalPeriod = periods.get(periods.size() - 1);
        }

        String criticalShoolaWindow = criticalPeriod != null
                ? String.format("Ages %d-%d (%s Mahadasa, %d-%d) [Risk: %s%s%s]",
                criticalPeriod.startAge(),
                criticalPeriod.endAge(),
                criticalPeriod.signName(),
                criticalPeriod.startYear(),
                criticalPeriod.endYear(),
                criticalPeriod.riskCategory(),
                criticalPeriod.isTrishoola() ? " - Trishoola" : "",
                criticalPeriod.isRudra() ? " - Rudra" : "")
                : "None";

        String classicalRationale = String.format(
                "Maharishi Jaimini Shoola Dasa commences from %s (%s) with %s progression (12 Mahadasas of 9 years each, totalling 108 years). Trishoola signs (%s) and Rudra sign (%s ruled by %s) pinpoint vulnerable life-force transitions, aligning with the Ayurdaya ceiling at %s.",
                startingSignName,
                startingSignReason,
                progressionDirection,
                String.join(", ", trishoolaSignNames),
                rudraSignName,
                rudraPlanetName,
                criticalShoolaWindow
        );

        return new ShoolaDasaReport(
                startingSignName,
                startingSign,
                startingSignReason,
                progressionDirection,
                rudraSignName,
                rudraSignNumber,
                rudraPlanetName,
                trishoolaSignNames,
                trishoolaSignNumbers,
                periods,
                criticalShoolaWindow,
                classicalRationale
        );
    }

    public static ShoolaDasaReport calculateShoolaDasa(
            int lagnaSign,
            List<ChartResponseDTO.PositionDetail> d1Chart,
            int birthYear,
            int targetLifespanAge) {
        Map<String, ChartResponseDTO.PositionDetail> planetMap = new HashMap<>();
        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                if (p != null && p.getPlanetKey() != null) {
                    planetMap.put(p.getPlanetKey().toUpperCase(Locale.ROOT), p);
                }
            }
        }
        return calculateShoolaDasa(lagnaSign, planetMap, birthYear, targetLifespanAge);
    }

    // =========================================================================
    // INTERNAL EVALUATION HELPERS
    // =========================================================================

    private record StartingSignEvaluation(int chosenSign, String reason) {}

    private static StartingSignEvaluation evaluateStartingSign(
            int lagnaSign,
            int seventhSign,
            Map<String, ChartResponseDTO.PositionDetail> planetMap) {

        List<ChartResponseDTO.PositionDetail> lagnaPlanets = getPlanetsInSign(planetMap, lagnaSign);
        List<ChartResponseDTO.PositionDetail> seventhPlanets = getPlanetsInSign(planetMap, seventhSign);

        // 1. Number of conjoined planets
        int count1 = lagnaPlanets.size();
        int count7 = seventhPlanets.size();
        if (count1 > count7) {
            return new StartingSignEvaluation(
                    lagnaSign,
                    String.format("Lagna sign (%s) selected: more conjoined planets (%d vs %d)",
                            ZodiacUtils.getSignName(lagnaSign), count1, count7)
            );
        } else if (count7 > count1) {
            return new StartingSignEvaluation(
                    seventhSign,
                    String.format("7th House sign (%s) selected: more conjoined planets (%d vs %d)",
                            ZodiacUtils.getSignName(seventhSign), count7, count1)
            );
        }

        // 2. Exalted / Own sign planets
        int dignity1 = countExaltedOrOwnSign(lagnaPlanets, lagnaSign);
        int dignity7 = countExaltedOrOwnSign(seventhPlanets, seventhSign);
        if (dignity1 > dignity7) {
            return new StartingSignEvaluation(
                    lagnaSign,
                    String.format("Lagna sign (%s) selected: higher planetary dignity (%d exalted/own vs %d)",
                            ZodiacUtils.getSignName(lagnaSign), dignity1, dignity7)
            );
        } else if (dignity7 > dignity1) {
            return new StartingSignEvaluation(
                    seventhSign,
                    String.format("7th House sign (%s) selected: higher planetary dignity (%d exalted/own vs %d)",
                            ZodiacUtils.getSignName(seventhSign), dignity7, dignity1)
            );
        }

        // 3. Benefic presence / aspect (Jupiter, Venus, Mercury)
        int benefic1 = calculateBeneficInfluence(lagnaSign, planetMap);
        int benefic7 = calculateBeneficInfluence(seventhSign, planetMap);
        if (benefic1 > benefic7) {
            return new StartingSignEvaluation(
                    lagnaSign,
                    String.format("Lagna sign (%s) selected: stronger Jupiter/benefic influence",
                            ZodiacUtils.getSignName(lagnaSign))
            );
        } else if (benefic7 > benefic1) {
            return new StartingSignEvaluation(
                    seventhSign,
                    String.format("7th House sign (%s) selected: stronger Jupiter/benefic influence",
                            ZodiacUtils.getSignName(seventhSign))
            );
        }

        // 4. Default to Lagna on tie
        return new StartingSignEvaluation(
                lagnaSign,
                String.format("Lagna sign (%s) selected by default (equal strength with 7th house)",
                        ZodiacUtils.getSignName(lagnaSign))
        );
    }

    private static int countExaltedOrOwnSign(List<ChartResponseDTO.PositionDetail> planets, int sign) {
        int count = 0;
        for (var p : planets) {
            String pName = p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey();
            if (pName == null) continue;
            pName = capitalize(pName);
            if (PlanetDignityUtils.isExalted(pName, sign) || PlanetDignityUtils.isOwnSign(pName, sign)) {
                count++;
            }
        }
        return count;
    }

    private static int calculateBeneficInfluence(int sign, Map<String, ChartResponseDTO.PositionDetail> planetMap) {
        int score = 0;
        for (String b : List.of("JUPITER", "VENUS", "MERCURY")) {
            ChartResponseDTO.PositionDetail pos = planetMap.get(b);
            if (pos == null) continue;
            String planetName = capitalize(b);
            if (pos.getSignNumber() == sign) {
                score += "Jupiter".equalsIgnoreCase(planetName) ? 3 : 2;
            } else if (PlanetDignityUtils.isAspecting(planetName, pos.getSignNumber(), sign)) {
                score += "Jupiter".equalsIgnoreCase(planetName) ? 2 : 1;
            }
        }
        return score;
    }

    private record RudraEvaluation(String planetName, int signNumber) {}

    private static RudraEvaluation evaluateRudra(
            String lord2,
            String lord8,
            int eighthSign,
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            int lagnaSign) {

        ChartResponseDTO.PositionDetail pos2 = planetMap.get(lord2.toUpperCase(Locale.ROOT));
        ChartResponseDTO.PositionDetail pos8 = planetMap.get(lord8.toUpperCase(Locale.ROOT));

        double score2 = evaluatePlanetStrength(lord2, pos2, lagnaSign, planetMap);
        double score8 = evaluatePlanetStrength(lord8, pos8, lagnaSign, planetMap);

        // Stronger planet becomes Rudra; if tied, 8th lord wins as primary Ayur/Mrityu lord
        String rudraPlanet = (score2 > score8) ? lord2 : lord8;
        ChartResponseDTO.PositionDetail rudraPos = (score2 > score8) ? pos2 : pos8;

        int rudraSign = (rudraPos != null) ? rudraPos.getSignNumber() : eighthSign;
        return new RudraEvaluation(rudraPlanet, rudraSign);
    }

    private static double evaluatePlanetStrength(
            String planetName,
            ChartResponseDTO.PositionDetail pos,
            int lagnaSign,
            Map<String, ChartResponseDTO.PositionDetail> planetMap) {

        if (pos == null) return 1.0;
        double score = 5.0;
        int sign = pos.getSignNumber();
        String pName = capitalize(planetName);

        if (PlanetDignityUtils.isExalted(pName, sign)) score += 3.0;
        else if (PlanetDignityUtils.isOwnSign(pName, sign)) score += 2.0;
        else if (PlanetDignityUtils.isDebilitated(pName, sign)) score -= 2.0;

        int house = ((sign - lagnaSign + 12) % 12) + 1;
        if (house == 1 || house == 4 || house == 7 || house == 10) score += 1.5;
        else if (house == 5 || house == 9) score += 1.0;
        else if (house == 6 || house == 8 || house == 12) score -= 1.0;

        // Conjunctions with other planets
        List<ChartResponseDTO.PositionDetail> conjoined = getPlanetsInSign(planetMap, sign);
        score += Math.max(0, conjoined.size() - 1) * 0.5;

        // Degree in sign tie-breaker
        score += (pos.getDegreeInSign() / 100.0);

        return score;
    }

    private static List<ChartResponseDTO.PositionDetail> getPlanetsInSign(
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            int signNumber) {
        List<ChartResponseDTO.PositionDetail> result = new ArrayList<>();
        if (planetMap == null) return result;
        Set<String> seen = new HashSet<>();
        for (var entry : planetMap.values()) {
            if (entry == null || entry.getPlanetKey() == null) continue;
            String key = entry.getPlanetKey().toUpperCase(Locale.ROOT);
            if ("LAGNA".equals(key) || seen.contains(key)) continue;
            if (entry.getSignNumber() == signNumber) {
                seen.add(key);
                result.add(entry);
            }
        }
        return result;
    }

    private static Map<String, ChartResponseDTO.PositionDetail> normalizePlanetMap(
            Map<String, ChartResponseDTO.PositionDetail> rawMap) {
        Map<String, ChartResponseDTO.PositionDetail> map = new HashMap<>();
        if (rawMap != null) {
            for (var entry : rawMap.entrySet()) {
                if (entry.getValue() != null) {
                    if (entry.getKey() != null) {
                        map.put(entry.getKey().toUpperCase(Locale.ROOT), entry.getValue());
                    }
                    if (entry.getValue().getPlanetKey() != null) {
                        map.put(entry.getValue().getPlanetKey().toUpperCase(Locale.ROOT), entry.getValue());
                    }
                }
            }
        }
        return map;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1).toLowerCase(Locale.ROOT);
    }
}
