package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.service.TtsService;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/tts")
@RequiredArgsConstructor
public class TtsController {

    private final TtsService ttsService;

    @GetMapping(produces = "audio/mpeg")
    public ResponseEntity<byte[]> getTtsAudio(
            @RequestParam String text,
            @RequestParam(defaultValue = "en") String lang) {
        byte[] audio = ttsService.synthesizeSpeech(text, lang);
        if (audio.length == 0) {
            return ResponseEntity.noContent().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/mpeg"));
        headers.setCacheControl(CacheControl.maxAge(3600, TimeUnit.SECONDS));
        return new ResponseEntity<>(audio, headers, HttpStatus.OK);
    }
}
