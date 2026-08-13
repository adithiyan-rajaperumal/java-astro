# Pure Unicode Tamil PDF Pre-Shaping & Autonomous AI Ayul/Yoga/Dosham Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix Tamil Unicode PDF export rendering by removing ASCII Bamini conversion and enabling direct Unicode font shaping in `IndicPreShaper.java`, and update AI prompt directives so Gemini autonomously calculates its own Ayul (longevity classification & lifespan ceiling), Yogas, and Doshams directly from the chart, generating unbroken predictions up to its calculated lifespan ceiling.

**Architecture:** 
- `IndicPreShaper.java`: Shapes Tamil Unicode characters by moving left-vowels (`ெ`, `ே`, `ை`, `ொ`, `ோ`, `ௌ`) before their consonants.
- `PdfExportService.java`: Renders shaped Tamil Unicode directly using `NotoSansTamil-Regular.ttf` with OpenPDF (removing `BaminiConverter`).
- `GeminiPredictionService.java`: Updates generation directives so Gemini independently evaluates Ayurdaya longevity (Poornayu/Madhyayu/Alpayu, lifespan range & ceiling), classical Vedic Yogas, and Doshams/nullifications, with year-by-year predictions covering the full span up to AI's calculated ceiling age (or next 10 years in 10-year mode).

**Tech Stack:** Java 17, OpenPDF, Spring Boot 3.3.4.

## Global Constraints
- `application.yml` retains `gemini.forecast-mode: FULL_LIFESPAN` as default.
- No artificial token ceilings on standard AI prediction generation.
- All Maven unit tests must pass (`mvn test`).

---

### Task 1: Pure Unicode Tamil PDF Shaping & Font Stream Engine

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/IndicPreShaper.java`
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`
- Test: `src/test/java/org/vedic/astro/PdfExportServiceTest.java`

- [ ] **Step 1: Update `IndicPreShaper.java`**
Remove `if (hasTamil) return text;` in `IndicPreShaper.java` so Tamil characters undergo proper vowel modifier re-ordering.

- [ ] **Step 2: Update `PdfExportService.java`**
In `PdfExportService.java`, in `buildMixedPhrase`:
Replace `BaminiConverter.convert(segmentStr)` with `IndicPreShaper.shape(segmentStr)`.

- [ ] **Step 3: Run `PdfExportServiceTest`**
Run: `mvn test "-Dtest=PdfExportServiceTest"`
Expected: PASS.

- [ ] **Step 4: Commit**
```bash
git add src/main/java/org/vedic/astro/util/IndicPreShaper.java src/main/java/org/vedic/astro/service/PdfExportService.java src/test/java/org/vedic/astro/PdfExportServiceTest.java
git commit -m "fix(pdf): render pure Unicode Tamil with pre-shaped vowel glyphs via NotoSansTamil"
```

---

### Task 2: Autonomous AI Prompt Directives for Ayul, Yogas, Doshams & Extended Lifespan Horizon

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Update `GeminiPredictionService.java`**
1. Generate yearly anchors up to age 95+ (or 10 years in 10-year mode) so Gemini has full planetary context for any lifespan it calculates.
2. In prompt Directive 2:
   - Direct Gemini to independently calculate classical Ayurdaya longevity (Poornayu / Madhyayu / Alpayu) and the exact estimated Lifespan Range & Ceiling Age based on Lagna/8th lords, Moon, Saturn, and Jaimini 3-pair analysis.
3. In prompt Directive 3:
   - Direct Gemini to independently identify and calculate all Classical Vedic Yogas (Gajakesari, Raja Yoga, Dhana Yoga, Vipareeta, Neechabhanga, Pancha Mahapurusha, etc.).
4. In prompt Directive 4:
   - Direct Gemini to independently evaluate all major Vedic Doshams (Sevvai/Kuja, Kala Sarpa, Pitru Dosha, etc.) and calculate active vs nullified status with classical cancellation rules.
5. In prompt Directive 6:
   - In `FULL_LIFESPAN` mode: instruct Gemini to forecast unbroken year-by-year predictions continuously from current year/age up to **its own calculated lifespan ceiling age**.

- [ ] **Step 2: Run `GeminiPredictionServiceTest`**
Run: `mvn test "-Dtest=GeminiPredictionServiceTest"`
Expected: PASS.

- [ ] **Step 3: Commit**
```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(ai): empower Gemini with autonomous Ayurdaya, Yoga, Dosham calculation and dynamic lifespan ceiling"
```

---

### Task 3: Full Test Suite Verification & Validation

**Files:**
- Test: `mvn test`

- [ ] **Step 1: Run full Maven test suite**
Run: `mvn test`
Expected: 55+ tests pass (`BUILD SUCCESS`).

- [ ] **Step 2: Push to remote repository**
```bash
git push origin feature/multi-panchangam-systems
```
