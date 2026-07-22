# Panchangam, Horoscope Diagnostics, Mobile UI, & North Indian Chart Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix 13 identified issues across Panchangam calculations, Sevvai/Rajayogams diagnostic engines, i18n translations, Ayanamsa DTO propagation, mobile-first responsive styling, and North Indian diamond chart rendering.

**Architecture:** 
- Backend: Update `DailyPanchangamServiceImpl` to check element end times against `jdNextSunrise` to suppress redundant next-element listings, fix Chandrastamam 8th-house math, and enhance `AstrologyDiagnosticsService` for Lagna-primary Sevvai exemptions and Rajayogam evaluations.
- Frontend: Refactor `index.css` for mobile responsiveness (`.tabs-header`, `.horai-table`, `.card`, `.nav-rail`, `.bottom-nav`), pass `report.ayanamsa` in `HoroscopePage.jsx`, and implement 12-house diamond layout in `IndianChart.jsx`.

**Tech Stack:** Java 17, Spring Boot, SwissEph (SweDate), React, Vite, CSS3.

## Global Constraints

- Preserve all existing API schemas and calculation contracts.
- Maintain localization across English (`en`), Tamil (`ta`), Hindi (`hi`), Kannada (`kn`), Telugu (`te`), and Malayalam (`ml`).
- Mobile-first responsive breakpoint: 768px.

---

### Task 1: Panchangam Next-Element Suppression & Chandrastamam Formula Fix

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java`

**Interfaces:**
- Consumes: `PanchangamElementDTO`, `jdSunrise`, `jdNextSunrise`
- Produces: Clean `DailyPanchangamDTO` with `next*` suppressed when current element extends past `jdNextSunrise`, and accurate Chandrastamam sign calculation.

- [ ] **Step 1: Update element DTO builders in DailyPanchangamServiceImpl**

Modify `buildThithiDTO`, `buildNakshatraDTO`, `buildYogamDTO`, `buildKaranamDTO` in `DailyPanchangamServiceImpl.java` to accept `double jdNextSunrise`. If `endJd >= jdNextSunrise`, set `nextName`, `nextLocalized`, and `nextEndTime` to `null`.

- [ ] **Step 2: Correct Chandrastamam sign math**

Change Chandrastamam calculation on line 222 of `DailyPanchangamServiceImpl.java`:
```java
int chandrastamamSign = (rashiNum + 7 - 1) % 12 + 1;
```

- [ ] **Step 3: Build & verify backend**

Run: `./mvnw test-compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java
git commit -m "fix(panchangam): suppress next element past sunrise and fix chandrastamam sign math"
```

---

### Task 2: Sevvai Dosham Exemptions, Rajayogams Engine, & i18n Translations

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/PlanetDignityUtils.java`
- Modify: `src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java`
- Modify: `src/main/resources/i18n/messages_*.properties` (all 6 language property files)

**Interfaces:**
- Consumes: `d1Map` (PlanetaryPosition map)
- Produces: Accurate `DiagnosticsDTO` with Sevvai exemptions, Dharma-Karmadhipati Yoga, Kendra-Trikona Rajayogam, and localized Neechabhanga Rajayogam.

- [ ] **Step 1: Add sign lord and aspect helpers to PlanetDignityUtils**

Ensure `getSignLord(int sign)` and `isAspecting` cover 4th/8th for Mars, 5th/9th for Jupiter, 3rd/10th for Saturn cleanly.

- [ ] **Step 2: Update Sevvai Dosham in AstrologyDiagnosticsService**

In `evaluateSevvaiDosham()`:
1. Primary check: `marsFromLagna == 1 || marsFromLagna == 2 || marsFromLagna == 4 || marsFromLagna == 7 || marsFromLagna == 8 || marsFromLagna == 12`.
2. Apply complete nullification rules:
   - Own sign / Exalted (Aries, Scorpio, Capricorn)
   - Conjunction or aspect from Jupiter or Venus
   - 2nd in Gemini/Virgo; 4th in Aries/Scorpio; 7th in Cancer/Capricorn; 8th in Sagittarius/Pisces; 12th in Taurus/Libra
   - Mars in Cancer (`Kadagam`) or Leo (`Simham`).

- [ ] **Step 3: Implement Dharma-Karmadhipati & Kendra-Trikona Rajayogams**

In `evaluateYogas()`:
1. Find 9th Lord and 10th Lord from Lagna. If conjunct or in 7th mutual aspect, add `Dharma-Karmadhipati Yoga`.
2. Find Kendra Lords (1, 4, 7, 10) and Trikona Lords (5, 9). If any Kendra Lord & Trikona Lord are conjunct/aspecting, add `Kendra-Trikona Rajayogam`.
3. Localize Neechabhanga Rajayogam using `ts.getLabel("yoga.neechabhanga")`.

- [ ] **Step 4: Update i18n property files**

Add `yoga.neechabhanga`, `yoga.neechabhanga.desc`, `yoga.dharma_karmadhipati`, `yoga.dharma_karmadhipati.desc`, `yoga.rajayogam`, `yoga.rajayogam.desc`, and `nullification.sevvai.*` keys to `messages_en.properties`, `messages_ta.properties`, `messages_hi.properties`, `messages_kn.properties`, `messages_te.properties`, `messages_ml.properties`.

- [ ] **Step 5: Build & verify backend**

Run: `./mvnw test-compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/org/vedic/astro/util/PlanetDignityUtils.java src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java src/main/resources/i18n/messages_*.properties
git commit -m "feat(diagnostics): refine Sevvai exemptions, add Rajayogams, and update i18n keys"
```

---

### Task 3: Horoscope Ayanamsa DTO & Severity Handling

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`
- Modify: `frontend/src/pages/HoroscopePage.jsx`

**Interfaces:**
- Consumes: `pay.ayanamsa()`
- Produces: `ChartUiResponseDTO` containing `ayanamsa` field, displayed in UI header card.

- [ ] **Step 1: Add ayanamsa field to ChartUiResponseDTO**

Add `private String ayanamsa;` to `ChartUiResponseDTO.java`.

- [ ] **Step 2: Populate ayanamsa in ChartOrchestrationService**

In `convertToUiDashboardResponse()`:
```java
.ayanamsa(pay.ayanamsa() != null ? pay.ayanamsa() : "LAHIRI")
```

- [ ] **Step 3: Update HoroscopePage.jsx**

In `HoroscopePage.jsx`:
1. Use `report.ayanamsa` in header card: `{t('ayanamsa', settings.language)}: {report.ayanamsa || settings.ayanamsa}`.
2. In `renderDiagnosticsTab()`: use `dosha.severity` directly for inactive/cancelled dosha badge display.

- [ ] **Step 4: Test frontend build**

Run: `npm run build` in `frontend/`
Expected: Vite build success

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java src/main/java/org/vedic/astro/service/ChartOrchestrationService.java frontend/src/pages/HoroscopePage.jsx
git commit -m "fix(horoscope): pass ayanamsa in ChartUiResponseDTO and use direct dosha severity string"
```

---

### Task 4: Mobile-First Responsive Layout & Navigation Theme Alignment

**Files:**
- Modify: `frontend/src/index.css`
- Modify: `frontend/src/pages/SettingsPage.jsx`

**Interfaces:**
- Consumes: CSS custom properties and media queries.
- Produces: Fully responsive layout for all screen sizes, light-themed `.nav-rail`, `.bottom-nav`, and `.lang-card`.

- [ ] **Step 1: Refactor navigation & language card themes in index.css**

1. Change `.nav-rail` background to `#ffffff` with border `#f0e2d0`.
2. Change `.bottom-nav` background to `#ffffff` with top border `#f0e2d0`.
3. Change `.lang-card` background to `#fffaf4` with hover/active saffron border.

- [ ] **Step 2: Add mobile responsive rules for tabs, tables, and cards**

1. Add `overflow-x: auto`, `-webkit-overflow-scrolling: touch`, `scrollbar-width: none` to `.tabs-header`.
2. Add `@media (max-width: 768px)` rules:
   - Reduce `.card` padding to `14px 12px`.
   - Set `.tab-btn` font-size to `14px` and padding to `8px 10px`.
   - Set `.horai-table th, .horai-table td` padding to `8px 10px` and font-size to `12.5px`.
   - Flex wrap header action buttons vertically when width < 480px.

- [ ] **Step 3: Test frontend build**

Run: `npm run build` in `frontend/`
Expected: Vite build success

- [ ] **Step 4: Commit**

```bash
git add frontend/src/index.css frontend/src/pages/SettingsPage.jsx
git commit -m "style(mobile): mobile-first responsive layout and light-themed navigation"
```

---

### Task 5: Chart Grid Borders & North Indian Diamond Chart Renderer

**Files:**
- Modify: `frontend/src/components/IndianChart.jsx`

**Interfaces:**
- Consumes: `positions` array, `style` ('south' or 'north'), `lang`, `title`
- Produces: High-contrast South Indian grid with highlighted house selection, and full North Indian diamond chart with 12 triangular house regions and planet placements.

- [ ] **Step 1: Enhance South Indian chart grid & selection highlighting**

In `IndianChart.jsx`:
1. Change grid lines `stroke` to `#c8b89a` with `strokeWidth="1.5"`.
2. Render selected house rect after all cell rects with `stroke="#ff6b00"` and `strokeWidth="3"` for complete 4-sided highlighting.

- [ ] **Step 2: Implement full North Indian diamond chart renderer**

In `renderNorthIndian()`:
1. Define 12 triangular/diamond house paths & center coordinates inside `viewBox="0 0 400 400"`.
2. Place Lagna in House 1 (top center diamond).
3. Number houses counter-clockwise starting from House 1: `House N = ((sign - lagnaSign + 12) % 12) + 1`.
4. Group planets by house number and render short names with dignity colors inside their respective house regions.
5. Add click interaction for house selection & aspect highlights.

- [ ] **Step 3: Test frontend build**

Run: `npm run build` in `frontend/`
Expected: Vite build success

- [ ] **Step 4: Commit**

```bash
git add frontend/src/components/IndianChart.jsx
git commit -m "feat(chart): implement North Indian diamond chart planet placement and enhance South Indian borders"
```

---

## Verification Plan

### Automated Build Verification
1. Backend compile: `./mvnw test-compile` in root
2. Frontend bundle: `npm run build` in `frontend/`

### Manual Verification
1. **Panchangam Page**: Verify elements ending after sunrise show no "next" item.
2. **Chandrastamam**: Confirm correct 8th transit sign and affected nakshatras.
3. **Horoscope Diagnostics**: Verify Sevvai Dosham exemptions, Dharma-Karmadhipati Yoga, and localized Neechabhanga Rajayogam in Tamil.
4. **Mobile View**: Inspect on mobile viewport width (375px) — tabs scroll smoothly, tables render without page horizontal scroll.
5. **North Indian Chart**: Set language to Hindi or toggle North chart — verify diamond houses display planets cleanly.
