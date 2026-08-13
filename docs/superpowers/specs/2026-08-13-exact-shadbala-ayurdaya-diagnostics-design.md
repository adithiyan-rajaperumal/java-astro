# Exact Classical Shadbala, Parashara-Jaimini Ayurdaya, & Zero-False-Positive Diagnostics Engine

**Date:** 2026-08-13
**Author:** Pair Programming Agent & User
**Status:** Approved Specification

---

## 1. Problem Statement

1. **Hardcoded Shadbala Strengths:** In `ShadbalaService.java`, all planetary strengths have hardcoded values: `kalaBala(60.0)`, `cheshtaBala(45.0)`, and `drigBala(15.0)`, producing fabricated Shadbala metrics across UI dashboards, PDF export documents, and the AI Balan prompt payload.
2. **Ayurdaya (Ayul / Longevity) Calculation Failure:** The AI Balan engine attempts to estimate longevity without a deterministic mathematical engine, and with corrupt Shadbala inputs, causing wild, inaccurate lifespan estimations and timeline contradictions.
3. **Yoga and Dosham False Positives:** The LLM generates false-positive Vedic Yogas (e.g. false Gajakesari, false Pancha Mahapurusha in non-Kendra houses, false Budhaditya in different signs) and fails to apply classical exceptions to Doshams (such as Sevvai Dosham cancellations).

---

## 2. Technical Architecture & Scope

```
+-----------------------------------------------------------------------------------+
|                            Astrological Engine Core                               |
+------------------------------------+----------------------------------------------+
                                     |
    +--------------------------------+--------------------------------+
    |                                |                                |
    v                                v                                v
+-----------------------+  +-----------------------+  +-------------------------------+
|  ShadbalaService      |  | AyurdayaCalculation   |  | AstrologyDiagnosticsService   |
|  (Exact 6-Fold BPHS)  |  | (Parashara + Jaimini) |  | (Pre-computed Yogas & Doshams)|
+-----------+-----------+  +-----------+-----------+  +---------------+---------------+
            |                          |                              |
            +--------------------------+------------------------------+
                                       |
                                       v
                     +-----------------------------------+
                     |  ChartOrchestrationService /      |
                     |  GeminiPredictionService          |
                     |  (Payload: exact mathematical     |
                     |   Shadbala, Ayurdaya & reference) |
                     +-----------------+-----------------+
                                       |
                     +-----------------+-----------------+
                     |                 |                 |
                     v                 v                 v
               +-----------+     +-----------+     +-----------+
               | Dashboard |     |    PDF    |     |  AI Balan |
               | UI        |     |  Reports  |     |  Prompt   |
               +-----------+     +-----------+     +-----------+
```

---

## 3. Detailed Component Specifications

### 3.1. Exact Classical Shadbala Engine (`ShadbalaService.java`)

Each of the 7 classical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn) will have their 6 constituent Balas calculated in Virupas (60 Virupas = 1 Rupa):

#### A. Sthana Bala (Positional Strength)
1. **Uchcha Bala (Exaltation)**:
   $$\text{UchchaBala} = 60.0 \times \frac{180.0 - |\text{AbsLong} - \text{DebilitationPoint}|}{180.0}$$
   - Deep exaltation longitudes: Sun $10^\circ$ (Aries), Moon $33^\circ$ (Taurus), Mars $298^\circ$ (Capricorn), Mercury $165^\circ$ (Virgo), Jupiter $95^\circ$ (Cancer), Venus $357^\circ$ (Pisces), Saturn $200^\circ$ (Libra).
2. **Saptavargiya Bala (D1, D2, D3, D7, D9, D12, D30)**:
   - Evaluates dignity in 7 vargas: Moolatrikona (45 V), Own Sign (30 V), Great Friend (22.5 V), Friend (15 V), Neutral (7.5 V), Enemy (3.75 V), Great Enemy (1.875 V).
3. **Ojhayugmarasyamsa Bala (Odd/Even Signs & Navamsas)**:
   - Male planets (Sun, Mars, Jupiter, Mercury) in Odd Signs $\rightarrow$ 15 V; in Odd Navamsas $\rightarrow$ 15 V.
   - Female planets (Moon, Venus) in Even Signs $\rightarrow$ 15 V; in Even Navamsas $\rightarrow$ 15 V.
4. **Kendradi Bala**: Kendra (Houses 1, 4, 7, 10) = 60.0 V; Panaphara (2, 5, 8, 11) = 30.0 V; Apoklima (3, 6, 9, 12) = 15.0 V.
5. **Drekkana Bala**: Male planets in 1st decanate (0–10°) = 15 V; Hermaphrodite in 2nd (10–20°) = 15 V; Female in 3rd (20–30°) = 15 V.

#### B. Dig Bala (Directional Strength)
- Power points: Jupiter & Mercury at Lagna ($0^\circ$), Sun & Mars at 10th Cusp ($270^\circ$), Saturn at 7th Cusp ($180^\circ$), Moon & Venus at 4th Cusp ($90^\circ$).
- $\text{DigBala} = \frac{180.0 - \text{Distance to Powerless Point}}{3.0}$ (Range: 0.0 to 60.0 Virupas).

#### C. Kala Bala (Temporal Strength)
1. **Nathonatha Bala**:
   - Day birth (Sun in houses 7–12): Sun, Jupiter, Venus = 60 V; Moon, Mars, Saturn = 0 V; Mercury = 60 V.
   - Night birth (Sun in houses 1–6): Moon, Mars, Saturn = 60 V; Sun, Jupiter, Venus = 0 V; Mercury = 60 V.
2. **Paksha Bala**:
   - Benefics (Jupiter, Venus, waxing Moon, well-associated Mercury) get $\frac{(\text{MoonLong} - \text{SunLong} + 360) \bmod 360}{3.0}$ (0 to 60 V).
   - Malefics get $60.0 - \text{Benefic Paksha Bala}$.
3. **Tribhaga Bala**: Lord of active 1/3 segment of Day or Night gets 60 V.
4. **Varsha/Masa/Dina/Hora Lords**: Lord of the year (15 V), month (30 V), day (45 V), planetary hora (60 V).
5. **Ayana Bala**: Declination-based power (Uttarayana vs. Dakshinayana).

#### D. Cheshta Bala (Motional Strength)
- Mars, Mercury, Jupiter, Venus, Saturn:
  - Retrograde (Vakra): 60.0 V
  - Stationary / Retrogression threshold (Anuvakra): 30.0 V
  - Fast/Direct (Chara / Sama): Scaled from 15 to 45 V based on speed relative to mean daily velocity.
  - Slow (Manda): 15.0 V.
- Sun & Moon: Ayana Bala is taken as Cheshta Bala per BPHS.

#### E. Naisargika Bala (Natural Luster - Fixed BPHS constant)
- Sun: 60.00 V (1.000 Rupa)
- Moon: 51.43 V (0.857 Rupa)
- Venus: 42.86 V (0.714 Rupa)
- Jupiter: 34.29 V (0.571 Rupa)
- Mercury: 25.71 V (0.428 Rupa)
- Mars: 17.14 V (0.286 Rupa)
- Saturn: 8.57 V (0.143 Rupa)

#### F. Drig Bala (Aspectual Strength)
- Full classical Parasari planetary drishti (7th aspect for all, special 4/8 for Mars, 5/9 for Jupiter, 3/10 for Saturn).
- Benefic aspects add positive Virupas, malefic aspects subtract Virupas.

---

### 3.2. Deterministic Parashara-Jaimini Ayurdaya Engine (`AyurdayaCalculationUtils.java`)

Calculates exact longevity spans and maraka vulnerability windows:

1. **Classical 3-Pair Modality Evaluation**:
   - Modalities: Movable (1, 4, 7, 10), Fixed (2, 5, 8, 11), Dual (3, 6, 9, 12).
   - **Pair 1**: Lagna Lord & 8th Lord sign modalities.
   - **Pair 2**: Moon & Saturn sign modalities.
   - **Pair 3**: Lagna & Hora Lagna (or Moon) sign modalities.
   - Standard 3-tier rules determine base category: **Alpayu** (0–36 yrs), **Madhyayu** (36–70 yrs), **Poornayu** (70–100+ yrs).
2. **Kakshya Vriddhi & Hrasa Adjustments**:
   - Jupiter in Kendra/Trikona or aspecting Lagna $\rightarrow$ **+1 Kakshya tier or +8 years**.
   - Lagna Lord or 8th Lord in Own/Exalted sign with Shadbala $> 1.0$ ratio $\rightarrow$ vitality extension.
   - Unmitigated malefics in 8th or debilitated Lagna Lord $\rightarrow$ Hrasa reduction.
3. **Maraka & Badhaka Dasa-Bhukthi Alignment**:
   - Cross-references with 2nd Lord (Maraka), 7th Lord (Maraka), Badhaka Lord (11th for Movable, 9th for Fixed, 7th for Dual) and 8th/12th lords against Vimshottari timeline to pin the exact lifespan ceiling (e.g. `82-86 years`).
4. **Structured JSON Output (`ayurdayaProfile`)**:
   - Injected into AI Balan prompt payload with full classical proof and rationale.

---

### 3.3. Zero-False-Positive Yogas & Doshams Architecture

1. **Pre-computed Diagnostics Injection (`preCalculatedDiagnostics`)**:
   - The verified outputs from `AstrologyDiagnosticsService` (which evaluates Gajakesari, Budhaditya, Pancha Mahapurusha, Dharma-Karmadhipati, Sevvai with nullifications, Kala Sarpa, Pitru, etc.) are injected into `inputData`.
2. **Strict System Prompt Constraints**:
   - Strict definition rules for Gajakesari (Kendra from Moon), Budhaditya (Same sign, non-combust), Pancha Mahapurusha (Kendra from Lagna in Own/Exalted sign only), Dharma-Karmadhipati (9th & 10th lords of specific Lagna), and Sevvai Dosham cancellation criteria.
   - Prompt mandates: *"You MUST verify that any proposed yoga strictly satisfies classical geometric conditions from the JSON matrix. Do NOT hallucinate yogas if conditions are not met."*

---

## 4. Verification & Testing Plan

1. **Shadbala Calculation Unit Tests (`ShadbalaServiceTest.java`)**:
   - Verify non-zero, dynamic values for Sthana, Dig, Kala, Cheshta, Naisargika, and Drig Bala across different charts.
   - Verify that Naisargika Bala precisely matches BPHS constants.
   - Verify that deep exaltation yields $\approx 60.0$ Uchcha Bala and deep debilitation yields $0.0$.
2. **Ayurdaya Engine Tests (`AyurdayaCalculationUtilsTest.java`)**:
   - Test 3-Pair determinations for known Movable, Fixed, and Dual Lagna combinations.
   - Test Kakshya Vriddhi with Jupiter in Kendra.
   - Test exact lifespan ceiling and Maraka Dasa alignment.
3. **AI Balan Prompt & Smoke Tests (`GeminiPredictionServiceTest.java`)**:
   - Verify `shadbalaStrengths`, `ayurdayaProfile`, and `preCalculatedDiagnostics` are properly serialized into the JSON prompt.
   - Run full Maven build (`mvn test`) to ensure all tests pass.
