# Design Specification: Comprehensive Astrology UX & Prediction Engine Fixes

## 1. Overview & Objectives

This design addresses all reported issues across the Astro Engine application:
1. **Dasa-Bhukthi UI**: Highlight currently active Mahadasa and Bhukthi, auto-expand the active Mahadasa card on load, and apply rich visual styling.
2. **Shadbala UI**: Enhance planetary strength presentation with relative strength indicators and color-coded status badges.
3. **Diagnostics (Yogas & Doshams)**: Correct dosha detection states so non-present doshas are never displayed as active, providing a clear 3-state badge hierarchy (🟢 Not Present, 🛡️ Cancelled/Nullified, ⚠️ Active).
4. **AI Life Balan Lifespan Horizon & Depth**: Extend the forecast horizon from current age through the full lifespan (age 85–90+ / 100) and demand rich multidimensional narratives covering career, wealth, health, marriage, kids, and parents.
5. **Print PDF Year-Wise Data**: Update PDF table generation to render full detailed predictions, astrological factors, and cautions/remedies.
6. **Horoscope Header Formatting**: Fix Ayanamsa translation lookup (e.g. `அயனாம்சம்: லஹிரி (சித்திர பக்ஷம்)`) and set Tamil Panchangam label cleanly as `"திருக்கணிதம்"`.
7. **Native Multilingual Headings**: Ensure 100% of all AI Balan card headers and sub-sections render in the selected language (`ta`, `hi`, `kn`, `te`, `ml`, `en`) without raw English key fallbacks.

---

## 2. Detailed Technical Design

### A. Frontend Localization (`frontend/src/i18n/translations.js`)
Synchronize and define all required keys across all 6 languages:
- `keyStrengths`:
  - `en`: "Key Strengths", `ta`: "முக்கிய பலங்கள்", `hi`: "प्रमुख शक्तियां", `kn`: "ಪ್ರಮುಖ ಸಾಮರ್ಥ್ಯಗಳು", `te`: "ప్రధాన బలాలు", `ml`: "പ്രധാന കഴിവുകൾ"
- `karmicLessons`:
  - `en`: "Karmic Lessons & Vulnerabilities", `ta`: "கர்ம வினைகள் & எச்சரிக்கைகள்", `hi`: "कार्मिक सबक व कमजोरियां", `kn`: "ಕಾರ್ಮಿಕ್ ಪಾಠಗಳು & ಎಚ್ಚರಿಕೆಗಳು", `te`: "కార్మిక్ పాఠాలు & జాగ్రత్తలు", `ml`: "കർമ്മ പാഠങ്ങളും മുൻകരുതലുകളും"
- `healthAnalysisTitle`:
  - `en`: "Ayurvedic Health & Vitality Analysis", `ta`: "ஆயுர்வேத உடல்நலம் & ஆயுள் பகுப்பாய்வு", `hi`: "आयुर्वेदिक स्वास्थ्य व आयु विश्लेषण", `kn`: "ಆಯುರ್ವೇದ ಆರೋಗ್ಯ & ಆಯುಷ್ಯ ವಿಶ್ಲೇಷಣೆ", `te`: "ఆయుర్వేద ఆరోగ్య & ఆయుష్షు విశ్లేషణ", `ml`: "ആയുർവേദ ആരോഗ്യവും ആയുസ്സും വിശകലനം"
- `ayurvedicConstitution`:
  - `en`: "Ayurvedic Constitution (Prakriti)", `ta`: "ஆயுர்வேத உடல் தத்துவம் (பிரகிருதி)", `hi`: "आयुर्वेदिक शारीरिक प्रकृति", `kn`: "ಆಯುರ್ವೇದ ಶಾರೀರಿಕ ಪ್ರಕೃತಿ", `te`: "ఆయుర్వేద శారీరక ప్రవృత్తి", `ml`: "ആയുർവേദ ശാരീരിക പ്രകൃതി"
- `longevityVitality`:
  - `en`: "Longevity & Immunity Vitality", `ta`: "ஆயுள் & நோய் எதிர்ப்பு பலம்", `hi`: "आयु व रोग प्रतिरोधक क्षमता", `kn`: "ಆಯುಷ್ಯ & ರೋಗನಿರೋಧಕ ಶಕ್ತಿ", `te`: "ఆయుష్షు & రోగనిరోಧక శక్తి", `ml`: "ആയുസ്സും രോഗപ്രതിരോധ ശേഷിയും"
- `organVulnerabilities`:
  - `en`: "Sensitive Organs & Vulnerabilities", `ta`: "கவனிக்க வேண்டிய உடல் உறுப்புகள்", `hi`: "सावधानी योग्य अंग", `kn`: "ಎಚ್ಚರ ವಹಿಸಬೇಕಾದ ಅಂಗಗಳು", `te`: "జాగ్రత్త వహించాల్సిన అవయవాలు", `ml`: "ശ്രദ്ധിക്കേണ്ട അവയവങ്ങൾ"
- `recommendedDietLifestyle`:
  - `en`: "Recommended Diet & Lifestyle", `ta`: "பரிந்துரைக்கப்படும் உணவு & வாழ்வியல்", `hi`: "अनुशंसित आहार व जीवनशैली", `kn`: "ಶಿಫಾರಸು ಮಾಡಿದ ಆಹಾರ & ಜೀವನಶೈಲಿ", `te`: "సిఫార్సు చేయబడిన ఆహారం & జీవనశైలి", `ml`: "നിർദ്ദേശിച്ച ആഹാരവും ജീവിതരീതിയും"
- `classicalYogasTitle`:
  - `en`: "Classical Astrological Yogas", `ta`: "ஜாதகத்தில் அமைந்த ராஜ யோகங்கள்", `hi`: "कुंडली में स्थित शुभ राजयोग", `kn`: "ಜಾತಕದಲ್ಲಿರುವ ರಾಜಯೋಗಗಳು", `te`: "జాతకంలో ఉన్న రాజయోగాలు", `ml`: "ജാതകത്തിലെ രാജയോഗങ്ങൾ"
- `doshamsAnalysisTitle`:
  - `en`: "Dosha Evaluation & Nullifications", `ta`: "தோஷங்கள் & தோஷ நிவர்த்தி பகுப்பாய்வு", `hi`: "दोष एवं दोष निवारण विश्लेषण", `kn`: "ದೋಷಗಳು & ದೋಷ ನಿವಾರಣೆ ವಿಶ್ಲೇಷಣೆ", `te`: "దోషాలు & దోష నివారణ విశ్లేషణ", `ml`: "ദോഷങ്ങളും പരിഹാരങ്ങളും"
- `pastTurningPointsTitle`:
  - `en`: "Key Past Turning Points (Birth to Present)", `ta`: "பிறப்பு முதல் இன்று வரை நடந்த முக்கிய திருப்புமுனைகள்", `hi`: "जन्म से अब तक के प्रमुख जीवन पड़ाव", `kn`: "ಹುಟ್ಟಿನಿಂದ ಇಂದಿನವರೆಗಿನ ಪ್ರಮುಖ ಜೀವನ ಹಂತಗಳು", `te`: "పుట్టినప్పటి నుండి ఇప్పటి వరకు ముఖ్యమైన దశలు", `ml`: "ജനനം മുതൽ ഇന്നുവരെയുള്ള പ്രധാന ഘട്ടങ്ങൾ"
- `lifetimeForecastTitle`:
  - `en`: "Year-by-Year Lifetime Predictions (Current Age to 100+)", `ta`: "வருடாந்திர வாழ்நாள் பலன்கள் (தற்போதைய வயது முதல் 100+ வரை)", `hi`: "वर्षवार संपूर्ण जीवन भविष्यफल (वर्तमान आयु से 100+ तक)", `kn`: "ವರ್ಷವಾರು ಜೀವಮಾನ ಭವಿಷ್ಯ (ಪ್ರಸ್ತುತ ವಯಸ್ಸಿನಿಂದ 100+ ವರೆಗೆ)", `te`: "వార్షిక సంపూర్ణ జీవిత ఫలాలు (ప్రస్తుత వయస్సు నుండి 100+ వరకు)", `ml`: "വർഷം തോറുമുള്ള ജീവിത ഫലങ്ങൾ (നിലവിലെ പ്രായം മുതൽ 100+ വരെ)"
- `noDosham`:
  - `en`: "No Affliction", `ta`: "தோஷம் இல்லை", `hi`: "दोष नहीं", `kn`: "ದೋಷವಿಲ್ಲ", `te`: "దోషం లేదు", `ml`: "ദോഷമില്ല"
- `currentActiveDasa`:
  - `en`: "Current Dasa", `ta`: "தற்போதைய திசா", `hi`: "वर्तमान दशा", `kn`: "ಪ್ರಸ್ತುತ ದಶಾ", `te`: "ప్రస్తుత దశ", `ml`: "നിലവിലെ ദശ"
- `panchangamThirukanitham` in `ta`: `"திருக்கணிதம்"`

---

### B. Dasa-Bhukthi UI Highlight & Auto-Expansion (`HoroscopePage.jsx`)
- Date utility to parse ISO dates and compare against current system date (`LocalDate.now()`).
- Auto-set `expandedDasa` on data load to the index of the active Mahadasa.
- Active Mahadasa container rendered with:
  - Gold border: `2px solid var(--accent-gold)`
  - Glowing background: `rgba(255, 215, 0, 0.07)`
  - Header badge: `✨ ${t('currentActiveDasa', language)}`
- Active Bhukthi row inside the table highlighted with `background: rgba(255, 215, 0, 0.15)` and `⭐` icon.

---

### C. Diagnostics UI 3-Tier Status (`HoroscopePage.jsx`)
Update the Dosha card rendering:
1. `!d.detected`:
   - Green / Neutral Card: `border: 1px solid rgba(39, 174, 96, 0.3)`, `background: rgba(39, 174, 96, 0.03)`
   - Badge: `✓ ${t('noDosham', language)}` in green.
   - Text: `d.description || "No planetary affliction detected for this house."`
2. `d.detected && d.nullified`:
   - Green / Gold Card: `border: 1px solid rgba(39, 174, 96, 0.5)`, `background: rgba(39, 174, 96, 0.06)`
   - Badge: `🛡️ ${t('cancelled', language)}` in green.
   - Text: `d.nullificationReason || d.reason`
3. `d.detected && !d.nullified`:
   - Red Warning Card: `border: 1px solid rgba(231, 76, 60, 0.5)`, `background: rgba(231, 76, 60, 0.06)`
   - Badge: `⚠️ ${t('active', language)}` in red.
   - Text: `d.remedySuggestion || d.remedy`

---

### D. AI Balan Prompt Horizon & Multidimensional Depth (`GeminiPredictionService.java`)
- Update prompt directive #6:
  - `"lifetimePredictions: Complete year-by-year forecasts covering the ENTIRE remaining lifespan from current age up to age 85-90+ (or up to age 100) across all running Vimshottari Mahadasas and Antardasas."`
  - Instruct Gemini:
    - For each year, generate an exhaustive, detailed narrative paragraph explicitly detailing:
      1. Career, business, job disruptions or major promotions.
      2. Wealth, financial inflow, investments, debts, and property.
      3. Physical health, surgeries, disease vulnerabilities, and overall vitality.
      4. Marriage, spouse harmony, children's milestones, and domestic life.
      5. Parents' health, longevity transitions, or family bereavement if indicated by D12/D30 & Dusthana dasas.
      6. Mindset, spiritual evolution, and inner strength.

---

### E. Print PDF Year-Wise Predictions (`PdfExportService.java`)
- Update `PdfExportService.java` to format future predictions table:
  - Column 1: `Year / Age`
  - Column 2: `Dasa-Bhukthi & Astrological Basis`
  - Column 3: `Yearly Theme & In-Depth Forecast` (`detailedPrediction`)
  - Column 4: `Cautions & Vedic Remedies`
- Handle table wrapping and pagination cleanly across all supported Indian language fonts.

---

### F. Frontend Year Cards Quick Domain Tags (`AiPredictionsView.jsx`)
- Render visual domain badge tags at the top of each year card:
  - `💼 Career & Wealth`
  - `🌿 Health & Vitality`
  - `👨‍👩‍👦 Family & Relationships`
  - `⚠️ Cautions & Remedies`
- This allows rapid visual scanning while preserving the rich, cohesive unified narrative paragraph below.

---

## 3. Verification & Validation Plan

### Automated Backend Tests
- Run `mvn clean test` across the full test suite.
- Ensure all 50+ unit and integration tests pass without regression.

### Frontend Production Build
- Run `npm run build` in `frontend/` to ensure flawless compilation and zero bundling errors.

### Manual / Browser Verification
1. Verify Dasa-Bhukthi tab highlights active Mahadasa and Bhukthi.
2. Verify Diagnostics tab renders non-present doshas as green (`✓ தோஷம் இல்லை`), nullified doshas as shield (`🛡️ தோஷ நிவர்த்தி`), and active doshas in red (`⚠️ தோஷம் உள்ளது`).
3. Verify AI Balan headings and cards render 100% localized native text in Tamil, Hindi, Kannada, Telugu, Malayalam, and English.
4. Verify AI Balan generates full lifespan year-by-year cards with domain tags and rich paragraphs.
5. Verify PDF download includes the complete year-by-year lifetime predictions table.
6. Verify Horoscope results header displays `அயனாம்சம்: லஹிரி (சித்திர பக்ஷம்) | பஞ்சாங்கக் கணிதம்: திருக்கணிதம்`.
