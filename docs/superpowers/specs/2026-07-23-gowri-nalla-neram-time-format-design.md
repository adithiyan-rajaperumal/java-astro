# Design Specification: Java-Driven Next-Day Time Slot Formatting

## 1. Overview
This specification details the Java backend-driven design for interval-based time slot formatting in Panchangam time slot cards (`TimeSlotDTO`), specifically addressing Gowri Nalla Neram and Nakshatra Yogam slots in `PanchangamPage.jsx`.

By leveraging `ZonedDateTime.toLocalDate().isAfter(zdtSunrise.toLocalDate())` on the Java backend, exact calendar date boundaries are evaluated for both start and end timestamps. Next Day indicators (`(அடுத்த நாள்)`, `(Next Day)`, etc.) are accurately attached whenever a timestamp falls on the post-midnight date relative to the Panchangam day's sunrise.

---

## 2. Requirements & Architecture

1. **Java Data Model (`TimeSlotDTO`) Extension**:
   - Add `boolean startNextDay` and `boolean endNextDay` to `TimeSlotDTO`.
   - Maintain overloaded constructors to ensure backwards compatibility.

2. **Backend Date Boundary Calculation**:
   - For all time slots generated in `DailyPanchangamServiceImpl.java` (including `calculateGowriNallaNeram`, `calculateNakshatraYogams`, `calculateKalam`, and `calculateDynamicNallaNeram`):
     - `startNextDay = startZdt.toLocalDate().isAfter(zdtSunrise.toLocalDate())`
     - `endNextDay = endZdt.toLocalDate().isAfter(zdtSunrise.toLocalDate())`

3. **Frontend Formatting (`PanchangamPage.jsx`)**:
   - In `formatSlotListTimes(slots, nextDayText)`, evaluate `s.startNextDay` and `s.endNextDay`.
   - Append `(${nextDayText})` to `formattedStart` and `formattedEnd` whenever the respective boolean flag is `true`.
   - Retain sequence fallback check (`startMins < prevMins` or `endMins < startMins`) for resiliency.

---

## 3. Detailed Component Changes

### 3.1 Backend: `TimeSlotDTO.java`
Update `TimeSlotDTO` record definition:
```java
package org.vedic.astro.dto;

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

### 3.2 Backend: `DailyPanchangamServiceImpl.java`
Helper method to construct `TimeSlotDTO` with automatic sunrise date comparison:
```java
private TimeSlotDTO createTimeSlotDTO(ZonedDateTime start, ZonedDateTime end, String label, ZonedDateTime zdtSunrise, DateTimeFormatter formatter) {
    LocalDate panchangamDate = zdtSunrise.toLocalDate();
    boolean startNextDay = start.toLocalDate().isAfter(panchangamDate);
    boolean endNextDay = end.toLocalDate().isAfter(panchangamDate);
    return new TimeSlotDTO(start.format(formatter), end.format(formatter), label, startNextDay, endNextDay);
}
```
Apply `createTimeSlotDTO` across:
- `calculateGowriNallaNeram` (Day and Night Gowri loops)
- `calculateNakshatraYogams` (Span 1 and Span 2 slots)
- `calculateKalam` (Rahu Kalam, Yamagandam, Gulikai)
- `calculateDynamicNallaNeram`

### 3.3 Frontend: `PanchangamPage.jsx`
Update `formatSlotListTimes`:
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

    // Use backend boolean flags if present, otherwise fallback to sequence checks
    let startIsNextDay = s.startNextDay || isOvernight || hasStartNextDayKey;
    if (!startIsNextDay && prevMins >= 0 && startMins >= 0 && startMins < prevMins) {
      startIsNextDay = true;
    }
    if (startIsNextDay) isOvernight = true;

    let endIsNextDay = s.endNextDay || isOvernight || hasEndNextDayKey;
    if (!endIsNextDay && startMins >= 0 && endMins >= 0 && endMins < startMins) {
      endIsNextDay = true;
    }
    if (endIsNextDay) isOvernight = true;

    if (endMins >= 0) prevMins = endMins;
    else if (startMins >= 0) prevMins = startMins;

    const formatSingleTime = (timeStr, isNext) => {
      if (!timeStr) return '';
      const ignoreKeywords = ['throughout', 'நாள் முழுவதும்', 'दिन भर', 'இಡೀ ದಿನ', 'త్రోలట్', 'മുഴുവൻ'];
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

---

## 4. Verification & Testing Strategy

1. **Unit Test Scenarios**:
   - Post-midnight Gowri slots (e.g. `12:15 AM - 01:39 AM`, `03:04 AM - 04:28 AM`) $\rightarrow$ `12:15 AM (அடுத்த நாள்) - 01:39 AM (அடுத்த நாள்)`.
   - Daytime/evening Gowri slots (e.g. `07:28 AM - 09:04 AM`, `09:26 PM - 10:51 PM`) $\rightarrow$ Clean (no tag).
   - Micro-span Nakshatra Yogam (e.g. `05:52 AM - 05:53 AM`) $\rightarrow$ Clean (no tag).
   - Overnight Nakshatra Yogam (e.g. `07:34 AM - 05:53 AM`) $\rightarrow$ `07:34 AM - 05:53 AM (அடுத்த நாள்)`.
2. **Build Verification**:
   - Backend: `mvn test-compile` / `./gradlew compileJava` or Java compilation.
   - Frontend: `npm run build` inside `frontend/`.
