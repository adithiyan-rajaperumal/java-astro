# Specification: Flag-Based AI Forecast Span Control & Dynamic UI/PDF Badges

## 1. Overview
Introduce a configuration property in `application.yml` (`gemini.forecast-mode` or `gemini.forecast-years`) allowing the system to switch between:
- `FULL_LIFESPAN` (continuous year-by-year predictions from current age up to calculated Ayurdaya ceiling, e.g. age 85-95+)
- `NEXT_10_YEARS` (focused 10-year forecast window from current year to current year + 10 with deep, enriched detail)
- Custom integer years (e.g., `gemini.forecast-years: 10`, where `0` or negative defaults to Full Lifespan).

The system utilizes **distinct prompt strategies based on the active mode**:
- In `NEXT_10_YEARS` mode, the prompt instructs Gemini to leverage the available token budget to produce **rich, deep, multi-paragraph yearly breakdowns** (covering quarterly career pivots, specific transit impacts, relationship timings, and granular remedies).
- In `FULL_LIFESPAN` mode, the prompt utilizes a **high-density synthesis** (2-3 potent sentences per year) ensuring all 40-60+ years fit cleanly within output token boundaries.

The generated output payload, UI headers, and exported PDF reports dynamically adapt their titles, badges, and year-range descriptions.

---

## 2. Configuration (`application.yml` & `GeminiProperties.java`)

### `application.yml` Structure
```yaml
gemini:
  # Forecast span mode: FULL_LIFESPAN (entire Ayurdaya lifespan) or NEXT_10_YEARS / NEXT_15_YEARS
  forecast-mode: ${GEMINI_FORECAST_MODE:FULL_LIFESPAN}
  # forecast-mode: NEXT_10_YEARS # Toggle this line to easily switch to 10-year mode
  forecast-years: ${GEMINI_FORECAST_YEARS:0} # 0 = use forecast-mode / full lifespan, >0 = exact custom number of years (e.g. 10)
```

### `GeminiProperties.java`
- Fields: `private String forecastMode = "FULL_LIFESPAN";` and `private int forecastYears = 0;`
- Helper methods:
  - `public boolean is10YearForecastMode()`: returns true if `NEXT_10_YEARS` or `forecastYears == 10`.
  - `public int resolveForecastYears(int currentAge, int ayurdayaCeilingAge)`:
    - If `forecastYears > 0`, return `forecastYears`.
    - If `"NEXT_10_YEARS".equalsIgnoreCase(forecastMode)` or `"10".equals(forecastMode)`, return `10`.
    - If `"NEXT_15_YEARS".equalsIgnoreCase(forecastMode)` or `"15".equals(forecastMode)`, return `15`.
    - Default / `FULL_LIFESPAN`: return `Math.max(1, ayurdayaCeilingAge - currentAge)`.

---

## 3. Data Transfer & API Changes

### `PredictionResponseDTO.java`
Add forecast metadata to the response DTO so consumers know the exact span generated:
```java
private String forecastMode;     // "FULL_LIFESPAN" | "NEXT_10_YEARS"
private int startYear;            // e.g. 2026
private int endYear;              // e.g. 2036 or 2085
private int startAge;             // e.g. 30
private int endAge;               // e.g. 40 or 89
private int totalForecastYears;   // e.g. 11 or 60
```

### `AppConfigDTO.java` & `ChartController.java`
Expose `forecastMode` and `forecastYears` in `/api/config` so the frontend knows the system default ahead of time.

---

## 4. Mode-Differentiated Prompt Synthesis (`GeminiPredictionService.java`)

In `constructAstrologicalPrompt`:
- Calculate `maxForecastYears = geminiProperties.resolveForecastYears(currentAge, targetLifespanAge);`
- Determine mode: `boolean isShortHorizon = maxForecastYears <= 15;`
- Target end year = `currentYear + maxForecastYears`.
- Target end age = `currentAge + maxForecastYears`.
- Construct `yearlyAnchors` precisely for years `0 .. maxForecastYears`.

### Mode A: `NEXT_10_YEARS` Mode (Enriched, Deep Forecasting)
When `isShortHorizon == true`:
- Directive 6 instructs Gemini:
  - `yearlyTheme`: Evocative and specific yearly theme headline.
  - `detailedPrediction`: **Comprehensive 5–7 sentence in-depth breakdown** explicitly detailing:
    1. **Career, Business & Wealth**: Major promotion/job change windows, business expansions, financial investments, real estate acquisition timings, and peak financial quarters.
    2. **Health, Vitality & Ayurvedic Care**: Specific physical vulnerabilities, seasonal dosha imbalances, diet and lifestyle adjustments aligned with transiting planetary energies.
    3. **Family, Marriage, Children & Relationships**: Marital harmony, auspicious marriage/progeny periods, children's milestones, and domestic peace.
    4. **Mindset, Spiritual Evolution & Key Turning Points**: Mental resilience, spiritual sadhana, pilgrimage windows, and decisive life choices.
  - `astrologicalBasis`: Detailed explanation citing active Dasa-Bhukthi-Pratyantar lords, planetary aspects (Drishti), transit impacts (Guru/Sani/Rahu/Ketu Gocharam), and relevant Varga alignments (D9, D10, D12, D30).
  - `cautionsAndRemedies`: Granular, actionable Vedic remedies (exact mantra with recitation count, deity archana, specific charity on designated days, and timing recommendations).

### Mode B: `FULL_LIFESPAN` Mode (High-Density Multi-Decade Synthesis)
When `isShortHorizon == false`:
- Directive 6 instructs Gemini:
  - `yearlyTheme`: Sharp 1-line headline.
  - `detailedPrediction`: 2–3 potent, comprehensive sentences synthesizing (a) Career, Business & Wealth, (b) Health & Vitality Realities, (c) Family, Marriage & Progeny, and (d) Parents, Elders & Mindset with spiritual milestones.
  - `astrologicalBasis`: 1 concise sentence citing active Dasa-Bhukthi lords and D1/D9/D10/D12/D30 placements from the yearly anchor.
  - `cautionsAndRemedies`: 1 practical cautionary note & authentic Vedic remedy.

---

## 5. UI Dynamic Display Tags (`AiPredictionsView.jsx` & `translations.js`)

In `AiPredictionsView.jsx`:
- Dynamic Heading above Year-by-Year Predictions:
  - When in 10-Year mode:
    - EN: `10-Year Astrological Forecast (2026 – 2036 • Age 30 to 40)`
    - TA: `அடுத்த 10 ஆண்டுகளுக்கான வருடாந்திர பலன்கள் (2026 – 2036 • வயது 30 முதல் 40 வரை)`
  - When in Full Lifespan mode:
    - EN: `Full Lifetime Astrological Forecast (2026 – 2085 • Age 30 to 89)`
    - TA: `முழு வாழ்நாள் வருடாந்திர பலன்கள் (2026 – 2085 • வயது 30 முதல் 89 வரை)`
- Scope Badge:
  - Adds a styled badge next to the section title showing `[ 10-Year In-Depth Scope ]` or `[ Full Lifespan • 59 Years ]`.

---

## 6. PDF Report Rendering (`PdfExportService.java`)

In `PdfExportService.java`:
- Dynamically format the Year-by-Year Forecast Table Title based on `data.getAiPredictions()`:
  - If `forecastMode` is `NEXT_10_YEARS` or count $\le 15$:
    - EN: `10-Year Astrological Forecast (2026 – 2036)`
    - TA: `அடுத்த 10 ஆண்டுகளுக்கான பலன்கள் & வழிகாட்டுதல் (2026 – 2036)`
  - If `FULL_LIFESPAN` / count $> 15$:
    - EN: `Year-by-Year Lifetime Astrological Forecast (Current Age to 89)`
    - TA: `வருடாந்திர வாழ்நாள் பலன்கள் & வழிகாட்டுதல் (வயது 30 முதல் 89 வரை)`

---

## 7. Multi-Lingual Property Bundles (`messages*.properties` & `translations.js`)
Add dynamic template keys for:
- `ai.forecast.title.10years`
- `ai.forecast.title.full_lifespan`
- `ai.forecast.badge.10years`
- `ai.forecast.badge.full_lifespan`
across EN, TA, HI, KN, TE, and ML.
