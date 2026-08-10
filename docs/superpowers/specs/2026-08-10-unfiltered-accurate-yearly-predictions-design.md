# Design Specification: Accurate, Unfiltered Vedic Life Predictions & Past Turning Points

## 1. Overview
This specification overhauls the AI Life Balan engine to produce sharp, distinct, unfiltered, and deeply personalized Vedic predictions. It removes generic childhood milestone templates and replaces them with 2–3 pivotal past life-defining phases (from birth to present) and authentic, chart-grounded yearly predictions (evaluating real challenges such as job loss, parental health/loss, health crises, surgeries, as well as career breakthroughs and financial growth).

---

## 2. Core Astrological Architecture

### A. The Vedic Prediction Synthesis
Each year's forecast is derived from the **Trinity of Vedic Interpretation**:
1. **Natal Varga Charts**:
   - **D1 (Rasi)**: Lagna, functional benefics/malefics, 6th/8th/12th Dusthana lords, 2nd/7th Maraka lords, Badhaka lord.
   - **D10 (Dasamsa)**: Career elevation vs job loss, workplace hostility, business risks.
   - **D2 (Hora)**: Wealth accumulation, debts, financial losses.
   - **D30 (Trimsamsa)**: Misfortunes, accidents, surgeries, acute/chronic illnesses.
   - **D12 (Dwadasamsa)**: Parents' longevity, parental health decline, or loss.
   - **D9 & D7 (Navamsa & Saptamsa)**: Marital harmony/discord, progeny milestones.
   - **Shadbala Strengths**: Planetary vitality and capacity to deliver results.
2. **Running Vimshottari Dasa & Bhukthi Lords**:
   - Activates specific natal houses, yogas, and afflictions for that year.
3. **Unfiltered Truthful Articulation**:
   - If Dusthana/Maraka/Badhaka lords or afflicted D30/D10 configurations are active: explicitly and truthfully predict events like **job loss, career setbacks, health crises, hospitalizations, family disputes, or parental health decline/bereavement**.
   - If Yogakaraka/Kendra/Trikona lords are active: predict promotions, property/gold purchases, academic success, and family celebrations.

---

## 3. Data Structure Updates

### `PredictionResponseDTO.java`
```java
public class PredictionResponseDTO {
    private String overallSummary;
    private NativePersonality nativePersonality;
    private HealthAnalysis healthAnalysis;
    private List<PastKeyPhase> pastKeyPhases;       // 2-3 pivotal turning points from birth to present
    private List<YearlyPrediction> lifetimePredictions; // Continuous year-by-year forecast
    private List<AiYoga> aiYogas;
    private List<AiDosham> aiDoshams;
    ...
}
```

#### `PastKeyPhase`
- `periodOrAge`: e.g. "Age 18 - 22 (2013 - 2017)"
- `dasaBhukthi`: Running Dasa-Bhukthi during that phase
- `phaseTitle`: e.g. "கல்வி & திசைமாறிய பாதை / Academic Transition & Relocation"
- `livedExperience`: Key turning points, struggles, achievements, and behavioral impact
- `astrologicalBasis`: Explanatory planetary combination

#### `YearlyPrediction` (Sharp & Articulated)
- `year`: int
- `age`: int
- `dasaBhukthi`: String
- `yearlyTheme`: Concise high-impact headline
- `astrologicalBasis`: Exact planetary reason (e.g. "10th lord afflicted in D10 during 8th lord Bhukthi")
- `careerAndFinance`: Direct job loss / promotion / business / wealth forecast
- `healthAndFamily`: Direct health / surgery / parents' health / family harmony forecast
- `cautionsAndRemedies`: Direct warning & specific remedial prayer/mantra

---

## 4. Prompt Engineering & Token Optimization
- Redesigned Gemini prompt to enforce high informational density per field instead of long verbose filler paragraphs.
- Explicit prompt mandate against generic boilerplate and sugarcoated predictions.
- Dynamic offline fallback that computes unique astrological readings for each year based on that year's Dasa lord and the native's Lagna.

---

## 5. UI Presentation (`AiPredictionsView.jsx`)
- **Native Personality & Health Analysis**: Clean cards with Ayurvedic vitality.
- **Pivotal Past Life Phases (பிறப்பு முதல் இன்று வரை)**: 2–3 milestone cards showing lived turning points.
- **Year-by-Year Predictions**:
  - Filter chips (All, Career & Wealth, Health & Family, Warnings & Remedies).
  - Severity badges (🚨 Critical Caution, ⚠️ Moderate Caution, 🌟 Favorable, 💎 Auspicious).
  - Clean, high-readability cards.
