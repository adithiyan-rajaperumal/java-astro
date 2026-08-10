# Ayurdaya Lifespan Determination & Deep Yearly Predictions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement Ayurdaya (classical Vedic longevity determination) in Gemini AI prompts, remove the 3 static domain pill tags from yearly prediction cards, and produce unconstrained, deeply detailed 4-pillar yearly forecasts through the native's calculated lifespan.

**Architecture:** 
1. `frontend/src/components/AiPredictionsView.jsx`: Strip the 3 static pill tags (`Career & Wealth`, `Health & Vitality`, `Family & Marriage`) from year-wise prediction cards to eliminate visual clutter.
2. `GeminiPredictionService.java`: Mandate Two-Phase Ayurdaya Determination in astrological prompt directives, outputting longevity classification (*Alpayu*, *Madhyayu*, *Poornayu*) and generating continuous year-by-year forecasts from `currentYear` through the calculated Ayurdaya ceiling age with rich, unconstrained 4-pillar narratives.
3. Automated verification via `GeminiPredictionServiceTest` and full Maven test suite.

**Tech Stack:** Java 17, Spring Boot 3.3.4, React 19, Vite 8, JUnit 5.

---

### Task 1: Remove Static Domain Indicator Tags from Yearly Prediction Cards

**Files:**
- Modify: `frontend/src/components/AiPredictionsView.jsx:415-430`

- [ ] **Step 1: Remove static pill tags in `AiPredictionsView.jsx`**
Remove lines 416-427 (the quick domain indicator pill tags):
```jsx
{/* Quick Domain Indicator Pill Tags */}
<div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', fontSize: '11px' }}>
  <span ...>💼 ...</span>
  <span ...>🌿 ...</span>
  <span ...>👨‍👩‍👦 ...</span>
</div>
```

- [ ] **Step 2: Build frontend to verify**
Run `npm run build` inside `frontend/`.

- [ ] **Step 3: Commit UI update**
```bash
git add frontend/src/components/AiPredictionsView.jsx
git commit -m "refactor(ui): remove static pill tags from year-wise prediction cards"
```

---

### Task 2: Implement Classical Ayurdaya Longevity Engine & Deep Forecasts in Gemini Prompt

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java:130-310`

- [ ] **Step 1: Update Gemini System Instruction and Prompt Directives**
Enhance prompt directives in `GeminiPredictionService.java`:
1. **Ayurdaya Determination Directive**: Instruct Gemini to systematically evaluate Lagna lord, 8th lord, Saturn (Ayushkaraka), 3rd house, 8th house, and Maraka lords (2nd/7th houses and D30 Trimsamsa).
2. **Longevity Ceiling**: Establish the native's longevity tier (*Alpayu* 0–32, *Madhyayu* 33–66/72, *Poornayu* 72–100+) and synthesize it into `healthAnalysis.longevityVitalitySummary`.
3. **Continuous Lifespan Coverage**: Generate unbroken year-by-year forecasts from current year `currentYear` (Age `currentAge`) up to the calculated Ayurdaya ceiling age.
4. **Unconstrained Deep Narrative**: Ensure `detailedPrediction` is a rich, multi-dimensional narrative synthesizing:
   - (a) Career, Business & Wealth
   - (b) Physical Health & Vitality Realities
   - (c) Marriage, Family & Progeny
   - (d) Parents, Elders & Mindset / Bereavement alerts

- [ ] **Step 2: Run targeted unit tests**
Run `mvn test -Dtest=GeminiPredictionServiceTest`.

- [ ] **Step 3: Commit backend prompt improvements**
```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java
git commit -m "feat(ai): integrate classical Ayurdaya longevity determination and deep yearly forecasts"
```

---

### Task 3: Test Suite Verification and Remote Push

**Files:**
- Modify: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Add Ayurdaya Prompt Verification Test**
Verify that `constructAstrologicalPrompt` contains Ayurdaya longevity instructions and 4-pillar narrative directives.

- [ ] **Step 2: Run full test suite**
Run `mvn test` to verify all 44 unit and integration tests.

- [ ] **Step 3: Commit and Push**
```bash
git add src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "test: add test coverage for Ayurdaya longevity directives"
git push origin feature/multi-panchangam-systems
```
