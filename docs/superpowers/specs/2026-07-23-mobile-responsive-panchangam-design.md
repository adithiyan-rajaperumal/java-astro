# Design Specification: Mobile-First Responsive Panchangam Page

## 1. Overview
This specification details the design for making the Panchangam page (`PanchangamPage.jsx` & `index.css`) fully **Mobile-First Responsive**, eliminating horizontal scrollbars, text clipping, and rigid pixel bounds across all device viewports (from 320px mobile screens to large desktop monitors).

---

## 2. Requirements & Capabilities

1. **Zero Horizontal Overflow Guarantee**:
   - The root container and card components enforce `max-width: 100%`, `box-sizing: border-box`, and `overflow-x: hidden`.

2. **Fluid & Wrap-Friendly Time Slot Bars (`.time-slot-bar`)**:
   - Removes rigid inline `whiteSpace: 'nowrap'` from time range badges.
   - Replaces fixed `minWidth: '140px'` on label containers with fluid `flex: 1 1 160px` and `min-width: 0`.
   - Allows long time strings (e.g. `01:39 AM (அடுத்த நாள்) - 03:04 AM (அடுத்த நாள்)`) to wrap smoothly when screen space is limited (`overflow-wrap: anywhere`).

3. **Responsive 2×2 Summary Grid**:
   - Sunrise, Sunset, Moonrise, and Moonset summary tiles display as a 2×2 grid (`grid-template-columns: repeat(2, 1fr)`) on mobile (< 600px).
   - Switches to a 4-column single row (`grid-template-columns: repeat(4, 1fr)`) on tablet/desktop (≥ 600px).

4. **Touch-Friendly Horai Table Container**:
   - Table wrapper ensures touch scrolling (`overflow-x: auto`, `-webkit-overflow-scrolling: touch`).

5. **Fluid Top Date Bar**:
   - Date picker and "Today" button flex-wrap gracefully without clipping on small devices.

---

## 3. CSS & Layout Architecture

### `index.css` Additions & Modifications

```css
/* Panchangam Mobile First Adjustments */
.panchangam-grid-2col {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

@media (min-width: 992px) {
  .panchangam-grid-2col {
    grid-template-columns: 1.1fr 1fr;
    gap: 20px;
  }
}

.panchangam-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}

@media (min-width: 600px) {
  .panchangam-summary-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.time-slot-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px 10px;
  padding: 8px 12px;
  margin-bottom: 6px;
  width: 100%;
  box-sizing: border-box;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.slot-time-badge {
  font-weight: bold;
  margin-left: auto;
  align-self: center;
  text-align: right;
  overflow-wrap: anywhere;
  word-break: break-word;
}
```

---

## 4. Verification Plan

1. **Build Verification**: Run `npm run build` in `frontend/` to ensure zero compilation errors.
2. **Viewport Testing**: Verify rendering at 320px (iPhone SE), 375px (iPhone 12/13), 768px (iPad portrait), and 1200px desktop.
