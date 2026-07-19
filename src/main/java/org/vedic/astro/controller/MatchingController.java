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

        response.setBoyProfile(orchestrationService.convertToUiDashboardResponse(boyChart, request.boy()));
        response.setGirlProfile(orchestrationService.convertToUiDashboardResponse(girlChart, request.girl()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/match/download-pdf")
    public ResponseEntity<byte[]> downloadCompatibilityReport(
            @RequestBody MatchingRequestDTO request,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {
        try {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(true);
            
            PanchangamEngine panchangamEngine = panchangamFactory.getEngine(systemType);
            ChartResult boyChart = panchangamEngine.calculate(request.boy());
            ChartResult girlChart = panchangamEngine.calculate(request.girl());

            MatchingContext context = new MatchingContext(boyChart, girlChart, request.boy(), request.girl(), request.strictness());
            MatchingEngine matchingEngine = matchingFactory.getEngine(request.matchingSystem());

            MatchingResponseDTO response = matchingEngine.calculateCompatibility(context);
            List<String> warnings = chartAugmentedAnalysis.runComparativeAnalysis(context);
            response.setWarnings(warnings);

            response.setBoyProfile(orchestrationService.convertToUiDashboardResponse(boyChart, request.boy()));
            response.setGirlProfile(orchestrationService.convertToUiDashboardResponse(girlChart, request.girl()));

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
