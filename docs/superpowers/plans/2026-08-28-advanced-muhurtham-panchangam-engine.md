# Advanced Vedic & Thirukanitham Auspicious Day (Subha Muhurtham) Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enhance the Panchangam engine to calculate Subha Muhurtham days and time windows with full Vedic accuracy by incorporating Nitya Yoga Mahadoshas (Vyatipata/Vaidhriti), Solar Sankranti Day exclusions, Guru/Sukra Moudhya (combustion) detection, and Thithi Soonya validation, verified across a 120-day continuous test for Chennai coordinates.

**Architecture:** Extend `DailyPanchangamDTO` with new astrological status flags, implement precise ephemeris-based combustion and transition calculations in `DailyPanchangamServiceImpl`, add localization strings, update UI rendering in `PanchangamPage.jsx`, and validate with a 120-day continuous JUnit test suite.

**Tech Stack:** Java 17, Spring Boot, Swiss Ephemeris (`swisseph`), React (JSX), JUnit 5

## Global Constraints
- Strictly isolate backend changes to `DailyPanchangamDTO.java`, `DailyPanchangamServiceImpl.java`, and message properties.
- Strictly isolate frontend changes to `translations.js` and `PanchangamPage.jsx`.
- Do not modify any other service, controller, or algorithm.
- Ensure all 120 days pass in the Chennai test suite with zero nulls, exceptions, or calculation errors.

---

### Task 1: Extend `DailyPanchangamDTO`

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java`

**Interfaces:**
- Produces: New fields in `DailyPanchangamDTO` record: `sankrantiDay`, `guruMoudhya`, `sukraMoudhya`, `thithiSoonya`, `muhurthamWindow`

- [ ] **Step 1: Update `DailyPanchangamDTO` record definition**

Add the new fields while preserving existing records:
```java
package org.vedic.astro.dto;

import java.util.List;

public record DailyPanchangamDTO(
    String date,
    String sunrise,
    String sunset,
    String moonrise,
    String moonset,
    PanchangamElementDTO thithi,
    PanchangamElementDTO nakshatra,
    PanchangamElementDTO yogam,
    PanchangamElementDTO karanam,
    String rashi,
    List<TimeSlotDTO> nallaNeram,
    List<TimeSlotDTO> gowriNallaNeram,
    List<TimeSlotDTO> nakshatraYogams,
    List<TimeSlotDTO> raghuKalam,
    List<TimeSlotDTO> emagandam,
    List<TimeSlotDTO> kulikai,
    List<HoraTimeSlotDTO> horais,
    TimeSlotDTO abhijitMuhurtham,
    List<String> chandrastamamNakshatras,
    int netram,
    double jeevan,
    boolean muhurthamDay,
    boolean vasthuDay,
    boolean vasthuAuspicious,
    boolean agniNakshathiram,
    boolean isTheiPirai,
    TimeSlotDTO vasthuNeram,
    TimeSlotDTO vasthuPujaNeram,
    boolean sankrantiDay,
    boolean guruMoudhya,
    boolean sukraMoudhya,
    boolean thithiSoonya,
    String muhurthamWindow
) {
    public record PanchangamElementDTO(
        int number,
        String name,
        String localizedName,
        String endTime,
        String nextName,
        String nextLocalizedName,
        String nextEndTime
    ) {}

    public record TimeSlotDTO(
        String start,
        String end,
        String label,
        boolean startNextDay,
        boolean endNextDay
    ) {
        public TimeSlotDTO(String start, String end, String label) {
            this(start, end, label, false, false);
        }
    }

    public record HoraTimeSlotDTO(
        int hour,
        String start,
        String end,
        String planet,
        String localizedPlanet
    ) {}
}
```

- [ ] **Step 2: Commit `DailyPanchangamDTO.java`**

```bash
git add src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java
git commit -m "feat(panchangam): extend DailyPanchangamDTO with sankranti, moudhya, and muhurthamWindow"
```

---

### Task 2: Implement Calculation Enhancements in `DailyPanchangamServiceImpl.java`

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java`

**Interfaces:**
- Consumes: SwissEph calculations for Sun, Moon, Jupiter, Venus
- Produces: Enhanced `isMuhurthamDay`, `sankrantiDay`, `guruMoudhya`, `sukraMoudhya`, `thithiSoonya`, `muhurthamWindow`

- [ ] **Step 1: Add planetary combustion and Sankranti helpers in `DailyPanchangamServiceImpl`**

1. **Sankranti Ingress Detection**:
   Compare Sun longitude at `jdSunrise` vs `jdNextSunrise`. If `(int)(sunLongSunrise / 30.0) != (int)(sunLongNextSunrise / 30.0)`, `isSankrantiDay = true`.
2. **Guru & Sukra Moudhya**:
   Calculate Jupiter and Venus positions at `jdSunrise`.
   - `guruMoudhya = Math.abs(angularDiff(sunLong, jupiterLong)) < 11.0`
   - `sukraMoudhya = Math.abs(angularDiff(sunLong, venusLong)) < 8.0`
3. **Thithi Soonya Mapping**:
   Check if Moon's current Rashi (`rashiNum`) falls in the Dagda Rashis for `thithiIdx`.
4. **Nitya Yoga Mahadosha**:
   `isNityaYogaAuspicious = (yogamIdx != 17 && yogamIdx != 27)` (excludes Vyatipata and Vaidhriti).
5. **Subha Muhurtham Rule**:
   ```java
   boolean isMuhurthamDay = (date.getDayOfWeek() != DayOfWeek.TUESDAY && date.getDayOfWeek() != DayOfWeek.SATURDAY)
           && isAuspiciousThithi
           && isAuspiciousNakshatra
           && isAuspiciousKaranam
           && isAuspiciousMonth
           && !isSankrantiDay
           && !guruMoudhya
           && !sukraMoudhya
           && !isThithiSoonya
           && isNityaYogaAuspicious
           && (yogamTypeAtSunrise == 0 || yogamTypeAtSunrise == 1)
           && (netram > 0 && jeevan > 0);
   ```
6. **Muhurtham Window Calculation**:
   If `isMuhurthamDay`, derive the timing window based on Thithi/Nakshatra/Yogam end times relative to sunrise/sunset, or output `"Throughout the day"`.

- [ ] **Step 2: Commit `DailyPanchangamServiceImpl.java`**

```bash
git add src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java
git commit -m "feat(panchangam): implement advanced Vedic Subha Muhurtham, Sankranti, and Moudhya rules"
```

---

### Task 3: Add Localization Labels

**Files:**
- Modify: `src/main/resources/i18n/messages_*.properties`
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Add translation keys for new badges and windows**
Keys:
- `sankrantiDay`: "Sankranti Day", "சங்கராந்தி / மாதப்பிறப்பு", "संक्रांति दिन", etc.
- `guruMoudhya`: "Guru Moudhya (Jupiter Combust)", "குரு அஸ்தமனம்", "गुरु मौढ्य", etc.
- `sukraMoudhya`: "Sukra Moudhya (Venus Combust)", "சுக்கிர அஸ்தமனம்", "शुक्र मौढ्य", etc.
- `thithiSoonya`: "Thithi Soonya", "திதி சூன்யம்", "तिथि शून्य", etc.
- `muhurthamWindow`: "Muhurtham Window", "முகூர்த்த நேரம்", "मुहूर्त समय", etc.

- [ ] **Step 2: Commit translations**

```bash
git add src/main/resources/i18n/ frontend/src/i18n/translations.js
git commit -m "feat(i18n): add translations for Sankranti, Moudhya, and Muhurtham window"
```

---

### Task 4: Update `PanchangamPage.jsx` UI

**Files:**
- Modify: `frontend/src/pages/PanchangamPage.jsx`

- [ ] **Step 1: Render badges and timing window**
- In Card 1 (Sunrise/Sunset & Muhurtham Summary), display:
  - Subha Muhurtham status and active `data.muhurthamWindow` (if available).
  - Sankranti badge (`☀️ ${t('sankrantiDay', settings.language)}`).
  - Guru Moudhya badge (`⚠️ ${t('guruMoudhya', settings.language)}`).
  - Sukra Moudhya badge (`⚠️ ${t('sukraMoudhya', settings.language)}`).
  - Thithi Soonya badge (`🌑 ${t('thithiSoonya', settings.language)}`).

- [ ] **Step 2: Commit `PanchangamPage.jsx`**

```bash
git add frontend/src/pages/PanchangamPage.jsx
git commit -m "feat(ui): display advanced muhurtham window and astrological dosha badges"
```

---

### Task 5: Create 120-Day Chennai Continuous Panchangam Validation Test

**Files:**
- Create: `src/test/java/org/vedic/astro/Panchangam120DaysChennaiValidationTest.java`

- [ ] **Step 1: Write test suite iterating 120 consecutive days for Chennai**
- Latitude: 13.0827, Longitude: 80.2707
- Validate all 120 days:
  - Sunrise/sunset present and in chronological order.
  - Thithi, Nakshatra, Yogam, Karanam numbers valid (`1..30`, `1..27`, `1..27`, `1..60`).
  - Sankranti days accurately detected around mid-month ingress dates.
  - Moudhya flags accurately detected.
  - Zero null pointer exceptions across 120 days.

- [ ] **Step 2: Run test with Maven**

```bash
./mvnw test -Dtest=Panchangam120DaysChennaiValidationTest
```
Expected: Tests pass with 100% success.

- [ ] **Step 3: Commit test suite**

```bash
git add src/test/java/org/vedic/astro/Panchangam120DaysChennaiValidationTest.java
git commit -m "test(panchangam): add 120-day continuous Chennai panchangam validation test"
```

---

### Task 6: Build Verification & Frontend Validation

**Files:**
- Test: `mvn clean test` and `npm run build`

- [ ] **Step 1: Run complete Maven test suite**
- [ ] **Step 2: Run frontend build**
