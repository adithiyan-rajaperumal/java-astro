package org.vedic.astro;
import org.junit.jupiter.api.Test;
import de.thmac.swisseph.SwissEph;
import de.thmac.swisseph.SweConst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SuryaSiddhantaAyanamsaTest {
    @Autowired
    private SwissEph swissEph;

    @Test
    public void testAyanamsa() {
        double julianDay = 2449918.0; // Approx July 19 1995
        
        swissEph.swe_set_sid_mode(21, 0, 0); // Native Surya Siddhanta
        double ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay);
        System.out.println("Native Surya Siddhanta Ayanamsa (mode 21) = " + ayanamsa);

        swissEph.swe_set_sid_mode(SweConst.SE_SIDM_USER, 2451545.0, 22.50608611); // JHora User Match
        double ayanamsaUser = swissEph.swe_get_ayanamsa_ut(julianDay);
        System.out.println("User Match Surya Siddhanta Ayanamsa = " + ayanamsaUser);
    }
}
