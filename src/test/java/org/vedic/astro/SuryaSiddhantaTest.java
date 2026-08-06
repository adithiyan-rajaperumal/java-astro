package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.panchangam.impl.SuryaSiddhantaPanchangamEngine;
import org.vedic.astro.service.impl.DailyPanchangamServiceImpl;

@SpringBootTest
public class SuryaSiddhantaTest {

    @Autowired
    private SuryaSiddhantaPanchangamEngine ssEngine;
    
    @Autowired
    private DailyPanchangamServiceImpl dailyPanchangamService;

    @Autowired
    private org.vedic.astro.panchangam.impl.DrikPanchangamEngine drikEngine;

    @Test
    public void runSSEngine() {
        System.out.println("=== ADITHIYAN SURYA SIDDHANTA ===");
        BirthDetailsDTO dto = new BirthDetailsDTO(
                "Adithiyan", 1995, 7, 19, 13, 10, 0, 12.9333, 79.1333, "SURYA_SIDDHANTA"
        );
        ChartResult res = ssEngine.calculate(dto);
        System.out.println("Lagna: " + res.getD1Positions().get("Lagna").getAbsoluteLongitude());
        System.out.println("Sidereal Time (ARMC hours): " + (res.getD1Positions().get("Lagna").getSpeed()));
        System.out.println("Sun: " + res.getD1Positions().get("Sun").getAbsoluteLongitude());
        System.out.println("Moon: " + res.getD1Positions().get("Moon").getAbsoluteLongitude());
        System.out.println("Nakshatra: " + res.getD1Positions().get("Moon").getNakshatra() + " (" + res.getD1Positions().get("Moon").getPada() + ")");
        org.vedic.astro.dto.PanchangamRequestDTO req = new org.vedic.astro.dto.PanchangamRequestDTO(
            "1995-07-19", 12.9333, 79.1333, "en", "SURYA_SIDDHANTA", "SURYA_SIDDHANTA"
        );
        DailyPanchangamDTO daily = dailyPanchangamService.calculateDailyPanchangam(req);
        System.out.println("Tithi: " + daily.thithi());
        
    }
}
