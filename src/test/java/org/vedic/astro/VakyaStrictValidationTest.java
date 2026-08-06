package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.panchangam.impl.VakyaPanchangamEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class VakyaStrictValidationTest {

    @Autowired
    private VakyaPanchangamEngine vakyaEngine;

    @Test
    public void testAdithiyanVakyaHoroscopePdfMatch() {
        BirthDetailsDTO dto = new BirthDetailsDTO(
                "Chi.R.Adithyan", 1995, 7, 19, 13, 10, 0, 12.9333, 79.1333, "VAKYA"
        );

        ChartResult res = vakyaEngine.calculate(dto);

        // Verify D1 Rashi Positions (Adithiyan)
        assertEquals("Tula", res.getD1Positions().get("Lagna").getRashi());
        assertEquals("Karka", res.getD1Positions().get("Sun").getRashi());
        assertEquals("Meena", res.getD1Positions().get("Moon").getRashi());
        assertEquals("Kanya", res.getD1Positions().get("Mars").getRashi());
        assertEquals("Karka", res.getD1Positions().get("Mercury").getRashi());
        assertEquals("Vrischika", res.getD1Positions().get("Jupiter").getRashi());
        assertEquals("Mithuna", res.getD1Positions().get("Venus").getRashi());
        assertEquals("Meena", res.getD1Positions().get("Saturn").getRashi());
        assertEquals("Tula", res.getD1Positions().get("Rahu").getRashi());
        assertEquals("Mesha", res.getD1Positions().get("Ketu").getRashi());

        // Verify D9 Navamsa Positions (Adithiyan)
        assertEquals("Kumbha", res.getD9Positions().get("Lagna").getRashi());
        assertEquals("Karka", res.getD9Positions().get("Sun").getRashi());
        assertEquals("Meena", res.getD9Positions().get("Moon").getRashi());
        assertEquals("Kumbha", res.getD9Positions().get("Mars").getRashi());
        assertEquals("Makara", res.getD9Positions().get("Mercury").getRashi());
        assertEquals("Tula", res.getD9Positions().get("Jupiter").getRashi());
        assertEquals("Vrishabha", res.getD9Positions().get("Venus").getRashi());
        assertEquals("Karka", res.getD9Positions().get("Saturn").getRashi());
        assertEquals("Dhanu", res.getD9Positions().get("Rahu").getRashi());
        assertEquals("Mithuna", res.getD9Positions().get("Ketu").getRashi());
    }

    @Test
    public void testUthayasriVakyaHoroscopePdfMatch() {
        BirthDetailsDTO dto = new BirthDetailsDTO(
                "Sow.D.Uthayasri", 2002, 8, 17, 15, 15, 0, 11.95, 79.5333, "VAKYA"
        );

        ChartResult res = vakyaEngine.calculate(dto);

        // Verify D1 Rashi Positions (Uthayasri)
        assertEquals("Dhanu", res.getD1Positions().get("Lagna").getRashi());
        assertEquals("Karka", res.getD1Positions().get("Sun").getRashi());
        assertEquals("Vrischika", res.getD1Positions().get("Moon").getRashi());
        assertEquals("Karka", res.getD1Positions().get("Mars").getRashi());
        assertEquals("Simha", res.getD1Positions().get("Mercury").getRashi());
        assertEquals("Karka", res.getD1Positions().get("Jupiter").getRashi());
        assertEquals("Kanya", res.getD1Positions().get("Venus").getRashi());
        assertEquals("Vrishabha", res.getD1Positions().get("Saturn").getRashi());
        assertEquals("Vrishabha", res.getD1Positions().get("Rahu").getRashi());
        assertEquals("Vrischika", res.getD1Positions().get("Ketu").getRashi());

        // Verify D9 Navamsa Positions (Uthayasri)
        assertEquals("Karka", res.getD9Positions().get("Lagna").getRashi());
        assertEquals("Meena", res.getD9Positions().get("Sun").getRashi());
        assertEquals("Kumbha", res.getD9Positions().get("Moon").getRashi());
        assertEquals("Meena", res.getD9Positions().get("Mars").getRashi());
        assertEquals("Vrischika", res.getD9Positions().get("Mercury").getRashi());
        assertEquals("Kanya", res.getD9Positions().get("Jupiter").getRashi());
        assertEquals("Vrishabha", res.getD9Positions().get("Venus").getRashi());
        assertEquals("Kanya", res.getD9Positions().get("Saturn").getRashi());
        assertEquals("Karka", res.getD9Positions().get("Rahu").getRashi());
        assertEquals("Makara", res.getD9Positions().get("Ketu").getRashi());
    }
}
