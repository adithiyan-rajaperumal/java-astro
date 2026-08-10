# Unfiltered Accurate Yearly Predictions & Past Life Phases Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Overhaul the AI Life Balan engine to provide 2-3 pivotal past life turning points (birth to present) and sharp, distinct, truthful year-by-year predictions (derived from Natal Charts D1, D2, D9, D10, D12, D30 + active Dasa-Bhukthis).

**Architecture:** Update DTOs (`PredictionResponseDTO`), revamp Gemini prompt & dynamic offline fallback in `GeminiPredictionService`, update translations for 6 languages in `translations.js`, and modernize UI in `AiPredictionsView.jsx`.

**Tech Stack:** Java 17, Spring Boot 3.3.4, Jackson, JUnit 5, React, Vite.

## Global Constraints
- Do not repeat identical boilerplate strings across year cards.
- Predictions must synthesize both Natal Varga charts (D1, D2, D9, D10, D12, D30) and the running Dasa-Bhukthi of that year.
- Unfiltered, truthful predictions: when maraka/dusthana lords are active, explicitly predict challenges (job loss, business loss, surgeries, parents' health decline/loss, family disputes).

---

### Task 1: Update DTOs in `PredictionResponseDTO.java`

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java`

- [ ] **Step 1: Add `PastKeyPhase` and update `YearlyPrediction` fields**
Add `pastKeyPhases` to `PredictionResponseDTO`. Update `YearlyPrediction` to include `yearlyTheme`, `astrologicalBasis`, `careerAndFinance`, `healthAndFamily`, and `cautionsAndRemedies`. Keep legacy getters for backwards compatibility with PDF export.

- [ ] **Step 2: Commit**
`git commit -m "feat(dto): update PredictionResponseDTO with PastKeyPhase and sharp YearlyPrediction models"`

---

### Task 2: Revamp Gemini Prompts & Dynamic Offline Fallback in `GeminiPredictionService.java`

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Modify: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Rewrite Gemini system instruction and prompt template**
Mandate 2-3 past key life phases from birth to present, and concise, high-impact yearly predictions synthesizing D1, D2, D9, D10, D12, D30 + Dasa-Bhukthi. Explicitly instruct no boilerplate repetition.

- [ ] **Step 2: Rewrite `generateOfflineRuleBasedBalan`**
Dynamically generate varied, astrological-lord-specific yearly predictions based on actual Dasa lords and Lagna.

- [ ] **Step 3: Update unit tests in `GeminiPredictionServiceTest.java`**
Verify `pastKeyPhases` and dynamic `yearlyPredictions`.

- [ ] **Step 4: Run tests**
`mvn test -Dtest=GeminiPredictionServiceTest`

- [ ] **Step 5: Commit**
`git commit -m "feat(ai): overhaul Gemini prompt and dynamic offline fallback for truthful yearly predictions"`

---

### Task 3: Update PDF Export Service & I18n Translations

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Update `PdfExportService.java`**
Render Past Key Life Phases and the sharp Yearly Prediction format in downloaded PDF.

- [ ] **Step 2: Update `frontend/src/i18n/translations.js`**
Add localized keys for `pastKeyPhases`, `yearlyTheme`, `careerAndFinance`, `healthAndFamily`, `cautionsAndRemedies`, `astrologicalBasis` across all 6 languages (`ta`, `hi`, `kn`, `te`, `ml`, `en`).

- [ ] **Step 3: Commit**
`git commit -m "feat(i18n): update translations and PDF export for past key phases and sharp yearly forecast"`

---

### Task 4: Modernize `AiPredictionsView.jsx`

**Files:**
- Modify: `frontend/src/components/AiPredictionsView.jsx`

- [ ] **Step 1: Update UI layout in `AiPredictionsView.jsx`**
Display:
1. Native Personality & Behavior Cards
2. Ayurvedic Health & Vitality Diagnostics
3. Pivotal Past Life Phases (பிறப்பு முதல் இன்று வரை) (2-3 cards)
4. Year-by-Year Predictions with filter chips, severity badges, and clear event predictions.

- [ ] **Step 2: Commit**
`git commit -m "feat(ui): overhaul AiPredictionsView with past key phases and sharp yearly prediction cards"`

---

### Task 5: Full Verification & Automated Tests

- [ ] **Step 1: Run full backend test suite**
Run `mvn clean test` (all 50+ tests passing).

- [ ] **Step 2: Run frontend build**
Run `npm run build` in `frontend/`.

- [ ] **Step 3: Commit and Push**
`git push origin feature/multi-panchangam-systems`
