# Implementation Plan: Mobile UX, PDF Enhancements, and AI-Based Marriage Matching

Implement mobile-first responsive layouts, interactive PDF progress feedback, Place of Birth in PDF headers, AI Balan 4-pillar narrative depth, subtab reordering/scrolling, and AI-based marriage matching with 3-hour caching and PDF integration.

## User Review Required

> [!IMPORTANT]
> 1. **Subtab Order in Horoscope Results**: Updated to `1. Charts` ➔ `2. Daily Balan` ➔ `3. Dasa-Bhukthi` ➔ `4. Shadbala` ➔ `5. Diagnostics` ➔ `6. AI Life Balan`.
> 2. **AI Marriage Matching Caching**: Cached for **3 hours** per unique pair + system + language combination.
> 3. **PDF Export**: Both Horoscope and Marriage Matching PDFs will now feature the updated human-readable Place of Birth and AI sections (when enabled and generated).

---

## Proposed Changes

### Component 1: Internationalization (`frontend/src/i18n/translations.js`)
- Add synchronized keys for matching subtabs, AI matching domain titles, PDF progress, and Place of Birth across all 6 languages (`en`, `ta`, `hi`, `kn`, `te`, `ml`).

### Component 2: Frontend Layouts & Mobile Responsiveness
#### [MODIFY] [HoroscopePage.jsx](file:///d:/Intellij_WS/java-astro/frontend/src/pages/HoroscopePage.jsx)
- Update Dasa-Bhukthi table with mobile-first responsive CSS / card view.
- Add `pdfLoading` state to the Download PDF button.
- Re-order subtabs (`Charts -> Daily Balan -> Dasa -> Shadbala -> Diagnostics -> AI Life Balan`).
- Add touch-friendly horizontal scroll and snap to subtabs container.

#### [MODIFY] [MatchingPage.jsx](file:///d:/Intellij_WS/java-astro/frontend/src/pages/MatchingPage.jsx)
- Add subtabs for `Classical Matching` vs `AI Marriage Compatibility`.
- Add `pdfLoading` state to the Download PDF button.
- Integrate AI Marriage Matching generation button, loading spinner, token usage badge, and domain compatibility cards.

#### [NEW] [AiMatchingView.jsx](file:///d:/Intellij_WS/java-astro/frontend/src/components/AiMatchingView.jsx)
- Specialized component rendering the 5 domain compatibility breakdowns, executive summary, strengths, cautions, and authentic remedies.

### Component 3: Backend Prediction Engine & Config
#### [MODIFY] [application.yml](file:///d:/Intellij_WS/java-astro/src/main/resources/application.yml)
- Add `gemini.matching-enabled: ${GEMINI_MATCHING_ENABLED:true}`.

#### [MODIFY] [GeminiProperties.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/config/GeminiProperties.java)
- Add `private boolean matchingEnabled = true;` with getter/setter.

#### [NEW] [MatchingAiPredictionDTO.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/matching/dto/MatchingAiPredictionDTO.java)
- DTO model for AI marriage compatibility analysis.

#### [MODIFY] [MatchingResponseDTO.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/matching/dto/MatchingResponseDTO.java)
- Add `private MatchingAiPredictionDTO aiMatchingPrediction;`.

#### [MODIFY] [GeminiPredictionService.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/service/GeminiPredictionService.java)
- Upgrade directive #6 in horoscope prompt for 4-pillar narrative depth (Career/Wealth, Health/Vitality, Family/Marriage, Parents/Mindset).
- Implement `generateMarriageMatchingAiAnalysis(MatchingRequestDTO req, MatchingResponseDTO classicalResult, String lang)` with 3-hour cache TTL.

#### [MODIFY] [MatchingController.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/controller/MatchingController.java)
- Add POST `/api/v1/astrology/match/ai` endpoint to trigger or retrieve cached AI marriage compatibility analysis.
- Include AI prediction in PDF download endpoint `/api/v1/astrology/match/download-pdf`.

### Component 4: PDF Export Service & DTOs
#### [MODIFY] [ComprehensiveReportDTO.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/dto/ComprehensiveReportDTO.java)
- Add `private String placeOfBirth;`.

#### [MODIFY] [DrikPanchangamEngine.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/panchangam/impl/DrikPanchangamEngine.java)
- Propagate `payload.location()` / `payload.name()` to `placeOfBirth`.

#### [MODIFY] [PdfExportService.java](file:///d:/Intellij_WS/java-astro/src/main/java/org/vedic/astro/service/PdfExportService.java)
- Replace raw latitude/longitude with Place of Birth in Horoscope PDF header.
- Render AI Marriage Compatibility section in Marriage Matching PDF report.

---

## Verification Plan

### Automated Tests
- Run `mvn test -Dtest=GeminiPredictionServiceTest,MatchingCompatibilityTest,PdfExportServiceTest`
- Run full suite: `mvn clean test` (ensuring 50+ passing tests).
- Run frontend production build: `npm run build` in `frontend/`.

### Manual / Browser Verification
- Test Horoscope subtab horizontal scroll & responsiveness on mobile viewport.
- Test PDF Download button loading state on both Horoscope and Matching pages.
- Test AI Marriage Matching subtab toggle, on-demand generation, and 3-hour cache.
- Inspect downloaded PDF for Place of Birth and AI Marriage section.
