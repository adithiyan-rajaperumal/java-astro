package org.vedic.astro;

import org.junit.jupiter.api.Test;
import de.thmac.swisseph.SwissEph;
import de.thmac.swisseph.SweConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.matching.MatchingEngine;
import org.vedic.astro.matching.MatchingFactory;
import org.vedic.astro.matching.MatchingType;
import org.vedic.astro.matching.StrictnessLevel;
import org.vedic.astro.matching.dto.KootaResultDTO;
import org.vedic.astro.matching.dto.MatchingResponseDTO;
import org.vedic.astro.matching.model.MatchingContext;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.panchangam.PanchangamEngine;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.ChartOrchestrationService;
import org.vedic.astro.service.PdfExportService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MatchingEngineTest {

    @Autowired
    private PanchangamFactory panchangamFactory;

    @Autowired
    private MatchingFactory matchingFactory;

    @Autowired
    private ChartOrchestrationService orchestrationService;

    @Autowired
    private PdfExportService pdfExportService;

    @Test
    public void testAyanamsaAndMarriageMatching() {
        PanchangamEngine panchangamEngine = panchangamFactory.getEngine(PanchangamType.DRIK_TIRUKANITHAM);

        // Test Boy birth details - default Ayanamsa (Lahiri)
        BirthDetailsDTO boyDefault = new BirthDetailsDTO("Boy Default", 1995, 5, 15, 10, 30, 0, 13.0827, 80.2707, "LAHIRI");
        ChartResult boyDefaultChart = panchangamEngine.calculate(boyDefault);

        // Test Boy birth details - KP Ayanamsa
        BirthDetailsDTO boyKp = new BirthDetailsDTO("Boy KP", 1995, 5, 15, 10, 30, 0, 13.0827, 80.2707, "KP");
        ChartResult boyKpChart = panchangamEngine.calculate(boyKp);

        // Moon absolute longitude should be slightly different under different ayanamsas
        double defaultMoonLong = boyDefaultChart.getD1Positions().get("Moon").getAbsoluteLongitude();
        double kpMoonLong = boyKpChart.getD1Positions().get("Moon").getAbsoluteLongitude();
        assertNotEquals(defaultMoonLong, kpMoonLong, "Moon positions must differ between Lahiri and KP Ayanamsa");

        // Test Girl birth details
        BirthDetailsDTO girl = new BirthDetailsDTO("Girl", 1997, 8, 20, 14, 15, 0, 12.9716, 77.5946, "LAHIRI");
        ChartResult girlChart = panchangamEngine.calculate(girl);

        // Compute compatibility using Ashta Koota
        MatchingContext context = new MatchingContext(boyDefaultChart, girlChart, boyDefault, girl, StrictnessLevel.MODERATE);
        MatchingEngine ashtaKoota = matchingFactory.getEngine(MatchingType.ASHTA_KOOTA);
        MatchingResponseDTO ashtaResponse = ashtaKoota.calculateCompatibility(context);

        assertNotNull(ashtaResponse);
        assertEquals(36.0, ashtaResponse.getMaxScore());
        assertTrue(ashtaResponse.getTotalScore() >= 0 && ashtaResponse.getTotalScore() <= 36.0);
        assertNotNull(ashtaResponse.getVerdict());
        assertFalse(ashtaResponse.getKootas().isEmpty());

        // Construct Profiles
        ashtaResponse.setBoyProfile(orchestrationService.convertToUiDashboardResponse(boyDefaultChart, boyDefault));
        ashtaResponse.setGirlProfile(orchestrationService.convertToUiDashboardResponse(girlChart, girl));

        // Test PDF Export
        byte[] pdfBytes = pdfExportService.generateMarriageMatchingReport(ashtaResponse);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0, "PDF binary report must be populated");
    }

    @Test
    public void testDasaPoruttham() {
        PanchangamEngine panchangamEngine = panchangamFactory.getEngine(PanchangamType.DRIK_TIRUKANITHAM);

        BirthDetailsDTO boy = new BirthDetailsDTO("Boy", 1993, 10, 12, 6, 45, 0, 17.3850, 78.4867, "LAHIRI");
        BirthDetailsDTO girl = new BirthDetailsDTO("Girl", 1996, 2, 28, 22, 10, 0, 17.3850, 78.4867, "LAHIRI");

        ChartResult boyChart = panchangamEngine.calculate(boy);
        ChartResult girlChart = panchangamEngine.calculate(girl);

        MatchingContext context = new MatchingContext(boyChart, girlChart, boy, girl, StrictnessLevel.MODERATE);
        MatchingEngine dasaPorutham = matchingFactory.getEngine(MatchingType.DASA_PORUTHAM);
        MatchingResponseDTO dasaResponse = dasaPorutham.calculateCompatibility(context);

        assertNotNull(dasaResponse);
        assertEquals(10.0, dasaResponse.getMaxScore());
        assertTrue(dasaResponse.getTotalScore() >= 0 && dasaResponse.getTotalScore() <= 10.0);
        assertFalse(dasaResponse.getKootas().isEmpty());
    }

    @Test
    public void testSunriseSunset() {
        SwissEph swissEph = new SwissEph();
        double julianDay = 2461241.5; // July 19, 2026 noon UT
        double[] geopos = {77.2090, 28.6139, 0}; // New Delhi (longitude, latitude, altitude)
        de.thmac.swisseph.DblObj tret = new de.thmac.swisseph.DblObj();
        StringBuffer serr = new StringBuffer();
        int res = swissEph.swe_rise_trans(
            julianDay,
            de.thmac.swisseph.SweConst.SE_SUN,
            null,
            de.thmac.swisseph.SweConst.SEFLG_SWIEPH,
            de.thmac.swisseph.SweConst.SE_CALC_RISE,
            geopos,
            1013.25,
            15.0,
            tret,
            serr
        );
        assertTrue(res >= 0, "Sunrise calculation should succeed");
    }
}


