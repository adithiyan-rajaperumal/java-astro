# Implementation Plan - Life Anchors Accuracy, 3-Principle Ayurdaya Engine & Feature Flags

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the Life Anchors and Ayurdaya (Longevity) system with a 3-Principle Vedic determination engine (Jaimini 3-pair + Hora Lagna + Kakshya Vriddhi/Hrasa, Parashara/Shadbala Ayur Bala, and Maraka/Badhaka Timeline), add YAML configuration flags (`life-anchors-enabled`, `ayurdaya-enabled`), and ensure 100% 6-language synchronization (`en`, `ta`, `hi`, `te`, `kn`, `ml`) across UI and PDF reports.

**Architecture:** 
- `AyurdayaCalculationUtils` evaluates:
  1. Jaimini 3-Pair Modality with Hora Lagna and Kakshya rules.
  2. Parashara & Shadbala life-force (Ayur Bala) constitutional score.
  3. Maraka & Badhaka timeline and remedial advice.
- `application.yml` and `ChartOrchestrationService` provide `lifeAnchorsEnabled` and `ayurdayaEnabled` flags to UI DTOs and PDF export services.
- `LifeAnchorsLongevityView.jsx` renders a 3-principle view when enabled.
- All resource bundles and frontend translation dictionaries are synchronized across all 6 languages.

**Tech Stack:** Java 17, Spring Boot 3.3.4, React (Vite), Swiss Ephemeris / JSwissEph, JUnit 5.

---

## Global Constraints
- Target Java: 17.
- Target Node: 22+.
- Supported Languages: `en` (English), `ta` (Tamil), `hi` (Hindi), `te` (Telugu), `kn` (Kannada), `ml` (Malayalam).
- All resource bundle keys MUST match across all 6 `messages_*.properties` files.
- All frontend translation keys MUST match across all 6 language objects in `frontend/src/i18n/translations.js`.

---

### Task 1: 3-Principle Ayurdaya Engine & Clean Model in `AyurdayaCalculationUtils`

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java`
- Modify: `src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java`

- [ ] **Step 1: Write the updated Ayurdaya unit tests covering all 3 principles**
Add tests in `AyurdayaCalculationUtilsTest.java` verifying:
- Jaimini 3-pair consensus with Hora Lagna.
- Kakshya Vriddhi promotion (e.g. Jupiter in Kendra promotes Alpayu to Madhyayu or Madhyayu to Poornayu).
- Parashara Ayur Bala evaluation.
- Maraka/Badhaka timeline evaluation.

- [ ] **Step 2: Run tests to verify failure/missing methods**
Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest`

- [ ] **Step 3: Implement 3-Principle calculation and clean English defaults in `AyurdayaCalculationUtils.java`**
Implement the enhanced record:
```java
public record AyurdayaProfile(
    String longevityClassification,
    int estimatedLifespanCeiling,
    String lifespanRange,
    Map<String, Object> jaiminiThreePairs,
    Map<String, Object> parasharaAyurBala,
    Map<String, Object> marakaBadhakaTimeline,
    List<String> kakshyaAdjustments,
    String classicalRationale
) {}
```
Calculate Hora Lagna from Sunrise and birth time in `AyurdayaCalculationUtils.calculateAyurdaya(...)`.

- [ ] **Step 4: Run tests to verify pass**
Run: `mvn test -Dtest=AyurdayaCalculationUtilsTest`

- [ ] **Step 5: Commit Task 1**
```bash
git add src/main/java/org/vedic/astro/util/AyurdayaCalculationUtils.java src/test/java/org/vedic/astro/AyurdayaCalculationUtilsTest.java
git commit -m "feat(ayurdaya): implement 3-principle longevity engine with Jaimini, Parashara, and Maraka timelines"
```

---

### Task 2: Clean Default Models in `NumerologyUtils` & `StructuralAnchorsUtils`

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/NumerologyUtils.java`
- Modify: `src/main/java/org/vedic/astro/util/StructuralAnchorsUtils.java`
- Modify: `src/main/java/org/vedic/astro/util/SpiritualDeityUtils.java`

- [ ] **Step 1: Replace hardcoded Tamil strings with clean English defaults in `NumerologyUtils.java`**
Update `conflictNotes` and `caution` in `NumerologyUtils.java` to use clean English defaults.

- [ ] **Step 2: Replace hardcoded Tamil strings with clean English defaults in `StructuralAnchorsUtils.java`**
Update `vitalityStatus`, `arudhaLagnaText`, `mindResilience`, `karmaAnchor`, `LuckyDayResult`, `getElementDirection`, and `getPlanetaryDigbalaDirection` to use clean English defaults.

- [ ] **Step 3: Run backend test suite to ensure no regression**
Run: `mvn test -Dtest=LifeAnchorsProfileTest,NumerologyUtilsTest,SpiritualDeityUtilsTest`

- [ ] **Step 4: Commit Task 2**
```bash
git add src/main/java/org/vedic/astro/util/NumerologyUtils.java src/main/java/org/vedic/astro/util/StructuralAnchorsUtils.java src/main/java/org/vedic/astro/util/SpiritualDeityUtils.java
git commit -m "refactor(anchors): remove hardcoded Tamil default strings in Numerology and Structural Anchors utils"
```

---

### Task 3: Feature Toggles in `application.yml`, DTOs, and `ChartOrchestrationService`

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`

- [ ] **Step 1: Add configuration properties in `application.yml`**
```yaml
astro:
  features:
    life-anchors-enabled: ${FEATURE_LIFE_ANCHORS_ENABLED:true}
    ayurdaya-enabled: ${FEATURE_AYURDAYA_ENABLED:true}

pdf:
  include-life-anchors: ${PDF_INCLUDE_LIFE_ANCHORS:true}
  include-ayurdaya: ${PDF_INCLUDE_AYURDAYA:true}
  include-yogas-doshams: ${PDF_INCLUDE_YOGAS_DOSHAMS:false}
```

- [ ] **Step 2: Update `ChartUiResponseDTO` and `ChartOrchestrationService`**
Add fields `boolean lifeAnchorsEnabled` and `boolean ayurdayaEnabled` to `ChartUiResponseDTO`.
In `ChartOrchestrationService`, read the properties and conditionally compute `lifeAnchorsProfile` and `ayurdayaProfile`.

- [ ] **Step 3: Update `PdfExportService` to respect `pdf.include-ayurdaya`**
Conditionally render the Ayurdaya section in PDF export based on `pdf.include-ayurdaya`.

- [ ] **Step 4: Run test suite**
Run: `mvn test`

- [ ] **Step 5: Commit Task 3**
```bash
git add src/main/resources/application.yml src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java src/main/java/org/vedic/astro/service/ChartOrchestrationService.java src/main/java/org/vedic/astro/service/PdfExportService.java
git commit -m "feat(config): add YAML feature flags for life-anchors-enabled and ayurdaya-enabled across UI and PDF"
```

---

### Task 4: 6-Language ResourceBundle and Frontend Translations Synchronization

**Files:**
- Modify: `src/main/resources/i18n/messages_en.properties`
- Modify: `src/main/resources/i18n/messages_ta.properties`
- Modify: `src/main/resources/i18n/messages_hi.properties`
- Modify: `src/main/resources/i18n/messages_te.properties`
- Modify: `src/main/resources/i18n/messages_kn.properties`
- Modify: `src/main/resources/i18n/messages_ml.properties`
- Modify: `src/main/resources/i18n/messages.properties`
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Add new 3-Principle Ayurdaya keys across all 6 backend property files**
Keys to add:
- `ayurdaya.principle1.title`, `ayurdaya.principle1.desc` (Jaimini 3-Pair)
- `ayurdaya.principle2.title`, `ayurdaya.principle2.desc` (Parashara & Shadbala Ayur Bala)
- `ayurdaya.principle3.title`, `ayurdaya.principle3.desc` (Maraka & Badhaka Timeline)
- `ayurdaya.kakshya.vriddhi`, `ayurdaya.kakshya.hrasa`
- `ayurdaya.span.poornayu`, `ayurdaya.span.madhyayu`, `ayurdaya.span.alpayu`

- [ ] **Step 2: Add matching keys to `frontend/src/i18n/translations.js`**
Ensure identical keys are populated in all 6 language objects (`en`, `ta`, `hi`, `te`, `kn`, `ml`).

- [ ] **Step 3: Run node verification script to confirm 0 missing keys**
Verify 100% key parity across backend and frontend.

- [ ] **Step 4: Commit Task 4**
```bash
git add src/main/resources/i18n/messages_*.properties src/main/resources/i18n/messages.properties frontend/src/i18n/translations.js
git commit -m "feat(i18n): synchronize 3-principle Ayurdaya localization keys across all 6 languages"
```

---

### Task 5: Frontend UI Conditional Sub-Tab & 3-Principle View

**Files:**
- Modify: `frontend/src/pages/HoroscopePage.jsx`
- Modify: `frontend/src/components/LifeAnchorsLongevityView.jsx`

- [ ] **Step 1: Update `HoroscopePage.jsx` to conditionally render Life Anchors tab**
Check `report?.lifeAnchorsEnabled !== false` before rendering the `healthAndLongevityTab` button and view.

- [ ] **Step 2: Update `LifeAnchorsLongevityView.jsx` to conditionally render Ayurdaya 3-principle section**
Check `chartData?.ayurdayaEnabled !== false && chartData?.ayurdayaProfile` before rendering Section 5.
Render the 3 principles cleanly:
- Principle 1: Jaimini 3-Pair Modality Table & Consensus with Kakshya badges.
- Principle 2: Parashara & Shadbala Life-Force (Ayur Bala) card with Lagna Lord and Kendra benefic scores.
- Principle 3: Maraka & Badhaka Timeline & Remedial Card.

- [ ] **Step 3: Test frontend build**
Run: `npm run build --prefix frontend`
Expected: Build succeeds with 0 errors.

- [ ] **Step 4: Commit Task 5**
```bash
git add frontend/src/pages/HoroscopePage.jsx frontend/src/components/LifeAnchorsLongevityView.jsx
git commit -m "feat(ui): conditionally render Life Anchors sub-tab and 3-principle Ayurdaya longevity view"
```

---

### Task 6: Comprehensive End-to-End Verification

- [ ] **Step 1: Run full backend test suite**
Run: `mvn test`
Expected: 100% passing tests (69+ tests passing, 0 failures, 0 errors).

- [ ] **Step 2: Run frontend production build**
Run: `npm run build --prefix frontend`
Expected: Production build succeeds in ~200ms.

- [ ] **Step 3: Run final git status & commit**
Ensure all changes are clean and pushed to `feature/multi-panchangam-systems`.
