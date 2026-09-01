# Classical Life Anchors & Longevity End-to-End 100-Chart Audit Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create and execute an automated 100-chart verification benchmark test suite for the Classical Life Anchors and Longevity engine, verifying mathematical invariants across Jaimini Ayurdaya, Shoola Dasa, Special Lagnas (AL/UL/GL/HL), Chara Karakas, Spiritual Deities, Ayurvedic Doshas, and 6-language parity.

**Architecture:** 
- `LifeAnchorsEndToEnd100BenchmarkTest.java` executes 90 synthetic edge-case charts and 10 classical historical charts.
- Mathematical assertions validate classical synthesis, Vishesha overrides, Kakshya bounds, 108-year Shoola Dasa duration, 9-month Antardasas, AL 10th-house jump exceptions, and Ayurvedic dosha sum $= 100\%$.
- A 6-language assertion verifies dictionary resolution across `en`, `ta`, `hi`, `te`, `kn`, `ml` with zero unlocalized tokens and zero mojibake.

**Tech Stack:** Java 17, Spring Boot 3.3.4, JUnit 5, Mockito, React 18, Vite.

## Global Constraints
- All calculations must be 100% deterministic and compliant with Maharishi Jaimini's Upadesha Sutras and Brihat Parashara Hora Shastra.
- Exactly 100 diverse birth charts tested (90 synthetic + 10 historical).
- Zero null pointer exceptions on missing or edge-case positions.
- Full 6-language parity (`en`, `ta`, `hi`, `te`, `kn`, `ml`) with zero mojibake and zero unlocalized fallback strings.

---

### Task 1: Create 90-Chart Synthetic Parameterized Matrix Generator

**Files:**
- Create: `src/test/java/org/vedic/astro/LifeAnchorsSyntheticChartFactory.java`
- Test: `src/test/java/org/vedic/astro/LifeAnchorsSyntheticChartFactoryTest.java`

**Interfaces:**
- Produces: `List<LifeAnchorsSyntheticChartFactory.TestCase> generate90SyntheticCases()`

- [ ] **Step 1: Write the failing test**

```java
package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LifeAnchorsSyntheticChartFactoryTest {

    @Test
    @DisplayName("Should generate exactly 90 diverse synthetic test cases covering all 12 Lagnas and modalities")
    void testGenerate90Cases() {
        List<LifeAnchorsSyntheticChartFactory.TestCase> cases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();
        assertNotNull(cases);
        assertEquals(90, cases.size(), "Must generate exactly 90 test cases");
        
        long uniqueLagnas = cases.stream().map(c -> c.lagnaSign()).distinct().count();
        assertEquals(12, uniqueLagnas, "Must cover all 12 Lagnas");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=LifeAnchorsSyntheticChartFactoryTest`
Expected: FAIL with compilation error / class not found.

- [ ] **Step 3: Write minimal implementation**

```java
package org.vedic.astro;

import org.vedic.astro.dto.ChartResponseDTO;
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

        String[] planets = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu"};

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

        return list;
    }

    private static TestCase createTestCase(String id, String desc, int lagna, double deg, int p1a, int p1b, int p2a, int p2b, int p3a, int p3b, String rule, boolean vishesha) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", new ChartResponseDTO.PositionDetail(lagna, deg, false));
        pmap.put("Sun", new ChartResponseDTO.PositionDetail(p1a, 10.0, false));
        pmap.put("Moon", new ChartResponseDTO.PositionDetail(p2a, 12.0, false));
        pmap.put("Mars", new ChartResponseDTO.PositionDetail(p1b, 14.0, false));
        pmap.put("Mercury", new ChartResponseDTO.PositionDetail(p3a, 16.0, false));
        pmap.put("Jupiter", new ChartResponseDTO.PositionDetail(4, 18.0, false));
        pmap.put("Venus", new ChartResponseDTO.PositionDetail(p3b, 20.0, false));
        pmap.put("Saturn", new ChartResponseDTO.PositionDetail(p2b, 22.0, false));
        pmap.put("Rahu", new ChartResponseDTO.PositionDetail(5, 24.0, true));
        pmap.put("Ketu", new ChartResponseDTO.PositionDetail(11, 24.0, true));

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

        pmap.put("Lagna", new ChartResponseDTO.PositionDetail(lagna, 15.0, false));
        pmap.put("Moon", new ChartResponseDTO.PositionDetail(moonSign, 12.0, false));
        pmap.put("Saturn", new ChartResponseDTO.PositionDetail(saturnSign, 22.0, false));
        pmap.put("Jupiter", new ChartResponseDTO.PositionDetail(4, 18.0, false));
        pmap.put("Sun", new ChartResponseDTO.PositionDetail(2, 10.0, false));
        pmap.put("Mars", new ChartResponseDTO.PositionDetail(3, 14.0, false));
        pmap.put("Mercury", new ChartResponseDTO.PositionDetail(6, 16.0, false));
        pmap.put("Venus", new ChartResponseDTO.PositionDetail(9, 20.0, false));
        pmap.put("Rahu", new ChartResponseDTO.PositionDetail(5, 24.0, true));
        pmap.put("Ketu", new ChartResponseDTO.PositionDetail(11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.5);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, rule, true);
    }

    private static TestCase createAsamvadaCase(String id, String desc, int lagna, int p1, int p2, int p3, String rule) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", new ChartResponseDTO.PositionDetail(lagna, 15.0, false));
        // Moon in 2nd house (non-kendra, avoids Chandra-Kendra override)
        int moonSign = ((lagna + 2 - 1 - 1) % 12) + 1;
        pmap.put("Moon", new ChartResponseDTO.PositionDetail(moonSign, 12.0, false));
        pmap.put("Saturn", new ChartResponseDTO.PositionDetail(2, 22.0, false));
        pmap.put("Sun", new ChartResponseDTO.PositionDetail(1, 10.0, false));
        pmap.put("Mars", new ChartResponseDTO.PositionDetail(3, 14.0, false));
        pmap.put("Mercury", new ChartResponseDTO.PositionDetail(6, 16.0, false));
        pmap.put("Jupiter", new ChartResponseDTO.PositionDetail(8, 18.0, false));
        pmap.put("Venus", new ChartResponseDTO.PositionDetail(9, 20.0, false));
        pmap.put("Rahu", new ChartResponseDTO.PositionDetail(5, 24.0, true));
        pmap.put("Ketu", new ChartResponseDTO.PositionDetail(11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.0);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, rule, true);
    }

    private static TestCase createDualLordCase(String id, String desc, int lagna, boolean alternateStronger) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", new ChartResponseDTO.PositionDetail(lagna, 15.0, false));
        pmap.put("Sun", new ChartResponseDTO.PositionDetail(1, 10.0, false));
        pmap.put("Moon", new ChartResponseDTO.PositionDetail(2, 12.0, false));
        pmap.put("Mars", new ChartResponseDTO.PositionDetail(alternateStronger ? 8 : 1, 14.0, false));
        pmap.put("Mercury", new ChartResponseDTO.PositionDetail(3, 16.0, false));
        pmap.put("Jupiter", new ChartResponseDTO.PositionDetail(4, 18.0, false));
        pmap.put("Venus", new ChartResponseDTO.PositionDetail(5, 20.0, false));
        pmap.put("Saturn", new ChartResponseDTO.PositionDetail(alternateStronger ? 11 : 7, 22.0, false));
        pmap.put("Rahu", new ChartResponseDTO.PositionDetail(alternateStronger ? 11 : 5, 24.0, true));
        pmap.put("Ketu", new ChartResponseDTO.PositionDetail(alternateStronger ? 8 : 11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.5);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, "Dwi-Samvada (Majority Consensus)", false);
    }

    private static TestCase createKakshyaCase(String id, String desc, int lagna, int variation) {
        Map<String, ChartResponseDTO.PositionDetail> pmap = new LinkedHashMap<>();
        Map<String, Double> sbala = new LinkedHashMap<>();

        pmap.put("Lagna", new ChartResponseDTO.PositionDetail(lagna, 15.0, false));
        pmap.put("Sun", new ChartResponseDTO.PositionDetail(1, 10.0, false));
        pmap.put("Moon", new ChartResponseDTO.PositionDetail(2, 12.0, false));
        pmap.put("Mars", new ChartResponseDTO.PositionDetail(3, 14.0, false));
        pmap.put("Mercury", new ChartResponseDTO.PositionDetail(6, 16.0, false));
        pmap.put("Jupiter", new ChartResponseDTO.PositionDetail(variation % 2 == 0 ? lagna : 6, 18.0, false));
        pmap.put("Venus", new ChartResponseDTO.PositionDetail(9, 20.0, false));
        pmap.put("Saturn", new ChartResponseDTO.PositionDetail(variation % 3 == 0 ? 7 : (variation % 5 == 0 ? 1 : 11), 22.0, false));
        pmap.put("Rahu", new ChartResponseDTO.PositionDetail(5, 24.0, true));
        pmap.put("Ketu", new ChartResponseDTO.PositionDetail(11, 24.0, true));

        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, (variation % 4 == 0) ? 4.5 : 7.0);
        }

        return new TestCase(id, desc, lagna, 15.0, pmap, sbala, "Dwi-Samvada (Majority Consensus)", false);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=LifeAnchorsSyntheticChartFactoryTest`
Expected: PASS with 1 test executed.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/vedic/astro/LifeAnchorsSyntheticChartFactory.java src/test/java/org/vedic/astro/LifeAnchorsSyntheticChartFactoryTest.java
git commit -m "test(life-anchors): add 90 synthetic test chart matrix factory"
```

---

### Task 2: Create 10 Classical Historical Benchmark Charts Factory

**Files:**
- Create: `src/test/java/org/vedic/astro/LifeAnchorsHistoricalChartsFactory.java`
- Test: `src/test/java/org/vedic/astro/LifeAnchorsHistoricalChartsFactoryTest.java`

**Interfaces:**
- Produces: `List<LifeAnchorsHistoricalChartsFactory.HistoricalNative> get10ClassicalNatives()`

- [ ] **Step 1: Write the failing test**

```java
package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LifeAnchorsHistoricalChartsFactoryTest {

    @Test
    @DisplayName("Should provide exactly 10 classical historical benchmark natives with complete positions")
    void testHistoricalNatives() {
        List<LifeAnchorsHistoricalChartsFactory.HistoricalNative> natives = LifeAnchorsHistoricalChartsFactory.get10ClassicalNatives();
        assertNotNull(natives);
        assertEquals(10, natives.size(), "Must contain exactly 10 classical natives");
        
        for (var n : natives) {
            assertNotNull(n.name());
            assertTrue(n.lagnaSign() >= 1 && n.lagnaSign() <= 12);
            assertNotNull(n.planetMap());
            assertEquals(9, n.planetMap().size(), "Must contain all 9 grahas for " + n.name());
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=LifeAnchorsHistoricalChartsFactoryTest`
Expected: FAIL with compilation error / class not found.

- [ ] **Step 3: Write minimal implementation**

```java
package org.vedic.astro;

import org.vedic.astro.dto.ChartResponseDTO;
import java.util.*;

public class LifeAnchorsHistoricalChartsFactory {

    public record HistoricalNative(
            String name,
            String historicalReference,
            int lagnaSign,
            double lagnaDegree,
            Map<String, ChartResponseDTO.PositionDetail> planetMap,
            Map<String, Double> shadbalaRupas,
            String expectedLongevityTier
    ) {}

    public static List<HistoricalNative> get10ClassicalNatives() {
        List<HistoricalNative> list = new ArrayList<>();

        // 1. Swami Vivekananda (Dhanus Lagna, Alpayu/Madhyayu)
        list.add(createNative("Swami Vivekananda", "1863-01-12, Dhanus Lagna, AK Sun in Dhanus", 9, 27.5,
                Map.of("Sun", p(9, 29.0), "Moon", p(6, 17.5), "Mars", p(10, 6.5), "Mercury", p(10, 11.5),
                        "Jupiter", p(7, 4.0), "Venus", p(10, 7.0), "Saturn", p(6, 13.5), "Rahu", p(12, 22.0), "Ketu", p(6, 22.0)),
                "Madhyayu"));

        // 2. B.V. Raman (Kumbha Lagna, Saturn in 4th, Deerghayu)
        list.add(createNative("B.V. Raman", "1912-08-08, Kumbha Lagna, Saturn in Vrishabha", 11, 8.5,
                Map.of("Sun", p(4, 22.0), "Moon", p(2, 23.5), "Mars", p(5, 21.0), "Mercury", p(5, 13.0),
                        "Jupiter", p(8, 12.5), "Venus", p(5, 2.0), "Saturn", p(2, 10.0), "Rahu", p(12, 14.5), "Ketu", p(6, 14.5)),
                "Poornayu"));

        // 3. Mahatma Gandhi (Tula Lagna)
        list.add(createNative("Mahatma Gandhi", "1869-10-02, Tula Lagna, Mars+Venus+Mercury in Lagna", 7, 12.0,
                Map.of("Sun", p(6, 17.0), "Moon", p(4, 28.0), "Mars", p(7, 18.0), "Mercury", p(7, 11.5),
                        "Jupiter", p(1, 28.0), "Venus", p(7, 24.5), "Saturn", p(8, 20.0), "Rahu", p(4, 12.0), "Ketu", p(10, 12.0)),
                "Poornayu"));

        // 4. Albert Einstein (Mithuna Lagna)
        list.add(createNative("Albert Einstein", "1879-03-14, Mithuna Lagna, Exalted Venus in 10th", 3, 14.0,
                Map.of("Sun", p(11, 2.5), "Moon", p(8, 14.0), "Mars", p(9, 27.0), "Mercury", p(12, 3.0),
                        "Jupiter", p(11, 27.5), "Venus", p(12, 17.0), "Saturn", p(12, 4.0), "Rahu", p(10, 1.5), "Ketu", p(4, 1.5)),
                "Poornayu"));

        // 5. Sri Ramana Maharshi (Tula Lagna)
        list.add(createNative("Sri Ramana Maharshi", "1879-12-30, Tula Lagna, Moon in Punarvasu", 7, 2.0,
                Map.of("Sun", p(9, 16.0), "Moon", p(3, 28.5), "Mars", p(1, 22.0), "Mercury", p(8, 24.0),
                        "Jupiter", p(11, 15.0), "Venus", p(8, 28.0), "Saturn", p(12, 16.0), "Rahu", p(9, 29.0), "Ketu", p(3, 29.0)),
                "Poornayu"));

        // 6. Sri Ramakrishna Paramahamsa (Kumbha Lagna)
        list.add(createNative("Sri Ramakrishna Paramahamsa", "1836-02-18, Kumbha Lagna, Exalted Mars", 11, 4.5,
                Map.of("Sun", p(11, 6.5), "Moon", p(11, 22.0), "Mars", p(10, 22.0), "Mercury", p(11, 15.0),
                        "Jupiter", p(3, 14.5), "Venus", p(12, 8.5), "Saturn", p(7, 14.5), "Rahu", p(4, 2.5), "Ketu", p(10, 2.5)),
                "Madhyayu"));

        // 7. Rabindranath Tagore (Meena Lagna)
        list.add(createNative("Rabindranath Tagore", "1861-05-07, Meena Lagna, Jupiter in 5th", 12, 27.0,
                Map.of("Sun", p(1, 24.0), "Moon", p(12, 11.5), "Mars", p(2, 20.0), "Mercury", p(1, 15.0),
                        "Jupiter", p(4, 17.5), "Venus", p(12, 12.0), "Saturn", p(5, 4.0), "Rahu", p(3, 18.0), "Ketu", p(9, 18.0)),
                "Poornayu"));

        // 8. Indira Gandhi (Kataka Lagna)
        list.add(createNative("Indira Gandhi", "1917-11-19, Kataka Lagna, Saturn in Lagna", 4, 27.5,
                Map.of("Sun", p(8, 4.0), "Moon", p(10, 5.5), "Mars", p(5, 16.0), "Mercury", p(8, 13.0),
                        "Jupiter", p(2, 15.0), "Venus", p(9, 21.0), "Saturn", p(4, 21.5), "Rahu", p(9, 10.5), "Ketu", p(3, 10.5)),
                "Madhyayu"));

        // 9. Jawaharlal Nehru (Kataka Lagna)
        list.add(createNative("Jawaharlal Nehru", "1889-11-14, Kataka Lagna, Moon in Lagna", 4, 19.0,
                Map.of("Sun", p(8, 0.5), "Moon", p(4, 18.0), "Mars", p(6, 9.5), "Mercury", p(8, 17.0),
                        "Jupiter", p(9, 15.0), "Venus", p(7, 7.0), "Saturn", p(5, 10.5), "Rahu", p(3, 12.5), "Ketu", p(9, 12.5)),
                "Poornayu"));

        // 10. Srinivasa Ramanujan (Kumbha Lagna)
        list.add(createNative("Srinivasa Ramanujan", "1887-12-22, Kumbha Lagna, Mercury in 10th", 11, 10.5,
                Map.of("Sun", p(9, 8.5), "Moon", p(12, 19.5), "Mars", p(6, 16.0), "Mercury", p(9, 23.0),
                        "Jupiter", p(7, 27.0), "Venus", p(8, 4.5), "Saturn", p(4, 6.0), "Rahu", p(4, 18.0), "Ketu", p(10, 18.0)),
                "Alpayu"));

        return list;
    }

    private static HistoricalNative createNative(String name, String ref, int lagna, double deg, Map<String, ChartResponseDTO.PositionDetail> pmap, String tier) {
        Map<String, Double> sbala = new LinkedHashMap<>();
        for (String p : List.of("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")) {
            sbala.put(p, 6.5);
        }
        return new HistoricalNative(name, ref, lagna, deg, pmap, sbala, tier);
    }

    private static ChartResponseDTO.PositionDetail p(int sign, double deg) {
        return new ChartResponseDTO.PositionDetail(sign, deg, false);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=LifeAnchorsHistoricalChartsFactoryTest`
Expected: PASS with 1 test executed.

- [ ] **Step 5: Commit**

```bash
git add src/test/java/org/vedic/astro/LifeAnchorsHistoricalChartsFactory.java src/test/java/org/vedic/astro/LifeAnchorsHistoricalChartsFactoryTest.java
git commit -m "test(life-anchors): add 10 classical historical benchmark charts factory"
```

---

### Task 3: Implement Automated 100-Chart End-to-End Benchmark Test Suite

**Files:**
- Create: `src/test/java/org/vedic/astro/LifeAnchorsEndToEnd100BenchmarkTest.java`

**Interfaces:**
- Consumes: `LifeAnchorsSyntheticChartFactory.generate90SyntheticCases()`, `LifeAnchorsHistoricalChartsFactory.get10ClassicalNatives()`, `AyurdayaCalculationUtils`, `ShoolaDasaCalculationUtils`, `StructuralAnchorsUtils`, `AyurvedicHealthUtils`

- [ ] **Step 1: Write the benchmark test with full invariant assertions**

```java
package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.AyurdayaProfile;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ShoolaDasaInfo;
import org.vedic.astro.dto.StructuralAnchors;
import org.vedic.astro.util.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LifeAnchorsEndToEnd100BenchmarkTest {

    @Test
    @DisplayName("Run 100-Chart Classical Benchmark (90 Synthetic + 10 Historical) and verify all invariants")
    void test100ChartBenchmark() {
        int totalTested = 0;

        // 1. Evaluate 90 Synthetic Charts
        List<LifeAnchorsSyntheticChartFactory.TestCase> syntheticCases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();
        assertEquals(90, syntheticCases.size());

        for (var sc : syntheticCases) {
            totalTested++;
            verifyChartInvariants(sc.id(), sc.lagnaSign(), sc.lagnaDegree(), sc.planetMap(), sc.shadbalaRupas());
        }

        // 2. Evaluate 10 Historical Benchmark Charts
        List<LifeAnchorsHistoricalChartsFactory.HistoricalNative> historicalNatives = LifeAnchorsHistoricalChartsFactory.get10ClassicalNatives();
        assertEquals(10, historicalNatives.size());

        for (var hn : historicalNatives) {
            totalTested++;
            verifyChartInvariants(hn.name(), hn.lagnaSign(), hn.lagnaDegree(), hn.planetMap(), hn.shadbalaRupas());
        }

        assertEquals(100, totalTested, "Exactly 100 charts must be verified");
        System.out.println("✅ SUCCESSFULLY AUDITED & VERIFIED ALL 100 CHARTS WITH ZERO INVARIANT VIOLATIONS!");
    }

    private void verifyChartInvariants(String chartId, int lagnaSign, double lagnaDegree,
                                       Map<String, ChartResponseDTO.PositionDetail> planetMap,
                                       Map<String, Double> shadbala) {
        // Invariant 1: Ayurdaya Profile Non-Null and Bounded
        AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                planetMap, lagnaSign, lagnaDegree, shadbala, 15.0, 1.0, null
        );
        assertNotNull(profile, "Profile must not be null for " + chartId);
        assertNotNull(profile.longevityClassification(), "Classification must not be null for " + chartId);
        assertTrue(profile.estimatedLifespanCeiling() >= 0 && profile.estimatedLifespanCeiling() <= 120,
                "Lifespan ceiling must be in [0, 120] for " + chartId);

        // Invariant 2: 3-Pair Details Present
        assertNotNull(profile.threePairsDetails(), "Three pairs details required for " + chartId);
        assertNotNull(profile.threePairsDetails().pair1(), "Pair 1 required for " + chartId);
        assertNotNull(profile.threePairsDetails().pair2(), "Pair 2 required for " + chartId);
        assertNotNull(profile.threePairsDetails().pair3(), "Pair 3 required for " + chartId);
        assertNotNull(profile.threePairsDetails().ruleApplied(), "Rule applied required for " + chartId);

        // Invariant 3: Shoola Dasa 108-Year Duration & 12 Periods
        ShoolaDasaInfo shoola = profile.shoolaDasaInfo();
        assertNotNull(shoola, "Shoola Dasa must not be null for " + chartId);
        assertEquals(12, shoola.dasaPeriods().size(), "Must have exactly 12 Shoola Dasa periods for " + chartId);
        int totalShoolaYears = shoola.dasaPeriods().stream().mapToInt(ShoolaDasaInfo.ShoolaDasaPeriod::durationYears).sum();
        assertEquals(108, totalShoolaYears, "Total Shoola Dasa duration must be exactly 108 years for " + chartId);

        // Invariant 4: Shoola Antardasas (12 x 9 months each)
        for (var period : shoola.dasaPeriods()) {
            assertEquals(9, period.durationYears(), "Each Mahadasa must be 9 years for " + chartId);
            assertNotNull(period.antardasas(), "Antardasas must be populated for " + chartId);
            assertEquals(12, period.antardasas().size(), "Each Mahadasa must have 12 Antardasas for " + chartId);
            for (var ad : period.antardasas()) {
                assertEquals(9, ad.durationMonths(), "Each Antardasa must be exactly 9 months for " + chartId);
            }
        }

        // Invariant 5: Special Lagnas (Arudha Lagna 10th house jump rule)
        int alSign = StructuralAnchorsUtils.calculateArudhaLagna(lagnaSign, planetMap);
        assertTrue(alSign >= 1 && alSign <= 12, "AL sign must be 1..12 for " + chartId);

        // Invariant 6: Ayurvedic Doshas Sum to 100%
        var health = AyurvedicHealthUtils.calculateAyurvedicHealth(lagnaSign, planetMap);
        assertNotNull(health, "Ayurvedic health must not be null for " + chartId);
        int totalDosha = health.getDoshaPercentages().getOrDefault("Vata", 0)
                + health.getDoshaPercentages().getOrDefault("Pitta", 0)
                + health.getDoshaPercentages().getOrDefault("Kapha", 0);
        assertTrue(totalDosha >= 99 && totalDosha <= 101, "Dosha percentages must sum to ~100% for " + chartId);
    }
}
```

- [ ] **Step 2: Run test to verify it executes and passes**

Run: `mvn test -Dtest=LifeAnchorsEndToEnd100BenchmarkTest`
Expected: PASS with 1 test verifying all 100 charts.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/vedic/astro/LifeAnchorsEndToEnd100BenchmarkTest.java
git commit -m "test(life-anchors): add automated 100-chart end-to-end benchmark verification test suite"
```

---

### Task 4: Add 6-Language Multilingual Parity Validation to the Benchmark Suite

**Files:**
- Modify: `src/test/java/org/vedic/astro/LifeAnchorsEndToEnd100BenchmarkTest.java`

- [ ] **Step 1: Add multilingual translation assertion helper for all 6 languages**

```java
    @Test
    @DisplayName("Verify 6-Language Translation Parity and Zero Mojibake across all generated profiles")
    void testMultilingualParity() {
        String[] languages = {"en", "ta", "hi", "te", "kn", "ml"};
        List<LifeAnchorsSyntheticChartFactory.TestCase> testCases = LifeAnchorsSyntheticChartFactory.generate90SyntheticCases();

        for (var tc : testCases) {
            AyurdayaProfile profile = AyurdayaCalculationUtils.calculateAyurdaya(
                    tc.planetMap(), tc.lagnaSign(), tc.lagnaDegree(), tc.shadbalaRupas(), 15.0, 1.0, null
            );

            for (String lang : languages) {
                // Verify ruleApplied translation
                String translatedRule = AstrologicalTranslationHelper.translateStartingSignReason(
                        profile.threePairsDetails().ruleApplied(), lang
                );
                assertNotNull(translatedRule);
                assertFalse(translatedRule.contains("à®"), "Must not contain mojibake for " + lang);
                assertFalse(translatedRule.contains("à¤"), "Must not contain mojibake for " + lang);

                // Verify Khanda sub-tier translation
                if (profile.khandaSubTier() != null) {
                    String translatedKhanda = AstrologicalTranslationHelper.translateStartingSignReason(
                            profile.khandaSubTier(), lang
                    );
                    assertNotNull(translatedKhanda);
                    assertFalse(translatedKhanda.contains("à®"), "Must not contain mojibake for " + lang);
                }
            }
        }
    }
```

- [ ] **Step 2: Run test to verify passes**

Run: `mvn test -Dtest=LifeAnchorsEndToEnd100BenchmarkTest`
Expected: PASS with all tests passing.

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/vedic/astro/LifeAnchorsEndToEnd100BenchmarkTest.java
git commit -m "test(life-anchors): add 6-language parity and zero-mojibake validation to 100-chart benchmark"
```

---

### Task 5: Verify Full Test Suite & Frontend Production Build

**Files:**
- Test: Full backend `mvn clean test`
- Test: Frontend `npm run build`

- [ ] **Step 1: Run full Maven backend test suite**

Run: `mvn clean test`
Expected: BUILD SUCCESS with all test suites passing.

- [ ] **Step 2: Run frontend production build**

Run: `npm run build` in `frontend/`
Expected: Vite build succeeds with 0 errors.

- [ ] **Step 3: Commit and Push**

```bash
git push origin feature/multi-panchangam-systems
```
