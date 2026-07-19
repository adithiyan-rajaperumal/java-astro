package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;

@RestController
@RequestMapping("/api/v1/astrology")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PanchangamController {

    private final DailyPanchangamService dailyPanchangamService;

    @PostMapping(path = "/panchangam", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DailyPanchangamDTO> calculateDailyPanchangam(
            @RequestBody PanchangamRequestDTO request) {
        
        DailyPanchangamDTO response = dailyPanchangamService.calculateDailyPanchangam(request);
        return ResponseEntity.ok(response);
    }
}
