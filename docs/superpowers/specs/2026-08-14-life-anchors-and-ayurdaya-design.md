# Design Specification: Life Anchors Accuracy, 3-Principle Ayurdaya Engine & Feature Flags

**Date**: 2026-08-14  
**Topic**: Life Anchors Calculations Integrity, 3-Principle Ayurdaya (Longevity) Engine, YAML Feature Toggles, and 6-Language Consistency  
**Status**: Approved

---

## 1. Overview & Problem Statement

Users observed that:
1. Longevity (Ayul / Ayurdaya) calculations previously yielded inaccurate or alarming raw numbers (e.g. Alpayu 32 years) because the calculations relied on raw Jaimini 3-pair modalities without evaluating Parashara longevity rules, Hora Lagna, Kakshya Vriddhi/Hrasa, or Shadbala life-force (Ayur Bala).
2. Several Life Anchor calculation utilities contained hardcoded Tamil default strings or incomplete modality branches.
3. There was no YAML configuration toggle to control the visibility of the Life Anchors sub-tab or specifically the Ayul / Longevity section across both the UI and PDF reports.

This specification outlines the end-to-end design to calculate all 3 classical principles of Longevity (Ayurdaya), provide clean YAML toggles for Life Anchors and Ayurdaya, ensure robust chart data propagation from Swiss Ephemeris, and synchronize all strings across all 6 supported languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`).

---

## 2. Core Architecture & Data Flow

```
+-----------------------------------------------------------------------------------+
|                        Swiss Ephemeris & D1 / D9 Engine                           |
+-----------------------------------------+-----------------------------------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                 |                                 |
+-------v--------------------+  +---------v--------------------+  +---------v--------------------+
| D1 Exact Positions &       |  | D9 Navamsa Positions &       |  | Vimshottari Dasa Timeline    |
| Planetary Longitudes       |  | Signs (Karakamsa, etc.)      |  | (Start, End Dates, Lords)    |
+-------+--------------------+  +---------+--------------------+  +---------+--------------------+
        |                                 |                                 |
+-------v---------------------------------v---------------------------------v--------------------+
|                             ChartOrchestrationService                                          |
|  - Computes Hora Lagna (HL) from Sunrise & birth time                                         |
|  - Evaluates 3-Principle Ayurdaya, Numerology, Deities, Gemology, and Structural Anchors       |
|  - Passes configuration flags: lifeAnchorsEnabled, ayurdayaEnabled                            |
+-----------------------------------------+------------------------------------------------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                                                   |
+-------v-----------------------------------+             +-----------------v--------------------+
| REST API (`/api/v1/astrology/chart`)      |             | PDF Export Engine                    |
| - `ChartUiResponseDTO`                    |             | (`PdfExportService`)                 |
| - `lifeAnchorsEnabled: true/false`        |             | - Respects `include-life-anchors`    |
| - `ayurdayaEnabled: true/false`           |             | - Respects `include-ayurdaya`        |
+-------+-----------------------------------+             +--------------------------------------+
        |
+-------v-----------------------------------+
| Frontend UI (`HoroscopePage.jsx`)         |
| - Conditionally renders Life Anchors Tab  |
| - Conditionally renders Ayurdaya Section  |
| - 100% 6-Language localized               |
+-------------------------------------------+
```

---

## 3. 3-Principle Ayurdaya (Longevity Determination) Engine

The enhanced engine in `AyurdayaCalculationUtils` will evaluate and present three distinct classical principles:

### Principle 1: Classical Jaimini 3-Pair Modality with Kakshya Rules
- **Pair 1**: Lagna Lord & 8th Lord modalities (`Chara`, `Sthira`, `Dwisvabhava`).
- **Pair 2**: Moon & Saturn (Ayushkaraka) modalities.
- **Pair 3**: Lagna & Hora Lagna (with Moon fallback if Hora Lagna is not present) modalities.
- **Modality Combinations**:
  - `Chara + Chara` $\rightarrow$ Poornayu (Long Life)
  - `Chara + Sthira` $\rightarrow$ Madhyayu (Medium Life)
  - `Chara + Dwisvabhava` $\rightarrow$ Alpayu (Short Life)
  - `Sthira + Chara` $\rightarrow$ Madhyayu
  - `Sthira + Sthira` $\rightarrow$ Alpayu
  - `Sthira + Dwisvabhava` $\rightarrow$ Poornayu
  - `Dwisvabhava + Chara` $\rightarrow$ Alpayu
  - `Dwisvabhava + Sthira` $\rightarrow$ Poornayu
  - `Dwisvabhava + Dwisvabhava` $\rightarrow$ Madhyayu
- **Majority Resolution**: 2 of 3 pairs agreement determines base category (*Poornayu 66–100+ yrs*, *Madhyayu 33–66 yrs*, *Alpayu 0–33 yrs*).
- **Kakshya Vriddhi (Life Extension Rules)**:
  1. *Jupiter in Kendra (1, 4, 7, 10) or Trikona (5, 9)* or aspecting Lagna/7th: Promotes span by **+1 Kakshya tier** (e.g. Alpayu $\rightarrow$ Madhyayu, Madhyayu $\rightarrow$ Poornayu).
  2. *Lagna Lord or Ayushkaraka Saturn in Swakshetra (Own Sign) or Uchha (Exalted)*: Adds **+4 to +7 years** of vitality ceiling.
- **Kakshya Hrasa (Life Reduction Rules)**:
  1. Applied only when Lagna, 8th House, and Saturn are all simultaneously afflicted by natural malefics without benefic aspects.

### Principle 2: Parashara & Shadbala Life-Force (Ayur Bala)
- Evaluates the native's constitutional stamina and protective yogas:
  - Lagna Lord Shadbala Virupas relative to required standard.
  - Presence of natural benefics (Jupiter, Venus, Mercury) in Kendras/Trikonas (*Deerghayu Yogas*).
  - 8th House benefic vs malefic occupancy and aspect balance.
  - Generates a qualitative score: *High Resilience & Robust Life Force*, *Balanced Vitality*, or *Health-Cautious Vitality*.

### Principle 3: Maraka & Badhaka Timeline & Planetary Vulnerability
- Identifies primary Maraka Lords (2nd & 7th Lords from Lagna) and Badhaka Lord (11th for Movable, 9th for Fixed, 7th for Dual Lagna).
- Cross-references the native's active and upcoming Vimshottari Mahadasa / Antardasa periods to provide specific age windows for health mindfulness and remedies (e.g. Maha Mrityunjaya, Dhanvantari, Shiva worship).

---

## 4. Configuration Properties & Feature Toggles

### `src/main/resources/application.yml`
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

### Backend DTOs
- `ChartUiResponseDTO`:
  - `boolean lifeAnchorsEnabled`
  - `boolean ayurdayaEnabled`
- If `lifeAnchorsEnabled == false`:
  - `lifeAnchors` field is set to `null` and the UI omits the tab button.
- If `ayurdayaEnabled == false`:
  - `ayurdayaProfile` field is set to `null`, UI omits the Ayurdaya hero card and calculations, and PDF export omits the Longevity section.

---

## 5. Clean English Models & 6-Language Localization

1. **Backend Utilities**:
   - `NumerologyUtils.java`: Replace inline Tamil strings with clean English defaults.
   - `StructuralAnchorsUtils.java`: Replace inline Tamil strings with clean English defaults.
   - `SpiritualDeityUtils.java`: Clean all remaining default text.
2. **ResourceBundles (`src/main/resources/i18n/`)**:
   - Add all Ayurdaya 3-principle breakdown keys across all 6 files:
     - `messages_en.properties`
     - `messages_ta.properties`
     - `messages_hi.properties`
     - `messages_te.properties`
     - `messages_kn.properties`
     - `messages_ml.properties`
     - `messages.properties`
3. **Frontend (`frontend/src/i18n/translations.js`)**:
   - Add all 3-principle headers, tooltips, and labels across all 6 languages.
   - Update `LifeAnchorsLongevityView.jsx` to render the 3-principle tabbed/accordion view cleanly.

---

## 6. Verification & Quality Gate Plan

1. **Unit & Integration Tests**:
   - `AyurdayaCalculationUtilsTest.java`: Validate Jaimini 3-pair calculation, Hora Lagna integration, and Kakshya Vriddhi/Hrasa adjustments across various Lagna charts (Poornayu, Madhyayu, Alpayu upgraded by Jupiter).
   - `ChartControllerTest.java` / `ChartOrchestrationServiceTest.java`: Verify that `lifeAnchorsEnabled` and `ayurdayaEnabled` flags properly toggle the response fields and sub-tabs.
   - `PdfExportServiceTest.java`: Verify PDF generation with and without Ayurdaya and Life Anchors sections.
2. **ResourceBundle Parity Check**:
   - Automated check confirming all 6 property files have 100% key match with 0 missing keys.
3. **Frontend Build**:
   - `npm run build --prefix frontend` builds with zero errors.
