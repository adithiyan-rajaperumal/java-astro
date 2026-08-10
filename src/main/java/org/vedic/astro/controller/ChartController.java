package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ChartUiResponseDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.panchangam.PanchangamEngine;
import org.vedic.astro.panchangam.PanchangamFactory;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.ChartOrchestrationService;
import org.vedic.astro.service.PdfExportService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/astrology")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ChartController {

    private final PanchangamFactory panchangamFactory;
    private final ChartOrchestrationService orchestrationService;
    private final PdfExportService pdfExportService;
    private final org.vedic.astro.config.GeminiProperties geminiProperties;
    private final org.vedic.astro.service.GeminiPredictionService geminiPredictionService;

    @GetMapping("/config")
    public ResponseEntity<org.vedic.astro.dto.AppConfigDTO> getAppConfig() {
        boolean lifeEnabled = geminiProperties != null && geminiProperties.isLifePredictionsEnabled();
        boolean dailyEnabled = geminiProperties != null && geminiProperties.isDailyBalanEnabled();
        boolean pdfEnabled = geminiProperties != null && geminiProperties.isPdfPredictionsEnabled();
        String model = geminiProperties != null ? geminiProperties.getModel() : "gemini-3.6-flash";
        return ResponseEntity.ok(org.vedic.astro.dto.AppConfigDTO.builder()
                .aiPredictionsEnabled(lifeEnabled)
                .lifePredictionsEnabled(lifeEnabled)
                .dailyBalanEnabled(dailyEnabled)
                .pdfPredictionsEnabled(pdfEnabled)
                .geminiModel(model)
                .build());
    }

    @PostMapping(path = "/calculate", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ChartUiResponseDTO> calculateNatalCharts(
            @RequestBody BirthDetailsDTO birthDetails,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {

        // Factory resolves strategy pattern dynamically
        PanchangamEngine engine = panchangamFactory.getEngine(systemType);
        ChartResult res = engine.calculate(birthDetails);
        ChartUiResponseDTO response = orchestrationService.convertToUiDashboardResponse(res, birthDetails);
        response.setPanchangamSystem(systemType.name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadComprehensiveAstrologyReport(@RequestBody BirthDetailsDTO payload, @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {
        try {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(true);
            // Factory resolves strategy pattern dynamically
            PanchangamEngine engine = panchangamFactory.getEngine(systemType);
            ChartResult res = engine.calculate(payload);

            ComprehensiveReportDTO deepReportData = engine.generateComprehensiveReport(payload, res);
            deepReportData.setPanchangamSystem(systemType.name());

            if (geminiProperties != null && geminiProperties.isFeatureEnabled() && geminiPredictionService != null) {
                try {
                    ChartUiResponseDTO uiResp = orchestrationService.convertToUiDashboardResponse(res, payload);
                    org.vedic.astro.dto.PredictionRequestDTO predReq = org.vedic.astro.dto.PredictionRequestDTO.builder()
                            .birthDetails(payload)
                            .chartData(uiResp)
                            .language(org.springframework.context.i18n.LocaleContextHolder.getLocale().getLanguage())
                            .build();
                    deepReportData.setAiPredictions(geminiPredictionService.generateLifePredictions(predReq));
                } catch (Exception ignored) {}
            }

            byte[] pdfBinaryReport = pdfExportService.generateAstrologyReport(deepReportData);
            String fileName = payload.name().replaceAll("[^a-zA-Z0-9]", "") + "_Premium_Kundali.pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF).contentLength(pdfBinaryReport.length).body(pdfBinaryReport);
        } finally {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(false);
        }
    }
}
