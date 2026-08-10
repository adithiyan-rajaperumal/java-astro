# Design Specification: Lifetime Personalized Vedic AI Predictions, Health Engine, Daily Balan & Dual-Layer Caching

**Date:** 2026-08-10  
**Status:** Validated Design / Ready for Planning  
**Target:** `java-astro` (Spring Boot Backend + React Frontend)

---

## 1. Overview & Objectives

This specification defines the architectural enhancements to transform the AI Prediction capabilities in `java-astro` into an authentic, personalized, high-precision Vedic Jyotish intelligence engine.

### Key Pain Points Addressed:
1. **Generic Boilerplate Elimination**: Replace generic, static milestone templates with 100% personalized predictions mathematically derived from D1 (Rasi), D2 (Hora - Wealth), D3 (Drekkana), D7 (Saptamsa), D9 (Navamsa), D10 (Dasamsa), D12 (Dwadasamsa), D30 (Trimsamsa), Shadbala, and active Vimshottari Dasa-Bhukthi timelines.
2. **Lifetime Granular Year-by-Year Forecast**: Deliver comprehensive year-by-year predictions across the native's entire lifespan (Current Age to 85+ years) covering 7 distinct pillars (Personal Mindset, Career & Profession, Wealth & Finances [D2], Health & Vitality, Marriage & Domestic Life, Parents & Kids, Favorable vs Caution windows, and Practical Vedic Remedies).
3. **Dedicated In-Depth Health Engine**: Multi-chart health and vitality diagnostics using Lagna lord dignity, 6th house (Roga sthana), 8th house (Ayur/chronic vulnerabilities), 12th house, D30 Trimsamsa, and Ayurvedic Tridosha balancing.
4. **Personalized Daily Balan (இன்றைய ராசி பலன்)**: Real-time transit (Gochara) synthesis with natal Moon/Lagna, Chandrashtamam detection, daily Panchangam, favorable horai timings, lucky color/number, and daily micro-remedy.
5. **Zero-Cost Dual-Layer Caching (Render Free Tier Optimized)**:
   * **30-Day Expiry** for Lifetime Predictions & Past Verification.
   * **11:59:59 PM Same-Day Expiry** for Daily Balan.
   * Client-side `localStorage` + Server-side In-Memory Cache (No Redis required, 0 API cost for repeated views & PDF downloads).
6. **Token & Latency Acceleration**: Optimized telegraphic prompt notation, native system instructions, and progressive loading cutting generation time from 30s to ~4–7s.
7. **100% Native Language Translations**: Full localization across **Tamil, Hindi, Telugu, Kannada, Malayalam, and English** with strict classical Jyotish terminology.
8. **UI Tab State Reset Bugfix**: Ensure clean state reset to the `charts` tab when creating a new horoscope or loading saved profiles.

---

## 2. Architecture & Data Flow

```
+-----------------------------------------------------------------------------------+
|                                React Frontend                                     |
|                                                                                   |
|  +-------------------------+  +-------------------------+  +--------------------+ |
|  |   D1/D9/Varga Charts    |  |  🔮 Lifetime AI Balan   |  |   📅 Daily Balan   | |
|  |     (HoroscopePage)     |  |   (AiPredictionsView)   |  |  (DailyBalanView)  | |
|  +-------------------------+  +-------------------------+  +--------------------+ |
|               |                            |                          |           |
|               v                            v                          v           |
|  +------------------------------------------------------------------------------+ |
|  |        Layer 1 Client Cache: localStorage (30-day / End-of-Day TTL)          | |
|  +------------------------------------------------------------------------------+ |
+-----------------------------------------------------------------------------------+
                                         |
                       REST API Calls (if cache miss / refresh)
                                         v
+-----------------------------------------------------------------------------------+
|                             Spring Boot Backend                                   |
|                                                                                   |
|  +-----------------------------+         +-------------------------------------+  |
|  | PredictionController        | <=====> | Layer 2 Server In-Memory TTL Cache  |  |
|  +-----------------------------+         +-------------------------------------+  |
|               |                                                                   |
|               v                                                                   |
|  +------------------------------------------------------------------------------+ |
|  | GeminiPredictionService (Telegraphic Matrix Prompt + 12-Varga Synthesis)     | |
|  +------------------------------------------------------------------------------+ |
|               |                                                                   |
|               v                                                                   |
|  +------------------------------------------------------------------------------+ |
|  | Google Gemini API (gemini-2.0-flash / System Instruction + JSON Schema)      | |
|  +------------------------------------------------------------------------------+ |
+-----------------------------------------------------------------------------------+
```

---

## 3. Detailed Component Specifications

### 3.1 Backend: Astrological Data Synthesis (`GeminiPredictionService`)
* **12-Varga Compressed Representation**:
  * Instead of verbose paragraphs, inject a compact astrological notation:
    * `D1:[Su:Leo 15.2° H5, Mo:Can 22.1° H4, Ma:Cap(Ex) 28.0° H10, ...] Lagna:Ari 12.4°`
    * `D2(Hora):[Su:Leo, Mo:Can, Ju:Leo, Ve:Can, ...] (Wealth potential)`
    * `D9(Navamsha):[Su:Sag, Mo:Pis, Ju:Can(Ex), ...]`
    * `D10(Dasamsa):[Su:Ari, Ma:Cap, Ju:Sag, ...] (Career prominence)`
    * `D30(Trimsamsa):[Afflictions/Health vulnerabilities: 6th lord Mars in Saturn Trimsamsa]`
    * `Shadbala:[Sun:1.45(Strong), Moon:1.10(Optimum), Mars:1.80(Very Strong)...]`
    * `Vimshottari Timeline:[2015-06 to 2031-06: Jupiter Mahadasa; 2024-03 to 2026-11: Jupiter-Venus]`
* **Prompt Token Optimization**:
  * Pass system instructions via Gemini API's `system_instruction` parameter for KV-cache acceleration.
  * Explicit JSON schema enforcement to ensure zero markdown preamble and minimal autoregressive completion tokens.

### 3.2 Backend: Data Models (`PredictionResponseDTO`, `DailyBalanDTO`)

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

### 3.3 Zero-Cost Dual-Layer Caching Engine (`PredictionCacheService`)
* **Key Generation**: Deterministic SHA-256 string combining `(dob, tob, lat, lon, ayanamsa, panchangamSystem, date, language)`.
* **Backend Layer**:
  * ConcurrentHashMap backed by expiration tracking.
  * Lifetime Balan TTL: `30 days`.
  * Daily Balan TTL: `Until 23:59:59 of targetDate`.
  * Automatically reused by `/download-pdf` endpoint so PDF downloads take 0 tokens and 0 latency.
* **Frontend Layer**:
  * Stores items in `localStorage` under `astro_pred_lifetime_<hash>` and `astro_pred_daily_<hash>_<date>`.
  * Checks expiry timestamp before triggering network requests.
  * "🔄 Regenerate" button sets `forceRefresh: true` and purges client/server cache keys.

### 3.4 100% Native Localization (`translations.js` & React Components)
* Update `translations.js` to ensure complete coverage for **Tamil (`ta`)**, **Hindi (`hi`)**, **Telugu (`te`)**, **Kannada (`kn`)**, **Malayalam (`ml`)**, and **English (`en`)**.
* Replace all hardcoded inline ternary conditionals (`language === 'ta' ? ... : ...`) with clean `t(key, language)` calls.
* Enforce native script generation for all AI responses (classical Jyotish vocabulary for each language).

### 3.5 UI Tab State Fix (`HoroscopePage.jsx`)
* When user clicks "New Horoscope" (`setReport(null)`) or loads a saved profile (`handleLoadSavedProfile`), explicitly reset `activeSubTab` to `'charts'`.
* Ensure smooth tab switching without UI lockups.

---

## 4. Verification & Testing Strategy

1. **Unit & Integration Tests (`GeminiPredictionServiceTest`, `PredictionCacheServiceTest`)**:
   * Verify compact telegraphic prompt construction including D1, D2, D9, D30, and Dasa timeline.
   * Test 30-day lifetime cache and end-of-day daily cache storage, retrieval, and eviction.
   * Verify PDF generation uses cached AI predictions without invoking Gemini API.
2. **Language & I18n Verification**:
   * Test Tamil, Hindi, Telugu, Kannada, Malayalam, and English UI rendering.
   * Ensure 0 untranslated raw keys or mismatched ternary fallbacks.
3. **UI Interaction Verification**:
   * Test creating a new horoscope, switching between profiles, and switching tabs.
   * Verify instant load from `localStorage` on repeat visits.
   * Verify force-refresh recalculation via "Regenerate" button.
