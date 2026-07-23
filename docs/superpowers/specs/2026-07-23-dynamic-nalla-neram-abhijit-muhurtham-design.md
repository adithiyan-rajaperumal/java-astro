# Design Spec: Dynamic Nalla Neram & Abhijit Muhurtham Engine

## 1. Goal
Replace static hour-offset Nalla Neram calculations with a 100% astronomical, dynamic calculation derived directly from Swiss Ephemeris Sunrise and Sunset timestamps, with zero static clock hour assumptions. Additionally, calculate and render **Abhijit Muhurtham** dynamically across backend DTOs, i18n, and UI.

---

## 2. Technical Architecture

### 2.1 Dynamic Nalla Neram Calculation (`DailyPanchangamServiceImpl.java`)
1. **Dynamic Daytime Division**:
   $$\text{DayPartDuration} = \frac{\text{jdSunset} - \text{jdSunrise}}{8.0}$$
2. **Dynamic Nighttime Division**:
   $$\text{NightPartDuration} = \frac{\text{jdNextSunrise} - \text{jdSunset}}{8.0}$$
3. **Selection Rule**:
   - For daytime parts $i \in [0..7]$:
     - Check Gowri state $S = \text{GOWRI\_DAY\_STATES}[dayOfWeek][i]$.
     - Check if part $i+1$ equals Rahu part, Yamagandam part, or Kulikai part for the day.
     - If $S \in \{\text{"Amirdha"}, \text{"Laabam"}, \text{"Dhanam"}, \text{"Sugam"}, \text{"Uthi"}\}$ AND part $i+1 \notin \{\text{RahuPart}, \text{YamaPart}, \text{KulikaiPart}\}$, then convert dynamic interval $[ \text{jdSunrise} + i \times \text{DayPartDuration}, \text{jdSunrise} + (i+1) \times \text{DayPartDuration} ]$ into `TimeSlotDTO`.
   - For nighttime parts $i \in [0..7]$:
     - Check Gowri state $S = \text{GOWRI\_NIGHT\_STATES}[dayOfWeek][i]$.
     - If $S \in \{\text{"Amirdha"}, \text{"Laabam"}, \text{"Dhanam"}, \text{"Sugam"}, \text{"Uthi"}\}$, convert dynamic interval into `TimeSlotDTO`.

---

### 2.2 Abhijit Muhurtham Engine
1. **Astronomical Definition**: 8th Muhurtham out of 15 equal daytime divisions ($\frac{1}{15}$ of day duration).
2. **Formula**:
   $$\text{MuhurthamLength} = \frac{\text{jdSunset} - \text{jdSunrise}}{15.0}$$
   $$\text{AbhijitStart} = \text{jdSunrise} + 7 \times \text{MuhurthamLength}$$
   $$\text{AbhijitEnd} = \text{jdSunrise} + 8 \times \text{MuhurthamLength}$$
3. **Wednesday Nullification**:
   - On Wednesday ($dayOfWeek == 3$), Abhijit Muhurtham is afflicted (Wednesday Rahu/Durmuhurtham collision). We return `TimeSlotDTO` with a label indicating nullification / inactive, or flag it accordingly.

---

## 3. Data Transfer & UI Components

### 3.1 DTO Updates (`DailyPanchangamDTO.java`)
Add field to record:
```java
TimeSlotDTO abhijitMuhurtham
```

### 3.2 i18n Translations (`frontend/src/i18n/translations.js`)
Add translation key `abhijitMuhurtham`:
- `en`: "Abhijit Muhurtham"
- `ta`: "அபிஜித் முகூர்த்தம்"
- `hi`: "अभिजित मुहूर्त"
- `kn`: "ಅಭಿಜಿತ್ ಮುಹೂರ್ತ"
- `te`: "అభిజిత్ ముహూర్తం"
- `ml`: "അഭിജിത് മുഹൂർത്തം"

### 3.3 UI Display (`frontend/src/pages/PanchangamPage.jsx`)
Render Abhijit Muhurtham card next to Nalla Neram & Gowri Nalla Neram.

---

## 4. Files Modified
- `DailyPanchangamDTO.java`
- `DailyPanchangamServiceImpl.java`
- `frontend/src/i18n/translations.js`
- `frontend/src/pages/PanchangamPage.jsx`
