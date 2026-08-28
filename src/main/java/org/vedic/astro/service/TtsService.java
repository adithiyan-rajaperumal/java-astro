package org.vedic.astro.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TtsService {

    private final RestTemplate restTemplate;
    private final Map<String, byte[]> audioCache = new ConcurrentHashMap<>();

    public TtsService() {
        this.restTemplate = new RestTemplate();
    }

    public byte[] synthesizeSpeech(String text, String lang) {
        if (text == null || text.isBlank()) {
            return new byte[0];
        }

        String effectiveLang = normalizeLang(lang);
        String trimmedText = text.trim();
        if (trimmedText.length() > 200) {
            trimmedText = trimmedText.substring(0, 200);
        }

        String cacheKey = effectiveLang + ":" + trimmedText;
        byte[] cached = audioCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            String encodedText = URLEncoder.encode(trimmedText, StandardCharsets.UTF_8);
            java.net.URI uri = java.net.URI.create("https://translate.google.com/translate_tts?ie=UTF-8&q=" + encodedText 
                    + "&tl=" + effectiveLang + "&client=tw-ob&total=1&idx=0&textlen=" + trimmedText.length());

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            headers.set(HttpHeaders.REFERER, "https://translate.google.com/");
            headers.set(HttpHeaders.ACCEPT, "audio/mpeg,audio/*;q=0.9");

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().length > 0) {
                byte[] audio = response.getBody();
                if (audioCache.size() < 1000) {
                    audioCache.put(cacheKey, audio);
                }
                return audio;
            }
        } catch (Exception e) {
            log.warn("Failed to synthesize speech for lang {}: {}", effectiveLang, e.getMessage());
        }

        return new byte[0];
    }

    private String normalizeLang(String lang) {
        if (lang == null) return "en";
        String lower = lang.trim().toLowerCase();
        if (lower.startsWith("ta")) return "ta";
        if (lower.startsWith("hi")) return "hi";
        if (lower.startsWith("te")) return "te";
        if (lower.startsWith("kn")) return "kn";
        if (lower.startsWith("ml")) return "ml";
        return "en";
    }
}
