package org.vedic.astro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.vedic.astro.dto.*;
import org.vedic.astro.model.DasaPeriod;
import org.vedic.astro.service.GeminiPredictionService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DailyBalanValidationTest {

    @Autowired
    private GeminiPredictionService predictionService;

    @Test
    @DisplayName("1. Native: Adithiyan (Tula Lagna, Mesha Moon) - Daily Balan & Chandrashtama")
    public void testAdithiyanDailyBalan() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1995, 7, 19, 13, 10, 0, 12.9165, 79.1325, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("Tula")
                        .rashi("Mesha")
                        .nakshatra("Bharani")
                        .build())
                .currentDasaTimeline(List.of(
                        DasaPeriod.builder()
                                .planetName("Rahu")
                                .startDate(LocalDate.of(2015, 1, 1))
                                .endDate(LocalDate.of(2033, 1, 1))
                                .bhukthis(List.of(
                                        DasaPeriod.BhukthiPeriod.builder()
                                                .planetName("Saturn")
                                                .startDate(LocalDate.of(2025, 1, 1))
                                                .endDate(LocalDate.of(2027, 1, 1))
                                                .build()
                                ))
                                .build()
                ))
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-15")
                .language("ta")
                .build();

        String prompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 15));
        assertNotNull(prompt);
        assertTrue(prompt.contains("dailyNarrative"));
        assertTrue(prompt.contains("dailyRemedy"));
        assertTrue(prompt.contains("Mesha"));
        assertTrue(prompt.contains("Bharani"));

        // Verify Chandrashtama when transit moon is Vrishchika
        DailyPanchangamDTO panchangamChandrashtama = new DailyPanchangamDTO(
                "2026-08-15",
                "06:00 AM",
                "06:30 PM",
                "12:00 PM",
                "11:30 PM",
                new DailyPanchangamDTO.PanchangamElementDTO(8, "Shukla Ashtami", "சுக்ல அஷ்டமி", "04:00 AM", null, null, null),
                new DailyPanchangamDTO.PanchangamElementDTO(17, "Anuradha", "அனுஷம்", "05:00 AM", null, null, null),
                new DailyPanchangamDTO.PanchangamElementDTO(25, "Brahma", "பிரம்ம", "03:00 AM", null, null, null),
                new DailyPanchangamDTO.PanchangamElementDTO(4, "Kaulava", "கௌலவ", "04:00 PM", null, null, null),
                "Vrishchika",
                null, null, null, null, null, null, null, null,
                List.of("Bharani", "Aswini", "Krittika"),
                2, 1.0, true, false, false, false, false, null, null
        );

        String promptWithChandrashtama = predictionService.constructDailyAstrologicalPrompt(req, panchangamChandrashtama, LocalDate.of(2026, 8, 15));
        assertTrue(promptWithChandrashtama.contains("\"chandrashtamaActive\" : true"));
    }

    @Test
    @DisplayName("2. Native: Uthayasri (Dhanus Lagna, Vrishchika Moon) - Daily Balan Synthesis")
    public void testUthayasriDailyBalan() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Uthayasri", 2002, 8, 17, 15, 15, 0, 11.9401, 79.4861, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("Dhanus")
                        .rashi("Vrishchika")
                        .nakshatra("Jyeshtha")
                        .build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-15")
                .language("ta")
                .build();

        String prompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 15));
        assertNotNull(prompt);
        assertTrue(prompt.contains("Vrishchika"));
        assertTrue(prompt.contains("Jyeshtha"));
    }

    @Test
    @DisplayName("3. Native: Padmasri (Makara Lagna, Dhanus Moon) - Daily Balan Synthesis")
    public void testPadmasriDailyBalan() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Padmasri", 2001, 7, 31, 19, 30, 0, 11.9401, 79.4861, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("Makara")
                        .rashi("Dhanus")
                        .nakshatra("Moola")
                        .build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-15")
                .language("hi")
                .build();

        String prompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 15));
        assertNotNull(prompt);
        assertTrue(prompt.contains("Dhanus"));
        assertTrue(prompt.contains("Moola"));
    }

    @Test
    @DisplayName("4. Native: Deepanathan (Makara Lagna, Makara Moon) - Daily Balan Synthesis")
    public void testDeepanathanDailyBalan() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Deepanathan", 1969, 4, 11, 2, 50, 0, 12.2253, 79.0747, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("Makara")
                        .rashi("Makara")
                        .nakshatra("Sravana")
                        .build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-15")
                .language("te")
                .build();

        String prompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 15));
        assertNotNull(prompt);
        assertTrue(prompt.contains("Makara"));
        assertTrue(prompt.contains("Sravana"));
    }

    @Test
    @DisplayName("5. Native: Mahaveer (Mesha Lagna, Cancer Moon) - Daily Balan Synthesis")
    public void testMahaveerDailyBalan() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Mahaveer", 2024, 4, 18, 6, 37, 0, 12.9165, 79.1325, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("Mesha")
                        .rashi("Kataka")
                        .nakshatra("Ashlesha")
                        .build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-15")
                .language("kn")
                .build();

        String prompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 15));
        assertNotNull(prompt);
        assertTrue(prompt.contains("Kataka"));
        assertTrue(prompt.contains("Ashlesha"));
    }

    @Test
    @DisplayName("6. Deterministic Daily Anchors across all 6 languages")
    public void testDeterministicDailyAnchorsAllSixLanguages() {
        String[] languages = {"en", "ta", "hi", "te", "kn", "ml"};
        LocalDate[] dates = {
                LocalDate.of(2026, 8, 9),  // Sunday
                LocalDate.of(2026, 8, 10), // Monday
                LocalDate.of(2026, 8, 11), // Tuesday
                LocalDate.of(2026, 8, 12), // Wednesday
                LocalDate.of(2026, 8, 13), // Thursday
                LocalDate.of(2026, 8, 14), // Friday
                LocalDate.of(2026, 8, 15)  // Saturday
        };

        for (String lang : languages) {
            for (LocalDate date : dates) {
                GeminiPredictionService.DeterministicDailyAnchors anchors =
                        GeminiPredictionService.calculateDeterministicAnchors(date, lang);

                assertNotNull(anchors, "Anchors null for lang: " + lang + " date: " + date);
                assertNotNull(anchors.getVaraLord(), "varaLord null for lang: " + lang);
                assertFalse(anchors.getVaraLord().isBlank(), "varaLord blank for lang: " + lang);
                assertNotNull(anchors.getLuckyColor(), "luckyColor null for lang: " + lang);
                assertNotNull(anchors.getLuckyNumber(), "luckyNumber null for lang: " + lang);
                assertNotNull(anchors.getFavorableDirection(), "favorableDirection null for lang: " + lang);
                assertNotNull(anchors.getAuspiciousTimeWindow(), "auspiciousTimeWindow null for lang: " + lang);
            }
        }
    }

    @Test
    @DisplayName("7. Offline Rule-Based Daily Balan generates unified single narrative")
    public void testOfflineDailyBalanGeneratesUnifiedNarrative() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1995, 7, 19, 13, 10, 0, 12.9165, 79.1325, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder()
                        .lagna("Tula")
                        .rashi("Mesha")
                        .nakshatra("Bharani")
                        .build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-15")
                .language("ta")
                .build();

        DailyBalanDTO offlineBalan = predictionService.generateOfflineRuleBasedDailyBalan(req, null, LocalDate.of(2026, 8, 15));
        assertNotNull(offlineBalan);
        assertTrue(offlineBalan.isEnabled());
        assertNotNull(offlineBalan.getDailyNarrative());
        assertTrue(offlineBalan.getDailyNarrative().contains("இன்றைய கோச்சார சந்திரன்"));
        assertNotNull(offlineBalan.getDailyRemedy());
        assertNotNull(offlineBalan.getLuckyColor());
        assertNotNull(offlineBalan.getLuckyNumber());
    }
}
