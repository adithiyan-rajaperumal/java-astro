# Nakshatra-Vara Yogam Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement dynamic Nakshatra-Vara Yogam calculation (*Amrita*, *Siddha*, *Marana*, *Prabalarishta*) and render localized interval cards in the UI across all 6 supported languages.

**Architecture:** Add a 27 × 7 lookup matrix in `DailyPanchangamServiceImpl.java`, compute exact transition points between `jdSunrise` and `jdNextSunrise`, deliver `nakshatraYogams` in `PanchangamResponseDTO`, synchronize translations across `messages*.properties` and `translations.js`, and render color-coded cards in `PanchangamPage.jsx`.

**Tech Stack:** Java 17, Spring Boot, React, Vite, i18n

## Global Constraints
- Target Java Version: 17
- Supported Languages: `en`, `ta`, `hi`, `kn`, `te`, `ml`
- UI Styling: Vanilla CSS with glassmorphism design system

---

### Task 1: Backend DTO & Calculation Engine Matrix

**Files:**
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\dto\response\PanchangamResponseDTO.java`
- Modify: `d:\Intellij_WS\java-astro\src\main\java\org\vedic\astro\service\impl\DailyPanchangamServiceImpl.java`

**Interfaces:**
- Produces: `List<TimeSlotDTO> nakshatraYogams` in `PanchangamResponseDTO`

- [ ] **Step 1: Add `nakshatraYogams` field to `PanchangamResponseDTO.java`**

```java
private List<TimeSlotDTO> nakshatraYogams;
```

- [ ] **Step 2: Add 27 × 7 Nakshatra-Vara matrix to `DailyPanchangamServiceImpl.java`**

```java
// 0=Amirdha, 1=Siddha, 2=Marana, 3=Prabalarishta
private static final int[][] NAKSHATRA_VARA_YOGAMS = {
    // Sun (0)
    {2, 3, 1, 1, 1, 0, 1, 1, 2, 0, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1},
    // Mon (1)
    {0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1},
    // Tue (2)
    {0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1, 1, 1},
    // Wed (3)
    {1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1},
    // Thu (4)
    {1, 1, 1, 3, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
    // Fri (5)
    {1, 1, 1, 1, 3, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 0},
    // Sat (6)
    {1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1}
};
```

- [ ] **Step 3: Implement `calculateNakshatraYogams` method in `DailyPanchangamServiceImpl.java`**

```java
private List<TimeSlotDTO> calculateNakshatraYogams(double jdSunrise, double jdNextSunrise, int dayOfWeek, List<PanchangamElementDTO> nakshatraList, ZoneId zoneId) {
    List<TimeSlotDTO> list = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

    if (nakshatraList == null || nakshatraList.isEmpty()) {
        return list;
    }

    for (PanchangamElementDTO nak : nakshatraList) {
        int nakIndex = getNakshatraIndexByName(nak.getName());
        if (nakIndex < 0 || nakIndex >= 27) continue;

        int yogamType = NAKSHATRA_VARA_YOGAMS[dayOfWeek][nakIndex];
        String key = switch (yogamType) {
            case 0 -> "gowri.amirdha_yogam";
            case 1 -> "gowri.siddha_yogam";
            case 2 -> "gowri.marana_yogam";
            default -> "gowri.prabalarishta_yogam";
        };

        ZonedDateTime startZdt = jdToZonedDateTime(nak.getStartJd(), zoneId);
        ZonedDateTime endZdt = jdToZonedDateTime(nak.getEndJd(), zoneId);
        String label = translationService.getLabel(key);

        list.add(new TimeSlotDTO(startZdt.format(formatter), endZdt.format(formatter), label));
    }
    return list;
}
```

- [ ] **Step 4: Populate `nakshatraYogams` in `buildPanchangamResponse`**

```java
response.setNakshatraYogams(calculateNakshatraYogams(jdSunrise, jdNextSunrise, dayOfWeek, nakshatraList, zoneId));
```

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/dto/response/PanchangamResponseDTO.java src/main/java/org/vedic/astro/service/impl/DailyPanchangamServiceImpl.java
git commit -m "feat(panchangam): add Nakshatra-Vara Yogam calculation engine matrix and DTO integration"
```

---

### Task 2: Multi-Language i18n Resource Bundles

**Files:**
- Modify: `src/main/resources/i18n/messages.properties`
- Modify: `src/main/resources/i18n/messages_en.properties`
- Modify: `src/main/resources/i18n/messages_ta.properties`
- Modify: `src/main/resources/i18n/messages_hi.properties`
- Modify: `src/main/resources/i18n/messages_kn.properties`
- Modify: `src/main/resources/i18n/messages_te.properties`
- Modify: `src/main/resources/i18n/messages_ml.properties`
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Add property keys to all `messages*.properties` files**

Add keys:
`gowri.amirdha_yogam`, `gowri.siddha_yogam`, `gowri.marana_yogam`, `gowri.prabalarishta_yogam` across all 6 languages.

- [ ] **Step 2: Add `nakshatraYogam` header to `frontend/src/i18n/translations.js`**

Add `nakshatraYogam` key to all 6 language objects (`en`, `ta`, `hi`, `kn`, `te`, `ml`).

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/i18n/messages*.properties frontend/src/i18n/translations.js
git commit -m "i18n: add synchronized Nakshatra-Vara Yogam keys across all 6 languages"
```

---

### Task 3: Frontend UI Rendering & Verification

**Files:**
- Modify: `frontend/src/pages/PanchangamPage.jsx`

- [ ] **Step 1: Render Nakshatra Yogams Card in `PanchangamPage.jsx`**

Add rendering for `data.nakshatraYogams` with color-coded badges (Green for Amrita/Siddha, Red for Marana/Prabalarishta).

- [ ] **Step 2: Build frontend to verify compilation**

Run: `npm run build` in `frontend/` directory.

- [ ] **Step 3: Commit**

```bash
git add frontend/src/pages/PanchangamPage.jsx
git commit -m "feat(ui): render Nakshatra Yogam timeline card with color-coded badges in PanchangamPage"
```
