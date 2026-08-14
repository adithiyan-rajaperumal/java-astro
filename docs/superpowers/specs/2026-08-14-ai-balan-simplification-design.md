# Simplified AI Balan Engine & Autonomous Synthesis Design Specification

**Date:** 2026-08-14  
**Status:** Approved for Implementation  
**Topic:** Simplified AI Balan Tab with Autonomous Ayul, Yogas, Retrospective Milestones, and Uncapped Rich Year-by-Year Narrative  

---

## 1. Overview & Objectives

The goal is to streamline and modernize the **AI Balan (AI Horoscope Predictions)** engine:
1. **Rich & Uncapped Annual Narrative**: Eliminate fragmented, disjointed bullet points in favor of an immersive, coherent, full-length story paragraph for each year weaving together Health, Wealth, Career, Education, Spouse, Children, Parents, Investments, and Business.
2. **Autonomous Astrological Analysis**: The AI autonomously determines **Ayul (Longevity lifespan ceiling)**, detects active **Yogams & Doshas**, and explains them classically from raw planetary positions, Shodashavarga charts, Shadbala, and Dasa timing.
3. **Core Life Dimensions**:
   - **🧠 குணம் & நடத்தை (Personality & Behavioral Nature)**: In-depth psychological profile, decision-making style, and innate strengths/vulnerabilities.
   - **🕰️ கடந்து வந்த முக்கிய நிகழ்வுகள் (Retrospective Life Milestones Till Date)**: 2–3 notable past life milestones that have occurred up to the current date (age-adaptive for adults vs infants/children) to validate astrological timing.
   - **⏳ சாஸ்திர ஆயுள் கணிப்பு (AI Ayul & Longevity Analysis)**: AI-calculated lifespan ceiling, classification (Poornayu, Madhyayu, Alpayu), and classical rationale.
   - **📜 ஆண்டு வாரியான முழுமையான பலன்கள் (Year-by-Year Predictions)**: Paragraph narrative for each year based on the active forecast mode.
4. **Forecast Modes**:
   - **10-Year Mode (`isTenYears = true`)**: Predicts the next 10 years (`currentYear` to `currentYear + 9`).
   - **Lifetime Mode (`isTenYears = false`)**: Predicts from `currentYear` up to `birthYear + calculatedAyulCeiling`.
5. **Token Efficiency & Speed**: Prune redundant explanatory JSON bloat while retaining our exact, mathematically proven disambiguated house and varga structures to deliver fast response times (~10–15s).

---

## 2. Backend Input Payload Architecture

### 2.1 Ephemeris Anchors & Native Profile
- `name`, `dateOfBirth`, `timeOfBirth`, `placeOfBirth`, `gender`.
- `currentYear` (e.g., 2026), `currentAge` (e.g., 31).
- `lagnaSign`, `moonSign`, `nakshatra`, `pada`, `tithi`, `yoga`, `karana`, `panchangamSystem`.
- `forecastMode`: `"TEN_YEARS"` or `"LIFETIME"`.

### 2.2 Pre-calculated 12-House Table (`houseLordshipTable`)
Guarantees the AI never calculates incorrect house offsets:
- `houseNumber` (1 to 12 from Lagna).
- `signName` (e.g., `"Tula"`, `"Kataka"`).
- `signNumber` (1 to 12 from Aries).
- `houseLord` (e.g., `"Venus"`, `"Moon"`).
- `occupants` (e.g., `["Sun", "Mercury"]`).
- `lordshipClarification` (e.g., `"Moon is the sole lord of House 10. Occupants [Sun, Mercury] are guests/occupants."`).

### 2.3 Disambiguated Planetary Matrix (`planetaryMatrix`)
For each of the 9 Grahas:
- `planet` (e.g., `"Sun"`, `"Moon"`, `"Mars"`, ...).
- `placedInD1Sign` (e.g., `"Kataka (Sign 4)"`).
- `placedInD1House` (1 to 12 from Lagna, e.g., `10`).
- `rulesHouses` (e.g., `[11]`).
- `lordshipTitle` (e.g., `"11th Lord (Labhadhipati)"`).
- `occupantRole` (e.g., `"Placed in House 10 (Occupant/Guest, NOT the 10th Lord)"`).
- `placedInD9NavamsaSign` (e.g., `"Mithuna"`).
- `d1Dignity`, `d9Dignity` (`EXALTED`, `DEBILITATED`, `OWN_SIGN`, `FRIENDLY`, `NEUTRAL`, etc.).
- `isCombust` (true/false), `isRetrograde` (true/false), `isVargottama` (true/false).
- `primaryDosha` (Ayurvedic dosha & governing bodily dhatu).

### 2.4 Complete Shodashavarga Divisional Charts (`divisionalCharts`)
Includes Varga Lagna and explicit house notation `(H#)` for every divisional chart:
- **D1 (Rasi)**: Physical body, vitality, overall destiny.
- **D2 (Hora)**: Wealth, finance, monetary assets.
- **D3 (Drekkana)**: Siblings, vitality, courage, third-house energy.
- **D7 (Saptamsa)**: Children, progeny, lineage.
- **D9 (Navamsa)**: Spouse, marriage, dharma, planetary core strength.
- **D10 (Dasamsa)**: Career, profession, reputation, status.
- **D12 (Dwadasamsa)**: Parents, ancestry, lineage karma.
- **D20 (Vimsamsa)**: Spiritual progress, devotion, upasana.
- **D24 (Siddhamsa)**: Higher education, learning, intellectual accomplishments.
- **D30 (Trimsamsa)**: Health vulnerabilities, hidden arishta, misfortunes.
- **D60 (Shashtyamsa)**: Deep past-life karma and subtle destiny.

*Format Example:*
```json
"divisionalCharts": {
  "D9_Navamsa_Dharma_Spouse": {
    "Lagna": "Kanya",
    "Sun": "Mithuna (H10)",
    "Moon": "Mesha (H8 - Vargottama)",
    "Mars": "Vrishabha (H9)",
    "Mercury": "Kanya (H1 - Exalted)",
    "Jupiter": "Kumbha (H6)",
    "Venus": "Meena (H7 - Exalted)",
    "Saturn": "Kataka (H11)",
    "Rahu": "Kumbha (H6)",
    "Ketu": "Simha (H12)"
  }
}
```

### 2.5 Shadbala Scores & Dasa-Bhukthi Chronology
- `shadbalaSummary`: Rupas, ratio, and strength tier for Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn.
- `dasaBhukthiTimeline`: Chronological list of active and upcoming Dasa-Bhukthi periods with exact start/end dates and age intervals.

### 2.6 Explicitly Excluded from Payload
- ❌ Pre-calculated Ayurdaya numbers (ensuring pure AI autonomous synthesis).
- ❌ Pre-detected Yogas & Doshas (AI detects and activates them in context).

---

## 3. AI System Instructions & Prompt Rules

1. **Strict Notation Guide**:
   - `(H#)` in `divisionalCharts` denotes the House number (1–12) from that specific Varga's Lagna.
   - `Zodiac Sign Number (1–12 from Aries)` is NEVER the house number unless Lagna is Aries. Always use `placedInD1House` for D1 houses.
2. **Age-Adaptive Milestone Handling**:
   - For adults (Age > 18): Reconstruct 2–3 major past milestones in education, career inflection points, marriage, or family transitions with approximate years/ages.
   - For children/infants (Age < 6): Focus on birth conditions, early vitality/health milestones, and parental dynamics around birth.
3. **Uncapped Rich Narrative Paragraphs**:
   - Each year in `yearlyPredictions` must be a rich, comprehensive paragraph narrative weaving together career/business, wealth/investments, health/vitality, family (spouse, kids, parents), and education. No forced sentence or word caps.
4. **Language & Script Uniformity**:
   - 100% of all textual values must be rendered in the native script of the selected language (`ta`, `en`, `hi`, `te`, `kn`, `ml`), while JSON keys remain in standard camelCase English.

---

## 4. AI Response JSON Schema

```json
{
  "aiLongevityAnalysis": {
    "calculatedAyulCeiling": 78,
    "classification": "Poornayu | Madhyayu | Alpayu",
    "primarySpanRationale": "Classical explanation synthesizing Jaimini 3-pair modality, Jupiter Kendra/Trikona placement, Ayushkaraka Saturn dignity, and 8th house lordship.",
    "activeYogasIdentified": [
      {
        "yogaName": "Gaja Kesari Yoga",
        "effect": "Jupiter in mutual Kendra to Moon conferring wisdom, wealth, and societal respect."
      }
    ],
    "activeDoshasIdentified": [
      {
        "doshaName": "Ketu in 7th House",
        "remedialAdvice": "Patience in marital communication and regular spiritual grounding."
      }
    ]
  },
  "personalityAndBehavior": {
    "coreTemperament": "Comprehensive analysis of the native's psychological nature, mental inclinations, emotional temperament, decision-making style, and innate behavioral strengths."
  },
  "retrospectivePastMilestones": [
    {
      "approxPeriod": "2018–2020 (Age ~23-25)",
      "milestoneTitle": "Higher Education & Major Career Pivot",
      "eventNarrative": "A defining transition in academic pursuit or professional initiation driven by Dasa shift."
    },
    {
      "approxPeriod": "2022–2023 (Age ~27-28)",
      "milestoneTitle": "Key Relocation / Family Milestone",
      "eventNarrative": "Significant personal milestone, geographical relocation, or major asset acquisition."
    }
  ],
  "yearlyPredictions": [
    {
      "year": 2026,
      "age": 31,
      "activeDasaBhukthi": "Rahu Dasa - Saturn Bhukthi",
      "annualNarrative": "A rich, comprehensive story paragraph for the entire year weaving together Career & Business (D10), Wealth & Investments (D2), Health & Vitality (D1/D30), Family (Spouse/Marriage in D9, Children in D7, Parents in D12), and Educational/Spiritual progress."
    }
  ]
}
```

---

## 5. Frontend UI/UX Architecture (`AiPredictionView.jsx`)

1. **Header & Mode Controls**:
   - Forecast Scope Selector: **📅 அடுத்த 10 ஆண்டுகள் (Next 10 Years)** vs **🌟 முழு ஆயுள் பலன் (Lifetime Balan)**.
   - Force Refresh Button with loading indicator.
2. **🧠 குணம் & நடத்தை (Personality & Behavior Card)**:
   - Highlighting the native's psychological nature, temperament, and behavioral traits.
3. **🕰️ கடந்து வந்த முக்கிய நிகழ்வுகள் (Retrospective Past Life Milestones)**:
   - Visual milestone cards validating past events with approximate periods, titles, and astrological roots.
4. **⏳ சாஸ்திர ஆயுள் கணிப்பு (AI Ayul & Longevity Analysis Card)**:
   - Longevity classification badge, ceiling age (~78 years), classical rationale, and active Yogas/Doshas chips.
5. **📜 ஆண்டு வாரியான முழுமையான பலன்கள் (Yearly Predictions Narrative Stream)**:
   - Chronological year cards displaying Year, Age, Active Dasa-Bhukthi badge, and the complete unified annual narrative paragraph.
6. **Full 6-Language Support**:
   - All section headers, labels, and badges translated across `en`, `ta`, `hi`, `te`, `kn`, `ml`.

---

## 6. Verification Plan

### 6.1 Automated Backend Tests
- `GeminiPredictionServiceTest.java`:
  - Verify prompt payload generation excludes `ayurdaya`, `detectedYogas`, and `detectedDoshas`.
  - Verify `divisionalCharts` includes `Lagna` and `(H#)` for D1 through D60.
  - Verify `houseLordshipTable` and `planetaryMatrix` retain all disambiguated house numbers.
  - Verify parsing of `aiLongevityAnalysis`, `personalityAndBehavior`, `retrospectivePastMilestones`, and `yearlyPredictions`.
- Full Maven test suite: `mvn test` (must pass all tests).

### 6.2 Frontend Verification
- Audit all i18n keys across all 6 languages (`check_all_i18n_keys.js` $\rightarrow$ 0 missing keys).
- Production build: `npm run build` in `frontend/`.
- Validate against all 5 core benchmark natives:
  1. **Adithiyan** (19-07-1995 13:10 Vellore, TN)
  2. **Uthayasri** (17-08-2002 15:15 Viluppuram, TN)
  3. **Padmasri** (31-07-2001 19:30 Viluppuram, TN)
  4. **Deepanathan** (11-04-1969 02:50 AM Tiruvannamalai, TN)
  5. **Mahaveer** (18-04-2024 06:37 AM Vellore, TN)
