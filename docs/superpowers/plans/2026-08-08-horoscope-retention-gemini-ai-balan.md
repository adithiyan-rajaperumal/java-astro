# Horoscope Retention & Gemini AI Balan Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide client-side horoscope profile retention (`localStorage`) and add Gemini AI-based year-by-year past life verification milestones and future lifetime predictions, controlled via an `application.yml` flag, with full PDF export support.

**Architecture:** Spring Boot backend with `GeminiPredictionService` invoking Google Gemini REST API using structured Vedic astrological prompts, exposing config and prediction endpoints; React frontend with localStorage profile management and conditional AI Life Balan interactive timeline.

**Tech Stack:** Java 17, Spring Boot 3.3.4, OpenPDF, React 18, Vite.

---

### Task 1: Backend Configuration, Gemini Properties, DTOs & Config Endpoint

**Files:**
- Modify: `src/main/resources/application.yml`
- Create: `src/main/java/org/vedic/astro/config/GeminiProperties.java`
- Create: `src/main/java/org/vedic/astro/dto/AppConfigDTO.java`
- Create: `src/main/java/org/vedic/astro/dto/PredictionRequestDTO.java`
- Create: `src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java`
- Create: `src/test/java/org/vedic/astro/AppConfigControllerTest.java`

- [ ] **Step 1: Update `application.yml`**
Add:
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:}
  enabled: ${GEMINI_ENABLED:true}
  model: ${GEMINI_MODEL:gemini-1.5-flash}
```

- [ ] **Step 2: Create `GeminiProperties.java`**
Spring configuration bean with `apiKey`, `enabled`, `model`.

- [ ] **Step 3: Create DTOs**
Create `AppConfigDTO`, `PredictionRequestDTO`, `PredictionResponseDTO` (with `PastMilestone` and `YearlyPrediction` records). Add `boolean aiPredictionsEnabled` to `ChartUiResponseDTO`.

- [ ] **Step 4: Expose `GET /api/v1/astrology/config` in `ChartController.java`**
Return `AppConfigDTO` with `aiPredictionsEnabled` reflecting `gemini.enabled && (apiKey != null && !apiKey.isBlank())` (or `gemini.enabled`).

- [ ] **Step 5: Write unit test & verify**
Run `mvn test -Dtest=AppConfigControllerTest`.

- [ ] **Step 6: Commit Task 1**
```powershell
git add src/main/resources/application.yml src/main/java/org/vedic/astro/config/GeminiProperties.java src/main/java/org/vedic/astro/dto/ src/main/java/org/vedic/astro/controller/ChartController.java src/test/java/org/vedic/astro/AppConfigControllerTest.java; git commit -m "feat(ai): add gemini properties, prediction DTOs, and config endpoint"
```

---

### Task 2: Gemini Prediction Service & REST Endpoints

**Files:**
- Create: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Create: `src/main/java/org/vedic/astro/controller/PredictionController.java`
- Create: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Create `GeminiPredictionService.java`**
Synthesizes astrological payload into a structured Gemini prompt for past verification milestones and year-by-year future predictions. Invokes Gemini REST API and parses JSON response into `PredictionResponseDTO`.

- [ ] **Step 2: Create `PredictionController.java`**
Expose `POST /api/v1/astrology/predictions/generate`.

- [ ] **Step 3: Write unit tests**
Test prompt generation, mock API handling, and graceful fallback when disabled. Run `mvn test -Dtest=GeminiPredictionServiceTest`.

- [ ] **Step 4: Commit Task 2**
```powershell
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/main/java/org/vedic/astro/controller/PredictionController.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java; git commit -m "feat(ai): implement gemini prediction service and rest controller"
```

---

### Task 3: PDF Export Integration for AI Life Balan

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java`
- Modify: `src/test/java/org/vedic/astro/PdfExportServiceTest.java` (or create if needed)

- [ ] **Step 1: Extend `PdfExportService.java`**
Add AI Life Balan section with past verification milestones and future predictions tables when predictions are included in the request or generated.

- [ ] **Step 2: Test PDF generation**
Run `mvn test -Dtest=MultiPanchangamEngineTest`.

- [ ] **Step 3: Commit Task 3**
```powershell
git add src/main/java/org/vedic/astro/service/PdfExportService.java src/main/java/org/vedic/astro/controller/ChartController.java; git commit -m "feat(pdf): append ai life balan and yearly predictions to pdf report"
```

---

### Task 4: Frontend Horoscope Profile Retention (`localStorage`)

**Files:**
- Create: `frontend/src/utils/savedHoroscopes.js`
- Modify: `frontend/src/pages/HoroscopePage.jsx`
- Modify: `frontend/src/pages/MatchingPage.jsx`

- [ ] **Step 1: Implement `savedHoroscopes.js`**
Helper functions: `getSavedHoroscopes()`, `saveHoroscope(profile)`, `deleteSavedHoroscope(id)`, `updateSavedHoroscope(profile)`.

- [ ] **Step 2: Add Saved Profiles Manager to `HoroscopePage.jsx`**
"Save Horoscope" button and quick-switcher dropdown to load/delete saved charts.

- [ ] **Step 3: Add "Load from Saved" picker in `MatchingPage.jsx`**
Quickly populate Boy and Girl birth details from saved horoscopes.

- [ ] **Step 4: Commit Task 4**
```powershell
git add frontend/src/utils/savedHoroscopes.js frontend/src/pages/HoroscopePage.jsx frontend/src/pages/MatchingPage.jsx; git commit -m "feat(ui): add client-side horoscope profile retention and quick loader"
```

---

### Task 5: Frontend AI Predictions Sub-Tab (Past Verification & Lifetime Timeline)

**Files:**
- Create: `frontend/src/components/AiPredictionsView.jsx`
- Modify: `frontend/src/pages/HoroscopePage.jsx`
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Add localization keys to `translations.js`**
Add keys for `aiBalanTab`, `pastVerificationTitle`, `futurePredictionsTitle`, `verifiedCheck`, `careerFinance`, `healthVitality`, `familyMarriage`, `remediesGuidance`, etc., across all supported languages.

- [ ] **Step 2: Create `AiPredictionsView.jsx`**
Interactive timeline component with:
- "Generate AI Balan" action button with spinner.
- Past life verification milestone cards with interactive "Verified" checkmark buttons.
- Future year-by-year prediction cards with category filter chips.

- [ ] **Step 3: Integrate with `HoroscopePage.jsx`**
Fetch `/api/v1/astrology/config` on mount. Conditionally render the AI Life Balan sub-tab only when `aiPredictionsEnabled: true`. Pass predictions payload to PDF download handler.

- [ ] **Step 4: Commit Task 5**
```powershell
git add frontend/src/components/AiPredictionsView.jsx frontend/src/pages/HoroscopePage.jsx frontend/src/i18n/translations.js; git commit -m "feat(ui): add ai predictions tab with past verification and future timeline"
```

---

### Task 6: Full Verification & Build Validation

**Files:**
- Modify: `.superpowers/sdd/progress.md`

- [ ] **Step 1: Run Maven clean test**
Run: `mvn clean test`
Verify: All tests pass (0 failures, 0 errors).

- [ ] **Step 2: Run frontend production build**
Run: `cd frontend; npm run build; cd ..`
Verify: Vite bundle compiles with 0 errors.

- [ ] **Step 3: Commit Task 6 & record completion**
```powershell
git add .superpowers/sdd/progress.md; git commit -m "docs: record full completion of horoscope retention and gemini ai balan"
```
