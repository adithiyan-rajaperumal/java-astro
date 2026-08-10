# Gemini Extended Thinking & Temperature Tuning Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add configurable Extended Thinking (`gemini.thinking-budget`) and temperature tuning (`gemini.temperature: 0.4`) across backend properties, Gemini payload builder, and config endpoint.

**Architecture:** Spring Boot properties binding with default `temperature = 0.4` and `thinkingBudget = 1024`, injected into Gemini REST API `generationConfig.thinkingConfig`, exposed to frontend via `/config`.

**Tech Stack:** Java 17, Spring Boot 3.3.4, Jackson, JUnit 5, React, Vite.

## Global Constraints
- Do not break existing API contracts or prediction models.
- Default `thinkingBudget` to 1024 and `temperature` to 0.4.
- If `thinkingBudget == 0`, omit `thinkingConfig` so non-reasoning models or zero-budget requests work smoothly.

---

### Task 1: Update Configuration Properties & Tests

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/org/vedic/astro/config/GeminiProperties.java`
- Modify: `src/main/java/org/vedic/astro/dto/AppConfigDTO.java`
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java`
- Test: `src/test/java/org/vedic/astro/AppConfigControllerTest.java`

- [ ] **Step 1: Update `application.yml`**
Add `temperature: 0.4` and `thinking-budget: 1024`.

- [ ] **Step 2: Update `GeminiProperties.java`**
Add `temperature` (default `0.4`) and `thinkingBudget` (default `1024`) with getters and setters.

- [ ] **Step 3: Update `AppConfigDTO.java` and `ChartController.java`**
Add `temperature` and `thinkingBudget` to `/api/v1/astrology/config` response.

- [ ] **Step 4: Update `AppConfigControllerTest.java` and verify**
Run `mvn test -Dtest=AppConfigControllerTest`.

- [ ] **Step 5: Commit**
`git commit -m "feat(config): add gemini temperature and thinking-budget configuration"`

---

### Task 2: Update Gemini REST API Generation Payload in `GeminiPredictionService`

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Update `callGeminiApi` in `GeminiPredictionService.java`**
Inject `thinkingConfig` into `generationConfig` when `geminiProperties.getThinkingBudget() > 0` and apply `geminiProperties.getTemperature()`.

- [ ] **Step 2: Add unit test in `GeminiPredictionServiceTest.java`**
Verify `thinkingBudget` and `temperature` properties handling.

- [ ] **Step 3: Run test suite**
Run `mvn test -Dtest=GeminiPredictionServiceTest`.

- [ ] **Step 4: Commit**
`git commit -m "feat(ai): inject thinkingConfig and configured temperature into Gemini API payload"`

---

### Task 3: Full Verification & Automated Tests

- [ ] **Step 1: Run full backend test suite**
Run `mvn test` (all 50+ tests must pass).

- [ ] **Step 2: Run frontend build**
Run `npm run build` in `frontend/`.

- [ ] **Step 3: Commit and Push**
`git push origin feature/multi-panchangam-systems`
