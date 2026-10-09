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
            }

            if (dto.sukraMoudhya()) {
                sukraMoudhyaCount++;
            }

            if (dto.thithiSoonya()) {
                thithiSoonyaCount++;
            }

            if (dto.yogam().number() == 17) {
                vyatipataCount++;
                assertFalse(dto.muhurthamDay(), "Vyatipata Mahadosha must NOT be Subha Muhurtham on: " + currentDate);
            }

            if (dto.yogam().number() == 27) {
                vaidhritiCount++;
                assertFalse(dto.muhurthamDay(), "Vaidhriti Mahadosha must NOT be Subha Muhurtham on: " + currentDate);
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

    @Test
    @DisplayName("Compare Chennai vs Vellore Muhurtham Discrepancies")
    public void testChennaiVsVelloreMuhurthamComparison() {
        double VELLORE_LAT = 12.9165;
        double VELLORE_LON = 79.1325;
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        int totalDays = 120;

        int diffCount = 0;
        for (int i = 0; i < totalDays; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            PanchangamRequestDTO reqChennai = new PanchangamRequestDTO(
                currentDate.toString(), CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta"
            );
            PanchangamRequestDTO reqVellore = new PanchangamRequestDTO(
                currentDate.toString(), VELLORE_LAT, VELLORE_LON, "LAHIRI", "ta"
            );

            DailyPanchangamDTO ch = dailyPanchangamService.calculateDailyPanchangam(reqChennai);
            DailyPanchangamDTO vel = dailyPanchangamService.calculateDailyPanchangam(reqVellore);

            if (ch.muhurthamDay() != vel.muhurthamDay()) {
                diffCount++;
                System.out.println("DISCREPANCY on " + currentDate + ":");
                System.out.println("  Chennai: isMuhurtham=" + ch.muhurthamDay() + " (Sunrise=" + ch.sunrise() + ", Thithi=" + ch.thithi().name() + " (" + ch.thithi().number() + "), Nakshatra=" + ch.nakshatra().name() + " (" + ch.nakshatra().number() + "), Netram=" + ch.netram() + ", Jeevan=" + ch.jeevan() + ", Window=" + ch.muhurthamWindow() + ")");
                System.out.println("  Vellore: isMuhurtham=" + vel.muhurthamDay() + " (Sunrise=" + vel.sunrise() + ", Thithi=" + vel.thithi().name() + " (" + vel.thithi().number() + "), Nakshatra=" + vel.nakshatra().name() + " (" + vel.nakshatra().number() + "), Netram=" + vel.netram() + ", Jeevan=" + vel.jeevan() + ", Window=" + vel.muhurthamWindow() + ")");
            }
        }
        System.out.println("Total Chennai vs Vellore Muhurtham discrepancies: " + diffCount);
        assertEquals(0, diffCount, "Expected 0 Chennai vs Vellore Subha Muhurtham discrepancies across 120 days");
    }

    @Test
    @DisplayName("Verify authentic Tamil Muhurtham dates in Avani, Aippasi, and Karthigai")
    public void testAuthenticTamilMuhurthamDates() {
        // Avani authentic Muhurthams
        DailyPanchangamDTO avaniMula = dailyPanchangamService.calculateDailyPanchangam(
            new PanchangamRequestDTO("2026-08-23", CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta")
        );
        assertTrue(avaniMula.muhurthamDay(), "2026-08-23 (Avani 6, Mula Nakshatra) must be a Subha Muhurtham day");

        DailyPanchangamDTO avaniUttaraBhadra = dailyPanchangamService.calculateDailyPanchangam(
            new PanchangamRequestDTO("2026-08-30", CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta")
        );
        assertTrue(avaniUttaraBhadra.muhurthamDay(), "2026-08-30 (Avani 13, Uttara Bhadrapada) must be a Subha Muhurtham day");

        DailyPanchangamDTO avaniRevati = dailyPanchangamService.calculateDailyPanchangam(
            new PanchangamRequestDTO("2026-08-31", CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta")
        );
        assertTrue(avaniRevati.muhurthamDay(), "2026-08-31 (Avani 14, Revati) must be a Subha Muhurtham day");

        // Aippasi & Karthigai authentic Muhurthams
        DailyPanchangamDTO aippasiPushya = dailyPanchangamService.calculateDailyPanchangam(
            new PanchangamRequestDTO("2026-11-01", CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta")
        );
        assertTrue(aippasiPushya.muhurthamDay(), "2026-11-01 (Aippasi 15, Pushya) must be a Subha Muhurtham day");

        DailyPanchangamDTO karthigaiUttaraBhadra = dailyPanchangamService.calculateDailyPanchangam(
            new PanchangamRequestDTO("2026-11-20", CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta")
        );
        assertTrue(karthigaiUttaraBhadra.muhurthamDay(), "2026-11-20 (Karthigai 4, Uttara Bhadrapada) must be a Subha Muhurtham day");

        // Purattasi month days show as Subha Muhurtham with isPurattasiOrAadi caution flag
        DailyPanchangamDTO purattasiSample = dailyPanchangamService.calculateDailyPanchangam(
            new PanchangamRequestDTO("2026-10-01", CHENNAI_LAT, CHENNAI_LON, "LAHIRI", "ta")
        );
        assertTrue(purattasiSample.muhurthamDay(), "Genuinely auspicious day in Purattasi should be marked as Subha Muhurtham with caution");
        assertTrue(purattasiSample.isPurattasiOrAadi(), "Purattasi month day must have isPurattasiOrAadi=true");
    }
}


