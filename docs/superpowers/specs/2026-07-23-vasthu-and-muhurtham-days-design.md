# Design Specification: Vasthu Days, Vasthu Neram & Subha Muhurtham Engine

## 1. Overview
This specification details the design for accurate astronomical calculation of **Vasthu Days**, **Vasthu Neram** (awake and Puja time windows), and **Subha Muhurtham Days** (auspicious days) in the Vedic Panchangam engine (`DailyPanchangamServiceImpl.java`) and frontend rendering (`PanchangamPage.jsx`).

---

## 2. Technical Requirements

### 2.1 Vasthu Days & Vasthu Neram Calculation
1. **Solar Month & Solar Date Calculation**:
   - Determine Sun's sidereal longitude `sunLong`.
   - Identify active Solar Rashi `solarRashi = (int)(sunLong / 30.0) + 1` (1=Chithirai, 2=Vaikasi, ..., 12=Panguni).
   - Compute exact `jdSankranti` (time Sun entered `(solarRashi - 1) * 30.0` degrees).
   - Solar Day index: `solarDay = (int) Math.floor(jdSunrise - jdSankranti) + 1`.

2. **Vasthu Canonical Day & Awake Offset Table**:
   - **Chithirai (1)**: 10th solar day $\rightarrow$ Awake at 8 *Nazhigai* after sunrise (offset = 3.2 hours).
   - **Vaikasi (2)**: 21st solar day $\rightarrow$ Awake at 10 *Nazhigai* after sunrise (offset = 4.0 hours).
   - **Aadi (4)**: 8th solar day $\rightarrow$ Awake at 2 *Nazhigai* after sunrise (offset = 0.8 hours).
   - **Avani (5)**: 6th solar day $\rightarrow$ Awake at 18 *Nazhigai* after sunrise (offset = 7.2 hours).
   - **Purattasi (6)**: 6th solar day $\rightarrow$ Awake at 18 *Nazhigai* after sunrise (offset = 7.2 hours).
   - **Aippasi (7)**: 11th solar day $\rightarrow$ Awake at 2 *Nazhigai* after sunrise (offset = 0.8 hours).
   - **Karthigai (8)**: 8th solar day $\rightarrow$ Awake at 10 *Nazhigai* after sunrise (offset = 4.0 hours).
   - **Thai (10)**: 12th solar day $\rightarrow$ Awake at 10 *Nazhigai* after sunrise (offset = 4.0 hours).
   - *(No Vasthu days in Aani 3, Margazhi 9, Masi 11, Panguni 12).*

3. **Vasthu Neram Window Construction**:
   - Total Awake Duration = 90 minutes (3 *Nazhigai*).
   - `vasthuNeram`: `[awakeStart, awakeStart + 90 mins]`.
   - `vasthuPujaNeram`: `[awakeStart + 36 mins, awakeStart + 72 mins]` (the 36-minute middle Puja window).

---

### 2.2 Subha Muhurtham Day Calculation
1. **Weekday Filter**: Exclude Tuesday & Saturday (`date.getDayOfWeek() != TUESDAY && date.getDayOfWeek() != SATURDAY`).
2. **Thithi Expansion**:
   - Include `12` (**Dwadashi**) and `15` (**Purnima** / Full Moon) in `isAuspiciousThithi`.
   - Auspicious Thithis: `[2, 3, 5, 7, 10, 11, 12, 13, 15, 17, 18, 20, 22, 25, 26, 28]`.
3. **Nakshatra Validation**:
   - Auspicious Nakshatras: `[1, 4, 5, 8, 12, 13, 14, 15, 17, 21, 22, 23, 24, 26, 27]`.
4. **Nakshatra-Vara Yogam Filter**:
   - Require `yogamType == 0 || yogamType == 1` (**Amrita Yogam** or **Siddha Yogam**).
   - Explicitly reject **Marana Yogam** (2) and **Prabalarishta Yogam** (3).
5. **Netram & Jeevan Validation**: `netram > 0 && jeevan > 0`.
6. **Solar Month Exclusion**: Exclude *Margazhi* (`solarRashi == 9`).

---

## 3. Data Model Changes

### 3.1 `DailyPanchangamDTO.java`
Update `DailyPanchangamDTO` record to include `vasthuNeram` and `vasthuPujaNeram`:
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
    TimeSlotDTO vasthuNeram,
    TimeSlotDTO vasthuPujaNeram
) {}
```

---

## 4. Frontend Integration (`PanchangamPage.jsx`)

1. **Vasthu Day Card**:
   When `data.isVasthuDay` is true, render a prominent card containing:
   - 🏠 **Vasthu Day / வாஸ்து நாள்**
   - ⏰ **Vasthu Awake Window (வாஸ்து நேரம்)**: `data.vasthuNeram.start` - `data.vasthuNeram.end`
   - 🙏 **Vasthu Puja Window (பூஜை செய்ய உத்தம நேரம்)**: `data.vasthuPujaNeram.start` - `data.vasthuPujaNeram.end`
2. **Subha Muhurtham Badge**: Highlight `isMuhurthamDay` in the page header card.

---

## 5. Verification Plan

1. **Backend Unit Tests (`DailyPanchangamServiceTest.java`)**:
   - Verify `isVasthuDay` and `vasthuNeram` for canonical Vasthu dates (e.g., Thai 12th / Jan 25).
   - Verify `isMuhurthamDay` is false when a day has Marana Yogam.
   - Verify Purnima (`thithiIdx == 15`) and Dwadashi (`thithiIdx == 12`) are treated as auspicious thithis.
2. **Build Verification**:
   - Backend: `./mvnw compile` / `$env:JAVA_HOME=... mvn test`.
   - Frontend: `npm run build` inside `frontend/`.
