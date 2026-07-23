# Gowri Nalla Neram Time Formatting & Alignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement precise midnight-triggered Next Day time slot formatting (`formatSlotListTimes`) and vertical layout alignment (`renderTimeSlotList` & `renderNakshatraYogamsList`) in `PanchangamPage.jsx`.

**Architecture:** Update `formatSlotListTimes(slots, nextDayText)` in `PanchangamPage.jsx` so that `isOvernight` is triggered exclusively when a time slot crosses 12:00 AM midnight (`endMins < startMins`), leaving all daytime/evening slots clean.

**Tech Stack:** React, JSX, Vanilla CSS, i18n

## Global Constraints
- Target Language Support: `en`, `ta`, `hi`, `kn`, `te`, `ml`
- UI Alignment: Vertical centering (`alignSelf: 'center'`) and responsive flex layout

---

### Task 1: Midnight-Triggered Next Day Time Slot Formatting & Layout Alignment

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\pages\PanchangamPage.jsx:90-250`

**Interfaces:**
- Consumes: `t('nextDay', settings.language)`
- Produces: `formattedStart` and `formattedEnd` properties on slot objects in `renderTimeSlotList` and `renderNakshatraYogamsList`.

- [ ] **Step 1: Update `formatSlotListTimes` in `PanchangamPage.jsx`**

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
      if (startIsNextDay) {
        isOvernight = true;
      }

      let endIsNextDay = isOvernight || hasEndNextDayKey;
      if (!endIsNextDay && startMins >= 0 && endMins >= 0) {
        if (endMins < startMins) {
          endIsNextDay = true;
        }
      }

      if (endIsNextDay) {
        isOvernight = true;
      }

      const formatSingleTime = (timeStr, isNext) => {
        if (!timeStr) return '';
        const ignoreKeywords = ['throughout', 'நாள் முழுவதும்', 'दिन भर', '<ctrl42><ctrl42>இಡೀ ದಿನ', 'త్రోలట్', 'മുഴുവൻ'];
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

- [ ] **Step 2: Verify `renderTimeSlotList` and `renderNakshatraYogamsList` apply `formatSlotListTimes`**

- [ ] **Step 3: Run `npm run build` in `frontend/` to verify clean compilation**

- [ ] **Step 4: Commit changes**

```bash
git add frontend/src/pages/PanchangamPage.jsx docs/superpowers/plans/2026-07-23-gowri-nalla-neram-time-format.md
git commit -m "fix(ui): update Gowri Nalla Neram time slot formatting to trigger next-day tag strictly on midnight crossing"
```
