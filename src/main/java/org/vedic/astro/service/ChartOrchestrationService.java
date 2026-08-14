package org.vedic.astro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.dto.ChartUiResponseDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.dto.LifeAnchorsProfile;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.model.DasaPeriod;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.ZodiacUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
    private final TimezoneService timezoneService;
    private final org.vedic.astro.config.GeminiProperties geminiProperties;

    @org.springframework.beans.factory.annotation.Value("${astro.features.life-anchors-enabled:true}")
    private boolean lifeAnchorsEnabled;

    @org.springframework.beans.factory.annotation.Value("${astro.features.ayurdaya-enabled:true}")
    private boolean ayurdayaEnabled;

    public ChartUiResponseDTO convertToUiDashboardResponse(ChartResult res, BirthDetailsDTO pay) {
        PlanetaryPosition moon = res.getD1Positions().get("Moon");
        LocalDate dob = LocalDate.of(pay.year(), pay.month(), pay.day());

        Map<String, PlanetaryPosition> d1 = res.getD1Positions();

        double sunLong = d1.get("Sun").getAbsoluteLongitude();
        double moonLong = d1.get("Moon").getAbsoluteLongitude();

        // ==========================================
        // BULLETPROOF PANCHANGAM CALCULATION ENGINE
        // ==========================================
        // FIX 1: Safety buffer (+720.0) prevents Java negative modulo bugs if
        // coordinates dip below 0
        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        int thithiIdx = (int) (elongation / 12.0) + 1;
        thithiIdx = Math.min(30, Math.max(1, thithiIdx)); // Absolute boundary clamp

        String paksha = (thithiIdx <= 15) ? ts.getLabel("panchangam.shukla") : ts.getLabel("panchangam.krishna");
        int localizedThithiNum = (thithiIdx > 15) ? thithiIdx - 15 : thithiIdx;

        String rawThithiLabel = ts.getLabel("thithi." + localizedThithiNum);
        String computedThithi;

        // FIX 2: Programmatic splitter resolves the bundle collision for
        // Purnima/Amavasya cleanly
        if (thithiIdx == 15 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[0].trim(); // Extracts "Purnima"
        } else if (thithiIdx == 30 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[1].trim(); // Extracts "Amavasya"
        } else {
            computedThithi = paksha + " - " + rawThithiLabel; // Standard structure for dates 1-14
        }

        // FIX 3: Safety buffer ensures coordinate additions remain positive across the
        // meridian bounds
        double totalYogaLong = (sunLong + moonLong + 720.0) % 360.0;
        int yogamIdx = (int) (totalYogaLong / (360.0 / 27.0)) + 1;
        String computedYogam = ts.getLabel("yogam." + Math.min(27, Math.max(1, yogamIdx)));

        int karanamIdx = (int) (elongation / 6.0) + 1;
        String computedKaranam = ts.getLabel("karanam." + resolveKaranamId(karanamIdx));

        String resolvedTz = timezoneService.getTimezoneFromCoordinates(pay.latitude(), pay.longitude());

        List<ChartResponseDTO.PositionDetail> d1List = compileVargaList(1, res.getD1Positions(), null);
        List<DasaPeriod> dasas = dasaEngine.calculateVimshottariTimeline(moon.getAbsoluteLongitude(), dob);
        int lagnaSignNum = d1.get("Lagna") != null ? d1.get("Lagna").getSignNumber() : 1;
        int moonSignNum = d1.get("Moon") != null ? d1.get("Moon").getSignNumber() : 1;

        ShadbalaDTO shadbala = shadbalaService.calculateShadbala(d1);
        var healthProfile = org.vedic.astro.util.AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSignNum, moonSignNum, d1List);
        var ayurdayaProfile = ayurdayaEnabled
                ? org.vedic.astro.util.AyurdayaCalculationUtils.calculateAyurdaya(lagnaSignNum, moonSignNum, d1List, dasas, pay.year(), pay.hour(), pay.minute(), shadbala)
                : null;
        var d9PosList = compileVargaList(9, res.getD1Positions(), null);
        var lifeAnchorsProfile = lifeAnchorsEnabled
                ? buildLifeAnchorsProfile(lagnaSignNum, moonSignNum, d1, d9PosList, pay, res.getJulianDayUT())
                : null;

        return ChartUiResponseDTO.builder().name(res.getName()).dateOfBirth(dob.toString())
                .timeOfBirth(String.format("%02d:%02d:%02d", pay.hour(), pay.minute(), pay.second()))
                .latitude(pay.latitude())
                .longitude(pay.longitude())
                .resolvedTimezone(resolvedTz != null ? resolvedTz : "Asia/Kolkata")
                .ayanamsa(pay.ayanamsa() != null ? pay.ayanamsa() : "LAHIRI")
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .thithi(computedThithi)
                .yogam(computedYogam)
                .karanam(computedKaranam)
                .aiPredictionsEnabled(geminiProperties != null && geminiProperties.isFeatureEnabled())
                .localMeanTime(res.getLocalMeanTime()).birthProfile(buildProfileHeader(res.getD1Positions()))
                .d1Chart(d1List)
                .d9Chart(d9PosList)
                .bhavaChart(compileVargaList(-1, res.getD1Positions(), null))
                .currentDasaTimeline(dasas)
                .shadbalaStrengths(shadbalaService.calculateShadbala(d1))
                .structuralDiagnostics(diagnosticsService.runHoroscopeDiagnostics(d1))
                .ayurvedicHealth(healthProfile)
                .ayurdayaProfile(ayurdayaProfile)
                .lifeAnchors(lifeAnchorsProfile)
                .lifeAnchorsEnabled(lifeAnchorsEnabled)
                .ayurdayaEnabled(ayurdayaEnabled)
                .build();
    }

    public ChartUiResponseDTO convertToUiDashboardResponse(ChartResult res, BirthDetailsDTO pay, String panchangamSystem) {
        ChartUiResponseDTO dto = convertToUiDashboardResponse(res, pay);
        dto.setPanchangamSystem(panchangamSystem != null ? panchangamSystem : "DRIK_TIRUKANITHAM");
        return dto;
    }

    public ComprehensiveReportDTO compileComprehensivePdfData(ChartResult res, BirthDetailsDTO pay, double[] cusps) {
        Map<String, PlanetaryPosition> d1 = res.getD1Positions();
        LocalDate dob = LocalDate.of(pay.year(), pay.month(), pay.day());

        double sunLong = d1.get("Sun").getAbsoluteLongitude();
        double moonLong = d1.get("Moon").getAbsoluteLongitude();

        // ==========================================
        // BULLETPROOF PANCHANGAM CALCULATION ENGINE
        // ==========================================
        // FIX 1: Safety buffer (+720.0) prevents Java negative modulo bugs if
        // coordinates dip below 0
        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        int thithiIdx = (int) (elongation / 12.0) + 1;
        thithiIdx = Math.min(30, Math.max(1, thithiIdx)); // Absolute boundary clamp

        String paksha = (thithiIdx <= 15) ? ts.getLabel("panchangam.shukla") : ts.getLabel("panchangam.krishna");
        int localizedThithiNum = (thithiIdx > 15) ? thithiIdx - 15 : thithiIdx;

        String rawThithiLabel = ts.getLabel("thithi." + localizedThithiNum);
        String computedThithi;

        // FIX 2: Programmatic splitter resolves the bundle collision for
        // Purnima/Amavasya cleanly
        if (thithiIdx == 15 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[0].trim(); // Extracts "Purnima"
        } else if (thithiIdx == 30 && rawThithiLabel.contains("/")) {
            computedThithi = rawThithiLabel.split("/")[1].trim(); // Extracts "Amavasya"
        } else {
            computedThithi = paksha + " - " + rawThithiLabel; // Standard structure for dates 1-14
        }

        // FIX 3: Safety buffer ensures coordinate additions remain positive across the
        // meridian bounds
        double totalYogaLong = (sunLong + moonLong + 720.0) % 360.0;
        int yogamIdx = (int) (totalYogaLong / (360.0 / 27.0)) + 1;
        String computedYogam = ts.getLabel("yogam." + Math.min(27, Math.max(1, yogamIdx)));

        int karanamIdx = (int) (elongation / 6.0) + 1;
        String computedKaranam = ts.getLabel("karanam." + resolveKaranamId(karanamIdx));

        String resolvedTz = timezoneService.getTimezoneFromCoordinates(pay.latitude(), pay.longitude());
        String place = pay.resolvePlaceName() != null ? pay.resolvePlaceName() : "Chennai, India";

        Map<String, List<ChartResponseDTO.PositionDetail>> suiteMap = new LinkedHashMap<>();
        int[] vargas = { 1, 2, 3, 4, 7, 9, 10, 12, 16, 20, 24, 27, 30, 40, 45, 60 };
        for (int v : vargas) {
            var vargaList = compileVargaList(v, d1, cusps);
            suiteMap.put("D" + v, vargaList);
            suiteMap.put("d" + v, vargaList);
        }
        var bhavaList = compileVargaList(-1, d1, cusps);
        suiteMap.put("Bhava", bhavaList);
        suiteMap.put("bhava", bhavaList);

        List<ChartResponseDTO.PositionDetail> d1PosList = d1.entrySet().stream()
                .map(e -> mapToDetail(e.getKey().toUpperCase(), e.getValue())).collect(Collectors.toList());
        List<DasaPeriod> pdfDasas = dasaEngine.calculateVimshottariTimeline(moonLong, dob);
        int pdfLagnaSign = d1.get("Lagna") != null ? d1.get("Lagna").getSignNumber() : 1;
        int pdfMoonSign = d1.get("Moon") != null ? d1.get("Moon").getSignNumber() : 1;

        var pdfShadbala = shadbalaService.calculateShadbala(d1);
        var pdfHealthProfile = org.vedic.astro.util.AyurvedicAstrologyUtils.calculateHealthProfile(pdfLagnaSign, pdfMoonSign, d1PosList);
        var pdfAyurdayaProfile = org.vedic.astro.util.AyurdayaCalculationUtils.calculateAyurdaya(pdfLagnaSign, pdfMoonSign, d1PosList, pdfDasas, pay.year(), pay.hour(), pay.minute(), pdfShadbala);
        var pdfD9List = compileVargaList(9, d1, cusps);
        var pdfLifeAnchors = buildLifeAnchorsProfile(pdfLagnaSign, pdfMoonSign, d1, pdfD9List, pay, res.getJulianDayUT());

        return ComprehensiveReportDTO.builder()
                .name(res.getName())
                .dateOfBirth(dob.toString())
                .timeOfBirth(String.format("%02d:%02d:%02d", pay.hour(), pay.minute(), pay.second()))
                .localMeanTime(res.getLocalMeanTime())
                .latitude(pay.latitude())
                .longitude(pay.longitude())
                .resolvedTimezone(resolvedTz != null ? resolvedTz : "Asia/Kolkata")
                .placeOfBirth(place)
                .thithi(computedThithi)
                .yogam(computedYogam)
                .karanam(computedKaranam)
                .ayanamsa(pay.ayanamsa() != null ? pay.ayanamsa() : "LAHIRI")
                .panchangamSystem("DRIK_TIRUKANITHAM")
                .birthProfile(buildProfileHeader(d1))
                .birthPlanetaryPositions(d1PosList)
                .vargaChartsMap(suiteMap)
                .vimshottariTimeline(pdfDasas)
                .shadbalaStrengths(shadbalaService.calculateShadbala(d1))
                .structuralDiagnostics(diagnosticsService.runHoroscopeDiagnostics(d1))
                .ayurvedicHealth(pdfHealthProfile)
                .ayurdayaProfile(pdfAyurdayaProfile)
                .lifeAnchors(pdfLifeAnchors)
                .build();
    }

    /**
     * Corrected Traditional Karana Mapping Engine
     * Maps 60 dynamic annual blocks cleanly into the 11 localized translation IDs
     * Karama cycle: Kintughna (1) → [Bava-Vishti repeat 8x] → Sakuni (9) →
     * Chatushpada (10) → Naga (11) → [cycle repeats]
     */
    private int resolveKaranamId(int idx) {
        // 1. First Fixed Karana: Shukla Prathama (1st Half)
        if (idx == 1)
            return 1; // Maps to karanam.1=Kintughna

        // 2. Final Three Fixed Karanas (Indices 58-60)
        if (idx >= 58 && idx <= 60) {
            return idx - 49;
            // 58 - 49 = 9 (Maps to karanam.9=Sakuni)
            // 59 - 49 = 10 (Maps to karanam.10=Chatushpada)
            // 60 - 49 = 11 (Maps to karanam.11=Naga)
        }
        // 3. Repeating Cyclic Chara Karanas (Indices 2 to 57)
        return ((idx - 2) % 7) + 2; // Maps idx 2-57 to karanam.2 through karanam.8
    }

    private ChartResponseDTO.BirthProfile buildProfileHeader(Map<String, PlanetaryPosition> d1) {
        PlanetaryPosition lagna = d1.get("Lagna");
        PlanetaryPosition moon = d1.get("Moon");
        int lagnaSign = (lagna != null) ? lagna.getSignNumber() : 1;
        int moonSign = (moon != null) ? moon.getSignNumber() : 1;
        double moonLong = (moon != null) ? moon.getAbsoluteLongitude() : 0.0;
        int nakNum = (moon != null) ? ZodiacUtils.getNakshatraNumber(moonLong) : 1;
        int pada = (moon != null) ? moon.getPada() : 1;

        return ChartResponseDTO.BirthProfile.builder()
                .lagna(ts.getLocalizedRashi(lagnaSign))
                .rashi(ts.getLocalizedRashi(moonSign))
                .nakshatra(ts.getLocalizedNakshatra(nakNum))
                .nakshatraPada(pada)
                .build();
    }

    private List<ChartResponseDTO.PositionDetail> compileVargaList(int dNo, Map<String, PlanetaryPosition> d1,
            double[] cusps) {
        return d1.entrySet().stream().map(e -> {
            int sig = (dNo == -1) ? vargaEngine.calculateBhavaHouse(e.getValue().getAbsoluteLongitude(), cusps)
                    : vargaEngine.calculateVargaSign(dNo, e.getValue().getSignNumber(), e.getValue().getDegreeInSign(),
                            e.getValue().getAbsoluteLongitude());
            return ChartResponseDTO.PositionDetail.builder().planetKey(e.getKey().toUpperCase())
                    .displayName(ts.getLabel("planet." + e.getKey().toUpperCase() + ".short")).signNumber(sig)
                    .rashiName(ts.getLocalizedRashi(sig)).degreeInSign(e.getValue().getDegreeInSign())
                    .formattedDegree(ZodiacUtils.formatDMS(e.getValue().getDegreeInSign())).build();
        }).collect(Collectors.toList());
    }

    private ChartResponseDTO.PositionDetail mapToDetail(String key, PlanetaryPosition p) {
        return ChartResponseDTO.PositionDetail.builder().planetKey(key)
                .displayName(ts.getLabel("planet." + key + ".short")).signNumber(p.getSignNumber())
                .rashiName(ts.getLocalizedRashi(p.getSignNumber())).degreeInSign(p.getDegreeInSign())
                .formattedDegree(ZodiacUtils.formatDMS(p.getDegreeInSign())).build();
    }

    private LifeAnchorsProfile buildLifeAnchorsProfile(
            int lagnaSign,
            int moonSign,
            Map<String, PlanetaryPosition> d1,
            List<ChartResponseDTO.PositionDetail> d9,
            BirthDetailsDTO pay,
            double julianDay) {

        String lagnaLord = org.vedic.astro.util.PlanetDignityUtils.getSignLord(lagnaSign);
        var numerology = org.vedic.astro.util.NumerologyUtils.calculateNumerology(pay.day(), pay.month(), pay.year(), lagnaLord);
        var luckyDates = org.vedic.astro.util.NumerologyUtils.calculateLuckyDates(numerology.radicalDriverNumber(), moonSign, java.util.Collections.emptyList());
        var deities = org.vedic.astro.util.SpiritualDeityUtils.calculateSpiritualDeities(d1, d9);
        var gemology = org.vedic.astro.util.GemologyEngineUtils.calculateGemologyRecommendation(lagnaSign, d1);
        var structuralBundle = org.vedic.astro.util.StructuralAnchorsUtils.calculateStructuralAnchors(lagnaSign, moonSign, d1, julianDay);

        return new LifeAnchorsProfile(
                numerology,
                structuralBundle.luckyDay(),
                luckyDates,
                structuralBundle.directions(),
                deities,
                gemology,
                structuralBundle.structuralAnchors()
        );
    }
}
