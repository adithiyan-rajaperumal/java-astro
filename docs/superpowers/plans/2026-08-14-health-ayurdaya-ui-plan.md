# Implementation Plan: Deterministic Health & Longevity (Ayurdaya) UI View

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Expose the engine's classical Ayurvedic Health Profile (Prakriti, Vata/Pitta/Kapha balance, 6th house Roga Sthana, organ vulnerabilities, dietary guidelines) and Parashara-Jaimini Ayurdaya Longevity Profile (3-Pair evaluation, Kakshya adjustments, Longevity classification, Lifespan range) as a dedicated, beautiful UI tab in `HoroscopePage.jsx`.

**Architecture:**
- **Backend**: Expose `ayurvedicHealth` (`AyurvedicHealthProfile`) and `ayurdayaProfile` (`AyurdayaProfile`) in `ChartUiResponseDTO.java` and populate them in `ChartOrchestrationService.java`.
- **Frontend**: Create a rich, dedicated component `HealthLongevityView.jsx` with visual Dosha meters, Ayurdaya hero badge, Jaimini 3-pair table, Kakshya factor list, organ vulnerability chips, and Ayurvedic dietary directives. Add multi-lingual translations in `translations.js` across all 6 languages (EN, TA, HI, KN, TE, ML) and mount the view as a tab in `HoroscopePage.jsx`.

**Tech Stack:** Java 17, Spring Boot 3.3.4, React 19, Vite, CSS Modules.

---

### Task 1: Expose Health & Ayurdaya Profiles in Backend DTOs & Orchestration Service

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`
- Test: `src/test/java/org/vedic/astro/ChartOrchestrationServiceTest.java` (or equivalent test)

- [ ] **Step 1: Update DTOs**
Add `ayurvedicHealth` and `ayurdayaProfile` fields to `ChartUiResponseDTO.java` and `ComprehensiveReportDTO.java`.

- [ ] **Step 2: Populate in `ChartOrchestrationService.java`**
In `ChartOrchestrationService.java`, compute `AyurvedicAstrologyUtils.calculateHealthProfile(...)` and `AyurdayaCalculationUtils.calculateAyurdaya(...)` and attach them to the response DTO.

- [ ] **Step 3: Verify with unit tests**
Run `mvn test "-Dtest=ChartOrchestrationServiceTest"` or controller tests.
Expected: PASS.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java src/main/java/org/vedic/astro/service/ChartOrchestrationService.java
git commit -m "feat(api): expose calculated ayurvedicHealth and ayurdayaProfile in ChartUiResponseDTO"
```

---

### Task 2: Multi-Lingual Translations & Dedicated `HealthLongevityView.jsx`

**Files:**
- Modify: `frontend/src/i18n/translations.js`
- Create: `frontend/src/components/HealthLongevityView.jsx`
- Modify: `frontend/src/pages/HoroscopePage.jsx`

- [ ] **Step 1: Add Translations in `translations.js`**
Add keys for `healthAndLongevityTab`, `prakritiTitle`, `longevityTitle`, `threePairsTitle`, `organVulnerabilitiesTitle`, `dietLifestyleTitle`, `vata`, `pitta`, `kapha`, `poornayu`, `madhyayu`, `alpayu`, `longevityCeiling`, `marakaPeriods`, `kakshyaAdjustments` across all 6 languages (EN, TA, HI, KN, TE, ML).

- [ ] **Step 2: Create `HealthLongevityView.jsx`**
Build the component displaying:
1. **Ayurdaya Longevity Hero Card**: Classification Badge (`Poornayu`, `Madhyayu`, `Alpayu`), Lifespan Range & Ceiling, Jaimini 3-Pair Table with Modalities, Kakshya Vriddhi/Harana adjustments, and Maraka window.
2. **Ayurvedic Prakriti & Constitution Card**: Interactive colored percentage bars for Vata (35%), Pitta (45%), Kapha (20%), Dominant Prakriti badge, Lagna Element, and 6th House Roga Lord details.
3. **Organ Vulnerabilities & Dietary Guidelines Card**: Vulnerable organ chips and actionable Ayurvedic dietary/lifestyle directives.

- [ ] **Step 3: Integrate Tab into `HoroscopePage.jsx`**
Add the `🌿 Health & Longevity` tab button and render `HealthLongevityView.jsx` when selected.

- [ ] **Step 4: Build Frontend**
Run `npm run build` in `frontend/` to verify zero build errors.

- [ ] **Step 5: Commit**
```bash
git add frontend/src/i18n/translations.js frontend/src/components/HealthLongevityView.jsx frontend/src/pages/HoroscopePage.jsx
git commit -m "feat(ui): add dedicated Health & Longevity (Ayurdaya) tab with interactive Dosha meters and Jaimini 3-pair analysis"
```

---

### Task 3: Full End-to-End Test Suite Verification & Remote Push

**Files:**
- Test: `mvn test`

- [ ] **Step 1: Run full Maven test suite**
Run `mvn test`.
Expected: 55+ tests pass (`BUILD SUCCESS`).

- [ ] **Step 2: Push commits to remote origin**
```bash
git push origin feature/multi-panchangam-systems
```
