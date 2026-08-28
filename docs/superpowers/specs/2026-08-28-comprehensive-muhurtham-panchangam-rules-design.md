# Advanced Vedic & Thirukanitham Auspicious Day (Subha Muhurtham) Engine - Design Spec

## Overview
Enhance the daily Panchangam calculation engine to comprehensively account for Vedic and Thirukanitham astrological doshas and transitions when determining auspicious days (Subha Muhurtham) and timings, without altering unrelated services.

## Scope & Constraints
- **Strict Isolation**: Only modify:
  - `src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java`
  - `src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java`
  - `src/main/resources/i18n/messages_*.properties` and `frontend/src/i18n/translations.js`
  - `frontend/src/pages/PanchangamPage.jsx`
- **Validation**: Implement a dedicated JUnit integration test validating 120 consecutive days for Chennai (Lat 13.0827, Lon 80.2707) to verify zero calculation errors, seamless transitions, and accurate dosha detection across seasons.

## Detailed Requirements

### 1. Nitya Yoga Mahadosha Filtering
- Evaluate the 27 Nitya Yogas calculated from `(Sun Longitude + Moon Longitude)`.
- Strictly prohibit **Vyatipata (17)** and **Vaidhriti (27)** from `isMuhurthamDay`.

### 2. Masa Sankranti Day (Sun Ingress / Masappirappu) Exclusion
- Detect whether the Sun transitions into a new zodiac sign during the day (between sunrise today and sunrise tomorrow).
- If the day contains a Solar Ingress (*Sankranti Punya Kala*), mark `isSankrantiDay = true` and exclude from Subha Muhurtham (`isMuhurthamDay = false`) due to Sankranti Dosha.
- Add `sankrantiDay: boolean` to `DailyPanchangamDTO`.

### 3. Guru & Sukra Moudhya (Planetary Combustion / Asthamanam)
- Query Swiss Ephemeris for Sun, Jupiter (Guru), and Venus (Sukra) longitudes at Sunrise.
- **Guru Moudhya**: $|Sun - Jupiter| < 11.0^\circ$.
- **Sukra Moudhya**: $|Sun - Venus| < 8.0^\circ$.
- Add `guruMoudhya: boolean` and `sukraMoudhya: boolean` to `DailyPanchangamDTO`.
- Factor into overall subha muhurtham or display dedicated caution badges in UI.

### 4. Thithi Soonya (Dagda / Void Rashi) Validation
- Map canonical Dagda Rashis for all 30 Thithis:
  - Dvitiya: Dhanus (9), Meena (12)
  - Tritiya: Simha (5), Makara (10)
  - Chaturthi: Kumbha (11), Vrishabha (2)
  - Panchami: Mithuna (3), Kanya (6)
  - Shashthi: Mesha (1), Simha (5)
  - Saptami: Dhanus (9), Kataka (4)
  - Ashtami: Mithuna (3), Kanya (6)
  - Navami: Simha (5), Vrischika (8)
  - Dashami: Simha (5), Vrischika (8)
  - Ekadashi: Dhanus (9), Meena (12)
  - Dvadashi: Tula (7), Makara (10)
  - Trayodashi: Vrishabha (2), Simha (5)
  - Chaturdashi: Mithuna (3), Kanya (6), Dhanus (9), Meena (12)
  - Prathama: Tula (7), Makara (10)
- Add `thithiSoonya: boolean` to `DailyPanchangamDTO`.

### 5. Transition-Aware Muhurtham Time Window
- Calculate the exact start and end times when Thithi, Nakshatra, and Yogam are simultaneously auspicious.
- Add `muhurthamWindow: String` (e.g., `"09:15 AM - 02:30 PM"`, `"Throughout the day"`, or `"After 10:45 AM"`) to `DailyPanchangamDTO`.
- In `PanchangamPage.jsx`, display the active time window and badges for Sankranti, Moudhya, and Thithi Soonya.

## Verification & 120-Day Test Plan
- Run `mvn test -Dtest=Panchangam120DaysChennaiValidationTest` spanning 120 days for Chennai coordinates.
- Ensure all panchangam calculations (Sunrise, Sunset, Gowri, Horais, Thithi, Nakshatra, Yogam, Karanam, Muhurtham, Vasthu) compute accurately without regressions or null pointer exceptions.
- Run `npm run build` in `frontend/`.
