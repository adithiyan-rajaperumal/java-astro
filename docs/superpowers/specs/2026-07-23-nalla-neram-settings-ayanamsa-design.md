# Design: Nalla Neram Fix & Settings Consolidation

## 1. Nalla Neram Inauspicious Window Exclusion

### Problem
`calculateNallaNeram()` uses fixed-hour offsets from sunrise without checking for overlaps with Rahu Kalam, Yamagandam, and Kulikai. Today (Wednesday), the morning Nalla Neram `09:07 AM - 10:07 AM` falls entirely within Kulikai `09:03 AM - 10:39 AM`.

### Solution
After computing the Nalla Neram baseline slots, subtract overlapping inauspicious windows:

1. Compute Rahu Kalam, Yamagandam, and Kulikai time ranges as `ZonedDateTime` start/end pairs.
2. For each Nalla Neram slot, check overlap with each inauspicious window.
3. If overlap exists:
   - **Fully contained**: Drop the slot entirely.
   - **Partial overlap at start**: Trim start to inauspicious end.
   - **Partial overlap at end**: Trim end to inauspicious start.
   - **Inauspicious spans middle**: Split into two sub-slots (before and after).
4. Drop any resulting sub-slot shorter than 5 minutes.

### Files Modified
- `DailyPanchangamServiceImpl.java`: Add `subtractInauspiciousWindows()` helper, call it after `calculateNallaNeram()`.

---

## 2. Global Language from Settings (Remove from BirthForm)

### Problem
If a user generates a horoscope in Tamil, then changes language to English in Settings, the existing results page still shows Tamil labels — creating UI inconsistency.

### Solution
- **Language** is global from Settings only. No language selector in BirthForm or individual pages.
- API calls always pass `settings.language` as the `Accept-Language` header.
- When `settings.language` changes, clear any cached horoscope/matching result state to force re-generation.

### Files Modified
- `HoroscopePage.jsx`: Add `useEffect` to clear `report` state when `settings.language` changes.
- `MatchingPage.jsx`: Add `useEffect` to clear matching result state when `settings.language` changes.

---

## 3. Ayanamsa in Matching API & UI/PDF Display

### Problem
- Matching page does not pass ayanamsa in the API request.
- Ayanamsa name is not displayed in the Horoscope results UI.

### Solution

#### 3a. Pass Ayanamsa in Matching API
- `MatchingPage.jsx`: Include `ayanamsa` from BirthForm in the matching API payload.
- `MatchingController.java`: Ensure ayanamsa from the request payload is forwarded to the calculation engine.

#### 3b. Display Ayanamsa in Horoscope Results UI
- `HoroscopePage.jsx`: Show the selected Ayanamsa name in the results header area.

#### 3c. Include Ayanamsa in PDF Report
- `PdfExportService.java`: Already includes `pdf.info.ayanamsa` label — verify it uses the correct ayanamsa key from the request payload.

---

## 4. Verification Plan

### Automated
- Verify `npm run build` succeeds with 0 errors.
- Verify Render deployment build succeeds.

### Manual
- **Nalla Neram**: Check Wednesday Panchangam — morning Nalla Neram should NOT overlap with Kulikai.
- **Language**: Generate horoscope in Tamil, change language to English, verify results are cleared.
- **Ayanamsa in Matching**: Submit matching with KP, verify payload includes `ayanamsa: "KP"`.
- **Ayanamsa in UI**: Verify Ayanamsa name displays in horoscope results.
