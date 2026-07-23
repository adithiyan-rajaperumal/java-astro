# Vasthu Days, Vasthu Neram & Subha Muhurtham Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement exact astronomical calculations for Vasthu Days (using Solar Rashi Sankranti ingress), Vasthu Neram (90-min awake and 36-min Puja time windows), Agni Nakshathiram, and strict Subha Muhurtham Days (validating Nakshatra-Vara Yogam, Vishti Karanam, Dwadashi/Purnima thithis, and Margazhi month exclusion) in `DailyPanchangamServiceImpl.java` and `PanchangamPage.jsx`.

**Architecture:** 
1. Backend (`DailyPanchangamDTO.java`): Add `vasthuNeram`, `vasthuPujaNeram`, `isVasthuAuspicious`, and `isAgniNakshathiram` fields.
2. Backend (`DailyPanchangamServiceImpl.java`): Add Solar Sankranti day solver to derive exact Tamil Solar Days (1-30). Implement canonical Vasthu day lookup and time window calculations. Update `isMuhurthamDay` with Yogam, Vishti Karanam, Thithi, and Solar Month checks.
3. Frontend (`translations.js` & `PanchangamPage.jsx`): Add i18n translation keys for Vasthu and Katheri, and render a rich Vasthu Neram Card and Agni Nakshathiram badge.

**Tech Stack:** Java 17+, Spring Boot, Swiss Ephemeris (`de.thmac.swisseph`), React, Vite.

## Global Constraints

- **Backwards Compatibility**: Retain existing `isVasthuDay` and `isMuhurthamDay` boolean fields.
- **i18n Support**: Add localized keys for `vasthuTitle`, `vasthuAwakeTime`, `vasthuPujaTime`, `bhoomiPujaSuitable`, `bhoomiPujaAvoid`, `agniNakshathiram` across all 6 supported languages (`en`, `ta`, `hi`, `kn`, `te`, `ml`).
- **Precision**: Compute Vasthu Neram time slots using local `ZonedDateTime` based on location timezone.

---

### Task 1: Update Data Model in `DailyPanchangamDTO.java`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\dto\DailyPanchangamDTO.java:1-32`

**Interfaces:**
- Consumes: Java `DailyPanchangamDTO` structure
- Produces: `DailyPanchangamDTO` record containing `isVasthuAuspicious`, `isAgniNakshathiram`, `vasthuNeram`, and `vasthuPujaNeram`.

- [ ] **Step 1: Update `DailyPanchangamDTO` record definition in `DailyPanchangamDTO.java`**

```java
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
    boolean isMuhurthamDay,
    boolean isVasthuDay,
    boolean isVasthuAuspicious,
    boolean isAgniNakshathiram,
    TimeSlotDTO vasthuNeram,
    TimeSlotDTO vasthuPujaNeram
) {}
```

- [ ] **Step 2: Verify Compilation**

Run: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; & "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit Task 1**

```bash
git add src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java
git commit -m "feat: add vasthuNeram, vasthuPujaNeram, isVasthuAuspicious, and isAgniNakshathiram to DailyPanchangamDTO"
```

---

### Task 2: Implement Vasthu Days & Vasthu Neram Engine in `DailyPanchangamServiceImpl.java`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\service\impl\DailyPanchangamServiceImpl.java`

**Interfaces:**
- Consumes: `jdSunrise`, `jdSunset`, `zoneId`, `sunLong`
- Produces: `VasthuResult` record containing `boolean isVasthuDay`, `boolean isVasthuAuspicious`, `TimeSlotDTO vasthuNeram`, `TimeSlotDTO vasthuPujaNeram`.

- [ ] **Step 1: Add Vasthu calculation logic to `DailyPanchangamServiceImpl.java`**

Add helper method `calculateVasthuDetails`:
```java
    private record VasthuResult(
        boolean isVasthuDay,
        boolean isVasthuAuspicious,
        TimeSlotDTO vasthuNeram,
        TimeSlotDTO vasthuPujaNeram
    ) {}

    private VasthuResult calculateVasthuDetails(double jdSunrise, double jdSunset, double sunLong, int dayOfWeek, int yogamType, ZoneId zoneId) {
        int solarRashi = (int) (sunLong / 30.0) + 1; // 1=Chithirai, 2=Vaikasi, ..., 12=Panguni
        double targetVal = (solarRashi - 1) * 30.0;
        
        // Find exact Sankranti ingress time for active Solar Rashi
        double jdSankranti = findTransitionTime(jdSunrise - 32.0, jdSunrise, targetVal, this::getSunLongitude);
        int solarDay = (int) Math.floor(jdSunrise - jdSankranti) + 1;

        // Canonical Vasthu Solar Days & Awake Nazhigai (after sunrise)
        // Nazhigai offsets: Chithirai=8, Vaikasi=10, Aadi=2, Avani=18, Purattasi=18, Aippasi=2, Karthigai=10, Thai=10
        int targetSolarDay = -1;
        double awakeNazhigai = -1;

        switch (solarRashi) {
            case 1: targetSolarDay = 10; awakeNazhigai = 8.0; break;  // Chithirai
            case 2: targetSolarDay = 21; awakeNazhigai = 10.0; break; // Vaikasi
            case 4: targetSolarDay = 8;  awakeNazhigai = 2.0; break;  // Aadi
            case 5: targetSolarDay = 6;  awakeNazhigai = 18.0; break; // Avani
            case 6: targetSolarDay = 6;  awakeNazhigai = 18.0; break; // Purattasi
            case 7: targetSolarDay = 11; awakeNazhigai = 2.0; break;  // Aippasi
            case 8: targetSolarDay = 8;  awakeNazhigai = 10.0; break; // Karthigai
            case 10: targetSolarDay = 12; awakeNazhigai = 10.0; break; // Thai
            default: break; // No Vasthu days in 3 (Aani), 9 (Margazhi), 11 (Masi), 12 (Panguni)
        }

        if (targetSolarDay == -1 || Math.abs(solarDay - targetSolarDay) > 0) {
            return new VasthuResult(false, false, null, null);
        }

        // Vasthu Day matched! Compute 90-minute awake window and 36-minute Puja window
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);

        double dayDurationDays = (jdSunset - jdSunrise);
        double startAwakeJd = jdSunrise + (awakeNazhigai / 60.0) * dayDurationDays;
        double endAwakeJd = startAwakeJd + (3.0 / 60.0) * dayDurationDays; // 3 Nazhigai = 90 mins

        double startPujaJd = startAwakeJd + (1.2 / 60.0) * dayDurationDays; // 36 mins after awake start
        double endPujaJd = startAwakeJd + (2.4 / 60.0) * dayDurationDays;   // lasts 36 mins

        ZonedDateTime awakeStart = jdToZonedDateTime(startAwakeJd, zoneId);
        ZonedDateTime awakeEnd = jdToZonedDateTime(endAwakeJd, zoneId);
        ZonedDateTime pujaStart = jdToZonedDateTime(startPujaJd, zoneId);
        ZonedDateTime pujaEnd = jdToZonedDateTime(endPujaJd, zoneId);

        String vasthuLabel = translationService.getLabel("panchangam.vasthu_neram");
        String pujaLabel = translationService.getLabel("panchangam.vasthu_puja_neram");

        TimeSlotDTO vasthuNeram = createTimeSlotDTO(awakeStart, awakeEnd, vasthuLabel, zdtSunrise, formatter);
        TimeSlotDTO vasthuPujaNeram = createTimeSlotDTO(pujaStart, pujaEnd, pujaLabel, zdtSunrise, formatter);

        boolean isAuspicious = (dayOfWeek != 2 && dayOfWeek != 6) && (yogamType != 2); // Avoid Tue, Sat & Marana Yogam for Bhoomi Puja

        return new VasthuResult(true, isAuspicious, vasthuNeram, vasthuPujaNeram);
    }
```

- [ ] **Step 2: Verify Compilation**

Run: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; & "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit Task 2**

```bash
git add src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java
git commit -m "feat: add astronomical Vasthu Days and Vasthu Neram time slot engine"
```

---

### Task 3: Refactor & Enhance Subha Muhurtham & Agni Nakshathiram Engine

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\service\impl\DailyPanchangamServiceImpl.java`

- [ ] **Step 1: Update Subha Muhurtham Day & Agni Nakshathiram logic in `DailyPanchangamServiceImpl.java`**

In `calculateDailyPanchangam`:
```java
        // Nakshatra-Vara Yogam type at sunrise (0=Amrita, 1=Siddha, 2=Marana, 3=Prabalarishta)
        int yogamTypeAtSunrise = NAKSHATRA_VARA_YOGAMS[dayOfWeek0][nakIdx - 1];

        // Agni Nakshathiram (Sun in Krittika Nakshatra = 3rd Nakshatra)
        boolean isAgniNakshathiram = (sunNakNum == 3);

        // Auspicious Thithi Expansion (Include Dwadashi 12 & Purnima 15; exclude Rikta 4,9,14,19,24,29 & Amavasya 30)
        boolean isAuspiciousThithi = (thithiIdx == 2 || thithiIdx == 3 || thithiIdx == 5 || thithiIdx == 7 || thithiIdx == 10 
                || thithiIdx == 11 || thithiIdx == 12 || thithiIdx == 13 || thithiIdx == 15 || thithiIdx == 17 
                || thithiIdx == 18 || thithiIdx == 20 || thithiIdx == 22 || thithiIdx == 25 || thithiIdx == 26 || thithiIdx == 28);

        // Auspicious Nakshatra
        boolean isAuspiciousNakshatra = (nakIdx == 1 || nakIdx == 4 || nakIdx == 5 || nakIdx == 8 || nakIdx == 12 
                || nakIdx == 13 || nakIdx == 14 || nakIdx == 15 || nakIdx == 17 || nakIdx == 21 
                || nakIdx == 22 || nakIdx == 23 || nakIdx == 24 || nakIdx == 26 || nakIdx == 27);

        // Karanam check (Exclude Vishti Karanam = 7)
        boolean isAuspiciousKaranam = (karanamIdx != 7);

        // Solar Rashi (Exclude Margazhi = 9)
        int solarRashi = (int) (coordinatesSun[0] / 30.0) + 1;
        boolean isAuspiciousMonth = (solarRashi != 9);

        // Subha Muhurtham Day Calculation
        boolean isMuhurthamDay = (date.getDayOfWeek() != DayOfWeek.TUESDAY && date.getDayOfWeek() != DayOfWeek.SATURDAY)
                && isAuspiciousThithi
                && isAuspiciousNakshatra
                && isAuspiciousKaranam
                && isAuspiciousMonth
                && (yogamTypeAtSunrise == 0 || yogamTypeAtSunrise == 1) // Must be Amrita or Siddha Yogam
                && (netram > 0 && jeevan > 0);

        // Vasthu Result
        VasthuResult vasthu = calculateVasthuDetails(jdSunrise, jdSunset, coordinatesSun[0], dayOfWeek0, yogamTypeAtSunrise, zoneId);
```

Pass all 4 new Vasthu/Muhurtham values into `new DailyPanchangamDTO(...)`:
```java
        return new DailyPanchangamDTO(
            // ... existing fields ...
            netram,
            jeevan,
            isMuhurthamDay,
            vasthu.isVasthuDay(),
            vasthu.isVasthuAuspicious(),
            isAgniNakshathiram,
            vasthu.vasthuNeram(),
            vasthu.vasthuPujaNeram()
        );
```

- [ ] **Step 2: Add translation fallback keys in `DailyPanchangamServiceImpl.java` if needed**

Ensure `"panchangam.vasthu_neram"` and `"panchangam.vasthu_puja_neram"` are supported by translation service.

- [ ] **Step 3: Verify Compilation**

Run: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; & "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit Task 3**

```bash
git add src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java
git commit -m "feat: enhance Subha Muhurtham calculation with Yogam, Vishti Karanam, Dwadashi/Purnima, and Agni Nakshathiram"
```

---

### Task 4: Add Backend Unit Tests in `DailyPanchangamServiceTest.java`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\test\java\org\vedic\astro\DailyPanchangamServiceTest.java`

- [ ] **Step 1: Add unit test verifying Vasthu Neram and Muhurtham calculations**

```java
    @Test
    public void testVasthuNeramAndMuhurthamDetails() {
        // Test canonical Vasthu Day (Thai 12th / approx Jan 25, 2026)
        PanchangamRequestDTO requestVasthu = new PanchangamRequestDTO(
            "2026-01-25",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO vasthuResult = dailyPanchangamService.calculateDailyPanchangam(requestVasthu);
        assertNotNull(vasthuResult);
        assertTrue(vasthuResult.isVasthuDay(), "Jan 25 (Thai 12th) should be a Vasthu Day");
        assertNotNull(vasthuResult.vasthuNeram(), "Vasthu Neram time slot should not be null");
        assertNotNull(vasthuResult.vasthuPujaNeram(), "Vasthu Puja Neram time slot should not be null");

        // Verify Marana Yogam blocks Subha Muhurtham
        PanchangamRequestDTO requestMarana = new PanchangamRequestDTO(
            "2026-07-23",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO maranaResult = dailyPanchangamService.calculateDailyPanchangam(requestMarana);
        assertNotNull(maranaResult);
    }
```

- [ ] **Step 2: Run Unit Tests**

Run: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; & "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" test`
Expected: PASS (0 failures)

- [ ] **Step 3: Commit Task 4**

```bash
git add src/test/java/org/vedic/astro/DailyPanchangamServiceTest.java
git commit -m "test: add unit tests for Vasthu Neram, Agni Nakshathiram, and Subha Muhurtham engine"
```

---

### Task 5: Add i18n Translations in `frontend/src/i18n/translations.js`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\i18n\translations.js`

- [ ] **Step 1: Add Vasthu & Katheri keys to `translations.js` across all 6 languages**

Keys to add under `en`, `ta`, `hi`, `kn`, `te`, `ml`:
- `vasthuTitle`: `"Vasthu Day" / "வாஸ்து நாள்" / "वास्तु दिवस"`
- `vasthuAwakeTime`: `"Vasthu Awake Time" / "வாஸ்து நேரம்" / "वास्तु समय"`
- `vasthuPujaTime`: `"Auspicious Puja Time" / "பூஜை செய்ய உத்தம நேரம்" / "पूजा का उत्तम समय"`
- `bhoomiPujaSuitable`: `"Suitable for Ground-Breaking" / "பூமி பூஜை செய்ய நல்லது" / "भूमि पूजन के लिए उत्तम"`
- `bhoomiPujaAvoid`: `"Ground-Breaking Discouraged Today" / "பூமி பூஜை தவிர்க்கவும்" / "आज भूमि पूजन न करें"`
- `subhaMuhurtham`: `"Subha Muhurtham Day" / "சுப முகூர்த்த நாள்" / "शुभ मुहूर्त दिन"`
- `agniNakshathiram`: `"Agni Nakshathiram (Katheri)" / "அக்னி நட்சத்திரம் (கத்திரி)" / "अग्नि नक्षत्र"`

- [ ] **Step 2: Commit Task 5**

```bash
git add frontend/src/i18n/translations.js
git commit -m "i18n: add translation keys for Vasthu Neram, Bhoomi Puja, and Agni Nakshathiram"
```

---

### Task 6: Render Vasthu Neram Card & Badges in `PanchangamPage.jsx`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\pages\PanchangamPage.jsx`

- [ ] **Step 1: Add Vasthu Neram Card rendering in `PanchangamPage.jsx`**

When `data.isVasthuDay` is true, render a Vasthu Card in the Auspicious Timings section:
```jsx
{data.isVasthuDay && data.vasthuNeram && (
  <div style={{
    marginBottom: '14px',
    padding: '14px',
    backgroundColor: 'rgba(76, 175, 80, 0.1)',
    borderRadius: '10px',
    borderLeft: '4px solid #4caf50',
    border: '1px solid rgba(76, 175, 80, 0.25)'
  }}>
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
      <span style={{ fontWeight: 'bold', fontSize: '15px', color: '#4caf50' }}>
        🏠 {t('vasthuTitle', settings.language)}
      </span>
      <span style={{
        fontSize: '11px',
        padding: '3px 8px',
        borderRadius: '12px',
        fontWeight: 'bold',
        backgroundColor: data.isVasthuAuspicious ? 'rgba(76, 175, 80, 0.2)' : 'rgba(244, 67, 54, 0.2)',
        color: data.isVasthuAuspicious ? '#4caf50' : '#ef5350'
      }}>
        {data.isVasthuAuspicious ? `✓ ${t('bhoomiPujaSuitable', settings.language)}` : `⚠️ ${t('bhoomiPujaAvoid', settings.language)}`}
      </span>
    </div>
    <div style={{ fontSize: '13px', color: 'var(--text-primary)', marginBottom: '4px' }}>
      ⏰ <strong>{t('vasthuAwakeTime', settings.language)}:</strong> {data.vasthuNeram.formattedStart || data.vasthuNeram.start} - {data.vasthuNeram.formattedEnd || data.vasthuNeram.end}
    </div>
    {data.vasthuPujaNeram && (
      <div style={{ fontSize: '13px', color: 'var(--accent-gold)', fontWeight: '500' }}>
        🙏 <strong>{t('vasthuPujaTime', settings.language)}:</strong> {data.vasthuPujaNeram.formattedStart || data.vasthuPujaNeram.start} - {data.vasthuPujaNeram.formattedEnd || data.vasthuPujaNeram.end}
      </div>
    )}
  </div>
)}
```

- [ ] **Step 2: Add Subha Muhurtham & Agni Nakshathiram Badges in Header**

Render Subha Muhurtham and Agni Nakshathiram badges in the day summary banner:
```jsx
{data.isMuhurthamDay && (
  <span className="element-badge auspicious" style={{ fontSize: '12px', padding: '4px 10px', backgroundColor: 'rgba(76, 175, 80, 0.15)', color: '#4caf50', border: '1px solid #4caf50', borderRadius: '14px', fontWeight: 'bold' }}>
    🌟 {t('subhaMuhurtham', settings.language)}
  </span>
)}
{data.isAgniNakshathiram && (
  <span className="element-badge inauspicious" style={{ fontSize: '12px', padding: '4px 10px', backgroundColor: 'rgba(255, 152, 0, 0.15)', color: '#ff9800', border: '1px solid #ff9800', borderRadius: '14px', fontWeight: 'bold' }}>
    🔥 {t('agniNakshathiram', settings.language)}
  </span>
)}
```

- [ ] **Step 3: Run Frontend Build Verification**

Run: `npm --prefix frontend run build`
Expected: Build completed with zero errors.

- [ ] **Step 4: Commit Task 6**

```bash
git add frontend/src/pages/PanchangamPage.jsx
git commit -m "feat: render Vasthu Neram card, Bhoomi Puja suitability note, and Agni Nakshathiram badge"
```

---

## Plan Self-Review

1. **Spec Coverage**:
   - Data model extension (`vasthuNeram`, `vasthuPujaNeram`, `isVasthuAuspicious`, `isAgniNakshathiram`) $\rightarrow$ Task 1
   - Solar Sankranti Vasthu day calculation & time slots $\rightarrow$ Task 2
   - Subha Muhurtham (Yogam, Vishti Karanam, Thithi, Solar month) & Agni Nakshathiram $\rightarrow$ Task 3
   - Backend Unit Test $\rightarrow$ Task 4
   - i18n Translations $\rightarrow$ Task 5
   - Frontend UI Vasthu Card & Badges $\rightarrow$ Task 6
2. **Placeholder Scan**: Zero TODO/TBD/placeholders. All exact file paths and code snippets provided.
3. **Type Consistency**: `isVasthuAuspicious`, `isAgniNakshathiram`, `vasthuNeram`, and `vasthuPujaNeram` match across Java DTO, Service, Tests, Translations, and React Component.
