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
    public void testOfflineRuleBasedBalan() {
        BirthDetailsDTO birth = new BirthDetailsDTO("Kavitha", 1990, 8, 20, 14, 15, 0, 13.0827, 80.2707, "LAHIRI");

        ChartUiResponseDTO chart = ChartUiResponseDTO.builder()
                .birthProfile(ChartResponseDTO.BirthProfile.builder().lagna("Simha").rashi("Kanya").nakshatra("Hasta").build())
                .currentDasaTimeline(Collections.emptyList())
                .build();

        PredictionRequestDTO req = PredictionRequestDTO.builder()
                .birthDetails(birth)
                .chartData(chart)
                .language("ta")
                .build();

        PredictionResponseDTO response = predictionService.generateOfflineRuleBasedBalan(req);
        assertNotNull(response);
        assertTrue(response.isEnabled());
        assertNotNull(response.getPastMilestones());
        assertFalse(response.getPastMilestones().isEmpty());
        assertNotNull(response.getFuturePredictions());
        assertFalse(response.getFuturePredictions().isEmpty());
        assertTrue(response.getFuturePredictions().get(0).getYear() >= 2026);
    }

    @Test
    public void testPredictionEndpoint() throws Exception {
        String jsonPayload = """
                {
                  "birthDetails": {
                    "name": "Arun",
                    "year": 1992,
                    "month": 4,
                    "day": 10,
                    "hour": 10,
                    "minute": 30,
                    "second": 0,
                    "latitude": 13.0827,
                    "longitude": 80.2707
                  },
                  "chartData": {
                    "panchangamSystem": "DRIK_TIRUKANITHAM",
                    "thithi": "Shukla - Ashtami",
                    "yogam": "Dhriti",
                    "karanam": "Vishti"
                  },
                  "language": "ta"
                }
                """;

        mockMvc.perform(post("/api/v1/astrology/predictions/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.pastMilestones").isArray())
                .andExpect(jsonPath("$.futurePredictions").isArray());
    }

    @Test
    public void testPredictionEndpointWithExtraFrontendProperties() throws Exception {
        String jsonPayload = """
                {
                  "birthDetails": {
                    "name": "Arun Kumar",
                    "year": 1992,
                    "month": 4,
                    "day": 10,
                    "hour": 10,
                    "minute": 30,
                    "second": 0,
                    "latitude": 13.0827,
                    "longitude": 80.2707,
                    "location": { "name": "Chennai, Tamil Nadu, India", "lat": 13.0827, "lon": 80.2707 },
                    "ayanamsa": "LAHIRI",
                    "panchangamSystem": "DRIK_TIRUKANITHAM",
                    "date": "1992-04-10",
                    "time": "10:30"
                  },
                  "chartData": {
                    "name": "Arun Kumar",
                    "dateOfBirth": "10/04/1992",
                    "timeOfBirth": "10:30:00",
                    "localMeanTime": "10:21:05",
                    "latitude": 13.0827,
                    "longitude": 80.2707,
                    "resolvedTimezone": "Asia/Kolkata",
                    "ayanamsa": "LAHIRI",
                    "panchangamSystem": "DRIK_TIRUKANITHAM",
                    "birthProfile": {
                      "lagna": "Mesha",
                      "rashi": "Mithuna",
                      "nakshatra": "Punarvasu",
                      "nakshatraPada": 2
                    },
                    "d1Chart": [
                      {
                        "planetKey": "SUN",
                        "displayName": "Sun",
                        "signNumber": 12,
                        "rashiName": "Pisces",
                        "degreeInSign": 26.54,
                        "formattedDegree": "26° 32' 24\\""
                      }
                    ],
                    "thithi": "Shukla - Ashtami",
                    "yogam": "Dhriti",
                    "karanam": "Vishti",
                    "aiPredictionsEnabled": true
                  },
                  "language": "ta"
                }
                """;

        mockMvc.perform(post("/api/v1/astrology/predictions/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.aiYogas").isArray())
                .andExpect(jsonPath("$.aiDoshams").isArray())
                .andExpect(jsonPath("$.pastMilestones").isArray())
                .andExpect(jsonPath("$.futurePredictions").isArray());
    }

    @Test
    public void testEncryptedApiKeyResolution() {
        org.vedic.astro.config.GeminiProperties props = new org.vedic.astro.config.GeminiProperties();
        props.setApiKey("enc:dGVzdC1rZXktMTIzNDU=");
        assertEquals("test-key-12345", props.getResolvedApiKey());
        assertTrue(props.isFeatureEnabled());
    }
}
