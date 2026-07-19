package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DailyPanchangamServiceTest {

    @Autowired
    private DailyPanchangamService dailyPanchangamService;

    @Test
    public void testDailyPanchangamCalculationsAndTranslations() {
        // Query for July 19, 2026 (Sunday) in Tamil (ta) for New Delhi coordinates
        PanchangamRequestDTO requestTa = new PanchangamRequestDTO(
            "2026-07-19",
            28.6139,
            77.2090,
            "ta",
            "LAHIRI"
        );

        DailyPanchangamDTO resultTa = dailyPanchangamService.calculateDailyPanchangam(requestTa);

        assertNotNull(resultTa);
        assertEquals("2026-07-19", resultTa.date());
        assertNotNull(resultTa.sunrise());
        assertNotNull(resultTa.sunset());
        assertNotNull(resultTa.moonrise());
        assertNotNull(resultTa.moonset());

        // Validate Thithi, Nakshatra, Yogam, Karanam are computed and translated
        assertNotNull(resultTa.thithi());
        assertNotNull(resultTa.nakshatra());
        assertNotNull(resultTa.yogam());
        assertNotNull(resultTa.karanam());
        assertNotNull(resultTa.rashi());

        // Verify some Tamil translations
        // Sun Rashi on July 19, 2026 Moon is in Kanya (Virgo) -> Tamil: கன்னி
        assertEquals("கன்னி", resultTa.rashi());
        assertTrue(resultTa.thithi().name().contains("Thithi"));
        
        // Kalam slots
        assertFalse(resultTa.raghuKalam().isEmpty());
        assertFalse(resultTa.emagandam().isEmpty());
        assertFalse(resultTa.kulikai().isEmpty());
        assertEquals("ராகு காலம்", resultTa.raghuKalam().get(0).label());

        // Horais
        assertEquals(24, resultTa.horais().size());
        assertEquals("சூரியன்", resultTa.horais().get(0).localizedPlanet()); // Sunday morning 1st hora is Sun -> சூரியன்

        // Netram and Jeevan
        assertTrue(resultTa.netram() >= 0 && resultTa.netram() <= 2);
        assertTrue(resultTa.jeevan() == 0.0 || resultTa.jeevan() == 0.5 || resultTa.jeevan() == 1.0);

        // Query in English
        PanchangamRequestDTO requestEn = new PanchangamRequestDTO(
            "2026-07-19",
            28.6139,
            77.2090,
            "en",
            "LAHIRI"
        );
        DailyPanchangamDTO resultEn = dailyPanchangamService.calculateDailyPanchangam(requestEn);
        assertEquals("Kanya", resultEn.rashi());
        assertEquals("Rahu Kalam", resultEn.raghuKalam().get(0).label());
        assertEquals("Sun", resultEn.horais().get(0).localizedPlanet());

        // Query in Hindi (hi)
        PanchangamRequestDTO requestHi = new PanchangamRequestDTO(
            "2026-07-19",
            28.6139,
            77.2090,
            "hi",
            "LAHIRI"
        );
        DailyPanchangamDTO resultHi = dailyPanchangamService.calculateDailyPanchangam(requestHi);
        assertEquals("कन्या", resultHi.rashi());
        assertEquals("राहु काल", resultHi.raghuKalam().get(0).label());
        assertEquals("सूर्य", resultHi.horais().get(0).localizedPlanet());
    }
}
