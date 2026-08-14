# Design Specification: Standardized Multilingual Daily Balan (இன்றைய ராசி பலன்)

**Date**: 2026-08-15  
**Status**: Approved by User  
**Topic**: Daily Astrological Forecast Standardization & Multilingual Parity  

---

## 1. Overview & Objective

The goal of this feature is to standardize the Daily Balan (இன்றைய ராசி பலன்) into a clean, simple, and user-friendly single-paragraph astrological forecast covering all key dimensions of daily life. All UI labels and AI-generated text will have full 6-language parity (`en`, `ta`, `hi`, `te`, `kn`, `ml`), seamlessly adopting the application's unified white & saffron visual identity.

---

## 2. Architectural Design & Data Flow

### 2.1 Backend Schema & DTO (`DailyBalanDTO.java`)
The response structure is unified to support a single comprehensive `dailyNarrative` alongside deterministic anchors and fallback compatibility:

```java
public class DailyBalanDTO {
    private boolean enabled;
    private String message;
    private String targetDate;
    private String rasi;
    private String nakshatra;
    private String runningDasaBhukthi;
    private boolean chandrashtama;
    
    // Core Unified Forecast
    private String dailyNarrative;       // 4-6 sentence unified daily forecast
    private String dailyRemedy;          // Actionable Vedic remedy or mantra
    
    // Deterministic Daily Anchors (Seeded per weekday & language)
    private String luckyColor;
    private String luckyNumber;
    private String favorableDirection;
    private String bestTimeWindow;
    
    // Legacy / Fallback Fields (Maintained for full backward compatibility)
    private String generalOutlook;
    private String careerWork;
    private String financeWealth;
    private String healthVitality;
    private String relationshipFamily;
    
    private PredictionResponseDTO.TokenUsage tokenUsage;
}
```

### 2.2 System Instruction & Astrological Prompting (`GeminiPredictionService.java`)
1. **System Instruction (`constructDailySystemInstruction`)**:
   - Instruct Gemini as an expert classical Vedic Astrologer specializing in Gochara (daily planetary transits) and Panchangam synthesis.
   - Enforce 100% native script generation for the selected language (`en`, `ta`, `hi`, `te`, `kn`, `ml`).
   - Forbid generic filler or fluff; mandate technical Vedic synthesis of transit Moon house relative to Janma Rasi & Janma Lagna, Tarabalam, and running Dasa-Bhukthi.
2. **Prompt Directives & JSON Schema (`constructDailyAstrologicalPrompt`)**:
   - Provide structured input JSON: Native details (Lagna, Rasi, Nakshatra, Dasa), Natal planetary matrix, Today's Gochara (transit Moon, Nakshatra, Tithi, Yoga, Tarabalam, Chandrashtama status, Transit Moon house).
   - Demand output in JSON format:
     - `dailyNarrative`: A cohesive, content-dense paragraph of **4 to 6 substantial sentences** weaving together:
       1. Today's Moon Gochara & Tarabalam energy.
       2. Active Dasa-Bhukthi interaction with today's transit.
       3. Career & Vocational opportunities / cautions.
       4. Wealth & Financial movement.
       5. Health, Energy & Vitality.
       6. Family, Domestic & Interpersonal relations.
     - `dailyRemedy`: Specific daily Graha mantra or simple Vedic spiritual action.
     - `luckyColor`, `luckyNumber`, `favorableDirection`, `bestTimeWindow`.
3. **Response Parsing (`parseDailyGeminiResponse`)**:
   - Extract `dailyNarrative` directly.
   - If legacy fields (`generalOutlook`, `careerWork`, etc.) are returned instead of `dailyNarrative`, synthesize them into `dailyNarrative` automatically.

### 2.3 Deterministic Daily Anchors (`calculateDeterministicAnchors`)
Enrich `calculateDeterministicAnchors` to provide fully localized, authentic values across all 6 languages:
- **Sunday**: Sun | Ruby Red / Orange | 1 & 4 | East | Morning auspicious window
- **Monday**: Moon | Pearl White / Silver | 2 & 7 | North-West | Morning auspicious window
- **Tuesday**: Mars | Coral Red / Crimson | 9 & 1 | South | Late morning auspicious window
- **Wednesday**: Mercury | Emerald Green | 5 & 6 | North | Morning auspicious window
- **Thursday**: Jupiter | Golden Yellow | 3 & 9 | North-East | Morning auspicious window
- **Friday**: Venus | Silk White / Cream | 6 & 5 | South-East | Morning auspicious window
- **Saturday**: Saturn | Deep Blue / Black | 8 & 3 | West | Afternoon auspicious window

---

## 3. Frontend UI & Multilingual Parity

### 3.1 Component Redesign (`DailyBalanView.jsx`)
- Adopt the application's clean **Unified White & Saffron** theme (`var(--bg-card)` `#ffffff`, `var(--bg-primary)` `#fffaf4`, `var(--border)` `#f0e2d0`, `var(--accent-gold)` / `var(--accent-saffron)`):
  1. **Top Status Bar**: Compact bar displaying Target Date, Janma Rasi, Nakshatra, Dasa, Token count, and Regenerate button.
  2. **Chandrashtama Warning Banner**: Conditionally rendered when `chandrashtama === true` with warning icon and guidance.
  3. **Daily Prediction Narrative Card**: Clean card displaying `dailyNarrative` (or synthesized fallback).
  4. **Lucky Factors Grid**: 4 responsive pill cards (`luckyColor`, `luckyNumber`, `favorableDirection`, `bestTimeWindow`).
  5. **Daily Vedic Remedy Box**: Highlighted card with gold icon and actionable guidance.

### 3.2 6-Language Localization (`translations.js`)
All labels localized uniformly across English (`en`), Tamil (`ta`), Hindi (`hi`), Telugu (`te`), Kannada (`kn`), and Malayalam (`ml`):
- `dailyBalanTitle`, `dailyBalanSubtitle`, `generateDailyBalan`, `generatingDailyBalan`, `regenerateDailyBalan`, `cachedNoticeDaily`, `chandrashtamaAlert`, `chandrashtamaAlertDesc`, `dailyForecastParagraphTitle`, `dailyLuckyFactorsTitle`, `luckyColor`, `luckyNumber`, `favorableDirection`, `bestTimeWindow`, `dailyRemedy`, `tokensCount`.

---

## 4. Verification & Validation Plan

### 4.1 Automated Tests Across 5 Core Natives
Test against the 5 established benchmark horoscopes:
1. **Adithiyan**: `19-07-1995 13:10 Vellore` (Tula Lagna, Mesha Moon - Bharani)
2. **Uthayasri**: `17-08-2002 15:15 Viluppuram` (Dhanus Lagna, Vrishchika Moon - Jyeshtha)
3. **Padmasri**: `31-07-2001 19:30 Viluppuram` (Makara Lagna, Dhanus Moon - Moola)
4. **Deepanathan**: `11-04-1969 02:50 AM Tiruvannamalai` (Makara Lagna, Makara Moon - Sravana)
5. **Mahaveer**: `18-04-2024 06:37 AM Vellore` (Mesha Lagna, Cancer/Leo Moon)

### 4.2 Test Coverage
- **Prompt Generation**: Verify correct JSON structure, Gochara Moon house, Tarabalam calculation, and system instruction.
- **DTO Serialization & Backward Compatibility**: Ensure both single-narrative and legacy formats parse smoothly.
- **Multilingual Token Parity**: Verify UI rendering and absence of missing translation keys in all 6 languages.
- **Automated Regression Test**: Run full test suite (`mvn test`) and frontend production build (`npm run build`).
