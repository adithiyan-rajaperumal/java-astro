# Design Specification: Nakshatra-Vara Yogam Timeline Engine

## 1. Overview
The **Nakshatra-Vara Yogam** engine calculates interval-based astrological Yogams (*Amrita*, *Siddha*, *Marana*, and *Prabalarishta Yogam*) derived from the combination of the prevailing Day of the Week (Vara) and the active Nakshatra (Star) between Sunrise and the following Sunrise.

Because Nakshatras transition mid-day, the system outputs time-bounded intervals showing exact start and end timestamps along with localized labels and color-coded UI badges.

---

## 2. Requirements & Capabilities

1. **4 Nakshatra-Vara Yogam Categories**:
   - 🟢 **Amrita Yogam (அமிர்த யோகம்)**: Supreme auspiciousness for all endeavors.
   - 🟢 **Siddha Yogam (சித்த யோகம்)**: Highly auspicious for business, work, and education.
   - 🔴 **Marana Yogam (மரண யோகம்)**: Inauspicious; avoid starting major new events.
   - 🔴 **Prabalarishta Yogam (பிரபலாரிஷ்ட யோகம்)**: Severe inauspicious combination; extreme caution.

2. **Interval-Based Timeline Engine**:
   - Calculates exact transition points between `jdSunrise` and `jdNextSunrise`.
   - Maps each active Nakshatra window to its corresponding Nakshatra-Vara Yogam.

3. **Multi-Language Synchronization (6 Languages)**:
   - Full native translation across English, Tamil, Hindi, Kannada, Telugu, and Malayalam in both backend resource bundles (`messages*.properties`) and frontend dictionaries (`translations.js`).

4. **UI Integration (`PanchangamPage.jsx`)**:
   - Dedicated card displaying color-coded time slot bars for each active Yogam period.

---

## 3. Data Model & Architecture

### Backend Changes

1. **`PanchangamResponseDTO.java`**:
   - Add field: `private List<TimeSlotDTO> nakshatraYogams;`

2. **`DailyPanchangamServiceImpl.java`**:
   - Add Nakshatra-Vara lookup matrix `NAKSHATRA_VARA_YOGAMS[7][27]`:
     - Index 0: Sunday (`0=Sun`, `1=Mon`, `2=Tue`, `3=Wed`, `4=Thu`, `5=Fri`, `6=Sat`)
     - Index 1: Nakshatra (0=Ashwini to 26=Revati)
   - Method `calculateNakshatraYogams(double jdSunrise, double jdNextSunrise, int dayOfWeek, List<PanchangamElementDTO> nakshatraList, ZoneId zoneId)`:
     - Iterates through daily Nakshatra spans.
     - Performs lookup in `NAKSHATRA_VARA_YOGAMS`.
     - Returns `List<TimeSlotDTO>` chronologically sorted.

---

## 4. Multi-Language Key Mapping

| Language | Amrita Yogam | Siddha Yogam | Marana Yogam | Prabalarishta Yogam |
|---|---|---|---|---|
| **English (`en`)** | Amrita Yogam | Siddha Yogam | Marana Yogam | Prabalarishta Yogam |
| **Tamil (`ta`)** | அமிர்த யோகம் | சித்த யோகம் | மரண யோகம் | பிரபலாரிஷ்ட யோகம் |
| **Hindi (`hi`)** | अमृत योग | सिद्ध योग | मरण योग | प्रबलारिष्ट योग |
| **Kannada (`kn`)** | ಅಮೃತ ಯೋಗ | ಸಿದ್ಧ ಯೋಗ | ಮರಣ ಯೋಗ | ಪ್ರಬಲಾರಿಷ್ಟ ಯೋಗ |
| **Telugu (`te`)** | అమృత యోగం | సిద్ధ యోగం | మరణ యోగం | ప్రబలారిష్ట యోగం |
| **Malayalam (`ml`)** | അമൃത യോഗം | സിദ്ധ യോഗം | മരണ യോഗം | പ്രബലാരിഷ്ട യോഗം |

---

## 5. Verification Plan

1. **Backend Tests**: Run unit tests verifying `calculateNakshatraYogams` for Thursday (Marana/Siddha transitions) and Friday.
2. **Frontend Build**: Verify `npm run build` with 0 warnings/errors.
3. **UI Verification**: Ensure green/red badges display with high contrast and exact timestamps.
