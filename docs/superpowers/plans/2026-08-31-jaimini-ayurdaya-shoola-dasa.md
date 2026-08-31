# Classical Jaimini Ayurdaya 3-Pair Matrix, Shoola Dasa & Longevity Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement the complete classical Jaimini Ayurdaya 3-pair longevity engine with dual-lordship resolution, odd/even counting, Vishesha Sutras overrides, Kakshya Vriddhi/Hrasa modifiers, Jaimini Shoola Dasa (9-year Rasi periods), and an automated verification test suite across 100+ diverse charts.

**Architecture:** Extend `AyurdayaCalculationUtils.java` with modular Jaimini sign mechanics, dual-lord strength evaluation (Scorpio/Aquarius), 3-pair matrix evaluation, Vishesha Sutras, and Kakshya adjustments. Introduce `ShoolaDasaCalculationUtils.java` to compute 12 x 9-year Shoola Dasa periods and Trishoola/Rudra indicators. Validate via `AyurdayaBenchmark100Test.java` across 100+ distinct parametric natal charts.

**Tech Stack:** Java 21, Spring Boot, JUnit 5, React 18, Tailwind CSS, Lucide Icons.

## Global Constraints
- All calculations must be 100% deterministic and compliant with Maharishi Jaimini's *Upadesha Sutras* and *Brihat Parashara Hora Shastra*.
- No null pointer exceptions or unhandled chart edge cases on partial planetary data.
- Benchmark test suite must validate a minimum of 100 diverse birth charts with 100% pass rate.

---

### Task 1: Jaimini Sign Mechanics, Dual-Lordship & Odd/Even Counting

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java`
- Modify: `src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java`

**Interfaces:**
- Produces:
  - `public static int getJaiminiEighthSign(int lagnaSign)`
  - `public static String resolveDualLord(String signName, Map<String, ChartResponseDTO.PositionDetail> planetMap, int lagnaSign)`
  - `public static String getActiveEighthLord(int lagnaSign, Map<String, ChartResponseDTO.PositionDetail> planetMap)`

- [ ] **Step 1: Write failing unit tests for Savya/Apasavya counting and dual lordship**

```java
@Test
public void testSavyaApasavyaCountingAndDualLords() {
    // Odd Lagna: Aries (Sign 1) -> 8th house is Scorpio (Sign 8)
    assertEquals(8, AyurdayaCalculationUtils.getJaiminiEighthSign(1));
    // Even Lagna: Taurus (Sign 2) -> Reverse 8th count -> Libra (Sign 7)
    assertEquals(7, AyurdayaCalculationUtils.getJaiminiEighthSign(2));
    // Even Lagna: Cancer (Sign 4) -> Reverse 8th count -> Sagittarius (Sign 9)
    assertEquals(9, AyurdayaCalculationUtils.getJaiminiEighthSign(4));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest#testSavyaApasavyaCountingAndDualLords`
Expected: FAIL (method not found or wrong logic)

- [ ] **Step 3: Implement Savya/Apasavya counting and dual-lord evaluation in `AyurdayaCalculationUtils.java`**

Implement:
1. `getJaiminiEighthSign(int lagnaSign)`:
   - Odd Lagna $\rightarrow ((lagnaSign + 7 - 1) \bmod 12) + 1$
   - Even Lagna $\rightarrow ((lagnaSign - 7 - 1 + 12) \bmod 12) + 1$
2. `resolveDualLord(String sign, Map<String, PositionDetail> planetMap, int lagnaSign)`:
   - Scorpio: Compare Mars vs Ketu (conjunction count, exaltation/own sign, Kendra/Trikona placement, longitude).
   - Aquarius: Compare Saturn vs Rahu using same strength rules.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest#testSavyaApasavyaCountingAndDualLords`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java
git commit -m "feat(ayurdaya): add Savya/Apasavya counting and dual-lord resolution"
```

---

### Task 2: 3-Pair Modality Matrix, Primary Synthesis & Vishesha Sutras

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java`
- Modify: `src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java`

**Interfaces:**
- Produces:
  - `public static SynthesisResult synthesizeThreePairs(String span1, String span2, String span3, int moonHouse, boolean isOddLagna, boolean akInKendra, ChartResponseDTO.PositionDetail akPos)`

- [ ] **Step 1: Write failing unit tests for Vishesha Sutras overrides**

```java
@Test
public void testVisheshaSutraMoonInLagnaOverridesMajority() {
    // Pair 1 = Poornayu, Pair 2 = Alpayu, Pair 3 = Poornayu
    // But Moon in 1st house -> Vishesha Sutra 1 overrides majority -> Alpayu!
    var result = AyurdayaCalculationUtils.synthesizeThreePairs("Poornayu", "Alpayu", "Poornayu", 1, true, false, null);
    assertEquals("Alpayu", result.span());
    assertTrue(result.overrideReason().contains("Moon in Lagna"));
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest#testVisheshaSutraMoonInLagnaOverridesMajority`
Expected: FAIL

- [ ] **Step 3: Implement synthesis and Vishesha Sutras hierarchy in `AyurdayaCalculationUtils.java`**

Implement synthesis logic:
- Check Vishesha Sutra 1: Moon in 1st or 7th house $\rightarrow$ Pair 2 overrides.
- Check Vishesha Sutra 2: Atmakaraka in 1st or 7th house $\rightarrow$ Lagna/AK pair overrides.
- Check Tri-Samvada (3/3) and Dwi-Samvada (2/3).
- Check Asamvada (1 Poorna, 1 Madhya, 1 Alpa): Odd Lagna $\rightarrow$ Pair 3; Even Lagna $\rightarrow$ Pair 1.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest#testVisheshaSutraMoonInLagnaOverridesMajority`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java
git commit -m "feat(ayurdaya): implement 3-pair synthesis and Vishesha Sutras overrides"
```

---

### Task 3: Kakshya Vriddhi, Kakshya Hrasa & 12-Year Khanda Sub-Tiers

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java`
- Modify: `src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java`

**Interfaces:**
- Produces:
  - `public static KakshyaResult evaluateKakshyaModifiers(String baseSpan, Map<String, PositionDetail> planetMap, int lagnaSign, int moonSign)`
  - `public static String determineKhandaSubTier(String span, int ceilingAge, int lagnaNavamshaSign, int hlNavamshaSign)`

- [ ] **Step 1: Write failing unit test for Kakshya Vriddhi promotions and 12-year Khanda**

```java
@Test
public void testKakshyaVriddhiPromotionAndKhandaBracket() {
    // Alpayu promoted to Madhyayu via exalted Jupiter in 1st house
    // Verify khanda sub-tier is calculated (e.g. "Adhama Madhyayu (36 - 48 Years)")
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest#testKakshyaVriddhiPromotionAndKhandaBracket`
Expected: FAIL

- [ ] **Step 3: Implement Kakshya Vriddhi, Kakshya Hrasa, and Khanda sub-tier logic**

Implement:
1. Promotions: Jupiter in 1st/7th/Kendra/Trikona, exalted AK/Saturn/LL.
2. Demotions: Debilitated Saturn without Neechabhanga, debilitated LL in Dusthana, Papakarthari on Lagna/Moon.
3. Khanda Sub-tier: 0–12, 12–24, 24–36 (Alpayu); 36–48, 48–60, 60–72 (Madhyayu); 72–84, 84–96, 96–108 (Poornayu).

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest#testKakshyaVriddhiPromotionAndKhandaBracket`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java
git commit -m "feat(ayurdaya): implement Kakshya Vriddhi, Hrasa and Khanda sub-tiers"
```

---

### Task 4: Jaimini Shoola Dasa Calculation Engine

**Files:**
- Create: `src/main/java/org/vedic/astro/util/ShoolaDasaCalculationUtils.java`
- Create: `src/test/java/org/vedic/astro/ShoolaDasaCalculationUtilsTest.java`

**Interfaces:**
- Produces:
  - `public record ShoolaDasaReport(String startingSign, String progressionDirection, String rudraSign, List<String> trishoolaSigns, List<ShoolaPeriod> periods, String criticalWindow)`
  - `public static ShoolaDasaReport calculateShoolaDasa(int lagnaSign, Map<String, PositionDetail> planetMap, int birthYear, int targetLifespanAge)`

- [ ] **Step 1: Write failing unit test for Shoola Dasa 9-year cycles and Trishoola signs**

```java
@Test
public void testShoolaDasaProgressionAndTrishoola() {
    // Test 12 periods of 9 years totaling 108 years
    // Test starting sign selection and direct/reverse progression
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=ShoolaDasaCalculationUtilsTest`
Expected: FAIL (class not found)

- [ ] **Step 3: Implement `ShoolaDasaCalculationUtils.java`**

Implement:
1. Determine stronger of Lagna (1st) vs 7th house.
2. Progression: Direct if starting sign is Odd, Reverse if Even.
3. 12 x 9-year periods with start/end years and age ranges.
4. Calculate Trishoola signs (1, 5, 9 from 8th house) and Rudra sign (stronger of 2nd/8th lord).
5. Identify critical Shoola period matching the Ayurdaya lifespan age.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=ShoolaDasaCalculationUtilsTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/ShoolaDasaCalculationUtils.java src/test/java/org/vedic/astro/ShoolaDasaCalculationUtilsTest.java
git commit -m "feat(shoola-dasa): implement Jaimini Shoola Dasa engine and Trishoola analysis"
```

---

### Task 5: 100+ Chart Automated Benchmark Test Suite

**Files:**
- Create: `src/test/java/org/vedic/astro/AyurdayaBenchmark100Test.java`

**Interfaces:**
- Consumes: `AyurdayaCalculationUtils`, `ShoolaDasaCalculationUtils`
- Validates: 100+ diverse parametric birth charts covering all 12 Lagnas, odd/even signs, dual lords, overrides, and longevity classes.

- [ ] **Step 1: Write the benchmark test harness with 100 distinct birth configurations**

```java
@Test
public void testAyurdayaAndShoolaDasaOn100DiverseCharts() {
    // Generates/executes 100 diverse astrological configurations across:
    // - All 12 Lagna signs (Aries to Pisces)
    // - Dual-lord configurations for Scorpio and Aquarius
    // - Exalted / Debilitated Saturn, Jupiter, Lagna Lord
    // - Moon in 1st / 7th house overrides
    // - Papakarthari and Benefic Kendra placements
    // Asserts:
    // 1. Non-null results and 100% deterministic output.
    // 2. Lifespan ceilings strictly bounded: 0 <= ceiling <= 108.
    // 3. Shoola Dasa periods strictly sum to 108 years across 12 signs.
    // 4. Valid Khanda sub-tier assigned.
    // 5. Zero exceptions thrown.
}
```

- [ ] **Step 2: Run test suite to verify 100 charts execute cleanly**

Run: `mvn test -Dtest=AyurdayaBenchmark100Test`
Expected: PASS (100 charts verified)

- [ ] **Step 3: Commit**

```bash
git add src/test/java/org/vedic/astro/AyurdayaBenchmark100Test.java
git commit -m "test(ayurdaya): add 100-chart benchmark automated verification test suite"
```

---

### Task 6: DTO, Orchestration & Frontend UI Integration

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`
- Modify: `frontend/src/components/HealthLongevityView.jsx`

- [ ] **Step 1: Expose Shoola Dasa and full Jaimini 3-pair metadata in DTOs**
- [ ] **Step 2: Connect `ChartOrchestrationService` to embed Shoola Dasa in UI payload**
- [ ] **Step 3: Update `HealthLongevityView.jsx` to render the 3-pair matrix table, Vishesha overrides, Kakshya adjustments, and Shoola Dasa timeline**
- [ ] **Step 4: Verify full backend tests and build**

Run: `mvn clean test`
Expected: BUILD SUCCESS with all tests passing

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/dto/ src/main/java/org/vedic/astro/service/ frontend/src/components/HealthLongevityView.jsx
git commit -m "feat(ui): render comprehensive Jaimini 3-pair matrix, Kakshya analysis, and Shoola Dasa timeline"
```
