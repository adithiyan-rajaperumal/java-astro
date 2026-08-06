# Multi-Panchangam Engine Alignment and Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix SwissEph `calculateLocalSunrise` compilation errors and enforce strict ayanamsa scoping for Surya Siddhanta, Parasara Bhattar, and Vakya panchangam engines.

**Architecture:** Update traditional engine implementations (`VakyaPanchangamEngine`, `ParasaraBhattarPanchangamEngine`, `SuryaSiddhantaPanchangamEngine`) to use `de.thmac.swisseph.DblObj` for sunrise calculations, apply strict ayanamsa rules (`SURYA_SIDDHANTA` for Surya Siddhanta, `PUSHYAPAKSHA`/Plain for Parasara Bhattar), and track `VargaCalculationService.java` in git.

**Tech Stack:** Java 17, Spring Boot, Swiss Ephemeris (`swisseph`), JUnit 5.

## Global Constraints

- SwissEph method `swe_rise_trans` requires `de.thmac.swisseph.DblObj` for output parameter `tret`.
- `SuryaSiddhantaPanchangamEngine` must use `SURYA_SIDDHANTA` ayanamsa for offset calculations.
- `ParasaraBhattarPanchangamEngine` must support standard plain epoch (offset = 0.0) and `PUSHYAPAKSHA` (22°39'34.88").
- `VakyaPanchangamEngine` uses traditional Kalisuddhadinam (Ahargana) and Vararuchi 248 Chandra Vakyas / 12 Surya Vakyas.

---

### Task 1: Fix `calculateLocalSunrise` Compilation Error in All 3 Engines

**Files:**
- Modify: `src/main/java/org/vedic/astro/panchangam/impl/VakyaPanchangamEngine.java:239-251`
- Modify: `src/main/java/org/vedic/astro/panchangam/impl/ParasaraBhattarPanchangamEngine.java:238-250`
- Modify: `src/main/java/org/vedic/astro/panchangam/impl/SuryaSiddhantaPanchangamEngine.java:257-269`

**Interfaces:**
- Consumes: `SwissEph.swe_rise_trans(...)`
- Produces: `calculateLocalSunrise(double julianDayUT, double latitude, double longitude) -> double`

- [ ] **Step 1: Update `VakyaPanchangamEngine.java` `calculateLocalSunrise` method**

Replace `double[] ret = new double[2];` with `de.thmac.swisseph.DblObj tret = new de.thmac.swisseph.DblObj();`:
```java
    private double calculateLocalSunrise(double julianDayUT, double latitude, double longitude) {
        synchronized (swissEph) {
            de.thmac.swisseph.DblObj tret = new de.thmac.swisseph.DblObj();
            StringBuffer serr = new StringBuffer();

            int searchFlags = SweConst.SE_CALC_RISE | SweConst.SE_BIT_DISC_CENTER;
            int result = swissEph.swe_rise_trans(
                    julianDayUT, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH,
                    searchFlags, new double[] { longitude, latitude, 0.0 }, 0.0, 0.0, tret, serr);

            return (result == SweConst.OK) ? tret.val : (julianDayUT - 0.25);
        }
    }
```

- [ ] **Step 2: Update `ParasaraBhattarPanchangamEngine.java` `calculateLocalSunrise` method**

Apply the same `de.thmac.swisseph.DblObj tret` fix to `ParasaraBhattarPanchangamEngine.java`.

- [ ] **Step 3: Update `SuryaSiddhantaPanchangamEngine.java` `calculateLocalSunrise` method**

Apply the same `de.thmac.swisseph.DblObj tret` fix to `SuryaSiddhantaPanchangamEngine.java`.

- [ ] **Step 4: Verify compilation succeeds**

Run: `mvn compile`
Expected: BUILD SUCCESS with 0 compilation errors.

- [ ] **Step 5: Commit task changes**

```bash
git add src/main/java/org/vedic/astro/panchangam/impl/VakyaPanchangamEngine.java src/main/java/org/vedic/astro/panchangam/impl/ParasaraBhattarPanchangamEngine.java src/main/java/org/vedic/astro/panchangam/impl/SuryaSiddhantaPanchangamEngine.java
git commit -m "fix: resolve SwissEph swe_rise_trans DblObj compilation error across traditional engines"
```

---

### Task 2: Align Ayanamsa Scoping & Offsets for Parasara Bhattar and Surya Siddhanta

**Files:**
- Modify: `src/main/java/org/vedic/astro/panchangam/impl/ParasaraBhattarPanchangamEngine.java:186-206`
- Modify: `src/main/java/org/vedic/astro/panchangam/impl/SuryaSiddhantaPanchangamEngine.java:238-255`

**Interfaces:**
- Consumes: `dto.ayanamsa()`, `AyanamsaType.applyTo(swissEph, panchangamType)`
- Produces: `calculateAyanamsaOffset(double julianDayUT, String ayanamsaStr) -> double`

- [ ] **Step 1: Update `SuryaSiddhantaPanchangamEngine.java` `calculateAyanamsaOffset`**

Ensure `calculateAyanamsaOffset` always applies `AyanamsaType.SURYA_SIDDHANTA`:
```java
    private double calculateAyanamsaOffset(double julianDayUT, String ayanamsaStr) {
        if ("NONE".equalsIgnoreCase(ayanamsaStr) || "PLAIN".equalsIgnoreCase(ayanamsaStr)) {
            return 0.0; // Standard Siddhantic Zero Epoch
        }

        synchronized (swissEph) {
            AyanamsaType selectedAyanamsa = AyanamsaType.SURYA_SIDDHANTA;

            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
            double lahiriVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            selectedAyanamsa.applyTo(swissEph, PanchangamType.SURYA_SIDDHANTA);
            double targetAyanamsaVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            return lahiriVal - targetAyanamsaVal;
        }
    }
```

- [ ] **Step 2: Update `ParasaraBhattarPanchangamEngine.java` `calculateAyanamsaOffset`**

Support `PLAIN` / `NONE` (offset 0.0) and `PUSHYAPAKSHA` (reference value 22°39'34.88"):
```java
    private double calculateAyanamsaOffset(double julianDayUT, String ayanamsaStr) {
        if (ayanamsaStr == null || ayanamsaStr.isBlank() || "NONE".equalsIgnoreCase(ayanamsaStr)
                || "PLAIN".equalsIgnoreCase(ayanamsaStr) || "PARASARA_BHATTAR".equalsIgnoreCase(ayanamsaStr)) {
            return 0.0; // Plain Parasara Bhattar Mode (Sidereal Epoch Zero)
        }

        synchronized (swissEph) {
            AyanamsaType selectedAyanamsa = AyanamsaType.fromString(ayanamsaStr);
            if (selectedAyanamsa != AyanamsaType.PUSHYAPAKSHA) {
                selectedAyanamsa = AyanamsaType.PUSHYAPAKSHA;
            }

            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
            double lahiriVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            selectedAyanamsa.applyTo(swissEph, PanchangamType.PARASARA_BHATTAR);
            double targetAyanamsaVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            return lahiriVal - targetAyanamsaVal;
        }
    }
```

- [ ] **Step 3: Commit ayanamsa alignment changes**

```bash
git add src/main/java/org/vedic/astro/panchangam/impl/ParasaraBhattarPanchangamEngine.java src/main/java/org/vedic/astro/panchangam/impl/SuryaSiddhantaPanchangamEngine.java
git commit -m "feat: align ayanamsa scoping for Parasara Bhattar and Surya Siddhanta engines"
```

---

### Task 3: Track `VargaCalculationService` & Verify Full Build & Test Suite

**Files:**
- Add: `src/main/java/org/vedic/astro/service/impl/VargaCalculationService.java`

- [ ] **Step 1: Track `VargaCalculationService.java` in git**

```bash
git add src/main/java/org/vedic/astro/service/impl/VargaCalculationService.java
git commit -m "feat: track VargaCalculationService for traditional engine divisional chart calculations"
```

- [ ] **Step 2: Run full build and test suite**

Run: `mvn clean test`
Expected: BUILD SUCCESS with all tests passing.

---
