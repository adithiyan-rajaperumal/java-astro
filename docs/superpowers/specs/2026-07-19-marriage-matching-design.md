# Marriage Matching Design Specification

## 1. Overview
A new subsystem for Vedic Astrology Marriage Matching (Porutham / Kundali Milan), evaluating compatibility based on boy's and girl's birth details.

## 2. Supported Matching Systems
- **Ashta Koota (North Indian)**: 8 parameters out of 36 total points (Varna, Vashya, Tara, Yoni, Graha Maitri, Gana, Bhakut, Nadi).
- **Dasa Porutham (South Indian)**: 10 parameters (Dinam, Ganam, Mahendram, Stree Deergham, Yoni, Rasi, Rajju, Vedha, Vasya, Nadi).

## 3. Analysis Depth
- **Primary**: Nakshatra, Pada, and Rashi-based checks.
- **Chart-Augmented**: D1 planetary positions are also generated for both to do advanced chart analysis (Manglik comparisons, Kala Sarpa warnings, specific planetary afflictions).

## 4. Dosha Nullification & Cancellation Rules
A robust nullification engine evaluates failed parameters against cancellation rules.
- Nullified parameters receive **Full Credit** (score = max points), marked as `MATCHED_VIA_NULLIFICATION` with an explanation.
- **Strictness Level** (STRICT, MODERATE, LENIENT) is provided by the UI to control which rules are applied:
    - `STRICT`: Only universally accepted, scripturally rigorous exceptions.
    - `MODERATE`: Standard traditional exceptions.
    - `LENIENT`: Allows most exceptions and partial overrides (e.g., Stree Deergham minimum counts).

## 5. API Design
**Single Unified Endpoint**: `POST /api/v1/astrology/match?lang=en`
- Accepts one request containing both Boy and Girl `BirthDetailsDTO`, `MatchingType` (ASHTA_KOOTA/DASA_PORUTHAM), and `StrictnessLevel`.
- Computes both charts, derives Nakshatra/Rashi details, applies scoring, runs nullification engine, and returns a unified JSON report.
- Fully localized labels, descriptions, and verdicts for all 6 supported languages.

**PDF Export Endpoint**: `POST /api/v1/astrology/match/pdf`
- Generates a PDF version of the compatibility report, including side-by-side boy/girl D1 & D9 chart grids.

## 6. Architecture & Package Structure (org.vedic.astro.matching)
Follows a Strategy Pattern mirroring the existing `PanchangamEngine`:
- `MatchingEngine` interface with `AshtaKootaEngine` and `DasaPorutthamEngine` implementations.
- `MatchingFactory` provides the implementation dynamically.
- `MatchingContext` computes/caches shared data (Moon positions, lords) to prevent redundant work.
- `NullificationEngine` evaluates individual failed rules against cancellation logic.
