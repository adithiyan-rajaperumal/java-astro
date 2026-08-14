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

        PredictionResponseDTO aiPredictions = PredictionResponseDTO.builder()
                .enabled(true)
                .message("ஜாதகரின் வாழ்க்கை சுப யோகங்களால் நிறைந்தது.")
                .personalityAndBehavior(PredictionResponseDTO.PersonalityAndBehavior.builder()
                        .coreTemperament("தைரியமும் விடாமுயற்சியும் கொண்டவர். தலைமைத்துவ ஆற்றல் மிக்கவர்.")
                        .build())
                .retrospectivePastMilestones(List.of(
                        PredictionResponseDTO.RetrospectivePastMilestone.builder()
                                .approxPeriod("2015 - 2018")
                                .milestoneTitle("உயர் கல்வி வெற்றி")
                                .eventNarrative("பொறியியல் கல்வி நிறைவு மற்றும் முதல் வேலைவாய்ப்பு.")
                                .build()
                ))
                .aiLongevityAnalysis(PredictionResponseDTO.AiLongevityAnalysis.builder()
                        .calculatedAyulCeiling(82)
                        .classification("Poornayu")
                        .primarySpanRationale("லக்னாதிபதி மற்றும் 8-ஆம் அதிபதியின் சுப பலம்.")
                        .activeYogasIdentified(List.of(
                                PredictionResponseDTO.AiYogaItem.builder().yogaName("கஜகேசரி யோகம்").effect("அறிவு மற்றும் நிதி வளம்.").build()
                        ))
                        .activeDoshasIdentified(List.of(
                                PredictionResponseDTO.AiDoshaItem.builder().doshaName("செவ்வாய் தோஷம்").remedialAdvice("செவ்வாய்க்கிழமை நெய்தீபம் ஏற்றுவது நலம்.").build()
                        ))
                        .build())
                .yearlyPredictions(List.of(
                        PredictionResponseDTO.YearlyPrediction.builder()
                                .year(2026)
                                .age(31)
                                .dasaBhukthi("சுக்கிரன் - குரு")
                                .annualNarrative("இந்த ஆண்டு புதிய தொழில் முயற்சிகள் கைகூடும். குடும்பத்தில் அமைதியும் மகிழ்ச்சியும் நிலவும்.")
                                .build()
                ))
                .build();

        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");
        var panchangam = panchangamFactory.getEngine(org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = panchangam.calculate(birth);

        ComprehensiveReportDTO report = orchestrationService.compileComprehensivePdfData(chartResult, birth, new double[12]);
        report.setAiPredictions(aiPredictions);

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

    @Test
    public void testPdfExportAcrossAll6Languages() {
        String[] languages = {"en", "ta", "hi", "te", "kn", "ml"};
        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");
        var panchangam = panchangamFactory.getEngine(org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = panchangam.calculate(birth);

        ComprehensiveReportDTO report = orchestrationService.compileComprehensivePdfData(chartResult, birth, new double[12]);
        assertNotNull(report);

        PredictionResponseDTO aiPredictions = PredictionResponseDTO.builder()
                .enabled(true)
                .message("Astrological analysis active.")
                .personalityAndBehavior(PredictionResponseDTO.PersonalityAndBehavior.builder()
                        .coreTemperament("தைரியமும் விடாமுயற்சியும் கொண்டவர். Leadership & perseverance.")
                        .build())
                .retrospectivePastMilestones(List.of(
                        PredictionResponseDTO.RetrospectivePastMilestone.builder()
                                .approxPeriod("2015 - 2018")
                                .milestoneTitle("உயர் கல்வி வெற்றி")
                                .eventNarrative("பொறியியல் கல்வி நிறைவு மற்றும் முதல் வேலைவாய்ப்பு.")
                                .build()
                ))
                .aiLongevityAnalysis(PredictionResponseDTO.AiLongevityAnalysis.builder()
                        .calculatedAyulCeiling(82)
                        .classification("Poornayu")
                        .primarySpanRationale("லக்னாதிபதி மற்றும் 8-ஆம் அதிபதியின் சுப பலம்.")
                        .activeYogasIdentified(List.of(
                                PredictionResponseDTO.AiYogaItem.builder().yogaName("கஜகேசரி யோகம்").effect("அறிவு மற்றும் நிதி வளம்.").build()
                        ))
                        .activeDoshasIdentified(List.of(
                                PredictionResponseDTO.AiDoshaItem.builder().doshaName("செவ்வாய் தோஷம்").remedialAdvice("செவ்வாய்க்கிழமை நெய்தீபம் ஏற்றுவது நலம்.").build()
                        ))
                        .build())
                .yearlyPredictions(List.of(
                        PredictionResponseDTO.YearlyPrediction.builder()
                                .year(2026)
                                .age(31)
                                .dasaBhukthi("சுக்கிரன் - குரு")
                                .annualNarrative("இந்த ஆண்டு புதிய தொழில் முயற்சிகள் கைகூடும். குடும்பத்தில் அமைதியும் மகிழ்ச்சியும் நிலவும்.")
                                .build()
                ))
                .build();

        report.setAiPredictions(aiPredictions);

        org.vedic.astro.config.PdfExportProperties customProps = new org.vedic.astro.config.PdfExportProperties();
        customProps.setIncludeLifeAnchors(true);
        customProps.setIncludeYogasDoshams(true);
        customProps.setIncludeAiPredictions(true);

        org.vedic.astro.service.PdfExportService customPdfService = new org.vedic.astro.service.PdfExportService(
                translationService,
                customProps
        );

        for (String lang : languages) {
            LocaleContextHolder.setLocale(new Locale(lang));
            byte[] pdfBytes = customPdfService.generateAstrologyReport(report);
            assertNotNull(pdfBytes, "PDF generation failed for language: " + lang);
            assertTrue(pdfBytes.length > 5000, "PDF size too small for language: " + lang);
        }
    }

    @Test
    public void testPdfAiBalanDasaBhukthiAutomaticBackfill() {
        LocaleContextHolder.setLocale(new Locale("ta"));

        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");
        var panchangam = panchangamFactory.getEngine(org.vedic.astro.panchangam.PanchangamType.DRIK_TIRUKANITHAM);
        var chartResult = panchangam.calculate(birth);

        ComprehensiveReportDTO report = orchestrationService.compileComprehensivePdfData(chartResult, birth, new double[12]);

        // AI predictions with empty dasaBhukthi to test backfill
        PredictionResponseDTO aiPredictions = PredictionResponseDTO.builder()
                .enabled(true)
                .forecastMode("TEN_YEARS")
                .startYear(2026)
                .endYear(2030)
                .startAge(30)
                .endAge(34)
                .totalForecastYears(5)
                .yearlyPredictions(List.of(
                        PredictionResponseDTO.YearlyPrediction.builder()
                                .year(2026)
                                .age(30)
                                .dasaBhukthi("") // Empty to trigger backfill
                                .annualNarrative("தொழில் வளர்ச்சி மற்றும் பணவரவு.")
                                .build(),
                        PredictionResponseDTO.YearlyPrediction.builder()
                                .year(2027)
                                .age(31)
                                .dasaBhukthi(null) // Null to trigger backfill
                                .annualNarrative("குடும்ப நலம் மற்றும் சுபகாரியங்கள்.")
                                .build()
                ))
                .build();

        report.setAiPredictions(aiPredictions);

        byte[] pdfBytes = pdfExportService.generateAstrologyReport(report);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 5000);
    }
}
