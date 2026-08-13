# Ayurvedic Prakriti & Unified Planetary Matrix Specification

## 1. Overview
This specification addresses 3 critical areas of prompt accuracy in AI Balan:
1. **Placement vs. House Ownership Disambiguation**: Explicit separation of `rulesHouses` vs `placedInD1House` and `houseLord` vs `occupantPlanets`.
2. **Deterministic Ayurvedic Tridosha & Health Engine**: Implementation of Parashara + Charaka Samhita classical scoring logic to generate authentic native Prakriti and organ vulnerability profiles.
3. **D1 vs. D9 Disambiguation**: Merging D1 physical placement and D9 Navamsa positions into a single unified `planetaryMatrix` object to prevent sign swaps in yearly predictions.

---

## 2. Ayurvedic Dosha & Health Engine (Parashara / Charaka Samhita)

### A. Planetary Dosha Mappings
- **Sun**: Pitta (Fire) — Bone density, heart, vital heat, bile
- **Moon**: Kapha & Vata (Water/Air) — Blood, bodily fluids, lymph, mental balance
- **Mars**: Pitta (Fire) — Blood, muscles, bone marrow, inflammatory response
- **Mercury**: Tridosha (Adaptable) — Skin, nervous system, speech, transport channels
- **Jupiter**: Kapha (Water/Ether) — Fat, liver, brain tissue, expansion/fluid retention
- **Venus**: Kapha & Vata (Water/Air) — Reproductive fluids, kidneys, glandular system
- **Saturn**: Vata (Air/Dryness) — Nerves, joints, tendons, chronic dryness, decay
- **Rahu**: Vata (Amplified Air) — Nervous tremors, illusions, toxicities, allergies
- **Ketu**: Pitta (Internal Fire) — Sharp fevers, surgical conditions, skin breakouts

### B. Rashi Element (Tattva) Mappings
- **Agni (Fire)**: Mesha (1), Simha (5), Dhanus (9) -> **Pitta**
- **Prithvi (Earth)**: Vrishabha (2), Kanya (6), Makara (10) -> **Vata-Kapha** (Dryness + Structure)
- **Vayu (Air)**: Mithuna (3), Thula (7), Kumbha (11) -> **Vata**
- **Jala (Water)**: Kataka (4), Vrishchika (8), Meena (12) -> **Kapha**

### C. Scoring Algorithm
1. **Lagna Sign Tattva** (Weight: 3)
2. **Lagna Lord Planetary Dosha** (Weight: 3)
3. **Moon Sign Tattva** (Weight: 2)
4. **Moon Planetary Dosha** (Weight: 1)
5. **Sun Sign Tattva** (Weight: 1)
6. **6th House Sign (Roga Sthana) & 6th Lord** (Weight: 2)
7. Normalize into percentage breakdown (e.g. `Pitta: 45%, Vata: 35%, Kapha: 20%`) and categorize primary/secondary Prakriti (e.g. `Pitta-Vata`).

---

## 3. Data Structures

### A. Unified `planetaryMatrix`
```json
{
  "planet": "Sun",
  "placedInD1Sign": "Kanya",
  "placedInD1House": 10,
  "occupantRole": "Occupant/Guest in House 10 (NOT 10th lord)",
  "placedInD9NavamsaSign": "Mesha",
  "isVargottama": false,
  "rulesHouses": [11],
  "lordshipTitle": "11th Lord (Rules Simha/Leo)",
  "d1Dignity": "NEUTRAL",
  "d9Dignity": "EXALTED",
  "isCombust": false,
  "primaryDosha": "Pitta (Bone density, heart, vital heat, bile)"
}
```

### B. Enhanced `houseLordshipTable`
```json
{
  "houseNumber": 10,
  "signName": "Kanya",
  "signNumber": 6,
  "houseLord": "Mercury",
  "significance": "Karma & Rajya / Career, Profession, Status (Kendra)",
  "occupantPlanets": ["Sun"],
  "lordshipClarification": "Mercury is the SOLE lord of House 10. Sun is an occupant/guest in House 10."
}
```

### C. `ayurvedicHealthProfile`
```json
{
  "dominantPrakriti": "Pitta-Vata",
  "doshaPercentages": { "pitta": 45, "vata": 35, "kapha": 20 },
  "lagnaElement": "Agni (Fire)",
  "rogaSthanaSign": "Vrishabha (Earth / House 6)",
  "rogaLord": "Venus (Kapha-Vata)",
  "calculatedOrganVulnerabilities": [
    "Heart, bone density & vital heat (Sun placement in 10th / 11th lordship)",
    "Digestive metabolic balance & bile regulation (Pitta dominant constitution)"
  ],
  "dietaryAndLifestyleDirectives": [
    "Favor cooling, grounding, fresh whole foods; moderate bitter & sweet tastes",
    "Avoid excess pungent spices, late dinners, and irregular sleep during malefic Bhukthi periods"
  ]
}
```

---

## 4. System Instruction Refinements
- Explicitly instruct the model to:
  1. Distinguish between `rulesHouses` (ownership) and `placedInD1House` (occupancy).
  2. Use D1 for material events and D9 for spiritual/inner strength without mixing their zodiac signs.
  3. Ground all health, longevity, and Ayurvedic predictions in the pre-calculated `ayurvedicHealthProfile`.
