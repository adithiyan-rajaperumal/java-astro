# Thirukanitham Panchangam Standardization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Standardize the Vedic Astrology application exclusively on Thirukanitham (Drik) as the sole Panchangam calculation engine, prune obsolete engine classes and test suites, remove Panchangam dropdowns across all UI pages, and enable global & per-feature user selection of Ayanamsas.

**Architecture:** Swiss Ephemeris Thirukanitham (`DrikPanchangamEngine`) acts as the single calculation engine across daily panchangam, natal charts, and marriage matching. All manual angle offset shifts are eliminated from `DailyPanchangamServiceImpl`. Settings, Horoscope, and Matching UIs allow users to select from standard Ayanamsas (`LAHIRI`, `KP`, `RAMAN`, `SURYA_SIDDHANTA`, `PUSHYAPAKSHA`), while retaining exact Jagannatha Hora (JHora) calibration values for Pushyapaksha and Surya Siddhanta in `AyanamsaType.java`.

**Tech Stack:** Java 21, Spring Boot 3, Swiss Ephemeris (`de.thmac.swisseph`), React 18, Vite.

## Global Constraints
- Target engine is strictly Thirukanitham (`DRIK_TIRUKANITHAM`).
- `AyanamsaType.java` must preserve exact JHora values for Pushyapaksha (`22.72238333` / `2451545.0`) and Surya Siddhanta (`22.50608611` / `2451545.0`).
- No Panchangam System dropdowns on Settings, Horoscope, or Matching pages.
- Ayanamsa selection must be accessible in Settings, Horoscope, and Matching pages.

---

### Task 1: Prune Obsolete Non-Thirukanitham Engine Classes and Tests

**Files:**
- Delete: `src/main/java/org/vedic/astro/panchangam/impl/VakyaPanchangamEngine.java`
- Delete: `src/main/java/org/vedic/astro/panchangam/impl/SuryaSiddhantaPanchangamEngine.java`
- Delete: `src/main/java/org/vedic/astro/panchangam/impl/ParasaraBhattarPanchangamEngine.java`
- Delete: `src/test/java/org/vedic/astro/VakyaStrictValidationTest.java`
- Delete: `src/test/java/org/vedic/astro/VakyaDebugTest.java`
- Delete: `src/test/java/org/vedic/astro/SuryaSiddhantaTest.java`

- [ ] **Step 1: Delete obsolete engine implementation files and tests**

Run:
```powershell
Remove-Item -Path "d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\panchangam\impl\VakyaPanchangamEngine.java" -Force;
Remove-Item -Path "d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\panchangam\impl\SuryaSiddhantaPanchangamEngine.java" -Force;
Remove-Item -Path "d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\panchangam\impl\ParasaraBhattarPanchangamEngine.java" -Force;
Remove-Item -Path "d:\Intellij_WS\java-astro\src\test\java\org\vedic\astro\VakyaStrictValidationTest.java" -Force;
Remove-Item -Path "d:\Intellij_WS\java-astro\src\test\java\org\vedic\astro\VakyaDebugTest.java" -Force;
Remove-Item -Path "d:\Intellij_WS\java-astro\src\test\java\org\vedic\astro\SuryaSiddhantaTest.java" -Force;
```

- [ ] **Step 2: Verify deleted files are removed from directory**

Run:
```powershell
Get-ChildItem -Path "d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\panchangam\impl"
```
Expected: Only `DrikPanchangamEngine.java` remains.

- [ ] **Step 3: Commit file deletions**

```powershell
git add -u; git commit -m "refactor: delete obsolete vakya, surya siddhanta, and parasara bhattar engine classes and tests"
```

---

### Task 2: Simplify Backend Factory and Daily Panchangam Calculations

**Files:**
- Modify: `src/main/java/org/vedic/astro/panchangam/PanchangamFactory.java`
- Modify: `src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java`
- Modify: `src/main/java/org/vedic/astro/model/AyanamsaType.java`
- Modify: `src/test/java/org/vedic/astro/MultiPanchangamEngineTest.java`

- [ ] **Step 1: Simplify `PanchangamFactory.java` to inject `DrikPanchangamEngine`**

Update `PanchangamFactory.java`:
```java
package org.vedic.astro.panchangam;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vedic.astro.panchangam.impl.DrikPanchangamEngine;

@Component
@RequiredArgsConstructor
public class PanchangamFactory {

    private final DrikPanchangamEngine drikPanchangamEngine;

    public PanchangamEngine getEngine(PanchangamType type) {
        return drikPanchangamEngine;
    }
}
```

- [ ] **Step 2: Clean `DailyPanchangamServiceImpl.java` of legacy delta offsets**

In `DailyPanchangamServiceImpl.java`:
- Remove `deltaOffset = -1.65`, `-1.83`, `-3.40` branches from `getSunMoonLongitude`.
- Standardize on `ayanamsaType.applyTo(swissEph)` directly.
- Ensure `calculateDailyPanchangam` uses `PanchangamType.DRIK_TIRUKANITHAM` exclusively.

- [ ] **Step 3: Verify `AyanamsaType.java` preserves exact JHora values**

Verify `AyanamsaType.java`:
```java
package org.vedic.astro.model;

import de.thmac.swisseph.SweConst;
import de.thmac.swisseph.SwissEph;

public enum AyanamsaType {
    LAHIRI(SweConst.SE_SIDM_LAHIRI),
    RAMAN(SweConst.SE_SIDM_RAMAN),
    KP(SweConst.SE_SIDM_KRISHNAMURTI),
    SURYA_SIDDHANTA(21),
    PUSHYAPAKSHA(SweConst.SE_SIDM_USER);

    private final int mode;

    AyanamsaType(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void applyTo(SwissEph swissEph) {
        applyTo(swissEph, org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
    }

    public void applyTo(SwissEph swissEph, org.vedic.astro.panchangam.PanchangamType pType) {
        if (this == PUSHYAPAKSHA) {
            // Standard True Pushyapaksha (JHora match 22°39'36.55" / 22-39-34.95)
            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_USER, 2451545.0, 22.72238333);
        } else if (this == SURYA_SIDDHANTA) {
            // Surya Siddhanta Ayanamsa (JHora exact match 22°26'37.88")
            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_USER, 2451545.0, 22.50608611);
        } else {
            swissEph.swe_set_sid_mode(this.mode, 0, 0);
        }
    }

    public static AyanamsaType fromString(String val) {
        if (val == null || val.trim().isEmpty()) {
            return LAHIRI;
        }
        String clean = val.trim().toUpperCase().replace("-", "_").replace(" ", "_").replace(".", "");
        if (clean.contains("SURYA") || clean.contains("SIDDHANT")) {
            return SURYA_SIDDHANTA;
        }
        if (clean.contains("PUSHYA")) {
            return PUSHYAPAKSHA;
        }
        if (clean.contains("KP") || clean.contains("KRISHNAMURTI")) {
            return KP;
        }
        if (clean.contains("RAMAN") || clean.contains("BV")) {
            return RAMAN;
        }
        try {
            return AyanamsaType.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return LAHIRI;
        }
    }
}
```

- [ ] **Step 4: Update `MultiPanchangamEngineTest.java` to test Thirukanitham with all Ayanamsas**

Rewrite `MultiPanchangamEngineTest.java` to validate `DrikPanchangamEngine` across `LAHIRI`, `KP`, `RAMAN`, `SURYA_SIDDHANTA`, and `PUSHYAPAKSHA`.

- [ ] **Step 5: Run tests to verify backend compilation and pass**

Run:
```powershell
mvn test -Dtest=MultiPanchangamEngineTest
```
Expected: Tests pass.

- [ ] **Step 6: Commit backend service changes**

```powershell
git add src/main/java/org/vedic/astro/panchangam/PanchangamFactory.java src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java src/main/java/org/vedic/astro/model/AyanamsaType.java src/test/java/org/vedic/astro/MultiPanchangamEngineTest.java; git commit -m "refactor: standardize panchangam factory and daily panchangam service on thirukanitham"
```

---

### Task 3: Refactor REST Controllers for Thirukanitham Default

**Files:**
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java`
- Modify: `src/main/java/org/vedic/astro/controller/MatchingController.java`
- Modify: `src/main/java/org/vedic/astro/controller/PanchangamController.java`
- Modify: `src/test/java/org/vedic/astro/MatchingEngineTest.java`

- [ ] **Step 1: Update `ChartController.java` and `MatchingController.java`**
Ensure endpoints default `systemType` to `DRIK_TIRUKANITHAM` and pass birth details containing user-selected `ayanamsa` to calculation engines.

- [ ] **Step 2: Verify `MatchingEngineTest.java`**
Run:
```powershell
mvn test -Dtest=MatchingEngineTest
```
Expected: Tests pass.

- [ ] **Step 3: Commit controller updates**

```powershell
git add src/main/java/org/vedic/astro/controller/ChartController.java src/main/java/org/vedic/astro/controller/MatchingController.java src/main/java/org/vedic/astro/controller/PanchangamController.java src/test/java/org/vedic/astro/MatchingEngineTest.java; git commit -m "refactor: standardize controllers on drik tirukanitham engine"
```

---

### Task 4: Update Settings and Daily Panchangam Frontend Pages

**Files:**
- Modify: `frontend/src/pages/SettingsPage.jsx`
- Modify: `frontend/src/pages/PanchangamPage.jsx`
- Modify: `frontend/src/App.jsx`

- [ ] **Step 1: Update `SettingsPage.jsx`**
- Remove Panchangam System dropdown.
- Provide clean Ayanamsa selector:
  - `LAHIRI`: "Lahiri (Chitra Paksha) - Default"
  - `KP`: "KP (Krishnamurti)"
  - `RAMAN`: "Raman (B.V. Raman)"
  - `SURYA_SIDDHANTA`: "Surya Siddhanta"
  - `PUSHYAPAKSHA`: "Pushyapaksha"
- Save change via `onSettingsChange({ ...settings, ayanamsa: e.target.value })`.

- [ ] **Step 2: Update `PanchangamPage.jsx`**
- Fetch panchangam using `settings.ayanamsa || 'LAHIRI'` and `settings.location`.
- Do not show any panchangam system dropdown.

- [ ] **Step 3: Update `App.jsx`**
- Set `DEFAULT_SETTINGS` with `ayanamsa: 'LAHIRI'` and `panchangamSystem: 'DRIK_TIRUKANITHAM'`.

- [ ] **Step 4: Commit settings and panchangam page changes**

```powershell
git add frontend/src/pages/SettingsPage.jsx frontend/src/pages/PanchangamPage.jsx frontend/src/App.jsx; git commit -m "feat(ui): remove panchangam dropdown and streamline ayanamsa in settings and daily panchangam"
```

---

### Task 5: Update Horoscope and Marriage Matching Frontend Pages

**Files:**
- Modify: `frontend/src/components/BirthForm.jsx`
- Modify: `frontend/src/pages/HoroscopePage.jsx`
- Modify: `frontend/src/pages/MatchingPage.jsx`

- [ ] **Step 1: Update `BirthForm.jsx`**
- Remove the `panchangamSystem` dropdown.
- Display an `ayanamsa` dropdown pre-filled with `settings.ayanamsa || 'LAHIRI'`:
  - `LAHIRI`: "Lahiri (Chitra Paksha) - Default"
  - `KP`: "KP (Krishnamurti)"
  - `RAMAN`: "Raman (B.V. Raman)"
  - `SURYA_SIDDHANTA`: "Surya Siddhanta"
  - `PUSHYAPAKSHA`: "Pushyapaksha"
- Pass `ayanamsa` and `panchangamSystem: 'DRIK_TIRUKANITHAM'` on submit.

- [ ] **Step 2: Update `HoroscopePage.jsx`**
- Update API query string and PDF export calls to use `systemType=DRIK_TIRUKANITHAM`.

- [ ] **Step 3: Update `MatchingPage.jsx`**
- Remove the `panchangamSystem` dropdown from the matching options card.
- Provide the `ayanamsa` dropdown initialized from `settings.ayanamsa || 'LAHIRI'`.
- Submit compatibility requests and PDF download requests with `systemType=DRIK_TIRUKANITHAM` and the selected `ayanamsa`.

- [ ] **Step 4: Commit horoscope and matching page changes**

```powershell
git add frontend/src/components/BirthForm.jsx frontend/src/pages/HoroscopePage.jsx frontend/src/pages/MatchingPage.jsx; git commit -m "feat(ui): remove panchangam dropdown and enable ayanamsa selection in horoscope and matching"
```

---

### Task 6: Full Verification & Build Validation

**Files:**
- Project-wide verification

- [ ] **Step 1: Run complete Maven test suite**
```powershell
mvn clean test
```
Expected: `BUILD SUCCESS` with 0 failures, 0 errors.

- [ ] **Step 2: Run frontend build validation**
```powershell
cd frontend; npm run build; cd ..
```
Expected: Successful bundle generation with 0 build errors.

- [ ] **Step 3: Final Git status check and commit if any residual adjustments**
```powershell
git status
```
