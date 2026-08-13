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
import java.util.Locale;

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
        double temperature = geminiProperties != null ? geminiProperties.getTemperature() : 0.4;
        int thinkingBudget = geminiProperties != null ? geminiProperties.getThinkingBudget() : 1024;
        String forecastMode = geminiProperties != null ? geminiProperties.getForecastMode() : "FULL_LIFESPAN";
        int forecastYears = geminiProperties != null ? geminiProperties.getForecastYears() : 0;
        return ResponseEntity.ok(org.vedic.astro.dto.AppConfigDTO.builder()
                .aiPredictionsEnabled(lifeEnabled)
                .lifePredictionsEnabled(lifeEnabled)
                .dailyBalanEnabled(dailyEnabled)
                .pdfPredictionsEnabled(pdfEnabled)
                .geminiModel(model)
                .temperature(temperature)
                .thinkingBudget(thinkingBudget)
                .forecastMode(forecastMode)
                .forecastYears(forecastYears)
                .build());
    }

    @PostMapping(path = "/calculate", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ChartUiResponseDTO> calculateNatalCharts(
            @RequestBody BirthDetailsDTO birthDetails,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType,
            @RequestParam(required = false) String language,
            @RequestHeader(value = "Accept-Language", defaultValue = "en") String acceptLanguage) {

        String lang = (language != null && !language.isBlank()) ? language : acceptLanguage;
        if (lang != null && !lang.isBlank()) {
            org.springframework.context.i18n.LocaleContextHolder.setLocale(new Locale(lang.split("[,;_-]")[0]));
        }

        // Factory resolves strategy pattern dynamically
        PanchangamEngine engine = panchangamFactory.getEngine(systemType);
        ChartResult res = engine.calculate(birthDetails);
        ChartUiResponseDTO response = orchestrationService.convertToUiDashboardResponse(res, birthDetails);
        response.setPanchangamSystem(systemType.name());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadComprehensiveAstrologyReport(
            @RequestBody BirthDetailsDTO payload,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType,
            @RequestParam(required = false) String language,
            @RequestHeader(value = "Accept-Language", defaultValue = "ta") String acceptLanguage) {
        try {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(true);
            String rawLang = (language != null && !language.isBlank()) ? language : acceptLanguage;
            String effectiveLang = (rawLang != null && rawLang.contains(",")) ? rawLang.split(",")[0].trim() : (rawLang != null ? rawLang : "ta");
            if (effectiveLang.contains("-")) effectiveLang = effectiveLang.split("-")[0].trim();
            if (effectiveLang.contains("_")) effectiveLang = effectiveLang.split("_")[0].trim();
            effectiveLang = effectiveLang.toLowerCase();
            org.springframework.context.i18n.LocaleContextHolder.setLocale(java.util.Locale.forLanguageTag(effectiveLang));

            // Factory resolves strategy pattern dynamically
            PanchangamEngine engine = panchangamFactory.getEngine(systemType);
            ChartResult res = engine.calculate(payload);

            ComprehensiveReportDTO deepReportData = engine.generateComprehensiveReport(payload, res);
            deepReportData.setPanchangamSystem(systemType.name());

            if (geminiProperties != null && geminiProperties.isPdfPredictionsEnabled() && geminiPredictionService != null) {
                try {
                    ChartUiResponseDTO uiResp = orchestrationService.convertToUiDashboardResponse(res, payload);
                    org.vedic.astro.dto.PredictionRequestDTO predReq = org.vedic.astro.dto.PredictionRequestDTO.builder()
                            .birthDetails(payload)
                            .chartData(uiResp)
                            .language(effectiveLang)
                            .forceRefresh(false)
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
