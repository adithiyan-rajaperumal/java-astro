package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.i18n.LocaleContextHolder;
import org.vedic.astro.dto.*;
import org.vedic.astro.service.PdfExportService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PdfExportServiceTest {

    @Autowired
    private PdfExportService pdfExportService;

    @Test
    public void testTamilPdfGenerationWithAiBalanAndAuspiciousAnchors() {
        LocaleContextHolder.setLocale(new Locale("ta"));

        PredictionResponseDTO.AuspiciousAnchors anchors = PredictionResponseDTO.AuspiciousAnchors.builder()
                .lifeGemstone("செம்பவளம் (Red Coral)")
                .favorableColors("பவள சிவப்பு, ஆரஞ்சு")
                .luckyNumbers("9, 1, 3")
                .favorableDays("செவ்வாய் & வியாழன்")
                .ishtaDevata("ஸ்ரீ முருகப்பெருமான்")
                .favorableDirections("தெற்கு (South)")
                .build();

        PredictionResponseDTO aiPredictions = PredictionResponseDTO.builder()
                .enabled(true)
                .overallSummary("ஜாதகரின் வாழ்க்கை சுப யோகங்களால் நிறைந்தது.")
                .nativePersonality(PredictionResponseDTO.NativePersonality.builder()
                        .coreTemperament("தைரியமும் விடாமுயற்சியும் கொண்டவர்.")
                        .keyStrengths(List.of("தலைமைத்துவம்", "தீர்க்கமான சிந்தனை"))
                        .vulnerabilitiesAndKarmicLessons(List.of("முன்கோபம்"))
                        .build())
                .auspiciousAnchors(anchors)
                .healthAnalysis(PredictionResponseDTO.HealthAnalysis.builder()
                        .ayurvedicConstitution("பித்த-வாத பிரகிருதி")
                        .longevityVitalitySummary("தீர்க்காயுள் யோகம்")
                        .organVulnerabilities(List.of("செரிமான மண்டலம்"))
                        .recommendedDietAndLifestyle(List.of("இயற்கை உணவு"))
                        .build())
                .aiYogas(List.of(
                        PredictionResponseDTO.AiYoga.builder().name("ருசக யோகம்").formingPlanets("செவ்வாய்").impact("அதிகார பதவி").build()
                ))
                .aiDoshams(List.of(
                        PredictionResponseDTO.AiDosham.builder().name("செவ்வாய் தோஷம்").status("நிவர்த்தி").nullificationFactor("யோககாரகன் ஆட்சி").remedy("முருகன் வழிபாடு").build()
                ))
                .lifetimePredictions(List.of(
                        PredictionResponseDTO.YearlyPrediction.builder()
                                .year(2026)
                                .age(30)
                                .dasaBhukthi("சுக்கிரன் - குரு")
                                .yearlyTheme("தொழில் வளர்ச்சி மற்றும் சுப காரிய யோகம்")
                                .detailedPrediction("இந்த ஆண்டு புதிய தொழில் முயற்சிகள் கைகூடும். குடும்பத்தில் அமைதியும் மகிழ்ச்சியும் நிலவும்.")
                                .astrologicalBasis("10-ஆம் அதிபதி சுப பார்வை")
                                .cautionsAndRemedies("வியாழக்கிழமைகளில் குரு பகவான் வழிபாடு செய்க.")
                                .build()
                ))
                .build();

        ComprehensiveReportDTO report = ComprehensiveReportDTO.builder()
                .name("சுரேஷ் குமார்")
                .dateOfBirth("1995-05-15")
                .timeOfBirth("06:30")
                .placeOfBirth("சென்னை")
                .ayanamsa("LAHIRI")
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .thithi("சுக்ல பக்ஷ துவிதியை")
                .yogam("சித்தம்")
                .karanam("பவ")
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("மேஷம்")
                        .rashi("ரிஷபம்")
                        .nakshatra("ரோகிணி")
                        .nakshatraPada(1)
                        .build())
                .birthPlanetaryPositions(Collections.emptyList())
                .vargaChartsMap(Collections.emptyMap())
                .vimshottariTimeline(Collections.emptyList())
                .shadbalaStrengths(ShadbalaDTO.builder().planetStrengths(Collections.emptyMap()).build())
                .aiPredictions(aiPredictions)
                .build();

        byte[] pdfBytes = pdfExportService.generateAstrologyReport(report);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "PDF bytes should be generated successfully with substantial content");
    }

    @Autowired
    private org.vedic.astro.panchangam.PanchangamFactory panchangamFactory;

    @Autowired
    private org.vedic.astro.service.ChartOrchestrationService orchestrationService;

    @Test
    public void testFullPdfReportWith12ChartVargaSuite() {
        LocaleContextHolder.setLocale(new Locale("ta"));

        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");
        var panchangam = panchangamFactory.getEngine(org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = panchangam.calculate(birth);

        ComprehensiveReportDTO report = orchestrationService.compileComprehensivePdfData(chartResult, birth, new double[12]);
        assertNotNull(report);
        assertNotNull(report.getVargaChartsMap());
        assertTrue(report.getVargaChartsMap().containsKey("D1") || report.getVargaChartsMap().containsKey("d1"));
        assertTrue(report.getVargaChartsMap().containsKey("D9") || report.getVargaChartsMap().containsKey("d9"));
        assertTrue(report.getVargaChartsMap().containsKey("Bhava") || report.getVargaChartsMap().containsKey("bhava"));

        byte[] pdfBytes = pdfExportService.generateAstrologyReport(report);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 5000, "PDF with full 12-chart varga suite should be substantial in size");
    }

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
        assertNotNull(uiDto.getLifeAnchors().directions());
        assertNotNull(uiDto.getLifeAnchors().structuralAnchors());
        assertEquals(7, uiDto.getLifeAnchors().numerology().radicalDriverNumber());
    }

    @Autowired
    private org.vedic.astro.service.TranslationService translationService;

    @Test
    public void testPdfWithCustomFlagsForYogasDoshamsAndLifeAnchors() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");
        var panchangam = panchangamFactory.getEngine(org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = panchangam.calculate(birth);

        ComprehensiveReportDTO report = orchestrationService.compileComprehensivePdfData(chartResult, birth, new double[12]);
        assertNotNull(report);

        // Test with custom properties where both flags are enabled
        org.vedic.astro.config.PdfExportProperties customProps = new org.vedic.astro.config.PdfExportProperties();
        customProps.setIncludeLifeAnchors(true);
        customProps.setIncludeYogasDoshams(true);

        org.vedic.astro.service.PdfExportService customPdfService = new org.vedic.astro.service.PdfExportService(
                translationService,
                customProps
        );

        byte[] pdfWithSections = customPdfService.generateAstrologyReport(report);
        assertNotNull(pdfWithSections);
        assertTrue(pdfWithSections.length > 5000);
    }
}
