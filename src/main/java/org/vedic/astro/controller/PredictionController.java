package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.dto.*;
import org.vedic.astro.service.GeminiPredictionService;

@RestController
@RequestMapping("/api/v1/astrology/predictions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PredictionController {

    private final GeminiPredictionService predictionService;

    @PostMapping(path = "/generate", produces = "application/json;charset=UTF-8")
    public ResponseEntity<PredictionResponseDTO> generateLifePredictions(@RequestBody PredictionRequestDTO request) {
        PredictionResponseDTO response = predictionService.generateLifePredictions(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/daily", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DailyBalanDTO> generateDailyBalan(@RequestBody DailyBalanRequestDTO request) {
        DailyBalanDTO response = predictionService.generateDailyBalan(request);
        return ResponseEntity.ok(response);
    }
}
