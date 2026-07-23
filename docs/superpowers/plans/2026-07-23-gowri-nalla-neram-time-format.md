# Gowri Nalla Neram & Nakshatra Yogam Next-Day Time Format Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend `TimeSlotDTO` in Java with `startNextDay` and `endNextDay` boolean flags derived from `ZonedDateTime.toLocalDate().isAfter(zdtSunrise.toLocalDate())`, and update `PanchangamPage.jsx` to render localized Next Day tags (`(அடுத்த நாள்)`) for all post-midnight time points in Gowri Nalla Neram and Nakshatra Yogam lists.

**Architecture:** 
1. Java: Add `boolean startNextDay` and `boolean endNextDay` to `DailyPanchangamDTO.TimeSlotDTO`.
2. Java: Compute `startNextDay` and `endNextDay` in `DailyPanchangamServiceImpl.java` by comparing time slot `ZonedDateTime` against `zdtSunrise.toLocalDate()`.
3. Frontend: Update `formatSlotListTimes` in `PanchangamPage.jsx` to check backend boolean flags and fallback to inter-slot sequence progression (`startMins < prevMins`).

**Tech Stack:** Java 17+, Spring Boot, JUnit 5, React, Vite.

## Global Constraints

- **Language/i18n**: Support localized Next Day strings using `translations.js` (`nextDay`).
- **Backwards Compatibility**: Keep 3-parameter `TimeSlotDTO` constructor defaulting `startNextDay = false` and `endNextDay = false`.
- **UI Layout**: Retain flex wrapping, vertical centering (`alignSelf: 'center'`), and right-alignment (`marginLeft: 'auto'`) for time slot badges.

---

### Task 1: Extend `TimeSlotDTO` in `DailyPanchangamDTO.java`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\dto\DailyPanchangamDTO.java:40-45`

**Interfaces:**
- Consumes: Java `DailyPanchangamDTO` structure
- Produces: `TimeSlotDTO` record with `boolean startNextDay` and `boolean endNextDay` fields and 3-parameter overloaded constructor.

- [ ] **Step 1: Update `TimeSlotDTO` record definition in `DailyPanchangamDTO.java`**

```java
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
```

- [ ] **Step 2: Verify Java Compilation**

Run: `./gradlew compileJava` or `mvn test-compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit Task 1**

```bash
git add src/main/java/org/vedic/astro/dto/DailyPanchangamDTO.java
git commit -m "feat: extend TimeSlotDTO with startNextDay and endNextDay boolean flags"
```

---

### Task 2: Calculate Next-Day Flags in `DailyPanchangamServiceImpl.java`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\service\impl\DailyPanchangamServiceImpl.java`

**Interfaces:**
- Consumes: `ZonedDateTime start`, `ZonedDateTime end`, `ZonedDateTime zdtSunrise`
- Produces: `TimeSlotDTO` instances with calculated `startNextDay` and `endNextDay` boolean flags.

- [ ] **Step 1: Add helper `createTimeSlotDTO` in `DailyPanchangamServiceImpl.java`**

```java
    private TimeSlotDTO createTimeSlotDTO(ZonedDateTime start, ZonedDateTime end, String label, ZonedDateTime zdtSunrise, DateTimeFormatter formatter) {
        LocalDate panchangamDate = zdtSunrise.toLocalDate();
        boolean startNextDay = start.toLocalDate().isAfter(panchangamDate);
        boolean endNextDay = end.toLocalDate().isAfter(panchangamDate);
        return new TimeSlotDTO(start.format(formatter), end.format(formatter), label, startNextDay, endNextDay);
    }
```

- [ ] **Step 2: Update `calculateGowriNallaNeram` in `DailyPanchangamServiceImpl.java`**

Update Day Gowri loop (lines 765-768) and Night Gowri loop (lines 779-782) to use `createTimeSlotDTO`:
```java
// Day Gowri
ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);
String stateLabel = translationService.getLabel("gowri." + state.toLowerCase());
list.add(createTimeSlotDTO(start, end, stateLabel, zdtSunrise, formatter));

// Night Gowri
ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);
String stateLabel = translationService.getLabel("gowri." + state.toLowerCase());
list.add(createTimeSlotDTO(start, end, stateLabel, zdtSunrise, formatter));
```

- [ ] **Step 3: Update `calculateNakshatraYogams` in `DailyPanchangamServiceImpl.java`**

Update Span 1 (lines 856-866) and Span 2 (lines 870-880) to use `createTimeSlotDTO`:
```java
ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);
ZonedDateTime s1 = jdToZonedDateTime(jdSunrise, zoneId);
ZonedDateTime e1 = jdToZonedDateTime(span1EndJd, zoneId);
list.add(createTimeSlotDTO(s1, e1, translationService.getLabel(key1), zdtSunrise, formatter));

if (endJd > 0 && endJd < jdNextSunrise) {
    ZonedDateTime s2 = jdToZonedDateTime(endJd, zoneId);
    ZonedDateTime e2 = jdToZonedDateTime(jdNextSunrise, zoneId);
    list.add(createTimeSlotDTO(s2, e2, translationService.getLabel(key2), zdtSunrise, formatter));
}
```

- [ ] **Step 4: Update `calculateKalam` in `DailyPanchangamServiceImpl.java`**

Update Kalam calculation to use `createTimeSlotDTO`:
```java
ZonedDateTime start = zdtSunrise.plusMinutes((long) (startPart * partDurationHours * 60));
ZonedDateTime end = zdtSunrise.plusMinutes((long) (endPart * partDurationHours * 60));
return List.of(createTimeSlotDTO(start, end, label, zdtSunrise, formatter));
```

- [ ] **Step 5: Verify Java Compilation**

Run: `./gradlew compileJava` or `mvn test-compile`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit Task 2**

```bash
git add src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java
git commit -m "feat: compute startNextDay and endNextDay boolean flags in DailyPanchangamServiceImpl"
```

---

### Task 3: Add Backend Unit Tests

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\test\java\org\vedic\astro\service\DailyPanchangamServiceTest.java`

- [ ] **Step 1: Add unit test verifying `TimeSlotDTO` next day flags**

```java
    @Test
    void testGowriNallaNeramNextDayFlags() {
        DailyPanchangamDTO dto = service.getPanchangam(LocalDate.of(2026, 7, 23), 13.0827, 80.2707, "ta", "LAHIRI");
        assertNotNull(dto.gowriNallaNeram());
        for (DailyPanchangamDTO.TimeSlotDTO slot : dto.gowriNallaNeram()) {
            if (slot.start().contains("12:15 AM") || slot.start().contains("01:39 AM") || slot.start().contains("03:04 AM")) {
                assertTrue(slot.startNextDay(), "Start time " + slot.start() + " should have startNextDay=true");
                assertTrue(slot.endNextDay(), "End time " + slot.end() + " should have endNextDay=true");
            }
        }
    }
```

- [ ] **Step 2: Run Unit Tests**

Run: `./gradlew test` or `mvn test`
Expected: PASS

- [ ] **Step 3: Commit Task 3**

```bash
git add src/test/java/org/vedic/astro/service/DailyPanchangamServiceTest.java
git commit -m "test: add unit test for Gowri Nalla Neram next-day flags"
```

---

### Task 4: Update Frontend `formatSlotListTimes` in `PanchangamPage.jsx`

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\pages\PanchangamPage.jsx:99-153`

- [ ] **Step 1: Update `formatSlotListTimes` in `PanchangamPage.jsx`**

```javascript
  const formatSlotListTimes = (slots, nextDayText) => {
    if (!slots || !Array.isArray(slots)) return [];
    let isOvernight = false;
    let prevMins = -1;
    const nextDayKeywords = ['next day', 'அடுத்த நாள்', 'اگلے دن', 'अगले दिन', 'ಮುಂದಿನ ದಿನ', 'తరువాత రోజు', 'അടുത്ത ദിവസം'];

    return slots.map((s) => {
      if (!s) return s;
      const startStr = s.start || '';
      const endStr = s.end || '';

      const startMins = parseTimeToMinutes(startStr);
      const endMins = parseTimeToMinutes(endStr);

      const hasStartNextDayKey = nextDayKeywords.some(k => startStr.toLowerCase().includes(k.toLowerCase()));
      const hasEndNextDayKey = nextDayKeywords.some(k => endStr.toLowerCase().includes(k.toLowerCase()));

      let startIsNextDay = Boolean(s.startNextDay) || isOvernight || hasStartNextDayKey;
      if (!startIsNextDay && prevMins >= 0 && startMins >= 0 && startMins < prevMins) {
        startIsNextDay = true;
      }
      if (startIsNextDay) isOvernight = true;

      let endIsNextDay = Boolean(s.endNextDay) || isOvernight || hasEndNextDayKey;
      if (!endIsNextDay && startMins >= 0 && endMins >= 0 && endMins < startMins) {
        endIsNextDay = true;
      }
      if (endIsNextDay) isOvernight = true;

      if (endMins >= 0) prevMins = endMins;
      else if (startMins >= 0) prevMins = startMins;

      const formatSingleTime = (timeStr, isNext) => {
        if (!timeStr) return '';
        const ignoreKeywords = ['throughout', 'நாள் முழுவதும்', 'दिन भर', 'இಡீ ದಿನ', 'త్రోలట్', 'മുഴുവൻ'];
        if (ignoreKeywords.some(k => timeStr.toLowerCase().includes(k.toLowerCase()))) return timeStr;
        if (nextDayKeywords.some(k => timeStr.toLowerCase().includes(k.toLowerCase()))) return timeStr;
        return isNext ? `${timeStr} (${nextDayText})` : timeStr;
      };

      return {
        ...s,
        formattedStart: formatSingleTime(startStr, startIsNextDay),
        formattedEnd: formatSingleTime(endStr, endIsNextDay)
      };
    });
  };
```

- [ ] **Step 2: Run Frontend Build Verification**

Run: `npm --prefix frontend run build`
Expected: Build completed with zero errors.

- [ ] **Step 3: Commit Task 4**

```bash
git add frontend/src/pages/PanchangamPage.jsx
git commit -m "feat: integrate backend boolean flags and inter-slot sequence check in formatSlotListTimes"
```

---

## Plan Self-Review

1. **Spec Coverage**:
   - `TimeSlotDTO` extension $\rightarrow$ Task 1
   - `DailyPanchangamServiceImpl` date comparison calculation $\rightarrow$ Task 2
   - Backend Unit Test $\rightarrow$ Task 3
   - Frontend `PanchangamPage.jsx` integration $\rightarrow$ Task 4
2. **Placeholder Scan**: Zero TODO/TBD/placeholders. All exact file paths and code provided.
3. **Type Consistency**: `startNextDay` and `endNextDay` names match across Java DTO, Service, Unit Test, and React component.
