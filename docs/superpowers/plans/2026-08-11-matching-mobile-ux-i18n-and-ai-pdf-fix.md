# Matching Page Mobile UX, Complete i18n, and AI PDF Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide a sleek mobile-first responsive layout for the Matching Page, ensure 100% multilingual translation coverage across all matching dropdowns and AI elements, and guarantee AI compatibility is synthesized and included in downloaded PDF reports.

**Architecture:**
1. `frontend/src/i18n/translations.js`: Add translations for methodology options (*Ashta Koota* / *Dasa Porutham*), strictness (*Lenient*, *Moderate*, *Strict*), error states, and AI banner/card metadata across all 6 languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`).
2. `frontend/src/index.css`: Add responsive mobile typography and scaling for `.matching-header`, `.score-circle`, and `.verdict-badge`.
3. `frontend/src/pages/MatchingPage.jsx` & `frontend/src/components/AiMatchingView.jsx`: Bind all UI strings to `t(key, language)` and tune grid minmax for mobile screens.
4. `MatchingController.java`: Normalize language extraction and on-demand attach AI prediction to PDF response if enabled.

**Tech Stack:** Java 17, Spring Boot 3.3.4, React 19, Vite 8, iText PDF.

---

### Task 1: Complete Multilingual Synchronization (i18n) Across All 6 Languages

**Files:**
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Add missing matching translation keys to `en`, `ta`, `hi`, `te`, `kn`, `ml`**
Add:
- `ashtaKoota`, `dasaPorutham`
- `strictnessLenient`, `strictnessModerate`, `strictnessStrict`
- `analyzingMatchNotice`, `matchingEngineError`, `tryAgain`, `exceptionLabel`
- `aiMatchingBannerDesc`, `aiMatchingLoadingDesc`, `cached3HourNotice`, `refreshAiAnalysis`, `astrologicalBasisLabel`

- [ ] **Step 2: Commit i18n additions**
```bash
git add frontend/src/i18n/translations.js
git commit -m "feat(i18n): add comprehensive translations for matching methodologies, strictness, and AI metadata"
```

---

### Task 2: Mobile-First Responsive Layout & Font Scaling in CSS and Components

**Files:**
- Modify: `frontend/src/index.css`
- Modify: `frontend/src/pages/MatchingPage.jsx`
- Modify: `frontend/src/components/AiMatchingView.jsx`

- [ ] **Step 1: Add mobile responsive rules in `index.css`**
Add `@media (max-width: 600px)` rules for `.score-circle` (90px size, 22px font), `.verdict-badge` (13px font), and matching cards.

- [ ] **Step 2: Update `MatchingPage.jsx` to use localized keys**
Replace hardcoded options and messages in `MatchingPage.jsx` with `t(...)`.

- [ ] **Step 3: Update `AiMatchingView.jsx` to use localized keys and mobile minmax**
Replace hardcoded strings in `AiMatchingView.jsx` with `t(...)` and set grid minmax to `260px`.

- [ ] **Step 4: Verify frontend build**
Run `npm run build`.

- [ ] **Step 5: Commit frontend responsiveness & i18n binding**
```bash
git add frontend/src/index.css frontend/src/pages/MatchingPage.jsx frontend/src/components/AiMatchingView.jsx
git commit -m "feat(ui): implement mobile-first responsive matching layout and complete localized text bindings"
```

---

### Task 3: Guaranteed AI Marriage Compatibility in PDF Generation

**Files:**
- Modify: `src/main/java/org/vedic/astro/controller/MatchingController.java`
- Modify: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Normalize language and attach AI predictions in `MatchingController.java`**
In `downloadCompatibilityReport`:
1. Extract base language (e.g., `ta` from `ta,en-US;q=0.9`).
2. If `geminiProperties.isPdfPredictionsEnabled()` & `geminiProperties.isMatchingEnabled()`, call `geminiPredictionService.generateMarriageMatchingAiAnalysis(request, response, effectiveLang, false)` and attach `response.setAiMatchingPrediction(aiPred)`.

- [ ] **Step 2: Run automated tests**
Run `mvn test`.

- [ ] **Step 3: Commit backend PDF integration and push**
```bash
git add src/main/java/org/vedic/astro/controller/MatchingController.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "fix(pdf): ensure AI marriage compatibility is attached to PDF exports with normalized language"
git push origin feature/multi-panchangam-systems
```
