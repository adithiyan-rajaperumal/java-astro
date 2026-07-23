# Panchangam UI Reorganization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reorganize `PanchangamPage.jsx` and CSS into a clean, mobile-first responsive 2-column layout with 24 Horai table under Section 2 in laptop/desktop view.

**Architecture:** Split Auspicious and Inauspicious timings into separate dedicated cards, anchor Nakshatra Vara Yogam badges to the Nakshatra card in Section 2, and use CSS media queries to place Horai table under Section 2 on desktop while maintaining a clean single-column flow on mobile.

**Tech Stack:** React, Vanilla CSS, Vite

## Global Constraints
- Target File: `frontend/src/pages/PanchangamPage.jsx` & `frontend/src/index.css`
- Responsive Breakpoint: `768px`

---

### Task 1: Reorganize `PanchangamPage.jsx` Component Hierarchy

**Files:**
- Modify: `d:\Intellij_WS\java-astro\frontend\src\pages\PanchangamPage.jsx`
- Modify: `d:\Intellij_WS\java-astro\frontend\src\index.css`

- [ ] **Step 1: Separate Auspicious and Inauspicious Timing Cards in `PanchangamPage.jsx`**

Create two distinct cards:
1. **Auspicious Timings Card**: Abhijit Muhurtham, Standard Solar Nalla Neram, Gowri Nalla Neram.
2. **Inauspicious Timings Card**: Rahu Kalam, Yamagandam, Gulika Kalam.

- [ ] **Step 2: Position 24 Horai Table under Section 2 in Desktop Column**

Move 24 Horai Table into the Left Column block right below Section 2 for desktop/laptop grid balance.

- [ ] **Step 3: Update CSS Grid rules in `index.css`**

Add CSS rules for `.panchangam-grid-2col`, `.horai-card`, and responsive media queries.

- [ ] **Step 4: Test frontend build**

Run: `npm run build` in `frontend/` directory.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/pages/PanchangamPage.jsx frontend/src/index.css
git commit -m "feat(ui): reorganize Panchangam page into responsive mobile-first 2-column grid"
```
