# Design Spec: Personal Elements, Deities & Life Anchors Engine

**Date:** 2026-08-14  
**Author:** Pair Programming Agent  
**Status:** Approved for Implementation Planning  

---

## 1. Overview & Goals

Transform the existing "Health & Longevity" sub-tab in the Horoscope Dashboard into a comprehensive **Life Anchors & Longevity (வாழ்க்கை நங்கூரங்கள் & ஆயுள்)** system.

This integrates:
1. **Mathematical Numerology & Lucky Numbers** (Driver, Conductor, Planet Numbers, Friendship Matrix & Conflict Bridge).
2. **Vedic Lucky Weekday (Vara)** (Calculated strictly from Local Sunrise at birth).
3. **Monthly Lucky Dates Matrix & Transit Override** (With live Chandrashtama transit filtering).
4. **Auspicious Directions (Vastu & Travel)** (Zodiac Element Directions, Planetary Digbala, Permanent Vastu vs. Short-Term Travel).
5. **Deities & Spiritual Anchors (Deiva Pulligal)** (Ishta Devata from 12th of Karakamsa in D9, Kula Devata 5th House blessing/blockage status, Dharma Devata from 9th of Karakamsa in D9).
6. **Vedic Gemology (Lucky Ratnam) Engine** (Trikona Lord rule, Dusthana exclusion, Combustion/Debilitation filter, Incompatible companion gem anti-patterns, Dual ownership exceptions, Metals, Fingers, Activation timings).
7. **Structural Astrological Anchors** (Physical Vitality via Lagna & Paka Lagna, Social Status via Arudha Lagna (AL), Mind Anchor via Janma Rashi Dispositor, Karma/Prosperity Anchor via Sarvashtakavarga).
8. **Classical Ayurvedic Health & Ayurdaya Longevity** (Parashara-Jaimini 3-Pair Modality, Kakshya Vriddhi, Prakriti percentages, Organ vulnerabilities, Diet & Lifestyle directives).
9. **Clean Sub-Tab UI Navigation** (Text-only sub-tab labels without leading emoji icons).

---

## 2. Architecture & Data Structures

### 2.1 Backend Data Model (DTOs & Records)

#### `LifeAnchorsProfile.java` (New DTO in `org.vedic.astro.dto`)
```java
package org.vedic.astro.dto;

import java.util.List;
import java.util.Map;

public record LifeAnchorsProfile(
        NumerologyDetails numerology,
        LuckyDayDetails luckyDay,
        LuckyDatesDetails luckyDates,
        AuspiciousDirectionsDetails directions,
        SpiritualDeitiesDetails deities,
        GemologyRecommendation gemology,
        StructuralAnchorsDetails structuralAnchors
) {
    public record NumerologyDetails(
            int radicalDriverNumber,
            String radicalRulingPlanet,
            int destinyConductorNumber,
            String destinyRulingPlanet,
            int astrologicalPlanetNumber,
            String astrologicalPlanetName,
            List<Integer> friendlyNumbers,
            List<Integer> neutralNumbers,
            List<Integer> enemyNumbers,
            String conflictResolutionNotes
    ) {}

    public record LuckyDayDetails(
            String vedicWeekdayName,
            String rulingPlanet,
            String luckySignifications
    ) {}

    public record LuckyDatesDetails(
            List<Integer> primaryLuckyDates,
            List<Integer> secondaryFriendlyDates,
            List<Integer> datesToAvoid,
            List<Integer> currentMonthChandrashtamaDates,
            String transitCautionNotes
    ) {}

    public record AuspiciousDirectionsDetails(
            String permanentVastuDirection,
            String travelDirection,
            String lagnaCompassZone,
            String moonCompassZone
    ) {}

    public record SpiritualDeitiesDetails(
            String atmakarakaPlanet,
            String karakamsaSignD9,
            String ishtaDevata,
            String ishtaDevataTamil,
            String ishtaDevataRationale,
            String kulaDevataBlessingStatus, // BLESSED or BLOCKED_ANCESTRAL_DOSHA
            String kulaDevataRemedy,
            String dharmaDevata,
            String dharmaDevataTamil
    ) {}

    public record GemologyRecommendation(
            String primaryGemstone,
            String primaryGemstoneTamil,
            String secondarySubstitute,
            String rulingPlanet,
            String recommendedMetal,
            String recommendedFinger,
            String activationDayAndTiming,
            List<String> forbiddenCompanionGems,
            String astrologicalRationale
    ) {}

    public record StructuralAnchorsDetails(
            String physicalVitalityAnchor, // Lagna & Paka Lagna evaluation
            String arudhaLagna,            // AL sign and description
            String mindAnchorResilience,   // Moon dispositor status
            String karmaAnchorHouse        // SAV highest bindu house
    ) {}
}
```

---

## 3. Core Calculation Algorithms

### 3.1 Numerology Engine (`NumerologyUtils.java`)
- **Digital Root $R(N)$**: `1 + ((N - 1) % 9)`
- **Driver**: $R(\text{Birth Day})$
- **Conductor**: $R(\text{Birth Day} + \text{Birth Month} + \text{Birth Year})$
- **Astrological Planet Number**:
  - Sun: 1, Moon: 2, Jupiter: 3, Rahu: 4, Mercury: 5, Venus: 6, Ketu: 7, Saturn: 8, Mars: 9.
  - Derived from Lagna Lord.
- **Conflict Bridge**: If Driver & Conductor are in mutual enemy list, recommend neutral bridge numbers (e.g. 5 or 6 for 1 & 8).

### 3.2 Lucky Day (Vara) Engine
- Calculated from **Local Sunrise** using Julian Day: `((int) Math.floor(jd + 0.5) % 7) + 1` (1 = Sunday, 2 = Monday, ..., 7 = Saturday).

### 3.3 Monthly Lucky Dates & Chandrashtama Override
- Generates standard monthly lucky date matrix.
- Evaluates native's 8th house from Moon (`((moonSign + 7 - 1) % 12) + 1`).
- Identifies any lucky date in the active month where transit Moon is in this 8th house and flags `UNFAVORABLE_ASTROLOGICAL_TRANSIT`.

### 3.4 Auspicious Directions Engine
- **Zodiac Element Direction**:
  - Fire (1, 5, 9) $\rightarrow$ East
  - Earth (2, 6, 10) $\rightarrow$ South
  - Air (3, 7, 11) $\rightarrow$ West
  - Water (4, 8, 12) $\rightarrow$ North
- **Digbala Compass**:
  - Sun/Mars (10th/South), Venus/Moon (4th/North), Saturn (7th/West), Jupiter/Mercury (1st/East).
- **Permanent Vastu**: Lagna Sign Element + Lagna Lord Digbala direction.
- **Short-Term Travel**: Moon Sign Element + active Dasa Lord direction.

### 3.5 Spiritual Deities (Deiva Pulligal) Engine (`SpiritualDeityUtils.java`)
1. **Atmakaraka (AK)**: Highest degree planet in D1 chart among the 7 classical planets (Sun, Moon, Mars, Mercury, Jupiter, Venus, Saturn).
2. **Karakamsa**: Sign occupied by AK in D9 Navamsa chart.
3. **Ishta Devata**: 12th house from Karakamsa in D9. Planet occupying or ruling that 12th sign maps to:
   - Sun $\rightarrow$ Lord Shiva / Lord Rama (சிவன் / ராமர்)
   - Moon $\rightarrow$ Goddess Parvati / Gauri / Krishna (பார்வதி / கிருஷ்ணர்)
   - Mars $\rightarrow$ Lord Murugan / Narasimha (முருகன் / நரசிம்மர்)
   - Mercury $\rightarrow$ Lord Vishnu / Venkateshwara (விஷ்ணு / வெங்கடாஜலபதி)
   - Jupiter $\rightarrow$ Lord Dakshinamurthy / Hayagriva (தக்ஷிணாமூர்த்தி)
   - Venus $\rightarrow$ Goddess Mahalakshmi / Annapoorneshwari (மகாலட்சுமி)
   - Saturn $\rightarrow$ Lord Hanuman / Shani Deva / Karuppanasamy (ஹனுமான்)
   - Rahu $\rightarrow$ Goddess Durga / Varahi (துர்க்கை / வாராஹி)
   - Ketu $\rightarrow$ Lord Ganesha (விநாயகர்)
4. **Kula Devata Status**: 5th House and 5th Lord in D1. If afflicted by Rahu/Ketu/Saturn without benefic aspect $\rightarrow$ `BLOCKED_ANCESTRAL_DOSHA` with Kula Devata Preethi remedy; otherwise `BLESSED`.
5. **Dharma Devata**: 9th house from Karakamsa in D9.

### 3.6 Vedic Gemology Engine (`GemologyEngineUtils.java`)
- **4 Golden Rules**:
  1. Only recommend lords of **1st, 5th, or 9th** (Trikona).
  2. Reject planets owning or placed in **6th, 8th, 12th** (Dusthana).
  3. Reject combust or debilitated planets (unless Neechabhanga confirmed).
  4. Dual ownership exception: Aries (Mars 1 & 8), Scorpio (Mars 1 & 6), Taurus (Venus 1 & 6), Libra (Venus 1 & 8) can wear Lagna Lord gemstone if placed in Kendra/Trikona.
- **Forbidden Companion Gems Grid**: Strict mutual incompatibility verification.
- Output includes primary gem, Uparatna, metal, finger, activation day & timing.

### 3.7 Structural Astrological Anchors (`StructuralAnchorsUtils.java`)
- **Paka Lagna**: Sign occupied by Lagna Lord in D1.
- **Arudha Lagna (AL)**: Distance from Lagna to Lagna Lord, projected forward from Lagna Lord (with 1/7 and 4/10 forward jumps).
- **Mind Resilience Anchor**: Janma Rashi Lord (Mati Karaka) placement in Kendra/Trikona vs. Dusthana.
- **Karma Anchor**: Sarvashtakavarga (SAV) house with highest bindus ($\ge 30$).

---

## 4. UI Design & Layout

### 4.1 Sub-Tab Navigation Header
In `HoroscopePage.jsx`:
Remove leading icons from all horoscope sub-tab buttons:
- `ராசி / நவாம்ச கட்டங்கள்` (Charts)
- `திசா புத்தி` (Dasa Timeline)
- `ஷட்பலம்` (Shadbala)
- `தோஷங்கள் & யோகங்கள்` (Diagnostics)
- `வாழ்க்கை நங்கூரங்கள் & ஆயுள்` (Life Anchors & Longevity)
- `AI வாழ்க்கை பலன்கள்` (AI Life Balan)

### 4.2 Tab Layout (`LifeAnchorsLongevityView.jsx`)
Organized into 5 clean visual sections:
1. **🕉️ தெய்வங்கள் & ஆன்மீக நங்கூரங்கள் (Spiritual & Deity Anchors)**: Ishta Devata, Kula Devata Status, Dharma Devata.
2. **💎 அதிர்ஷ்ட ரத்தினம் & விதிகள் (Vedic Gemology Engine)**: Recommended Ratnam, Uparatna, Metal, Finger, Day/Time, Incompatible gems list.
3. **🔢 எண்கணிதம் & அதிர்ஷ்ட கூறுகள் (Numerology & Lucky Elements)**: Driver ($DD$), Conductor ($DD+MM+YYYY$), Planet Number, Friendly/Enemy numbers, Monthly Lucky Dates matrix, Auspicious Directions.
4. **🏛️ கட்டமைப்பு நங்கூரங்கள் (Structural Astrological Anchors)**: Paka Lagna, Arudha Lagna (AL), Mind Resilience, SAV Karma Anchor.
5. **🌿 ஆயுர்வேத பிரகிருதி & ஆயுள்தாய நிர்ணயம் (Ayurvedic Health & Longevity)**: Classical Ayurdaya 3-Pair Modality, Kakshya Vriddhi, Prakriti sliders, Organ vulnerabilities, Diet & Lifestyle directives.

---

## 5. Multi-Language & PDF Integration

1. **Translations (`translations.js`)**:
   - `lifeAnchorsTab`: Localized across all 6 languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`).
   - Group headers, deity descriptions, numerology labels, and gemology rules localized across languages.
2. **PDF Integration (`PdfExportService.java`)**:
   - Add a dedicated page/section in the PDF for **Spiritual Anchors, Vedic Gemology & Life Anchors**, using the existing language font engines.

---

## 6. Verification & Test Plan

1. **Automated Unit Tests**:
   - `NumerologyUtilsTest`: Validate digital root, driver/conductor reduction, friendship matrix, conflict bridge.
   - `SpiritualDeityUtilsTest`: Validate Atmakaraka identification, Karakamsa D9 placement, Ishta Devata from 12th, Dharma Devata from 9th, Kula Devata affliction detection.
   - `GemologyEngineUtilsTest`: Validate Trikona rule, Dusthana exclusion, combustion filter, forbidden companion pairs, dual ownership exception.
   - `StructuralAnchorsUtilsTest`: Validate Arudha Lagna (AL) with exceptions, Paka Lagna, SAV Karma anchor.
   - `PdfExportServiceTest`: Verify PDF compilation with Life Anchors profile.
2. **Frontend Verification**:
   - `npm run build` with zero errors.
   - Verify tab navigation without emoji icons.
   - Verify multi-language switching (`ta`, `en`, `hi`, `te`, `kn`, `ml`).
