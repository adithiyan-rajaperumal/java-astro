# Design Specification: Lifetime Personalized Vedic AI Predictions, Health Engine, Daily Balan & Dual-Layer Caching

**Date:** 2026-08-10  
**Status:** Validated Design / Ready for Planning  
**Target:** `java-astro` (Spring Boot Backend + React Frontend)

---

## 1. Overview & Objectives

This specification defines the architectural enhancements to transform the AI Prediction capabilities in `java-astro` into an authentic, personalized, high-precision Vedic Jyotish intelligence engine.

### Key Capabilities & Control Flags:
1. **Granular Feature Flagging in `application.yml`**:
   * `gemini.enabled`: Master switch for AI services.
   * `gemini.life-predictions-enabled`: Controls whether the **AI Life Balan** tab is enabled and returned to UI.
   * `gemini.daily-balan-enabled`: Controls whether the **Daily Balan** tab is enabled and returned to UI.
   * `gemini.pdf-predictions-enabled`: Controls whether AI Balan details are included in the generated PDF report (if false, PDF generates only classical astronomical and chart details).
2. **Generic Boilerplate Elimination**: Replace generic, static milestone templates with 100% personalized predictions mathematically derived from D1 (Rasi), D2 (Hora - Wealth), D3 (Drekkana), D7 (Saptamsa), D9 (Navamsa), D10 (Dasamsa), D12 (Dwadasamsa), D30 (Trimsamsa), Shadbala, and active Vimshottari Dasa-Bhukthi timelines.
3. **Lifetime Granular Year-by-Year Forecast**: Deliver comprehensive year-by-year predictions across the native's entire lifespan (Current Age to 85+ years) covering 7 distinct pillars (Personal Mindset, Career & Profession, Wealth & Finances [D2], Health & Vitality, Marriage & Domestic Life, Parents & Kids, Favorable vs Caution windows, and Practical Vedic Remedies).
4. **Dedicated In-Depth Health Engine**: Multi-chart health and vitality diagnostics using Lagna lord dignity, 6th house (Roga sthana), 8th house (Ayur/chronic vulnerabilities), 12th house, D30 Trimsamsa, and Ayurvedic Tridosha balancing.
5. **Personalized Daily Balan (இன்றைய ராசி பலன்)**: Real-time transit (Gochara) synthesis with natal Moon/Lagna, Chandrashtamam detection, daily Panchangam, favorable horai timings, lucky color/number, and daily micro-remedy.
6. **Zero-Cost Dual-Layer Caching (Render Free Tier Optimized)**:
   * **30-Day Expiry** for Lifetime Predictions & Past Verification.
   * **11:59:59 PM Same-Day Expiry** for Daily Balan.
   * Client-side `localStorage` + Server-side In-Memory Cache (No Redis required, 0 API cost for repeated views & PDF downloads).
7. **Token & Latency Acceleration**: Optimized telegraphic prompt notation, native system instructions, and progressive loading cutting generation time from 30s to ~4–7s.
8. **100% Native Language Translations**: Full localization across **Tamil, Hindi, Telugu, Kannada, Malayalam, and English** with strict classical Jyotish terminology.
9. **UI Tab State Reset Bugfix**: Ensure clean state reset to the `charts` tab when creating a new horoscope or loading saved profiles.

---

## 2. Configuration & Flag Control Architecture

### 2.1 `application.yml` Properties Schema
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:enc:QVEuQWI4Uk42SXZvc2QwSktCUF90UTZma0ZDOEhvSHk2anY5Y2JuMGdtTDRVc21JNW9acUE=}
  enabled: ${GEMINI_ENABLED:true}
  life-predictions-enabled: ${GEMINI_LIFE_PREDICTIONS_ENABLED:true} # Controls AI Life Balan tab
  daily-balan-enabled: ${GEMINI_DAILY_BALAN_ENABLED:true}           # Controls Daily Balan tab
  pdf-predictions-enabled: ${GEMINI_PDF_PREDICTIONS_ENABLED:true}   # Controls AI prediction inclusion in PDF
  model: ${GEMINI_MODEL:gemini-3.6-flash}
```

### 2.2 `GeminiProperties.java` & `AppConfigDTO.java`
* `GeminiProperties` exposes helper methods:
  * `isFeatureEnabled()`: checks `enabled && apiKeyPresent`.
  * `isLifePredictionsEnabled()`: checks `isFeatureEnabled() && lifePredictionsEnabled`.
  * `isDailyBalanEnabled()`: checks `isFeatureEnabled() && dailyBalanEnabled`.
  * `isPdfPredictionsEnabled()`: checks `isFeatureEnabled() && pdfPredictionsEnabled`.
* `/api/v1/astrology/config` returns:
  ```json
  {
    "aiPredictionsEnabled": true,
    "lifePredictionsEnabled": true,
    "dailyBalanEnabled": true,
    "pdfPredictionsEnabled": true,
    "geminiModel": "gemini-3.6-flash"
  }
  ```

---

## 3. Architecture & Data Flow

```
+-------------------------------------------------------------------------------------------------+
|                                       React Frontend                                            |
|                                                                                                 |
|  +---------------------+  +-------------------------------+  +--------------------------------+ |
|  | D1/D9/Varga Charts  |  | 🔮 Lifetime AI Balan          |  | 📅 Daily Balan                 | |
|  |   (HoroscopePage)   |  | (Visible if lifeBalanEnabled) |  | (Visible if dailyBalanEnabled)| |
|  +---------------------+  +-------------------------------+  +--------------------------------+ |
|             |                            |                                   |                  |
|             v                            v                                   v                  |
|  +--------------------------------------------------------------------------------------------+ |
|  |              Layer 1 Client Cache: localStorage (30-day / End-of-Day TTL)                  | |
|  +--------------------------------------------------------------------------------------------+ |
+-------------------------------------------------------------------------------------------------+
                                                |
                              REST API Calls (if cache miss / refresh)
                                                v
+-------------------------------------------------------------------------------------------------+
|                                    Spring Boot Backend                                          |
|                                                                                                 |
|  +-----------------------------+                +---------------------------------------------+ |
|  | PredictionController        | <============> | Layer 2 Server In-Memory TTL Cache          | |
|  +-----------------------------+                +---------------------------------------------+ |
|               |                                                                                 |
|               v                                                                                 |
|  +--------------------------------------------------------------------------------------------+ |
|  | GeminiPredictionService (Telegraphic Matrix Prompt + 12-Varga Synthesis)                   | |
|  +--------------------------------------------------------------------------------------------+ |
|               |                                                                                 |
|               v                                                                                 |
|  +--------------------------------------------------------------------------------------------+ |
|  | Google Gemini API (gemini-2.0-flash / System Instruction + JSON Schema)                    | |
|  +--------------------------------------------------------------------------------------------+ |
+-------------------------------------------------------------------------------------------------+
```

---

## 4. Detailed Component Specifications

### 4.1 Backend: Astrological Data Synthesis (`GeminiPredictionService`)
* **12-Varga Compressed Representation**:
  * Injects compact astrological notation into prompt:
    * `D1:[Su:Leo 15.2° H5, Mo:Can 22.1° H4, Ma:Cap(Ex) 28.0° H10, ...] Lagna:Ari 12.4°`
    * `D2(Hora):[Su:Leo, Mo:Can, Ju:Leo, Ve:Can, ...] (Wealth potential)`
    * `D9(Navamsha):[Su:Sag, Mo:Pis, Ju:Can(Ex), ...]`
    * `D10(Dasamsa):[Su:Ari, Ma:Cap, Ju:Sag, ...] (Career prominence)`
    * `D30(Trimsamsa):[Afflictions/Health vulnerabilities: 6th lord Mars in Saturn Trimsamsa]`
    * `Shadbala:[Sun:1.45(Strong), Moon:1.10(Optimum), Mars:1.80(Very Strong)...]`
    * `Vimshottari Timeline:[2015-06 to 2031-06: Jupiter Mahadasa; 2024-03 to 2026-11: Jupiter-Venus]`
* **Prompt Token Optimization**:
  * Pass system instructions via Gemini API's `system_instruction` parameter for KV-cache acceleration.
  * Explicit JSON schema enforcement to ensure zero markdown preamble and minimal completion tokens.

### 4.2 Backend: Data Models (`PredictionResponseDTO`, `DailyBalanDTO`)

```java
// Lifetime Prediction Response
public class PredictionResponseDTO {
    private boolean enabled;
    private String message;
    private TokenUsage tokenUsage;
    private String overallSummary;
    private NativePersonality nativePersonality;
    private List<AiYoga> aiYogas;
    private List<AiDosham> aiDoshams;
    private HealthAnalysis healthAnalysis;
    private List<PastMilestone> pastMilestones;
    private List<YearlyPrediction> lifetimePredictions;

    public record NativePersonality(
        String coreTemperament,
        List<String> keyStrengths,
        List<String> vulnerabilitiesAndKarmicLessons
    ) {}

    public record HealthAnalysis(
        String ayurvedicConstitution,
        List<String> organVulnerabilities,
        String longevityVitalitySummary,
        List<String> recommendedDietAndLifestyle
    ) {}

    public record PastMilestone(
        int year,
        int age,
        String dasaBhukthi,
        String milestoneTitle,
        String nature, // POSITIVE, CHALLENGING, NEUTRAL
        String description,
        String astrologicalFactor,
        boolean verified
    ) {}

    public record YearlyPrediction(
        int year,
        int age,
        String dasaBhukthi,
        String personalMindset,
        String careerProfession,
        String wealthFinance,
        String healthVitality,
        String marriageFamily,
        String parentsKids,
        String favorableVsCaution,
        String remediesGuidance
    ) {}
}

// Daily Balan Response
public class DailyBalanDTO {
    private boolean enabled;
    private String targetDate;
    private String rasi;
    private String nakshatra;
    private String runningDasaBhukthi;
    private boolean chandrashtama;
    private String generalOutlook;
    private String careerWork;
    private String financeWealth;
    private String healthVitality;
    private String relationshipFamily;
    private String luckyColor;
    private String luckyNumber;
    private String favorableDirection;
    private String bestTimeWindow;
    private String dailyRemedy;
    private PredictionResponseDTO.TokenUsage tokenUsage;
}
```

### 4.3 Zero-Cost Dual-Layer Caching Engine (`PredictionCacheService`)
* **Key Generation**: Deterministic SHA-256 string combining `(dob, tob, lat, lon, ayanamsa, panchangamSystem, date, language)`.
* **Backend Layer**:
  * ConcurrentHashMap backed by expiration tracking.
  * Lifetime Balan TTL: `30 days`.
  * Daily Balan TTL: `Until 23:59:59 of targetDate`.
  * Automatically reused by `/download-pdf` endpoint when `pdf-predictions-enabled: true` so PDF downloads take 0 tokens and 0 latency.
* **Frontend Layer**:
  * Stores items in `localStorage` under `astro_pred_lifetime_<hash>` and `astro_pred_daily_<hash>_<date>`.
  * Checks expiry timestamp before triggering network requests.
  * "🔄 Regenerate" button sets `forceRefresh: true` and purges client/server cache keys.

### 4.4 100% Native Localization (`translations.js` & React Components)
* Update `translations.js` to ensure complete coverage for **Tamil (`ta`)**, **Hindi (`hi`)**, **Telugu (`te`)**, **Kannada (`kn`)**, **Malayalam (`ml`)**, and **English (`en`)**.
* Replace all hardcoded inline ternary conditionals (`language === 'ta' ? ... : ...`) with clean `t(key, language)` calls.
* Enforce native script generation for all AI responses (classical Jyotish vocabulary for each language).

### 4.5 UI Tab State Fix (`HoroscopePage.jsx`)
* When user clicks "New Horoscope" (`setReport(null)`) or loads a saved profile (`handleLoadSavedProfile`), explicitly reset `activeSubTab` to `'charts'`.
* Ensure smooth tab switching without UI lockups.

---

## 5. Verification & Testing Strategy

1. **Unit & Integration Tests (`GeminiPredictionServiceTest`, `PredictionCacheServiceTest`, `AppConfigControllerTest`)**:
   * Verify all 3 flags (`life-predictions-enabled`, `daily-balan-enabled`, `pdf-predictions-enabled`) correctly govern endpoint responses and PDF exports.
   * Verify compact telegraphic prompt construction including D1, D2, D9, D30, and Dasa timeline.
   * Test 30-day lifetime cache and end-of-day daily cache storage, retrieval, and eviction.
   * Verify PDF generation respects `pdf-predictions-enabled` and reuses cached AI predictions without invoking Gemini API.
2. **Language & I18n Verification**:
   * Test Tamil, Hindi, Telugu, Kannada, Malayalam, and English UI rendering.
   * Ensure 0 untranslated raw keys or mismatched ternary fallbacks.
3. **UI Interaction Verification**:
   * Test conditional rendering of Life Balan and Daily Balan tabs based on backend config flags.
   * Test creating a new horoscope, switching between profiles, and switching tabs without getting stuck.
   * Verify instant load from `localStorage` on repeat visits and force-refresh recalculation via "Regenerate" button.
