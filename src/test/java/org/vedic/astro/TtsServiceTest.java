package org.vedic.astro;

import org.junit.jupiter.api.Test;
import org.vedic.astro.service.TtsService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TtsServiceTest {

    @Test
    public void testTtsSynthesis() {
        TtsService ttsService = new TtsService();
        String[] languages = {"ta", "hi", "te", "kn", "ml", "en"};
        String[] samples = {
                "வணக்கம் வாழ்க வளமுடன்",
                "नमस्ते आप कैसे हैं",
                "నమస్కారం బాగున్నారా",
                "ನಮಸ್ಕಾರ ಹೇಗಿದ್ದೀರಾ",
                "നമസ്കാരം സുഖമാണോ",
                "Hello, welcome to Vedic Astrology"
        };

        for (int i = 0; i < languages.length; i++) {
            byte[] audio = ttsService.synthesizeSpeech(samples[i], languages[i]);
            assertNotNull(audio);
            if (audio.length > 0) {
                assertTrue(audio.length > 100, "Synthesized audio should contain MP3 stream bytes for " + languages[i]);
            }
        }
    }

    @Test
    public void testTtsEmptyInput() {
        TtsService ttsService = new TtsService();
        byte[] audio = ttsService.synthesizeSpeech("", "en");
        assertNotNull(audio);
        assertTrue(audio.length == 0);
    }
}
