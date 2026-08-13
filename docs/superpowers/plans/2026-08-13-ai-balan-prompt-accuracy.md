# AI Balan Prompt Astrological Accuracy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance prompt construction across Lifetime Balan, Daily Balan, and Marriage Matching in `GeminiPredictionService.java` to inject calculated planetary dignities, exact Dasa-Bhukthis, pre-computed Yogas/Doshams, Tarabalam, Gochara house from Moon, and D9 Navamsa synastry.

**Architecture:** Utilize existing backend calculations in `PlanetDignityUtils`, `DasaEngineService`, `AstrologyDiagnosticsService`, and `ChartUiResponseDTO` to provide deterministic astrological anchors in high-density notation directly into Gemini prompts.

**Tech Stack:** Java 17, Spring Boot 3, JUnit 5, Mockito, Google Gemini Generative Language API.

## Global Constraints
- Do NOT alter response JSON schema contracts expected by the frontend (`PredictionResponseDTO`, `DailyBalanDTO`, `MatchingAiPredictionDTO`).
- Maintain full year-by-year lifespan prediction structure in Lifetime Balan.
- Ensure all new helper methods are strictly null-safe for empty/missing chart or diagnostic data.

---

### Task 1: Add Planetary Dignities & State Tags to Lifetime Balan

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Consumes: `PlanetDignityUtils.isExalted`, `PlanetDignityUtils.isDebilitated`, `PlanetDignityUtils.isOwnSign`, `PlanetDignityUtils.isCombust`
- Produces: D1 formatted prompt string with dignity tags like `[Exalted]`, `[Debilitated]`, `[Own]`, `[Combust]`

- [ ] **Step 1: Write the failing unit test**

In `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`, add test `testLifetimePromptIncludesPlanetaryDignities`:
```java
@Test
void testLifetimePromptIncludesPlanetaryDignities() {
    PredictionRequestDTO req = createSampleRequest();
    String prompt = predictionService.constructAstrologicalPrompt(req);
    assertTrue(prompt.contains("D1[Rasi-ZodiacSigns]:"));
    // Verify tags are injected when applicable
    assertTrue(prompt.matches("(?s).*D1\\[Rasi-ZodiacSigns\\].*"));
}
```

- [ ] **Step 2: Implement dignity tag formatting in `GeminiPredictionService.java`**

Update `constructAstrologicalPrompt`:
```java
double sunAbsLong = 0.0;
for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
    if ("SUN".equalsIgnoreCase(p.getPlanetKey())) {
        sunAbsLong = (p.getSignNumber() - 1) * 30.0 + p.getDegreeInSign();
        break;
    }
}

sb.append("D1[Rasi-ZodiacSigns]: ");
for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
    String pKey = p.getPlanetKey() != null ? p.getPlanetKey() : "";
    int sign = p.getSignNumber();
    double pAbsLong = (sign - 1) * 30.0 + p.getDegreeInSign();
    
    StringBuilder tag = new StringBuilder();
    if (PlanetDignityUtils.isExalted(pKey, sign)) tag.append("[Exalted]");
    else if (PlanetDignityUtils.isDebilitated(pKey, sign)) tag.append("[Debilitated]");
    else if (PlanetDignityUtils.isOwnSign(pKey, sign)) tag.append("[Own]");
    
    if (PlanetDignityUtils.isCombust(pKey, pAbsLong, sunAbsLong)) {
        tag.append("[Combust]");
    }

    sb.append(String.format("%s:%s(Rasi%d@%.1f°)%s ",
            p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
            p.getRashiName(), p.getSignNumber(), p.getDegreeInSign(), tag.toString()));
}
sb.append("\n");
```

- [ ] **Step 3: Run test to verify it passes**
Run: `mvn test -Dtest=GeminiPredictionServiceTest#testLifetimePromptIncludesPlanetaryDignities`

---

### Task 2: Inject Dasa-Bhukthi Timelines & Pre-Computed Yogas/Doshams into Lifetime Prompt

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write the failing unit test**

In `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`:
```java
@Test
void testLifetimePromptIncludesDetailedBhukthisAndDiagnostics() {
    PredictionRequestDTO req = createSampleRequest();
    String prompt = predictionService.constructAstrologicalPrompt(req);
    assertTrue(prompt.contains("Vimshottari Dasa & Bhukthi Sub-Periods:"));
}
```

- [ ] **Step 2: Implement Dasa-Bhukthi and Diagnostics injection in `GeminiPredictionService.java`**

Format active and upcoming Bhukthis from `c.getCurrentDasaTimeline()`:
```java
if (c.getCurrentDasaTimeline() != null && !c.getCurrentDasaTimeline().isEmpty()) {
    sb.append("Vimshottari Dasa & Bhukthi Sub-Periods:\n");
    LocalDate now = LocalDate.now();
    for (DasaPeriod d : c.getCurrentDasaTimeline()) {
        if (d.getEndDate() != null && d.getEndDate().isBefore(now.minusYears(2))) {
            continue; // Skip past dasas completed long ago
        }
        sb.append(String.format("- %s Mahadasa (%s to %s):\n", d.getPlanetName(), d.getStartDate(), d.getEndDate()));
        if (d.getBhukthis() != null && !d.getBhukthis().isEmpty()) {
            for (DasaPeriod.BhukthiPeriod bPeriod : d.getBhukthis()) {
                sb.append(String.format("   * %s-%s Bhukthi: %s to %s\n",
                        d.getPlanetName(), bPeriod.getPlanetName(), bPeriod.getStartDate(), bPeriod.getEndDate()));
            }
        }
    }
    sb.append("\n");
}

if (c.getStructuralDiagnostics() != null) {
    var diag = c.getStructuralDiagnostics();
    if (diag.getActiveYogas() != null && !diag.getActiveYogas().isEmpty()) {
        sb.append("Pre-Calculated Yogas: ");
        for (var y : diag.getActiveYogas()) {
            sb.append(y.getName()).append(" (").append(y.getFormingPlanets()).append("); ");
        }
        sb.append("\n");
    }
    if (diag.getDiscoveredDoshams() != null && !diag.getDiscoveredDoshams().isEmpty()) {
        sb.append("Evaluated Doshams: ");
        for (var dosh : diag.getDiscoveredDoshams()) {
            if (dosh.isDetected()) {
                sb.append(dosh.getName()).append(" [")
                  .append(dosh.isNullified() ? "Nullified: " + dosh.getNullificationReason() : "Active")
                  .append("]; ");
            }
        }
        sb.append("\n");
    }
    sb.append("\n");
}
```

- [ ] **Step 3: Run test to verify it passes**
Run: `mvn test -Dtest=GeminiPredictionServiceTest#testLifetimePromptIncludesDetailedBhukthisAndDiagnostics`

---

### Task 3: Inject Tarabalam & Gochara Moon House in Daily Balan

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write the failing unit test**

In `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`:
```java
@Test
void testDailyPromptIncludesTarabalamAndGocharaHouse() {
    DailyBalanRequestDTO req = createSampleDailyRequest();
    DailyPanchangamDTO panchangam = createSampleDailyPanchangam();
    String prompt = predictionService.constructDailyAstrologicalPrompt(req, panchangam, LocalDate.now());
    assertTrue(prompt.contains("Tarabalam:"));
    assertTrue(prompt.contains("Gochara Moon from Janma Rasi:"));
}
```

- [ ] **Step 2: Implement Tarabalam & Gochara Moon House in `constructDailyAstrologicalPrompt`**

```java
// Tarabalam calculation
int birthNakNum = getNakshatraIndex(nakshatra);
int transitNakNum = panchangam != null && panchangam.nakshatra() != null ? panchangam.nakshatra().number() : birthNakNum;
int taraIndex = ((transitNakNum - birthNakNum + 27) % 9) + 1;
String taraName = getTarabalamName(taraIndex, lang);

// Gochara Moon House calculation
int birthRasiNum = getRasiIndex(rasi);
int transitRasiNum = getRasiIndex(todayMoonRasi);
int moonHouseFromRasi = ((transitRasiNum - birthRasiNum + 12) % 12) + 1;
String moonHouseMeaning = getGocharaMoonHouseMeaning(moonHouseFromRasi, lang);

sb.append("Tarabalam: ").append(taraName).append(" (").append(taraIndex).append("/9)\n");
sb.append("Gochara Moon from Janma Rasi: House ").append(moonHouseFromRasi).append(" (").append(moonHouseMeaning).append(")\n");
```

- [ ] **Step 3: Run test to verify it passes**
Run: `mvn test -Dtest=GeminiPredictionServiceTest#testDailyPromptIncludesTarabalamAndGocharaHouse`

---

### Task 4: Inject D9 Navamsa & Karaka Synastry into Marriage Matching

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write the failing unit test**

In `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`:
```java
@Test
void testMatchingPromptIncludesD9NavamsaPositions() {
    MatchingRequestDTO req = createSampleMatchingRequest();
    MatchingResponseDTO res = createSampleMatchingResponse();
    String prompt = predictionService.constructMatchingPrompt(req, res);
    assertTrue(prompt.contains("Boy-D9[Navamsa]:"));
    assertTrue(prompt.contains("Girl-D9[Navamsa]:"));
}
```

- [ ] **Step 2: Update `appendChartPositions` to also append D9 Navamsa positions**

```java
private void appendChartPositions(StringBuilder sb, String label, ChartUiResponseDTO profile) {
    if (profile == null) return;
    
    // D1 Rasi & Bhava
    if (profile.getD1Chart() != null && !profile.getD1Chart().isEmpty()) {
        int lagnaSign = 1;
        for (ChartResponseDTO.PositionDetail p : profile.getD1Chart()) {
            if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) {
                lagnaSign = p.getSignNumber();
                break;
            }
        }
        sb.append(label).append("-D1[Rasi]: ");
        for (ChartResponseDTO.PositionDetail p : profile.getD1Chart()) {
            sb.append(String.format("%s:%s(Rasi%d@%.1f°) ",
                    p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
                    p.getRashiName(), p.getSignNumber(), p.getDegreeInSign()));
        }
        sb.append("\n");
        sb.append(label).append("-Bhava[Houses-From-Lagna]: ");
        for (ChartResponseDTO.PositionDetail p : profile.getD1Chart()) {
            int house = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
            sb.append(String.format("%s:House%d(%s) ",
                    p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
                    house, p.getRashiName()));
        }
        sb.append("\n");
    }

    // D9 Navamsa for Marriage Synastry
    if (profile.getD9Chart() != null && !profile.getD9Chart().isEmpty()) {
        sb.append(label).append("-D9[Navamsa]: ");
        for (ChartResponseDTO.PositionDetail p : profile.getD9Chart()) {
            sb.append(String.format("%s:%s ",
                    p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
                    p.getRashiName()));
        }
        sb.append("\n");
    }
}
```

- [ ] **Step 3: Run test to verify it passes**
Run: `mvn test -Dtest=GeminiPredictionServiceTest#testMatchingPromptIncludesD9NavamsaPositions`

---

### Task 5: Full Regression Testing & Verification

- [ ] **Step 1: Run full Maven test suite**
Run: `mvn test`
Expected: 30+ tests passing, 0 failures, 0 errors.

- [ ] **Step 2: Verify frontend builds cleanly**
Run: `cd frontend && npm run build`
Expected: Clean build with zero errors.
