# Plan: Ayurvedic Health Context with Autonomous AI Longevity, Yogas & Doshams

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Provide Ayurvedic health calculations to Gemini for physical health context while ensuring Gemini independently evaluates and calculates its own Ayurdaya longevity (classification, ceiling age, and range), Yogas, and Doshams directly from raw astrological chart positions.

**Architecture:** Update `GeminiPredictionService.java` to strip `ayurdayaProfile` and `preCalculatedDiagnostics` from the prompt JSON input payload while preserving `ayurvedicHealthProfile`. Clean up `constructSystemInstruction` to eliminate all baseline anchoring mandates and mandate autonomous synthesis. Verify via `GeminiPredictionServiceTest.java` and full Maven test suite.

**Tech Stack:** Java 17, Spring Boot 3.3.4, JUnit 5, Maven.

## Global Constraints
- Do NOT pass `ayurdayaProfile` or `preCalculatedDiagnostics` in the prompt JSON sent to Gemini.
- Do NOT instruct Gemini to anchor to or treat any pre-calculated diagnostic profiles as baselines.
- Retain `ayurvedicHealthProfile` (Prakriti, Tattvas, organ vulnerabilities) for medical astrology context.
- Ensure lifetime predictions continue to run up to Gemini's calculated lifespan ceiling age (or 10 years in 10-year mode).

---

### Task 1: Update GeminiPredictionService Prompt & System Instruction

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Update `GeminiPredictionService.java`**
  - In `constructAstrologicalPrompt`: Exclude `ayurdayaProfile` and `preCalculatedDiagnostics` from `inputData`. Keep `ayurvedicHealthProfile`.
  - In `constructSystemInstruction`: Remove anchoring directives (`"Ground the longevity classification and lifespan ceiling age strictly in the pre-calculated 'ayurdayaProfile'..."` and `"If preCalculatedDiagnostics is provided in the JSON, use it as your verified baseline."`).

- [ ] **Step 2: Update `GeminiPredictionServiceTest.java`**
  - Verify `constructAstrologicalPrompt` contains `ayurvedicHealthProfile` and excludes `ayurdayaProfile` and `preCalculatedDiagnostics`.

- [ ] **Step 3: Run unit tests**
  - Run `mvn test -Dtest=GeminiPredictionServiceTest`
  - Expected: PASS

- [ ] **Step 4: Commit**
  - Commit changes to git.

---

### Task 2: Full Test Suite Verification and Remote Push

**Files:**
- Test: All tests via `mvn test`

- [ ] **Step 1: Run full Maven test suite**
  - Run `mvn test`
  - Expected: All 55 tests pass (`BUILD SUCCESS`).

- [ ] **Step 2: Push commits to remote origin**
  - Run `git push origin feature/multi-panchangam-systems`
