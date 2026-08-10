# Comprehensive Astrology UX & Prediction Engine Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement comprehensive fixes for Dasa-Bhukthi active period highlighting, Shadbala UI, 3-tier Diagnostics Dosha statuses, Full Lifespan AI Balan depth & domain pill tags, 100% native multilingual translations, and PDF lifetime forecast export.

**Architecture:** 
- Frontend: Add synchronized i18n keys across all 6 languages in `translations.js`, enhance `HoroscopePage.jsx` (Dasa timeline date parsing/highlighting, Shadbala visual indicators, Dosha 3-state rendering, header Ayanamsa/Panchangam fix), and enhance `AiPredictionsView.jsx` with domain pill tags.
- Backend: Update `GeminiPredictionService.java` prompt instructions for full lifetime horizon (age 85-90+/100) and deep multidimensional synthesis; update `PdfExportService.java` to render rich lifetime tables.

**Tech Stack:** Java 17, Spring Boot 3, iText PDF, React 19, Vite, Vanilla CSS, JUnit 5.

---

### Task 1: Complete Multilingual i18n Key Synchronization across All 6 Languages

**Files:**
- Modify: `frontend/src/i18n/translations.js`

**Interfaces:**
- Produces: Keys `keyStrengths`, `karmicLessons`, `healthAnalysisTitle`, `ayurvedicConstitution`, `longevityVitality`, `organVulnerabilities`, `recommendedDietLifestyle`, `classicalYogasTitle`, `doshamsAnalysisTitle`, `pastTurningPointsTitle`, `lifetimeForecastTitle`, `phaseLabel`, `noDosham`, `currentActiveDasa`, and clean `panchangamThirukanitham: "திருக்கணிதம்"`.

- [ ] **Step 1: Update `translations.js` with all localized title keys across `en`, `ta`, `hi`, `kn`, `te`, `ml`**
- [ ] **Step 2: Verify frontend compilation with `npm run build` in `frontend/`**
- [ ] **Step 3: Commit changes**
```bash
git add frontend/src/i18n/translations.js
git commit -m "feat(i18n): synchronize all AI Balan titles and localized keys across 6 languages"
```

---

### Task 2: Dasa-Bhukthi UI Active Period Highlighting, Auto-Expansion & Shadbala Polish

**Files:**
- Modify: `frontend/src/pages/HoroscopePage.jsx:380-560`

**Interfaces:**
- Consumes: `report.currentDasaTimeline`, `report.shadbalaStrengths`, `t(key, lang)`
- Produces: Auto-expanded active Mahadasa card, gold border & `✨ Current Dasa` badge, active Bhukthi row highlight with `⭐`, and relative strength indicators in Shadbala table.

- [ ] **Step 1: Implement date range helper and active Mahadasa/Bhukthi finder in `HoroscopePage.jsx`**
- [ ] **Step 2: Auto-set `expandedDasa` on data load and apply active gold card styling**
- [ ] **Step 3: Enhance `renderShadbalaTab` with relative strength color tiers**
- [ ] **Step 4: Verify frontend build with `npm run build`**
- [ ] **Step 5: Commit changes**
```bash
git add frontend/src/pages/HoroscopePage.jsx
git commit -m "feat(ui): add active Dasa-Bhukthi highlights, auto-expansion, and Shadbala polish"
```

---

### Task 3: Diagnostics UI 3-Tier Status & Header Ayanamsa / Panchangam Label Fix

**Files:**
- Modify: `frontend/src/pages/HoroscopePage.jsx:600-750`

**Interfaces:**
- Consumes: `report.structuralDiagnostics.discoveredDoshams`, `getAyanamsaLabel`, `getPanchangamSystemLabel`
- Produces: 3-tier status badges for Doshams (🟢 Not Present, 🛡️ Cancelled, ⚠️ Active), and accurate Ayanamsa/Panchangam header display.

- [ ] **Step 1: Remove shadowed `getAyanamsaLabel` function at line 642 of `HoroscopePage.jsx`**
- [ ] **Step 2: Refactor `renderDiagnosticsTab` to support 3 distinct Dosha states (`!d.detected`, `d.detected && d.nullified`, `d.detected && !d.nullified`)**
- [ ] **Step 3: Verify header displays `அயனாம்சம்: லஹிரி (சித்திர பக்ஷம்) | பஞ்சாங்கக் கணிதம்: திருக்கணிதம்`**
- [ ] **Step 4: Verify frontend build with `npm run build`**
- [ ] **Step 5: Commit changes**
```bash
git add frontend/src/pages/HoroscopePage.jsx
git commit -m "fix(ui): implement 3-tier Dosha status and fix Ayanamsa/Panchangam header display"
```

---

### Task 4: AI Balan UI Domain Pill Tags & Clean Component Refactoring

**Files:**
- Modify: `frontend/src/components/AiPredictionsView.jsx`

**Interfaces:**
- Consumes: `predictions.lifetimePredictions`, `t(key, lang)`
- Produces: Domain pill tags (`💼 Career & Wealth`, `🌿 Health & Vitality`, `👨‍👩‍👦 Family & Relationships`, `⚠️ Cautions & Remedies`) inside year cards, rendering 100% synchronized native translations.

- [ ] **Step 1: Add domain pill tags in `AiPredictionsView.jsx` year cards**
- [ ] **Step 2: Replace any hardcoded or desynchronized translation keys with standardized `t(...)` calls**
- [ ] **Step 3: Verify frontend build with `npm run build`**
- [ ] **Step 4: Commit changes**
```bash
git add frontend/src/components/AiPredictionsView.jsx
git commit -m "feat(ui): add domain tags to AI Balan year cards and use synchronized i18n keys"
```

---

### Task 5: Gemini Full Lifespan Prompt Horizon & Deep Multidimensional Synthesis

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Modify: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- Produces: Comprehensive prompt directive requesting full lifespan coverage (current age through 85-90+/100) and deep multidimensional narrative paragraphs covering career, wealth, health, marriage, kids, and parents.

- [ ] **Step 1: Update `GeminiPredictionService.java` directive #6 and prompt construction**
- [ ] **Step 2: Update `GeminiPredictionServiceTest.java` unit tests**
- [ ] **Step 3: Run backend unit tests with `mvn test -Dtest=GeminiPredictionServiceTest`**
- [ ] **Step 4: Commit changes**
```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(ai): extend Gemini prediction horizon to full lifespan with multidimensional depth"
```

---

### Task 6: PDF Export Service Year-Wise Lifetime Table & Pagination

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`

**Interfaces:**
- Consumes: `data.getAiPredictions().getFuturePredictions()`
- Produces: Formatted PDF multi-page table embedding `year/age`, `dasaBhukthi & astrologicalBasis`, `yearlyTheme & detailedPrediction`, and `cautionsAndRemedies`.

- [ ] **Step 1: Refactor future predictions table generator in `PdfExportService.java` to print `detailedPrediction`, `yearlyTheme`, `astrologicalBasis`, and `cautionsAndRemedies`**
- [ ] **Step 2: Run full backend test suite with `mvn clean test`**
- [ ] **Step 3: Run full frontend build with `npm run build`**
- [ ] **Step 4: Commit changes and push to `origin/feature/multi-panchangam-systems`**
```bash
git add src/main/java/org/vedic/astro/service/PdfExportService.java
git commit -m "feat(pdf): format full detailed lifetime predictions table in PDF reports"
git push origin feature/multi-panchangam-systems
```
