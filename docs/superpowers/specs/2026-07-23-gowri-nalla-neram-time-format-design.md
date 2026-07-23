# Design Specification: Gowri Nalla Neram Time Formatting & Alignment Engine

## 1. Overview
This specification details the design for interval-based time slot formatting and placement in the Gowri Panchangam and standard Panchangam time slot cards (`renderTimeSlotList`) within `PanchangamPage.jsx`.

The system ensures that Next Day indicators (`(அடுத்த நாள்)`, `(Next Day)`, `(अगले दिन)`, etc.) are attached **exclusively to time points that cross or occur after 12:00 AM (midnight)**, leaving pre-midnight slots clean. Furthermore, time ranges are vertically centered and right-aligned with flex wrapping support.

---

## 2. Requirements & Capabilities

1. **Selective Midnight Next-Day Indicator**:
   - Pre-midnight time points (before 12:00 AM) carry no Next Day tag.
   - Transition end times (crossing 12:00 AM) carry the Next Day tag.
   - Post-midnight time points (start & end after 12:00 AM before next sunrise) carry Next Day tags on both timestamps.

2. **Unified Slot Formatting**:
   - Reusable helper `formatSlotListTimes(slots, nextDayText, nextDayKeywords)` used by both Gowri Panchangam slots (`renderTimeSlotList`) and Nakshatra Yogam slots (`renderNakshatraYogamsList`).

3. **UI Layout & Placement Correction**:
   - Vertically center time range badges (`alignSelf: 'center'`) alongside title/description labels.
   - Right-aligned (`marginLeft: 'auto'`), with responsive wrapping to prevent overflow on mobile.

---

## 3. Data Model & Formatting Algorithm

### `formatSlotListTimes(slots, nextDayText)` Algorithm

For each slot `s` in `slots`:
1. Parse `startMins` and `endMins` in minutes from midnight (0 to 1439).
2. Maintain `isOvernight` flag across chronological slot list.
3. Determine `startIsNextDay`:
   - `true` if `isOvernight` is true, or `startStr` contains next-day keywords.
   - `true` if `idx > 0` and `startMins` is early morning (00:00 to 08:30 AM).
   - If `startIsNextDay` is true, set `isOvernight = true`.
4. Determine `endIsNextDay`:
   - `true` if `isOvernight` is true, or `endStr` contains next-day keywords.
   - `true` if `endMins < startMins` (midnight crossing).
   - `true` if `startMins >= 12:00 PM` and `endMins <= 08:30 AM`.
   - If `endIsNextDay` is true, set `isOvernight = true`.
5. Format times:
   - `formattedStart = startIsNextDay ? "${s.start} (${nextDayText})" : s.start`
   - `formattedEnd = endIsNextDay ? "${s.end} (${nextDayText})" : s.end`

---

## 4. Multi-Language Key Support

Uses the existing `nextDay` key from `translations.js`:
- **English (`en`)**: `Next Day`
- **Tamil (`ta`)**: `அடுத்த நாள்`
- **Hindi (`hi`)**: `अगले दिन`
- **Kannada (`kn`)**: `ಮುಂದಿನ ದಿನ`
- **Telugu (`te`)**: `తరువాత రోజు`
- **Malayalam (`ml`)**: `അടുത്ത ദിവസം`

---

## 5. Verification Plan

1. **Unit Verification**: Test `formatSlotListTimes` with sample Gowri slots:
   - `06:00 AM - 07:30 AM` $\rightarrow$ `06:00 AM - 07:30 AM`
   - `10:30 PM - 12:00 AM` $\rightarrow$ `10:30 PM - 12:00 AM (அடுத்த நாள்)`
   - `12:00 AM - 01:30 AM` $\rightarrow$ `12:00 AM (அடுத்த நாள்) - 01:30 AM (அடுத்த நாள்)`
2. **Frontend Build**: Run `npm run build` in `frontend/` to ensure zero compilation errors.
