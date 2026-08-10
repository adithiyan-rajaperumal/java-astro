# Design Specification: Matching Page Mobile UX, Complete i18n, and AI PDF Integration

## 1. Overview & Problem Statement
Users identified three usability and functional issues on the Marriage Matching page:
1. **Mobile Typography & Layout**: Fonts, score gauge (`.score-circle`), and table layouts feel oversized and clunky on small screens (< 600px).
2. **Hardcoded English Titles & Options**: Methodology options (*Ashta Koota* / *Dasa Porutham*), strictness dropdown (*Lenient*, *Moderate*, *Strict*), loading messages, error prompts, exception labels, and AI domain metadata are hardcoded in English instead of using the active application language.
3. **AI Matching Missing in Generated PDF**: Even when AI matching is enabled in `application.yml` and the UI, the downloaded PDF report does not include the AI compatibility section due to language header mismatches and lack of on-demand AI attachment in `MatchingController.java`.

---

## 2. Technical Architecture & Proposed Changes

### 2.1 Mobile-First UI & Responsive Typography
- **`frontend/src/index.css`**:
  - Add `@media (max-width: 600px)` rules for `.matching-header`, `.score-circle`, `.score-circle .number`, `.verdict-badge`.
  - `.score-circle`: scaled to `90px x 90px`, `border: 4px solid`, number font scaled to `22px`.
  - `.verdict-badge`: scaled to `13px` font with `4px 12px` padding.
  - Card padding and font sizes tuned for small mobile viewports.
- **`frontend/src/components/AiMatchingView.jsx`**:
  - Grid minmax changed from `320px` to `repeat(auto-fit, minmax(260px, 1fr))` to avoid off-screen overflow on 360–390px phones.
  - Adjusted mobile typography and line heights.
- **`frontend/src/pages/MatchingPage.jsx`**:
  - Ensure Koota/Porutham table is wrapped in a responsive, scroll-friendly container with compact mobile padding.

### 2.2 Complete Multilingual (i18n) Synchronization
- **`frontend/src/i18n/translations.js`**:
  Add keys across all 6 languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`):
  - `ashtaKoota`: e.g. "அஷ்ட கூடம் (36 புள்ளிகள் முறை)"
  - `dasaPorutham`: e.g. "தசப் பொருத்தம் (தென்னிந்திய 10 பொருத்தங்கள்)"
  - `strictnessLenient`: e.g. "எளிதானது (Lenient)"
  - `strictnessModerate`: e.g. "நடுத்தரமானது (Moderate)"
  - `strictnessStrict`: e.g. "கடுமையானது (Strict)"
  - `analyzingMatchNotice`: "திருமணப் பொருத்த விதிகள் மற்றும் தோஷ நிலைகள் பகுப்பாய்வு செய்யப்படுகின்றன..."
  - `matchingEngineError`: "பொருத்த கணிப்பு பிழை"
  - `tryAgain`: "மீண்டும் முயற்சிக்கவும்"
  - `exceptionLabel`: "விதிவிலக்கு (நிவர்த்தி)"
  - `aiMatchingBannerDesc`: Localized description
  - `aiMatchingLoadingDesc`: Localized loading description
  - `cached3HourNotice`: "3 மணிநேர சேமிக்கப்பட்ட பகுப்பாய்வு"
  - `refreshAiAnalysis`: "AI பகுப்பாய்வை புதுப்பிக்கவும்"
  - `astrologicalBasisLabel`: "ஜோதிட அடிப்படை"

- **`MatchingPage.jsx` & `AiMatchingView.jsx`**:
  Replace all raw strings with `t(key, settings.language)`.

### 2.3 Guaranteed AI Matching in PDF Exports
- **`MatchingController.java`**:
  - Normalize the incoming `Accept-Language` or `language` parameter (extract primary language tag, e.g., `"ta"`, `"hi"`, `"en"`).
  - In `downloadCompatibilityReport`:
    ```java
    String effectiveLang = normalizeLanguage(language);
    if (geminiProperties.isPdfPredictionsEnabled() && geminiProperties.isMatchingEnabled()) {
        var aiPred = geminiPredictionService.generateMarriageMatchingAiAnalysis(request, response, effectiveLang, false);
        if (aiPred != null && aiPred.isEnabled()) {
            response.setAiMatchingPrediction(aiPred);
        }
    }
    ```
  - This guarantees that if AI matching is enabled, it retrieves from 3-hour cache (or computes on-demand) and embeds the full AI compatibility analysis into the downloaded PDF report.

---

## 3. Verification Plan
1. **Frontend Verification**: `npm run build` with zero syntax/type errors.
2. **Automated Backend Tests**: `mvn test` (verifying all 44 unit & integration tests).
3. **Manual Flow Check**:
   - Verify language switching translates all dropdowns and AI domain headers.
   - Verify PDF export endpoint attaches AI compatibility report.
