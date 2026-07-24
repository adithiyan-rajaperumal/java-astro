# Mobile Panchangam Image Share Specification (v4 - Layout Refinements)

## Overview
This document specifies the updated design refinements for the Panchangam Share Card image to fix line-wrapping, balance column heights, replace cryptic `H1`-`H24` labels with clear Horai time intervals, and style Gowri Nalla Neram as a presentable mini-table.

## User Intent & Requirements
1. **No AM/PM Line Break**: Apply `whiteSpace: 'nowrap'` on Sunrise, Sunset, Moonrise, and Moonset timestamps so `05:53 AM` / `06:37 PM` never wraps onto a new line in Tamil or any language.
2. **Auspicious Timings Column Balance**:
   - **Left Column**: Nalla Neram + Abhijit Muhurtham + Nakshatra Vara Yogam (🌟).
   - **Right Column**: Gowri Nalla Neram (styled as a structured mini-table/card list).
3. **Common-Person Friendly 24 Horai Table**:
   - **Remove `H1`...`H24` labels**.
   - Show exact start & end time intervals (e.g. `05:53 AM - 06:56 AM`) and localized Planet names (e.g. `🌕 சந்திரன் (Moon)`) in a clean 2-column or 3-column table grid.
4. **Presentable Gowri & Nalla Neram Mini-Tables**:
   - Format time slots into structured mini-tables with clear time badges and quality icons.

---

## Technical Approach & Architecture

### `PanchangamShareCard` Layout Updates
- Container Width: `1080px`.
- Sun & Moon Times: `display: flex; justify-content: center; gap: 20px; whiteSpace: nowrap`.
- Auspicious Timings Grid:
  - Left Col: `<NallaNeramBlock>`, `<AbhijitBlock>`, `<NakshatraYogamBlock>`.
  - Right Col: `<GowriNallaNeramTable>`.
- 24 Horai Grid: `display: grid; gridTemplateColumns: 1fr 1fr; gap: 8px`. Each cell displays:
  - Time range: `05:53 AM - 06:56 AM`
  - Planet badge: `🌕 சந்திரன்`

---

## Verification Plan
1. Build frontend bundle and verify no line breaks in Sun/Moon times.
2. Verify left and right auspicious columns are vertically balanced.
3. Verify 24 Horai table displays full time ranges instead of `H1`-`H24`.
