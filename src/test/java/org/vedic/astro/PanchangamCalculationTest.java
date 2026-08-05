package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;

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
}
