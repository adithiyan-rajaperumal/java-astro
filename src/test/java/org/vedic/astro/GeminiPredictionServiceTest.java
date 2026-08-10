package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.vedic.astro.dto.*;
import org.vedic.astro.service.GeminiPredictionService;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GeminiPredictionServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeminiPredictionService predictionService;

    @Test
    public void testPromptConstruction() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Ramesh", 1995, 5, 15, 6, 30, 0, 13.0827, 80.2707, "LAHIRI");

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Mesha").rashi("Vrishabha").nakshatra("Rohini").build())
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .thithi("Shukla - Dvitiya")
                .yogam("Siddha")
                .karanam("Bava")
                .currentDasaTimeline(Collections.emptyList())
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
    }

    @Test
    public void testOfflineServiceReturnsUnavailable() {
        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(new BirthDetailsDTO("Kavitha", 1990, 8, 20, 14, 15, 0, 13.0827, 80.2707, "LAHIRI"))
                .chartData(ChartUiResponseDTO.builder().build())
                .language("ta")
                .build();

        PredictionResponseDTO response = predictionService.generateOfflineRuleBasedBalan(req);
        assertNotNull(response);
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
                            "text": "{\\"overallSummary\\":\\"உயர்ந்த அறிவும் ராஜ யோகமும் நிறைந்த ஜாதகம்.\\",\\"aiYogas\\":[],\\"aiDoshams\\":[],\\"pastMilestones\\":[],\\"futurePredictions\\":[]}"
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
    public void testDailyBalanPromptConstruction() {
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

        DailyBalanDTO offlineDaily = predictionService.generateOfflineRuleBasedDailyBalan(req, null, java.time.LocalDate.of(2026, 8, 10));
        assertNotNull(offlineDaily);
        assertTrue(offlineDaily.isEnabled());
        assertNotNull(offlineDaily.getGeneralOutlook());
        assertNotNull(offlineDaily.getLuckyColor());
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
}
