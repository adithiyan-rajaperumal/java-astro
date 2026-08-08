# Tamil Localization Fix for Lahiri (Chitra Paksham) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Correct the Tamil translation for Lahiri (Chitra Paksha / Chitra Paksham) across frontend and backend localization files from "லஹிரி (சித்திர பக்கம்)" to "லஹிரி (சித்திர பக்ஷம்)".

**Architecture:** Update localization dictionaries in `frontend/src/i18n/translations.js` and `src/main/resources/i18n/messages_ta.properties`.

**Tech Stack:** JavaScript, Spring Boot ResourceBundle Properties.

## Global Constraints
- The Tamil translation for `ayanamsaLahiri` / `ayanamsa.LAHIRI` must be exactly `"லஹிரி (சித்திர பக்ஷம்)"`.

---

### Task 1: Update Tamil Translation for Lahiri (Chitra Paksham)

**Files:**
- Modify: `frontend/src/i18n/translations.js:355-365`
- Modify: `src/main/resources/i18n/messages_ta.properties:70-76`

- [ ] **Step 1: Update `frontend/src/i18n/translations.js`**
Update line 359:
```javascript
ayanamsaLahiri: "லஹிரி (சித்திர பக்ஷம்)",
```

- [ ] **Step 2: Update `src/main/resources/i18n/messages_ta.properties`**
Update line 74:
```properties
ayanamsa.LAHIRI=லஹிரி (சித்திர பக்ஷம்)
```

- [ ] **Step 3: Run backend test suite**
Run:
```powershell
mvn test -Dtest=MultiPanchangamEngineTest
```
Expected: Tests pass.

- [ ] **Step 4: Run frontend build verification**
Run:
```powershell
cd frontend; npm run build; cd ..
```
Expected: Build passes with 0 errors.

- [ ] **Step 5: Commit changes**
```powershell
git add frontend/src/i18n/translations.js src/main/resources/i18n/messages_ta.properties; git commit -m "fix(i18n): correct tamil translation for lahiri chitra paksham"
```
