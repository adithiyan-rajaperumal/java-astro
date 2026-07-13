package org.vedic.astro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ChartUiResponseDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.ZodiacUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChartOrchestrationService {

    private final TranslationService ts;
    private final VargaEngineService vargaEngine;
    private final DasaEngineService dasaEngine;
    private final ShadbalaService shadbalaService;
    private final AstrologyDiagnosticsService diagnosticsService;

    public ChartUiResponseDTO convertToUiDashboardResponse(ChartResult res, LocalDate dob) {
        PlanetaryPosition moon = res.getD1Positions().get("Moon");
        return ChartUiResponseDTO.builder().name(res.getName()).localMeanTime(res.getLocalMeanTime()).birthProfile(buildProfileHeader(res.getD1Positions())).d1Chart(compileVargaList(1, res.getD1Positions(), null)).d9Chart(compileVargaList(9, res.getD1Positions(), null)).currentDasaTimeline(dasaEngine.calculateVimshottariTimeline(moon.getAbsoluteLongitude(), dob).stream().collect(Collectors.toList())).build();
    }

    public ComprehensiveReportDTO compileComprehensivePdfData(ChartResult res, BirthDetailsDTO pay, double[] cusps) {
        Map<String, PlanetaryPosition> d1 = res.getD1Positions();
        LocalDate dob = LocalDate.of(pay.year(), pay.month(), pay.day());
        List<List<ChartResponseDTO.PositionDetail>> suite = new ArrayList<>();
        int[] targets = {1, 9, 10, 7, 3, -1, 2, 12, 30};
        for (int t : targets) { suite.add(compileVargaList(t, d1, cusps)); }

        return ComprehensiveReportDTO.builder().name(pay.name()).dateOfBirth(dob.toString()).timeOfBirth(String.format("%02d:%02d:%02d", pay.hour(), pay.minute(), pay.second())).latitude(pay.latitude()).longitude(pay.longitude()).birthProfile(buildProfileHeader(d1)).birthPlanetaryPositions(d1.entrySet().stream().map(e -> mapToDetail(e.getKey().toUpperCase(), e.getValue())).collect(Collectors.toList())).vargaChartsSuite(suite).vimshottariTimeline(dasaEngine.calculateVimshottariTimeline(d1.get("Moon").getAbsoluteLongitude(), dob)).shadbalaStrengths(shadbalaService.calculateShadbala(d1)).structuralDiagnostics(diagnosticsService.runHoroscopeDiagnostics(d1)).build();
    }

    private ChartResponseDTO.BirthProfile buildProfileHeader(Map<String, PlanetaryPosition> d1) {
        return ChartResponseDTO.BirthProfile.builder().lagna(ts.getLabel("profile.lagna") + ": " + ts.getLocalizedRashi(d1.get("Lagna").getSignNumber())).rashi(ts.getLabel("profile.rashi") + ": " + ts.getLocalizedRashi(d1.get("Moon").getSignNumber())).nakshatra(ts.getLabel("profile.nakshatra") + ": " + ts.getLocalizedNakshatra(ZodiacUtils.getNakshatraNumber(d1.get("Moon").getAbsoluteLongitude()))).nakshatraPada(d1.get("Moon").getPada()).build();
    }

    private List<ChartResponseDTO.PositionDetail> compileVargaList(int dNo, Map<String, PlanetaryPosition> d1, double[] cusps) {
        return d1.entrySet().stream().map(e -> {
            int sig = (dNo == -1) ? vargaEngine.calculateBhavaHouse(e.getValue().getAbsoluteLongitude(), cusps) : vargaEngine.calculateVargaSign(dNo, e.getValue().getSignNumber(), e.getValue().getDegreeInSign(), e.getValue().getAbsoluteLongitude());
            return ChartResponseDTO.PositionDetail.builder().planetKey(e.getKey().toUpperCase()).displayName(ts.getLabel("planet." + e.getKey().toUpperCase() + ".short")).signNumber(sig).rashiName(ts.getLocalizedRashi(sig)).degreeInSign(e.getValue().getDegreeInSign()).formattedDegree(ZodiacUtils.formatDMS(e.getValue().getDegreeInSign())).build();
        }).collect(Collectors.toList());
    }

    private ChartResponseDTO.PositionDetail mapToDetail(String key, PlanetaryPosition p) {
        return ChartResponseDTO.PositionDetail.builder().planetKey(key).displayName(ts.getLabel("planet." + key + ".short")).signNumber(p.getSignNumber()).rashiName(ts.getLocalizedRashi(p.getSignNumber())).degreeInSign(p.getDegreeInSign()).formattedDegree(ZodiacUtils.formatDMS(p.getDegreeInSign())).build();
    }
}
