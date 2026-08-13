package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.vedic.astro.dto.*;
import org.vedic.astro.service.GeminiPredictionService;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GeminiPredictionServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeminiPredictionService predictionService;

    @Test
    public void testPromptConstructionWithoutPrecalculatedDiagnostics() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Vrishabha").nakshatra("Rohini").build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .thithi("Shukla - Dvitiya")
                .yogam("Siddha")
                .karanam("Bava")
                .d1Chart(Collections.emptyList())
                .currentDasaTimeline(Collections.emptyList())
                .structuralDiagnostics(DiagnosticsDTO.builder()
                        .activeYogas(Collections.emptyList())
                        .discoveredDoshams(Collections.emptyList())
                        .build())
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Ramesh"));
        assertTrue(prompt.contains("Mesha"));
        assertTrue(prompt.contains("Rohini"));
        assertTrue(prompt.contains("tamil") || prompt.contains("ta") || prompt.contains("தமிழ்"));
        // Pre-calculated structural diagnostics should NOT be passed to Gemini
        assertFalse(prompt.contains("Diagnostics:"));
        assertTrue(prompt.contains("detailedPrediction"));
        assertTrue(prompt.contains("Ayurdaya Determination") || prompt.contains("ஆயுள் நிர்ணயம்"));
        assertTrue(prompt.contains("Career, Business & Wealth"));
        assertTrue(prompt.contains("Health & Vitality Realities"));
        assertTrue(prompt.contains("Family, Marriage & Progeny"));
        assertTrue(prompt.contains("Parents, Elders & Mindset"));
    }

    @Test
    public void testRasiAndBhavaDisambiguationWithNonAriesLagna() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Suresh", 1990, 8, 20, 10, 15, 0, 13.0827, 80.2707, "LAHIRI");

        // Lagna in Taurus (Sign 2 / Vrishabha), Sun in Leo (Sign 5 / Simha), Moon in Aries (Sign 1 / Mesha)
        ChartResponseDTO.PositionDetail lagnaPos = ChartResponseDTO.PositionDetail.builder()
                .planetKey("LAGNA").displayName("Lagna").signNumber(2).rashiName("Vrishabha").degreeInSign(12.5).build();
        ChartResponseDTO.PositionDetail sunPos = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SUN").displayName("Sun").signNumber(5).rashiName("Simha").degreeInSign(4.2).build();
        ChartResponseDTO.PositionDetail moonPos = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MOON").displayName("Moon").signNumber(1).rashiName("Mesha").degreeInSign(18.0).build();

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Vrishabha").rashi("Mesha").nakshatra("Bharani").build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .d1Chart(java.util.List.of(lagnaPos, sunPos, moonPos))
                .currentDasaTimeline(Collections.emptyList())
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);

        // Verify D1 Rasi formatting uses Rasi, NOT H
        assertTrue(prompt.contains("D1[Rasi-ZodiacSigns]:"));
        assertTrue(prompt.contains("Sun:Simha(Rasi5@4.2°)"));
        assertTrue(prompt.contains("Moon:Mesha(Rasi1@18.0°)"));
        assertFalse(prompt.contains("Sun:Simha(H5"));

        // Verify Bhava (House) from Taurus (Sign 2) Lagna:
        // Lagna in Sign 2 -> House 1
        // Sun in Sign 5 -> House 4 ((5 - 2 + 12) % 12 + 1 = 4)
        // Moon in Sign 1 -> House 12 ((1 - 2 + 12) % 12 + 1 = 12)
        assertTrue(prompt.contains("Bhava[Houses-From-Lagna]:"));
        assertTrue(prompt.contains("Lagna:House1(Vrishabha)"));
        assertTrue(prompt.contains("Sun:House4(Simha)"));
        assertTrue(prompt.contains("Moon:House12(Mesha)"));

        // Verify system instructions contain Rasi vs Bhava disambiguation rules
        String systemInstruction = predictionService.constructSystemInstruction("ta");
        assertTrue(systemInstruction.contains("CRITICAL ASTROLOGICAL INTERPRETATION RULES"));
        assertTrue(systemInstruction.contains("Rasi"));
        assertTrue(systemInstruction.contains("Bhava"));
    }

    @Test
    public void testUnavailableResponseWhenDisabledOrFails() {
        PredictionResponseDTO lifeResp = predictionService.createUnavailableLifeResponse("ta");
        assertNotNull(lifeResp);
        assertFalse(lifeResp.isEnabled());
        assertTrue(lifeResp.getMessage().contains("கிடைக்கவில்லை"));

        DailyBalanDTO dailyResp = predictionService.createUnavailableDailyResponse("en", "2026-08-10");
        assertNotNull(dailyResp);
        assertFalse(dailyResp.isEnabled());
        assertEquals("2026-08-10", dailyResp.getTargetDate());
        assertTrue(dailyResp.getMessage().contains("currently unavailable"));
    }

    @Test
    public void testDeterministicDailyAnchors() {
        LocalDate date = LocalDate.of(2026, 8, 10); // Monday
        GeminiPredictionService.DeterministicDailyAnchors anchors = GeminiPredictionService.calculateDeterministicAnchors(date, "ta");

        assertNotNull(anchors);
        assertTrue(anchors.getVaraLord().contains("சந்திரன்"));
        assertTrue(anchors.getLuckyColor().contains("வெள்ளை"));
        assertEquals("2 & 7", anchors.getLuckyNumber());
        assertTrue(anchors.getFavorableDirection().contains("வடமேற்கு"));
        assertTrue(anchors.getAuspiciousTimeWindow().contains("06:00"));
    }

    @Test
    public void testTokenUsageParsingAndCostCalculation() {
        String mockGeminiResponse = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{\\"overallSummary\\":\\"உயர்ந்த அறிவும் ராஜ யோகமும் நிறைந்த ஜாதகம்.\\",\\"aiYogas\\":[],\\"aiDoshams\\":[],\\"pastKeyPhases\\":[],\\"lifetimePredictions\\":[]}"
                          }
                        ]
                      }
                    }
                  ],
                  "usageMetadata": {
                    "promptTokenCount": 1500,
                    "candidatesTokenCount": 800,
                    "totalTokenCount": 2300
                  }
                }
                """;

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI"))
                .chartData(ChartUiResponseDTO.builder().build())
                .language("ta")
                .build();

        PredictionResponseDTO parsed = predictionService.parseGeminiResponse(mockGeminiResponse, req);
        assertNotNull(parsed);
        assertTrue(parsed.isEnabled());
        assertNotNull(parsed.getTokenUsage());
        assertEquals(1500, parsed.getTokenUsage().getPromptTokens());
        assertEquals(800, parsed.getTokenUsage().getCompletionTokens());
        assertEquals(2300, parsed.getTokenUsage().getTotalTokens());
        assertTrue(parsed.getTokenUsage().getEstimatedCostUsd() > 0);
        assertTrue(parsed.getTokenUsage().getEstimatedCostInr() > 0);
    }

    @Test
    public void testDailyBalanPromptConstructionAndCost() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Vrishabha").nakshatra("Rohini").build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .targetDate("2026-08-10")
                .language("ta")
                .build();

        String dailyPrompt = predictionService.constructDailyAstrologicalPrompt(req, null, LocalDate.of(2026, 8, 10));
        assertNotNull(dailyPrompt);
        assertTrue(dailyPrompt.contains("Fixed Astrological Anchors"));
        assertTrue(dailyPrompt.contains("Vara Lord"));

        String mockDailyJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "{\\"generalOutlook\\":\\"இன்று நல்ல நாள்.\\",\\"careerWork\\":\\"பணியில் முன்னேற்றம்.\\",\\"financeWealth\\":\\"தனவரவு உண்டு.\\",\\"healthVitality\\":\\"உடல் நலம் நன்று.\\",\\"relationshipFamily\\":\\"குடும்ப மகிழ்ச்சி.\\",\\"dailyRemedy\\":\\"விநாயகர் வழிபாடு.\\"}"
                          }
                        ]
                      }
                    }
                  ],
                  "usageMetadata": {
                    "promptTokenCount": 600,
                    "candidatesTokenCount": 200,
                    "totalTokenCount": 800
                  }
                }
                """;

        DailyBalanDTO parsedDaily = predictionService.parseDailyGeminiResponse(mockDailyJson, req, null, "2026-08-10");
        assertNotNull(parsedDaily);
        assertTrue(parsedDaily.isEnabled());
        assertEquals("2026-08-10", parsedDaily.getTargetDate());
        assertNotNull(parsedDaily.getTokenUsage());
        assertEquals(800, parsedDaily.getTokenUsage().getTotalTokens());
        assertTrue(parsedDaily.getTokenUsage().getEstimatedCostUsd() > 0);
        assertTrue(parsedDaily.getTokenUsage().getEstimatedCostInr() > 0);
        assertTrue(parsedDaily.getLuckyColor().contains("வெள்ளை"));
        assertEquals("2 & 7", parsedDaily.getLuckyNumber());
    }

    @Test
    public void testEncryptedApiKeyResolution() {
        org.vedic.astro.config.GeminiProperties props = new org.vedic.astro.config.GeminiProperties();
        props.setApiKey("enc:dGVzdC1rZXktMTIzNDU=");
        assertEquals("test-key-12345", props.getResolvedApiKey());
        assertTrue(props.isFeatureEnabled());
        assertEquals(0.4, props.getTemperature(), 0.001);
        assertEquals(1024, props.getThinkingBudget());

        props.setThinkingBudget(2048);
        props.setTemperature(0.5);
        assertEquals(2048, props.getThinkingBudget());
        assertEquals(0.5, props.getTemperature(), 0.001);
    }

    @Test
    public void testMatchingPromptConstructionAndDirectives() {
        BirthDetailsDTO boy = new BirthDetailsDTO("Karthik", 1992, 4, 18, 9, 30, 0, 13.0827, 80.2707, "LAHIRI");
        BirthDetailsDTO girl = new BirthDetailsDTO("Priya", 1995, 8, 22, 14, 15, 0, 13.0827, 80.2707, "LAHIRI");

        org.vedic.astro.matching.dto.MatchingRequestDTO req = new org.vedic.astro.matching.dto.MatchingRequestDTO(
                boy, girl, org.vedic.astro.matching.MatchingType.ASHTA_KOOTA, org.vedic.astro.matching.StrictnessLevel.MODERATE
        );

        org.vedic.astro.matching.dto.MatchingResponseDTO classical = org.vedic.astro.matching.dto.MatchingResponseDTO.builder()
                .totalScore(28.0)
                .maxScore(36.0)
                .percentage(77.8)
                .verdict("Good")
                .boyProfile(ChartUiResponseDTO.builder()
                        .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Thula").nakshatra("Swati").nakshatraPada(2).build())
                        .build())
                .girlProfile(ChartUiResponseDTO.builder()
                        .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Karka").rashi("Mithuna").nakshatra("Ardra").nakshatraPada(3).build())
                        .build())
                .build();

        String prompt = predictionService.constructMatchingPrompt(req, classical);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Karthik"));
        assertTrue(prompt.contains("Priya"));
        assertTrue(prompt.contains("ASHTA_KOOTA"));
        assertTrue(prompt.contains("emotionalMentalHarmony"));
        assertTrue(prompt.contains("healthLongevityNadi"));
        assertTrue(prompt.contains("careerFinancialSynergy"));
        assertTrue(prompt.contains("progenyFamilyLineage"));
        assertTrue(prompt.contains("doshaPapasamyaParity"));

        org.vedic.astro.matching.dto.MatchingAiPredictionDTO unavail = predictionService.createUnavailableMatchingResponse("ta");
        assertNotNull(unavail);
        assertFalse(unavail.isEnabled());
        assertTrue(unavail.getMessage().contains("கிடைக்கவில்லை"));
    }

    @Test
    public void testLifetimePromptIncludesPlanetaryDignities() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");

        // Sun in Aries (Sign 1) -> Exalted
        ChartResponseDTO.PositionDetail sun = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SUN").displayName("Sun").signNumber(1).rashiName("Mesha").degreeInSign(10.0).build();
        // Saturn in Aries (Sign 1) -> Debilitated, and Combust (close to Sun at 12°)
        ChartResponseDTO.PositionDetail saturn = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SATURN").displayName("Saturn").signNumber(1).rashiName("Mesha").degreeInSign(12.0).build();
        // Mars in Aries (Sign 1) -> Own Sign
        ChartResponseDTO.PositionDetail mars = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MARS").displayName("Mars").signNumber(1).rashiName("Mesha").degreeInSign(25.0).build();

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Mesha").nakshatra("Ashwini").build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .d1Chart(java.util.List.of(sun, saturn, mars))
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);
        assertTrue(prompt.contains("D1[Rasi-ZodiacSigns]:"));
        assertTrue(prompt.contains("Sun:Mesha(Rasi1@10.0°)[Exalted]"));
        assertTrue(prompt.contains("Saturn:Mesha(Rasi1@12.0°)[Debilitated][Combust]"));
        assertTrue(prompt.contains("Mars:Mesha(Rasi1@25.0°)[Own]"));
    }

    @Test
    public void testLifetimePromptIncludesDetailedBhukthisAndDiagnostics() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");

        org.vedic.astro.model.DasaPeriod.BhukthiPeriod bhukthi1 = org.vedic.astro.model.DasaPeriod.BhukthiPeriod.builder()
                .planetName("Venus").startDate(LocalDate.of(2024, 1, 1)).endDate(LocalDate.of(2026, 5, 1)).build();
        org.vedic.astro.model.DasaPeriod.BhukthiPeriod bhukthi2 = org.vedic.astro.model.DasaPeriod.BhukthiPeriod.builder()
                .planetName("Sun").startDate(LocalDate.of(2026, 5, 2)).endDate(LocalDate.of(2027, 5, 1)).build();

        org.vedic.astro.model.DasaPeriod dasa = org.vedic.astro.model.DasaPeriod.builder()
                .planetName("Jupiter")
                .startDate(LocalDate.of(2020, 1, 1))
                .endDate(LocalDate.of(2036, 1, 1))
                .bhukthis(java.util.List.of(bhukthi1, bhukthi2))
                .build();

        DiagnosticsDTO diag = DiagnosticsDTO.builder()
                .activeYogas(java.util.List.of(DiagnosticsDTO.YogaDetail.builder().name("Gajakesari Yoga").description("Jupiter-Moon Kendra").build()))
                .discoveredDoshams(java.util.List.of(DiagnosticsDTO.DoshaDetail.builder().name("Sevvai Dosha").detected(true).nullified(true).nullificationReason("Jupiter Aspect").build()))
                .build();

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Mesha").nakshatra("Ashwini").build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .currentDasaTimeline(java.util.List.of(dasa))
                .structuralDiagnostics(diag)
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Vimshottari Dasa & Bhukthi Sub-Periods:"));
        assertTrue(prompt.contains("Jupiter Mahadasa"));
        assertTrue(prompt.contains("Jupiter-Venus Bhukthi"));
        assertTrue(prompt.contains("Jupiter-Sun Bhukthi"));
        assertTrue(prompt.contains("Pre-Calculated Yogas:"));
        assertTrue(prompt.contains("Gajakesari Yoga"));
        assertTrue(prompt.contains("Evaluated Doshams:"));
        assertTrue(prompt.contains("Sevvai Dosha [Nullified: Jupiter Aspect]"));
    }

    @Test
    public void testDailyPromptIncludesTarabalamAndGocharaHouse() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Mesha").nakshatra("Ashwini").build())
                .build();

        DailyBalanRequestDTO req = DailyBalanRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        // Transit Moon in Kanya (Virgo / Sign 6) with Hasta Nakshatra (Star 13)
        // From Ashwini (Star 1) to Hasta (Star 13): (13 - 1 + 27) % 9 + 1 = 12 % 9 + 1 = 4 (Kshema Tara)
        // From Mesha (Sign 1) to Kanya (Sign 6): (6 - 1 + 12) % 12 + 1 = 6 (6th House)
        DailyPanchangamDTO panchangam = new DailyPanchangamDTO(
                "2026-08-10", "06:00", "18:00", "07:00", "19:00",
                null,
                new DailyPanchangamDTO.PanchangamElementDTO(13, "Hasta", "அஸ்தம்", "18:00", null, null, null),
                null, null,
                "Kanya",
                null, null, null, null, null, null, null, null,
                java.util.List.of(),
                0, 0.0, false, false, false, false, false, null, null
        );

        String prompt = predictionService.constructDailyAstrologicalPrompt(req, panchangam, LocalDate.of(2026, 8, 10));
        assertNotNull(prompt);
        assertTrue(prompt.contains("Tarabalam:"));
        assertTrue(prompt.contains("க்ஷேம தாரை (4/9"));
        assertTrue(prompt.contains("Gochara Moon from Janma Rasi: House 6"));
    }

    @Test
    public void testMatchingPromptIncludesD9NavamsaPositions() {
        BirthDetailsDTO boy = new BirthDetailsDTO("Karthik", 1992, 4, 18, 9, 30, 0, 13.0827, 80.2707, "LAHIRI");
        BirthDetailsDTO girl = new BirthDetailsDTO("Priya", 1995, 8, 22, 14, 15, 0, 13.0827, 80.2707, "LAHIRI");

        org.vedic.astro.matching.dto.MatchingRequestDTO req = new org.vedic.astro.matching.dto.MatchingRequestDTO(
                boy, girl, org.vedic.astro.matching.MatchingType.ASHTA_KOOTA, org.vedic.astro.matching.StrictnessLevel.MODERATE
        );

        ChartResponseDTO.PositionDetail boyD9Sun = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SUN").displayName("Sun").rashiName("Simha").build();
        ChartResponseDTO.PositionDetail girlD9Moon = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MOON").displayName("Moon").rashiName("Vrishabha").build();

        org.vedic.astro.matching.dto.MatchingResponseDTO classical = org.vedic.astro.matching.dto.MatchingResponseDTO.builder()
                .totalScore(28.0)
                .maxScore(36.0)
                .percentage(77.8)
                .verdict("Good")
                .boyProfile(ChartUiResponseDTO.builder()
                        .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Thula").nakshatra("Swati").nakshatraPada(2).build())
                        .d9Chart(java.util.List.of(boyD9Sun))
                        .build())
                .girlProfile(ChartUiResponseDTO.builder()
                        .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Karka").rashi("Mithuna").nakshatra("Ardra").nakshatraPada(3).build())
                        .d9Chart(java.util.List.of(girlD9Moon))
                        .build())
                .build();

        String prompt = predictionService.constructMatchingPrompt(req, classical);
        assertNotNull(prompt);
        assertTrue(prompt.contains("Boy-D9[Navamsa]:"));
        assertTrue(prompt.contains("Sun:Simha"));
        assertTrue(prompt.contains("Girl-D9[Navamsa]:"));
        assertTrue(prompt.contains("Moon:Vrishabha"));
    }
}
