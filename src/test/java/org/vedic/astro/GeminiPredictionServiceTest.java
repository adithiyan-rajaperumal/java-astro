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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GeminiPredictionServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeminiPredictionService predictionService;

    @Autowired
    private org.vedic.astro.config.GeminiProperties geminiProperties;

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
        // Pre-calculated structural diagnostics and ayurdayaProfile should NOT be passed in input JSON
        assertFalse(prompt.contains("Diagnostics:"));
        assertFalse(prompt.contains("\"ayurdayaProfile\""));
        assertFalse(prompt.contains("\"preCalculatedDiagnostics\""));
        assertTrue(prompt.contains("divisionalCharts"));
        assertTrue(prompt.contains("yearlyPredictions"));
        assertTrue(prompt.contains("aiLongevityAnalysis"));
        assertTrue(prompt.contains("personalityAndBehavior"));
        assertTrue(prompt.contains("retrospectivePastMilestones"));
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

        // Verify JSON input contains structured houseLordshipTable and planetaryMatrix
        assertTrue(prompt.contains("houseLordshipTable"));
        assertTrue(prompt.contains("planetaryMatrix"));
        assertTrue(prompt.contains("divisionalCharts"));

        // Verify Sun in Leo (Sign 5) -> House 4 from Taurus (Sign 2) Lagna
        assertTrue(prompt.contains("\"planet\" : \"Sun\""));
        assertTrue(prompt.contains("\"placedInD1Sign\" : \"Simha\""));
        assertTrue(prompt.contains("\"placedInD1House\" : 4"));

        // Verify Moon in Aries (Sign 1) -> House 12 from Taurus (Sign 2) Lagna
        assertTrue(prompt.contains("\"planet\" : \"Moon\""));
        assertTrue(prompt.contains("\"placedInD1Sign\" : \"Mesha\""));
        assertTrue(prompt.contains("\"placedInD1House\" : 12"));

        // Verify system instructions contain Rasi vs Bhava disambiguation and lordship rules
        String systemInstruction = predictionService.constructSystemInstruction("ta");
        assertTrue(systemInstruction.contains("CRITICAL ASTROLOGICAL INTERPRETATION & NOTATION RULES"));
        assertTrue(systemInstruction.contains("AUTONOMOUS AYURDAYA (LONGEVITY) CALCULATION"));
        assertTrue(systemInstruction.contains("UNIFORM COMPREHENSIVE DEPTH ACROSS ALL YEARS"));
    }

    @Test
    public void testDailyBalanDTOSerializationWithDailyNarrative() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        DailyBalanDTO dto = DailyBalanDTO.builder()
                .enabled(true)
                .targetDate("2026-08-15")
                .rasi("Mesha")
                .dailyNarrative("Today's transit Moon brings auspicious energy for career and wealth growth.")
                .dailyRemedy("Chant Gayatri Mantra 9 times.")
                .luckyColor("Ruby Red")
                .luckyNumber("1 & 4")
                .favorableDirection("East")
                .bestTimeWindow("07:30 AM - 09:00 AM")
                .build();
        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("dailyNarrative"));
        DailyBalanDTO deserialized = mapper.readValue(json, DailyBalanDTO.class);
        assertEquals("Today's transit Moon brings auspicious energy for career and wealth growth.", deserialized.getDailyNarrative());
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
        assertTrue(dailyPrompt.contains("fixedDailyAnchors"));
        assertTrue(dailyPrompt.contains("varaLord"));

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
        assertTrue(prompt.contains("planetaryMatrix"));
        assertTrue(prompt.contains("\"d1Dignity\" : \"EXALTED\""));
        assertTrue(prompt.contains("\"d1Dignity\" : \"DEBILITATED\""));
        assertTrue(prompt.contains("\"d1Dignity\" : \"OWN_SIGN\""));
        assertTrue(prompt.contains("\"isCombust\" : true"));
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

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Mesha").nakshatra("Ashwini").build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .currentDasaTimeline(java.util.List.of(dasa))
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);
        assertTrue(prompt.contains("vimshottariTimeline"));
        assertTrue(prompt.contains("\"dasa\" : \"Jupiter\""));
        assertTrue(prompt.contains("\"bhukthi\" : \"Venus\""));
        assertTrue(prompt.contains("\"bhukthi\" : \"Sun\""));
        assertTrue(prompt.contains("houseLordshipTable"));
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
        assertTrue(prompt.contains("todayGocharaAndPanchangam"));
        assertTrue(prompt.contains("tarabalam"));
        assertTrue(prompt.contains("transitMoonHouseFromJanmaRasi"));
        assertTrue(prompt.contains("\"transitMoonHouseFromJanmaRasi\" : 6"));
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
        assertTrue(prompt.contains("groomBoy"));
        assertTrue(prompt.contains("brideGirl"));
        assertTrue(prompt.contains("d9Navamsa"));
        assertTrue(prompt.contains("\"Sun\" : \"Simha\""));
        assertTrue(prompt.contains("\"Moon\" : \"Vrishabha\""));
    }

    @Test
    public void testSmokeTestPromptAccuracySagittariusLagnaWithMercuryJupiterCancer() throws Exception {
        // Native with Sagittarius (Sign 9 / Dhanus) Lagna
        BirthDetailsDTO birth = new BirthDetailsDTO("Adithiyan", 1996, 7, 25, 17, 45, 0, 13.0827, 80.2707, "LAHIRI");

        ChartResponseDTO.PositionDetail lagna = ChartResponseDTO.PositionDetail.builder()
                .planetKey("LAGNA").displayName("Lagna").signNumber(9).rashiName("Dhanus").degreeInSign(14.2).build();
        ChartResponseDTO.PositionDetail mercury = ChartResponseDTO.PositionDetail.builder()
                .planetKey("MERCURY").displayName("Mercury").signNumber(4).rashiName("Kataka").degreeInSign(8.5).build();
        ChartResponseDTO.PositionDetail jupiter = ChartResponseDTO.PositionDetail.builder()
                .planetKey("JUPITER").displayName("Jupiter").signNumber(4).rashiName("Kataka").degreeInSign(15.0).build();
        ChartResponseDTO.PositionDetail venus = ChartResponseDTO.PositionDetail.builder()
                .planetKey("VENUS").displayName("Venus").signNumber(2).rashiName("Vrishabha").degreeInSign(12.0).build();
        ChartResponseDTO.PositionDetail sun = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SUN").displayName("Sun").signNumber(1).rashiName("Mesha").degreeInSign(10.0).build();
        ChartResponseDTO.PositionDetail saturn = ChartResponseDTO.PositionDetail.builder()
                .planetKey("SATURN").displayName("Saturn").signNumber(10).rashiName("Makara").degreeInSign(20.0).build();

        org.vedic.astro.model.DasaPeriod.BhukthiPeriod bhukthi = org.vedic.astro.model.DasaPeriod.BhukthiPeriod.builder()
                .planetName("Venus").startDate(LocalDate.of(2024, 1, 1)).endDate(LocalDate.of(2026, 12, 31)).build();
        org.vedic.astro.model.DasaPeriod dasa = org.vedic.astro.model.DasaPeriod.builder()
                .planetName("Jupiter").startDate(LocalDate.of(2020, 1, 1)).endDate(LocalDate.of(2036, 1, 1))
                .bhukthis(java.util.List.of(bhukthi)).build();

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Dhanus").rashi("Vrishabha").nakshatra("Rohini").nakshatraPada(2).build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .thithi("Shukla - Dashami")
                .yogam("Shubha")
                .karanam("Taitila")
                .d1Chart(java.util.List.of(lagna, mercury, jupiter, venus, sun, saturn))
                .currentDasaTimeline(java.util.List.of(dasa))
                .structuralDiagnostics(DiagnosticsDTO.builder()
                        .activeYogas(java.util.List.of(DiagnosticsDTO.YogaDetail.builder().name("Gajakesari Yoga").description("Moon-Jupiter Kendra").impactLevel("High").build()))
                        .discoveredDoshams(Collections.emptyList())
                        .build())
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        // 1. Generate Lifetime Prompt
        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);

        // Extract JSON substring from prompt
        int jsonStart = prompt.indexOf("{");
        int jsonEnd = prompt.lastIndexOf("}") + 1;
        assertTrue(jsonStart > 0 && jsonEnd > jsonStart);
        String extractedJson = prompt.substring(jsonStart, prompt.indexOf("=== GENERATION DIRECTIVES ===")).trim();

        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(extractedJson);
        assertNotNull(root);

        // Verify Native Info
        assertEquals("Adithiyan", root.get("native").get("name").asText());
        assertEquals("Dhanus", root.get("native").get("janmaLagna").asText());
        assertEquals("Vrishabha", root.get("native").get("janmaRasi").asText());

        // Verify House Lordship Table for Sagittarius (Dhanus / Sign 9) Lagna
        com.fasterxml.jackson.databind.JsonNode houseLords = root.get("houseLordshipTable");
        assertNotNull(houseLords);
        assertEquals(12, houseLords.size());

        // House 1: Dhanus -> Jupiter
        assertEquals(1, houseLords.get(0).get("houseNumber").asInt());
        assertEquals("Dhanus", houseLords.get(0).get("signName").asText());
        assertEquals("Jupiter", houseLords.get(0).get("houseLord").asText());

        // House 6: Vrishabha -> Venus (Roga / Shatru lord), Occupants: Venus
        assertEquals(6, houseLords.get(5).get("houseNumber").asInt());
        assertEquals("Vrishabha", houseLords.get(5).get("signName").asText());
        assertEquals("Venus", houseLords.get(5).get("houseLord").asText());

        // House 10: Kanya -> Mercury (Karma / Rajya lord - NOT Venus!), Vacant
        assertEquals(10, houseLords.get(9).get("houseNumber").asInt());
        assertEquals("Kanya", houseLords.get(9).get("signName").asText());
        assertEquals("Mercury", houseLords.get(9).get("houseLord").asText());
        assertEquals(0, houseLords.get(9).get("occupantPlanets").size());
        assertTrue(houseLords.get(9).get("lordshipClarification").asText().contains("vacant house"));

        // House 5: Mesha -> Mars (5th Lord), Occupant: Sun
        assertEquals(5, houseLords.get(4).get("houseNumber").asInt());
        assertEquals("Mesha", houseLords.get(4).get("signName").asText());
        assertEquals("Mars", houseLords.get(4).get("houseLord").asText());
        assertTrue(houseLords.get(4).get("occupantPlanets").toString().contains("Sun"));
        assertTrue(houseLords.get(4).get("lordshipClarification").asText().contains("Mars is the sole lord"));

        // House 11: Thula -> Venus (Labha lord)
        assertEquals(11, houseLords.get(10).get("houseNumber").asInt());
        assertEquals("Tula", houseLords.get(10).get("signName").asText());
        assertEquals("Venus", houseLords.get(10).get("houseLord").asText());

        // Verify Planetary Matrix (D1, D9, rulesHouses, lordshipTitle, occupantRole, Dignities, Dosha)
        com.fasterxml.jackson.databind.JsonNode planets = root.get("planetaryMatrix");
        assertNotNull(planets);

        com.fasterxml.jackson.databind.JsonNode mercNode = null;
        com.fasterxml.jackson.databind.JsonNode jupNode = null;
        com.fasterxml.jackson.databind.JsonNode venNode = null;
        com.fasterxml.jackson.databind.JsonNode sunNode = null;

        for (com.fasterxml.jackson.databind.JsonNode p : planets) {
            String pName = p.get("planet").asText();
            if ("Mercury".equalsIgnoreCase(pName)) mercNode = p;
            if ("Jupiter".equalsIgnoreCase(pName)) jupNode = p;
            if ("Venus".equalsIgnoreCase(pName)) venNode = p;
            if ("Sun".equalsIgnoreCase(pName)) sunNode = p;
        }

        // Mercury in Cancer -> House 8, Dignity: NEUTRAL (NOT own sign!), Rules Houses [7, 10]
        assertNotNull(mercNode);
        assertEquals("Kataka", mercNode.get("placedInD1Sign").asText());
        assertEquals(8, mercNode.get("placedInD1House").asInt());
        assertEquals("NEUTRAL", mercNode.get("d1Dignity").asText());
        assertEquals("[7,10]", mercNode.get("rulesHouses").toString());
        assertTrue(mercNode.get("lordshipTitle").asText().contains("7th & 10th Lord"));
        assertTrue(mercNode.get("occupantRole").asText().contains("NOT the 8th Lord"));
        assertTrue(mercNode.get("primaryDosha").asText().contains("Tridosha"));

        // Jupiter in Cancer -> House 8, Dignity: EXALTED, Rules Houses [1, 4]
        assertNotNull(jupNode);
        assertEquals("Kataka", jupNode.get("placedInD1Sign").asText());
        assertEquals(8, jupNode.get("placedInD1House").asInt());
        assertEquals("EXALTED", jupNode.get("d1Dignity").asText());
        assertEquals("[1,4]", jupNode.get("rulesHouses").toString());
        assertTrue(jupNode.get("lordshipTitle").asText().contains("Lagnesha"));
        assertTrue(jupNode.get("occupantRole").asText().contains("NOT the 8th Lord"));
        assertTrue(jupNode.get("primaryDosha").asText().contains("Kapha"));

        // Venus in Taurus -> House 6, Dignity: OWN_SIGN, Rules Houses [6, 11]
        assertNotNull(venNode);
        assertEquals("Vrishabha", venNode.get("placedInD1Sign").asText());
        assertEquals(6, venNode.get("placedInD1House").asInt());
        assertEquals("OWN_SIGN", venNode.get("d1Dignity").asText());
        assertEquals("[6,11]", venNode.get("rulesHouses").toString());
        assertTrue(venNode.get("lordshipTitle").asText().contains("6th & 11th Lord"));
        assertTrue(venNode.get("occupantRole").asText().contains("Own House"));

        // Sun in Aries -> House 5, Dignity: EXALTED, Rules Houses [9]
        assertNotNull(sunNode);
        assertEquals("Mesha", sunNode.get("placedInD1Sign").asText());
        assertEquals(5, sunNode.get("placedInD1House").asInt());
        assertEquals("EXALTED", sunNode.get("d1Dignity").asText());
        assertEquals("[9]", sunNode.get("rulesHouses").toString());
        assertTrue(sunNode.get("lordshipTitle").asText().contains("9th Lord"));
        assertTrue(sunNode.get("occupantRole").asText().contains("NOT the 5th Lord"));
        assertTrue(sunNode.get("primaryDosha").asText().contains("Pitta"));

        // Verify Divisional Charts Matrix with Lagna & House tags
        com.fasterxml.jackson.databind.JsonNode vargas = root.get("divisionalCharts");
        assertNotNull(vargas);
        assertNotNull(vargas.get("D9_Navamsa_Dharma_Spouse"));
        assertNotNull(vargas.get("D10_Dasamsa_Career"));
        assertTrue(vargas.get("D9_Navamsa_Dharma_Spouse").has("Lagna"));

        // Verify that ayurdayaProfile and preCalculatedDiagnostics are NOT passed in prompt JSON to ensure autonomous AI calculation
        com.fasterxml.jackson.databind.JsonNode ayurdaya = root.get("ayurdayaProfile");
        assertNull(ayurdaya, "ayurdayaProfile must NOT be present in prompt JSON to ensure autonomous AI longevity evaluation");

        com.fasterxml.jackson.databind.JsonNode diag = root.get("preCalculatedDiagnostics");
        assertNull(diag, "preCalculatedDiagnostics must NOT be present in prompt JSON to ensure autonomous AI yoga/dosham evaluation");

        // Verify Pre-Computed Yearly Anchors exist and are accurate
        com.fasterxml.jackson.databind.JsonNode anchors = root.get("preComputedYearlyAnchors");
        assertNotNull(anchors, "preComputedYearlyAnchors must be present in prompt JSON");
        assertTrue(anchors.isArray() && anchors.size() > 0, "Anchors array must not be empty");

        // Check first anchor entry for Sagittarius Lagna: Lagna Lord = Jupiter
        com.fasterxml.jackson.databind.JsonNode firstAnchor = anchors.get(0);
        assertNotNull(firstAnchor);
        assertTrue(firstAnchor.get("lagnaLordReminder").asText().contains("Jupiter"),
                "Sagittarius Lagna must have Jupiter as Lagna Lord, got: " + firstAnchor.get("lagnaLordReminder").asText());
        assertTrue(firstAnchor.get("lagnaLordReminder").asText().contains("Dhanus"),
                "lagnaLordReminder must include Lagna rasi name");

        // Verify dasaLord and bhukthiLord sub-objects exist with required fields
        com.fasterxml.jackson.databind.JsonNode dasaLord = firstAnchor.get("dasaLord");
        com.fasterxml.jackson.databind.JsonNode bhukthiLord = firstAnchor.get("bhukthiLord");
        assertNotNull(dasaLord, "dasaLord anchor must exist");
        assertNotNull(bhukthiLord, "bhukthiLord anchor must exist");
        assertNotNull(dasaLord.get("planet"));
        assertNotNull(dasaLord.get("placedInBhava"));
        assertNotNull(dasaLord.get("rulesHouses"));
        assertNotNull(dasaLord.get("isLagnaLord"));
        assertNotNull(dasaLord.get("d1Dignity"));
        assertNotNull(bhukthiLord.get("planet"));
        assertNotNull(bhukthiLord.get("isLagnaLord"));
    }

    @Test
    public void testBuildPlanetAnchorWithCapricornLagna() {
        // Capricorn Lagna (sign 10): Lagna Lord = Saturn
        int lagnaSign = 10; // Makara
        String lagnaLord = org.vedic.astro.util.PlanetDignityUtils.getSignLord(lagnaSign);
        assertEquals("Saturn", lagnaLord, "Capricorn/Makara Lagna Lord must be Saturn");

        // Build a mock planetLookup
        java.util.Map<String, java.util.Map<String, Object>> planetLookup = new java.util.HashMap<>();

        java.util.Map<String, Object> saturnEntry = new java.util.LinkedHashMap<>();
        saturnEntry.put("planet", "Saturn");
        saturnEntry.put("placedInD1House", 2);
        saturnEntry.put("rulesHouses", java.util.List.of(1, 2));
        saturnEntry.put("d1Dignity", "OWN_SIGN");
        planetLookup.put("saturn", saturnEntry);

        java.util.Map<String, Object> venusEntry = new java.util.LinkedHashMap<>();
        venusEntry.put("planet", "Venus");
        venusEntry.put("placedInD1House", 6);
        venusEntry.put("rulesHouses", java.util.List.of(5, 10));
        venusEntry.put("d1Dignity", "NEUTRAL");
        planetLookup.put("venus", venusEntry);

        // Test Saturn anchor (should be isLagnaLord = true)
        java.util.Map<String, Object> saturnAnchor = GeminiPredictionService.buildPlanetAnchor(
                "Saturn", lagnaSign, lagnaLord, planetLookup);
        assertEquals("Saturn", saturnAnchor.get("planet"));
        assertEquals(2, saturnAnchor.get("placedInBhava"));
        assertEquals(java.util.List.of(1, 2), saturnAnchor.get("rulesHouses"));
        assertEquals("OWN_SIGN", saturnAnchor.get("d1Dignity"));
        assertEquals(true, saturnAnchor.get("isLagnaLord"),
                "Saturn must be isLagnaLord=true for Capricorn/Makara Lagna");

        // Test Venus anchor (should be isLagnaLord = false)
        java.util.Map<String, Object> venusAnchor = GeminiPredictionService.buildPlanetAnchor(
                "Venus", lagnaSign, lagnaLord, planetLookup);
        assertEquals("Venus", venusAnchor.get("planet"));
        assertEquals(6, venusAnchor.get("placedInBhava"));
        assertEquals(java.util.List.of(5, 10), venusAnchor.get("rulesHouses"));
        assertEquals("NEUTRAL", venusAnchor.get("d1Dignity"));
        assertEquals(false, venusAnchor.get("isLagnaLord"),
                "Venus must be isLagnaLord=false for Capricorn/Makara Lagna — the exact bug that caused the drift");

        // Test Rahu anchor (shadow node, not in planetLookup)
        java.util.Map<String, Object> rahuAnchor = GeminiPredictionService.buildPlanetAnchor(
                "Rahu", lagnaSign, lagnaLord, planetLookup);
        assertEquals("Rahu", rahuAnchor.get("planet"));
        assertEquals(0, rahuAnchor.get("placedInBhava"));  // Shadow node default
        assertEquals(false, rahuAnchor.get("isLagnaLord"));
    }

    @Test
    public void testFindDasaAndBhukthiForYear() {
        // Build test dasa timeline: Saturn Mahadasa 2060-2079, Venus Bhukthi 2070-2073
        java.util.List<org.vedic.astro.model.DasaPeriod> dasas = new java.util.ArrayList<>();
        org.vedic.astro.model.DasaPeriod saturn = org.vedic.astro.model.DasaPeriod.builder()
                .planetName("Saturn")
                .startDate(LocalDate.of(2060, 1, 1))
                .endDate(LocalDate.of(2079, 12, 31))
                .bhukthis(java.util.List.of(
                        org.vedic.astro.model.DasaPeriod.BhukthiPeriod.builder()
                                .planetName("Mercury")
                                .startDate(LocalDate.of(2060, 1, 1))
                                .endDate(LocalDate.of(2062, 8, 15))
                                .build(),
                        org.vedic.astro.model.DasaPeriod.BhukthiPeriod.builder()
                                .planetName("Venus")
                                .startDate(LocalDate.of(2070, 1, 1))
                                .endDate(LocalDate.of(2073, 3, 31))
                                .build()
                ))
                .build();
        dasas.add(saturn);

        // Year 2071 mid-year should find Saturn-Venus
        String[] result = GeminiPredictionService.findDasaAndBhukthiForYear(dasas, 2071);
        assertEquals("Saturn", result[0], "Dasa Lord for 2071 must be Saturn");
        assertEquals("Venus", result[1], "Bhukthi Lord for 2071 must be Venus");

        // Year 2061 should find Saturn-Mercury
        String[] result2 = GeminiPredictionService.findDasaAndBhukthiForYear(dasas, 2061);
        assertEquals("Saturn", result2[0]);
        assertEquals("Mercury", result2[1]);
    }

    @Test
    public void testAutonomousAyulAndLeanParsing() {
        String sampleJson = "{\n" +
                "  \"candidates\": [{\n" +
                "    \"content\": {\n" +
                "      \"parts\": [{\n" +
                "        \"text\": \"{\\n" +
                "          \\\"aiLongevityAnalysis\\\": {\\n" +
                "            \\\"calculatedAyulCeiling\\\": 82,\\n" +
                "            \\\"classification\\\": \\\"Poornayu\\\",\\n" +
                "            \\\"primarySpanRationale\\\": \\\"Strong Lagna and 8th lord\\\",\\n" +
                "            \\\"activeYogasIdentified\\\": [{\\\"yogaName\\\": \\\"Gaja Kesari\\\", \\\"effect\\\": \\\"Wisdom\\\"}],\\n" +
                "            \\\"activeDoshasIdentified\\\": []\\n" +
                "          },\\n" +
                "          \\\"personalityAndBehavior\\\": {\\n" +
                "            \\\"coreTemperament\\\": \\\"Principled and analytical\\\"\\n" +
                "          },\\n" +
                "          \\\"retrospectivePastMilestones\\\": [{\\\"approxPeriod\\\": \\\"2018-2020\\\", \\\"milestoneTitle\\\": \\\"Degree\\\", \\\"eventNarrative\\\": \\\"Graduation\\\"}],\\n" +
                "          \\\"yearlyPredictions\\\": [{\\\"year\\\": 2026, \\\"age\\\": 31, \\\"activeDasaBhukthi\\\": \\\"Rahu-Saturn\\\", \\\"annualNarrative\\\": \\\"Career breakthrough\\\"}]\\n" +
                "        }\"\n" +
                "      }]\n" +
                "    }\n" +
                "  }]\n" +
                "}";

        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");
        PredictionRequestDTO req = PredictionRequestDTO.builder().birthDetails(birth).language("ta").build();

        PredictionResponseDTO parsed = predictionService.parseGeminiResponse(sampleJson, req);
        assertNotNull(parsed);
        assertTrue(parsed.isEnabled());
        assertNotNull(parsed.getAiLongevityAnalysis());
        assertEquals(82, parsed.getAiLongevityAnalysis().getCalculatedAyulCeiling());
        assertEquals("Poornayu", parsed.getAiLongevityAnalysis().getClassification());
        assertNotNull(parsed.getPersonalityAndBehavior());
        assertEquals("Principled and analytical", parsed.getPersonalityAndBehavior().getCoreTemperament());
        assertEquals(1, parsed.getRetrospectivePastMilestones().size());
        assertEquals(1, parsed.getYearlyPredictions().size());
        assertEquals("Career breakthrough", parsed.getYearlyPredictions().get(0).getAnnualNarrative());
    }

    @Test
    public void test10YearModePromptDirectives() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");
        ChartResponseDTO.PositionDetail lagna = ChartResponseDTO.PositionDetail.builder()
                .planetKey("LAGNA").displayName("Lagna").signNumber(1).rashiName("Mesha").build();
        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Vrishabha").nakshatra("Rohini").build())
                .d1Chart(List.of(lagna))
                .currentDasaTimeline(Collections.emptyList())
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        geminiProperties.setForecastMode("NEXT_10_YEARS");
        geminiProperties.setForecastYears(10);
        String prompt = predictionService.constructAstrologicalPrompt(req);
        assertNotNull(prompt);
        assertTrue(prompt.contains("NEXT") || prompt.contains("10 YEARS") || prompt.contains("TEN_YEARS"));
        assertTrue(prompt.contains("Career & Business") || prompt.contains("yearlyPredictions"));

        // Test fallback prediction returns populated metadata
        PredictionResponseDTO fallback = predictionService.generateOfflineRuleBasedBalan(req);
        assertNotNull(fallback);
        assertTrue("TEN_YEARS".equals(fallback.getForecastMode()) || "NEXT_10_YEARS".equals(fallback.getForecastMode()));
        assertEquals(11, fallback.getTotalForecastYears());
        assertTrue(fallback.getStartYear() > 2020);
        assertTrue(fallback.getEndYear() >= fallback.getStartYear() + 10);

        // Reset
        geminiProperties.setForecastMode("FULL_LIFESPAN");
        geminiProperties.setForecastYears(0);
    }
}
