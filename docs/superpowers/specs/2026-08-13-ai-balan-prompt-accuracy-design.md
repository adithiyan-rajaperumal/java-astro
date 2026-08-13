# AI Balan Prompt Astrological Accuracy & Optimization Specification

## 1. Overview
The AI Balan feature currently provides astrological interpretations via Google Gemini across three modules:
1. **Lifetime Balan** (`generateLifePredictions`)
2. **Daily Balan** (`generateDailyBalan`)
3. **Marriage Matching** (`generateMarriageMatchingAiAnalysis`)

While the core astronomical calculations in the Java backend are comprehensive, the prompt construction in `GeminiPredictionService.java` currently omits key computed astronomical data points (planetary dignities, exact Bhukthis, pre-evaluated Yogas/Doshams, Tarabalam, Gochara house from Moon, and D9 Navamsa in matching). This design bridges those gaps to maximize prediction accuracy and eliminate LLM hallucinations.

---

## 2. Detailed Technical Design

### A. Lifetime Balan Prompt (`constructAstrologicalPrompt`)
1. **Planetary Dignity & State Tagging**:
   - For every planet in D1 chart, use `PlanetDignityUtils` to append dignities:
     - `[Exalted]` (`PlanetDignityUtils.isExalted(planet, signNumber)`)
     - `[Debilitated]` (`PlanetDignityUtils.isDebilitated(planet, signNumber)`)
     - `[Own]` (`PlanetDignityUtils.isOwnSign(planet, signNumber)`)
     - `[Combust]` (`PlanetDignityUtils.isCombust(planet, planetAbsLong, sunAbsLong)`)
   - Format: `Sun:Mesha(House1@15.2°)[Exalted]`, `Venus:Vrishabha(House2@12.4°)[Own]`, `Mercury:Mesha(House1@18.0°)[Combust]`.

2. **Dasa-Bhukthi (Antardasa) Timeline**:
   - Instead of passing only broad Mahadasas, extract and inject the **active and upcoming Dasa-Bhukthi sub-periods** from `c.getCurrentDasaTimeline()`:
   - Format:
     ```
     Active & Upcoming Dasa-Bhukthi Timeline:
     - Jupiter Dasa > Rahu Bhukthi: 2024-02-15 to 2026-07-10
     - Jupiter Dasa > Jupiter Bhukthi: 2026-07-11 to 2028-11-20
     - Saturn Dasa > Saturn Bhukthi: 2028-11-21 to 2031-11-24
     ```

3. **Pre-Computed Yogas & Doshams**:
   - If `c.getStructuralDiagnostics()` is present:
     - Inject active Yogas: `Gajakesari Yoga (Jupiter in Kendra from Moon - High intellect & prosperity)`.
     - Inject evaluated Doshams with nullification status: `Sevvai Dosha: Nullified (Exalted/Friendly house exemption)`.

4. **Year-by-Year Lifespan Forecast**:
   - Retain full year-by-year lifespan prediction covering each year from current age through the Ayurdaya determined lifespan.

---

## 3. Daily Balan Prompt (`constructDailyAstrologicalPrompt`)
1. **Tarabalam (1–9 Daily Star Energy Score)**:
   - Compute Tarabalam from birth Nakshatra to transit Nakshatra:
     `int tara = ((transitNakshatraNumber - birthNakshatraNumber + 27) % 9) + 1;`
   - Map 1..9 to Vedic definitions:
     1. Janma (Body energy / Caution)
     2. Sampat (Wealth & Gain)
     3. Vipat (Obstacles / Danger)
     4. Kshema (Wellbeing & Comfort)
     5. Pratyak (Resistance / Delays)
     6. Sadhana (Achievement & Success)
     7. Naidhana (Severe Affliction / Caution)
     8. Mitra (Friendly / High Harmony)
     9. Parama Mitra (Supreme Benefactor / Highly Favorable)
   - Format: `Tarabalam: Sadhana (6/9 - High Achievement & Favorable)`

2. **Gochara Moon House from Janma Rasi**:
   - Compute transit Moon house relative to native's Janma Rasi:
     `int moonHouseFromRasi = ((todayMoonSign - janmaRasiSign + 12) % 12) + 1;`
   - Format: `Gochara Moon House: House 6 from Janma Rasi (Victory over obstacles, health vitality)`

---

## 4. Marriage Matching Prompt (`constructMatchingPrompt`)
1. **D9 Navamsa Integration**:
   - In addition to D1 Rasi and Bhava, append D9 Navamsa positions for both Boy and Girl:
     - `Boy-D9[Navamsa]: Sun:Leo Moon:Taurus Mars:Capricorn ...`
     - `Girl-D9[Navamsa]: Sun:Aries Moon:Cancer Mars:Scorpio ...`

2. **7th Lord & Karaka Identification**:
   - Highlight the 7th house lord and primary marriage karakas (Boy's Venus placement, Girl's Jupiter/Mars placement).

---

## 5. Files Impacted
1. `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
   - Update `constructAstrologicalPrompt` (Lifetime)
   - Update `constructDailyAstrologicalPrompt` (Daily)
   - Update `constructMatchingPrompt` (Matching)
   - Add helpers: `appendPlanetaryDignities`, `appendDasaBhukthiTimeline`, `calculateTarabalam`, `getMoonGocharaHouse`
2. `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`
   - Add unit tests verifying dignity tags, Tarabalam calculation, Dasa-Bhukthi inclusion, and matching D9 presence.

---

## 6. Verification Plan
- `mvn test` - verify all unit tests pass with zero regressions.
- Validate generated prompts via test assertions to ensure exact formatting and absence of null pointers.
