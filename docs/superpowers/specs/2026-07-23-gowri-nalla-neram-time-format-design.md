# Design Specification: Gowri Nalla Neram Time Formatting & Alignment Engine

## 1. Overview
This specification details the design for interval-based time slot formatting and placement in the Gowri Panchangam and standard Panchangam time slot cards (`renderTimeSlotList`) within `PanchangamPage.jsx`.

The system ensures that Next Day indicators (`(அடுத்த நாள்)`, `(Next Day)`, `(اگلے دن)`, `(अगले दिन)`, etc.) are triggered **only when a time slot crosses 12:00 AM (midnight)**, leaving all pre-midnight daytime and evening slots clean. Furthermore, time ranges are vertically centered and right-aligned with flex wrapping support.

---

## 2. Requirements & Capabilities

1. **Selective Midnight Next-Day Indicator**:
   - Pre-midnight time points (daytime and evening slots before 12:00 AM) carry no Next Day tag.
   - Transition end times (crossing 12:00 AM, where `endMins < startMins`) trigger the `isOvernight` flag and carry the Next Day tag on the end timestamp.
   - Post-midnight time points (occurring after midnight transition before next sunrise) carry Next Day tags on both start and end timestamps.

2. **Unified Slot Formatting**:
   - Reusable helper `formatSlotListTimes(slots, nextDayText)` used by both Gowri Panchangam slots (`renderTimeSlotList`) and Nakshatra Yogam slots (`renderNakshatraYogamsList`).

3. **UI Layout & Placement Correction**:
   - Vertically center time range badges (`alignSelf: 'center'`) alongside title/description labels.
   - Right-aligned (`marginLeft: 'auto'`), with responsive wrapping to prevent overflow on mobile.

---

## 3. Data Model & Formatting Algorithm

### `formatSlotListTimes(slots, nextDayText)` Algorithm

For each slot `s` in `slots`:
1. Parse `startMins` and `endMins` in minutes from midnight (0 to 1439).
2. Maintain `isOvernight` flag initialized to `false`.
3. Determine `startIsNextDay`:
   - `true` if `isOvernight` is already true, or `startStr` contains next-day keywords.
   - If `startIsNextDay` is true, set `isOvernight = true`.
4. Determine `endIsNextDay`:
   - `true` if `isOvernight` is true, or `endStr` contains next-day keywords.
   - `true` if `startMins >= 0 && endMins >= 0 && endMins < startMins` (slot crosses 12:00 AM midnight).
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

1. **Unit Verification**: Test `formatSlotListTimes` with exact Gowri sequence:
   - `05:52 AM - 07:28 AM` $\rightarrow$ `05:52 AM - 07:28 AM` *(Clean)*
   - `07:28 AM - 09:03 AM` $\rightarrow$ `07:28 AM - 09:03 AM` *(Clean)*
   - `10:51 PM - 12:15 AM` $\rightarrow$ `10:51 PM - 12:15 AM (அடுத்த நாள்)` *(Transition)*
   - `12:15 AM - 01:39 AM` $\rightarrow$ `12:15 AM (அடுத்த நாள்) - 01:39 AM (அடுத்த நாள்)` *(Post-midnight)*
2. **Frontend Build**: Run `npm run build` in `frontend/` to ensure zero compilation errors.
