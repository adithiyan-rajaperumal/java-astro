# Design Specification: Horoscope Data Retention & Gemini AI-Based Life Balan (Past Verification & Lifetime Predictions)

## 1. Overview
Enhance the Vedic Astrology platform with:
1. **Horoscope Data Retention:** Client-side profile manager (`localStorage`) to save, rename, delete, switch, and instantly load horoscope profiles in both the Horoscope and Marriage Matching pages.
2. **Gemini AI-Based Life Balan & Timeline:** A Vedic prompt synthesis and Gemini AI engine controlled by a Spring Boot `application.yml` boolean flag (`gemini.enabled`) and API key (`gemini.api-key`).
   - **Past Years Verification Milestones (Birth to Current Age):** Highlighting historical life transitions and Dasa shifts so users can verify the accuracy of their horoscope.
   - **Year-by-Year Future Predictions (Current Age to Age 100+):** Detailed astrological forecasts across Career/Finance, Health/Vitality, Relationships/Family, and Spiritual Remedies.
3. **PDF Export Integration:** Include the structured AI Life Balan and year-wise predictions in the downloadable PDF report when AI predictions are enabled.
4. **Conditional UI Activation:** Expose `aiPredictionsEnabled` from the backend so the UI dynamically shows or hides the AI Balan tab and features based on the server configuration.

---

## 2. Architecture & Components

### 2.1 Backend Configuration & DTOs
* **`application.yml`**:
  ```yaml
  gemini:
    api-key: ${GEMINI_API_KEY:}
    enabled: ${GEMINI_ENABLED:true}
    model: ${GEMINI_MODEL:gemini-1.5-flash}
  ```
* **`GeminiProperties.java`**: Spring `@ConfigurationProperties(prefix = "gemini")` bean.
* **`PredictionRequestDTO.java`**: Contains birth details, language, current year/age, and calculated planetary chart context.
* **`PredictionResponseDTO.java`**: Contains:
  * `boolean enabled`
  * `String overallSummary`
  * `List<PastVerificationMilestone> pastMilestones` (year, age, dasaLord, events, keyHighlights)
  * `List<YearlyPrediction> futurePredictions` (year, age, dasaLord, careerFinance, healthVitality, familyMarriage, remedies)
* **`AppConfigDTO.java` / `ChartUiResponseDTO.java`**: Includes `boolean aiPredictionsEnabled`.

### 2.2 Backend Services & REST Endpoints
* **`GeminiPredictionService.java`**:
  * Formulates structured astrological prompts incorporating D1/D9 positions, Bhavas, Shadbala, active Yogas/Doshams, and Vimshottari Dasa-Bhukthi timeline.
  * Communicates with the Google Gemini REST API (`https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent`).
  * Parses response into structured `PredictionResponseDTO`.
  * Fallback behavior: If API key is missing or disabled, returns appropriate status and message.
* **`PdfExportService.java`**:
  * Extends the OpenPDF report builder to include a dedicated section for "AI Life Balan & Yearly Predictions" with dual-language headers.
* **`AstrologyController.java` / `PredictionController.java`**:
  * `GET /api/v1/astrology/config` -> returns configuration capabilities (`aiPredictionsEnabled`).
  * `POST /api/v1/astrology/predictions/generate` -> generates AI predictions for the horoscope.

### 2.3 Frontend Storage & UI
* **Saved Horoscopes Manager (`localStorage` key: `drikvedic_saved_horoscopes`)**:
  * Save current birth details as a profile with custom name (e.g. "My Horoscope", "Father", "Daughter").
  * Quick-switcher dropdown in Horoscope page to populate form and calculate chart instantly.
  * Integration in Matching Page to pick Boy or Girl from saved horoscopes with 1 click.
* **AI Life Balan Tab in Horoscope Page (`HoroscopePage.jsx`)**:
  * Visible only when `aiPredictionsEnabled` is `true`.
  * **"Generate AI Life Balan"** action button.
  * **Past Life Verification Section:** Year-by-year milestone cards with verification status checkmarks.
  * **Future Year-by-Year Predictions Section:** Filterable timeline cards from current age to lifetime (Career, Health, Family, Remedies).
* **Translations (`translations.js`, `messages_*.properties`)**:
  * Complete labels in Tamil, English, Hindi, Kannada, Telugu, Malayalam for all AI Balan sections.

---

## 3. Verification Plan

1. **Automated Backend Tests (`mvn test`)**:
   * Unit tests for `GeminiPredictionService` prompt construction, JSON parsing, and graceful fallback.
   * Integration test for `/api/v1/astrology/config` and prediction endpoints.
   * Regression tests for standard chart calculations and PDF generation.
2. **Frontend Build & UI Tests**:
   * Run `npm run build` to verify clean compilation.
   * Test saving, loading, switching, and deleting horoscope profiles in `localStorage`.
   * Test conditional visibility of the AI Balan tab based on `aiPredictionsEnabled`.
   * Test PDF download with AI Balan included.
