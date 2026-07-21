# DrikVedic Bug Fix & Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Resolve all 11 user-reported astrology bugs across endpoints, Panchangam transitions, Chandrastamam nakshatras, chart interaction/rendering, Yoga/Dosha logic, and localization, followed by applying the new White & Saffron visual theme.

**Architecture:** 
1. Consolidate backend DTOs so `/api/v1/astrology/calculate` returns all horoscope data with explicit named chart properties (`d1Chart`, `d9Chart`, `bhavaChart`) and remove the redundant `/comprehensive` endpoint.
2. Refactor PDF report generation to use a key-based `vargaChartsMap` to ensure PDF charts are explicitly bound to their names without relying on array indexes.
3. Overhaul `DailyPanchangamServiceImpl` for mid-day element transitions and nakshatra-based Chandrastamam calculations.
4. Rewrite `AstrologyDiagnosticsService` for comprehensive traditional Yoga and Dosha detection.
5. Upgrade `IndianChart.jsx` to render clean house boxes, aspect lines on click, dignity details below, and auto-toggle North/South style based on selected language.
6. Complete all localization keys (including localized PDF titles like "ஜனன கால ஜாதகம்") and refresh theme styling in `index.css` to a light gradient white & saffron palette.

**Tech Stack:** Java 17, Spring Boot, OpenPDF / iText, React, Vanilla CSS.

## Global Constraints
- Target workspace: `d:\Intellij_WS\java-astro`
- Endpoint paths: `/api/v1/astrology/calculate`, `/api/v1/astrology/download-pdf`, `/api/v1/astrology/panchangam`
- PDF Title Key: `pdf.report.title` mapped to "Janana Kala Jadhagam" for Tamil and localized equivalents for all 6 languages.
- Language Code Mapping: `en` (English), `ta` (Tamil), `hi` (Hindi), `kn` (Kannada), `te` (Telugu), `ml` (Malayalam).

---

### Task 1: Enrich `ChartUiResponseDTO`, update `ChartOrchestrationService`, and remove `/comprehensive` endpoint

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java:29-83`
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java:41-51`

**Interfaces:**
- Consumes: `BirthDetailsDTO`, `ChartResult`, `PlanetaryPosition`
- Produces: Enriched `ChartUiResponseDTO` containing `d1Chart`, `d9Chart`, `bhavaChart`, `shadbalaStrengths`, `structuralDiagnostics`, `latitude`, `longitude`, `resolvedTimezone`

- [ ] **Step 1: Update `ChartUiResponseDTO.java` fields**

Add missing fields to `ChartUiResponseDTO`:
```java
package org.vedic.astro.dto;

import lombok.Builder;
import lombok.Data;
import org.vedic.astro.model.DasaPeriod;

import java.util.List;

@Data
@Builder
public class ChartUiResponseDTO {

    private String name;
    private String dateOfBirth;
    private String timeOfBirth;
    private String localMeanTime;
    private double latitude;
    private double longitude;
    private String resolvedTimezone;

    private ChartResponseDTO.BirthProfile birthProfile;
    
    // Explicit named charts
    private List<ChartResponseDTO.PositionDetail> d1Chart;
    private List<ChartResponseDTO.PositionDetail> d9Chart;
    private List<ChartResponseDTO.PositionDetail> bhavaChart;

    private List<DasaPeriod> currentDasaTimeline;
    private ShadbalaDTO shadbalaStrengths;
    private DiagnosticsDTO structuralDiagnostics;

    // Core Panchangam Element Block
    private String thithi;
    private String yogam;
    private String karanam;
}
```

- [ ] **Step 2: Update `ChartOrchestrationService.convertToUiDashboardResponse`**

In `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`, populate all enriched fields:
```java
    public ChartUiResponseDTO convertToUiDashboardResponse(ChartResult res, BirthDetailsDTO pay) {
        PlanetaryPosition moon = res.getD1Positions().get("Moon");
        LocalDate dob = LocalDate.of(pay.year(), pay.month(), pay.day());
        Map<String, PlanetaryPosition> d1 = res.getD1Positions();

        double sunLong = d1.get("Sun").getAbsoluteLongitude();
        double moonLong = d1.get("Moon").getAbsoluteLongitude();

        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        int thithiIdx = (int) (elongation / 12.0) + 1;
        thithiIdx = Math.min(30, Math.max(1, thithiIdx));

        String paksha = (thithiIdx <= 15) ? ts.getLabel("panchangam.shukla") : ts.getLabel("panchangam.krishna");
        int localizedThithiNum = (thithiIdx > 15) ? thithiIdx - 15 : thithiIdx;

        String rawThithiLabel = ts.getLabel("thithi." + localizedThithiNum);
        String computedThithi;
        if (thithiIdx == 15 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[0].trim();
        } else if (thithiIdx == 30 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[1].trim();
        } else {
            computedThithi = paksha + " - " + rawThithiLabel;
        }

        double totalYogaLong = (sunLong + moonLong + 720.0) % 360.0;
        int yogamIdx = (int) (totalYogaLong / (360.0 / 27.0)) + 1;
        String computedYogam = ts.getLabel("yogam." + Math.min(27, Math.max(1, yogamIdx)));

        int karanamIdx = (int) (elongation / 6.0) + 1;
        String computedKaranam = ts.getLabel("karanam." + resolveKaranamId(karanamIdx));

        return ChartUiResponseDTO.builder()
                .name(res.getName())
                .dateOfBirth(dob.toString())
                .timeOfBirth(String.format("%02d:%02d:%02d", pay.hour(), pay.minute(), pay.second()))
                .localMeanTime(res.getLocalMeanTime())
                .latitude(pay.latitude())
                .longitude(pay.longitude())
                .resolvedTimezone(res.getResolvedTimezone() != null ? res.getResolvedTimezone() : "Asia/Kolkata")
                .thithi(computedThithi)
                .yogam(computedYogam)
                .karanam(computedKaranam)
                .birthProfile(buildProfileHeader(d1))
                .d1Chart(compileVargaList(1, d1, null))
                .d9Chart(compileVargaList(9, d1, null))
                .bhavaChart(compileVargaList(-1, d1, null))
                .currentDasaTimeline(dasaEngine.calculateVimshottariTimeline(moon.getAbsoluteLongitude(), dob))
                .shadbalaStrengths(shadbalaService.calculateShadbala(d1))
                .structuralDiagnostics(diagnosticsService.runHoroscopeDiagnostics(d1))
                .build();
    }
```

- [ ] **Step 3: Remove `/comprehensive` endpoint from `ChartController.java`**

Delete the `/comprehensive` `@PostMapping` method from `ChartController.java`.

- [ ] **Step 4: Verify backend compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit changes**

```bash
git add src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java src/main/java/org/vedic/astro/service/ChartOrchestrationService.java src/main/java/org/vedic/astro/controller/ChartController.java
git commit -m "refactor: enrich ChartUiResponseDTO and remove redundant /comprehensive endpoint"
```

---

### Task 2: Refactor `ComprehensiveReportDTO` and `PdfExportService` for Explicit PDF Chart Mapping

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java:26`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java:126-149`
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java:161-183`

**Interfaces:**
- Consumes: `vargaChartsMap` in `ComprehensiveReportDTO`
- Produces: PDF chart grid cleanly bound to explicit chart keys (`"d1"`, `"d2"`, `"d3"`, `"bhava"`, `"d7"`, `"d9"`, `"d10"`, `"d12"`, `"d20"`, `"d24"`, `"d30"`, `"d60"`)

- [ ] **Step 1: Replace `vargaChartsSuite` with `vargaChartsMap` in `ComprehensiveReportDTO.java`**

```java
package org.vedic.astro.dto;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.vedic.astro.model.DasaPeriod;

@Data
@Builder
public class ComprehensiveReportDTO {
    private String name;
    private String dateOfBirth;
    private String timeOfBirth;
    private String localMeanTime;
    private double latitude;
    private double longitude;
    private String resolvedTimezone;

    // Core Panchangam Element Block
    private String thithi;
    private String yogam;
    private String karanam;

    private ChartResponseDTO.BirthProfile birthProfile;
    private List<ChartResponseDTO.PositionDetail> birthPlanetaryPositions;
    private Map<String, List<ChartResponseDTO.PositionDetail>> vargaChartsMap;
    private List<DasaPeriod> vimshottariTimeline;
    private ShadbalaDTO shadbalaStrengths;
    private DiagnosticsDTO structuralDiagnostics;
}
```

- [ ] **Step 2: Update `ChartOrchestrationService.compileComprehensivePdfData` to populate `vargaChartsMap`**

```java
        Map<String, List<ChartResponseDTO.PositionDetail>> suiteMap = new java.util.LinkedHashMap<>();
        suiteMap.put("d1", compileVargaList(1, d1, cusps));
        suiteMap.put("d2", compileVargaList(2, d1, cusps));
        suiteMap.put("d3", compileVargaList(3, d1, cusps));
        suiteMap.put("bhava", compileVargaList(-1, d1, cusps));
        suiteMap.put("d7", compileVargaList(7, d1, cusps));
        suiteMap.put("d9", compileVargaList(9, d1, cusps));
        suiteMap.put("d10", compileVargaList(10, d1, cusps));
        suiteMap.put("d12", compileVargaList(12, d1, cusps));
        suiteMap.put("d20", compileVargaList(20, d1, cusps));
        suiteMap.put("d24", compileVargaList(24, d1, cusps));
        suiteMap.put("d30", compileVargaList(30, d1, cusps));
        suiteMap.put("d60", compileVargaList(60, d1, cusps));
```
And set `.vargaChartsMap(suiteMap)`.

- [ ] **Step 3: Update `PdfExportService.java` chart suite iteration to use `vargaChartsMap` lookup**

In `PdfExportService.java` around line 161:
```java
            String[] vargaKeys = {
                    "pdf.chart.d1", "pdf.chart.d2", "pdf.chart.d3", "pdf.chart.bhava",
                    "pdf.chart.d7", "pdf.chart.d9", "pdf.chart.d10", "pdf.chart.d12",
                    "pdf.chart.d20", "pdf.chart.d24", "pdf.chart.d30", "pdf.chart.d60"
            };
            String[] chartMapKeys = {
                    "d1", "d2", "d3", "bhava", "d7", "d9", "d10", "d12", "d20", "d24", "d30", "d60"
            };

            for (int i = 0; i < 12; i++) {
                PdfPCell layoutCell = new PdfPCell(); layoutCell.setBorder(PdfPCell.NO_BORDER); layoutCell.setPadding(6);
                String resolvedTitleText = ts.getLabel(vargaKeys[i]);

                List<ChartResponseDTO.PositionDetail> planets = data.getVargaChartsMap() != null ? data.getVargaChartsMap().get(chartMapKeys[i]) : null;
                if (planets != null) {
                    if (isHi) {
                        Paragraph chartLabel = buildMixedParagraph(resolvedTitleText, boldB, engBoldB);
                        chartLabel.setSpacingAfter(4);
                        layoutCell.addElement(chartLabel);
                        layoutCell.addElement(buildNorthIndianTemplateImage(writer, planets, chartFontActual, engBf));
                    } else {
                        layoutCell.addElement(buildCleanSouthIndianGrid(planets, resolvedTitleText, chartFontActual, chartBoldActual, isKn));
                    }
                }
                masterGrid.addCell(layoutCell);
            }
```

- [ ] **Step 4: Verify backend compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit changes**

```bash
git add src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java src/main/java/org/vedic/astro/service/ChartOrchestrationService.java src/main/java/org/vedic/astro/service/PdfExportService.java
git commit -m "fix(pdf): map PDF varga charts explicitly by name using vargaChartsMap"
```

---

### Task 3: Switch Frontend `HoroscopePage.jsx` to `/calculate` and consume named chart fields

**Files:**
- Modify: `frontend/src/pages/HoroscopePage.jsx:19, 72-85`

**Interfaces:**
- Consumes: Enriched `ChartUiResponseDTO` from `/api/v1/astrology/calculate`
- Produces: Horoscope UI rendered directly from named fields (`d1Chart`, `d9Chart`, `currentDasaTimeline`, `shadbalaStrengths`, `structuralDiagnostics`)

- [ ] **Step 1: Update API call in `HoroscopePage.jsx`**

In `frontend/src/pages/HoroscopePage.jsx` line 19:
Change `/api/v1/astrology/comprehensive` to `/api/v1/astrology/calculate`.

- [ ] **Step 2: Update chart rendering to use `report.d1Chart` and `report.d9Chart`**

In `renderChartsTab`:
```javascript
  const renderChartsTab = () => {
    if (!report) return null;
    const d1 = report.d1Chart || [];
    const d9 = report.d9Chart || d1;

    return (
      <div>
        <div className="grid-2">
          <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 className="title-gold">D1 Rasi Chart</h3>
            <IndianChart positions={d1} style="south" title="D1 Rasi" lang={settings.language} />
          </div>
          <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <h3 className="title-gold">D9 Navamsa Chart</h3>
            <IndianChart positions={d9} style="south" title="D9 Navamsa" lang={settings.language} />
          </div>
        </div>
      </div>
    );
  };
```

- [ ] **Step 3: Update `renderDasaTab` property references**

Ensure `renderDasaTab` reads `report.currentDasaTimeline`.

- [ ] **Step 4: Verify frontend build**

Run: `cd frontend && npm run build`
Expected: built in XXXms (clean build)

- [ ] **Step 5: Commit changes**

```bash
git add frontend/src/pages/HoroscopePage.jsx
git commit -m "refactor(frontend): switch HoroscopePage to /calculate endpoint with explicit d1Chart and d9Chart props"
```

---

### Task 4: Mid-Day Panchangam Element Transitions

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java`
- Modify: `frontend/src/pages/PanchangamPage.jsx`

**Interfaces:**
- Consumes: Ephemeris transit search for Thithi, Nakshatra, Yogam, Karanam
- Produces: Panchangam elements with structured `nextName` and `nextEndTime` fields

- [ ] **Step 1: Extend `ElementDetail` in `DailyPanchangamDTO.java`**

```java
    @Data
    @Builder
    public static class ElementDetail {
        private String name;
        private String localizedName;
        private String endTime;
        private String nextName;
        private String nextLocalizedName;
        private String nextEndTime;
    }
```

- [ ] **Step 2: Populate transition info in `DailyPanchangamServiceImpl.java`**

Compute the next consecutive element when `endTime` occurs before midnight:
- Thithi: next index `(currentIdx % 30) + 1`
- Nakshatra: next index `(currentIdx % 27) + 1`
- Yogam: next index `(currentIdx % 27) + 1`
- Karanam: next karana index `(currentIdx % 60) + 1`

- [ ] **Step 3: Render transition text in `PanchangamPage.jsx`**

Format Panchangam element lines:
```javascript
<div>
  <strong>{t('thithi', settings.language)}:</strong> {data.thithi?.localizedName || data.thithi?.name} 
  <span style={{ color: 'var(--text-secondary)', marginLeft: '10px' }}>
    {t('endsAt', settings.language)} {data.thithi?.endTime}
    {data.thithi?.nextLocalizedName && `, ${t('then', settings.language)} ${data.thithi?.nextLocalizedName} ${data.thithi?.nextEndTime || ''}`}
  </span>
</div>
```

- [ ] **Step 4: Verify build**

Run: `mvn compile` and `cd frontend && npm run build`
Expected: Both pass cleanly.

- [ ] **Step 5: Commit changes**

```bash
git add src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java frontend/src/pages/PanchangamPage.jsx
git commit -m "feat(panchangam): compute and display mid-day element transitions"
```

---

### Task 5: Nakshatra-Based Chandrastamam Calculation

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java:221-224`
- Modify: `src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java:22`
- Modify: `frontend/src/pages/PanchangamPage.jsx:126`

**Interfaces:**
- Consumes: Moon's transit sign on target date
- Produces: List of nakshatras falling within the 8th rashi from transit Moon

- [ ] **Step 1: Update DTO field in `DailyPanchangamDTO.java`**

Change `String chandrastamamRashi` to `List<String> chandrastamamNakshatras`.

- [ ] **Step 2: Compute Chandrastamam nakshatras in `DailyPanchangamServiceImpl.java`**

Find the 8th sign from Moon's transit sign `targetSign = ((rashiNum - 1 + 7) % 12) + 1`.
Map the nakshatras corresponding to `targetSign`:
```java
// Signs map to 2.25 nakshatras each (e.g. Sign 8 Vrischika -> Vishakha 4th pada, Anuradha 1-4, Jyeshtha 1-4)
List<String> nakshatras = getNakshatrasForSign(targetSign).stream()
    .map(nakNum -> translationService.getLocalizedNakshatra(nakNum))
    .collect(Collectors.toList());
```

- [ ] **Step 3: Render in `PanchangamPage.jsx`**

Display `data.chandrastamamNakshatras.join(', ')`.

- [ ] **Step 4: Verify build**

Run: `mvn compile` & `cd frontend && npm run build`
Expected: PASS

- [ ] **Step 5: Commit changes**

```bash
git add src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java frontend/src/pages/PanchangamPage.jsx
git commit -m "fix(panchangam): calculate Chandrastamam based on nakshatras instead of sign"
```

---

### Task 6: Chart Rendering Improvements (Clean Houses, Aspect Lines, Dignity Info & North/South Toggle)

**Files:**
- Modify: `frontend/src/components/IndianChart.jsx`

**Interfaces:**
- Consumes: `positions` (List of PositionDetail), `lang` (settings.language)
- Produces: Interactive chart (cleared house numbers, aspect line on click, dignity/lordship details panel below, North Indian diamond layout when `lang === 'hi'`)

- [ ] **Step 1: Add North Indian Diamond SVG generator to `IndianChart.jsx`**

When `lang === 'hi'`, render North Indian diamond grid layout.
Otherwise, render South Indian square grid layout.

- [ ] **Step 2: Clean house background labels**

Remove background sign numbers/rashi names from grid boxes. Paint ONLY calculated planet abbreviations.

- [ ] **Step 3: Add click handler and SVG aspect line**

On house click:
1. Store `selectedHouse` state.
2. Draw SVG line between `selectedHouse` and `oppositeHouse = ((selectedHouse + 5) % 12) + 1`.
3. Display detailed breakdown below the chart:
   - Planet Name (localized)
   - Degree in Sign
   - Dignity (Atchi / Neetcham / Aatchi / Utcham / Samam)
   - House Lordship

- [ ] **Step 4: Verify frontend build**

Run: `cd frontend && npm run build`
Expected: PASS

- [ ] **Step 5: Commit changes**

```bash
git add frontend/src/components/IndianChart.jsx
git commit -m "feat(chart): clean house boxes, add click aspect line, dignity panel and Hindi North Indian toggle"
```

---

### Task 7: Comprehensive Yoga & Dosha Rewrite

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java`
- Modify: `src/main/java/org/vedic/astro/util/PlanetDignityUtils.java`

**Interfaces:**
- Consumes: `Map<String, PlanetaryPosition> d1Map`
- Produces: `DiagnosticsDTO` with verified Yogas (Gajakesari, Budha-Aditya non-combust, Chandra-Mangala, Pancha Mahapurusha, Amala, Neechabhanga Raja, Vipareetha Raja) and Doshas (Sevvai, Kala Sarpa, Sarpam, Pithru, Putra, Kalathira, Shani)

- [ ] **Step 1: Add combustion and lordship utilities in `PlanetDignityUtils.java`**

```java
public static boolean isCombust(String planet, double planetAbsLong, double sunAbsLong) {
    if ("Sun".equalsIgnoreCase(planet) || "Rahu".equalsIgnoreCase(planet) || "Ketu".equalsIgnoreCase(planet)) return false;
    double diff = Math.abs(planetAbsLong - sunAbsLong);
    if (diff > 180) diff = 360 - diff;
    return diff < 14.0; // Standard 14° orb
}

public static String getSignLord(int sign) {
    return switch (sign) {
        case 1, 8 -> "Mars";
        case 2, 7 -> "Venus";
        case 3, 6 -> "Mercury";
        case 4 -> "Moon";
        case 5 -> "Sun";
        case 9, 12 -> "Jupiter";
        case 10, 11 -> "Saturn";
        default -> "";
    };
}
```

- [ ] **Step 2: Rewrite Yoga logic in `AstrologyDiagnosticsService.java`**

Add Budha-Aditya non-combust check, Amala Yoga, Neechabhanga Raja Yoga, and Vipareetha Raja Yoga checks.

- [ ] **Step 3: Tighten Kala Sarpa & Sevvai Dosha nullifications**

Ensure Kala Sarpa verifies all 7 non-node planets are enclosed strictly on one side of the Rahu-Ketu axis.

- [ ] **Step 4: Verify backend compilation**

Run: `mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit changes**

```bash
git add src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java src/main/java/org/vedic/astro/util/PlanetDignityUtils.java
git commit -m "refactor(astrology): comprehensive rewrite of Yogas and Doshams evaluation engine"
```

---

### Task 8: Localization Completeness & PDF Title Fix

**Files:**
- Modify: `src/main/resources/i18n/messages*.properties` (all 7 files)
- Modify: `frontend/src/i18n/translations.js`
- Modify: `frontend/src/pages/HoroscopePage.jsx`
- Modify: `frontend/src/pages/MatchingPage.jsx`
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`

**Interfaces:**
- Consumes: Resource keys for all 6 supported languages
- Produces: 100% localized UI and PDF report titled "Janana Kala Jadhagam" / "ஜனன கால ஜாதகம்" / "जन्म कुंडली" / etc.

- [ ] **Step 1: Add `pdf.report.title` and missing keys to backend `.properties` files**

- `messages_ta.properties`: `pdf.report.title=ஜனன கால ஜாதகம்`
- `messages_hi.properties`: `pdf.report.title=जन्म कुंडली`
- `messages_kn.properties`: `pdf.report.title=ಜನ್ಮ ಕುಂಡಲಿ`
- `messages_te.properties`: `pdf.report.title=జన్మ కుండలి`
- `messages_ml.properties`: `pdf.report.title=ജാതക കുറിപ്പ്`
- `messages_en.properties`: `pdf.report.title=Janana Kala Jadhagam (Horoscope Report)`
- `messages.properties`: `pdf.report.title=Janana Kala Jadhagam (Horoscope Report)`

Add dignity labels (`dignity.own`, `dignity.exalted`, `dignity.debilitated`, `dignity.neutral`) to all files.

- [ ] **Step 2: Update `frontend/src/i18n/translations.js`**

Add keys: `then`, `active`, `cancelled`, `impact`, `remedy`, `nullification`, `specialFeatures`, `matched`, `matchedViaException`, `notMatched`.

- [ ] **Step 3: Update `HoroscopePage.jsx` and `MatchingPage.jsx` text usages**

Replace hardcoded English text in Diagnostics tab, loading banners, and matching status text with `t(key, lang)`.

- [ ] **Step 4: Update `PdfExportService.java` to use `ts.getLabel("pdf.report.title")`**

- [ ] **Step 5: Verify build**

Run: `mvn compile` & `cd frontend && npm run build`
Expected: PASS

- [ ] **Step 6: Commit changes**

```bash
git add src/main/resources/i18n/ frontend/src/i18n/translations.js frontend/src/pages/HoroscopePage.jsx frontend/src/pages/MatchingPage.jsx src/main/java/org/vedic/astro/service/PdfExportService.java
git commit -m "fix(i18n): complete localization dictionary and localize PDF report title"
```

---

### Task 9: White & Saffron Theme Redesign

**Files:**
- Modify: `frontend/src/index.css`
- Modify: `frontend/src/App.css`

**Interfaces:**
- Consumes: CSS variables in `:root`
- Produces: Fresh, warm, temple-inspired light gradient (white & pale saffron) theme palette

- [ ] **Step 1: Redefine `:root` CSS variables in `frontend/src/index.css`**

```css
:root {
  --bg-primary: #fffaf4;
  --bg-secondary: #ffffff;
  --bg-card: #ffffff;
  --bg-card-hover: #fff5e8;
  --accent-saffron: #ff6b00;
  --accent-gold: #d4a843;
  --accent-warm: #e85d04;
  --text-primary: #1a1a2e;
  --text-secondary: #665c52;
  --success: #2e7d32;
  --warning: #ed6c02;
  --danger: #d32f2f;
  --border: #f0e2d0;
  --shadow: 0 4px 20px rgba(232, 93, 4, 0.08);
  --font-sans: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
```

- [ ] **Step 2: Apply light gradient background to body and main container**

```css
body {
  background: linear-gradient(180deg, #ffffff 0%, #fff4e5 100%);
  color: var(--text-primary);
  min-height: 100vh;
}

.card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.navbar {
  background-color: #ffffff;
  border-bottom: 2px solid var(--accent-saffron);
}

.btn-primary {
  background: linear-gradient(135deg, var(--accent-saffron), var(--accent-warm));
  color: #ffffff;
}
```

- [ ] **Step 3: Verify frontend build**

Run: `cd frontend && npm run build`
Expected: PASS

- [ ] **Step 4: Commit changes**

```bash
git add frontend/src/index.css frontend/src/App.css
git commit -m "style: apply white and saffron light gradient theme redesign"
```

---

### Task 10: Final Verification & End-to-End Build

**Files:**
- All modified project files

- [ ] **Step 1: Run full Maven backend build**

Run: `mvn clean package`
Expected: BUILD SUCCESS

- [ ] **Step 2: Run full Frontend production build**

Run: `cd frontend && npm run build`
Expected: Built in XXXms

- [ ] **Step 3: Push changes to GitHub**

Run: `git push origin master`
Expected: Successfully pushed to origin/master

- [ ] **Step 4: Announce completion**

Provide summary of all fixed bugs, enriched DTO architecture, and new white & saffron design.
