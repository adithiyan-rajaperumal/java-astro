package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAppConfigEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/astrology/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geminiModel").exists())
                .andExpect(jsonPath("$.aiPredictionsEnabled").isBoolean());
    }
}
