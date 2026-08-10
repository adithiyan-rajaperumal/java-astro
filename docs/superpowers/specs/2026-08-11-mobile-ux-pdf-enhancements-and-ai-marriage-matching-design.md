# Design Specification: Mobile UX, PDF Enhancements, and AI-Based Marriage Matching

**Document Version:** 1.0.0  
**Date:** 2026-08-11  
**Status:** PROPOSED (Ready for Review)

---

## 1. Overview & Objectives

This specification defines the comprehensive architecture and UX refinements across 6 critical areas of the astrology platform:
1. **Dasa-Bhukthi Mobile-First Responsive Layout**: Adaptive cards and touch-optimized responsive views for mobile viewports.
2. **Download PDF Progress Indicator**: Interactive loading state with progress feedback during PDF generation on Horoscope & Matching pages.
3. **AI Life Balan Deep 4-Pillar Narrative**: Enforce an exhaustive 4-pillar narrative for each year across the native's full lifespan.
4. **Horoscope Subtabs Mobile Scrolling & Intuitive Order**: Smooth horizontal scrollability with edge indicators and reordered subtabs (`Charts -> Daily Balan -> Dasa -> Shadbala -> Diagnostics -> AI Life Balan`).
5. **PDF Report Birth Header**: Replace raw latitude/longitude coordinates with the human-readable `Place of Birth` (e.g. `Chennai, Tamil Nadu, India`).
6. **AI-Based Marriage Matching**: Full AI marriage compatibility analysis supporting both Ashta Koota (36 Gunas) and Dasa Porutham (10 Poruthams), with 3-hour caching, multilingual rendering, nullification logic, and PDF inclusion.

---

## 2. Detailed Technical Design

### Section 1: Dasa-Bhukthi Mobile-First Layout
- **Current Issue**: Fixed table columns (`Bhukthi | From | To`) cause horizontal overflow or cramped text on narrow mobile screens (< 640px).
- **Solution**:
  - Implement mobile-first CSS with `@media (max-width: 640px)` where each Bhukthi is rendered as a responsive compact card/row:
    - Left side: Planet name with icon (and `⭐ Current` badge if active).
    - Right side: Clean date range badge (`YYYY-MM-DD ➔ YYYY-MM-DD`).
  - Active Mahadasa and Bhukthi will retain gold glow, bold styling, and high contrast.

### Section 2: Download PDF Loading State & Progress Feedback
- **Frontend State**:
  - Add `const [pdfLoading, setPdfLoading] = useState(false)` to `HoroscopePage.jsx` and `MatchingPage.jsx`.
  - When the user clicks `Download PDF`, `pdfLoading` becomes `true`.
  - Button UI updates:
    - Text: `⏳ Generating PDF...` (or localized `⏳ PDF உருவாக்கப்படுகிறது...`).
    - Cursor: `wait`, button disabled to prevent duplicate submissions.
    - Spinner animation next to text.
  - Reset `pdfLoading` in `finally` block when the blob download triggers or an error occurs.

### Section 3: AI Balan 4-Pillar Deep Multi-Dimensional Synthesis
- **Prompt Directive #6 Refinement in `GeminiPredictionService.java`**:
  - For each year in `lifetimePredictions`, mandate a deep, structured narrative paragraph (150–250 words per year) covering all 4 core pillars:
    1. **💼 Career, Business & Financial Growth**: Job shifts, promotions, salary/business gains, property/land acquisitions, financial investments.
    2. **🌿 Health, Vitality & Organ Realities**: Vitality levels, organ vulnerabilities, surgical or hospitalization alerts.
    3. **👨‍👩‍👦 Family, Marriage & Progeny**: Domestic harmony, spouse milestones, children's education/birth.
    4. **👴 Parents, Elders & Mindset**: Parental wellbeing, elder transitions, emotional resilience with unbroken lifespan continuity.
  - Mandate that Gemini must not omit any pillar or provide generic one-liners.

### Section 4: Horoscope Results Subtabs: Mobile Scrolling & Re-ordering
- **Subtab Styling in `HoroscopePage.jsx`**:
  - Container style: `display: flex; overflow-x: auto; white-space: nowrap; scroll-snap-type: x mandatory; -webkit-overflow-scrolling: touch; gap: 8px; padding-bottom: 4px; mask-image: linear-gradient(to right, black 90%, transparent 100%);`
- **Updated Subtab Ordering**:
  1. `📊 Charts (ஜாதக கட்டங்கள்)`
  2. `📅 Daily Balan (இன்றைய பலன்)` — *Repositioned immediately after Charts for fast daily access*
  3. `⏳ Dasa-Bhukthi (திசா புக்தி)`
  4. `⚖️ Shadbala (கிரக பலம்)`
  5. `🛡️ Diagnostics (யோகம் & தோஷம்)`
  6. `🔮 AI Life Balan (AI வாழ்நாள் பலன்)`

### Section 5: PDF Report Birth Header: Place of Birth
- **DTO Update**:
  - Add `private String placeOfBirth;` to `ComprehensiveReportDTO.java`.
  - In `DrikPanchangamEngine.java` (and all engines), populate `placeOfBirth` from `payload.location()` / `payload.name()`.
- **PDF Layout Update in `PdfExportService.java`**:
  - Replace lines 109-112:
    - Instead of `Latitude` and `Longitude`, output `ts.getLabel("pdf.info.place_of_birth")` (or `"Place of Birth"`) with `data.getPlaceOfBirth()`.
    - Fallback to city/state string from location payload.

### Section 6: AI-Based Marriage Matching (Engine, Subtabs, 3-Hour Cache, & PDF)
- **Configuration**:
  - `application.yml`:
    ```yaml
    gemini:
      matching-enabled: ${GEMINI_MATCHING_ENABLED:true}
    ```
  - `GeminiProperties.java`: `private boolean matchingEnabled = true;`

- **DTO Model (`MatchingAiPredictionDTO.java`)**:
  ```java
  package org.vedic.astro.matching.dto;
  
  import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
  import lombok.AllArgsConstructor;
  import lombok.Builder;
  import lombok.Data;
  import lombok.NoArgsConstructor;
  import org.vedic.astro.dto.PredictionResponseDTO.TokenUsage;
  import java.util.List;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  public class MatchingAiPredictionDTO {
      private boolean enabled;
      private String message;
      private String overallVerdict; // EXCELLENT, VERY_GOOD, GOOD, AVERAGE, NOT_RECOMMENDED
      private double compatibilityPercentage; // 0 - 100
      private String executiveSummary;
      private TokenUsage tokenUsage;
      
      // Compatibility Domain Breakdowns
      private DomainAnalysis emotionalMentalHarmony;
      private DomainAnalysis healthLongevityNadi;
      private DomainAnalysis careerFinancialSynergy;
      private DomainAnalysis progenyFamilyLineage;
      private DomainAnalysis doshaPapasamyaParity;
      
      private List<String> keyStrengths;
      private List<String> growthAreasAndCautions;
      private List<String> authenticVedicRemedies;

      @Data
      @Builder
      @NoArgsConstructor
      @AllArgsConstructor
      @JsonIgnoreProperties(ignoreUnknown = true)
      public static class DomainAnalysis {
          private String title;
          private String scoreOrStatus; // e.g. "90% (High Harmony)"
          private String analysis;
          private String astrologicalBasis;
      }
  }
  ```

- **Backend AI Matching Pipeline (`GeminiPredictionService.java`)**:
  - Endpoint / Service Method:
    `public MatchingAiPredictionDTO generateMarriageMatchingAiAnalysis(MatchingRequestDTO req, MatchingResponseDTO classicalResult, String lang)`
  - **Cache Key & TTL**:
    - Cache key: `MATCH_AI:v1:{boyHash}:{girlHash}:{matchingSystem}:{strictness}:{lang}`
    - TTL: **3 Hours** (`3 * 3600 * 1000L`).
  - **Prompt Synthesis**:
    - Ingests:
      - Boy & Girl birth details, Rasi, Nakshatra, Pada, Lagna, D1 Planetary placements, D9 Navamsa positions, Shadbala strengths.
      - Classical matching score & koota breakdown (Ashta Koota 36 Gunas or Dasa Porutham 10 Poruthams).
      - Kuja Dosha presence & nullification flags, Papasamya scores, Dasa Sandhi warnings.
    - Instructs Gemini to provide authentic Vedic synthesis in the user's selected language (`ta`, `hi`, `kn`, `te`, `ml`, `en`), applying all classical nullifications (e.g. Kuja Dosha cancellation in 2nd/4th/7th/8th/12th when specific signs or aspects match, Rajju cancellation exceptions).

- **Frontend Subtabs on Matching Results (`MatchingPage.jsx`)**:
  - Render subtab switcher at top of results:
    - `Tab 1: 📊 Classical Matching (சாஸ்திர பொருத்தம்)`
    - `Tab 2: 🔮 AI Marriage Compatibility (AI திருமணப் பொருத்தம்)`
  - Under `Tab 2`:
    - If not generated: Display a clean callout with a prominent `✨ Generate AI Marriage Compatibility Report` button.
    - While generating: Display animated loading spinner and message.
    - When generated: Render:
      - Token usage & cost pill badge.
      - Overall verdict badge with compatibility percentage.
      - Executive summary paragraph.
      - 5 domain cards (Emotional Harmony, Health & Nadi, Financial Synergy, Progeny, Dosha/Papasamya Balance).
      - Strengths, Cautions & Authentic Vedic Pariharams.

- **PDF Export Integration (`PdfExportService.java`)**:
  - Update `generateMarriageMatchingReport(MatchingResponseDTO data)` to check if `data.getAiMatchingPrediction()` is present.
  - If present and enabled, append a dedicated "AI Marriage Compatibility Analysis" section with overall verdict, domain scores, and remedies to the exported PDF report.

---

## 3. Verification & Testing Strategy

1. **Automated Unit & Integration Tests**:
   - `GeminiMatchingPredictionTest.java`: Verify AI matching prompt generation, JSON schema deserialization, and cache lookup with 3-hour TTL.
   - `MatchingControllerTest.java`: Verify REST endpoint `/api/v1/astrology/match/ai` with mock Gemini response.
   - `PdfExportServiceTest.java`: Verify PDF generation containing both Classical Matching and AI Marriage Matching.
   - `HoroscopeChartCalculationTest.java`: Verify `placeOfBirth` propagation in `ComprehensiveReportDTO`.
2. **Frontend UI Verification**:
   - Verify mobile responsiveness of Dasa-Bhukthi table in responsive viewport (< 640px).
   - Verify `Download PDF` button progress state on Horoscope and Matching pages.
   - Verify horizontal scrolling and reordered subtabs on mobile.
   - Verify AI Marriage Matching subtab toggle, generation button, and rendering.
   - Run `npm run build` and `mvn clean test`.

---
