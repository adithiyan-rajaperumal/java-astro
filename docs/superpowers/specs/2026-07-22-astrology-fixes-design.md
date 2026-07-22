# Design Document: Panchangam, Horoscope Diagnostics, Mobile UI, & North Indian Chart Fixes

**Date:** 2026-07-22  
**Status:** Approved by User  

---

## 1. Overview & Objectives

This design addresses 13 reported issues across the application:
1. **Panchangam Elements**: Suppress "next" element calculations when the current element extends past next sunrise.
2. **Chandrastamam**: Fix sign calculation formula (8th house from transit Moon) and show specific affected birth nakshatras.
3. **Sevvai (Manglik) Dosham**: Base primary detection on Lagna and apply comprehensive traditional nullifications to eliminate false positives.
4. **Rajayogams**: Implement Dharma-Karmadhipati Yoga, Kendra-Trikona Rajayogam, and fully localize Neechabhanga Rajayogam across all 6 languages.
5. **Horoscope Ayanamsa**: Include selected Ayanamsa in `ChartUiResponseDTO` and display it in the UI header.
6. **Dosham Severity**: Use backend-localized `dosha.severity` directly in frontend components.
7. **Mobile-First Responsive Layout**: Ensure all pages, tables, tabs, and headers scale cleanly on mobile devices without horizontal scrolling or text clipping.
8. **UI Navigation Theme**: Align nav-rail, bottom nav, and language cards with the warm cream/saffron design system.
9. **Chart Borders & Highlighting**: Make grid lines visible and highlight selected houses with a distinct 4-sided border.
10. **North Indian Diamond Chart**: Implement full planet placement logic for the North Indian diamond layout.

---

## 2. Component Design & Changes

### 2.1 Backend Panchangam Logic (`DailyPanchangamServiceImpl.java`)
- Update `buildThithiDTO`, `buildNakshatraDTO`, `buildYogamDTO`, and `buildKaranamDTO` to accept `jdNextSunrise`.
- If `endJd >= jdNextSunrise`, set `nextName`, `nextLocalized`, and `nextEndTime` to `null`.
- Fix Chandrastamam sign calculation: `int chandrastamamSign = (rashiNum + 7 - 1) % 12 + 1;` (8th sign counting forward from current Moon sign).

### 2.2 Backend Diagnostics (`AstrologyDiagnosticsService.java` & `PlanetDignityUtils.java`)
- **Sevvai (Manglik) Dosham**:
  - Primary check from Lagna (houses 1, 2, 4, 7, 8, 12).
  - Nullifications:
    - Own sign (Aries, Scorpio) or Exalted (Capricorn).
    - Aspect/conjunction by Jupiter or Venus.
    - 2nd house in Gemini/Virgo; 4th in Aries/Scorpio; 7th in Cancer/Capricorn; 8th in Sagittarius/Pisces; 12th in Taurus/Libra.
    - Cancer/Leo sign placement.
- **Rajayogams**:
  - **Dharma-Karmadhipati Yoga**: 9th Lord and 10th Lord conjunct or in mutual aspect.
  - **Kendra-Trikona Rajayogam**: Kendra Lord (1, 4, 7, 10) and Trikona Lord (5, 9) conjunct or in mutual aspect.
  - **Neechabhanga Rajayogam**: Replace hardcoded English strings with `ts.getLabel("yoga.neechabhanga")` and `ts.getLabel("yoga.neechabhanga.desc")`. Add i18n keys for English, Tamil, Hindi, Kannada, Telugu, Malayalam.

### 2.3 Backend Horoscope DTO & Service (`ChartUiResponseDTO.java` & `ChartOrchestrationService.java`)
- Add `private String ayanamsa;` to `ChartUiResponseDTO`.
- Populate `ayanamsa` in `convertToUiDashboardResponse()`.

### 2.4 Frontend Mobile UI & Responsive Styling (`index.css`)
- Set `.tabs-header` to `overflow-x: auto`, `-webkit-overflow-scrolling: touch`, `scrollbar-width: none`.
- Adjust font sizes and padding for `.tab-btn`, `.card`, `.dasa-header`, `.horai-table th/td` on mobile screens (`@media (max-width: 768px)`).
- Update `.nav-rail`, `.bottom-nav`, and `.lang-card` backgrounds from `#0b0b14`/`#121224` to `#ffffff` / `#fffaf4` with saffron borders and active indicators.

### 2.5 Chart Rendering (`IndianChart.jsx`)
- Grid lines stroke: `#c8b89a` with `strokeWidth="1.5"`.
- Selected house rect: drawn last with `strokeWidth="3"` and saffron stroke `#ff6b00`.
- North Indian Diamond Chart (`renderNorthIndian()`): Implement 12 triangular/diamond house regions with Lagna in House 1 (top center) counting counter-clockwise, rendering planet short names in their respective houses.

---

## 3. Verification Plan

- Run `npm run build` in `frontend/` to confirm zero build errors.
- Test Panchangam page for date crossing next sunrise.
- Verify Sevvai Dosham and Rajayogams on test horoscopes.
- Test mobile view responsiveness and Hindi North Indian diamond chart.
