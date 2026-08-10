# Design Specification: Daily Balan Deterministic Gochara Anchors & Horoscope Page Fixes

## 1. Problem Statement & User Directives

1. **Unified Articulated Yearly Narrative (No Fragmented Subcategory Boxes)**:
   - Rigid subcategory boxes (`Career`, `Health`, `Parents & Kids`, etc.) force repetitive filler text and cause logical anomalies (e.g. mentioning parents' health every year after age 38 even if parents have already passed away).
   - Replace fragmented subcategory boxes with a **single, rich, well-articulated yearly narrative paragraph (`detailedPrediction`)** alongside the **yearly theme**, **astrological basis**, and **cautions/remedies**.
   - As life events occur (e.g., career change, marriage, parental loss), subsequent years naturally progress in their narrative context without repetitive boilerplate.
2. **Independent Yogas & Doshams Calculation**: Do NOT feed our pre-calculated structural diagnostics (yogas/doshas) into Gemini. Gemini must calculate and discover all classical Vedic Yogas and evaluate all Doshams fresh from raw planetary matrices (D1, D2, D9, D10, D12, D30, Shadbala, Dasa) to independently verify accuracy and identify additional astrological combinations.
3. **No Offline Synthetic Fallback**: Eliminate synthetic rule-based offline fallbacks. If Gemini API is disabled, unconfigured, fails, or throws errors, return a clear, localized message: "AI prediction service is currently unavailable. Please try again later." in the chosen language (`en`, `ta`, `hi`, `te`, `kn`, `ml`).
4. **Horoscope Results Header**: Fix Ayanamsa and Panchangam system display in `HoroscopePage.jsx` results header using proper i18n lookup maps.
5. **Clean Text Sub Tabs**: Remove emoji prefixes (`🔮`, `📅`) from subtab buttons for consistent text tabs.
6. **Token & Cost Metrics**: Display total tokens, estimated cost in USD ($) and INR (₹), and model name on both AI Life Balan and Daily Balan cards.
7. **Lock Daily Balan to Today**: Remove the date picker and forecast strictly for today's system date (`LocalDate.now()`).
8. **Deterministic Gochara Anchors for Daily Balan**: Precalculate Vara Lord, Direction, Lucky Color, Lucky Number, and Auspicious Gowri Window deterministically in the backend so regenerating never fluctuates.

---

## 2. Technical Architecture & Component Changes

### A. Unified Yearly Narrative & Clean Card Schema (`PredictionResponseDTO.java`, `GeminiPredictionService.java`, `AiPredictionsView.jsx`)
- **Yearly Card Structure**:
  - `year`: Target forecast year (int)
  - `age`: Native's age in that year (int)
  - `dasaBhukthi`: Active Vimshottari Dasa & Bhukthi during that year (String)
  - `yearlyTheme`: Sharp, 1-sentence headline capturing the central life theme for the year.
  - `detailedPrediction`: A comprehensive, highly articulated narrative paragraph synthesizing the year's real-life events (career moves/job loss, financial shifts, health/surgeries, family/domestic events, bereavement if indicated, psychological mindset) with continuity across the lifespan.
  - `astrologicalBasis`: Specific planetary placements and house activations from D1/D10/D12/D30.
  - `cautionsAndRemedies`: Targeted period warnings and authentic Vedic remedial guidance.
- **Frontend Card UI (`AiPredictionsView.jsx`)**:
  - Render an elegant, premium card with:
    - Header: Year, Age, Running Dasa
    - 🎯 **Yearly Theme Headline**
    - 📖 **Articulated Prediction Narrative**
    - 🪐 **Astrological Basis** (subtle badge / callout)
    - ⚠️ **Cautions & Remedies** (actionable alert box)

### B. Fresh Gemini Astrological Synthesis & No Offline Fallback (`GeminiPredictionService.java`)
- **Exclude Pre-calculated Diagnostics**:
  - Remove `c.getStructuralDiagnostics()` from `constructAstrologicalPrompt`.
  - Pass all vargas (D1, D2, D9, D10, D12, D30), Shadbala, and Dasa timeline.
  - Instruct Gemini:
    - *"Analyze the complete planetary matrix to independently calculate ALL applicable classical Vedic Yogas (Gajakesari, Raja Yogas, Dhana Yogas, Vipareeta Raja Yoga, Budhaditya, Neechabhanga, Pancha Mahapurusha, Parivarthana, etc.) and evaluate all Doshams (Sevvai/Kuja, Kala Sarpa, Pitru, Papakarthari, Rahu-Ketu afflictions) with status, nullification factors, and authentic remedies."*
- **No Offline Fallback**:
  - If API key is missing/disabled, or if Gemini call fails, return `PredictionResponseDTO` / `DailyBalanDTO` with `enabled: false` and localized message:
    - `ta`: "AI கணிப்பு சேவை தற்போது கிடைக்கவில்லை. சிறிது நேரம் கழித்து மீண்டும் முயற்சிக்கவும்."
    - `en`: "AI prediction service is currently unavailable. Please try again later."
    - `hi`: "एआई भविष्यफल सेवा वर्तमान में उपलब्ध नहीं है। कृपया कुछ समय बाद पुनः प्रयास करें।"
    - `te`: "AI జ్యోతిష్య సేవ ప్రస్తుతం అందుబాటులో లేదు. దయచేసి కాసేపటి తర్వాత మళ్లీ ప్రయత్నించండి."
    - `kn`: "AI ಭವಿಷ್ಯ ಸೇವೆ ಪ್ರಸ್ತುತ ಲಭ್ಯವಿಲ್ಲ. ದಯವಿಟ್ಟು ಸ್ವಲ್ಪ ಸಮಯದ ನಂತರ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ."
    - `ml`: "AI പ്രവചന സേവനം ഇപ്പോൾ ലഭ്യമല്ല. ദയവായി അല്പം കഴിഞ്ഞ് വീണ്ടും ശ്രമിക്കുക."

### C. Frontend Header Display & I18n Key Normalization (`HoroscopePage.jsx`)
- Add explicit mapping dictionaries for Ayanamsa and Panchangam systems:
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

### D. Clean Text Sub Tabs (`HoroscopePage.jsx`, `translations.js`)
- Standardize sub tab labels without emoji prefixes:
  - `AI Life Balan` / `AI வாழ்நாள் பலன்கள்`
  - `Daily Balan` / `இன்றைய ராசி பலன்`

### E. Token Usage & Monetary Cost Badge (`DailyBalanView.jsx`, `AiPredictionsView.jsx`, `GeminiPredictionService.java`)
- Calculate `estimatedCostUsd` and `estimatedCostInr` in `parseDailyGeminiResponse`.
- In `AiPredictionsView.jsx` and `DailyBalanView.jsx`, display:
  - **Total Tokens** (Prompt + Completion)
  - **Estimated Cost**: `$0.0002 / ₹0.02`
  - **Model Name**: `gemini-3.6-flash`

### F. Lock Daily Balan to Today & Deterministic Gochara Engine (`DailyBalanView.jsx`, `GeminiPredictionService.java`)
- Remove `<input type="date">` selection.
- Precalculate deterministic Vara Lord, Direction, Lucky Color, Lucky Number, and Auspicious Gowri Window from target date & natal Moon sign.
- Enforce these deterministic values in `DailyBalanDTO` so regenerate never causes fluctuating astrological parameters.

---

## 3. Verification Plan

1. **Automated Unit Tests**:
   - `mvn test -Dtest=GeminiPredictionServiceTest` verifying prompt construction without precalculated diagnostics, unavailable error responses without offline synthetic fallback, deterministic daily balan attributes, and token cost computation.
   - `mvn clean test` for full regression pass.
2. **Frontend Validation**:
   - `npm run build` validating Vite compilation.
   - Check horoscope header rendering for Ayanamsa and Panchangam systems across languages.
   - Verify token and cost badges in both AI Life Balan and Daily Balan views.
   - Verify unified narrative paragraphs in yearly prediction cards.
