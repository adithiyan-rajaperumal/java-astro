# JSON-Structured Astrological Prompts Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor `GeminiPredictionService.java` prompt builders to construct clean, unambiguous JSON input payloads across Lifetime Balan, Daily Balan, and Marriage Matching.

**Architecture:** Use Jackson `ObjectMapper` or helper maps to format the calculated astrological matrix into a clean, pretty-printed JSON block embedded in each prompt request.

**Tech Stack:** Java 17, Spring Boot 3, Jackson `ObjectMapper`, JUnit 5.

---

### Task 1: Refactor Lifetime Balan Prompt to JSON Input Format

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write/update unit tests for Lifetime Balan JSON input prompt**
- [ ] **Step 2: Implement JSON input generation for `constructAstrologicalPrompt`**
- [ ] **Step 3: Run unit tests to verify pass**

---

### Task 2: Refactor Daily Balan Prompt to JSON Input Format

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write/update unit tests for Daily Balan JSON input prompt**
- [ ] **Step 2: Implement JSON input generation for `constructDailyAstrologicalPrompt`**
- [ ] **Step 3: Run unit tests to verify pass**

---

### Task 3: Refactor Marriage Matching Prompt to JSON Input Format

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Write/update unit tests for Marriage Matching JSON input prompt**
- [ ] **Step 2: Implement JSON input generation for `constructMatchingPrompt`**
- [ ] **Step 3: Run unit tests to verify pass**

---

### Task 4: Full Test Suite Verification

- [ ] **Step 1: Run full Maven test suite (`mvn test`)**
- [ ] **Step 2: Verify frontend compilation (`npm run build`)**
