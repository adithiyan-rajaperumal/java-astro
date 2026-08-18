package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.dto.PredictionResponseDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.panchangam.PanchangamEngine;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.PdfExportService;
import org.vedic.astro.util.AstrologicalTranslationHelper;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AdithiyanPdfDataInspectTest {

    @Autowired
    private PanchangamFactory panchangamFactory;

    @Autowired
    private PdfExportService pdfExportService;

    @Test
    public void testDasaBhukthiTranslationAcrossLanguages() {
        assertEquals("குரு - சனி", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter - Saturn", "ta"));
        assertEquals("குரு மகாதிசை", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter Mahadasa", "ta"));
        assertEquals("गुरु - शनि", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter - Saturn", "hi"));
        assertEquals("గురుడు - శని", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter - Saturn", "te"));
        assertEquals("ಗುರು - ಶನಿ", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter - Saturn", "kn"));
        assertEquals("വ്യാഴം - ശനി", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter - Saturn", "ml"));
        assertEquals("Jupiter - Saturn", AstrologicalTranslationHelper.translateDasaBhukthi("Jupiter - Saturn", "en"));
    }

    @Test
    public void fullPropertiesAuditAndPdfVerification() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:i18n/messages*.properties");

        Map<String, Properties> bundleMap = new HashMap<>();
        for (Resource r : resources) {
            String filename = r.getFilename();
            Properties p = new Properties();
            try (InputStreamReader isr = new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8)) {
                p.load(isr);
            }
            bundleMap.put(filename, p);
        }

        Properties defaultBundle = bundleMap.get("messages.properties");
        assertNotNull(defaultBundle, "messages.properties must exist");
        Set<String> defaultKeys = defaultBundle.stringPropertyNames();

        String[] langFiles = {
                "messages_en.properties",
                "messages_ta.properties",
                "messages_hi.properties",
                "messages_te.properties",
                "messages_kn.properties",
                "messages_ml.properties"
        };

        for (String lf : langFiles) {
            Properties p = bundleMap.get(lf);
            assertNotNull(p, lf + " must exist");
            List<String> missingFromLang = new ArrayList<>();
            for (String dk : defaultKeys) {
                if (!p.containsKey(dk) || p.getProperty(dk).isBlank()) {
                    missingFromLang.add(dk);
                }
            }
            assertEquals(0, missingFromLang.size(), lf + " should have 0 missing keys from default: " + missingFromLang);
        }

        // Generate PDF for Adithiyan in all 6 languages
        BirthDetailsDTO payload = BirthDetailsDTO.builder()
                .name("Adithiyan")
                .year(1996)
                .month(8)
                .day(18)
                .hour(14)
                .minute(5)
                .second(0)
                .latitude(10.7905)
                .longitude(78.7047)
                .ayanamsa("LAHIRI")
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .build();

        PanchangamEngine engine = panchangamFactory.getEngine(PanchangamType.DRIK_TIRUKANITHAM);
        ChartResult res = engine.calculate(payload);

        String[] langs = {"ta", "hi", "te", "kn", "ml", "en"};
        for (String lang : langs) {
            LocaleContextHolder.setLocale(Locale.forLanguageTag(lang));
            ComprehensiveReportDTO reportData = engine.generateComprehensiveReport(payload, res);
            reportData.setPanchangamSystem("DRIK_TIRUKANITHAM");

            // Attach mock AI predictions to test yearly prediction Dasa Bhukthi translation
            PredictionResponseDTO aiPred = PredictionResponseDTO.builder()
                    .enabled(true)
                    .startYear(2026)
                    .endYear(2027)
                    .yearlyPredictions(List.of(
                            PredictionResponseDTO.YearlyPrediction.builder()
                                    .year(2026)
                                    .age(30)
                                    .dasaBhukthi("Jupiter - Saturn")
                                    .annualNarrative("Favorable growth year.")
                                    .build(),
                            PredictionResponseDTO.YearlyPrediction.builder()
                                    .year(2027)
                                    .age(31)
                                    .dasaBhukthi("Jupiter - Mercury")
                                    .annualNarrative("High intellectual vitality.")
                                    .build()
                    ))
                    .build();
            reportData.setAiPredictions(aiPred);

            byte[] pdfBytes = pdfExportService.generateAstrologyReport(reportData);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 1000, "PDF should be successfully generated for language: " + lang);
        }
    }
}
