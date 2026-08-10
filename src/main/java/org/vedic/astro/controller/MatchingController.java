package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.matching.MatchingEngine;
import org.vedic.astro.matching.MatchingFactory;
import org.vedic.astro.matching.chart.ChartAugmentedAnalysis;
import org.vedic.astro.matching.dto.MatchingRequestDTO;
import org.vedic.astro.matching.dto.MatchingResponseDTO;
import org.vedic.astro.matching.model.MatchingContext;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.panchangam.PanchangamEngine;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.ChartOrchestrationService;
import org.vedic.astro.service.PdfExportService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/astrology")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MatchingController {

    private final PanchangamFactory panchangamFactory;
    private final MatchingFactory matchingFactory;
    private final ChartAugmentedAnalysis chartAugmentedAnalysis;
    private final ChartOrchestrationService orchestrationService;
    private final PdfExportService pdfExportService;
    private final org.vedic.astro.service.GeminiPredictionService geminiPredictionService;
    private final org.vedic.astro.service.PredictionCacheService cacheService;
    private final org.vedic.astro.config.GeminiProperties geminiProperties;

    private String normalizeLanguage(String lang) {
        if (lang == null || lang.isBlank()) return "ta";
        String l = lang.contains(",") ? lang.split(",")[0].trim() : lang.trim();
        if (l.contains("-")) l = l.split("-")[0].trim();
        if (l.contains("_")) l = l.split("_")[0].trim();
        return l.toLowerCase();
    }

    @PostMapping(path = "/match", produces = "application/json;charset=UTF-8")
    public ResponseEntity<MatchingResponseDTO> calculateCompatibility(
            @RequestBody MatchingRequestDTO request,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {

        PanchangamEngine panchangamEngine = panchangamFactory.getEngine(systemType);
        ChartResult boyChart = panchangamEngine.calculate(request.boy());
        ChartResult girlChart = panchangamEngine.calculate(request.girl());

        MatchingContext context = new MatchingContext(boyChart, girlChart, request.boy(), request.girl(), request.strictness());
        MatchingEngine matchingEngine = matchingFactory.getEngine(request.matchingSystem());

        MatchingResponseDTO response = matchingEngine.calculateCompatibility(context);
        List<String> warnings = chartAugmentedAnalysis.runComparativeAnalysis(context);
        response.setWarnings(warnings);

        response.setPanchangamSystem(systemType.name());
        response.setBoyProfile(orchestrationService.convertToUiDashboardResponse(boyChart, request.boy(), systemType.name()));
        response.setGirlProfile(orchestrationService.convertToUiDashboardResponse(girlChart, request.girl(), systemType.name()));

        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/match/ai", produces = "application/json;charset=UTF-8")
    public ResponseEntity<org.vedic.astro.matching.dto.MatchingAiPredictionDTO> calculateAiCompatibility(
            @RequestBody MatchingRequestDTO request,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType,
            @RequestParam(defaultValue = "ta") String language,
            @RequestParam(defaultValue = "false") boolean forceRefresh) {

        String effectiveLang = normalizeLanguage(language);

        PanchangamEngine panchangamEngine = panchangamFactory.getEngine(systemType);
        ChartResult boyChart = panchangamEngine.calculate(request.boy());
        ChartResult girlChart = panchangamEngine.calculate(request.girl());

        MatchingContext context = new MatchingContext(boyChart, girlChart, request.boy(), request.girl(), request.strictness());
        MatchingEngine matchingEngine = matchingFactory.getEngine(request.matchingSystem());

        MatchingResponseDTO classicalResponse = matchingEngine.calculateCompatibility(context);
        List<String> warnings = chartAugmentedAnalysis.runComparativeAnalysis(context);
        classicalResponse.setWarnings(warnings);
        classicalResponse.setPanchangamSystem(systemType.name());
        classicalResponse.setBoyProfile(orchestrationService.convertToUiDashboardResponse(boyChart, request.boy(), systemType.name()));
        classicalResponse.setGirlProfile(orchestrationService.convertToUiDashboardResponse(girlChart, request.girl(), systemType.name()));

        org.vedic.astro.matching.dto.MatchingAiPredictionDTO aiResponse = geminiPredictionService.generateMarriageMatchingAiAnalysis(
                request, classicalResponse, effectiveLang, forceRefresh);

        return ResponseEntity.ok(aiResponse);
    }

    @PostMapping("/match/download-pdf")
    public ResponseEntity<byte[]> downloadCompatibilityReport(
            @RequestBody MatchingRequestDTO request,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType,
            @RequestHeader(value = "Accept-Language", defaultValue = "ta") String language) {
        try {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(true);
            String effectiveLang = normalizeLanguage(language);
            
            PanchangamEngine panchangamEngine = panchangamFactory.getEngine(systemType);
            ChartResult boyChart = panchangamEngine.calculate(request.boy());
            ChartResult girlChart = panchangamEngine.calculate(request.girl());

            MatchingContext context = new MatchingContext(boyChart, girlChart, request.boy(), request.girl(), request.strictness());
            MatchingEngine matchingEngine = matchingFactory.getEngine(request.matchingSystem());

            MatchingResponseDTO response = matchingEngine.calculateCompatibility(context);
            List<String> warnings = chartAugmentedAnalysis.runComparativeAnalysis(context);
            response.setWarnings(warnings);
            response.setPanchangamSystem(systemType.name());

            response.setBoyProfile(orchestrationService.convertToUiDashboardResponse(boyChart, request.boy(), systemType.name()));
            response.setGirlProfile(orchestrationService.convertToUiDashboardResponse(girlChart, request.girl(), systemType.name()));

            // Attach AI Marriage Compatibility if enabled in configuration
            if (geminiProperties != null && geminiProperties.isPdfPredictionsEnabled() && geminiProperties.isMatchingEnabled() && geminiPredictionService != null) {
                try {
                    var aiPred = geminiPredictionService.generateMarriageMatchingAiAnalysis(request, response, effectiveLang, false);
                    if (aiPred != null && aiPred.isEnabled()) {
                        response.setAiMatchingPrediction(aiPred);
                    }
                } catch (Exception ignored) {}
            } else {
                String cacheKey = cacheService.generateMatchingKey(request, effectiveLang);
                var cachedAi = cacheService.getMatchingPrediction(cacheKey);
                if (cachedAi != null && cachedAi.isEnabled()) {
                    response.setAiMatchingPrediction(cachedAi);
                }
            }

            byte[] pdfBinaryReport = pdfExportService.generateMarriageMatchingReport(response);
            String fileName = "Compatibility_Report.pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF).contentLength(pdfBinaryReport.length).body(pdfBinaryReport);
        } finally {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(false);
        }
    }
}
