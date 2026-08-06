package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.panchangam.impl.VakyaPanchangamEngine;

import java.util.Map;

@SpringBootTest
public class PrintChartTest {

    @Autowired
    private VakyaPanchangamEngine vakyaEngine;

    @Test
    public void printCharts() {
        System.out.println("=== ADITHIYAN ===");
        BirthDetailsDTO dto1 = new BirthDetailsDTO(
                "Adithiyan", 1995, 7, 19, 13, 10, 0, 12.9165, 79.1325, "VAKYA"
        );
        ChartResult res1 = vakyaEngine.calculate(dto1);
        for (Map.Entry<String, PlanetaryPosition> entry : res1.getD1Positions().entrySet()) {
            System.out.println("D1 " + entry.getKey() + ": " + entry.getValue().getRashi());
        }
        for (Map.Entry<String, PlanetaryPosition> entry : res1.getD9Positions().entrySet()) {
            System.out.println("D9 " + entry.getKey() + ": " + entry.getValue().getRashi());
        }

        System.out.println("=== UTHAYASRI ===");
        BirthDetailsDTO dto2 = new BirthDetailsDTO(
                "Sow.D.Uthayasri", 2002, 8, 17, 15, 15, 0, 11.95, 79.5333, "VAKYA"
        );
        ChartResult res2 = vakyaEngine.calculate(dto2);
        for (Map.Entry<String, PlanetaryPosition> entry : res2.getD1Positions().entrySet()) {
            System.out.println("D1 " + entry.getKey() + ": " + entry.getValue().getRashi());
        }
        for (Map.Entry<String, PlanetaryPosition> entry : res2.getD9Positions().entrySet()) {
            System.out.println("D9 " + entry.getKey() + ": " + entry.getValue().getRashi());
        }
    }
}
