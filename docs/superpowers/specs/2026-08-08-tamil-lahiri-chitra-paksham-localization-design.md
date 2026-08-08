# Design Specification: Tamil Localization Fix for Lahiri (Chitra Paksham)

## Overview
Correct the Tamil translation of "Lahiri (Chitra Paksha / Chitra Paksham)" across frontend and backend localization files, replacing the mistranslated term "பக்கம்" (page/side) with the traditional astrological term "பக்ஷம்" (Paksham).

---

## 1. Changes Required
1. **`frontend/src/i18n/translations.js`**:
   - Update `ta.ayanamsaLahiri` from `"லஹிரி (சித்திர பக்கம்)"` to `"லஹிரி (சித்திர பக்ஷம்)"`.
2. **`src/main/resources/i18n/messages_ta.properties`**:
   - Update `ayanamsa.LAHIRI` from `லஹிரி (சித்திர பக்கம்)` to `லஹிரி (சித்திர பக்ஷம்)`.

---

## 2. Verification Plan
1. Run `mvn clean test` to ensure backend properties load with correct UTF-8 / Unicode characters.
2. Run `npm run build` in `frontend/` to ensure clean JavaScript bundle compilation.
