# Astrology App Enhancements & Fixes Design Spec

**Date**: 2026-08-18  
**Topic**: Mobile AI Balan Fixes, Multilingual Horoscope PDF (Life Anchors & Ayurdaya), Text-to-Speech (TTS) Integration, Saffron-White Card Theming, and Marriage Matching System & Nullification Engine Fixes.

---

## 1. Background & Objectives

This specification covers 5 interrelated UX, calculation, export, and accessibility enhancements:
1. **AI Balan Mobile Responsiveness**: Eliminate horizontal scrolling, text clipping, and off-screen wrapping of yearly prediction cards and longevity sections on mobile screens.
2. **Multilingual PDF Export for Horoscope (Life Anchors & Ayurdaya)**: Replace partial Tamil checks and hardcoded English strings in `PdfExportService.java` with complete 6-language translations (`ta`, `hi`, `te`, `kn`, `ml`, `en`) backed by `messages_*.properties` and localized astrological dictionaries.
3. **Text-to-Speech (TTS) for AI Balan & Daily Balan**: Implement a unified top audio player with Play / Pause / Resume / Stop controls using browser-native Web Speech API configured with locale-appropriate voices (`ta-IN`, `hi-IN`, `te-IN`, `kn-IN`, `ml-IN`, `en-IN`).
4. **Saffron & White Card Theming**: Align `LifeAnchorsLongevityView.jsx` and `HealthLongevityView.jsx` with the app's established design tokens (`var(--accent-saffron)`, `var(--accent-warm)`, `var(--accent-gold)`, `var(--bg-card)`, `var(--border)`).
5. **Marriage Matching & AI Matching System Selection & Nullifications**:
   - Marriage matching (Classical & AI) will dynamically follow the user's selected dropdown method: **36 Ashta Koota** (`ASHTA_KOOTA`) or **10 Dhasa Porutham** (`DASA_PORUTHAM`).
   - Fix all classical edge cases and nullification rationale (Rajju, Nadi, Gana, Rasi/Bhakut, Kuja Dosha, Dasa Sandhi) with rich justifications and write-ups generated 100% in the selected language.

---

## 2. Detailed Technical Design by Area

### 2.1 AI Balan Mobile Responsiveness (`AiPredictionsView.jsx`, `index.css`)
- **Problem**: Yearly prediction grid uses `gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))'`. On mobile screens (<360px width), this forces cards to exceed viewport width, causing horizontal scroll and text overflow.
- **Solution**:
  - Replace grid columns with `gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 300px), 1fr))'`.
  - Add explicit responsive rules in `index.css` and container styles:
    - Ensure `.card` and prediction items have `max-width: 100%`, `box-sizing: border-box`, and `overflow-wrap: break-word`.
    - Stack flex headers on narrow viewports (`flex-direction: column`, `align-items: flex-start` below 600px).
    - Ensure token usage / metadata bar wraps cleanly without truncation.

### 2.2 Multilingual PDF Export for Horoscope (`PdfExportService.java`, `i18n/messages_*.properties`)
- **Problem**: Life Anchors and Ayurdaya sections in `PdfExportService.java` currently check only `"ta"` for a few fields (e.g. `ishtaDevataTamil()`) and fallback to hardcoded English strings (`"Driver: "`, `"Primary Vastu: "`, `ayu.longevityClassification()`, `ayu.classicalRationale()`).
- **Solution**:
  - Add complete translation keys in `messages.properties` and all language files (`messages_ta.properties`, `messages_hi.properties`, `messages_te.properties`, `messages_kn.properties`, `messages_ml.properties`, `messages_en.properties`):
    - Life Anchors: Deities, gemstones, metals, fingers, numerology driver/conductor, lucky days/dates, vastu directions, arudha lagna.
    - Ayurdaya: Longevity classification (`Poornayu`, `Madhyayu`, `Alpayu`), lifespan ranges, maraka timeline explanations, and classical consensus rationale in native scripts.
  - Refactor `PdfExportService.java` to use `ts.getLabel(...)` with formatted placeholders and Indic shaping for all 6 languages.

### 2.3 Text-to-Speech (TTS) Integration (`AiPredictionsView.jsx`, `DailyBalanView.jsx`, `useTextToSpeech.js`)
- **Design**:
  - Create a reusable React hook / utility `useTextToSpeech` that manages Web Speech API (`window.speechSynthesis`).
  - Capabilities:
    - Language mapping: `ta` -> `ta-IN`, `hi` -> `hi-IN`, `te` -> `te-IN`, `kn` -> `kn-IN`, `ml` -> `ml-IN`, `en` -> `en-IN` (fallback to `en-US`).
    - Speech preparation: Filters out emojis, symbols, and formatting artifacts for clean, intelligible reading.
    - Playback states: `IDLE`, `PLAYING`, `PAUSED`.
    - Top audio bar UI: Saffron-styled floating/header toolbar with:
      - 🔊 **Play All / Read Out** button
      - ⏸ **Pause** button
      - ▶ **Resume** button
      - ⏹ **Stop** button
      - Animated audio wave/pulse indicator when actively speaking.

### 2.4 Saffron & White Design Consistency (`LifeAnchorsLongevityView.jsx`, `HealthLongevityView.jsx`)
- **Design**:
  - Replace any dark backgrounds (`#1e1e1e`, `#2a2a2a`) or off-palette borders with `var(--bg-card)` (#ffffff), `var(--bg-primary)` (#fffaf4), and `var(--border)` (#f0e2d0).
  - Use gold/saffron gradient accents on card headers and section banners.
  - Standardize badge styling:
    - Auspicious / Poornayu: `rgba(46, 125, 50, 0.08)` bg, `var(--success)` text & border.
    - Moderate / Madhyayu: `rgba(212, 136, 6, 0.08)` bg, `var(--accent-gold)` text & border.
    - Attention / Alpayu / Dosha: `rgba(232, 93, 4, 0.08)` bg, `var(--accent-warm)` text & border.

### 2.5 Marriage Matching & AI Matching System Selection & Classical Nullifications
- **System Selection**:
  - The calculation and AI prediction will strictly follow the dropdown selection in `MatchingPage.jsx`:
    - `ASHTA_KOOTA` (36 Points: Varna, Vashya, Tara, Yoni, Graha Maitri, Gana, Bhakut, Nadi).
    - `DASA_PORUTHAM` (10 Points: Dinam, Ganam, Mahendram, Stree Deergham, Yoni, Rasi, Rasi Adhipathi/Vasya, Rajju, Vedha, Nadi).
- **Classical Nullification & Edge Case Accuracy**:
  - **Rajju**: Nullification/mitigation when Nakshatras belong to different Padas or when Rasi lords are identical/friendly.
  - **Nadi Dosha**: Nullification when Rasi is the same but Nakshatras differ, when Nakshatras are the same but Rasis differ (e.g. Krittika 1 vs 2), or when Nakshatra lords are natural friends.
  - **Gana Dosha**: Nullification when Rasi lords are mutual friends or identical, or Tara porutham is exceptionally strong.
  - **Bhakut / Rasi Dosha**: Cancellation for 6-8 (Shashtashtaka) or 9-5 (Navapanchama) or 12-2 (Dwidwadasa) when Rasi lords are the same (Aries-Scorpio, Taurus-Libra, etc.) or mutual friends (Pisces-Sagittarius, Cancer-Leo).
  - **Kuja Dosha (Manglik / Sevvai Dosham)**: Comprehensive evaluation of 2nd, 4th, 7th, 8th, 12th houses from Lagna, Moon, and Venus; full cancellation rules (Mars in Aries/Scorpio/Capricorn/Cancer/Leo, Jupiter/Moon aspect, or both charts having balanced dosham).
- **AI Matching Integration (`GeminiPredictionService.java`)**:
  - Pass the selected matching method and complete classical koota breakdown + nullifications to Gemini.
  - Tailor prompts and schemas for `ASHTA_KOOTA` vs `DASA_PORUTHAM`.
  - Ensure 100% native language generation in the user's selected language (`ta`, `hi`, `te`, `kn`, `ml`, `en`).
  - Update Marriage Matching PDF export to reflect the selected system and localized labels.

---

## 3. Verification Plan

### 3.1 Automated & Unit Tests
- Run backend unit tests (`mvn test`) covering:
  - `DasaPorutthamEngineTest` & `AshtaKootaEngineTest` with nullification scenarios.
  - `PdfExportServiceTest` verifying PDF generation across all 6 locales without missing fonts or null pointers.
  - `GeminiPredictionServiceTest` validating matching prompt construction for both systems.

### 3.2 Manual UI Verification
- Test mobile viewport (320px, 375px, 414px) using Chrome DevTools device mode on AI Balan prediction tab.
- Test Text-to-Speech Play / Pause / Resume / Stop in Tamil, Hindi, Telugu, Kannada, Malayalam, and English.
- Export Horoscope PDF in all 6 languages and verify Life Anchors and Ayurdaya tables render in native Indic fonts with no English fallbacks.
- Test Marriage Matching in both Ashta Koota and Dhasa Porutham modes; generate AI matching and verify scores, justifications, nullifications, and remedies in the chosen language.
