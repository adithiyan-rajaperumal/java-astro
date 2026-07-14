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

    @PostMapping(path = "/calculate", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ChartUiResponseDTO> calculateNatalCharts(
            @RequestBody BirthDetailsDTO birthDetails,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {

        // Factory resolves strategy pattern dynamically
        PanchangamEngine engine = panchangamFactory.getEngine(systemType);
        ChartResult res = engine.calculate(birthDetails);
        return ResponseEntity.ok(orchestrationService.convertToUiDashboardResponse(res, birthDetails));
    }





    @PostMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadComprehensiveAstrologyReport(@RequestBody BirthDetailsDTO payload, @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {
        try {
            org.vedic.astro.util.IndicPreShaper.setPdfMode(true);
            // Factory resolves strategy pattern dynamically
            PanchangamEngine engine = panchangamFactory.getEngine(systemType);
            ChartResult res = engine.calculate(payload);

            ComprehensiveReportDTO deepReportData = engine.generateComprehensiveReport(payload, res);
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
