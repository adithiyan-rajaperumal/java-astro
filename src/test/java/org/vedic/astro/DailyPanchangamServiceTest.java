package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;

import java.time.LocalDate;
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

    @Test
    public void testGowriNallaNeramAndNakshatraYogamNextDayFlags() {
        PanchangamRequestDTO request = new PanchangamRequestDTO(
            "2026-07-23",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO result = dailyPanchangamService.calculateDailyPanchangam(request);

        assertNotNull(result);
        assertNotNull(result.gowriNallaNeram());
        assertFalse(result.gowriNallaNeram().isEmpty());

        for (DailyPanchangamDTO.TimeSlotDTO slot : result.gowriNallaNeram()) {
            if (slot.start().toUpperCase().contains("AM") && (slot.start().startsWith("12:") || slot.start().startsWith("01:") || slot.start().startsWith("02:") || slot.start().startsWith("03:") || slot.start().startsWith("04:"))) {
                assertTrue(slot.startNextDay(), "Post-midnight start time " + slot.start() + " should have startNextDay=true");
                assertTrue(slot.endNextDay(), "Post-midnight end time " + slot.end() + " should have endNextDay=true");
            }
        }

        assertNotNull(result.nakshatraYogams());
        assertFalse(result.nakshatraYogams().isEmpty());
    }

    @Test
    public void testVasthuNeramAndMuhurthamDetails() {
        // Thai 12th (Jan 26, 2026) -> Vasthu Day
        PanchangamRequestDTO requestThai = new PanchangamRequestDTO(
            "2026-01-26",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO resultThai = dailyPanchangamService.calculateDailyPanchangam(requestThai);
        assertNotNull(resultThai);
        assertTrue(resultThai.vasthuDay(), "Jan 26 (Thai 12th) should be a Vasthu Day");
        assertNotNull(resultThai.vasthuNeram(), "Vasthu Neram time slot should not be null");
        assertNotNull(resultThai.vasthuPujaNeram(), "Vasthu Puja Neram time slot should not be null");

        // Aadi 11th (Jul 27, 2026) -> Vasthu Day
        PanchangamRequestDTO requestAadi = new PanchangamRequestDTO(
            "2026-07-27",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO resultAadi = dailyPanchangamService.calculateDailyPanchangam(requestAadi);
        assertNotNull(resultAadi);
        assertTrue(resultAadi.vasthuDay(), "Jul 27 (Aadi 11th) should be a Vasthu Day");
        System.out.println("Aadi 11 Vasthu Awake: " + resultAadi.vasthuNeram().start() + " - " + resultAadi.vasthuNeram().end());
        System.out.println("Aadi 11 Vasthu Puja: " + resultAadi.vasthuPujaNeram().start() + " - " + resultAadi.vasthuPujaNeram().end());

        // Masi 22nd (Mar 06, 2026) -> Vasthu Day
        PanchangamRequestDTO requestMasi = new PanchangamRequestDTO(
            "2026-03-06",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO resultMasi = dailyPanchangamService.calculateDailyPanchangam(requestMasi);
        assertNotNull(resultMasi);
        assertTrue(resultMasi.vasthuDay(), "Mar 06 (Masi 22nd) should be a Vasthu Day");

        // July 24, 2026 (Aadi 8th) -> NOT a Vasthu Day
        PanchangamRequestDTO requestJul24 = new PanchangamRequestDTO(
            "2026-07-24",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        PanchangamRequestDTO requestAug = new PanchangamRequestDTO(
            "2026-08-23",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );
        DailyPanchangamDTO augResult = dailyPanchangamService.calculateDailyPanchangam(requestAug);
        assertNotNull(augResult);
        assertTrue(augResult.vasthuDay(), "Aug 23 (Avani 6th) should be a Vasthu Day");
        System.out.println("Avani 6 Vasthu Awake: " + augResult.vasthuNeram().start() + " - " + augResult.vasthuNeram().end());
        System.out.println("Avani 6 Vasthu Puja: " + augResult.vasthuPujaNeram().start() + " - " + augResult.vasthuPujaNeram().end());
    }

    @Test
    void testNext90DaysNakshatraYogams() {
        LocalDate startDate = LocalDate.of(2026, 7, 24);
        System.out.println("=== NEXT 90 DAYS NAKSHATRA YOGAMS FOR CHENNAI ===");
        for (int i = 0; i < 90; i++) {
            LocalDate date = startDate.plusDays(i);
            PanchangamRequestDTO req = new PanchangamRequestDTO(
                date.toString(),
                13.0827,
                80.2707,
                "ta",
                "LAHIRI"
            );
            DailyPanchangamDTO res = dailyPanchangamService.calculateDailyPanchangam(req);
            assertNotNull(res);
            StringBuilder sb = new StringBuilder();
            sb.append(date).append(" (").append(date.getDayOfWeek()).append("): ")
              .append(res.nakshatra().localizedName()).append(" [");
            if (res.nakshatraYogams() != null) {
                for (DailyPanchangamDTO.TimeSlotDTO slot : res.nakshatraYogams()) {
                    sb.append(slot.label()).append(" (").append(slot.start()).append("-").append(slot.end()).append(") ");
                }
            }
            sb.append("]");
            System.out.println(sb.toString());
        }
    }
}
