# Specification: Flag-Based AI Forecast Span Control & Dynamic UI/PDF Badges

## 1. Overview
Introduce a configuration property in `application.yml` (`gemini.forecast-mode` or `gemini.forecast-years`) allowing the system to switch between:
- `FULL_LIFESPAN` (continuous year-by-year predictions from current age up to calculated Ayurdaya ceiling, e.g. age 85-95+)
- `NEXT_10_YEARS` (focused 10-year forecast window from current year to current year + 10)
- Custom integer years (e.g., `gemini.forecast-years: 10`, where `0` or negative defaults to Full Lifespan).

The generated output payload, UI headers, and exported PDF reports will dynamically adapt their titles, badges, and year-range descriptions instead of displaying static "Lifetime" labels when in 10-year mode.

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
- Helper method: `public int resolveForecastYears(int currentAge, int ayurdayaCeilingAge)`
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

## 4. Prompt Synthesis & Engine Logic (`GeminiPredictionService.java`)

In `constructAstrologicalPrompt`:
- Calculate `maxForecastYears = geminiProperties.resolveForecastYears(currentAge, targetLifespanAge);`
- Target end year = `currentYear + maxForecastYears`.
- Target end age = `currentAge + maxForecastYears`.
- Construct `yearlyAnchors` precisely for years `0 .. maxForecastYears`.
- In prompt Directive 6:
  - If `FULL_LIFESPAN`: instruct model to cover the full remaining lifespan from current age to target lifespan age.
  - If `NEXT_10_YEARS` / custom years: instruct model to generate predictions specifically for the next $N$ years ($2026 - 2036$).
- In `generateFallbackLifePredictions`: use the exact same resolved `maxForecastYears`.

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
  - Adds a styled badge next to the section title showing `[ 10-Year Scope ]` or `[ Full Lifespan • 59 Years ]`.

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
