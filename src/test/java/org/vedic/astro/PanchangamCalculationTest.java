package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PanchangamCalculationTest {

    @Autowired
    private DailyPanchangamService dailyPanchangamService;

    @Test
    public void testTheiPiraiAndNetramJeevanCalculations() {
        // Test date during Krishna Paksha (e.g. 2026-08-05 is Waning Moon / Krishna Paksha)
        PanchangamRequestDTO request = new PanchangamRequestDTO(
            "2026-08-05",
            13.0827,
            80.2707,
            "ta",
            "LAHIRI"
        );

        DailyPanchangamDTO dto = dailyPanchangamService.calculateDailyPanchangam(request);

        assertNotNull(dto);
        // Verify Thithi and Netram/Jeevan bounds
        assertTrue(dto.netram() >= 0 && dto.netram() <= 2);
        assertTrue(dto.jeevan() == 0.0 || dto.jeevan() == 0.5 || dto.jeevan() == 1.0);

        // Verify Thei Pirai boolean flag exists
        assertNotNull(dto.isTheiPirai());
        
        // Verify Bad Times partitions are calculated
        assertFalse(dto.raghuKalam().isEmpty());
        assertFalse(dto.emagandam().isEmpty());
        assertFalse(dto.kulikai().isEmpty());
        assertEquals(24, dto.horais().size());
    }

    @Test
    public void testInspectCandidateMuhurthamDays() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        int totalDays = 365;
        System.out.println("=== Comprehensive 365-Day 2026 Muhurtham Inspection ===");
        int totalMuhurthamDays = 0;
        for (int i = 0; i < totalDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            PanchangamRequestDTO request = new PanchangamRequestDTO(
                currentDate.toString(),
                13.0827,
                80.2707,
                "LAHIRI",
                "en"
            );
            DailyPanchangamDTO dto = dailyPanchangamService.calculateDailyPanchangam(request);
            if (dto.muhurthamDay()) {
                totalMuhurthamDays++;
                System.out.printf("[%s %-9s] Thithi: %2d (%-12s) ends %-10s | Nak: %2d (%-12s) ends %-10s | Yogam: %2d | Netram: %d, Jeevan: %.1f | Window: %s%n",
                    currentDate,
                    currentDate.getDayOfWeek(),
                    dto.thithi().number(), dto.thithi().name(), dto.thithi().endTime(),
                    dto.nakshatra().number(), dto.nakshatra().name(), dto.nakshatra().endTime(),
                    dto.yogam().number(),
                    dto.netram(), dto.jeevan(),
                    dto.muhurthamWindow()
                );
            }
        }
        System.out.println("Total Authentic Subha Muhurtham Days in 2026: " + totalMuhurthamDays);
        assertTrue(totalMuhurthamDays >= 10, "Expected authentic Subha Muhurtham days in 2026, found: " + totalMuhurthamDays);
    }

    @Test
    public void testInspectSpecificDates() {
        List<String> dates = List.of("2026-08-17", "2026-08-30");
        for (String dateStr : dates) {
            PanchangamRequestDTO request = new PanchangamRequestDTO(
                dateStr,
                13.0827,
                80.2707,
                "LAHIRI",
                "en"
            );
            DailyPanchangamDTO dto = dailyPanchangamService.calculateDailyPanchangam(request);
            System.out.println("==================================================");
            System.out.println("DATE: " + dto.date() + " (" + LocalDate.parse(dto.date()).getDayOfWeek() + ")");
            System.out.println("Thithi: " + dto.thithi().number() + " (" + dto.thithi().name() + ") ends: " + dto.thithi().endTime() + " (next: " + dto.thithi().nextName() + ")");
            System.out.println("Nakshatra: " + dto.nakshatra().number() + " (" + dto.nakshatra().name() + ") ends: " + dto.nakshatra().endTime() + " (next: " + dto.nakshatra().nextName() + ")");
            System.out.println("Nitya Yogam: " + dto.yogam().number() + " (" + dto.yogam().name() + ")");
            System.out.println("Karanam: " + dto.karanam().number() + " (" + dto.karanam().name() + ")");
            System.out.println("Rashi: " + dto.rashi());
            System.out.println("Netram: " + dto.netram() + ", Jeevan: " + dto.jeevan());
            System.out.println("isTheiPirai: " + dto.isTheiPirai());
            System.out.println("isMuhurthamDay: " + dto.muhurthamDay());
            System.out.println("sankrantiDay: " + dto.sankrantiDay());
            System.out.println("guruMoudhya: " + dto.guruMoudhya());
            System.out.println("sukraMoudhya: " + dto.sukraMoudhya());
            System.out.println("thithiSoonya: " + dto.thithiSoonya());
            System.out.println("muhurthamWindow: " + dto.muhurthamWindow());
        }
    }
}
