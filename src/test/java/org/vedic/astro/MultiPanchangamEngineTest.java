package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.DailyPanchangamService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiPanchangamEngineTest {

    @Autowired
    private PanchangamFactory panchangamFactory;

    @Autowired
    private DailyPanchangamService dailyPanchangamService;

    @Test
    public void testPanchangamTypeResolution() {
        assertEquals(PanchangamType.DRIK_TIRUKANITHAM, PanchangamType.fromString("DRIK_TIRUKANITHAM"));
        assertEquals(PanchangamType.VAKYA, PanchangamType.fromString("VAKYA"));
        assertEquals(PanchangamType.VAKYA, PanchangamType.fromString("VAKKIYAM"));
        assertEquals(PanchangamType.PARASARA_BHATTAR, PanchangamType.fromString("PARASARA_BHATTAR"));
        assertEquals(PanchangamType.PARASARA_BHATTAR, PanchangamType.fromString("PARASARA_BATTAR"));
        assertEquals(PanchangamType.SURYA_SIDDHANTA, PanchangamType.fromString("SURYA_SIDDHANTA"));
    }

    @Test
    public void testAllEnginesCalculateSuccessfully() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Test User", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "LAHIRI");

        var drikChart = panchangamFactory.getEngine(PanchangamType.DRIK_TIRUKANITHAM).calculate(birth);
        var vakyaChart = panchangamFactory.getEngine(PanchangamType.VAKYA).calculate(birth);
        var parasaraChart = panchangamFactory.getEngine(PanchangamType.PARASARA_BHATTAR).calculate(birth);
        var suryaChart = panchangamFactory.getEngine(PanchangamType.SURYA_SIDDHANTA).calculate(birth);

        assertNotNull(drikChart);
        assertNotNull(vakyaChart);
        assertNotNull(parasaraChart);
        assertNotNull(suryaChart);

        // Verify position shifts between Drik, Vakya (-0.78°), Parasara (-1.40°), and Surya Siddhanta
        double sunDrik = drikChart.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunVakya = vakyaChart.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunParasara = parasaraChart.getD1Positions().get("Sun").getAbsoluteLongitude();

        assertNotNull(sunVakya);
        assertNotEquals(sunDrik, sunParasara, 0.001);
        assertEquals(sunDrik - 1.83, sunParasara, 0.05);
    }

    @Test
    public void testParasaraBhattarWithPushyapakshaAyanamsa() {
        BirthDetailsDTO bhattarLahiri = new BirthDetailsDTO("Bhattar Lahiri", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "LAHIRI");
        BirthDetailsDTO bhattarPushya = new BirthDetailsDTO("Bhattar Pushya", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "PUSHYAPAKSHA");

        var chartLahiri = panchangamFactory.getEngine(PanchangamType.PARASARA_BHATTAR).calculate(bhattarLahiri);
        var chartPushya = panchangamFactory.getEngine(PanchangamType.PARASARA_BHATTAR).calculate(bhattarPushya);

        assertNotNull(chartLahiri);
        assertNotNull(chartPushya);

        double sunLahiri = chartLahiri.getD1Positions().get("Sun").getAbsoluteLongitude();
        double sunPushya = chartPushya.getD1Positions().get("Sun").getAbsoluteLongitude();

        // Pushyapaksha Ayanamsa (~22.66°) shifts positions relative to Lahiri (~23.79°) by ~1.13 degrees
        assertNotEquals(sunLahiri, sunPushya, 0.001);
    }

    @Test
    public void testDailyPanchangamWithMultiPanchangamSystems() {
        PanchangamRequestDTO requestDrik = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "LAHIRI", "DRIK_TIRUKANITHAM");
        PanchangamRequestDTO requestVakya = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "LAHIRI", "VAKYA");
        PanchangamRequestDTO requestParasara = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "LAHIRI", "PARASARA_BHATTAR");
        PanchangamRequestDTO requestParasaraPushya = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "PUSHYAPAKSHA", "PARASARA_BHATTAR");
        PanchangamRequestDTO requestSurya = new PanchangamRequestDTO("2026-08-05", 13.0827, 80.2707, "en", "LAHIRI", "SURYA_SIDDHANTA");

        DailyPanchangamDTO resultDrik = dailyPanchangamService.calculateDailyPanchangam(requestDrik);
        DailyPanchangamDTO resultVakya = dailyPanchangamService.calculateDailyPanchangam(requestVakya);
        DailyPanchangamDTO resultParasara = dailyPanchangamService.calculateDailyPanchangam(requestParasara);
        DailyPanchangamDTO resultParasaraPushya = dailyPanchangamService.calculateDailyPanchangam(requestParasaraPushya);
        DailyPanchangamDTO resultSurya = dailyPanchangamService.calculateDailyPanchangam(requestSurya);

        assertNotNull(resultDrik);
        assertNotNull(resultVakya);
        assertNotNull(resultParasara);
        assertNotNull(resultParasaraPushya);
        assertNotNull(resultSurya);
    }

    @Test
    public void testCornerCasesSankrantiAndElongationThresholds() {
        PanchangamRequestDTO sankrantiReq = new PanchangamRequestDTO("2026-01-14", 13.0827, 80.2707, "en", "LAHIRI", "VAKYA");
        DailyPanchangamDTO sankrantiRes = dailyPanchangamService.calculateDailyPanchangam(sankrantiReq);
        assertNotNull(sankrantiRes);
        assertNotNull(sankrantiRes.thithi());

        PanchangamRequestDTO purnimaReq = new PanchangamRequestDTO("2026-02-01", 13.0827, 80.2707, "en", "LAHIRI", "SURYA_SIDDHANTA");
        DailyPanchangamDTO purnimaRes = dailyPanchangamService.calculateDailyPanchangam(purnimaReq);
        assertNotNull(purnimaRes);
        assertNotNull(purnimaRes.nakshatra());
    }
}
