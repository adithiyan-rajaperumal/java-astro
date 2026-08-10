# Daily Balan Deterministic Gochara Anchors & Horoscope Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide unified articulated yearly narrative paragraphs (replacing rigid subcategory boxes) with lifespan continuity; prompt Gemini to calculate all Yogas/Doshams fresh from raw planetary matrices without passing pre-calculated diagnostics; eliminate offline synthetic fallbacks and return localized unavailable messages on failure; provide deterministic daily astrological anchors (direction, color, number, time window); lock Daily Balan to today; display token & monetary cost badges; standardize clean text subtabs; and fix Ayanamsa/Panchangam display in the horoscope results header.

**Architecture:** 
1. `PredictionResponseDTO.java` & `GeminiPredictionService.java`:
   - `YearlyPrediction` model with `yearlyTheme`, `detailedPrediction`, `astrologicalBasis`, `cautionsAndRemedies`.
   - Exclude pre-calculated diagnostics from prompt; instruct Gemini to calculate all classical Yogas and Doshams fresh.
   - Eliminate synthetic rule-based offline fallbacks; return `enabled: false` with localized unavailable messages on failure or when disabled.
   - Compute deterministic Vara Lord, Lucky Color, Lucky Number, Favorable Direction, and Auspicious Gowri Window from date & natal Moon sign; calculate USD/INR token costs in Daily Balan parser.
2. `DailyBalanView.jsx`: Remove date picker, lock forecast to today, and render Token Usage & Cost badge (`tokens, USD, INR, model`).
3. `AiPredictionsView.jsx`: Render Token Usage & Cost badge, and render elegant cards with unified narrative paragraphs (`detailedPrediction`), theme, basis, and cautions.
4. `HoroscopePage.jsx`: Normalize Ayanamsa/Panchangam system lookup mappings, display both in header results, and use clean text labels without emoji prefixes on subtabs.
5. `translations.js`: Update subtab labels for clean text presentation.

**Tech Stack:** Java 17, Spring Boot, React 18, Vite.

---

### Task 1: Backend Unified Yearly Narrative, Fresh Yogas/Doshams, No Offline Fallback & Deterministic Gochara Anchors

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Modify: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Update `PredictionResponseDTO.YearlyPrediction` with `detailedPrediction` field**
- [ ] **Step 2: Update unit tests in `GeminiPredictionServiceTest.java` for prompt schema without precalculated diagnostics, unavailable error message on disabled/failure, deterministic daily Gochara attributes, and token cost computation**
- [ ] **Step 3: Update `GeminiPredictionService.java` with unified narrative schema in prompt, fresh Yogas/Doshams prompt directives, localized unavailable error builder (eliminating offline fallbacks), deterministic Gochara anchors, and daily token cost computation**
- [ ] **Step 4: Run `mvn test -Dtest=GeminiPredictionServiceTest` to verify PASS**
- [ ] **Step 5: Commit changes**

---

### Task 2: Frontend Token Cost Badges, Unified Narrative Cards & Today-Only Daily Balan

**Files:**
- Modify: `frontend/src/components/DailyBalanView.jsx`
- Modify: `frontend/src/components/AiPredictionsView.jsx`

- [ ] **Step 1: In `DailyBalanView.jsx`, remove date picker and lock target date to today**
- [ ] **Step 2: In `DailyBalanView.jsx` and `AiPredictionsView.jsx`, render the full Token Usage & Monetary Cost badge (`tokens, USD, INR, model`)**
- [ ] **Step 3: In `AiPredictionsView.jsx`, overhaul yearly cards to render unified narrative paragraphs (`detailedPrediction`), yearly theme, astrological basis, and cautions/remedies**
- [ ] **Step 4: Commit changes**

---

### Task 3: Horoscope Results Header Display & Clean Text Subtabs

**Files:**
- Modify: `frontend/src/pages/HoroscopePage.jsx`
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: In `HoroscopePage.jsx`, fix Ayanamsa and Panchangam system i18n mapping and display both in header card**
- [ ] **Step 2: Remove emoji prefixes from subtab buttons in `HoroscopePage.jsx` and `translations.js`**
- [ ] **Step 3: Commit changes**

---

### Task 4: End-to-End Verification & Build

**Files:**
- Test: Full backend test suite & frontend build

- [ ] **Step 1: Run `mvn clean test`**
- [ ] **Step 2: Run `npm run build` in `frontend/`**
- [ ] **Step 3: Commit and push to `origin/feature/multi-panchangam-systems`**
