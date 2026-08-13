# Ayurvedic Prakriti & Unified Planetary Matrix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement Parashara + Charaka Samhita Ayurvedic Tridosha calculation engine, unified `planetaryMatrix` (with explicit `rulesHouses` vs `placedInD1House`), and enhanced `houseLordshipTable` (with `occupantPlanets`) in `GeminiPredictionService.java`.

---

### Task 1: Create `AyurvedicAstrologyUtils` Calculation Utility

**Files:**
- Create: `src/main/java/org/vedic/astro/util/AyurvedicAstrologyUtils.java`
- Test: `src/test/java/org/vedic/astro/AyurvedicAstrologyUtilsTest.java`

- [ ] **Step 1: Write unit tests for Ayurvedic Tridosha scoring and organ vulnerabilities**
- [ ] **Step 2: Implement `AyurvedicAstrologyUtils.java` with Parashara + Charaka Samhita formulas**
- [ ] **Step 3: Run unit tests to verify pass**

---

### Task 2: Refactor Lifetime Balan Prompt with Unified Planetary Matrix and Ayurvedic Health Profile

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write/update unit tests for unified `planetaryMatrix` and `ayurvedicHealthProfile`**
- [ ] **Step 2: Update `constructAstrologicalPrompt` and `constructSystemInstruction` in `GeminiPredictionService.java`**
- [ ] **Step 3: Run unit tests to verify pass**

---

### Task 3: Refactor Marriage Matching and Daily Balan with Enhanced Planetary Matrix

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Update `buildMatchingProfileJson` and `constructDailyAstrologicalPrompt` with explicit `rulesHouses` vs `placedInHouse`**
- [ ] **Step 2: Run unit tests to verify pass**

---

### Task 4: Full Regression & Smoke Testing

- [ ] **Step 1: Run full Maven test suite (`mvn test`)**
- [ ] **Step 2: Verify frontend compilation (`npm run build`)**
