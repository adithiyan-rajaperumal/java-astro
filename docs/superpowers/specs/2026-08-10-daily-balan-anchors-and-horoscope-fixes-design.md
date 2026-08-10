# Design Specification: Daily Balan Deterministic Gochara Anchors & Horoscope Page Fixes

## 1. Problem Statement

1. **Horoscope Results Header**: The selected Ayanamsa and Panchangam system are either broken or omitted in the horoscope results summary due to enum-to-i18n key casing mismatches (`LAHIRI` vs `ayanamsaLahiri`).
2. **Sub Tab Images/Emojis**: The subtab headers for AI Balan and Daily Balan currently contain emoji prefixes (`🔮`, `📅`) unlike the standard clean text tabs.
3. **Token & Cost Metrics**: Users cannot see the token count and monetary amount (USD & INR) in both AI Life Balan and Daily Balan cards.
4. **Daily Balan Date Selection**: Daily prediction should strictly represent **today's forecast** for the current system date rather than allowing arbitrary historical/future date picking.
5. **Daily Balan Regeneration Inconsistencies**: Regenerating Daily Balan produces fluctuating lucky numbers, colors, and favorable directions because LLMs hallucinate these values unless anchored to deterministic Vedic Gochara rules.

---

## 2. Technical Architecture & Component Changes

### A. Frontend Header Display & I18n Key Normalization (`HoroscopePage.jsx`)
- Introduce explicit mapping dictionaries for Ayanamsa and Panchangam systems:
  ```javascript
  const AYANAMSA_I18N_MAP = {
    'LAHIRI': 'ayanamsaLahiri',
    'KP': 'ayanamsaKP',
    'RAMAN': 'ayanamsaRaman',
    'SURYA_SIDDHANTA': 'ayanamsaSurya',
    'PUSHYAPAKSHA': 'ayanamsaPushyapaksha'
  };
  const PANCHANGAM_I18N_MAP = {
    'DRIK_TIRUKANITHAM': 'panchangamThirukanitham',
    'VAKYAM': 'panchangamVakyam',
    'SURYA_SIDDHANTA': 'panchangamSurya'
  };
  ```
- Display both **Ayanamsa** and **Panchangam System** in the results header card alongside Lagna, Rasi, and Nakshatra.

### B. Clean Text Sub Tabs (`HoroscopePage.jsx`, `translations.js`)
- Standardize sub tab labels without emoji prefixes:
  - `AI Life Balan` / `AI வாழ்நாள் பலன்கள்`
  - `Daily Balan` / `இன்றைய ராசி பலன்`
- Keep UI consistent across all tabs (`Charts`, `Dasa`, `Shadbala`, `Diagnostics`, `AI Life Balan`, `Daily Balan`).

### C. Token Usage & Monetary Cost Badge (`DailyBalanView.jsx`, `AiPredictionsView.jsx`, `GeminiPredictionService.java`)
- In `GeminiPredictionService.java`, calculate `estimatedCostUsd` and `estimatedCostInr` in `parseDailyGeminiResponse` (matching `parseGeminiResponse`).
- In `AiPredictionsView.jsx` and `DailyBalanView.jsx`, display:
  - **Total Tokens** (Prompt + Completion)
  - **Estimated Cost**: `$0.0002 / ₹0.02`
  - **Model Name**: `gemini-3.6-flash`

### D. Lock Daily Balan to Today (`DailyBalanView.jsx`)
- Remove the `<input type="date">` selection.
- Generate forecasts strictly for `today` (the current date in local system time).

### E. Deterministic Gochara Engine (`GeminiPredictionService.java`)
- Implement deterministic computation of:
  1. **Transit Day Lord (Vara Adhipati)**:
     - Sunday (Sun): Direction = East, Color = Ruby Red / Orange, Number = 1.
     - Monday (Moon): Direction = North-West, Color = Pearl White / Silver, Number = 2.
     - Tuesday (Mars): Direction = South, Color = Coral Red / Crimson, Number = 9.
     - Wednesday (Mercury): Direction = North, Color = Emerald Green, Number = 5.
     - Thursday (Jupiter): Direction = North-East, Color = Gold / Yellow, Number = 3.
     - Friday (Venus): Direction = South-East, Color = Silk White / Cream, Number = 6.
     - Saturday (Saturn): Direction = West, Color = Navy Blue / Dark Blue, Number = 8.
  2. **Gochara Moon House Position**:
     - Compute the house distance of the transit Moon from the native's Janma Rasi (1st to 12th house).
  3. **Auspicious Time Window (உத்தம நேரம்)**:
     - Derive deterministic auspicious Gowri Nalla Neram / Abhijit Muhurtha for that weekday.
- Enforce these deterministic values in `DailyBalanDTO` and pass them to Gemini as fixed anchors so that regenerations never mutate the astrological direction, color, number, or time window.

---

## 3. Verification Plan

1. **Automated Unit Tests**:
   - `mvn test -Dtest=GeminiPredictionServiceTest` verifying deterministic daily balan attributes and token cost computation.
   - `mvn clean test` for full regression pass.
2. **Frontend Validation**:
   - `npm run build` validating Vite compilation.
   - Check horoscope header rendering for Ayanamsa and Panchangam systems across languages.
   - Verify token and cost badges in both AI Life Balan and Daily Balan views.
