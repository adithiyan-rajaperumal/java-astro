# Gowri Nalla Neram Time Formatting & Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement unified selective Next Day time slot formatting (`formatSlotListTimes`) and vertical layout alignment (`renderTimeSlotList` & `renderNakshatraYogamsList`) in `PanchangamPage.jsx`.

**Architecture:** Create a shared `formatSlotListTimes(slots, nextDayText)` helper in `PanchangamPage.jsx` that attaches Next Day tags (`(அடுத்த நாள்)`, `(Next Day)`) exclusively to time points crossing or occurring after 12:00 AM (midnight). Update flex styling to vertically center time range badges (`alignSelf: 'center'`) across all time slot bars.

**Tech Stack:** React, JSX, Vanilla CSS, i18n

## Global Constraints
- Target Language Support: `en`, `ta`, `hi`, `kn`, `te`, `ml`
- UI Alignment: Vertical centering (`alignSelf: 'center'`) and responsive flex layout

---

### Task 1: Selective Next-Day Time Slot Formatting & Layout Alignment

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\pages\PanchangamPage.jsx:90-250`

**Interfaces:**
- Consumes: `t('nextDay', settings.language)`
- Produces: `formattedStart` and `formattedEnd` properties on slot objects in `renderTimeSlotList` and `renderNakshatraYogamsList`.

- [ ] **Step 1: Write `formatSlotListTimes` helper in `PanchangamPage.jsx`**

```javascript
  const formatSlotListTimes = (slots, nextDayText) => {
    if (!slots || !Array.isArray(slots)) return [];
    let isOvernight = false;
    const nextDayKeywords = ['next day', 'அடுத்த நாள்', 'اگلے دن', 'अगले दिन', 'ಮುಂದಿನ ದಿನ', 'తరువాత రోజు', 'അടുത്ത ദിവസം'];

    return slots.map((s, idx) => {
      if (!s) return s;
      const startStr = s.start || '';
      const endStr = s.end || '';

      const startMins = parseTimeToMinutes(startStr);
      const endMins = parseTimeToMinutes(endStr);

      const hasStartNextDayKey = nextDayKeywords.some(k => startStr.toLowerCase().includes(k.toLowerCase()));
      const hasEndNextDayKey = nextDayKeywords.some(k => endStr.toLowerCase().includes(k.toLowerCase()));

      let startIsNextDay = isOvernight || hasStartNextDayKey;
      if (!startIsNextDay && idx > 0 && startMins >= 0 && startMins <= 8 * 60 + 30) {
        startIsNextDay = true;
      }

      if (startIsNextDay) {
        isOvernight = true;
      }

      let endIsNextDay = isOvernight || hasEndNextDayKey;
      if (!endIsNextDay && startMins >= 0 && endMins >= 0) {
        if (endMins < startMins) {
          endIsNextDay = true;
        } else if (endMins <= 8 * 60 + 30 && startMins >= 20 * 60) {
          endIsNextDay = true;
        }
      }

      if (endIsNextDay) {
        isOvernight = true;
      }

      const formatSingleTime = (timeStr, isNext) => {
        if (!timeStr) return '';
        const ignoreKeywords = ['throughout', 'நாள் முழுவதும்', 'दिन भर', 'இಡೀ ದಿನ', 'త్రోలಟ್', 'മുഴുവൻ'];
        if (ignoreKeywords.some(k => timeStr.toLowerCase().includes(k.toLowerCase()))) {
          return timeStr;
        }
        const alreadyHasTag = nextDayKeywords.some(k => timeStr.toLowerCase().includes(k.toLowerCase()));
        if (alreadyHasTag) {
          return timeStr;
        }
        if (isNext) {
          return `${timeStr} (${nextDayText})`;
        }
        return timeStr;
      };

      return {
        ...s,
        formattedStart: formatSingleTime(startStr, startIsNextDay),
        formattedEnd: formatSingleTime(endStr, endIsNextDay)
      };
    });
  };
```

- [ ] **Step 2: Update `renderTimeSlotList` and `renderNakshatraYogamsList` to use `formatSlotListTimes` and `alignSelf: 'center'`**

- [ ] **Step 3: Run `npm run build` in `frontend/` to verify clean compilation**

- [ ] **Step 4: Commit changes**

```bash
git add frontend/src/pages/PanchangamPage.jsx docs/superpowers/plans/2026-07-23-gowri-nalla-neram-time-format.md
git commit -m "feat(ui): implement selective next-day time slot formatting and vertical alignment for Gowri Nalla Neram"
```
