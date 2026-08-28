# Per-Year Anchor Injection — Eliminate Late-Sequence Narrative Drift

**Date:** 2026-08-13
**Problem:** LLM drifts on Lagna Lord identity, house placements, and lordship roles during long generation loops (30-50+ yearly entries). Example: Capricorn Lagna native gets Venus called Lagna Lord in year 2071 instead of Saturn.

## Root Cause

The `houseLordshipTable` and `planetaryMatrix` are injected once at the top of the prompt. By entry ~40-45, these facts are thousands of tokens away from where the LLM is generating narrative, causing attention degradation and confabulation.

## Solution: Per-Year Anchor Injection

For every year in `lifetimePredictions`, pre-compute and inject a `preComputedAnchor` block containing the active Dasa Lord and Bhukthi Lord with full placement context.

### Anchor Schema

```json
{
  "year": 2071,
  "age": 76,
  "preComputedAnchor": {
    "dasaBhukthi": "Saturn - Venus",
    "lagnaLordReminder": "Saturn (Makara Lagna)",
    "dasaLord": {
      "planet": "Saturn",
      "placedInBhava": 2,
      "rulesHouses": [1, 2],
      "isLagnaLord": true,
      "d1Dignity": "OWN_SIGN"
    },
    "bhukthiLord": {
      "planet": "Venus",
      "placedInBhava": 6,
      "rulesHouses": [5, 10],
      "isLagnaLord": false,
      "d1Dignity": "NEUTRAL"
    }
  }
}
```

### Fields

| Field | Source | Purpose |
|-------|--------|---------|
| `dasaBhukthi` | `findDasaForYear()` | Pre-computed dasa-bhukthi string |
| `lagnaLordReminder` | `PlanetDignityUtils.getSignLord(lagnaSign)` | Unambiguous lagna lord identity repeated every year |
| `dasaLord.planet` | Parsed from dasa timeline | Active Mahadasa lord name |
| `dasaLord.placedInBhava` | `planetaryMatrix` lookup | D1 house placement |
| `dasaLord.rulesHouses` | `getRuledHouses()` | Houses this planet owns |
| `dasaLord.isLagnaLord` | `planet == lagnaLord` | Boolean flag |
| `dasaLord.d1Dignity` | `PlanetDignityUtils` | EXALTED / OWN_SIGN / DEBILITATED / NEUTRAL |
| `bhukthiLord.*` | Same as above for bhukthi lord | Same fields for sub-period lord |

### System Instruction Addition

> CRITICAL: For EACH year in lifetimePredictions, a `preComputedAnchor` is provided containing the exact Dasa Lord and Bhukthi Lord with their house placements, ruled houses, Lagna Lord identity, and dignity. You MUST use ONLY these values when describing planetary roles for that year. NEVER override or contradict the anchor data. The `isLagnaLord` flag is definitive — if it says `false`, that planet is NOT the Lagna Lord regardless of any other reasoning.

## Files to Modify

### GeminiPredictionService.java
- `constructAstrologicalPrompt()`: Build `preComputedYearlyAnchors` list and inject into `inputData`.
- `constructSystemInstruction()`: Add anchor usage mandate.
- New helper: `buildYearlyAnchors()`.

### GeminiPredictionServiceTest.java
- Add test: `testPreComputedAnchorsContainLagnaLordAndBhukthiLord`.
- Update smoke test to verify anchor presence in prompt JSON.

## Verification

- Unit tests for anchor generation with known Sagittarius and Capricorn lagna charts.
- Smoke test verifying `preComputedAnchor` appears in serialized JSON prompt.
- Full `mvn test` suite passes.
