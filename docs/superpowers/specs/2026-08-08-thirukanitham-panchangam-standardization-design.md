# Design Specification: Thirukanitham Panchangam Standardization & Multi-Ayanamsa Support

## Overview
This specification details the transition of the Vedic Astrology application to standardize exclusively on **Thirukanitham (Drik)** as the sole Panchangam calculation engine, eliminating redundant engines (Vakya, Parasara Bhattar, Surya Siddhanta) while providing complete, unified support for user-selectable Ayanamsas across Daily Panchangam, Natal Horoscope charts, and Marriage Compatibility matching.

---

## 1. Requirements & User Intent
1. **Thirukanitham Exclusivity:** All astronomical calculations (planetary coordinates, bhavas, thithi, nakshatra, yogam, karanam, dasa-bhukthi, vasthu, shadbala, doshas, and ashta-koota/dasa-porutham compatibility) will run strictly on Swiss Ephemeris Thirukanitham / Drik algorithms.
2. **Engine Pruning:** Remove non-Thirukanitham engine classes (`VakyaPanchangamEngine`, `SuryaSiddhantaPanchangamEngine`, `ParasaraBhattarPanchangamEngine`) and their dedicated obsolete test files.
3. **Daily Panchangam Computation:** Remove legacy angle-offset shifts in `DailyPanchangamServiceImpl` so daily panchangam runs purely on Thirukanitham with the active user-configured Ayanamsa.
4. **UI Streamlining:** Remove the Panchangam System dropdown from all pages (Settings, Horoscope/BirthForm, and Marriage Matching).
5. **Ayanamsa Customization:** 
   - Allow users to set their default Ayanamsa in Settings.
   - Pre-populate and allow custom Ayanamsa selection in Horoscope generation and Marriage Matching forms.
   - Retain exact Jagannatha Hora (JHora) calibrated values for `PUSHYAPAKSHA` (22.72238333°) and `SURYA_SIDDHANTA` (22.50608611°) in `AyanamsaType.java`.

---

## 2. Architecture & Components

```
                      +-----------------------------+
                      |       User Interface        |
                      |  (Settings / Horoscope /    |
                      |   Matching / Panchangam)    |
                      +--------------+--------------+
                                     |
               Ayanamsa Selection:   |   Engine: Standardized on
               LAHIRI / KP / RAMAN / |   DRIK_TIRUKANITHAM
               SURYA_SIDDHANTA /     |
               PUSHYAPAKSHA          |
                                     v
                      +-----------------------------+
                      |     REST Controllers        |
                      |  - ChartController          |
                      |  - MatchingController       |
                      |  - PanchangamController     |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |   DrikPanchangamEngine /    |
                      |   DailyPanchangamService    |
                      +--------------+--------------+
                                     |
                                     v
                      +-----------------------------+
                      |       Swiss Ephemeris       |
                      |      (de.thmac.swisseph)    |
                      +-----------------------------+
```

### 2.1 Backend Architecture

#### A. Engine Pruning & Factory Simplification
* **Files to Delete:**
  * `src/main/java/org/vedic/astro/panchangam/impl/VakyaPanchangamEngine.java`
  * `src/main/java/org/vedic/astro/panchangam/impl/SuryaSiddhantaPanchangamEngine.java`
  * `src/main/java/org/vedic/astro/panchangam/impl/ParasaraBhattarPanchangamEngine.java`
  * `src/test/java/org/vedic/astro/VakyaStrictValidationTest.java`
  * `src/test/java/org/vedic/astro/VakyaDebugTest.java`
  * `src/test/java/org/vedic/astro/SuryaSiddhantaTest.java`
* **`PanchangamFactory.java`:**
  * Injects `DrikPanchangamEngine` as the canonical engine.
  * Resolves all requests to `DrikPanchangamEngine`.
* **`PanchangamType.java`:**
  * Retains `DRIK_TIRUKANITHAM` as the primary enum value (and preserves backwards-compatible helper mapping if needed).

#### B. `DailyPanchangamServiceImpl.java`
* Remove legacy conditional offset checks (`deltaOffset = -1.65`, `-1.83`, `-3.40`) and branch logic for `VAKYA`, `PARASARA_BHATTAR`, and `SURYA_SIDDHANTA`.
* Apply the selected `AyanamsaType` (default `LAHIRI`) directly to `SwissEph`.
* Compute sunrise, sunset, thithi, nakshatra, yogam, karanam, horais, gowri nalla neram, subha muhurtham, and vasthu timings purely with Swiss Ephemeris Thirukanitham calculations.

#### C. `AyanamsaType.java`
* Preserve exact JHora calibration constants:
  * `PUSHYAPAKSHA`: `22.72238333` (Reference Julian Epoch `2451545.0`)
  * `SURYA_SIDDHANTA`: `22.50608611` (Reference Julian Epoch `2451545.0`)
  * `LAHIRI`: `SweConst.SE_SIDM_LAHIRI`
  * `KP`: `SweConst.SE_SIDM_KRISHNAMURTI`
  * `RAMAN`: `SweConst.SE_SIDM_RAMAN`
* `applyTo(SwissEph swissEph)` applies the respective mode directly without requiring legacy engine type overrides.

#### D. Controllers & DTOs
* **`ChartController.java`:** Default `systemType` to `DRIK_TIRUKANITHAM`.
* **`MatchingController.java`:** Default `systemType` to `DRIK_TIRUKANITHAM`.
* **`PanchangamController.java`:** Accepts `PanchangamRequestDTO` with `ayanamsa`.

---

### 2.2 Frontend Architecture

#### A. Settings Page (`SettingsPage.jsx`)
* Remove the Panchangam System dropdown.
* Provide an Ayanamsa selection dropdown:
  * `Lahiri (Chitra Paksha) - Default`
  * `KP (Krishnamurti)`
  * `Raman (B.V. Raman)`
  * `Surya Siddhanta`
  * `Pushyapaksha`
* Store selection in `settings.ayanamsa` in `localStorage`.

#### B. Daily Panchangam (`PanchangamPage.jsx`)
* Automatically queries `/api/v1/astrology/panchangam` using `settings.ayanamsa` and `settings.location`.
* No panchangam system selector is displayed.

#### C. Horoscope Form (`BirthForm.jsx` & `HoroscopePage.jsx`)
* Remove the Panchangam System selector.
* Provide an Ayanamsa selector initialized from `settings.ayanamsa` (default: `LAHIRI`), allowing custom per-chart calculation.
* Submit payload with `panchangamSystem: 'DRIK_TIRUKANITHAM'` and chosen `ayanamsa`.

#### D. Marriage Matching (`MatchingPage.jsx`)
* Remove the Panchangam System selector from matching controls.
* Provide an Ayanamsa selector initialized from `settings.ayanamsa` (default: `LAHIRI`).
* Submit compatibility and PDF export requests with `systemType=DRIK_TIRUKANITHAM` and selected `ayanamsa`.

#### E. App State & Translations
* `DEFAULT_SETTINGS` in `App.jsx` set to `panchangamSystem: 'DRIK_TIRUKANITHAM'`, `ayanamsa: 'LAHIRI'`.
* Clean up any obsolete i18n keys for removed engine systems while retaining multilingual labels for Ayanamsa options across all supported languages (EN, TA, HI, KN, TE, ML).

---

## 3. Verification & Testing Plan

### 3.1 Automated Tests
* Execute `mvn clean test` to ensure:
  * `MultiPanchangamEngineTest` tests Thirukanitham across all Ayanamsas (Lahiri, Raman, KP, Surya Siddhanta, Pushyapaksha).
  * `MatchingEngineTest` passes compatibility checks using Thirukanitham.
  * All controller endpoints return HTTP 200 with valid JSON/PDF output.
* Execute `npm run build` in `frontend/` to confirm zero lint or compilation errors.

### 3.2 Manual Verification
1. **Settings:** Change Ayanamsa to Raman / KP / Surya Siddhanta / Pushyapaksha, reload, verify persistence.
2. **Daily Panchangam:** Verify planetary elements, gowri, and vasthu timings calculate accurately.
3. **Horoscope:** Generate chart, verify D1/D9 charts, dasa-bhukthi, doshas, and PDF download work without panchangam dropdown.
4. **Matching:** Perform Ashta Koota / Dasa Porutham match, verify report and PDF download work with chosen Ayanamsa.
