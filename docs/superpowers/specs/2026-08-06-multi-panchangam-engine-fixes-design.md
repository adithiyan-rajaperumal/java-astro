# Multi-Panchangam Engine Alignment & Fixes Design Spec

## Overview
This specification details the compilation fixes and strict Ayanamsa scoping for the authentic traditional Panchangam engines (`VakyaPanchangamEngine`, `ParasaraBhattarPanchangamEngine`, and `SuryaSiddhantaPanchangamEngine`) within `java-astro`.

---

## 1. Bug Fix: SwissEph `calculateLocalSunrise` Type Mismatch

In all three traditional engines (`VakyaPanchangamEngine`, `ParasaraBhattarPanchangamEngine`, and `SuryaSiddhantaPanchangamEngine`), `calculateLocalSunrise` previously defined a `double[]` output parameter for `swissEph.swe_rise_trans`, which caused `javac` compilation failures.

### Solution:
Replace `double[] ret = new double[2];` with `de.thmac.swisseph.DblObj tret = new de.thmac.swisseph.DblObj();` across all three classes:
```java
synchronized (swissEph) {
    de.thmac.swisseph.DblObj tret = new de.thmac.swisseph.DblObj();
    StringBuffer serr = new StringBuffer();

    int searchFlags = SweConst.SE_CALC_RISE | SweConst.SE_BIT_DISC_CENTER;
    int result = swissEph.swe_rise_trans(
            julianDayUT, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH,
            searchFlags, new double[] { longitude, latitude, 0.0 }, 0.0, 0.0, tret, serr);

    return (result == SweConst.OK) ? tret.val : (julianDayUT - 0.25);
}
```

---

## 2. Strict Panchangam & Ayanamsa Scoping Rules

| Panchangam System | Supported Ayanamsa Options | Calculation & Alignment Strategy |
| :--- | :--- | :--- |
| **Surya Siddhanta** | **`SURYA_SIDDHANTA` Only** | Forces `SURYA_SIDDHANTA` ayanamsa calculation in `calculateAyanamsaOffset(...)`. |
| **Parasara Bhattar** | **Standard Parasara Bhattar** (Plain Epoch Zero) & **Pushyapaksham 22** (`PUSHYAPAKSHA`) | Returns `0.0` offset for Plain/Standard. Computes `PUSHYAPAKSHA` offset (reference value 22°39'34.88" via `AyanamsaType.PUSHYAPAKSHA`) when Pushyapaksha is selected. |
| **Vakya** | **Traditional Vakya System** | Uses Vararuchi 248 Chandra Vakyas and 12 Surya Vakyas without ephemeris longitude modification. |

---

## 3. Architecture & Dependency Flow

- **Strategy & Factory Pattern**: `PanchangamFactory` resolves the requested `PanchangamEngine` (`DRIK_TIRUKANITHAM`, `VAKYA`, `PARASARA_BHATTAR`, `SURYA_SIDDHANTA`).
- **Divisional Charts**: Engines call `VargaCalculationService.generateD1MapFromLongitudes(...)` and `VargaCalculationService.generateVargaChart(..., VargaType.D9_NAVAMSA)` to construct D1 and D9 positions.
- **Git Tracking**: `VargaCalculationService.java` tracked and committed as core component of the engine suite.

---

## 4. Verification & Testing

- `mvn compile` must succeed with zero errors.
- `mvn test` must verify all 22 tests pass clean.
