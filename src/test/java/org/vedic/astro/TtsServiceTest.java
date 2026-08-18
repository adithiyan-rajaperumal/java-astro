package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.service.TtsService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TtsServiceTest {

    @Test
    public void testTtsSynthesis() {
        TtsService ttsService = new TtsService();
        byte[] audio = ttsService.synthesizeSpeech("வணக்கம்", "ta");
        assertNotNull(audio);
        // Returns valid audio bytes or empty on no network
    }

    @Test
    public void testTtsEmptyInput() {
        TtsService ttsService = new TtsService();
        byte[] audio = ttsService.synthesizeSpeech("", "en");
        assertNotNull(audio);
        assertTrue(audio.length == 0);
    }
}
