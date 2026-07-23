# Design Specification: Responsive Reorganization & Grouping of Panchangam UI

## 1. Overview
Reorganize the **Panchangam Page (`PanchangamPage.jsx`)** into logically grouped, mobile-first responsive sections:
- Grouping core astronomical limbs & Nakshatra Vara Yogams together.
- Grouping Auspicious & Inauspicious timing windows into distinct, focused cards.
- Placing the 24 Horai Table in the Left Column on Desktop/Laptop for visual balance, while keeping it at the bottom on Mobile.

---

## 2. Section Hierarchy & Responsive Layout

```
┌────────────────────────────────────────────────────────────────────────┐
│  📅 Top Date Chooser & Location Bar (Today Button + Date Picker)        │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────────────┐
│ 🌅 SECTION 1: Solar/Lunar Times & Day Status Badges                     │
│    - Sunrise / Sunset / Moonrise / Moonset                             │
│    - Badges: Muhurtham Day | Vasthu Day | Netram (2/2) | Jeevan (1.0)  │
└────────────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────┬───────────────────────────────┐
│ 📜 SECTION 2: Core Panchangam Limbs    │ 🌟 SECTION 3: Auspicious      │
│    & Nakshatra Vara Yogams             │    Solar & Gowri Timings      │
│    - Thithi (Paksha & End Time)        │    - Abhijit Muhurtham         │
│    - Nakshatra + 🌟 Nakshatra Yogams   │    - Standard Solar Nalla Neram│
│      (Amrita/Siddha/Marana Badges)     │    - Gowri Nalla Neram (ℹ️)    │
│    - Yogam & Karanam                   ├───────────────────────────────┤
│    - Rashi & Chandrastamam Caution     │ ⚠️ SECTION 4: Inauspicious    │
│                                        │    Kalam Divisions            │
│                                        │    - Rahu Kalam               │
│                                        │    - Yamagandam               │
│                                        │    - Gulika Kalam             │
│ ────────────────────────────────────── │                               │
│ ⏳ SECTION 5 (Desktop Left Column):     │                               │
│    24 Horai Table (Hourly Divisions)   │                               │
└────────────────────────────────────────┴───────────────────────────────┘
```

---

## 3. Detailed Component Structure

### Left Column (Desktop) / Main Priority Stack (Mobile)
1. **Solar & Day Status Header**: Sunrise, Sunset, Moonrise, Moonset + Badges (Muhurtham Day, Vasthu Day, Netram, Jeevan).
2. **Core Panchangam Limbs Card**:
   - Thithi (Paksha & End Time)
   - Nakshatra & Nakshatra-Vara Yogam Badges (*Amrita*, *Siddha*, *Marana*, *Prabalarishta*)
   - Yogam & Karanam
   - Rashi & Chandrastamam Caution Star List
3. **24 Horai Table** (Rendered in Left Column on Desktop, stacked at bottom on Mobile).

### Right Column (Desktop)
1. **Auspicious Timings Card**:
   - ☀️ Abhijit Muhurtham
   - ☀️ Standard Solar Nalla Neram
   - 🌙 Gowri Nalla Neram (with ℹ️ Guide Modal)
2. **Inauspicious Timings Card**:
   - 🚫 Rahu Kalam
   - ⚡ Yamagandam
   - ⏳ Gulika Kalam

---

## 4. Verification Plan

1. **Build Test**: Run `npm run build` in `frontend/` to verify 0 compilation errors.
2. **Responsive Verification**: Verify layout at `< 768px` (Mobile vertical stack) and `>= 768px` (Desktop 2-column balanced grid).
