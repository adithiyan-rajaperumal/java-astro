# Specification: Ayurvedic Health Context with Autonomous AI Longevity, Yogas & Doshams

## 1. Context & Motivation
Currently, deterministic calculation engines calculate **Ayurvedic Health (Prakriti, Tattvas, Organ vulnerabilities)**, **Parashara-Jaimini Ayurdaya (Longevity, Kakshya, Lifespan Range)**, and **Structural Diagnostics (Yogas & Doshams)** for the UI dashboard.

In the AI Life Balan generator (`GeminiPredictionService`), embedding `ayurdayaProfile` and `preCalculatedDiagnostics` in the prompt JSON—along with system instruction mandates to "ground readings strictly in pre-calculated profiles"—causes the AI to echo backend-calculated values rather than conducting its own astrological evaluation.

Under this specification, the AI receives the calculated **Ayurvedic Health Profile** for medical astrology context, while `ayurdayaProfile` and `preCalculatedDiagnostics` are **completely excluded** from the AI prompt input JSON. The AI independently evaluates Ayurdaya longevity (classification, ceiling age, and range), Yogas, and Doshams from the raw chart geometry.

---

## 2. Architectural Design

### 2.1 Prompt Input Payload (`constructAstrologicalPrompt`)
1. **Included in JSON Payload:**
   - Native Identity (DOB, TOB, Lagna, Rasi, Nakshatra, Pada, Panchangam elements).
   - 12 House Bhavas, Signs, Lords, and Occupant Roles.
   - Unified Planetary Matrix (D1 physical placement, D9 Navamsa, Dignities, Combustions, Ruled houses).
   - `ayurvedicHealthProfile`: Pre-calculated Ayurvedic Prakriti, Vata/Pitta/Kapha percentages, Lagna element, and organ vulnerabilities.
   - Divisional Charts (D2, D10, D12, D30).
   - Shadbala planet strength scores.
   - Vimshottari Dasa-Bhukthi timeline.
   - `preComputedYearlyAnchors` (Anti-drift Dasa/Bhukthi/Lagna Lord references).
2. **Excluded from JSON Payload:**
   - ❌ `ayurdayaProfile`: Stripped completely.
   - ❌ `preCalculatedDiagnostics`: Stripped completely.

---

### 2.2 System Instruction & Generation Directives (`constructSystemInstruction`)
1. **Remove Anchoring Directives:**
   - Delete all references requiring the model to anchor to pre-calculated Ayurdaya or diagnostic baselines.
2. **Autonomous Calculation Mandates:**
   - **Ayurdaya (Longevity):** Instruct Gemini to evaluate Lagna Lord, 8th Lord, 3rd Lord, Moon, and Saturn (Ayushkaraka), determine longevity bracket (`Poornayu`, `Madhyayu`, `Alpayu`), estimate lifespan ceiling (e.g. 75–95+ years), and generate continuous lifetime predictions up to its calculated ceiling.
   - **Yogas:** Instruct Gemini to identify all classical Vedic Yogas (Gajakesari, Raja, Dhana, Vipareeta Raja, Budhaditya, Neechabhanga, Pancha Mahapurusha, Parivarthana) from planetary relationships.
   - **Doshams:** Instruct Gemini to evaluate Kuja/Sevvai Dosha, Kala Sarpa, Pitru Dosha, and Rahu-Ketu afflictions with classical cancellation analysis.

---

## 3. Verification Plan
- `GeminiPredictionServiceTest.java`: Verify that `constructAstrologicalPrompt` does NOT contain `ayurdayaProfile` or `preCalculatedDiagnostics`, while retaining `ayurvedicHealthProfile`.
- Verify that generation directives mandate autonomous calculation of Ayul, Yogas, and Doshams.
- Run full test suite: `mvn test` (all 55 tests pass).
