# Daily Panchangam and User Preferences Design

## 1. Overview
The goal is to implement a Daily Panchangam feature that calculates exact astrological timings for any given day and a user-specified base location. Additionally, a new global Settings/Preferences UI will be introduced to manage the user's preferred location, language, ayanamsam, and future calculation engine toggles.

## 2. Architecture & Data Flow
- **Global Settings**: 
  - A new Settings page/tab where users configure:
    - Base Location (using existing Photon API location search)
    - Language (en, hi, ta, te, kn, ml)
    - Ayanamsam (Lahiri, Raman, KP, etc.)
    - Engine (SwissEph, future placeholder for pyJhora)
  - These preferences will be stored persistently on the frontend (e.g., `localStorage`).
- **Daily Panchangam Engine (`DailyPanchangamService`)**:
  - A backend service that uses `DrikPanchangamEngine` (SwissEph) to calculate astrological phenomena dynamically for the provided date, latitude, longitude, timezone, and ayanamsam.
- **API Endpoint**:
  - `GET /api/panchangam/daily`
  - Query Params: `date` (YYYY-MM-DD), `lat`, `lon`, `ayanamsa`, `timezone`
  - Response payload: `DailyPanchangamDTO` containing calculated values with localized strings based on the client's language preference.

## 3. Panchangam Calculations
Calculations will be performed dynamically based on exact local Sunrise and Sunset to ensure maximum accuracy:
- **Sunrise & Sunset**: Calculated exactly for the base location.
- **Thithi, Nakshatra, Yogam, Karanam**: Start and exact end-times calculated by tracking longitudinal differences between the Sun and Moon.
- **Raghu Kalam, Emagandam, Kulikai**: Calculated using proportional daytime/nighttime division rules (based on sunrise/sunset).
- **Horais**: Exact planetary hour timings based on sunrise.
- **Good Time (Nalla Neram) & Gowri Good Time**: Fixed day/night rules adjusted to sunrise.
- **Chandrastamam, Nethram, Jeevan**: Derived from the current day's Moon position (Chandrastamam displays the Rashi currently experiencing it).
- **Muhurtham / Vasthu Day**: Highlighted if applicable for the given date.

## 4. UI Components
- **Settings/Preferences Tab**: A dedicated section to manage global state.
- **Panchangam View**: 
  - Date Picker, defaulting to today.
  - "Previous Day" and "Next Day" navigation buttons.
  - A clean, grid-based layout displaying all Panchangam metrics.
  - If a user has not configured a base location, they will be prompted to do so before the Panchangam can be calculated.

## 5. Future Considerations
- The architecture will be kept decoupled so that the SwissEph engine can be easily swapped with a `PyJhoraEngine` in the future based on user preferences.
