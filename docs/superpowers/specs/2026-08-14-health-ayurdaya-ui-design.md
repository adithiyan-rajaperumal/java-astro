# Specification: Deterministic Health & Ayurdaya Engine Exposure with Dedicated UI Tab & AI Synthesis

## 1. Overview
Expose the classical **Ayurvedic Health Diagnostics** (Prakriti, Vata/Pitta/Kapha proportions, 6th house Roga Sthana, organ vulnerabilities, dietary guidelines) and **Parashara-Jaimini Ayurdaya Longevity Engine** (3-Pair evaluation, Kakshya adjustments, Longevity classification, Lifespan range, Maraka analysis) directly in the application UI as a dedicated tab/card alongside Horoscope & Charts, while passing the exact calculated data to the AI Balan synthesizer for rich, narrative synthesis with zero mathematical hallucinations.

---

## 2. Core Architecture & Data Flow

```
+--------------------------------------------------------------------------------+
|                 Classical Vedic Calculation Engine                             |
|  - AyurvedicAstrologyUtils.calculateHealthProfile(...)                        |
|  - AyurdayaCalculationUtils.calculateAyurdaya(...)                            |
|  - YogaCalculator.detectYogas(...)                                            |
|  - DoshaCalculator.calculateComprehensiveDoshams(...)                         |
+--------------------------------------------------------------------------------+
                                   |
                  +----------------+----------------+
                  |                                 |
                  v                                 v
+-----------------------------------+   +------------------------------------+
| Deterministic Web UI & PDF Cards  |   | AI Life Balan Synthesis (Gemini)   |
| - Interactive Vata/Pitta/Kapha    |   | - Receives exact calculated JSON   |
|   visual progress bars            |   | - Writes eloquent interpretations  |
| - Longevity Span & Ceiling Badge  |   | - Explains nuances & remedies      |
| - 3-Pair Jaimini Longevity Table  |   | - 100% Shastric accuracy           |
| - Organ Vulnerability Badges      |   | - Zero calculation hallucination   |
| - Ayurvedic Dietary Directives    |   +------------------------------------+
+-----------------------------------+
```

---

## 3. Backend Model & DTO Updates

### 1. `ChartUiResponseDTO.java` & `ComprehensiveReportDTO.java`
Attach the pre-computed profiles to the chart calculation response:
```java
private AyurvedicAstrologyUtils.AyurvedicHealthProfile ayurvedicHealth;
private AyurdayaCalculationUtils.AyurdayaProfile ayurdayaProfile;
```

### 2. `ChartOrchestrationService.java`
When assembling `ChartUiResponseDTO` and `ComprehensiveReportDTO`:
```java
var health = AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSign, moonSign, d1Positions);
var ayurdaya = AyurdayaCalculationUtils.calculateAyurdaya(lagnaSign, moonSign, d1Positions, dasas, birthYear);
response.setAyurvedicHealth(health);
response.setAyurdayaProfile(ayurdaya);
```

---

## 4. Frontend UI Design (`HoroscopePage.jsx` & New Component)

Add a new dedicated Sub-Tab in the Horoscope Report view: **"🌿 Health & Longevity" (உடல்நலம் & ஆயுள் கணிப்பு)**.

### Sub-Tab UI Components:
1. **Longevity (Ayurdaya) Hero Card**:
   - **Longevity Classification Badge**: e.g., `Poornayu (தீர்க்காயுள் - 75–100+ Years)`, `Madhyayu (மத்தியாயுள் - 36–75 Years)`.
   - **Calculated Span & Ceiling**: e.g., `Lifespan Range: 75 - 95 Years (Ceiling: 88 Years)`.
   - **Classical Jaimini 3-Pair Table**:
     - Pair 1: Lagna Lord Sign Modality & 8th Lord Sign Modality
     - Pair 2: Moon Sign Modality & Saturn Sign Modality
     - Pair 3: Lagna Sign Modality & Hora Lagna Modality
   - **Kakshya Adjustments List**: Jupiter/Saturn exaltations, benefic aspect additions.
   - **Maraka Watch Window**: Critical dasa-bhukthi periods for caution.

2. **Ayurvedic Constitution (Prakriti) Card**:
   - **Dominant Prakriti Tag**: e.g. `Pitta-Kapha (பித்த-கபம்)`.
   - **Visual Dosha Breakdown Progress Bars**:
     - 🔵 **Vata (காற்று/வாதம்)**: `35%`
     - 🔴 **Pitta (அக்னி/பித்தம்)**: `45%`
     - 🟢 **Kapha (நீர்/கபம்)**: `20%`
   - **Lagna Element & Roga Sthana (6th House) Indicator**: Roga sign, Roga Lord dignity.

3. **Organ Vulnerabilities & Medical Astrology Directives**:
   - Chips for vulnerable organ systems (e.g., `Digestive Tract`, `Joints & Bone Density`, `Bile & Liver`).
   - Actionable Ayurvedic Dietary Guidelines & Lifestyle Recommendations.

---

## 5. Multi-Lingual Translations (`translations.js` & `messages*.properties`)
Add keys across all 6 supported languages (EN, TA, HI, KN, TE, ML):
- `healthAndLongevityTab`: "Health & Longevity" / "உடல்நலம் & ஆயுள்"
- `prakritiTitle`: "Ayurvedic Prakriti & Constitution" / "ஆயுர்வேத பிரகிருதி & உடல்வாகு"
- `longevityTitle`: "Ayurdaya Longevity Determination" / "சாஸ்திர ஆயுள் நிர்ணயம்"
- `threePairsTitle`: "Parashara-Jaimini 3-Pair Evaluation" / "பராசர-ஜைமினி மூன்று ஜோடி பகுப்பாய்வு"
- `organVulnerabilitiesTitle`: "Vulnerable Organ Systems" / "பாதிக்கப்படக்கூடிய உறுப்புகள்"
- `dietLifestyleTitle`: "Ayurvedic Diet & Lifestyle Directives" / "ஆயுர்வேத உணவு & வாழ்க்கை முறை வழிகாட்டல்"
- `vata`: "Vata" / "வாதம்"
- `pitta`: "Pitta" / "பித்தம்"
- `kapha`: "Kapha" / "கபம்"
- `poornayu`: "Poornayu (Full Lifespan 75–100+)" / "தீர்க்காயுள் (75–100+ வயது)"
- `madhyayu`: "Madhyayu (Medium Lifespan 36–75)" / "மத்தியாயுள் (36–75 வயது)"
- `alpayu`: "Alpayu (Short Lifespan 0–35)" / "அல்பாயுள் (0–35 வயது)"
