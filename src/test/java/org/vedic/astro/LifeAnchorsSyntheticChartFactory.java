package org.vedic.astro;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.util.ZodiacUtils;

import java.util.*;

public class LifeAnchorsSyntheticChartFactory {

    public record TestCase(
            String id,
            String description,
            int lagnaSign,
            double lagnaDegree,
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            Map<String, Double> shadbalaRupas,
            String expectedSynthesisRule,
            boolean isVisheshaExpected
    ) {}

    public static List<TestCase> generate90SyntheticCases() {
        List<TestCase> list = new ArrayList<>();

        // 1. 36 Modality Permutations (12 Lagnas x 3 Modality Configurations)
        for (int lagna = 1; lagna <= 12; lagna++) {
            // Chara-Chara configuration
            list.add(createTestCase("MOD_CC_L" + lagna, "Chara-Chara modality for Lagna " + lagna, lagna, 15.0, 1, 1, 1, 1, 1, 1, "Tri-Samvada (Unanimous Consensus)", false));
            // Chara-Sthira configuration
            list.add(createTestCase("MOD_CS_L" + lagna, "Chara-Sthira modality for Lagna " + lagna, lagna, 15.0, 1, 2, 1, 2, 1, 2, "Tri-Samvada (Unanimous Consensus)", false));
            // Chara-Dwisvabhava configuration
            list.add(createTestCase("MOD_CD_L" + lagna, "Chara-Dwisvabhava modality for Lagna " + lagna, lagna, 15.0, 1, 3, 1, 3, 1, 3, "Tri-Samvada (Unanimous Consensus)", false));
        }

        // 2. 24 Vishesha Sutra Override Cases
        for (int i = 1; i <= 6; i++) {
            int oddLagna = (i * 2) - 1; // 1, 3, 5, 7, 9, 11
            int evenLagna = i * 2;      // 2, 4, 6, 8, 10, 12

            // Vishesha Sutra 1: Moon in Lagna (1st house)
            list.add(createVisheshaCase("VISH_MOON_L1_" + oddLagna, "Chandra in 1st house for Lagna " + oddLagna, oddLagna, oddLagna, 5, "Vishesha Sutra 1 (Chandra-Kendra)"));
            // Vishesha Sutra 1: Moon in 7th house
            int seventhHouse = ((oddLagna + 6 - 1) % 12) + 1;
            list.add(createVisheshaCase("VISH_MOON_L7_" + oddLagna, "Chandra in 7th house for Lagna " + oddLagna, oddLagna, seventhHouse, 5, "Vishesha Sutra 1 (Chandra-Kendra)"));

            // Asamvada: Odd Lagna -> Pair 3 (Lagna-Hora)
            list.add(createAsamvadaCase("ASAMVADA_ODD_" + oddLagna, "Asamvada Odd Lagna " + oddLagna, oddLagna, 1, 2, 3, "Asamvada (Odd Lagna Tie-Breaker)"));
            // Asamvada: Even Lagna -> Pair 1 (Lagna Lord-8th Lord)
            list.add(createAsamvadaCase("ASAMVADA_EVEN_" + evenLagna, "Asamvada Even Lagna " + evenLagna, evenLagna, 1, 2, 3, "Asamvada (Even Lagna Tie-Breaker)"));
        }

        // 3. 10 Dual Lord Resolution Cases (Vrishchika Mars/Ketu, Kumbha Saturn/Rahu)
        for (int i = 1; i <= 5; i++) {
            list.add(createDualLordCase("DUAL_VRISHCHIKA_" + i, "Vrishchika dual lord test " + i, 8, i % 2 == 0));
            list.add(createDualLordCase("DUAL_KUMBHA_" + i, "Kumbha dual lord test " + i, 11, i % 2 == 0));
        }

        // 4. 20 Kakshya Vriddhi & Hrasa Edge Cases
        for (int i = 1; i <= 20; i++) {
            list.add(createKakshyaCase("KAKSHYA_EDGE_" + i, "Kakshya Vriddhi/Hrasa variation " + i, ((i - 1) % 12) + 1, i));
        }

        return Collections.unmodifiableList(list);
    }

    private static ChartResponseDTO.PositionDetail pos(String key, int sign, double deg, boolean isRetrograde) {
        return ChartResponseDTO.PositionDetail.builder()
                .planetKey(key.toUpperCase())
                .displayName(key)
                .signNumber(sign)
                .rashiName(ZodiacUtils.getSignName(sign))
                .degreeInSign(deg)
                .build();
    }

    private static TestCase createTestCase(String id, String desc, int lagna, double deg, int p1a, int p1b, int p2a, int p2b, int p3a, int p3b, String rule, boolean vishesha) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", pos("Lagna", lagna, deg, false));
        pmap.put("Sun", pos("Sun", p1a, 10.0, false));
        pmap.put("Moon", pos("Moon", p2a, 12.0, false));
        pmap.put("Mars", pos("Mars", p1b, 14.0, false));
        pmap.put("Mercury", pos("Mercury", p3a, 16.0, false));
        pmap.put("Jupiter", pos("Jupiter", 4, 18.0, false));
        pmap.put("Venus", pos("Venus", p3b, 20.0, false));
        pmap.put("Saturn", pos("Saturn", p2b, 22.0, false));
        pmap.put("Rahu", pos("Rahu", 5, 24.0, true));
        pmap.put("Ketu", pos("Ketu", 11, 24.0, true));

        sbala.put("Sun", 6.5);
        sbala.put("Moon", 6.2);
        sbala.put("Mars", 6.0);
        sbala.put("Mercury", 6.8);
        sbala.put("Jupiter", 7.5);
        sbala.put("Venus", 6.4);
        sbala.put("Saturn", 6.1);

        return new TestCase(id, desc, lagna, deg, pmap, sbala, rule, vishesha);
    }

    private static TestCase createVisheshaCase(String id, String desc, int lagna, int moonSign, int saturnSign, String rule) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", pos("Lagna", lagna, 15.0, false));
        pmap.put("Moon", pos("Moon", moonSign, 12.0, false));
        pmap.put("Saturn", pos("Saturn", saturnSign, 22.0, false));
        pmap.put("Jupiter", pos("Jupiter", 4, 18.0, false));
        pmap.put("Sun", pos("Sun", 2, 10.0, false));
        pmap.put("Mars", pos("Mars", 3, 14.0, false));
        pmap.put("Mercury", pos("Mercury", 6, 16.0, false));
        pmap.put("Venus", pos("Venus", 9, 20.0, false));
        pmap.put("Rahu", pos("Rahu", 5, 24.0, true));
        pmap.put("Ketu", pos("Ketu", 11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.5);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, rule, true);
    }

    private static TestCase createAsamvadaCase(String id, String desc, int lagna, int p1, int p2, int p3, String rule) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", pos("Lagna", lagna, 15.0, false));
        // Moon in 2nd house (non-kendra, avoids Chandra-Kendra override)
        int moonSign = ((lagna + 2 - 1 - 1) % 12) + 1;
        pmap.put("Moon", pos("Moon", moonSign, 12.0, false));
        pmap.put("Saturn", pos("Saturn", 2, 22.0, false));
        pmap.put("Sun", pos("Sun", 1, 10.0, false));
        pmap.put("Mars", pos("Mars", 3, 14.0, false));
        pmap.put("Mercury", pos("Mercury", 6, 16.0, false));
        pmap.put("Jupiter", pos("Jupiter", 8, 18.0, false));
        pmap.put("Venus", pos("Venus", 9, 20.0, false));
        pmap.put("Rahu", pos("Rahu", 5, 24.0, true));
        pmap.put("Ketu", pos("Ketu", 11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.0);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, rule, true);
    }

    private static TestCase createDualLordCase(String id, String desc, int lagna, boolean alternateStronger) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", pos("Lagna", lagna, 15.0, false));
        pmap.put("Sun", pos("Sun", 1, 10.0, false));
        pmap.put("Moon", pos("Moon", 2, 12.0, false));
        pmap.put("Mars", pos("Mars", alternateStronger ? 8 : 1, 14.0, false));
        pmap.put("Mercury", pos("Mercury", 3, 16.0, false));
        pmap.put("Jupiter", pos("Jupiter", 4, 18.0, false));
        pmap.put("Venus", pos("Venus", 5, 20.0, false));
        pmap.put("Saturn", pos("Saturn", alternateStronger ? 11 : 7, 22.0, false));
        pmap.put("Rahu", pos("Rahu", alternateStronger ? 11 : 5, 24.0, true));
        pmap.put("Ketu", pos("Ketu", alternateStronger ? 8 : 11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.5);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, "Dwi-Samvada (Majority Consensus)", false);
    }

    private static TestCase createKakshyaCase(String id, String desc, int lagna, int variation) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", pos("Lagna", lagna, 15.0, false));
        pmap.put("Sun", pos("Sun", 1, 10.0, false));
        pmap.put("Moon", pos("Moon", 2, 12.0, false));
        pmap.put("Mars", pos("Mars", 3, 14.0, false));
        pmap.put("Mercury", pos("Mercury", 6, 16.0, false));
        pmap.put("Jupiter", pos("Jupiter", variation % 2 == 0 ? lagna : 6, 18.0, false));
        pmap.put("Venus", pos("Venus", 9, 20.0, false));
        pmap.put("Saturn", pos("Saturn", variation % 3 == 0 ? 7 : (variation % 5 == 0 ? 1 : 11), 22.0, false));
        pmap.put("Rahu", pos("Rahu", 5, 24.0, true));
        pmap.put("Ketu", pos("Ketu", 11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, (variation % 4 == 0) ? 4.5 : 7.0);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, "Dwi-Samvada (Majority Consensus)", false);
    }
}
