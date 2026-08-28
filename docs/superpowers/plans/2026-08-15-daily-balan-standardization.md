# Standardized Multilingual Daily Balan Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize the Daily Balan (இன்றைய ராசி பலன்) into a unified single-paragraph astrological forecast covering all key dimensions of daily life, with full 6-language localization parity across UI and AI outputs, and validated across 5 core benchmark natives.

**Architecture:** Update `DailyBalanDTO` with a single unified `dailyNarrative` field, update `GeminiPredictionService` prompt instructions and response parser with 6-language deterministic anchors, redesign `DailyBalanView.jsx` in the application's clean white & saffron visual identity, localize all translation keys in `translations.js`, and add comprehensive validation tests in `DailyBalanValidationTest.java`.

**Tech Stack:** Java 17, Spring Boot, Jackson, React (JSX), Vanilla CSS tokens, JUnit 5.

## Global Constraints

- **Language Parity**: All 6 application languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`) must be 100% supported in UI labels, deterministic daily anchors, and AI prompts.
- **Unified Aesthetics**: Frontend components must use unified tokens (`var(--bg-card)` `#ffffff`, `var(--bg-primary)` `#fffaf4`, `var(--border)` `#f0e2d0`, `var(--accent-gold)` / `var(--accent-saffron)`).
- **Astrological Rigor**: Daily narrative must be a dense, cohesive 4-6 sentence paragraph synthesizing Gochara Moon transit, Dasa-Bhukthi, Career, Wealth, Health, Family, and Remedy without social boilerplate.
- **Deterministic Anchors**: Lucky color, lucky number, favorable direction, and auspicious time window are computed deterministically per weekday & language to prevent hallucinations.
- **Backward Compatibility**: Fallback fields (`generalOutlook`, `careerWork`, `financeWealth`, `healthVitality`, `relationshipFamily`) must remain in `DailyBalanDTO` and automatically synthesize into `dailyNarrative` if received.

---

### Task 1: Update `DailyBalanDTO.java`

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/DailyBalanDTO.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Produces: `DailyBalanDTO.getDailyNarrative()`, `DailyBalanDTO.setDailyNarrative(String)`

- [ ] **Step 1: Write test for DailyBalanDTO with dailyNarrative serialization**

```java
@Test
public void testDailyBalanDTOSerializationWithDailyNarrative() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    DailyBalanDTO dto = DailyBalanDTO.builder()
            .enabled(true)
            .targetDate("2026-08-15")
            .rasi("Mesha")
            .dailyNarrative("Today's transit Moon brings auspicious energy for career and wealth growth.")
            .dailyRemedy("Chant Gayatri Mantra 9 times.")
            .luckyColor("Ruby Red")
            .luckyNumber("1 & 4")
            .favorableDirection("East")
            .bestTimeWindow("07:30 AM - 09:00 AM")
            .build();
    String json = mapper.writeValueAsString(dto);
    assertTrue(json.contains("dailyNarrative"));
    DailyBalanDTO deserialized = mapper.readValue(json, DailyBalanDTO.class);
    assertEquals("Today's transit Moon brings auspicious energy for career and wealth growth.", deserialized.getDailyNarrative());
}
```

- [ ] **Step 2: Run test to verify status**

Run: `mvn test -Dtest=GeminiPredictionServiceTest#testDailyBalanDTOSerializationWithDailyNarrative`
Expected: FAIL or compilation error before adding field.

- [ ] **Step 3: Add `dailyNarrative` field to `DailyBalanDTO.java`**

```java
package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyBalanDTO {
    private boolean enabled;
    private String message;
    private String targetDate;
    private String rasi;
    private String nakshatra;
    private String runningDasaBhukthi;
    private boolean chandrashtama;
    private String dailyNarrative;
    private String generalOutlook;
    private String careerWork;
    private String financeWealth;
    private String healthVitality;
    private String relationshipFamily;
    private String luckyColor;
    private String luckyNumber;
    private String favorableDirection;
    private String bestTimeWindow;
    private String dailyRemedy;
    private PredictionResponseDTO.TokenUsage tokenUsage;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=GeminiPredictionServiceTest#testDailyBalanDTOSerializationWithDailyNarrative`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/dto/DailyBalanDTO.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(dto): add dailyNarrative field to DailyBalanDTO"
```

---

### Task 2: Standardize Daily System Instructions, Prompt Schema & Response Parser in `GeminiPredictionService.java`

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Consumes: `DailyBalanRequestDTO`, `DailyPanchangamDTO`, `LocalDate`
- Produces: `constructDailySystemInstruction(lang)`, `constructDailyAstrologicalPrompt(req, panchangam, targetDate)`, `parseDailyGeminiResponse(rawJson, req, targetDate, panchangam)`

- [ ] **Step 1: Write test verifying prompt schema and parser for single dailyNarrative**

```java
@Test
public void testDailyBalanPromptSchemaAndResponseParsing() {
    DailyBalanRequestDTO req = new DailyBalanRequestDTO();
    req.setLanguage("ta");
    BirthDetailsDTO b = new BirthDetailsDTO("Adithiyan", 1995, 7, 19, 13, 10, 0, 12.9165, 79.1325, 5.5, "Vellore", "Asia/Kolkata");
    req.setBirthDetails(b);
    ChartUiResponseDTO c = new ChartUiResponseDTO();
    ChartUiResponseDTO.BirthProfile bp = new ChartUiResponseDTO.BirthProfile();
    bp.setLagna("Tula");
    bp.setRashi("Mesha");
    bp.setNakshatra("Bharani");
    c.setBirthProfile(bp);
    req.setChartData(c);

    String prompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 15));
    assertTrue(prompt.contains("dailyNarrative"));
    assertTrue(prompt.contains("dailyRemedy"));

    String mockAiJson = "{\n" +
            "  \"candidates\": [{\n" +
            "    \"content\": {\n" +
            "      \"parts\": [{\n" +
            "        \"text\": \"{\\n  \\\"dailyNarrative\\\": \\\"இன்றைய கோச்சார சந்திரன் தொழில் மற்றும் பொருளாதாரத்தில் மேன்மையை தரும்.\\\",\\n  \\\"dailyRemedy\\\": \\\"சூரிய நமஸ்காரம் செய்யவும்.\\\"\\n}\"\n" +
            "      }]\n" +
            "    }\n" +
            "  }]\n" +
            "}";

    DailyBalanDTO parsed = predictionService.parseDailyGeminiResponse(mockAiJson, req, LocalDate.of(2026, 8, 15), null);
    assertNotNull(parsed);
    assertTrue(parsed.isEnabled());
    assertEquals("இன்றைய கோச்சார சந்திரன் தொழில் மற்றும் பொருளாதாரத்தில் மேன்மையை தரும்.", parsed.getDailyNarrative());
    assertEquals("சூரிய நமஸ்காரம் செய்யவும்.", parsed.getDailyRemedy());
}
```

- [ ] **Step 2: Run test to verify status**

Run: `mvn test -Dtest=GeminiPredictionServiceTest#testDailyBalanPromptSchemaAndResponseParsing`
Expected: FAIL before updating prompts and parser.

- [ ] **Step 3: Update `GeminiPredictionService.java`**
  1. In `constructDailySystemInstruction(String lang)`: Mandate writing 100% in native script of `lang` (`en`, `ta`, `hi`, `te`, `kn`, `ml`) with pure classical astrological deductions and zero boilerplate.
  2. In `constructDailyAstrologicalPrompt(...)`: Update JSON output schema to return `dailyNarrative` and `dailyRemedy`.
  3. In `parseDailyGeminiResponse(...)`: Parse `dailyNarrative`, with fallback aggregation if legacy fields are returned.
  4. In `calculateDeterministicAnchors(LocalDate targetDate, String lang)`: Complete full 6-language translations for all 7 days of the week.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=GeminiPredictionServiceTest#testDailyBalanPromptSchemaAndResponseParsing`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(ai): standardize Daily Balan prompt to single comprehensive paragraph with full 6-language deterministic anchors"
```

---

### Task 3: Localize Daily Balan Keys in `frontend/src/i18n/translations.js`

**Files:**
- Modify: `frontend/src/i18n/translations.js`
- Test: `frontend/src/i18n/translations.test.js` (or node check script)

**Interfaces:**
- Produces: 6-language translation parity for `dailyBalanTitle`, `dailyBalanSubtitle`, `generateDailyBalan`, `generatingDailyBalan`, `regenerateDailyBalan`, `cachedNoticeDaily`, `chandrashtamaAlert`, `chandrashtamaAlertDesc`, `dailyForecastParagraphTitle`, `dailyLuckyFactorsTitle`, `luckyColor`, `luckyNumber`, `favorableDirection`, `bestTimeWindow`, `dailyRemedy`, `tokensCount`.

- [ ] **Step 1: Write script to verify all Daily Balan keys in `translations.js`**

Verify that all keys are present at root level for `en`, `ta`, `hi`, `te`, `kn`, and `ml`.

- [ ] **Step 2: Update `frontend/src/i18n/translations.js` with all 6 languages**

Add missing translations for Daily Balan across `en`, `ta`, `hi`, `te`, `kn`, and `ml`.

- [ ] **Step 3: Run node verification script**

Run: `node -e "const { t } = require('./frontend/src/i18n/translations'); ['en','ta','hi','te','kn','ml'].forEach(l => console.log(l, t('dailyBalanTitle', l), t('dailyForecastParagraphTitle', l)));"`
Expected: Clean outputs for all 6 languages without returning raw fallback keys.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/i18n/translations.js
git commit -m "feat(i18n): add 6-language translation keys for standardized Daily Balan"
```

---

### Task 4: Redesign `DailyBalanView.jsx` in Unified White & Saffron Theme

**Files:**
- Modify: `frontend/src/components/DailyBalanView.jsx`

**Interfaces:**
- Consumes: `dailyBalan`, `language`, `loading`, `error`, `onGenerateDaily`

- [ ] **Step 1: Redesign `DailyBalanView.jsx`**
  - Card 1: Top Status & Cache Metadata Bar (`var(--bg-card)`, date, rasi, nakshatra, dasa, token usage, regenerate button).
  - Card 2: Chandrashtama Alert (conditional, high-contrast caution styling).
  - Card 3: Daily Astrological Forecast Card (displaying `dailyBalan.dailyNarrative` or synthesized fallback).
  - Card 4: Daily Lucky Factors Pill Bar (4 compact cards: Lucky Color, Lucky Number, Direction, Best Time).
  - Card 5: Daily Vedic Remedy Box (saffron/gold highlighted card).

- [ ] **Step 2: Verify frontend build**

Run: `cd frontend && npm run build`
Expected: Build succeeds with 0 errors.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/components/DailyBalanView.jsx
git commit -m "feat(ui): redesign DailyBalanView into unified white & saffron layout with single cohesive forecast paragraph"
```

---

### Task 5: Implement Comprehensive Automated Validation Across 5 Core Natives (`DailyBalanValidationTest.java`)

**Files:**
- Create: `src/test/java/org/vedic/astro/DailyBalanValidationTest.java`

**Benchmark Natives Tested:**
1. **Adithiyan**: `19-07-1995 13:10 Vellore` (Tula Lagna, Mesha Moon)
2. **Uthayasri**: `17-08-2002 15:15 Viluppuram` (Dhanus Lagna, Vrishchika Moon)
3. **Padmasri**: `31-07-2001 19:30 Viluppuram` (Makara Lagna, Dhanus Moon)
4. **Deepanathan**: `11-04-1969 02:50 AM Tiruvannamalai` (Makara Lagna, Makara Moon)
5. **Mahaveer**: `18-04-2024 06:37 AM Vellore` (Mesha Lagna, Cancer Moon)

- [ ] **Step 1: Write `DailyBalanValidationTest.java`**
  - Verify Gochara Moon house computation relative to Janma Rasi for all 5 natives.
  - Verify Chandrashtama detection when transit Moon is in the 8th house from Janma Rasi.
  - Verify Tarabalam calculation (1-9 cycle) for each native.
  - Verify deterministic anchor translations across all 6 languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`).
  - Verify prompt construction and mock JSON response parsing for both new and legacy formats.

- [ ] **Step 2: Run test suite**

Run: `mvn test -Dtest=DailyBalanValidationTest`
Expected: 5/5 tests PASS.

- [ ] **Step 3: Run full backend test suite**

Run: `mvn test`
Expected: All 88+ tests PASS.

- [ ] **Step 4: Commit**

```bash
git add src/test/java/org/vedic/astro/DailyBalanValidationTest.java
git commit -m "test(ai): add comprehensive Daily Balan validation tests across 5 core benchmark natives"
```
