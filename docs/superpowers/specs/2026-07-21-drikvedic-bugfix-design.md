# DrikVedic Comprehensive Bug Fix & Improvements Design Spec

**Date:** 2026-07-21
**Scope:** 11 bugs + theme redesign (Phase 2)

---

## 1. Unified `/calculate` Endpoint Architecture

### Problem
The frontend Horoscope page calls `/comprehensive` which returns a `ComprehensiveReportDTO` with an indexed `vargaChartsSuite` array (`List<List<PositionDetail>>`). The index mapping (`[0]=D1, 1=D2, 2=D3, 3=Bhava, 4=D7, 5=D9...`) is fragile and has caused the D9 chart to render D2 Hora data instead of Navamsa.

### Solution
- **Enrich `ChartUiResponseDTO`** with all horoscope data currently only in `ComprehensiveReportDTO`:
  - `shadbalaStrengths` (ShadbalaDTO)
  - `structuralDiagnostics` (DiagnosticsDTO — yogas/doshas)
  - Named varga charts: `d1Chart`, `d9Chart`, `bhavaChart` (explicit fields, no indexed suite)
  - `latitude`, `longitude`, `resolvedTimezone`
- **Delete** the `/comprehensive` endpoint from [ChartController.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/controller/ChartController.java)
- **Keep `ComprehensiveReportDTO`** as an internal-only DTO used solely by the `/download-pdf` endpoint
- **Update `ChartOrchestrationService.convertToUiDashboardResponse()`** to populate the new fields
- **Update `HoroscopePage.jsx`** to call `/calculate` instead of `/comprehensive` and read named fields directly

### New ChartUiResponseDTO fields
```java
@Data @Builder
public class ChartUiResponseDTO {
    private String name;
    private String dateOfBirth;
    private String timeOfBirth;
    private String localMeanTime;
    private double latitude;
    private double longitude;
    private ChartResponseDTO.BirthProfile birthProfile;

    // Named charts — no index confusion
    private List<ChartResponseDTO.PositionDetail> d1Chart;    // Rasi
    private List<ChartResponseDTO.PositionDetail> d9Chart;    // Navamsa
    private List<ChartResponseDTO.PositionDetail> bhavaChart; // Bhava

    // Panchangam
    private String thithi;
    private String yogam;
    private String karanam;

    // Enriched data (moved from /comprehensive)
    private List<DasaPeriod> currentDasaTimeline;
    private ShadbalaDTO shadbalaStrengths;
    private DiagnosticsDTO structuralDiagnostics;
}
```

### 1b. Fix PDF Chart Rendering — Same Index Problem

**Problem:** The PDF generation in `PdfExportService` (line 171) also uses `data.getVargaChartsSuite().get(i)` paired with a separate `vargaKeys[]` array. If the suite index order doesn't match the label order, charts are mislabeled in the PDF too.

**Solution:** Refactor `ComprehensiveReportDTO` to replace the indexed `List<List<PositionDetail>> vargaChartsSuite` with a **named map**:

```java
// ComprehensiveReportDTO — internal PDF-only DTO
private Map<String, List<ChartResponseDTO.PositionDetail>> vargaChartsMap;
// Keys: "d1", "d2", "d3", "bhava", "d7", "d9", "d10", "d12", "d20", "d24", "d30", "d60"
```

**PdfExportService update:** Instead of iterating by index, iterate by chart key name:

```java
String[] chartKeys = {"d1", "d2", "d3", "bhava", "d7", "d9", "d10", "d12", "d20", "d24", "d30", "d60"};
String[] labelKeys = {"pdf.chart.d1", "pdf.chart.d2", ..., "pdf.chart.d60"};

for (int i = 0; i < chartKeys.length; i++) {
    List<PositionDetail> planets = data.getVargaChartsMap().get(chartKeys[i]); // Named lookup!
    String title = ts.getLabel(labelKeys[i]);
    // ... render chart with correct data guaranteed by name match
}
```

**`ChartOrchestrationService.compileComprehensivePdfData()`:** Build the map with explicit key→chart binding:
```java
Map<String, List<PositionDetail>> chartsMap = new LinkedHashMap<>();
chartsMap.put("d1", compileVargaList(1, d1, cusps));
chartsMap.put("d2", compileVargaList(2, d1, cusps));
chartsMap.put("d3", compileVargaList(3, d1, cusps));
chartsMap.put("bhava", compileVargaList(-1, d1, cusps));
chartsMap.put("d7", compileVargaList(7, d1, cusps));
chartsMap.put("d9", compileVargaList(9, d1, cusps));
// ... etc
```

This guarantees that the chart labeled "D9 - நவாம்ச சக்கரம்" in the PDF always contains actual D9 Navamsa data, regardless of iteration order.

## 2. Panchangam Transitional Display

### Problem
Thithi, Nakshatram, Yogam, and Karanam can change mid-day. Currently only the starting value is shown with an end-time, but the *next* value is not displayed.

### Solution
- **Backend (`DailyPanchangamServiceImpl`):** For each panchangam element, return both the current value AND the next value if it transitions before midnight. The DTO field becomes:
  ```
  thithi: { name: "Dwithiyai", endTime: "14:30", nextName: "Thrithiyai", nextEndTime: "next day" }
  ```
- **Frontend (`PanchangamPage.jsx`):** Render as structured text:
  > **திதி:** துவிதியை 14:30 வரை, பின் திரிதியை அடுத்த நாள் வரை

---

## 3. Chandrastamam — Nakshatra-Based

### Problem
Currently calculates Chandrastamam as a rashi (sign 8th from Moon). Traditional Panchangam shows Chandrastamam as the *nakshatras* that fall in the 8th sign from Moon's current transit sign.

### Solution
- **Backend:** Calculate the 8th sign from Moon's daily transit sign. Then list all 2-3 nakshatras that fall within that sign (e.g., sign 8 = Vrischika → Vishakha 4th pada, Anuradha, Jyeshtha).
- **DTO:** Change `chandrastamamRashi` (String) to `chandrastamamNakshatras` (List<String>) containing the localized nakshatra names.
- **Frontend:** Display as comma-separated nakshatras instead of a single rashi.

---

## 4. Chart Rendering Improvements

### 4a. Remove House Numbers/Rashi Labels
- **Current:** Each house in the South Indian chart displays the house/rashi number or rashi name as background text.
- **Fix:** Only render planet abbreviations from the calculation results. Houses show nothing if empty.

### 4b. Interactive House Click
- **On click:** Draw an SVG aspect line from the clicked house to its 7th house (opposite).
- **Below chart:** Display a detail panel showing:
  - Planet name (localized)
  - Degree: e.g., `15°23'`
  - Dignity: Translated (e.g., "ஆட்சி" for own sign, "நீசம்" for debilitated in Tamil)
  - Lordship: Which planet rules this house

### 4c. Language-Driven Chart Style
- **Hindi (`hi`):** Render North Indian diamond layout
- **All other languages:** Render South Indian square layout
- Logic in `IndianChart.jsx`: `if (lang === 'hi') renderNorthStyle() else renderSouthStyle()`

---

## 5. Yoga & Dosha Comprehensive Rewrite

### Problem
Current yoga evaluation uses simplified checks. Example: Budha-Aditya Yoga only checks Sun-Mercury same-sign conjunction but doesn't verify combustion distance. Kala Sarpa checks are incomplete.

### Solution — Full Rewrite of `AstrologyDiagnosticsService`

#### Yogas to Implement (with proper rules)
| Yoga | Rule | Current Status |
|------|------|---------------|
| Gajakesari | Jupiter in kendra (1/4/7/10) from Moon | ✅ Exists — verify |
| Budha-Aditya | Sun-Mercury conjunction, Mercury NOT combust (<14°) | ⚠️ Missing combustion check |
| Chandra-Mangala | Moon-Mars conjunction | ✅ Exists — verify |
| Pancha Mahapurusha | Mars/Mercury/Jupiter/Venus/Saturn in kendra AND own/exalted | ✅ Exists — verify |
| Amala Yoga | Only benefic in 10th from Moon or Lagna | 🆕 Add |
| Neechabhanga Raja | Debilitated planet's dispositor in kendra from Lagna/Moon | 🆕 Add |
| Vipareetha Raja | Lord of 6/8/12 in another dusthana | 🆕 Add |

#### Doshas to Verify/Fix
| Dosha | Known Issues |
|-------|-------------|
| Sevvai Dosham | Check from Lagna, Moon, AND Venus — all 3 references. Current logic ✅ looks correct |
| Kala Sarpa | Must verify ALL 7 planets are hemmed between Rahu-Ketu axis (not just majority) |
| Sarpam | Review conjunction/aspect rules |
| Pithru/Putra/Kalathira/Shani | Review nullification conditions against traditional texts |

#### Nullification Logic Audit
Each dosha's nullification conditions will be reviewed against Brihat Parashara Hora Shastra rules. Current nullification reasons from resource bundles will be preserved but the triggering conditions will be tightened.

---

## 6. Localization Completeness

### 6a. Frontend Translations
Items already partially done (Dasa/Bhukthi labels, Shadbala headers). Remaining gaps:
- Diagnostics tab: "Impact:", "Nullification:", "Remedy:", "No affliction detected.", "Special Features" — all hardcoded in English in HoroscopePage.jsx
- Matching page: status text "✅ Matched", "🔄 Matched via Exception", "❌ Not Matched" — hardcoded in English in MatchingPage.jsx
- Loading/error messages in HoroscopePage still partially English

### 6b. Backend Resource Files Audit
- Scan all 7 `.properties` files for missing keys compared to `messages.properties` (base)
- Add missing Shadbala title key for PDF
- Add dignity labels: `dignity.own`, `dignity.exalted`, `dignity.debilitated`, `dignity.neutral` to all language files

### 6c. PDF Title Localization
- Add resource key `pdf.title` to all language bundles:
  - en: `Horoscope Report`
  - ta: `ஜனன கால ஜாதகம்`
  - hi: `जन्म कुंडली`
  - kn: `ಜನ್ಮ ಕುಂಡಲಿ`
  - te: `జన్మ కుండలి`
  - ml: `ജാതക കുറിപ്പ്`
- Update `PdfExportService` to use this key for the report title

---

## 7. Theme Redesign (Phase 2 — After Bug Fixes)

### Target Palette
- **Background:** Light gradient from `#FFFFFF` to `#FFF5E6` (white → pale saffron)
- **Cards:** Pure white `#FFFFFF` with soft shadow
- **Primary accent:** Deep saffron `#FF6B00`
- **Secondary accent:** Burnt orange `#E85D04`
- **Gold highlight:** `#D4A843` (keep current)
- **Text primary:** `#1A1A2E` (dark navy)
- **Text secondary:** `#666666`
- **Border:** `#E8E0D4` (warm gray)
- **Inputs:** White background with warm border
- **Navbar:** White with saffron bottom border accent

### Implementation
- Update CSS custom properties in `:root` of `index.css`
- Adjust card shadows, hover states, and gradient backgrounds
- Ensure charts (SVG) adapt colors accordingly

---

## Execution Order

1. **Workstream A** — Enrich `/calculate` + delete `/comprehensive` + update frontend
2. **Workstream B** — Panchangam transitions + Chandrastamam nakshatra fix
3. **Workstream C** — Chart rendering (clean houses, click interaction, North/South toggle)
4. **Workstream D** — Yoga/Dosha comprehensive rewrite
5. **Workstream E** — Localization completeness (frontend + backend + PDF)
6. **Workstream F** — Theme redesign (white + saffron)

---

## Verification Plan

### Automated
- `mvn clean package` — backend compiles
- `npm run build` — frontend compiles
- Git push triggers Render auto-deploy

### Manual
- Test horoscope for known birth data — verify D1/D9 charts show correct positions
- Compare Dasa timeline dates with established astrology software
- Switch languages and verify all text renders in selected language
- Download PDF in Tamil — verify title shows "ஜனன கால ஜாதகம்"
- Check Panchangam transitions across midnight boundary
- Click chart houses and verify aspect lines + dignity display
