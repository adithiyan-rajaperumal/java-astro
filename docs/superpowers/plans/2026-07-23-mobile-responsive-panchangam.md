# Mobile-First Responsive Panchangam Page Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Transform the Panchangam page (`PanchangamPage.jsx` & `index.css`) into a fully Mobile-First responsive dashboard that guarantees zero horizontal overflow on small device screens (320px+).

**Architecture:** Add responsive CSS classes (`panchangam-summary-grid`, `slot-time-badge`) to `index.css` and refactor inline rigid styles (`whiteSpace: 'nowrap'`, fixed `minWidth`) in `PanchangamPage.jsx` to fluid flex/grid layouts.

**Tech Stack:** React, JSX, Vanilla CSS

## Global Constraints
- Target Viewports: Mobile 320px+ (iPhone SE, Android small screens), Tablet 768px+, Desktop 1200px+
- Overflow Constraint: `max-width: 100%`, `box-sizing: border-box`, zero horizontal scrollbars

---

### Task 1: Mobile-First Responsive CSS & Component Styling

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\index.css:450-580`
- Modify: `d:\Intellij_WS\java-astro\frontend\src\pages\PanchangamPage.jsx:90-420`

**Interfaces:**
- Consumes: `index.css` classes (`panchangam-summary-grid`, `slot-time-badge`)
- Produces: Responsive flex/grid rendering in `PanchangamPage.jsx`

- [ ] **Step 1: Update `index.css` with responsive Panchangam layout rules**

Add CSS rules for `.panchangam-summary-grid` (2 columns on mobile, 4 columns on desktop ≥600px), `.slot-time-badge`, and container overflow protection.

- [ ] **Step 2: Refactor `PanchangamPage.jsx` inline styles**

- Update Sunrise/Sunset grid container `className="panchangam-summary-grid"`.
- Update `renderSlotLabelContent` flex style from `minWidth: '140px'` to `flex: '1 1 160px', minWidth: 0`.
- Update time range spans in `renderTimeSlotList` and `renderNakshatraYogamsList` to use `className="slot-time-badge"`.

- [ ] **Step 3: Run `npm run build` in `frontend/` to verify clean compilation**

- [ ] **Step 4: Commit changes**

```bash
git add frontend/src/index.css frontend/src/pages/PanchangamPage.jsx docs/superpowers/plans/2026-07-23-mobile-responsive-panchangam.md
git commit -m "feat(ui): make Panchangam page mobile-first responsive with zero overflow"
```
