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
}
