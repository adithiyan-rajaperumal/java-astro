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

    public ChartUiResponseDTO convertToUiDashboardResponse(ChartResult res, BirthDetailsDTO pay) {
        PlanetaryPosition moon = res.getD1Positions().get("Moon");
        LocalDate dob = LocalDate.of(pay.year(), pay.month(), pay.day());

        Map<String, PlanetaryPosition> d1 = res.getD1Positions();

        double sunLong = d1.get("Sun").getAbsoluteLongitude();
        double moonLong = d1.get("Moon").getAbsoluteLongitude();

        // ==========================================
        // BULLETPROOF PANCHANGAM CALCULATION ENGINE
        // ==========================================
        // FIX 1: Safety buffer (+720.0) prevents Java negative modulo bugs if coordinates dip below 0
        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        int thithiIdx = (int) (elongation / 12.0) + 1;
        thithiIdx = Math.min(30, Math.max(1, thithiIdx)); // Absolute boundary clamp

        String paksha = (thithiIdx <= 15) ? ts.getLabel("panchangam.shukla") : ts.getLabel("panchangam.krishna");
        int localizedThithiNum = (thithiIdx > 15) ? thithiIdx - 15 : thithiIdx;

        String rawThithiLabel = ts.getLabel("thithi." + localizedThithiNum);
        String computedThithi;

        // FIX 2: Programmatic splitter resolves the bundle collision for Purnima/Amavasya cleanly
        if (thithiIdx == 15 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[0].trim(); // Extracts "Purnima"
        } else if (thithiIdx == 30 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[1].trim(); // Extracts "Amavasya"
        } else {
            computedThithi = paksha + " - " + rawThithiLabel; // Standard structure for dates 1-14
        }

        // FIX 3: Safety buffer ensures coordinate additions remain positive across the meridian bounds
        double totalYogaLong = (sunLong + moonLong + 720.0) % 360.0;
        int yogamIdx = (int) (totalYogaLong / (360.0 / 27.0)) + 1;
        String computedYogam = ts.getLabel("yogam." + Math.min(27, Math.max(1, yogamIdx)));

        int karanamIdx = (int) (elongation / 6.0) + 1;
        String computedKaranam = ts.getLabel("karanam." + resolveKaranamId(karanamIdx));

        return ChartUiResponseDTO.builder().name(res.getName()).dateOfBirth(dob.toString())
                .timeOfBirth(String.format("%02d:%02d:%02d", pay.hour(), pay.minute(), pay.second())).thithi(computedThithi)
                .yogam(computedYogam)
                .karanam(computedKaranam)
                .localMeanTime(res.getLocalMeanTime()).birthProfile(buildProfileHeader(res.getD1Positions())).d1Chart(compileVargaList(1, res.getD1Positions(), null)).d9Chart(compileVargaList(9, res.getD1Positions(), null)).currentDasaTimeline(dasaEngine.calculateVimshottariTimeline(moon.getAbsoluteLongitude(), dob).stream().collect(Collectors.toList())).build();
    }

    public ComprehensiveReportDTO compileComprehensivePdfData(ChartResult res, BirthDetailsDTO pay, double[] cusps) {
        Map<String, PlanetaryPosition> d1 = res.getD1Positions();
        LocalDate dob = LocalDate.of(pay.year(), pay.month(), pay.day());

        double sunLong = d1.get("Sun").getAbsoluteLongitude();
        double moonLong = d1.get("Moon").getAbsoluteLongitude();

        // ==========================================
        // BULLETPROOF PANCHANGAM CALCULATION ENGINE
        // ==========================================
        // FIX 1: Safety buffer (+720.0) prevents Java negative modulo bugs if coordinates dip below 0
        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        int thithiIdx = (int) (elongation / 12.0) + 1;
        thithiIdx = Math.min(30, Math.max(1, thithiIdx)); // Absolute boundary clamp

        String paksha = (thithiIdx <= 15) ? ts.getLabel("panchangam.shukla") : ts.getLabel("panchangam.krishna");
        int localizedThithiNum = (thithiIdx > 15) ? thithiIdx - 15 : thithiIdx;

        String rawThithiLabel = ts.getLabel("thithi." + localizedThithiNum);
        String computedThithi;

        // FIX 2: Programmatic splitter resolves the bundle collision for Purnima/Amavasya cleanly
        if (thithiIdx == 15 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[0].trim(); // Extracts "Purnima"
        } else if (thithiIdx == 30 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[1].trim(); // Extracts "Amavasya"
        } else {
            computedThithi = paksha + " - " + rawThithiLabel; // Standard structure for dates 1-14
        }

        // FIX 3: Safety buffer ensures coordinate additions remain positive across the meridian bounds
        double totalYogaLong = (sunLong + moonLong + 720.0) % 360.0;
        int yogamIdx = (int) (totalYogaLong / (360.0 / 27.0)) + 1;
        String computedYogam = ts.getLabel("yogam." + Math.min(27, Math.max(1, yogamIdx)));

        int karanamIdx = (int) (elongation / 6.0) + 1;
        String computedKaranam = ts.getLabel("karanam." + resolveKaranamId(karanamIdx));

        List<List<ChartResponseDTO.PositionDetail>> suite = new ArrayList<>();
        int[] targets = {1, 2, 3, -1, 7, 9, 10, 12, 20, 24, 30, 60};
        for (int t : targets) { suite.add(compileVargaList(t, d1, cusps)); }

        return ComprehensiveReportDTO.builder()
                .name(pay.name())
                .dateOfBirth(dob.toString())
                .timeOfBirth(String.format("%02d:%02d:%02d", pay.hour(), pay.minute(), pay.second()))
                .localMeanTime(res.getLocalMeanTime())
                .latitude(pay.latitude())
                .longitude(pay.longitude())
                .thithi(computedThithi)
                .yogam(computedYogam)
                .karanam(computedKaranam)
                .birthProfile(buildProfileHeader(d1))
                .birthPlanetaryPositions(d1.entrySet().stream().map(e -> mapToDetail(e.getKey().toUpperCase(), e.getValue())).collect(Collectors.toList()))
                .vargaChartsSuite(suite)
                .vimshottariTimeline(dasaEngine.calculateVimshottariTimeline(moonLong, dob))
                .shadbalaStrengths(shadbalaService.calculateShadbala(d1))
                .structuralDiagnostics(diagnosticsService.runHoroscopeDiagnostics(d1))
                .build();
    }

    /**
     * Corrected Traditional Karana Mapping Engine
     * Maps 60 dynamic annual blocks cleanly into the 11 localized translation IDs
     * Karama cycle: Kintughna (1) → [Bava-Vishti repeat 8x] → Sakuni (9) → Chatushpada (10) → Naga (11) → [cycle repeats]
     */
    private int resolveKaranamId(int idx) {
        // 1. First Fixed Karana: Shukla Prathama (1st Half)
        if (idx == 1) return 1; // Maps to karanam.1=Kintughna

        // 2. Final Three Fixed Karanas (Indices 57-59)
        if (idx >= 57 && idx <= 59) {
            return idx - 48;
            // 57 - 48 = 9  (Maps to karanam.9=Sakuni)
            // 58 - 48 = 10 (Maps to karanam.10=Chatushpada)
            // 59 - 48 = 11 (Maps to karanam.11=Naga)
        }

        // 3. Index 60 completes the cycle - loops back to Vishti (8)
        if (idx == 60) return 8;

        // 4. Repeating Cyclic Chara Karanas (Indices 2 to 56)
        // 7 repeating karanas: Bava (2), Balava (3), Kaulava (4), Taitila (5), Gara (6), Vanija (7), Vishti (8)
        return ((idx - 2) % 7) + 2; // Maps idx 2-56 to karanam.2 through karanam.8
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
