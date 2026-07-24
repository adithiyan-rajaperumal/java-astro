# Mobile-First Date Navigation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 1-click single-touch date navigation buttons (`◀ Prev` and `Next ▶`) alongside the Today button and Date Picker in a mobile-first responsive top bar.

**Architecture:** Add `changeDateByDays(deltaDays)` in `PanchangamPage.jsx`, add `prevDay` translation keys in `translations.js`, and style `.panchangam-top-bar` with flex-wrap and responsive mobile button sizing.

**Tech Stack:** React 19, CSS3, `t()` translation helper.

## Global Constraints
- Target File 1: `frontend/src/i18n/translations.js`
- Target File 2: `frontend/src/pages/PanchangamPage.jsx`
- Target File 3: `frontend/src/index.css` (or `App.css` / inline responsive styles)

---

### Task 1: Add `prevDay` Translation Keys in `translations.js`

**Files:**
- Modify: `frontend/src/i18n/translations.js`

**Interfaces:**
- Consumes: None
- Produces: `prevDay` and `nextDayBtn` translation strings across all supported languages

- [ ] **Step 1: Add translation keys to `frontend/src/i18n/translations.js`**

Add `prevDay: "Prev Day"` and `nextDayBtn: "Next Day"` to `en`, `ta` (`prevDay: "முந்தைய நாள்"`, `nextDayBtn: "அடுத்த நாள்"`), `hi`, `kn`, `te`, `ml`.

- [ ] **Step 2: Commit**

```bash
git add frontend/src/i18n/translations.js
git commit -m "i18n: add prevDay and nextDayBtn translation keys"
```

---

### Task 2: Add `changeDateByDays` Handler and Responsive Buttons to `PanchangamPage.jsx`

**Files:**
- Modify: `frontend/src/pages/PanchangamPage.jsx`

**Interfaces:**
- Consumes: `currentDate`, `setCurrentDate`, `t` helper
- Produces: 1-click `◀ Prev` and `Next ▶` buttons with mobile-first responsive flex wrapping

- [ ] **Step 1: Add `changeDateByDays` function in `PanchangamPage`**

```javascript
const changeDateByDays = (deltaDays) => {
  if (!currentDate) return;
  const [year, month, day] = currentDate.split('-').map(Number);
  const dateObj = new Date(year, month - 1, day);
  dateObj.setDate(dateObj.getDate() + deltaDays);
  const y = dateObj.getFullYear();
  const m = String(dateObj.getMonth() + 1).padStart(2, '0');
  const d = String(dateObj.getDate()).padStart(2, '0');
  setCurrentDate(`${y}-${m}-${d}`);
};
```

- [ ] **Step 2: Render Top Bar Navigation Buttons in `PanchangamPage.jsx`**

Replace `panchangam-top-bar` JSX with:
```jsx
<div className="panchangam-top-bar" style={{ display: 'flex', gap: '8px', alignItems: 'center', justifyContent: 'center', flexWrap: 'wrap', marginBottom: '16px' }}>
  <div style={{ display: 'flex', gap: '6px', alignItems: 'center' }}>
    <button 
      onClick={() => changeDateByDays(-1)} 
      className="today-btn"
      title="Previous Day"
      style={{ padding: '6px 14px', fontSize: '13px' }}
    >
      ◀ {t('prevDay', settings.language) || 'Prev'}
    </button>
    <button 
      onClick={() => setCurrentDate(getTodayDateString(settings.location))} 
      className="today-btn"
      style={{ padding: '6px 14px', fontSize: '13px' }}
    >
      {t('today', settings.language)}
    </button>
    <button 
      onClick={() => changeDateByDays(1)} 
      className="today-btn"
      title="Next Day"
      style={{ padding: '6px 14px', fontSize: '13px' }}
    >
      {t('nextDayBtn', settings.language) || 'Next'} ▶
    </button>
  </div>

  <input
    type="date"
    className="date-picker-input"
    value={currentDate}
    onChange={(e) => e.target.value && setCurrentDate(e.target.value)}
    style={{ height: '36px', padding: '4px 10px', fontSize: '14px' }}
  />

  <button
    onClick={handleShareAsImage}
    disabled={sharing || !data}
    style={{
      background: 'linear-gradient(135deg, #ffd700, #ff9800)',
      color: '#000000',
      border: 'none',
      padding: '8px 18px',
      borderRadius: '20px',
      fontWeight: 'bold',
      fontSize: '13.5px',
      cursor: sharing ? 'wait' : 'pointer',
      boxShadow: '0 4px 12px rgba(255, 215, 0, 0.3)',
      display: 'inline-flex',
      alignItems: 'center',
      gap: '6px'
    }}
  >
    📲 {sharing ? 'Generating...' : 'Share as Image'}
  </button>
</div>
```

- [ ] **Step 3: Compile and test frontend build**

Run: `$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"; & "C:\Program Files\JetBrains\IntelliJ IDEA 2026.1.4\plugins\maven\lib\maven3\bin\mvn.cmd" compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add frontend/src/pages/PanchangamPage.jsx
git commit -m "feat: add 1-click Prev and Next date navigation buttons with mobile-first responsiveness"
```

---
