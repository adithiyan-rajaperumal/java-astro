# Matching Page Location Autocomplete & Card Scroll Fix - Design Spec

## Problem Summary
When typing a location in the Boy's or Girl's card on the Matching page, the card container enters a scroll state, causing the autocomplete suggestions dropdown to get trapped inside a cramped, scrollable box rather than floating smoothly above subsequent sections. In contrast, the Horoscope page (`BirthForm`) displays suggestions as a floating overlay without forcing card scrollbars.

## Root Cause Analysis
1. **CSS Overflow Trap (`index.css`)**:
   Under `@media (max-width: 600px)`, `.card` specifies `overflow-x: hidden !important;`. Under the CSS standard, applying `overflow-x: hidden` automatically computes `overflow-y` to `auto` whenever it was `visible`. Consequently, `.card` becomes a vertical scroll container. When `.autocomplete-dropdown` expands below the input at the bottom of the card, it triggers vertical scrolling on the card.
2. **Matching Page Form Layout (`MatchingPage.jsx`)**:
   The location fields in Boy's and Girl's cards are rendered without bottom margin spacing and without explicit placeholder hints, unlike `BirthForm.jsx` on the Horoscope page.

## Requirements & Scope
- **Strict Isolation**: Only touch `frontend/src/index.css` and `frontend/src/pages/MatchingPage.jsx`. Do not touch backend services or other frontend pages/components.
- **Card Scrolling Behavior**: Disable unintended `.card` scrollbars by removing `overflow-x: hidden !important` from the mobile media query.
- **Autocomplete Overlay**: Ensure `.autocomplete-dropdown` overlays cleanly on top of subsequent form cards (`z-index: 1050`, absolute positioning, solid background).
- **Parity with Horoscope Page**: Update Boy and Girl location inputs on `MatchingPage.jsx` with consistent bottom margins (`marginBottom: '15px'`) and localized placeholder text (`t('searchLocation', settings.language)`).

## Proposed Changes

### 1. `frontend/src/index.css`
- In the mobile media query (`@media (max-width: 600px)`), update the `.card` rule to remove `overflow-x: hidden !important;`:
```css
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

### 2. `frontend/src/pages/MatchingPage.jsx`
- Wrap Boy and Girl `LocationSearch` components with `<div style={{ marginBottom: '15px' }}>`.
- Add `placeholder={t('searchLocation', settings.language) || 'Type city name and select suggestion...'}` to both `LocationSearch` instances.

## Verification Plan
1. **Desktop & Mobile Autocomplete Verification**:
   - Navigate to Matching Page.
   - Type in the Boy location input; verify suggestions dropdown appears as a floating overlay above the rest of the form without triggering any scrollbar on the Boy's card.
   - Select a suggestion; verify selection populates cleanly.
   - Type in the Girl location input; verify the same behavior.
2. **Horoscope Page Regression Check**:
   - Navigate to Horoscope Page; verify `BirthForm` location search continues to function seamlessly.
3. **Build / Lint Check**:
   - Verify frontend builds cleanly with no syntax errors.
