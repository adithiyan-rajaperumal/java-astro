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
}
