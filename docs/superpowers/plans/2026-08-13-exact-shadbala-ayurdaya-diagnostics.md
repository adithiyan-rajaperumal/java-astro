# Exact Classical Shadbala, Parashara-Jaimini Ayurdaya, & Zero-False-Positive Diagnostics Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 100% mathematically exact classical Shadbala (6-fold strengths), a deterministic Parashara-Jaimini Ayurdaya (Longevity) engine, and inject pre-computed diagnostics with strict prompt rules to eliminate false-positive yogas and doshams across the UI, PDF exports, and AI Balan.

**Architecture:** 
- `ShadbalaService.java`: Rewritten to compute all 6 Balas (Sthana, Dig, Kala, Cheshta, Naisargika, Drig) in Virupas and Rupas for all 7 classical planets per Brihat Parasara Hora Shastra (BPHS).
- `AyurdayaCalculationUtils.java`: Implements classical 3-pair modality longevity evaluation (Lagna/8th Lord, Moon/Saturn, Lagna/Hora Lagna) with Kakshya Vriddhi/Hrasa adjustments and Maraka/Badhaka Dasa alignment.
- `GeminiPredictionService.java`: Injects exact `shadbalaStrengths`, `ayurdayaProfile`, and `preCalculatedDiagnostics` into the AI prompt JSON with strict Parasari constraint rules.

**Tech Stack:** Java 17, Spring Boot 3.3.4, Jackson, JUnit 5, Maven.

## Global Constraints
- Must NOT use any hardcoded or placeholder values for Shadbala metrics (`kalaBala`, `cheshtaBala`, `drigBala`, `sthanaBala`, `digBala`).
- Classical constants (e.g. Naisargika Bala: Sun 60.0, Moon 51.43, Venus 42.86, Jupiter 34.29, Mercury 25.71, Mars 17.14, Saturn 8.57) must strictly adhere to BPHS standards.
- All existing 39 Maven tests must continue to pass without regression.

---

### Task 1: Comprehensive Classical Shadbala Engine

**Files:**
- Create/Modify: `src/main/java/org/vedic/astro/service/ShadbalaService.java`
- Test: `src/test/java/org/vedic/astro/ShadbalaServiceTest.java`

**Interfaces:**
- Consumes: `Map<String, PlanetaryPosition> d1Map`, `Map<String, Double> cusps`
- Produces: `ShadbalaDTO calculateShadbala(Map<String, PlanetaryPosition> d1Map)` where each planet has authentic `sthanaBala`, `digBala`, `kalaBala`, `cheshtaBala`, `drigBala`, `totalShadbalaRupas`, `strengthCategory`

- [ ] **Step 1: Write unit tests for Shadbala calculation**
Create `src/test/java/org/vedic/astro/ShadbalaServiceTest.java` testing:
1. Dynamic, non-zero values for all 6 Balas across all 7 classical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn).
2. Naisargika Bala matching exact BPHS constants (Sun=60.0, Moon=51.43, Venus=42.86, Jupiter=34.29, Mercury=25.71, Mars=17.14, Saturn=8.57).
3. Exalted planets having significantly higher Uchcha Bala than debilitated planets.
4. Total Rupas calculation: `totalVirupas / 60.0`.

- [ ] **Step 2: Run test to verify it fails**
Run: `mvn test -Dtest=ShadbalaServiceTest`
Expected: FAIL (or fails assertions on dynamic calculation).

- [ ] **Step 3: Implement exact 6-fold Shadbala calculations in `ShadbalaService.java`**
Implement:
1. `computeSthanaBala`: Uchcha Bala (distance from deep debilitation), Saptavargiya Bala (D1-D30 dignities), Ojhayugmarasyamsa Bala (odd/even sign and navamsa), Kendradi Bala (60/30/15 V), Drekkana Bala (15 V by decan gender).
2. `computeDigBala`: Angular distance from cardinal powerless point divided by 3 (0-60 V).
3. `computeKalaBala`: Nathonatha Bala (Day vs Night birth), Paksha Bala (Moon phase angle / 3), Tribhaga Bala, Dina/Hora Bala, Ayana Bala.
4. `computeCheshtaBala`: Planetary motional velocity and retrogradation (Vakra=60, Anuvakra=30, Chara/Sama=15-45, Manda=15; Sun/Moon use Ayana Bala).
5. `computeNaisargikaBala`: Exact BPHS fixed cosmic values.
6. `computeDrigBala`: Full classical Parasari aspect sum (benefic aspects minus malefic aspects).

- [ ] **Step 4: Run test to verify it passes**
Run: `mvn test -Dtest=ShadbalaServiceTest`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add src/main/java/org/vedic/astro/service/ShadbalaService.java src/test/java/org/vedic/astro/ShadbalaServiceTest.java
git commit -m "feat(shadbala): implement exact classical 6-fold Shadbala calculation engine per BPHS"
```

---

### Task 2: Deterministic Parashara-Jaimini Ayurdaya (Ayul) Engine

**Files:**
- Create: `src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java`
- Test: `src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java`

**Interfaces:**
- Consumes: `int lagnaSign`, `int moonSign`, `List<ChartResponseDTO.PositionDetail> d1Chart`, `ShadbalaDTO shadbala`, `List<DasaPeriod> dasaTimeline`
- Produces: `AyurdayaProfile calculateAyurdaya(...)` returning `longevityClassification` (Alpayu, Madhyayu, Poornayu), `estimatedLifespanCeiling`, `lifespanRange`, `threePairsDetails`, `kakshyaAdjustments`, `criticalMarakaWindow`

- [ ] **Step 1: Write unit tests for Ayurdaya calculation**
Create `src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java` testing:
1. 3-Pair determination for Movable, Fixed, and Dual Lagna combinations.
2. Kakshya Vriddhi with Jupiter in Kendra/Trikona.
3. Maraka/Badhaka timeline alignment producing realistic lifespan ranges (e.g. 78-85 for Poornayu).

- [ ] **Step 2: Run test to verify it fails**
Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest`
Expected: FAIL (class not found).

- [ ] **Step 3: Implement `AyurdayaCalculationUtils.java`**
Implement:
1. `getModality(int sign)`: Movable (1,4,7,10), Fixed (2,5,8,11), Dual (3,6,9,12).
2. `evaluateThreePairs(...)`: Pair 1 (Lagna Lord & 8th Lord), Pair 2 (Moon & Saturn), Pair 3 (Lagna & Moon/Hora Lagna) with 3-tier rule matrix and majority resolution.
3. `applyKakshyaAdjustments(...)`: Jupiter in Kendra/Trikona (+1 tier / +years), Saturn/Lagna Lord dignity, 8th house benefic aspects, malefic afflictions.
4. `determineMarakaWindow(...)`: Cross-referencing 2nd, 7th, Badhaka, and Dusthana lords with running Dasa timeline to pinpoint the critical transition age window.

- [ ] **Step 4: Run test to verify it passes**
Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java
git commit -m "feat(ayurdaya): implement deterministic Parashara-Jaimini longevity calculation engine"
```

---

### Task 3: Integration into AI Balan & Prompt Diagnostics

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Modify: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Injects: `ayurdayaProfile`, `preCalculatedDiagnostics`, exact `shadbalaStrengths` into `constructAstrologicalPrompt` JSON.
- Enforces: Strict Parasari constraint rules in `constructSystemInstruction` for Yogas & Doshams.

- [ ] **Step 1: Write test verifying prompt includes exact Shadbala, Ayurdaya, and Diagnostics**
In `GeminiPredictionServiceTest.java`, add assertions verifying that the generated prompt JSON contains `ayurdayaProfile`, `preCalculatedDiagnostics`, and non-hardcoded `shadbalaStrengths`.

- [ ] **Step 2: Run test to verify failure**
Run: `mvn test -Dtest=GeminiPredictionServiceTest`
Expected: FAIL (missing fields in prompt JSON).

- [ ] **Step 3: Update `GeminiPredictionService.java`**
1. Compute and inject `AyurdayaCalculationUtils.calculateAyurdaya(...)` into `inputData` as `ayurdayaProfile`.
2. Inject `c.getStructuralDiagnostics()` into `inputData` as `preCalculatedDiagnostics`.
3. Update `constructSystemInstruction` with strict classical definitions for Gajakesari, Budhaditya, Pancha Mahapurusha, and Sevvai Dosham cancellations.

- [ ] **Step 4: Run test to verify it passes**
Run: `mvn test -Dtest=GeminiPredictionServiceTest`
Expected: PASS.

- [ ] **Step 5: Commit**
```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(ai-balan): inject deterministic Ayurdaya and verified diagnostics with zero-false-positive constraints"
```

---

### Task 4: Full Suite Regression Verification & Build Validation

- [ ] **Step 1: Run full Maven test suite**
Run: `mvn test`
Expected: All 40+ tests pass with `BUILD SUCCESS`.

- [ ] **Step 2: Verify frontend production build**
Run: `cd frontend && npm run build`
Expected: Clean build, 0 errors.

- [ ] **Step 3: Commit and push**
```bash
git push origin feature/multi-panchangam-systems
```
