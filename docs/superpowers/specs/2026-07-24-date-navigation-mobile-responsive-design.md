# Mobile-First Date Navigation Specification

## Overview
This document specifies the design for adding single-click **Previous Day (`◀`)** and **Next Day (`▶`)** navigation buttons alongside the **Today** button and **Date Picker** on `PanchangamPage`, designed with a strict **Mobile-First Responsive** layout.

## User Intent & Requirements
- **Primary Goal**: Allow 1-click single-touch date navigation (1 day backward or forward) without opening the native date picker modal.
- **Mobile-First Responsiveness**: Ensure all top-bar action buttons (`◀ Prev`, `Today`, Date Picker, `Next ▶`, and `📲 Share as Image`) adapt cleanly to small mobile viewports (320px – 480px) without horizontal scrolling or text clipping.
- **Multilingual Labels**: Fully localized labels using `t()` helper for English, Tamil, Hindi, Kannada, Telugu, and Malayalam.

---

## Technical Approach & Architecture

### 1. Date Navigation Logic
Add a helper `changeDateByDays(deltaDays)` in `PanchangamPage.jsx`:
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
Since `useEffect` listens to `[currentDate, settings.location, settings.language]`, calling `setCurrentDate` immediately triggers single-click API fetching.

### 2. Mobile-First Responsive CSS Layout
- Wrap top bar controls in `.panchangam-top-bar`:
  - `display: flex`
  - `flex-wrap: wrap`
  - `justify-content: center`
  - `gap: 8px`
  - `padding: 10px 12px`
- On mobile viewports (`@media (max-width: 600px)`):
  - Group `[◀ Prev]`, `[Today]`, `[Next ▶]` in a flex row with equal padding.
  - Position Date Picker and `📲 Share as Image` button cleanly below.

---

## Verification Plan
1. Test 1-click `◀ Prev` and `Next ▶` date navigation forward and backward.
2. Verify mobile viewport responsiveness at 360px, 390px, and 414px width in browser developer tools.
3. Test Tamil and English localization.
