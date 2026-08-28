package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class Panchangam120DaysChennaiValidationTest {

    @Autowired
    private DailyPanchangamService dailyPanchangamService;

    private static final double CHENNAI_LAT = 13.0827;
    private static final double CHENNAI_LON = 80.2707;

    @Test
    @DisplayName("Verify 120-Day Continuous Daily Panchangam & Advanced Muhurtham Calculations for Chennai")
    public void testContinuous120DaysChennaiPanchangam() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        int totalDays = 120;

        int sankrantiCount = 0;
        int guruMoudhyaCount = 0;
        int sukraMoudhyaCount = 0;
        int subhaMuhurthamCount = 0;
        int thithiSoonyaCount = 0;
        int vyatipataCount = 0;
        int vaidhritiCount = 0;

        List<String> subhaMuhurthamDates = new ArrayList<>();

        for (int i = 0; i < totalDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            PanchangamRequestDTO req = new PanchangamRequestDTO(
                currentDate.toString(),
                CHENNAI_LAT,
                CHENNAI_LON,
                "LAHIRI",
                "ta"
            );

            DailyPanchangamDTO dto = dailyPanchangamService.calculateDailyPanchangam(req);

            // Core Astronomical Assertions
            assertNotNull(dto, "DTO must not be null for date: " + currentDate);
            assertEquals(currentDate.toString(), dto.date());
            assertNotNull(dto.sunrise(), "Sunrise null on: " + currentDate);
            assertNotNull(dto.sunset(), "Sunset null on: " + currentDate);
            assertTrue(dto.sunrise().contains("AM") || dto.sunrise().contains("PM"), "Sunrise invalid: " + dto.sunrise());
            assertTrue(dto.sunset().contains("AM") || dto.sunset().contains("PM"), "Sunset invalid: " + dto.sunset());

            // 5 Panchangam Limbs
            assertNotNull(dto.thithi(), "Thithi null on: " + currentDate);
            assertTrue(dto.thithi().number() >= 1 && dto.thithi().number() <= 30, "Thithi out of bounds: " + dto.thithi().number());

            assertNotNull(dto.nakshatra(), "Nakshatra null on: " + currentDate);
            assertTrue(dto.nakshatra().number() >= 1 && dto.nakshatra().number() <= 27, "Nakshatra out of bounds: " + dto.nakshatra().number());

            assertNotNull(dto.yogam(), "Yogam null on: " + currentDate);
            assertTrue(dto.yogam().number() >= 1 && dto.yogam().number() <= 27, "Yogam out of bounds: " + dto.yogam().number());

            assertNotNull(dto.karanam(), "Karanam null on: " + currentDate);
            assertTrue(dto.karanam().number() >= 1 && dto.karanam().number() <= 60, "Karanam out of bounds: " + dto.karanam().number());

            assertNotNull(dto.rashi(), "Rashi null on: " + currentDate);

            // Horais and Time Slots
            assertNotNull(dto.horais(), "Horais null on: " + currentDate);
            assertEquals(24, dto.horais().size(), "Must have 24 horais on: " + currentDate);

            assertNotNull(dto.raghuKalam(), "Rahu Kalam null on: " + currentDate);
            assertFalse(dto.raghuKalam().isEmpty(), "Rahu Kalam empty on: " + currentDate);

            assertNotNull(dto.emagandam(), "Yamagandam null on: " + currentDate);
            assertFalse(dto.emagandam().isEmpty(), "Yamagandam empty on: " + currentDate);

            assertNotNull(dto.kulikai(), "Kulikai null on: " + currentDate);
            assertFalse(dto.kulikai().isEmpty(), "Kulikai empty on: " + currentDate);

            assertNotNull(dto.gowriNallaNeram(), "Gowri Nalla Neram null on: " + currentDate);
            assertFalse(dto.gowriNallaNeram().isEmpty(), "Gowri Nalla Neram empty on: " + currentDate);

            // Netram & Jeevan bounds
            assertTrue(dto.netram() >= 0 && dto.netram() <= 2, "Netram out of bounds: " + dto.netram());
            assertTrue(dto.jeevan() >= 0.0 && dto.jeevan() <= 1.0, "Jeevan out of bounds: " + dto.jeevan());

            // Track Vedic Muhurtham Enhancements
            if (dto.sankrantiDay()) {
                sankrantiCount++;
                assertFalse(dto.muhurthamDay(), "Sankranti day must NOT be a Subha Muhurtham day on: " + currentDate);
            }

            if (dto.guruMoudhya()) {
                guruMoudhyaCount++;
                assertFalse(dto.muhurthamDay(), "Guru Moudhya day must NOT be a Subha Muhurtham day on: " + currentDate);
            }

            if (dto.sukraMoudhya()) {
                sukraMoudhyaCount++;
                assertFalse(dto.muhurthamDay(), "Sukra Moudhya day must NOT be a Subha Muhurtham day on: " + currentDate);
            }

            if (dto.thithiSoonya()) {
                thithiSoonyaCount++;
                assertFalse(dto.muhurthamDay(), "Thithi Soonya day must NOT be a Subha Muhurtham day on: " + currentDate);
            }

            if (dto.yogam().number() == 17) {
                vyatipataCount++;
                assertFalse(dto.muhurthamDay(), "Vyatipata Yoga day must NOT be a Subha Muhurtham day on: " + currentDate);
            }

            if (dto.yogam().number() == 27) {
                vaidhritiCount++;
                assertFalse(dto.muhurthamDay(), "Vaidhriti Yoga day must NOT be a Subha Muhurtham day on: " + currentDate);
            }

            if (dto.muhurthamDay()) {
                subhaMuhurthamCount++;
                subhaMuhurthamDates.add(currentDate.toString());
                assertNotNull(dto.muhurthamWindow(), "Muhurtham window must be populated on: " + currentDate);
                assertFalse(dto.muhurthamWindow().isBlank(), "Muhurtham window must not be blank on: " + currentDate);
            }
        }

        // Verify across 120 days that realistic astrological transitions occurred
        assertTrue(sankrantiCount >= 3, "Expected at least 3-4 monthly Solar Sankrantis in 120 days, found: " + sankrantiCount);
        assertTrue(thithiSoonyaCount > 0, "Expected Thithi Soonya days in 120 days, found: " + thithiSoonyaCount);
        assertTrue(subhaMuhurthamCount > 0, "Expected valid Subha Muhurtham days in 120 days, found: " + subhaMuhurthamCount);

        System.out.println("=== 120-Day Chennai Panchangam Validation Summary ===");
        System.out.println("Total Days Evaluated: " + totalDays);
        System.out.println("Sankranti Ingress Days: " + sankrantiCount);
        System.out.println("Guru Moudhya Days: " + guruMoudhyaCount);
        System.out.println("Sukra Moudhya Days: " + sukraMoudhyaCount);
        System.out.println("Thithi Soonya Days: " + thithiSoonyaCount);
        System.out.println("Vyatipata Yoga Days: " + vyatipataCount);
        System.out.println("Vaidhriti Yoga Days: " + vaidhritiCount);
        System.out.println("Subha Muhurtham Days (" + subhaMuhurthamCount + "): " + subhaMuhurthamDates);
    }
}
