# Specification: Pure Unicode Tamil PDF Pre-Shaping & Autonomous AI Ayul/Yoga/Dosham Synthesis

## 1. Overview
1. **Fix PDF Tamil Unicode Rendering**:
   - Eliminate obsolete `BaminiConverter` ASCII transcoding in `PdfExportService.java`.
   - Enable full Unicode visual pre-shaping in `IndicPreShaper.java` for Tamil combining vowel signs (`ெ`, `ே`, `ை`, `ொ`, `ோ`, `ௌ`).
   - Deliver pristine, unbroken Tamil typography in PDF exports via `NotoSansTamil-Regular.ttf`.
2. **Autonomous AI Calculation of Ayul, Yogas & Doshams**:
   - Direct Gemini to independently calculate and determine the native's **Ayurdaya (Longevity Span, Classification & Lifespan Ceiling)**, **Classical Vedic Yogas**, and **Doshams & Shastric Nullifications** directly from the raw planetary positions and 12-Varga charts.
   - Instruct Gemini to forecast unbroken year-by-year life predictions continuously up to **AI's own calculated lifespan ceiling** (or 10 years when 10-year mode is active).
   - Supply pre-computed yearly anchors across the entire potential lifespan (up to age 95+) so Gemini has full planetary context for every year it decides to forecast.

---

## 2. PDF Tamil Unicode Fix Design

### Root Cause
- `PdfExportService.java` loads `NotoSansTamil-Regular.ttf` (a Unicode font), but `buildMixedPhrase` was invoking `BaminiConverter.convert()`, which converted Tamil Unicode into typewriter ASCII keystrokes (`rpdt;`).
- `IndicPreShaper.java` had an explicit bypass `if (hasTamil) return text;`, preventing the necessary visual vowel shuffling for left-side vowel modifiers in primitive PDF engines.

### Solution
1. In `IndicPreShaper.java`:
   - Remove `if (hasTamil) return text;`.
   - Let Tamil characters flow through the vowel-shaping state machine:
     - `ெ` (`\u0BC6`), `ே` (`\u0BC7`), `ை` (`\u0BC8`) moved before the consonant.
     - `ொ` (`\u0BCA`) reshaped to `ெ` + Consonant + `ா` (`\u0BBE`).
     - `ோ` (`\u0BCB`) reshaped to `ே` + Consonant + `ா` (`\u0BBE`).
     - `ௌ` (`\u0BCC`) reshaped to `ெ` + Consonant + `ௗ` (`\u0BD7`).
2. In `PdfExportService.java`:
   - In `buildMixedPhrase`, shape text with `IndicPreShaper.shape(segmentStr)` and render directly with `tamFont` (`NotoSansTamil-Regular.ttf`).
   - Remove `BaminiConverter.convert()`.

---

## 3. Autonomous AI Prompt Synthesis Design (`GeminiPredictionService.java`)

### Prompt Directives Updates:
1. **Longevity & Health (Directive 2)**:
   - Direct Gemini: *"Independently calculate and determine the native's classical Ayurdaya (Longevity) by evaluating the Lagna Lord, 8th Lord, Moon, and Saturn (Ayushkaraka), applying Parashara and Jaimini principles. State the calculated Longevity Classification (Poornayu / Madhyayu / Alpayu) and the exact estimated Lifespan Range & Ceiling Age (e.g. 75 - 90 Years) derived from your astrological evaluation."*
2. **Yogas (Directive 3)**:
   - Direct Gemini: *"Independently identify and calculate all active Classical Vedic Yogas (Gajakesari, Raja Yoga, Dhana Yoga, Vipareeta Raja Yoga, Budhaditya, Neechabhanga, Pancha Mahapurusha, Parivarthana) from the planetary matrix and Varga charts, with forming planets and lifelong impact."*
3. **Doshams (Directive 4)**:
   - Direct Gemini: *"Independently evaluate all major Vedic Doshams (Sevvai/Kuja Dosha, Kala Sarpa Dosha, Pitru Dosha, Papakarthari, Rahu-Ketu afflictions), calculating whether each is active or nullified based on classical cancellation rules, and prescribe authentic Vedic remedies."*
4. **Life Forecast Window (Directive 6)**:
   - Provide pre-computed yearly anchors up to age 95+ (or 10 years in 10-year mode).
   - In `FULL_LIFESPAN` mode: instruct Gemini to forecast continuously year-by-year from current age up to **its own calculated lifespan ceiling**.
   - In `NEXT_10_YEARS` mode: instruct Gemini to forecast in-depth for the next 10 years.
