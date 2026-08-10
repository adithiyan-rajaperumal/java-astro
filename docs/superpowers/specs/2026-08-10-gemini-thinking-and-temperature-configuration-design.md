# Design Specification: Configurable Gemini Extended Thinking & Temperature Tuning

## 1. Overview
This specification details the addition of configurable Extended Thinking (`thinkingConfig.thinkingBudget`) and Temperature tuning (`temperature: 0.4`) to the Gemini Jyotish prediction engine.

---

## 2. Motivation & Goals
- **Astrological Reasoning Depth**: Allow Gemini Flash models to deliberate through multi-chart cross-interactions (D1, D9, D30, Shadbala, running Vimshottari Mahadasa-Bhukthi) using reasoning tokens prior to generating final JSON.
- **Natural Classical Phrasing**: Upgrade default `temperature` from `0.2` to `0.4` to eliminate robotic repetition and produce richer classical astrological vocabulary across all supported languages (Tamil, Hindi, Telugu, Kannada, Malayalam, English).
- **Zero Lock-in & Total Config Control**: Provide clean YAML keys (`gemini.temperature` and `gemini.thinking-budget`) so developers and users can easily adjust budgets or disable thinking (`0`) without code changes.

---

## 3. Configuration & Architecture

### A. YAML Properties (`application.yml`)
```yaml
gemini:
  api-key: ${GEMINI_API_KEY:}
  model: ${GEMINI_MODEL:gemini-2.0-flash}
  temperature: ${GEMINI_TEMPERATURE:0.4}
  thinking-budget: ${GEMINI_THINKING_BUDGET:1024} # 0 to disable, 1024-2048 for deep synthesis
  life-predictions-enabled: ${GEMINI_LIFE_ENABLED:true}
  daily-balan-enabled: ${GEMINI_DAILY_ENABLED:true}
  pdf-predictions-enabled: ${GEMINI_PDF_ENABLED:true}
```

### B. Java Properties (`GeminiProperties.java`)
- `temperature`: `double` (default: `0.4`)
- `thinkingBudget`: `int` (default: `1024`)
- Getters/setters with validation (`temperature >= 0.0 && temperature <= 1.0`).

### C. Backend Engine Integration (`GeminiPredictionService.java`)
In `callGeminiApi`:
```java
Map<String, Object> generationConfig = new HashMap<>();
generationConfig.put("temperature", geminiProperties.getTemperature());
generationConfig.put("responseMimeType", "application/json");

if (geminiProperties.getThinkingBudget() > 0) {
    generationConfig.put("thinkingConfig", Map.of("thinkingBudget", geminiProperties.getThinkingBudget()));
}
```

### D. App Config Endpoint (`AppConfigDTO.java`)
Expose `thinkingBudget` and `temperature` so the frontend UI can display reasoning status in token usage badges.

---

## 4. Verification Plan
1. **Unit Tests**:
   - Verify `GeminiProperties` loads `temperature` and `thinkingBudget`.
   - Verify `GeminiPredictionService` builds correct `generationConfig` with `thinkingConfig` when budget > 0.
   - Verify `AppConfigControllerTest` asserts `thinkingBudget` and `temperature`.
2. **End-to-End Build**:
   - `mvn test`
   - `npm run build`
