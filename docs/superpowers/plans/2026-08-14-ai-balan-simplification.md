# Simplified AI Balan Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the AI Balan (Horoscope) tab into a simplified, high-speed, autonomous prediction engine that provides deep psychological analysis, past life retrospective milestones, autonomous Ayul & Yoga calculation, and rich uncapped yearly paragraph narratives.

**Architecture:** 
- Backend reuses proven mathematical house and divisional chart engines (`VargaEngineService`, `PlanetDignityUtils`) to build an unambiguous, compact prompt matrix with explicit `(H#)` house tags and Varga Lagnas, while omitting pre-computed Ayul, Yogas, and Doshas.
- Gemini 2.5 autonomously calculates lifespan ceiling (Ayul), detects active Yogas/Doshas, generates age-adaptive past retrospective milestones, and writes comprehensive annual narrative paragraphs.
- Frontend `AiPredictionView.jsx` renders a streamlined, modern UI with 10-Year / Lifetime scope toggles and 4 core narrative cards with full 6-language i18n support.

**Tech Stack:**
- Java 21 / Spring Boot 3.3.0
- Google Gemini API (gemini-2.5-flash / gemini-2.5-pro)
- React 19 / Vite / Vanilla CSS (Rich Glassmorphism & Gold Vedic Dark Theme)
- JUnit 5 / AssertJ / Mockito

## Global Constraints
- Do NOT pass pre-calculated `ayurdaya`, `detectedYogas`, or `detectedDoshas` to Gemini.
- Always include `Lagna` and `(H#)` house notation in every divisional chart in `divisionalCharts`.
- Retain disambiguated `houseLordshipTable` and `placedInD1House` in `planetaryMatrix`.
- Do NOT impose artificial word or token caps on yearly narratives.
- Support 100% full translations across all 6 languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`).

---

### Task 1: Backend DTO & Service Prompt Construction Updates

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Consumes: `ChartResponseDTO`, `VargaEngineService.calculateVargaSign(...)`, `PlanetDignityUtils`.
- Produces: `constructAstrologicalPrompt(PredictionRequestDTO req)` returning dense JSON prompt omitting Ayul/Yogas/Doshas and including D1-D60 with `Lagna` and `(H#)`.

- [ ] **Step 1: Update `PredictionResponseDTO.java` with new clean schema**
  Add DTO inner classes:
  - `AiLongevityAnalysis` (`calculatedAyulCeiling`, `classification`, `primarySpanRationale`, `activeYogasIdentified`, `activeDoshasIdentified`).
  - `PersonalityAndBehavior` (`coreTemperament`).
  - `RetrospectivePastMilestone` (`approxPeriod`, `milestoneTitle`, `eventNarrative`).
  - `YearlyPrediction` (`year`, `age`, `activeDasaBhukthi`, `annualNarrative`).

- [ ] **Step 2: Update `GeminiPredictionService.java` prompt builder**
  - Update `constructSystemInstruction(lang)` with autonomous Ayul, age-adaptive past events, and uncapped rich paragraph narrative directives.
  - Update `constructAstrologicalPrompt(req)` to:
    - Omit `ayurvedicHealthProfile`, `ayurdaya`, `detectedYogas`, `detectedDoshas`.
    - Build `divisionalCharts` for D1, D2, D3, D7, D9, D10, D12, D20, D24, D30, D60 with Varga `Lagna` and `(H#)` house placements computed via `VargaEngineService`.

- [ ] **Step 3: Update `parseGeminiResponse` in `GeminiPredictionService.java`**
  - Parse the new JSON schema cleanly into `PredictionResponseDTO`.
  - Provide fallback handling for backwards compatibility and resilient rendering.

- [ ] **Step 4: Update and run unit tests in `GeminiPredictionServiceTest.java`**
  - Run `mvn test -Dtest=GeminiPredictionServiceTest`
  - Expected: PASS

- [ ] **Step 5: Commit backend service updates**
  ```bash
  git add src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
  git commit -m "feat(ai): update Gemini prompt payload to autonomous Ayul and rich annual narrative schema"
  ```

---

### Task 2: Frontend UI & Internationalization Redesign

**Files:**
- Modify: `frontend/src/components/AiPredictionView.jsx`
- Modify: `frontend/src/i18n/translations.js`

**Interfaces:**
- Consumes: `PredictionResponseDTO` with `aiLongevityAnalysis`, `personalityAndBehavior`, `retrospectivePastMilestones`, and `yearlyPredictions`.
- Produces: Responsive, simplified AI Balan tab UI with 6-language support.

- [ ] **Step 1: Add i18n translation keys in `translations.js` across all 6 languages**
  Keys: `personalityBehaviorTitle`, `pastMilestonesTitle`, `aiAyulLongevityTitle`, `yearlyPredictionsTitle`, `tenYearsForecast`, `lifetimeForecast`, `ageLabel`, `activeYogasLabel`, `activeDoshasLabel`, etc.

- [ ] **Step 2: Redesign `AiPredictionView.jsx` layout**
  - Forecast Mode Toggle Bar (Next 10 Years vs Lifetime Balan).
  - 🧠 Personality & Behavior Card.
  - 🕰️ Retrospective Life Milestones Card (2–3 past events with timeline badges).
  - ⏳ AI Longevity & Active Yogas Card (Ayul ceiling, classification badge, classical rationale, active yogas chips).
  - 📜 Year-by-Year Narrative Stream Cards (Year, Age, Active Dasa-Bhukthi badge, complete unified narrative paragraph).

- [ ] **Step 3: Audit translations and verify 0 missing keys**
  Run: `node check_all_i18n_keys.js`
  Expected: SUCCESS: Zero missing keys across all 6 languages!

- [ ] **Step 4: Build frontend bundle**
  Run: `npm run build` in `frontend/`
  Expected: Clean build with 0 errors.

- [ ] **Step 5: Commit frontend updates**
  ```bash
  git add frontend/src/components/AiPredictionView.jsx frontend/src/i18n/translations.js
  git commit -m "feat(ui): redesign AI Balan tab with personality, retrospective milestones, and annual narrative stream"
  ```

---

### Task 3: Full End-to-End Validation & Verification

**Files:**
- Test: `src/test/java/org/vedic/astro/ThreeCoreNativesValidationTest.java`

- [ ] **Step 1: Run full Maven test suite**
  Run: `mvn test`
  Expected: All 87+ tests pass (0 failures, 0 errors).

- [ ] **Step 2: Validate against 5 core benchmark charts**
  1. Adithiyan (19-07-1995 13:10 Vellore, TN)
  2. Uthayasri (17-08-2002 15:15 Viluppuram, TN)
  3. Padmasri (31-07-2001 19:30 Viluppuram, TN)
  4. Deepanathan (11-04-1969 02:50 AM Tiruvannamalai, TN)
  5. Mahaveer (18-04-2024 06:37 AM Vellore, TN)

- [ ] **Step 3: Commit and Push**
  ```bash
  git commit -m "chore: complete validation of simplified AI Balan engine across benchmark natives"
  git push origin feature/multi-panchangam-systems
  ```
