# Personal Elements, Deities & Life Anchors Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build and integrate the Personal Elements, Deities & Life Anchors Engine (Numerology, Lucky Days/Dates with Chandrashtama filter, Auspicious Directions, Ishta/Kula/Dharma Devatas, Vedic Gemology with anti-pattern filters, Arudha/Paka Lagna, and SAV Karma Anchors) alongside Ayurvedic Health & Longevity in the Horoscope Dashboard and PDF reports.

**Architecture:** Modular utility classes (`NumerologyUtils`, `SpiritualDeityUtils`, `GemologyEngineUtils`, `StructuralAnchorsUtils`) compute deterministic astrological and mathematical properties into a unified `LifeAnchorsProfile` record. `ChartOrchestrationService` integrates these into `ChartUiResponseDTO` and `ComprehensiveReportDTO`. A rich React view (`LifeAnchorsLongevityView.jsx`) displays these across 5 localized visual groups, and `HoroscopePage.jsx` provides clean, text-only sub-tab navigation without leading icons.

**Tech Stack:** Java 17, Spring Boot 3.3.4, React 18, Vite 8, JUnit 5, OpenPDF / iText.

## Global Constraints

- Preserve all existing Ayurvedic Health and Ayurdaya calculations without regressions.
- Support all 6 languages (`en`, `ta`, `hi`, `te`, `kn`, `ml`) across UI and translations.
- Sub-tab buttons on `HoroscopePage.jsx` must be text-only without leading emoji icons.
- All calculations must be fully deterministic with 100% test coverage.

---

### Task 1: Numerology & Lucky Dates Calculation Engine

**Files:**
- Create: `src/main/java/org/vedic/astro/util/NumerologyUtils.java`
- Test: `src/test/java/org/vedic/astro/NumerologyUtilsTest.java`

**Interfaces:**
- Produces: `NumerologyUtils.calculateNumerology(int day, int month, int year, String lagnaLord)`
- Produces: `NumerologyUtils.calculateLuckyDates(int driverNumber, int moonSign, List<ChartResponseDTO.PositionDetail> transitPlanets)`

- [ ] **Step 1: Write failing unit test for Numerology & Lucky Dates**

```java
package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.util.NumerologyUtils;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class NumerologyUtilsTest {

    @Test
    public void testDigitalRootAndDriverConductor() {
        assertEquals(7, NumerologyUtils.getDigitalRoot(25));
        assertEquals(4, NumerologyUtils.getDigitalRoot(1996 + 7 + 25)); // 2028 -> 12 -> 3 + ...

        var num = NumerologyUtils.calculateNumerology(25, 7, 1996, "Mars");
        assertEquals(7, num.radicalDriverNumber());
        assertEquals("Ketu", num.radicalRulingPlanet());
        assertEquals(9, num.astrologicalPlanetNumber());
        assertTrue(num.friendlyNumbers().contains(1));
        assertTrue(num.enemyNumbers().contains(8));
    }

    @Test
    public void testDriverConductorConflictBridge() {
        // Driver 1 (Sun), Conductor 8 (Saturn) -> Neutral Bridge (5 or 6)
        var num = NumerologyUtils.calculateNumerology(10, 8, 1988, "Sun");
        assertEquals(1, num.radicalDriverNumber());
        assertEquals(8, num.destinyConductorNumber());
        assertNotNull(num.conflictResolutionNotes());
        assertTrue(num.conflictResolutionNotes().contains("5") || num.conflictResolutionNotes().contains("6"));
    }

    @Test
    public void testMonthlyLuckyDatesWithChandrashtama() {
        var dates = NumerologyUtils.calculateLuckyDates(7, 4, null); // Kataka Moon
        assertNotNull(dates.primaryLuckyDates());
        assertTrue(dates.primaryLuckyDates().contains(7));
        assertTrue(dates.primaryLuckyDates().contains(16));
        assertTrue(dates.primaryLuckyDates().contains(25));
        assertTrue(dates.datesToAvoid().contains(8));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=NumerologyUtilsTest`  
Expected: Compilation failure / Test not found.

- [ ] **Step 3: Implement NumerologyUtils**

Create `src/main/java/org/vedic/astro/util/NumerologyUtils.java`:
- Digital root formula: `1 + ((n - 1) % 9)`.
- Driver: `getDigitalRoot(day)`.
- Conductor: `getDigitalRoot(day + month + year)`.
- Planet Numbers: Sun 1, Moon 2, Jupiter 3, Rahu 4, Mercury 5, Venus 6, Ketu 7, Saturn 8, Mars 9.
- Friendship & Enemy matrix.
- Conflict bridge for mutual enemies.
- Monthly Lucky Dates matrix & Chandrashtama detection.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=NumerologyUtilsTest`  
Expected: BUILD SUCCESS (3 tests pass).

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/NumerologyUtils.java src/test/java/org/vedic/astro/NumerologyUtilsTest.java
git commit -m "feat(numerology): implement NumerologyUtils with driver, conductor, and lucky dates matrix"
```

---

### Task 2: Deities & Spiritual Anchors Calculation Engine

**Files:**
- Create: `src/main/java/org/vedic/astro/util/SpiritualDeityUtils.java`
- Test: `src/test/java/org/vedic/astro/SpiritualDeityUtilsTest.java`

**Interfaces:**
- Produces: `SpiritualDeityUtils.calculateSpiritualDeities(Map<String, PlanetaryPosition> d1, List<ChartResponseDTO.PositionDetail> d9Navamsa)`

- [ ] **Step 1: Write failing unit test for SpiritualDeityUtils**

```java
package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.SpiritualDeityUtils;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SpiritualDeityUtilsTest {

    @Test
    public void testAtmakarakaAndIshtaDevataDerivation() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Mars has highest degree in sign (28 deg) -> Atmakaraka
        d1.put("Sun", PlanetaryPosition.builder().planetKey("Sun").signNumber(1).degreeInSign(12.5).build());
        d1.put("Moon", PlanetaryPosition.builder().planetKey("Moon").signNumber(4).degreeInSign(18.2).build());
        d1.put("Mars", PlanetaryPosition.builder().planetKey("Mars").signNumber(8).degreeInSign(28.7).build());
        d1.put("Mercury", PlanetaryPosition.builder().planetKey("Mercury").signNumber(2).degreeInSign(5.1).build());
        d1.put("Jupiter", PlanetaryPosition.builder().planetKey("Jupiter").signNumber(9).degreeInSign(14.3).build());
        d1.put("Venus", PlanetaryPosition.builder().planetKey("Venus").signNumber(3).degreeInSign(22.0).build());
        d1.put("Saturn", PlanetaryPosition.builder().planetKey("Saturn").signNumber(12).degreeInSign(9.4).build());
        d1.put("Lagna", PlanetaryPosition.builder().planetKey("Lagna").signNumber(9).degreeInSign(10.0).build());

        List<ChartResponseDTO.PositionDetail> d9 = List.of(
                ChartResponseDTO.PositionDetail.builder().planetKey("MARS").signNumber(1).build(), // Mars in Mesha in D9 -> Karakamsa = Mesha (1)
                ChartResponseDTO.PositionDetail.builder().planetKey("JUPITER").signNumber(12).build() // 12th from Mesha is Meena (12) -> Jupiter
        );

        var deities = SpiritualDeityUtils.calculateSpiritualDeities(d1, d9);
        assertEquals("Mars", deities.atmakarakaPlanet());
        assertEquals("Mesha", deities.karakamsaSignD9());
        assertNotNull(deities.ishtaDevata());
        assertNotNull(deities.ishtaDevataTamil());
        assertEquals("BLESSED", deities.kulaDevataBlessingStatus());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=SpiritualDeityUtilsTest`  
Expected: Compilation failure.

- [ ] **Step 3: Implement SpiritualDeityUtils**

Create `src/main/java/org/vedic/astro/util/SpiritualDeityUtils.java`:
- Find Atmakaraka (highest degree among 7 classical planets).
- Identify Karakamsa sign in D9.
- Determine 12th house from Karakamsa $\rightarrow$ Ishta Devata (Sun $\rightarrow$ Shiva/Rama, Moon $\rightarrow$ Parvati/Krishna, Mars $\rightarrow$ Murugan/Narasimha, Mercury $\rightarrow$ Vishnu/Venkateshwara, Jupiter $\rightarrow$ Dakshinamurthy, Venus $\rightarrow$ Mahalakshmi, Saturn $\rightarrow$ Hanuman/Shani/Karuppanasamy, Rahu $\rightarrow$ Durga/Varahi, Ketu $\rightarrow$ Ganesha).
- Determine 9th house from Karakamsa $\rightarrow$ Dharma Devata.
- Check 5th house and 5th lord in D1 for malefic afflictions $\rightarrow$ Kula Devata blessing/remedy status.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=SpiritualDeityUtilsTest`  
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/SpiritualDeityUtils.java src/test/java/org/vedic/astro/SpiritualDeityUtilsTest.java
git commit -m "feat(deities): implement SpiritualDeityUtils for Ishta, Kula, and Dharma Devata calculations"
```

---

### Task 3: Vedic Gemology Engine

**Files:**
- Create: `src/main/java/org/vedic/astro/util/GemologyEngineUtils.java`
- Test: `src/test/java/org/vedic/astro/GemologyEngineUtilsTest.java`

**Interfaces:**
- Produces: `GemologyEngineUtils.calculateGemologyRecommendation(int lagnaSign, Map<String, PlanetaryPosition> d1)`

- [ ] **Step 1: Write failing unit test for GemologyEngineUtils**

```java
package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.GemologyEngineUtils;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GemologyEngineUtilsTest {

    @Test
    public void testTrikonaLordGemstoneSelection() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        // Dhanus Lagna (9): 1st Lord Jupiter (in 1st/Dhanus), 5th Lord Mars (in 5th/Mesha), 9th Lord Sun (in 10th/Kanya)
        d1.put("Lagna", PlanetaryPosition.builder().planetKey("Lagna").signNumber(9).build());
        d1.put("Jupiter", PlanetaryPosition.builder().planetKey("Jupiter").signNumber(9).build());
        d1.put("Mars", PlanetaryPosition.builder().planetKey("Mars").signNumber(5).build());
        d1.put("Sun", PlanetaryPosition.builder().planetKey("Sun").signNumber(10).build());

        var gem = GemologyEngineUtils.calculateGemologyRecommendation(9, d1);
        assertNotNull(gem.primaryGemstone());
        assertNotNull(gem.primaryGemstoneTamil());
        assertNotNull(gem.recommendedMetal());
        assertNotNull(gem.recommendedFinger());
        assertNotNull(gem.activationDayAndTiming());
        assertTrue(gem.forbiddenCompanionGems().size() > 0);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=GemologyEngineUtilsTest`  
Expected: Compilation failure.

- [ ] **Step 3: Implement GemologyEngineUtils**

Create `src/main/java/org/vedic/astro/util/GemologyEngineUtils.java`:
- 4 Golden Laws (Trikona 1, 5, 9; Dusthana 6, 8, 12 exclusion; Combustion & Debilitation filter; Dual ownership exceptions).
- Gemstone Matrix (Ruby, Pearl, Red Coral, Emerald, Yellow Sapphire, Diamond, Blue Sapphire, Hessonite, Cat's Eye) with Uparatna, metal, finger, activation time.
- Incompatible Gemstone Anti-Pattern lookup.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=GemologyEngineUtilsTest`  
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/GemologyEngineUtils.java src/test/java/org/vedic/astro/GemologyEngineUtilsTest.java
git commit -m "feat(gemology): implement GemologyEngineUtils with 4 golden laws and anti-pattern matrix"
```

---

### Task 4: Auspicious Directions & Structural Astrological Anchors Engine

**Files:**
- Create: `src/main/java/org/vedic/astro/util/StructuralAnchorsUtils.java`
- Test: `src/test/java/org/vedic/astro/StructuralAnchorsUtilsTest.java`

**Interfaces:**
- Produces: `StructuralAnchorsUtils.calculateStructuralAnchors(int lagnaSign, int moonSign, Map<String, PlanetaryPosition> d1, double julianDay)`

- [ ] **Step 1: Write failing unit test for StructuralAnchorsUtils**

```java
package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.StructuralAnchorsUtils;

import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class StructuralAnchorsUtilsTest {

    @Test
    public void testAuspiciousDirectionsAndArudhaLagna() {
        Map<String, PlanetaryPosition> d1 = new HashMap<>();
        d1.put("Lagna", PlanetaryPosition.builder().planetKey("Lagna").signNumber(9).build()); // Dhanus (Fire -> East)
        d1.put("Jupiter", PlanetaryPosition.builder().planetKey("Jupiter").signNumber(1).build()); // Jupiter in Mesha (5th house from Lagna) -> AL = 9 + 4 = 1 (Mesha) + 4 = Simha (5)
        d1.put("Moon", PlanetaryPosition.builder().planetKey("Moon").signNumber(4).build()); // Kataka (Water -> North)

        var anchors = StructuralAnchorsUtils.calculateStructuralAnchors(9, 4, d1, 2450290.5);
        assertNotNull(anchors.directions().permanentVastuDirection());
        assertNotNull(anchors.structuralAnchors().arudhaLagna());
        assertNotNull(anchors.structuralAnchors().physicalVitalityAnchor());
        assertNotNull(anchors.luckyDay().vedicWeekdayName());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=StructuralAnchorsUtilsTest`  
Expected: Compilation failure.

- [ ] **Step 3: Implement StructuralAnchorsUtils**

Create `src/main/java/org/vedic/astro/util/StructuralAnchorsUtils.java`:
- Calculate Vedic Weekday (Vara) from Julian Day.
- Calculate Auspicious Directions (Zodiac element, Digbala compass, permanent Vastu vs travel).
- Calculate Paka Lagna & Vitality Anchor status.
- Calculate Arudha Lagna (AL) with $1/7$ and $4/10$ forward jump rules.
- Calculate Mind Resilience Anchor (Janma Rashi Dispositor).
- Calculate Sarvashtakavarga (SAV) Karma Anchor.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=StructuralAnchorsUtilsTest`  
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/util/StructuralAnchorsUtils.java src/test/java/org/vedic/astro/StructuralAnchorsUtilsTest.java
git commit -m "feat(anchors): implement StructuralAnchorsUtils for directions, Arudha Lagna, and SAV karma anchors"
```

---

### Task 5: Backend DTO & Orchestration Integration

**Files:**
- Create: `src/main/java/org/vedic/astro/dto/LifeAnchorsProfile.java`
- Modify: `src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java`
- Modify: `src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/ChartOrchestrationService.java`

**Interfaces:**
- Consumes: `NumerologyUtils`, `SpiritualDeityUtils`, `GemologyEngineUtils`, `StructuralAnchorsUtils`
- Produces: `ChartUiResponseDTO.getLifeAnchors()`, `ComprehensiveReportDTO.getLifeAnchors()`

- [ ] **Step 1: Write integration test in `PdfExportServiceTest.java`**

```java
    @Test
    public void testLifeAnchorsProfileIntegration() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");
        var panchangam = panchangamFactory.getEngine(org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = panchangam.calculate(birth);

        ChartUiResponseDTO uiDto = orchestrationService.convertToUiDashboardResponse(chartResult, birth);
        assertNotNull(uiDto.getLifeAnchors());
        assertNotNull(uiDto.getLifeAnchors().numerology());
        assertNotNull(uiDto.getLifeAnchors().deities());
        assertNotNull(uiDto.getLifeAnchors().gemology());
    }
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn test -Dtest=PdfExportServiceTest#testLifeAnchorsProfileIntegration`  
Expected: Compilation failure (`getLifeAnchors` method not found).

- [ ] **Step 3: Create `LifeAnchorsProfile.java` and update DTOs and `ChartOrchestrationService`**

Add `LifeAnchorsProfile lifeAnchors` field to `ChartUiResponseDTO` and `ComprehensiveReportDTO`.  
In `ChartOrchestrationService`:
- Call all 4 engine utilities to assemble `LifeAnchorsProfile`.
- Attach to both response DTOs.

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn test -Dtest=PdfExportServiceTest`  
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/vedic/astro/dto/LifeAnchorsProfile.java src/main/java/org/vedic/astro/dto/ChartUiResponseDTO.java src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java src/main/java/org/vedic/astro/service/ChartOrchestrationService.java src/test/java/org/vedic/astro/PdfExportServiceTest.java
git commit -m "feat(orchestration): assemble LifeAnchorsProfile in ChartOrchestrationService"
```

---

### Task 6: Multi-Language Translations & UI Component

**Files:**
- Modify: `frontend/src/i18n/translations.js`
- Create: `frontend/src/components/LifeAnchorsLongevityView.jsx` (Replaces `HealthLongevityView.jsx`)
- Modify: `frontend/src/pages/HoroscopePage.jsx`

**Interfaces:**
- Consumes: `report.lifeAnchors`, `report.ayurvedicHealth`, `report.ayurdayaProfile`

- [ ] **Step 1: Update `translations.js` for 6 languages**

Add `lifeAnchorsTab` and sub-group translations across `en`, `ta`, `hi`, `te`, `kn`, `ml`.

- [ ] **Step 2: Remove leading icons from all sub-tab buttons in `HoroscopePage.jsx`**

Update `HoroscopePage.jsx` lines 910-955:
- `chartsTab` (text only)
- `dasaTab` (text only)
- `shadbalaTab` (text only)
- `diagnosticsTab` (text only)
- `lifeAnchorsTab` (text only)
- `aiBalanTab` (text only)

- [ ] **Step 3: Build `LifeAnchorsLongevityView.jsx`**

Structure into 5 visual groups:
1. **🕉️ தெய்வங்கள் & ஆன்மீக நங்கூரங்கள் (Spiritual & Deity Anchors)**: Ishta Devata, Kula Devata Status, Dharma Devata.
2. **💎 அதிர்ஷ்ட ரத்தினம் & விதிகள் (Vedic Gemology Engine)**: Recommended Ratnam, Uparatna, Metal, Finger, Day/Time, Incompatible gems list.
3. **🔢 எண்கணிதம் & அதிர்ஷ்ட கூறுகள் (Numerology & Lucky Elements)**: Driver, Conductor, Planet Number, Friendly/Enemy numbers, Monthly Lucky Dates matrix, Auspicious Directions.
4. **🏛️ கட்டமைப்பு நங்கூரங்கள் (Structural Astrological Anchors)**: Paka Lagna, Arudha Lagna (AL), Mind Resilience, SAV Karma Anchor.
5. **🌿 ஆயுர்வேத பிரகிருதி & ஆயுள்தாய நிர்ணயம் (Ayurvedic Health & Longevity)**: Classical Ayurdaya 3-Pair Modality, Kakshya Vriddhi, Prakriti sliders, Organ vulnerabilities, Diet & Lifestyle directives.

- [ ] **Step 4: Build frontend to verify compilation**

Run: `npm run build` in `frontend`  
Expected: `✓ built in ...` with 0 errors.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/i18n/translations.js frontend/src/components/LifeAnchorsLongevityView.jsx frontend/src/pages/HoroscopePage.jsx
git commit -m "feat(ui): create LifeAnchorsLongevityView and clean text-only sub-tab navigation"
```

---

### Task 7: PDF Export Integration & Full Verification

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`
- Modify: `src/test/java/org/vedic/astro/PdfExportServiceTest.java`

- [ ] **Step 1: Update `PdfExportService.java`**

Add section for **Spiritual Anchors, Vedic Gemology & Life Anchors** to the generated PDF with proper font encoding.

- [ ] **Step 2: Run all tests in backend**

Run: `mvn test`  
Expected: All tests pass with `BUILD SUCCESS`.

- [ ] **Step 3: Build frontend in frontend/**

Run: `npm run build`  
Expected: `✓ built` with 0 errors.

- [ ] **Step 4: Commit and Push**

```bash
git add src/main/java/org/vedic/astro/service/PdfExportService.java src/test/java/org/vedic/astro/PdfExportServiceTest.java
git commit -m "feat(pdf): integrate LifeAnchors into PDF export and verify full test suite"
git push origin feature/multi-panchangam-systems
```
