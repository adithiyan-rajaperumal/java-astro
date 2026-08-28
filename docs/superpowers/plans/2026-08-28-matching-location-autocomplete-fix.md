# Matching Page Location Autocomplete & Card Scroll Fix Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the issue where typing in the Boy or Girl location input on the Matching page triggers a scroll container inside the card and traps the autocomplete dropdown, restoring clean floating overlay behavior with full parity to the Horoscope page.

**Architecture:** Remove `overflow-x: hidden !important;` from `.card` in mobile media query so that the browser does not auto-compute `overflow-y: auto`, and update Boy/Girl location fields in `MatchingPage.jsx` with consistent bottom margins and localized placeholder text.

**Tech Stack:** React (JSX), Vanilla CSS

## Global Constraints
- Strictly isolate edits to `frontend/src/index.css` and `frontend/src/pages/MatchingPage.jsx`.
- Do not modify any other pages, components, backend APIs, or test files.
- Maintain existing visual styles and colors.

---

### Task 1: Fix Mobile Card CSS Overflow Trap in `index.css`

**Files:**
- Modify: `frontend/src/index.css:1007-1017`

**Interfaces:**
- Consumes: `.card` CSS class definitions and `@media (max-width: 600px)` media query
- Produces: Clean non-scroll-trapping `.card` rule on mobile viewports

- [ ] **Step 1: Check existing media query rule in `frontend/src/index.css`**

Review lines 1007-1017:
```css
/* AI Balan & Prediction Card Mobile Safeguards */
@media (max-width: 600px) {
  .card {
    padding: 14px 12px !important;
    overflow-x: hidden !important;
    word-break: break-word !important;
  }
  .grid-2, .grid-3 {
    grid-template-columns: 1fr !important;
    gap: 12px !important;
  }
}
```

- [ ] **Step 2: Update `.card` mobile style in `frontend/src/index.css`**

Remove `overflow-x: hidden !important;` from `.card` so that `overflow-y` is not forced to `auto`:
```css
/* AI Balan & Prediction Card Mobile Safeguards */
@media (max-width: 600px) {
  .card {
    padding: 14px 12px !important;
    word-break: break-word !important;
  }
  .grid-2, .grid-3 {
    grid-template-columns: 1fr !important;
    gap: 12px !important;
  }
}
```

- [ ] **Step 3: Commit CSS change**

```bash
git add frontend/src/index.css
git commit -m "fix(css): remove card mobile overflow-x scroll trap for autocomplete overlay"
```

---

### Task 2: Enhance Matching Page Location Inputs in `MatchingPage.jsx`

**Files:**
- Modify: `frontend/src/pages/MatchingPage.jsx:352-356` and `frontend/src/pages/MatchingPage.jsx:398-402`

**Interfaces:**
- Consumes: `LocationSearch` component from `frontend/src/components/LocationSearch.jsx` and `t()` from `frontend/src/i18n/translations.js`
- Produces: Structured wrapper `<div style={{ marginBottom: '15px' }}>` and `placeholder` attribute matching `BirthForm.jsx`

- [ ] **Step 1: Update Boy's location input wrapper and placeholder**

In `frontend/src/pages/MatchingPage.jsx`:
```jsx
              <div style={{ marginBottom: '15px' }}>
                <label>{t('birthLocation', settings.language)}</label>
                <LocationSearch
                  value={boyLocation}
                  onChange={setBoyLocation}
                  placeholder={t('searchLocation', settings.language) || 'Type city name and select suggestion...'}
                />
              </div>
```

- [ ] **Step 2: Update Girl's location input wrapper and placeholder**

In `frontend/src/pages/MatchingPage.jsx`:
```jsx
              <div style={{ marginBottom: '15px' }}>
                <label>{t('birthLocation', settings.language)}</label>
                <LocationSearch
                  value={girlLocation}
                  onChange={setGirlLocation}
                  placeholder={t('searchLocation', settings.language) || 'Type city name and select suggestion...'}
                />
              </div>
```

- [ ] **Step 3: Commit MatchingPage changes**

```bash
git add frontend/src/pages/MatchingPage.jsx
git commit -m "fix(matching): align location inputs spacing and placeholder with horoscope form"
```

---

### Task 3: Build Verification & Regression Testing

**Files:**
- Test: Build frontend using `npm run build` or `mvn compile`

- [ ] **Step 1: Run frontend build verification**

Run:
```bash
cd frontend; npm run build
```
Expected: Build passes with 0 errors.

- [ ] **Step 2: Verify git diff for strict boundary compliance**

Run:
```bash
git diff HEAD~2 --stat
```
Expected: Only `frontend/src/index.css` and `frontend/src/pages/MatchingPage.jsx` modified.
