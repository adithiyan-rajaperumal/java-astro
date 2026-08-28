# Forecast Span Mode Configuration & Dynamic UI/PDF Badges Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement a configuration-driven control (`gemini.forecast-mode` and `gemini.forecast-years`) enabling seamless toggling between `FULL_LIFESPAN` (continuous year-by-year predictions across native's lifespan up to age 85-95+) and `NEXT_10_YEARS` (enriched 10-year deep forecasts), complete with mode-differentiated prompts, unconstrained token ceilings, and dynamic title/badge displays in the web UI and exported PDF reports.

**Architecture:** 
- `GeminiProperties.java` reads `gemini.forecast-mode` (default: `FULL_LIFESPAN`) and `gemini.forecast-years` (numeric override) from `application.yml`.
- `GeminiPredictionService.java` calculates the target forecast span and generates distinct prompt instructions: in 10-year mode, it prompts for enriched 5–7 sentence breakdowns with quarterly financial/career milestones and granular remedies, while preserving the existing full lifespan generation in full mode. Native output token limits are left unconstrained.
- `PredictionResponseDTO.java` and `/api/config` expose forecast metadata (`forecastMode`, `startYear`, `endYear`, `startAge`, `endAge`, `totalForecastYears`).
- `AiPredictionsView.jsx` and `PdfExportService.java` dynamically adjust headers and scope badges according to the active forecast span across all 6 supported languages.

**Tech Stack:** Java 17, Spring Boot 3.3.4, OpenPDF (iText), React 18, Vite.

## Global Constraints
- `application.yml` defaults `gemini.forecast-mode` to `FULL_LIFESPAN`, with `# forecast-mode: NEXT_10_YEARS` commented out directly below for quick toggling.
- Do NOT set artificial `maxOutputTokens` restriction on standard calls — allow native unconstrained generation.
- Full multi-lingual support across English, Tamil, Hindi, Kannada, Telugu, and Malayalam.
- All unit tests must pass (`mvn test`).

---

### Task 1: Backend Configuration & Model Data Extension

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/org/vedic/astro/config/GeminiProperties.java`
- Modify: `src/main/java/org/vedic/astro/dto/AppConfigDTO.java`
- Modify: `src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java`
- Test: `src/test/java/org/vedic/astro/AppConfigControllerTest.java`

**Interfaces:**
- Consumes: `application.yml` properties
- Produces: `geminiProperties.resolveForecastYears(int currentAge, int ayurdayaCeilingAge)` and metadata fields in `AppConfigDTO` and `PredictionResponseDTO`.

- [ ] **Step 1: Write the failing unit test**
Add test in `AppConfigControllerTest.java` verifying that `/api/config` returns `forecastMode`.

```java
@Test
void testGetAppConfigContainsForecastMode() throws Exception {
    mockMvc.perform(get("/api/config"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.forecastMode").isNotEmpty());
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `mvn test "-Dtest=AppConfigControllerTest"`
Expected: FAIL due to missing `forecastMode` field.

- [ ] **Step 3: Update `application.yml` and `GeminiProperties.java`**
In `application.yml`:
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:enc:QVEuQWI4Uk42SnB2Q3pJcU1QWjh5SXhGVExXZmhWeUlONk9xb3I5RFFGNFVxSGcyTjFQRXc}
  enabled: ${GEMINI_ENABLED:true}
  life-predictions-enabled: ${GEMINI_LIFE_PREDICTIONS_ENABLED:true}
  daily-balan-enabled: ${GEMINI_DAILY_BALAN_ENABLED:true}
  matching-enabled: ${GEMINI_MATCHING_ENABLED:true}
  pdf-predictions-enabled: ${GEMINI_PDF_PREDICTIONS_ENABLED:true}
  # Forecast span mode: FULL_LIFESPAN (entire Ayurdaya lifespan) or NEXT_10_YEARS / NEXT_15_YEARS
  forecast-mode: ${GEMINI_FORECAST_MODE:FULL_LIFESPAN}
  # forecast-mode: NEXT_10_YEARS
  forecast-years: ${GEMINI_FORECAST_YEARS:0}
  model: ${GEMINI_MODEL:gemini-3.6-flash}
  temperature: ${GEMINI_TEMPERATURE:0.4}
  thinking-budget: ${GEMINI_THINKING_BUDGET:1024}
```

In `GeminiProperties.java`:
```java
private String forecastMode = "FULL_LIFESPAN";
private int forecastYears = 0;

public int resolveForecastYears(int currentAge, int ayurdayaCeilingAge) {
    if (forecastYears > 0) {
        return forecastYears;
    }
    if ("NEXT_10_YEARS".equalsIgnoreCase(forecastMode) || "10".equals(forecastMode)) {
        return 10;
    }
    if ("NEXT_15_YEARS".equalsIgnoreCase(forecastMode) || "15".equals(forecastMode)) {
        return 15;
    }
    return Math.max(1, ayurdayaCeilingAge - currentAge);
}
```

- [ ] **Step 4: Update `AppConfigDTO.java`, `PredictionResponseDTO.java`, and `ChartController.java`**
Add `forecastMode` and `forecastYears` to `AppConfigDTO`.
Add `forecastMode`, `startYear`, `endYear`, `startAge`, `endAge`, `totalForecastYears` to `PredictionResponseDTO`.
Populate them in `ChartController.getAppConfig()`.

- [ ] **Step 5: Run test to verify it passes**
Run: `mvn test "-Dtest=AppConfigControllerTest"`
Expected: PASS.

- [ ] **Step 6: Commit**
```bash
git add src/main/resources/application.yml src/main/java/org/vedic/astro/config/GeminiProperties.java src/main/java/org/vedic/astro/dto/AppConfigDTO.java src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java src/main/java/org/vedic/astro/controller/ChartController.java src/test/java/org/vedic/astro/AppConfigControllerTest.java
git commit -m "feat(config): add forecast-mode configuration properties and metadata fields"
```

---

### Task 2: Mode-Differentiated AI Prompt Generation & Fallback Logic

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Consumes: `geminiProperties.resolveForecastYears(...)`
- Produces: Mode-specific prompt text with enriched 10-year instructions or full lifespan instructions, and populated `PredictionResponseDTO` metadata.

- [ ] **Step 1: Write unit tests for mode-differentiated prompt construction**
Add test in `GeminiPredictionServiceTest.java` testing both `FULL_LIFESPAN` mode and `NEXT_10_YEARS` mode prompt generation.

```java
@Test
void test10YearModePromptDirectives() {
    geminiProperties.setForecastMode("NEXT_10_YEARS");
    String prompt = predictionService.constructAstrologicalPrompt(req);
    assertTrue(prompt.contains("NEXT 10 YEARS") || prompt.contains("10-year"));
}
```

- [ ] **Step 2: Run test to verify it fails**
Run: `mvn test "-Dtest=GeminiPredictionServiceTest"`
Expected: FAIL.

- [ ] **Step 3: Implement Mode-Differentiated Prompt in `GeminiPredictionService.java`**
1. Resolve `maxForecastYears = geminiProperties.resolveForecastYears(currentAge, targetLifespanAge);`
2. If `maxForecastYears <= 15` (10-Year Mode):
   - Instruct Gemini for an enriched 5–7 sentence breakdown per year detailing:
     - (a) Career, Business & Wealth: Promotion windows, job switches, financial investments, and peak financial quarters.
     - (b) Health, Vitality & Ayurvedic Care: Seasonal dosha imbalances, transit vulnerabilities, and diet/lifestyle adjustments.
     - (c) Family, Marriage & Progeny: Auspicious timing, relationship dynamics, and children's milestones.
     - (d) Mindset, Spiritual Evolution & Decisive Turning Points.
   - Astrological basis explains active Dasa-Bhukthi-Pratyantar lords, transits (Guru/Sani/Rahu-Ketu Gocharam), and Varga alignments (D9, D10, D12, D30).
   - Granular Vedic remedies with specific mantra recitation counts and charity guidelines.
3. If `maxForecastYears > 15` (Full Lifespan Mode):
   - Preserve the full unbroken yearly prediction synthesis across all remaining years up to the Ayurdaya ceiling without artificial token caps.
4. Populate metadata on `PredictionResponseDTO`:
   - `forecastMode(maxForecastYears <= 15 ? "NEXT_10_YEARS" : "FULL_LIFESPAN")`
   - `startYear(currentYear)`
   - `endYear(currentYear + maxForecastYears)`
   - `startAge(currentAge)`
   - `endAge(currentAge + maxForecastYears)`
   - `totalForecastYears(maxForecastYears + 1)`
5. Update `generateFallbackLifePredictions` to use the exact same `maxForecastYears` and populate response metadata.

- [ ] **Step 4: Run unit tests**
Run: `mvn test "-Dtest=GeminiPredictionServiceTest"`
Expected: PASS (16/16 tests passing).

- [ ] **Step 5: Commit**
```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(ai): implement mode-differentiated prompt directives and metadata population for 10-year vs full lifespan forecasts"
```

---

### Task 3: Dynamic PDF Export Report Headers

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`
- Test: `src/test/java/org/vedic/astro/PdfExportServiceTest.java`

**Interfaces:**
- Consumes: `data.getAiPredictions().getForecastMode()`, `getStartYear()`, `getEndYear()`
- Produces: Contextualized table header in the PDF report.

- [ ] **Step 1: Write unit test for dynamic PDF title**
Add test in `PdfExportServiceTest.java` verifying PDF generation with 10-year forecast data.

- [ ] **Step 2: Update `PdfExportService.java`**
In `PdfExportService.java` (around line 432):
```java
boolean is10Year = "NEXT_10_YEARS".equalsIgnoreCase(data.getAiPredictions().getForecastMode()) 
        || (futureList != null && futureList.size() <= 15);
int sYr = data.getAiPredictions().getStartYear() > 0 ? data.getAiPredictions().getStartYear() : (futureList != null && !futureList.isEmpty() ? futureList.get(0).getYear() : 2026);
int eYr = data.getAiPredictions().getEndYear() > 0 ? data.getAiPredictions().getEndYear() : (futureList != null && !futureList.isEmpty() ? futureList.get(futureList.size() - 1).getYear() : sYr + 10);

String futTitleStr;
if (is10Year) {
    futTitleStr = "ta".equalsIgnoreCase(lang)
            ? "அடுத்த 10 ஆண்டுகளுக்கான பலன்கள் & வழிகாட்டுதல் (" + sYr + " – " + eYr + ")"
            : "10-Year Astrological Forecast & Guidance (" + sYr + " – " + eYr + ")";
} else {
    futTitleStr = "ta".equalsIgnoreCase(lang)
            ? "வருடாந்திர வாழ்நாள் பலன்கள் & வழிகாட்டுதல் (" + sYr + " – " + eYr + ")"
            : "Year-by-Year Lifetime Astrological Forecast (" + sYr + " – " + eYr + ")";
}
```

- [ ] **Step 3: Run PDF tests**
Run: `mvn test "-Dtest=PdfExportServiceTest"`
Expected: PASS.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/org/vedic/astro/service/PdfExportService.java src/test/java/org/vedic/astro/PdfExportServiceTest.java
git commit -m "feat(pdf): dynamically render 10-year vs full lifetime forecast title in PDF exports"
```

---

### Task 4: Frontend UI Dynamic Heading, Badges & Multi-Lingual Translations

**Files:**
- Modify: `frontend/src/i18n/translations.js`
- Modify: `frontend/src/components/AiPredictionsView.jsx`
- Modify: `src/main/resources/i18n/messages*.properties`

**Interfaces:**
- Consumes: `predictions.forecastMode`, `predictions.startYear`, `predictions.endYear`, `predictions.startAge`, `predictions.endAge`
- Produces: Dynamic reactive section heading and styled scope badges in UI.

- [ ] **Step 1: Add translation keys in `translations.js` and `messages*.properties`**
Add keys for:
- `forecast10YearsTitle`: "10-Year Astrological Forecast" / "அடுத்த 10 ஆண்டுகளுக்கான வருடாந்திர பலன்கள்"
- `forecastLifetimeTitle`: "Year-by-Year Lifetime Predictions" / "முழு வாழ்நாள் வருடாந்திர பலன்கள்"
- `scope10Years`: "10-Year In-Depth Scope" / "10 ஆண்டுகள் விரிவான பலன்கள்"
- `scopeLifetime`: "Full Lifespan" / "முழு வாழ்நாள்"
across EN, TA, HI, KN, TE, ML.

- [ ] **Step 2: Update `AiPredictionsView.jsx`**
In `AiPredictionsView.jsx`:
Dynamically display the title and badge above the yearly cards:
- If `predictions.forecastMode === 'NEXT_10_YEARS'` or `lifetimeList.length <= 15`:
  - Title: `${t('forecast10YearsTitle', language)} (${sYr} – ${eYr} • Age ${sAge} to ${eAge})`
  - Badge: `[ ✨ 10-Year In-Depth Scope ]`
- If Full Lifespan:
  - Title: `${t('forecastLifetimeTitle', language)} (${sYr} – ${eYr} • Age ${sAge} to ${eAge})`
  - Badge: `[ 🔮 Full Lifespan • ${lifetimeList.length} Years ]`

- [ ] **Step 3: Test frontend build**
Run: `npm run build` in `frontend` directory.
Expected: Build succeeds with 0 errors.

- [ ] **Step 4: Commit**
```bash
git add frontend/src/i18n/translations.js frontend/src/components/AiPredictionsView.jsx src/main/resources/i18n/messages*.properties
git commit -m "feat(ui): add dynamic forecast scope badges and titles in AI Balan view"
```

---

### Task 5: End-to-End Verification & Full Test Suite Execution

**Files:**
- Test all repository tests: `mvn test`

- [ ] **Step 1: Run full Maven test suite**
Run: `mvn test`
Expected: 54+ tests pass (`BUILD SUCCESS`).

- [ ] **Step 2: Commit and Push**
```bash
git push origin feature/multi-panchangam-systems
```
