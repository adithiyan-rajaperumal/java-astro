# Unified Master Yogas, Doshams, & Engine Laws Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement 100% deterministic mathematical accuracy across all 26 Classical Yogas, 15 Classical Doshams with complete nullifications, exact per-planet combustion degree orbs, and Vakra dignity overrides per BPHS.

**Architecture:** Enhance `PlanetDignityUtils` with exact degree orbs and retrograde overrides; expand `AstrologyDiagnosticsService` into dedicated modular evaluators for all 26 Yogas and 15 Doshams; update i18n bundles; verify with unit tests.

**Tech Stack:** Java 17, Spring Boot, Swiss Ephemeris, JUnit 5, Maven.

## Global Constraints
- Pure mathematical compliance with BPHS and Jaimini classical rules.
- Exact combustion orbs: Moon $\le 12^\circ$, Mars $\le 17^\circ$, Mercury $\le 14^\circ/12^\circ$ retro, Jupiter $\le 11^\circ$, Venus $\le 10^\circ/8^\circ$ retro, Saturn $\le 15^\circ$.
- Zero false-positive Yogas (strictly enforce Kendra from Moon vs. Lagna, exact same sign for Budhaditya, non-combust Mahapurusha).
- Zero uncancelled Doshams (proper Nivritti checks for Sevvai, Kalasarpa, Pitru, Putra, Kalathra, Shani, Guru-Chandala, Angarak, Punarphoo, Papakarthari, Grahan, Daridra, Duryoga, Sarpa Dosha).

---

### Task 1: Enhance `PlanetDignityUtils` with Exact Combustion Orbs & Vakra Overrides

**Files:**
- Modify: `src/main/java/org/vedic/astro/util/PlanetDignityUtils.java`
- Test: `src/test/java/org/vedic/astro/PlanetDignityUtilsTest.java`

**Interfaces:**
- Produces:
  - `PlanetDignityUtils.isCombust(String planet, double planetAbsLong, double sunAbsLong, boolean isRetrograde)`
  - `PlanetDignityUtils.getEffectiveDignity(String planet, int sign, boolean isRetrograde)`
  - `PlanetDignityUtils.isKendra(int house)`
  - `PlanetDignityUtils.isTrikona(int house)`
  - `PlanetDignityUtils.isUpachaya(int house)`
  - `PlanetDignityUtils.isDusthana(int house)`

- [ ] **Step 1: Write the failing unit tests in `PlanetDignityUtilsTest.java`**
- [ ] **Step 2: Run test to verify it fails (`mvn test -Dtest=PlanetDignityUtilsTest`)**
- [ ] **Step 3: Implement exact combustion orbs and dignity helpers in `PlanetDignityUtils.java`**
- [ ] **Step 4: Run test to verify it passes (`mvn test -Dtest=PlanetDignityUtilsTest`)**
- [ ] **Step 5: Commit changes**

---

### Task 2: Implement 26 Major Classical Yogas in `AstrologyDiagnosticsService`

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java`
- Modify: `src/main/resources/i18n/messages_en.properties`
- Modify: `src/main/resources/i18n/messages_ta.properties`
- Modify: `src/main/resources/i18n/messages.properties`
- Test: `src/test/java/org/vedic/astro/AstrologyDiagnosticsServiceTest.java`

**Interfaces:**
- Produces:
  - `evaluateYogas(Map<String, PlanetaryPosition> d1Map, List<DiagnosticsDTO.YogaDetail> yogas)` covering:
    - Pancha Mahapurusha (Ruchaka, Bhadra, Hamsa, Malavya, Sasa + combustion check)
    - Major Raja/Dhana: Dharma-Karmadhipati, Budhaditya, Gajakesari, Chandra-Mangala, Lakshmi, Bhagyalakshmi, Rajalakshmi, Amala, Adhi (Chandradi/Lagnadhi), Vasumathi, Akhanda Samrajya, Saraswati, Kalanidhi, Kahala, Parvata, Pushkala, Shakata & Shakata Bhanga, Shubhakarthari, Mahabhagya (Day/Night & Male/Female), General Kendra-Trikona, Chamara, Pravrajya, Parivartana (Maha/Khala/Dainya)
    - Solar (Vesi, Vosi, Obhayachari) & Lunar (Sunapha, Anapha, Dhurudhura, Kemadruma & Kemadruma Bhanga)
    - Vipareeta Raja Yogas (Harsha, Sarala, Vimala) with Lagna Lord Exclusion
    - 5-Law Neechabhanga Raja Yoga

- [ ] **Step 1: Write unit tests for all 26 Yogas in `AstrologyDiagnosticsServiceTest.java`**
- [ ] **Step 2: Implement all 26 Yogas in `AstrologyDiagnosticsService.java`**
- [ ] **Step 3: Add all i18n labels in `messages*.properties`**
- [ ] **Step 4: Run tests and verify they pass (`mvn test -Dtest=AstrologyDiagnosticsServiceTest`)**
- [ ] **Step 5: Commit changes**

---

### Task 3: Implement All 15 Classical Doshams & Nivritti Rules

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java`
- Modify: `src/main/resources/i18n/messages*.properties`
- Test: `src/test/java/org/vedic/astro/AstrologyDiagnosticsServiceTest.java`

**Interfaces:**
- Produces:
  - 15 Dosha evaluators:
    1. `evaluateSevvaiDosham` (Triple frame: Lagna, Moon, Venus + Cancer/Leo Yogakaraka exemption + 11th Upachaya + house-sign pairs)
    2. `evaluateKalaSarpaDosham` (degree-level enclosure + Jupiter aspect)
    3. `evaluateSarpamDosham` (1, 2, 5, 7, 8 with 3/6/11 Upachaya exemptions)
    4. `evaluatePithruDosham` (9th/Sun with Jupiter aspect & exaltation exemptions)
    5. `evaluatePutraDosham` (5th with Jupiter aspect & lord dignity exemptions)
    6. `evaluateKalathiraDosham` (7th with Venus/Jupiter protections)
    7. `evaluateShaniDosham` (7th/8th with Sasa Yoga & Yogakaraka exemptions)
    8. `evaluateGuruChandalaDosham` (Jupiter-Nodes with 5th/9th Gyan Yoga conversion)
    9. `evaluateAngarakDosham` (Mars-Nodes with 3/6/11 Shatru Jaya conversion)
    10. `evaluatePunarphooDosham` (Saturn-Moon with Purnima/Jupiter cancellation)
    11. `evaluatePapakarthariDosham` (Malefics in 2nd and 12th from Lagna, Moon, or 10th)
    12. `evaluateGrahanDosham` (Sun/Moon conjunct Rahu/Ketu within $12^\circ$)
    13. `evaluateDaridraYoga` (11th lord in 6/8/12 without VRY)
    14. `evaluateDuryoga` (10th lord in 6/8/12 without VRY)
    15. `evaluateSarpaDosha` (Malefics in 3-4 Kendras, no benefics)

- [ ] **Step 1: Write unit tests for all 15 Doshams and nullification edge cases in `AstrologyDiagnosticsServiceTest.java`**
- [ ] **Step 2: Implement all 15 Dosha evaluators in `AstrologyDiagnosticsService.java`**
- [ ] **Step 3: Add remedy and description keys in `messages*.properties`**
- [ ] **Step 4: Run tests and verify they pass (`mvn test -Dtest=AstrologyDiagnosticsServiceTest`)**
- [ ] **Step 5: Commit changes**

---

### Task 4: Full Regression Verification & Build Validation

**Files:**
- Test: All test classes in `src/test/java/org/vedic/astro/`
- Frontend: `frontend/`

- [ ] **Step 1: Run full Maven test suite (`mvn test`)**
- [ ] **Step 2: Validate frontend build (`npm run build` in `frontend/`)**
- [ ] **Step 3: Commit and push to `origin/feature/multi-panchangam-systems`**
