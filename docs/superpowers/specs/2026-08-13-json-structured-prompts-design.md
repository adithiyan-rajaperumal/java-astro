# JSON-Structured Astrological Prompts Specification

## 1. Goal
Transform all prompt data inputs for **Lifetime Balan**, **Daily Balan**, and **Marriage Matching** into 100% clean, unambiguous **JSON data blocks**. This provides deterministic astronomical ground truth to Google Gemini, eliminating modulo arithmetic bugs (such as wrong house lords), dignity hallucinations (such as Mercury in Cancer as own sign), and invalid yoga detections.

---

## 2. Architecture & Data Structures

### A. Lifetime Balan JSON Payload
In `constructAstrologicalPrompt`:
- Build a structured JSON object containing:
  1. `native`: Name, DOB, TOB, Age, Current Year, Lagna, Rasi, Nakshatra, Pada, Panchangam elements.
  2. `houseLordships`: Explicit array of all 12 houses reckoned from Lagna, with `houseNumber`, `signName`, `signNumber`, `lordName`, and `functionalRole` (Lagnesha, Dhana, Sahaya, Sukha, Purva Punya, Roga/Dusthana, Kalathra/Maraka, Ayurdaya/Dusthana, Bhagya/Trikona, Karma/Kendra, Labha/Badhaka, Vyaya/Dusthana).
  3. `planetaryPositionsD1`: Explicit array of all D1 planets with `planet`, `rashi`, `signNumber`, `degreeInSign`, `houseFromLagna`, `dignity` (`EXALTED`, `DEBILITATED`, `OWN_SIGN`, or `NEUTRAL`), and `isCombust`.
  4. `divisionalVargas`: Maps for `d2Hora`, `d9Navamsa`, `d10Dasamsa`, `d12Dwadasamsa`, `d30Trimsamsa`.
  5. `shadbalaStrengths`: Map of planet to `{ "rupas": X.XX, "category": "Strong" }`.
  6. `vimshottariTimeline`: Active and upcoming Mahadasas and Antardasas with exact start/end dates.

### B. Daily Balan JSON Payload
In `constructDailyAstrologicalPrompt`:
- Build a structured JSON object containing:
  1. `targetDate`, `weekday`.
  2. `native`: Name, Janma Lagna, Janma Rasi, Janma Nakshatra, Running Dasa-Bhukthi.
  3. `natalPlanetsAndHouses`: Natal D1 planets, signs, and houses from Lagna.
  4. `todayGocharaAndPanchangam`:
     - `transitMoonSign`: sign number and name.
     - `transitNakshatra`: number and name.
     - `tithi`, `yoga`.
     - `tarabalam`: score (1-9), name, energy description.
     - `transitMoonHouseFromJanmaRasi`: house number (1-12) and classical result meaning.
     - `transitMoonHouseFromJanmaLagna`: house number and significance.
     - `chandrashtama`: boolean.
  5. `fixedDailyAnchors`: Vara Lord, Lucky Color, Lucky Number, Favorable Direction, Auspicious Time Window.

### C. Marriage Matching JSON Payload
In `constructMatchingPrompt`:
- Build a structured JSON object containing:
  1. `groomBoy`: Name, DOB, TOB, Lagna, Rasi, Nakshatra, Pada, `houseLordships`, `d1Planets` (with house and dignity), `d9Navamsa`.
  2. `brideGirl`: Name, DOB, TOB, Lagna, Rasi, Nakshatra, Pada, `houseLordships`, `d1Planets` (with house and dignity), `d9Navamsa`.
  3. `classicalKootaResults`: System, Strictness, Total Score, Max Score, Percentage, Verdict, Koota breakdown (10/12 items), Algorithmic warnings.

---

## 3. System Instruction Clarifications
- System instructions for all 3 modules will explicitly state:
  - *"Read the mathematically exact calculations from the provided JSON matrix. All house numbers, lordships, dignities, and planetary placements in the JSON are pre-calculated ground truths."*
  - *"Calculate and evaluate Yogas, Doshams, and life forecasts by strictly referencing the `houseFromLagna`, `dignity`, and `houseLordships` defined in the input JSON."*

---

## 4. Verification Plan
- Unit tests in `GeminiPredictionServiceTest.java` to verify valid JSON formatting in all prompts.
- Full regression tests (`mvn test`) to ensure 100% test pass rate with zero errors.
