package org.vedic.astro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.vedic.astro.config.GeminiProperties;
import org.vedic.astro.dto.*;
import org.vedic.astro.matching.dto.MatchingAiPredictionDTO;
import org.vedic.astro.matching.dto.MatchingRequestDTO;
import org.vedic.astro.matching.dto.MatchingResponseDTO;
import org.vedic.astro.matching.dto.KootaResultDTO;
import org.vedic.astro.model.DasaPeriod;
import org.vedic.astro.util.AyurdayaCalculationUtils;
import org.vedic.astro.util.AyurvedicAstrologyUtils;
import org.vedic.astro.util.PlanetDignityUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiPredictionService {

    private final GeminiProperties geminiProperties;
    private final PredictionCacheService cacheService;
    private final VargaEngineService vargaEngineService;
    private final DailyPanchangamService dailyPanchangamService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public PredictionResponseDTO generateLifePredictions(PredictionRequestDTO req) {
        if (req == null || req.getBirthDetails() == null || req.getChartData() == null) {
            return PredictionResponseDTO.builder()
                    .enabled(false)
                    .message("Invalid birth details or chart data provided.")
                    .build();
        }

        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        String cacheKey = cacheService.generateLifetimeKey(req.getBirthDetails(), lang);

        // Check 30-Day In-Memory Cache if not forced refresh
        if (!req.isForceRefresh()) {
            PredictionResponseDTO cached = cacheService.getLifetimePrediction(cacheKey);
            if (cached != null) {
                log.info("Returning 30-day cached Lifetime Balan for native: {}", req.getBirthDetails().name());
                return cached;
            }
        }

        if (!geminiProperties.isLifePredictionsEnabled()) {
            log.info("Gemini life predictions disabled or API key absent.");
            return createUnavailableLifeResponse(lang);
        }

        try {
            String systemInstruction = constructSystemInstruction(lang);
            String prompt = constructAstrologicalPrompt(req);
            String rawJson = callGeminiApi(systemInstruction, prompt);
            PredictionResponseDTO parsed = parseGeminiResponse(rawJson, req);
            if (parsed.isEnabled()) {
                cacheService.putLifetimePrediction(cacheKey, parsed);
            }
            return parsed;
        } catch (Exception e) {
            log.error("Failed to generate AI predictions via Gemini: {}", e.getMessage(), e);
            return createUnavailableLifeResponse(lang);
        }
    }

    public DailyBalanDTO generateDailyBalan(DailyBalanRequestDTO req) {
        if (req == null || req.getBirthDetails() == null || req.getChartData() == null) {
            return DailyBalanDTO.builder()
                    .enabled(false)
                    .message("Invalid birth details or chart data provided.")
                    .build();
        }

        String targetDateStr = req.getTargetDate() != null && !req.getTargetDate().isBlank()
                ? req.getTargetDate()
                : LocalDate.now().toString();
        LocalDate targetDate = LocalDate.parse(targetDateStr);
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";

        String cacheKey = cacheService.generateDailyKey(req.getBirthDetails(), targetDateStr, lang);

        // Check End-of-Day Cache if not forced refresh
        if (!req.isForceRefresh()) {
            DailyBalanDTO cached = cacheService.getDailyBalan(cacheKey);
            if (cached != null) {
                log.info("Returning same-day cached Daily Balan for: {} on {}", req.getBirthDetails().name(), targetDateStr);
                return cached;
            }
        }

        DailyPanchangamDTO panchangam = null;
        try {
            PanchangamRequestDTO pReq = new PanchangamRequestDTO(
                    targetDateStr,
                    req.getBirthDetails().getLatitude(),
                    req.getBirthDetails().getLongitude(),
                    req.getBirthDetails().getLocation() != null ? req.getBirthDetails().getLocation().toString() : "Chennai",
                    req.getBirthDetails().getAyanamsa() != null ? req.getBirthDetails().getAyanamsa() : "LAHIRI",
                    lang
            );
            panchangam = dailyPanchangamService.calculateDailyPanchangam(pReq);
        } catch (Exception e) {
            log.warn("Could not calculate daily panchangam for daily balan: {}", e.getMessage());
        }

        if (!geminiProperties.isDailyBalanEnabled()) {
            return createUnavailableDailyResponse(lang, targetDateStr);
        }

        try {
            String systemInstruction = constructDailySystemInstruction(lang);
            String prompt = constructDailyAstrologicalPrompt(req, panchangam, targetDate);
            String rawJson = callGeminiApi(systemInstruction, prompt);
            DailyBalanDTO parsed = parseDailyGeminiResponse(rawJson, req, panchangam, targetDateStr);
            if (parsed.isEnabled()) {
                cacheService.putDailyBalan(cacheKey, parsed, targetDate);
            }
            return parsed;
        } catch (Exception e) {
            log.error("Failed to generate Daily Balan via Gemini: {}", e.getMessage(), e);
            return createUnavailableDailyResponse(lang, targetDateStr);
        }
    }

    public String constructSystemInstruction(String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an elite, classical Vedic Astrologer (Jyotish Guru) versed in Brihat Parasara Hora Shastra, Jataka Parijata, Saravali, and Phaladeepika.\n")
          .append("Your task is to analyze the provided mathematically exact structured JSON astrological matrix and generate a deep, 100% personalized, authentic Vedic Life Balan in the user's selected language: '").append(lang).append("'.\n\n")
          .append("CRITICAL LANGUAGE & SCRIPT DIRECTIVES:\n")
          .append("- You MUST write 100% of all JSON text fields in the native script of language code '").append(lang).append("':\n");
        if ("ta".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Tamil (தமிழ்). Use classical terminology: லக்னாதிபதி, பூர்வ புண்ணியம், யோககாரகன், விம்சோத்தரி திசா புக்தி, கஜகேசரி யோகம், ரோக ஸ்தானம், பரிகாரங்கள்.\n");
        } else if ("hi".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Hindi (हिन्दी). Use classical Vedic terms: लग्नेश, राजयोग, पूर्व पुण्य, दशा-अन्तर्दशा, षष्ठ भाव रोग, वैदिक उपाय.\n");
        } else if ("te".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Telugu (తెలుగు). Use authentic terms: లగ్నాధిపతి, రాజయోగాలు, పూర్వ புణ్యం, దశ అంతర్దశ, రోగ స్థానం, పరిహారాలు.\n");
        } else if ("kn".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Kannada (ಕನ್ನಡ). Use authentic terms: ಲಗ್ನಾಧಿಪತಿ, ರಾಜಯೋಗಗಳು, ಪೂರ್ವ ಪುಣ್ಯ, ದಶಾ ಭುಕ್ತಿ, ಪರಿಹಾರಗಳು.\n");
        } else if ("ml".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Malayalam (മലയാളം). Use authentic terms: ലഗ്നാധിപൻ, രാജയോഗങ്ങൾ, പൂർവ്വ പുണ്യം, ദശാ ഫലങ്ങൾ, പരിഹാരങ്ങൾ.\n");
        } else {
            sb.append("  * Language: English with classical Sanskrit astrological terms in parentheses.\n");
        }
        sb.append("- Output dense, punchy, actionable astrological readings. FORBID repetitive boilerplate or generic optimistic filler across years.\n")
          .append("- TRUTHFULLY AND ACCURATELY predict potential difficulties (job loss, career disruption, acute/chronic illness, surgeries, parental health decline/bereavement, debts) when Maraka/Dusthana/Badhaka/afflicted lords are active.\n")
          .append("- CRITICAL ASTROLOGICAL INTERPRETATION & LORDSHIP RULES:\n")
          .append("  * The input is provided in clean, structured JSON containing the native's details, exact house lordships (House 1-12 signs & lords), unified planetary matrix (D1 physical placement, D9 Navamsa, rulesHouses, lordshipTitle, occupantRole), Ayurvedic health profile (Prakriti, Tattvas, organ vulnerabilities), 12-varga divisional charts (D2, D9, D10, D12, D30), Shadbala, and Dasa-Bhukthi timelines.\n")
          .append("  * PLACEMENT VS. OWNERSHIP: A planet is ONLY the lord of the house(s) listed in 'rulesHouses'. A planet occupying a house is ONLY a guest/occupant (as stated in 'occupantRole'). NEVER call an occupant the lord of that house unless it rules that sign.\n")
          .append("  * D1 VS. D9 DISTINCTION: 'placedInD1Sign' & 'placedInD1House' represent material/physical events in the world. 'placedInD9NavamsaSign' represents internal spiritual/dharma strength. In yearly predictions, NEVER state that a planet is placed in its D9 sign in the material chart.\n")
          .append("  * AYURVEDIC PREDICTIONS: Ground all health, longevity, and vitality readings strictly in the pre-calculated 'ayurvedicHealthProfile' (dominant Prakriti, dosha percentages, and specific organ vulnerabilities). Do NOT default to generic boilerplate.\n")
          .append("  * AYURDAYA (LONGEVITY) DETERMINATION: Ground the longevity classification and lifespan ceiling age strictly in the pre-calculated 'ayurdayaProfile' (longevityClassification, estimatedLifespanCeiling, lifespanRange, and criticalMarakaWindow). Your lifetime forecasts MUST span up to this exact pre-calculated lifespan ceiling.\n")
          .append("  * PER-YEAR ANCHOR INJECTION (ANTI-DRIFT): For EACH year in lifetimePredictions, a 'preComputedAnchor' block is provided containing the exact Dasa Lord and Bhukthi Lord with their house placements (placedInBhava), ruled houses (rulesHouses), Lagna Lord identity (isLagnaLord: true/false), and dignity (d1Dignity). You MUST use ONLY these anchor values when describing planetary roles for that year. NEVER override or contradict the anchor data. The 'isLagnaLord' flag is definitive — if it says false, that planet is NOT the Lagna Lord. The 'lagnaLordReminder' field states the true Lagna Lord for EVERY year — reference it.\n")
          .append("  * YOGAS & DOSHAMS STRICT PARASARI RULES (ZERO FALSE POSITIVES):\n")
          .append("    (1) Gajakesari Yoga REQUIRES Jupiter in a Kendra (1, 4, 7, 10 house) from the MOON (NOT from Lagna unless Moon is also there).\n")
          .append("    (2) Budhaditya Yoga REQUIRES Sun and Mercury in the EXACT SAME SIGN without deep combustion.\n")
          .append("    (3) Pancha Mahapurusha Yogas (Ruchaka, Bhadra, Hamsa, Malavya, Sasa) REQUIRE Mars, Mercury, Jupiter, Venus, or Saturn in a KENDRA (1, 4, 7, 10) from JANMA LAGNA and placed in OWN SIGN or EXALTED SIGN. Placements in 5th, 9th, 2nd, 3rd, 6th, 8th, 11th, 12th do NOT form Mahapurusha yogas.\n")
          .append("    (4) Dharma-Karmadhipati Yoga REQUIRES conjunction or mutual 7th aspect between the exact 9th Lord and 10th Lord of that Lagna.\n")
          .append("    (5) Sevvai/Kuja Dosha in 1,2,4,7,8,12 MUST authentically apply classical cancellations (Own/Exalted sign, Jupiter/Venus aspect, friendly sign Leo/Cancer, etc.). If preCalculatedDiagnostics is provided in the JSON, use it as your verified baseline.\n")
          .append("- Return ONLY valid JSON matching the exact schema specified in the prompt.\n");
        return sb.toString();
    }

    public String constructAstrologicalPrompt(PredictionRequestDTO req) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        int birthYear = b.year();
        int currentYear = LocalDate.now().getYear();
        int currentAge = Math.max(0, currentYear - birthYear);

        Map<String, Object> inputData = new LinkedHashMap<>();

        // 1. Native Identity & Panchangam
        Map<String, Object> nativeInfo = new LinkedHashMap<>();
        nativeInfo.put("name", b.name());
        nativeInfo.put("dob", String.format("%04d-%02d-%02d", b.year(), b.month(), b.day()));
        nativeInfo.put("tob", String.format("%02d:%02d", b.hour(), b.minute()));
        nativeInfo.put("currentAge", currentAge);
        nativeInfo.put("currentYear", currentYear);
        nativeInfo.put("janmaLagna", c.getBirthProfile() != null ? c.getBirthProfile().getLagna() : "");
        nativeInfo.put("janmaRasi", c.getBirthProfile() != null ? c.getBirthProfile().getRashi() : "");
        nativeInfo.put("janmaNakshatra", c.getBirthProfile() != null ? c.getBirthProfile().getNakshatra() : "");
        nativeInfo.put("nakshatraPada", c.getBirthProfile() != null ? c.getBirthProfile().getNakshatraPada() : 1);
        nativeInfo.put("panchangamSystem", c.getPanchangamSystem());
        nativeInfo.put("tithi", c.getThithi());
        nativeInfo.put("yoga", c.getYogam());
        nativeInfo.put("karana", c.getKaranam());
        inputData.put("native", nativeInfo);

        // Determine Lagna sign
        int lagnaSign = 1;
        if (c.getD1Chart() != null) {
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) {
                    lagnaSign = p.getSignNumber();
                    break;
                }
            }
        }

        // 2. Pre-calculated 12 House Lordships with Occupant Planets
        Map<Integer, List<String>> houseOccupants = new HashMap<>();
        for (int h = 1; h <= 12; h++) houseOccupants.put(h, new ArrayList<>());
        if (c.getD1Chart() != null) {
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) continue;
                int house = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
                String name = p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey();
                houseOccupants.get(house).add(name);
            }
        }

        List<Map<String, Object>> houseLordships = new ArrayList<>();
        for (int h = 1; h <= 12; h++) {
            int signNumber = ((lagnaSign - 1 + (h - 1)) % 12) + 1;
            String rasiName = RASHIS[signNumber - 1];
            String lord = PlanetDignityUtils.getSignLord(signNumber);
            List<String> occupants = houseOccupants.get(h);

            Map<String, Object> hObj = new LinkedHashMap<>();
            hObj.put("houseNumber", h);
            hObj.put("signName", rasiName);
            hObj.put("signNumber", signNumber);
            hObj.put("houseLord", lord);
            hObj.put("significance", getHouseSignificance(h));
            hObj.put("occupantPlanets", occupants);
            String clarification = occupants.isEmpty()
                    ? lord + " is the sole lord of House " + h + " (vacant house)."
                    : lord + " is the sole lord of House " + h + ". Occupants " + occupants + " are guests/occupants.";
            hObj.put("lordshipClarification", clarification);
            houseLordships.add(hObj);
        }
        inputData.put("houseLordshipTable", houseLordships);

        // 3. Unified Planetary Matrix (D1 physical, D9 Navamsa, rulesHouses, lordshipTitle, occupantRole, Dignities, Dosha)
        double sunAbsLong = 0.0;
        if (c.getD1Chart() != null) {
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                if ("SUN".equalsIgnoreCase(p.getPlanetKey()) || "SURYA".equalsIgnoreCase(p.getPlanetKey())) {
                    sunAbsLong = (p.getSignNumber() - 1) * 30.0 + p.getDegreeInSign();
                    break;
                }
            }
        }

        Map<String, String> d9Map = new HashMap<>();
        if (c.getD9Chart() != null) {
            for (ChartResponseDTO.PositionDetail p : c.getD9Chart()) {
                d9Map.put(p.getPlanetKey().toUpperCase(), p.getRashiName());
            }
        }

        List<Map<String, Object>> planetaryMatrix = new ArrayList<>();
        if (c.getD1Chart() != null) {
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) continue;

                String pKey = capitalizePlanet(p.getPlanetKey());
                int sign = p.getSignNumber();
                double pAbsLong = (sign - 1) * 30.0 + p.getDegreeInSign();
                int house = ((sign - lagnaSign + 12) % 12) + 1;

                String d1Dignity = "NEUTRAL";
                if (PlanetDignityUtils.isExalted(pKey, sign)) d1Dignity = "EXALTED";
                else if (PlanetDignityUtils.isDebilitated(pKey, sign)) d1Dignity = "DEBILITATED";
                else if (PlanetDignityUtils.isOwnSign(pKey, sign)) d1Dignity = "OWN_SIGN";

                boolean combust = PlanetDignityUtils.isCombust(pKey, pAbsLong, sunAbsLong);
                List<Integer> ruledHouses = getRuledHouses(pKey, lagnaSign);
                String lordshipTitle = getLordshipTitle(pKey, lagnaSign);

                String d9Rasi = d9Map.getOrDefault(p.getPlanetKey().toUpperCase(), "");
                int d9Sign = getRasiIndex(d9Rasi);
                String d9Dignity = "NEUTRAL";
                if (PlanetDignityUtils.isExalted(pKey, d9Sign)) d9Dignity = "EXALTED_NAVAMSA";
                else if (PlanetDignityUtils.isDebilitated(pKey, d9Sign)) d9Dignity = "DEBILITATED_NAVAMSA";
                else if (PlanetDignityUtils.isOwnSign(pKey, d9Sign)) d9Dignity = "OWN_SIGN_NAVAMSA";

                boolean isVargottama = !d9Rasi.isBlank() && p.getRashiName().equalsIgnoreCase(d9Rasi);

                String occupantRole = ruledHouses.contains(house)
                        ? "Placed in House " + house + " (Own House / Swakshetra)"
                        : "Placed in House " + house + " (Occupant/Guest, NOT the " + house + "th Lord)";

                Map<String, Object> pObj = new LinkedHashMap<>();
                pObj.put("planet", p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey());
                pObj.put("placedInD1Sign", p.getRashiName());
                pObj.put("placedInD1House", house);
                pObj.put("occupantRole", occupantRole);
                pObj.put("placedInD9NavamsaSign", d9Rasi);
                pObj.put("isVargottama", isVargottama);
                pObj.put("rulesHouses", ruledHouses);
                pObj.put("lordshipTitle", lordshipTitle);
                pObj.put("d1Dignity", d1Dignity);
                pObj.put("d9Dignity", d9Dignity);
                pObj.put("isCombust", combust);
                pObj.put("primaryDosha", AyurvedicAstrologyUtils.getPlanetaryPrimaryDosha(pKey) + " — " + AyurvedicAstrologyUtils.getPlanetaryTissueSignification(pKey));
                planetaryMatrix.add(pObj);
            }
        }
        inputData.put("planetaryMatrix", planetaryMatrix);

        // 4. Deterministic Ayurvedic Health Profile (Parashara / Charaka Samhita)
        int moonSign = 1;
        if (c.getBirthProfile() != null && c.getBirthProfile().getRashi() != null) {
            moonSign = getRasiIndex(c.getBirthProfile().getRashi());
        }
        AyurvedicAstrologyUtils.AyurvedicHealthProfile healthProfile =
                AyurvedicAstrologyUtils.calculateHealthProfile(lagnaSign, moonSign, c.getD1Chart());
        inputData.put("ayurvedicHealthProfile", healthProfile);

        // 4b. Deterministic Parashara-Jaimini Ayurdaya (Longevity) Profile
        AyurdayaCalculationUtils.AyurdayaProfile ayurdayaProfile =
                AyurdayaCalculationUtils.calculateAyurdaya(lagnaSign, moonSign, c.getD1Chart(), c.getCurrentDasaTimeline(), birthYear);
        inputData.put("ayurdayaProfile", ayurdayaProfile);

        // 4c. Pre-Calculated Horoscopic Diagnostics (Verified Yogas & Doshams)
        if (c.getStructuralDiagnostics() != null) {
            inputData.put("preCalculatedDiagnostics", c.getStructuralDiagnostics());
        }

        // 5. Divisional Vargas (D2, D9, D10, D12, D30)
        Map<String, Object> vargas = new LinkedHashMap<>();
        if (c.getD1Chart() != null && !c.getD1Chart().isEmpty()) {
            Map<String, String> d2 = new LinkedHashMap<>();
            Map<String, String> d10 = new LinkedHashMap<>();
            Map<String, String> d12 = new LinkedHashMap<>();
            Map<String, String> d30 = new LinkedHashMap<>();
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                String key = p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey();
                int d2Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(2, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : (p.getSignNumber() % 2 != 0 ? (p.getDegreeInSign() < 15.0 ? 5 : 4) : (p.getDegreeInSign() < 15.0 ? 4 : 5));
                d2.put(key, d2Sign == 5 ? "Leo(Sun)" : "Cancer(Moon)");

                int d10Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(10, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : ((p.getSignNumber() - 1 + (int)(p.getDegreeInSign() / 3.0)) % 12 + 1);
                d10.put(key, RASHIS[d10Sign - 1]);

                int d12Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(12, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : ((p.getSignNumber() - 1 + (int)(p.getDegreeInSign() / 2.5)) % 12 + 1);
                d12.put(key, RASHIS[d12Sign - 1]);

                int d30Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(30, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : 1;
                d30.put(key, RASHIS[d30Sign - 1]);
            }
            vargas.put("d2Hora", d2);
            if (c.getD9Chart() != null && !c.getD9Chart().isEmpty()) {
                Map<String, String> d9 = new LinkedHashMap<>();
                for (ChartResponseDTO.PositionDetail p : c.getD9Chart()) {
                    d9.put(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(), p.getRashiName());
                }
                vargas.put("d9Navamsa", d9);
            }
            vargas.put("d10Dasamsa", d10);
            vargas.put("d12Dwadasamsa", d12);
            vargas.put("d30Trimsamsa", d30);
        }
        inputData.put("divisionalVargas", vargas);

        // 6. Shadbala Strengths
        if (c.getShadbalaStrengths() != null && c.getShadbalaStrengths().getPlanetStrengths() != null) {
            inputData.put("shadbalaStrengths", c.getShadbalaStrengths().getPlanetStrengths());
        }

        // 7. Dasa & Bhukthi Timelines
        if (c.getCurrentDasaTimeline() != null && !c.getCurrentDasaTimeline().isEmpty()) {
            List<Map<String, Object>> dasas = new ArrayList<>();
            LocalDate now = LocalDate.now();
            for (DasaPeriod d : c.getCurrentDasaTimeline()) {
                if (d.getEndDate() != null && d.getEndDate().isBefore(now.minusYears(2))) continue;
                Map<String, Object> dObj = new LinkedHashMap<>();
                dObj.put("dasa", d.getPlanetName());
                dObj.put("startDate", d.getStartDate() != null ? d.getStartDate().toString() : "");
                dObj.put("endDate", d.getEndDate() != null ? d.getEndDate().toString() : "");
                if (d.getBhukthis() != null && !d.getBhukthis().isEmpty()) {
                    List<Map<String, String>> bhukthis = new ArrayList<>();
                    for (DasaPeriod.BhukthiPeriod bPeriod : d.getBhukthis()) {
                        bhukthis.add(Map.of(
                                "bhukthi", bPeriod.getPlanetName(),
                                "startDate", bPeriod.getStartDate() != null ? bPeriod.getStartDate().toString() : "",
                                "endDate", bPeriod.getEndDate() != null ? bPeriod.getEndDate().toString() : ""
                        ));
                    }
                    dObj.put("activeBhukthis", bhukthis);
                }
                dasas.add(dObj);
            }
            inputData.put("vimshottariTimeline", dasas);
        }

        // 8. Pre-Computed Yearly Anchors (Anti-Drift: Lagna Lord, Dasa/Bhukthi Lord placement per year)
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        String lagnaRasiName = RASHIS[lagnaSign - 1];
        Map<String, Map<String, Object>> planetLookup = new HashMap<>();
        for (Map<String, Object> pObj : planetaryMatrix) {
            String pName = pObj.get("planet").toString();
            planetLookup.put(pName.toLowerCase(), pObj);
        }

        int maxForecastYears = Math.min(100 - currentAge, 30);
        List<Map<String, Object>> yearlyAnchors = new ArrayList<>();
        for (int i = 0; i <= maxForecastYears; i++) {
            int yr = currentYear + i;
            int age = currentAge + i;
            String[] dasaBhukthi = findDasaAndBhukthiForYear(c.getCurrentDasaTimeline(), yr);
            String dasaLordName = dasaBhukthi[0];
            String bhukthiLordName = dasaBhukthi[1];

            Map<String, Object> anchor = new LinkedHashMap<>();
            anchor.put("year", yr);
            anchor.put("age", age);
            anchor.put("dasaBhukthi", dasaLordName + " - " + bhukthiLordName);
            anchor.put("lagnaLordReminder", lagnaLord + " (" + lagnaRasiName + " Lagna)");
            anchor.put("dasaLord", buildPlanetAnchor(dasaLordName, lagnaSign, lagnaLord, planetLookup));
            anchor.put("bhukthiLord", buildPlanetAnchor(bhukthiLordName, lagnaSign, lagnaLord, planetLookup));
            yearlyAnchors.add(anchor);
        }
        inputData.put("preComputedYearlyAnchors", yearlyAnchors);

        String inputJson = "{}";
        try {
            inputJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(inputData);
        } catch (Exception e) {
            log.error("Could not serialize astrological input data to JSON: {}", e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== STRUCTURED ASTROLOGICAL INPUT DATA (JSON) ===\n")
          .append(inputJson).append("\n\n");

        sb.append("=== GENERATION DIRECTIVES ===\n")
          .append("1. 'nativePersonality': Deep core psychological temperament, 3-4 key strengths, and 2-3 vulnerabilities/karmic patterns.\n")
          .append("2. 'healthAnalysis':\n")
          .append("   - 'ayurvedicConstitution': Vata/Pitta/Kapha balance deduced from Lagna, Moon, and 6th house.\n")
          .append("   - 'organVulnerabilities': 2-4 specific vulnerable organs deduced from 6th/8th/12th houses & D30 Trimsamsa.\n")
          .append("   - 'longevityVitalitySummary': Ground your longevity reading in the pre-calculated 'ayurdayaProfile' from the input JSON. Explicitly state the longevity classification (").append(ayurdayaProfile.longevityClassification()).append(") and state the exact calculated lifespan range and ceiling age (e.g. '").append(ayurdayaProfile.lifespanRange()).append("'). Explain the Parashara-Jaimini three-pair evaluation and Kakshya adjustments from the JSON data.\n")
          .append("   - 'recommendedDietAndLifestyle': Targeted Ayurvedic diet and lifestyle practices.\n")
          .append("   - 'PER-YEAR ANCHOR MANDATE': For all longitudinal predictions, rely on the preComputedYearlyAnchors object in the JSON for the correct planetary dignity and placement at any specific age.\n")
          .append("3. 'aiYogas': Calculate and identify ALL classical Vedic Yogas (Gajakesari, Raja Yoga, Dhana Yoga, Vipareeta Raja Yoga, Budhaditya, Neechabhanga, Pancha Mahapurusha, Parivarthana) from the input JSON data with name, forming planets, and lifelong impact.\n")
          .append("4. 'aiDoshams': Evaluate all major doshams (Sevvai/Kuja Dosha, Kala Sarpa Dosha, Pitru Dosha, Papakarthari, Rahu-Ketu afflictions) from the input JSON data, determining whether they are active or nullified, the exact astrological nullification factors, and authentic Vedic remedies.\n")
          .append("5. 'pastKeyPhases': 2-3 pivotal life-defining turning points from birth to present age ").append(currentAge).append(" (periodOrAge, dasaBhukthi, phaseTitle, livedExperience, astrologicalBasis).\n")
          .append("6. 'lifetimePredictions': Exhaustive, year-by-year forecasts covering the native's FULL REMAINING LIFESPAN starting from current year ").append(currentYear).append(" (Age ").append(currentAge).append(") continuously through the EXACT calculated Ayurdaya lifespan age determined in Step 2.\n")
          .append("   - For EACH year, a 'preComputedAnchor' is provided in the 'preComputedYearlyAnchors' section of the input JSON. You MUST cross-reference it for the correct Dasa-Bhukthi lords, their house placements, ruled houses, isLagnaLord flag, and dignity. DO NOT deviate from the anchor data.\n")
          .append("   - For EACH year, you MUST provide 'yearlyTheme', 'detailedPrediction', 'astrologicalBasis' (explicit planetary combinations from D1/D9/D10/D12/D30 & running Dasa-Bhukthi), and 'cautionsAndRemedies'.\n")
          .append("   - 'detailedPrediction' MUST be a deeply articulated, unconstrained narrative synthesized with unbroken lifespan continuity covering ALL 4 core life pillars without omission:\n")
          .append("     (a) Career, Business & Wealth: Promotions, career transitions, entrepreneurial ventures, income trajectory, real estate/property/vehicle purchases, debts or wealth accumulation.\n")
          .append("     (b) Health & Vitality Realities: Specific physical energy, organ health alerts, surgical/hospitalization risks during malefic periods, and vitality recovery phases.\n")
          .append("     (c) Family, Marriage & Progeny: Marital dynamics, relationship harmony, spouse milestones, children's birth/education/achievements.\n")
          .append("     (d) Parents, Elders & Mindset: Father/mother wellbeing (D12), elder care, bereavement risks if indicated during Maraka/Dusthana dasas, and spiritual growth.\n\n")
          .append("Return ONLY valid JSON matching this schema:\n")
          .append("{\n")
          .append("  \"overallSummary\": \"(Comprehensive synthesis)\",\n")
          .append("  \"nativePersonality\": {\n")
          .append("    \"coreTemperament\": \"(Detailed personality)\",\n")
          .append("    \"keyStrengths\": [\"(Strength 1)\", \"(Strength 2)\"],\n")
          .append("    \"vulnerabilitiesAndKarmicLessons\": [\"(Lesson 1)\", \"(Lesson 2)\"]\n")
          .append("  },\n")
          .append("  \"healthAnalysis\": {\n")
          .append("    \"ayurvedicConstitution\": \"(Vata/Pitta/Kapha analysis)\",\n")
          .append("    \"organVulnerabilities\": [\"(Vulnerability 1)\", \"(Vulnerability 2)\"],\n")
          .append("    \"longevityVitalitySummary\": \"(Vitality forecast)\",\n")
          .append("    \"recommendedDietAndLifestyle\": [\"(Guidance 1)\", \"(Guidance 2)\"]\n")
          .append("  },\n")
          .append("  \"aiYogas\": [{ \"name\": \"...\", \"formingPlanets\": \"...\", \"impact\": \"...\" }],\n")
          .append("  \"aiDoshams\": [{ \"name\": \"...\", \"status\": \"...\", \"nullificationFactor\": \"...\", \"remedy\": \"...\" }],\n")
          .append("  \"pastKeyPhases\": [\n")
          .append("    {\n")
          .append("      \"periodOrAge\": \"(e.g. Age 16 - 22 / 2012 - 2018)\",\n")
          .append("      \"dasaBhukthi\": \"(Running Dasa)\",\n")
          .append("      \"phaseTitle\": \"(Phase Milestone Title)\",\n")
          .append("      \"livedExperience\": \"(Turning point, lived struggles and achievements)\",\n")
          .append("      \"astrologicalBasis\": \"(Planetary basis in D1/D9)\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"lifetimePredictions\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(currentYear).append(",\n")
          .append("      \"age\": ").append(currentAge).append(",\n")
          .append("      \"dasaBhukthi\": \"(Dasa-Bhukthi)\",\n")
          .append("      \"yearlyTheme\": \"(Sharp 1-sentence headline for the year)\",\n")
          .append("      \"detailedPrediction\": \"(Rich, multi-dimensional narrative paragraph synthesizing career, wealth, health, marriage, kids, parents, and inner growth with lifespan continuity)\",\n")
          .append("      \"astrologicalBasis\": \"(Explicit planetary reason from D1/D10/D12/D30 and active Dasa lords)\",\n")
          .append("      \"cautionsAndRemedies\": \"(Direct warning and authentic targeted Vedic remedy)\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n");

        return sb.toString();
    }

    public String constructDailySystemInstruction(String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Vedic Astrologer specializing in Gochara (daily planetary transits) and Panchangam synthesis.\n")
          .append("Analyze the native's natal Moon/Lagna matrix and today's planetary transit from the provided structured JSON data to generate a precise, actionable Daily Balan (இன்றைய ராசி பலன்) in language: '").append(lang).append("'.\n")
          .append("Write 100% of all JSON text fields in the native script of '").append(lang).append("'. Output concise, practical, empowering guidance.\n")
          .append("CRITICAL ASTROLOGICAL INTERPRETATION RULES:\n")
          .append("- The input is provided in clean, structured JSON containing the native's details, natal planets and houses, today's transit Moon sign and nakshatra, Tarabalam score, and Gochara Moon house relative to Janma Rasi and Janma Lagna.\n")
          .append("- 'Bhava' (House 1-12) refers to the HOUSE reckoned relative to Lagna (Ascendant = House 1).\n")
          .append("- Strictly interpret transit Moon house positions relative to Janma Rasi and Janma Lagna as calculated in the input JSON.\n")
          .append("- Return ONLY valid JSON matching the schema.\n");
        return sb.toString();
    }

    public String constructDailyAstrologicalPrompt(DailyBalanRequestDTO req, DailyPanchangamDTO panchangam, LocalDate targetDate) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        String lagna = c.getBirthProfile() != null && c.getBirthProfile().getLagna() != null ? c.getBirthProfile().getLagna() : "";
        String rasi = c.getBirthProfile() != null ? c.getBirthProfile().getRashi() : "Mesha";
        String nakshatra = c.getBirthProfile() != null ? c.getBirthProfile().getNakshatra() : "Ashwini";
        String runningDasa = findDasaForYear(c.getCurrentDasaTimeline(), targetDate.getYear());

        String todayMoonRasi = panchangam != null ? panchangam.rashi() : "Transit Moon";
        String todayNakshatra = panchangam != null && panchangam.nakshatra() != null ? panchangam.nakshatra().name() : "";
        String todayTithi = panchangam != null && panchangam.thithi() != null ? panchangam.thithi().name() : "";
        String todayYoga = panchangam != null && panchangam.yogam() != null ? panchangam.yogam().name() : "";

        boolean chandrashtama = panchangam != null && panchangam.chandrastamamNakshatras() != null
                && panchangam.chandrastamamNakshatras().contains(nakshatra);

        DeterministicDailyAnchors anchors = calculateDeterministicAnchors(targetDate, lang);

        Map<String, Object> dailyInput = new LinkedHashMap<>();
        dailyInput.put("targetDate", targetDate.toString());
        dailyInput.put("weekday", targetDate.getDayOfWeek().toString());

        Map<String, Object> nativeInfo = new LinkedHashMap<>();
        nativeInfo.put("name", b.name());
        nativeInfo.put("janmaLagna", lagna);
        nativeInfo.put("janmaRasi", rasi);
        nativeInfo.put("janmaNakshatra", nakshatra);
        nativeInfo.put("runningDasaBhukthi", runningDasa);
        dailyInput.put("native", nativeInfo);

        // Natal Planets & Houses
        if (c.getD1Chart() != null && !c.getD1Chart().isEmpty()) {
            int lagnaSign = 1;
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) {
                    lagnaSign = p.getSignNumber();
                    break;
                }
            }
            List<Map<String, Object>> natalPlanets = new ArrayList<>();
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                int house = ((p.getSignNumber() - lagnaSign + 12) % 12) + 1;
                Map<String, Object> np = new LinkedHashMap<>();
                np.put("planet", p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey());
                np.put("rashi", p.getRashiName());
                np.put("signNumber", p.getSignNumber());
                np.put("houseFromLagna", house);
                natalPlanets.add(np);
            }
            dailyInput.put("natalPlanetsAndHouses", natalPlanets);
        }

        // Tarabalam calculation
        int birthNakNum = getNakshatraIndex(nakshatra);
        int transitNakNum = (panchangam != null && panchangam.nakshatra() != null && panchangam.nakshatra().number() > 0)
                ? panchangam.nakshatra().number()
                : (panchangam != null && panchangam.nakshatra() != null ? getNakshatraIndex(panchangam.nakshatra().name()) : birthNakNum);
        String tarabalamInfo = calculateTarabalam(birthNakNum, transitNakNum, lang);

        // Gochara Moon House calculation
        int birthRasiNum = getRasiIndex(rasi);
        int transitRasiNum = getRasiIndex(todayMoonRasi);
        int moonHouseFromRasi = ((transitRasiNum - birthRasiNum + 12) % 12) + 1;
        String moonHouseMeaning = getGocharaMoonHouseMeaning(moonHouseFromRasi, lang);

        Map<String, Object> gochara = new LinkedHashMap<>();
        gochara.put("transitMoonSign", todayMoonRasi);
        gochara.put("transitNakshatra", todayNakshatra);
        gochara.put("tithi", todayTithi);
        gochara.put("yoga", todayYoga);
        gochara.put("tarabalam", tarabalamInfo);
        gochara.put("transitMoonHouseFromJanmaRasi", moonHouseFromRasi);
        gochara.put("transitMoonHouseSignificance", moonHouseMeaning);
        gochara.put("chandrashtamaActive", chandrashtama);
        dailyInput.put("todayGocharaAndPanchangam", gochara);

        Map<String, String> anchorsMap = new LinkedHashMap<>();
        anchorsMap.put("varaLord", anchors.varaLord);
        anchorsMap.put("luckyColor", anchors.luckyColor);
        anchorsMap.put("luckyNumber", anchors.luckyNumber);
        anchorsMap.put("favorableDirection", anchors.favorableDirection);
        anchorsMap.put("bestTimeWindow", anchors.auspiciousTimeWindow);
        dailyInput.put("fixedDailyAnchors", anchorsMap);

        String dailyJson = "{}";
        try {
            dailyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dailyInput);
        } catch (Exception e) {
            log.error("Could not serialize daily input data to JSON: {}", e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== STRUCTURED DAILY GOCHARA & PANCHANGAM INPUT (JSON) ===\n")
          .append(dailyJson).append("\n\n")
          .append("Return ONLY valid JSON matching this schema:\n")
          .append("{\n")
          .append("  \"generalOutlook\": \"(1-2 sentence overall energy & mood for the day)\",\n")
          .append("  \"careerWork\": \"(Career and workplace opportunities/cautions)\",\n")
          .append("  \"financeWealth\": \"(Financial transactions, expenses, gains)\",\n")
          .append("  \"healthVitality\": \"(Physical stamina and mental wellbeing)\",\n")
          .append("  \"relationshipFamily\": \"(Family and relationship harmony)\",\n")
          .append("  \"luckyColor\": \"").append(anchors.luckyColor).append("\",\n")
          .append("  \"luckyNumber\": \"").append(anchors.luckyNumber).append("\",\n")
          .append("  \"favorableDirection\": \"").append(anchors.favorableDirection).append("\",\n")
          .append("  \"bestTimeWindow\": \"").append(anchors.auspiciousTimeWindow).append("\",\n")
          .append("  \"dailyRemedy\": \"(Simple actionable mantra or prayer for the day)\"\n")
          .append("}\n");

        return sb.toString();
    }

    private String callGeminiApi(String systemInstruction, String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                + geminiProperties.getModel() + ":generateContent?key=" + geminiProperties.getResolvedApiKey();

        Map<String, Object> systemPart = Map.of("text", systemInstruction);
        Map<String, Object> systemInstructionObj = Map.of("parts", List.of(systemPart));

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentObj = Map.of("parts", List.of(textPart));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", geminiProperties.getTemperature());
        generationConfig.put("responseMimeType", "application/json");

        if (geminiProperties.getThinkingBudget() > 0) {
            generationConfig.put("thinkingConfig", Map.of("thinkingBudget", geminiProperties.getThinkingBudget()));
        }

        Map<String, Object> requestBody = Map.of(
                "system_instruction", systemInstructionObj,
                "contents", List.of(contentObj),
                "generationConfig", generationConfig
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }
        throw new RuntimeException("Gemini API call failed with status: " + response.getStatusCode());
    }

    public PredictionResponseDTO parseGeminiResponse(String rawApiResponse, PredictionRequestDTO req) {
        try {
            JsonNode root = objectMapper.readTree(rawApiResponse);

            // Extract usageMetadata
            JsonNode usageNode = root.path("usageMetadata");
            PredictionResponseDTO.TokenUsage tokenUsage = null;
            if (!usageNode.isMissingNode()) {
                int promptTokens = usageNode.path("promptTokenCount").asInt(0);
                int completionTokens = usageNode.path("candidatesTokenCount").asInt(0);
                int totalTokens = usageNode.path("totalTokenCount").asInt(promptTokens + completionTokens);

                String model = geminiProperties.getModel() != null ? geminiProperties.getModel() : "gemini-3.6-flash";
                double promptRate = model.contains("pro") ? 0.00000125 : 0.00000010;
                double completionRate = model.contains("pro") ? 0.00000500 : 0.00000040;

                double costUsd = (promptTokens * promptRate) + (completionTokens * completionRate);
                double costInr = costUsd * 87.0;

                tokenUsage = PredictionResponseDTO.TokenUsage.builder()
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .totalTokens(totalTokens)
                        .estimatedCostUsd(costUsd)
                        .estimatedCostInr(costInr)
                        .modelUsed(model)
                        .build();
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                if (!textNode.isMissingNode()) {
                    String jsonText = textNode.asText().trim();
                    if (jsonText.startsWith("```json")) {
                        jsonText = jsonText.substring(7);
                    }
                    if (jsonText.startsWith("```")) {
                        jsonText = jsonText.substring(3);
                    }
                    if (jsonText.endsWith("```")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 3);
                    }
                    jsonText = jsonText.trim();

                    PredictionResponseDTO parsed = objectMapper.readValue(jsonText, PredictionResponseDTO.class);
                    parsed.setEnabled(true);
                    parsed.setTokenUsage(tokenUsage);
                    parsed.setMessage("AI Life Balan synthesized via Google Gemini.");
                    if (parsed.getFuturePredictions() == null && parsed.getLifetimePredictions() != null) {
                        parsed.setFuturePredictions(parsed.getLifetimePredictions());
                    } else if (parsed.getLifetimePredictions() == null && parsed.getFuturePredictions() != null) {
                        parsed.setLifetimePredictions(parsed.getFuturePredictions());
                    }
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.error("Could not parse Gemini JSON response: {}", e.getMessage(), e);
        }
        return createUnavailableLifeResponse(req.getLanguage() != null ? req.getLanguage() : "ta");
    }

    public DailyBalanDTO parseDailyGeminiResponse(String rawApiResponse, DailyBalanRequestDTO req, DailyPanchangamDTO panchangam, String targetDateStr) {
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        LocalDate targetDate = LocalDate.parse(targetDateStr);
        DeterministicDailyAnchors anchors = calculateDeterministicAnchors(targetDate, lang);

        try {
            JsonNode root = objectMapper.readTree(rawApiResponse);
            JsonNode usageNode = root.path("usageMetadata");
            PredictionResponseDTO.TokenUsage tokenUsage = null;
            if (!usageNode.isMissingNode()) {
                int promptTokens = usageNode.path("promptTokenCount").asInt(0);
                int completionTokens = usageNode.path("candidatesTokenCount").asInt(0);
                int totalTokens = usageNode.path("totalTokenCount").asInt(promptTokens + completionTokens);

                String model = geminiProperties.getModel() != null ? geminiProperties.getModel() : "gemini-3.6-flash";
                double promptRate = model.contains("pro") ? 0.00000125 : 0.00000010;
                double completionRate = model.contains("pro") ? 0.00000500 : 0.00000040;

                double costUsd = (promptTokens * promptRate) + (completionTokens * completionRate);
                double costInr = costUsd * 87.0;

                tokenUsage = PredictionResponseDTO.TokenUsage.builder()
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .totalTokens(totalTokens)
                        .estimatedCostUsd(costUsd)
                        .estimatedCostInr(costInr)
                        .modelUsed(model)
                        .build();
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                if (!textNode.isMissingNode()) {
                    String jsonText = textNode.asText().trim();
                    if (jsonText.startsWith("```json")) jsonText = jsonText.substring(7);
                    if (jsonText.startsWith("```")) jsonText = jsonText.substring(3);
                    if (jsonText.endsWith("```")) jsonText = jsonText.substring(0, jsonText.length() - 3);
                    jsonText = jsonText.trim();

                    DailyBalanDTO parsed = objectMapper.readValue(jsonText, DailyBalanDTO.class);
                    parsed.setEnabled(true);
                    parsed.setTargetDate(targetDateStr);
                    parsed.setRasi(req.getChartData().getBirthProfile() != null ? req.getChartData().getBirthProfile().getRashi() : "");
                    parsed.setNakshatra(req.getChartData().getBirthProfile() != null ? req.getChartData().getBirthProfile().getNakshatra() : "");
                    parsed.setRunningDasaBhukthi(findDasaForYear(req.getChartData().getCurrentDasaTimeline(), targetDate.getYear()));
                    parsed.setChandrashtama(panchangam != null && panchangam.chandrastamamNakshatras() != null
                            && panchangam.chandrastamamNakshatras().contains(parsed.getNakshatra()));
                    parsed.setTokenUsage(tokenUsage);
                    parsed.setLuckyColor(anchors.luckyColor);
                    parsed.setLuckyNumber(anchors.luckyNumber);
                    parsed.setFavorableDirection(anchors.favorableDirection);
                    parsed.setBestTimeWindow(anchors.auspiciousTimeWindow);
                    parsed.setMessage("Daily Balan synthesized successfully via Google Gemini.");
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.error("Could not parse Daily Gemini JSON response: {}", e.getMessage(), e);
        }
        return createUnavailableDailyResponse(lang, targetDateStr);
    }

    public PredictionResponseDTO createUnavailableLifeResponse(String lang) {
        return PredictionResponseDTO.builder()
                .enabled(false)
                .message(getLocalizedUnavailableMessage(lang))
                .build();
    }

    public DailyBalanDTO createUnavailableDailyResponse(String lang, String targetDateStr) {
        return DailyBalanDTO.builder()
                .enabled(false)
                .targetDate(targetDateStr)
                .message(getLocalizedUnavailableMessage(lang))
                .build();
    }

    public static String getLocalizedUnavailableMessage(String lang) {
        if ("ta".equalsIgnoreCase(lang)) {
            return "AI கணிப்பு சேவை தற்போது கிடைக்கவில்லை. சிறிது நேரம் கழித்து மீண்டும் முயற்சிக்கவும்.";
        } else if ("hi".equalsIgnoreCase(lang)) {
            return "एआई भविष्यफल सेवा वर्तमान में उपलब्ध नहीं है। कृपया कुछ समय बाद पुनः प्रयास करें।";
        } else if ("te".equalsIgnoreCase(lang)) {
            return "AI జ్యోతిష్య సేవ ప్రస్తుతం అందుబాటులో లేదు. దయచేసి కాసేపటి తర్వాత మళ్లీ ప్రయత్నించండి.";
        } else if ("kn".equalsIgnoreCase(lang)) {
            return "AI ಭವಿಷ್ಯ ಸೇವೆ ಪ್ರಸ್ತುತ ಲಭ್ಯವಿಲ್ಲ. ದಯವಿಟ್ಟು ಸ್ವಲ್ಪ ಸಮಯದ ನಂತರ ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.";
        } else if ("ml".equalsIgnoreCase(lang)) {
            return "AI പ്രവചന സേവനം ഇപ്പോൾ ലഭ്യമല്ല. ദയവായി അല്പം കഴിഞ്ഞ് വീണ്ടും ശ്രമിക്കുക.";
        }
        return "AI prediction service is currently unavailable. Please try again later.";
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DeterministicDailyAnchors {
        private String varaLord;
        private String luckyColor;
        private String luckyNumber;
        private String favorableDirection;
        private String auspiciousTimeWindow;
    }

    public static DeterministicDailyAnchors calculateDeterministicAnchors(LocalDate targetDate, String lang) {
        java.time.DayOfWeek day = targetDate.getDayOfWeek();
        boolean isTa = "ta".equalsIgnoreCase(lang);
        boolean isHi = "hi".equalsIgnoreCase(lang);
        boolean isTe = "te".equalsIgnoreCase(lang);
        boolean isKn = "kn".equalsIgnoreCase(lang);
        boolean isMl = "ml".equalsIgnoreCase(lang);

        return switch (day) {
            case SUNDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "சூரியன் (Sun)" : "Sun")
                    .luckyColor(isTa ? "தாமரை சிவப்பு / ஆரஞ்சு (Ruby Red)" : (isHi ? "माणिक्य लाल / नारंगी" : (isTe ? "కెంపు ఎరుపు / నారింజ" : (isKn ? "ಮಾಣಿಕ್ಯ ಕೆಂಪು" : (isMl ? "മാണിക്യ ചുവപ്പ്" : "Ruby Red / Deep Orange")))))
                    .luckyNumber("1 & 4")
                    .favorableDirection(isTa ? "கிழக்கு (East)" : (isHi ? "पूर्व (East)" : (isTe ? "తూర్పు (East)" : (isKn ? "ಪೂರ್ವ (East)" : (isMl ? "കിഴക്ക് (East)" : "East")))))
                    .auspiciousTimeWindow(isTa ? "காலை 07:30 - 09:00 (உத்தம நேரம்)" : "07:30 AM - 09:00 AM")
                    .build();
            case MONDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "சந்திரன் (Moon)" : "Moon")
                    .luckyColor(isTa ? "முத்து வெள்ளை / வெள்ளி (Pearl White)" : (isHi ? "मोती सफेद / चांदी" : (isTe ? "ముత్యపు తెలుపు / వెండి" : (isKn ? "ಮುತ್ತಿನ ಬಿಳಿ" : (isMl ? "മുത്ത് വെളുപ്പ്" : "Pearl White / Silver")))))
                    .luckyNumber("2 & 7")
                    .favorableDirection(isTa ? "வடமேற்கு (North-West)" : (isHi ? "उत्तर-पश्चिम (North-West)" : (isTe ? "వాయవ్య (North-West)" : (isKn ? "ವಾಯುವ್ಯ (North-West)" : (isMl ? "വടക്കുപടിഞ്ഞാറ് (North-West)" : "North-West")))))
                    .auspiciousTimeWindow(isTa ? "காலை 06:00 - 07:30 (உத்தம நேரம்)" : "06:00 AM - 07:30 AM")
                    .build();
            case TUESDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "செவ்வாய் (Mars)" : "Mars")
                    .luckyColor(isTa ? "பவள சிவப்பு / அடர் சிவப்பு (Coral Red)" : (isHi ? "मूंगा लाल / सिंदूरी" : (isTe ? "పగడపు ఎరుపు" : (isKn ? "ಹವಳದ ಕೆಂಪು" : (isMl ? "പവിഴ ചുവപ്പ്" : "Coral Red / Crimson")))))
                    .luckyNumber("9 & 1")
                    .favorableDirection(isTa ? "தெற்கு (South)" : (isHi ? "दक्षिण (South)" : (isTe ? "దక్షిణం (South)" : (isKn ? "ದಕ್ಷಿಣ (South)" : (isMl ? "തെക്ക് (South)" : "South")))))
                    .auspiciousTimeWindow(isTa ? "காலை 10:30 - 12:00 (உத்தம நேரம்)" : "10:30 AM - 12:00 PM")
                    .build();
            case WEDNESDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "புதன் (Mercury)" : "Mercury")
                    .luckyColor(isTa ? "மரகத பச்சை / புல் பச்சை (Emerald Green)" : (isHi ? "पन्ना हरा / तोतिया" : (isTe ? "మరకత పచ్చ" : (isKn ? "ಪಚ್ಚೆ ಹಸಿರು" : (isMl ? "മരതക പച്ച" : "Emerald Green / Light Green")))))
                    .luckyNumber("5 & 6")
                    .favorableDirection(isTa ? "வடக்கு (North)" : (isHi ? "उत्तर (North)" : (isTe ? "ఉత్తరం (North)" : (isKn ? "ಉತ್ತರ (North)" : (isMl ? "വടക്ക് (North)" : "North")))))
                    .auspiciousTimeWindow(isTa ? "காலை 09:00 - 10:30 (உத்தம நேரம்)" : "09:00 AM - 10:30 AM")
                    .build();
            case THURSDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "குரு (Jupiter)" : "Jupiter")
                    .luckyColor(isTa ? "பொன் மஞ்சள் / தங்கம் (Golden Yellow)" : (isHi ? "पुखराज पीला / स्वर्णिम" : (isTe ? "బంగారు పసుపు" : (isKn ? "ಚಿನ್ನದ ಹಳದಿ" : (isMl ? "സ്വർണ്ണ മഞ്ഞ" : "Golden Yellow / Amber")))))
                    .luckyNumber("3 & 9")
                    .favorableDirection(isTa ? "வடகிழக்கு (North-East)" : (isHi ? "ईशान / उत्तर-पूर्व (North-East)" : (isTe ? "ఈశాన్యం (North-East)" : (isKn ? "ಈಶಾನ್ಯ (North-East)" : (isMl ? "വടക്കുകിഴക്ക് (North-East)" : "North-East")))))
                    .auspiciousTimeWindow(isTa ? "காலை 09:15 - 10:45 (உத்தம நேரம்)" : "09:15 AM - 10:45 AM")
                    .build();
            case FRIDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "சுக்கிரன் (Venus)" : "Venus")
                    .luckyColor(isTa ? "பட்டு வெள்ளை / கிரீம் (Silk White)" : (isHi ? "चमकीला सफेद / क्रीम" : (isTe ? "పట్టు తెలుపు / క్రీమ్" : (isKn ? "ರೇಷ್ಮೆ ಬಿಳಿ" : (isMl ? "പട്ട് വെളുപ്പ്" : "Silk White / Cream")))))
                    .luckyNumber("6 & 5")
                    .favorableDirection(isTa ? "தென்கிழக்கு (South-East)" : (isHi ? "आग्नेय / दक्षिण-पूर्व (South-East)" : (isTe ? "ఆగ్నేయం (South-East)" : (isKn ? "ಆಗ್ನೇಯ (South-East)" : (isMl ? "തെക്കുകിഴക്ക് (South-East)" : "South-East")))))
                    .auspiciousTimeWindow(isTa ? "காலை 06:30 - 08:00 (உத்தம நேரம்)" : "06:30 AM - 08:00 AM")
                    .build();
            case SATURDAY -> DeterministicDailyAnchors.builder()
                    .varaLord(isTa ? "சனி (Saturn)" : "Saturn")
                    .luckyColor(isTa ? "நீலம் / கருநீலம் (Navy Blue)" : (isHi ? "नीलम नीला / गहरा नीला" : (isTe ? "నీలం / ముదురు నీలం" : (isKn ? "ನೀಲಿ / ಕಡು ನೀಲಿ" : (isMl ? "നീല / കടും നീല" : "Navy Blue / Dark Blue")))))
                    .luckyNumber("8 & 4")
                    .favorableDirection(isTa ? "மேற்கு (West)" : (isHi ? "पश्चिम (West)" : (isTe ? "పడమర (West)" : (isKn ? "ಪಶ್ಚಿಮ (West)" : (isMl ? "പടിഞ്ഞാറ് (West)" : "West")))))
                    .auspiciousTimeWindow(isTa ? "காலை 07:30 - 09:00 (உத்தம நேரம்)" : "07:30 AM - 09:00 AM")
                    .build();
        };
    }

    public PredictionResponseDTO generateOfflineRuleBasedBalan(PredictionRequestDTO req) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        boolean isTa = "ta".equalsIgnoreCase(lang);

        int birthYear = b != null ? b.year() : 1995;
        int currentYear = LocalDate.now().getYear();
        int currentAge = Math.max(0, currentYear - birthYear);

        PredictionResponseDTO.NativePersonality personality = PredictionResponseDTO.NativePersonality.builder()
                .coreTemperament(isTa
                        ? "சுயமரியாதை, நுட்பமான அறிவு மற்றும் ஆழ்ந்த சிந்தனை கொண்டவர். கொள்கை பிடிப்புடன் செயல்படுபவர்."
                        : "High self-esteem, analytical mindset, noble demeanor, and principled decision-making.")
                .keyStrengths(isTa
                        ? List.of("தீர்மானமான தலைமைத்துவ ஆற்றல்", "விரைந்து கற்கும் நுட்பம்", "நிதி நிர்வாகத் திறன்")
                        : List.of("Decisive leadership", "Rapid learning aptitude", "Sound financial judgment"))
                .vulnerabilitiesAndKarmicLessons(isTa
                        ? List.of("அதிக யோசனையால் ஏற்படும் தாமதம்", "செவ்வாய்/சனி காலங்களில் முன்கோபத்தைக் கட்டுப்படுத்துதல்")
                        : List.of("Over-analysis causing occasional delays", "Patience needed during Saturn/Mars transits"))
                .build();

        PredictionResponseDTO.HealthAnalysis health = PredictionResponseDTO.HealthAnalysis.builder()
                .ayurvedicConstitution(isTa ? "பித்த-வாத சமநிலை (Pitta-Vata)" : "Pitta-Vata Balanced Constitution")
                .organVulnerabilities(isTa
                        ? List.of("செரிமானம் மற்றும் வயிறு", "கண் பார்வை & தூக்க சுழற்சி")
                        : List.of("Digestive metabolism & gastric care", "Eye strain and sleep regulation"))
                .longevityVitalitySummary(isTa
                        ? "லக்னாதிபதி பலத்தால் நல்ல ஆயுள் மற்றும் நோய் எதிர்ப்பு சக்தி அமையும்."
                        : "Lagna lord strength grants strong vitality, immune resilience, and good longevity.")
                .recommendedDietAndLifestyle(isTa
                        ? List.of("மிதமான கார உணவு மற்றும் போதுமான நீர் அருந்துதல்", "தினசரி காலை தியானம் அல்லது பிராணாயாமம்")
                        : List.of("Hydration and balanced sattvic diet", "Morning pranayama and regular walking routine"))
                .build();

        List<PredictionResponseDTO.AiYoga> aiYogas = new ArrayList<>();
        aiYogas.add(PredictionResponseDTO.AiYoga.builder()
                .name(isTa ? "கஜகேசரி யோகம் (Gajakesari Yoga)" : "Gajakesari Yoga (Jupiter-Moon Kendra)")
                .formingPlanets(isTa ? "குரு மற்றும் சந்திரன் கேந்திர அமைவு" : "Jupiter in Kendra from Moon")
                .impact(isTa ? "உயர்ந்த அறிவு, சமுதாய நற்பெயர் மற்றும் குரு திசையில் நிலையான பொருளாதார உயர்வு." : "High intellect, noble reputation, and lasting financial growth in Jupiter Dasa.")
                .build());

        List<PredictionResponseDTO.AiDosham> aiDoshams = new ArrayList<>();
        aiDoshams.add(PredictionResponseDTO.AiDosham.builder()
                .name(isTa ? "செவ்வாய் தோஷம் (Kuja / Sevvai Dosha)" : "Sevvai / Kuja Dosha (Mars Placement)")
                .status(isTa ? "தோஷ நிவர்த்தி (Nullified)" : "Nullified by Benefic Aspect")
                .nullificationFactor(isTa ? "செவ்வாய் சுப வீடான மேஷம்/விருச்சிகத்தில் அமைந்ததாலும், குருவின் சுப பார்வையாலும் தோஷம் நிவர்த்தி அடைகிறது." : "Mars is in friendly house and aspected by benefic Jupiter, nullifying adverse effects.")
                .remedy(isTa ? "வைத்தீஸ்வரன் கோவில் வழிபாடு மற்றும் செவ்வாய்க்கிழமை நெய்தீபம் ஏற்றுவது சிறந்தது." : "Chant Angaraka Stotram on Tuesdays or visit Vaitheeswaran Koil.")
                .build());

        List<DasaPeriod> dasas = c != null ? c.getCurrentDasaTimeline() : Collections.emptyList();

        List<PredictionResponseDTO.PastKeyPhase> pastKeyPhases = new ArrayList<>();
        pastKeyPhases.add(PredictionResponseDTO.PastKeyPhase.builder()
                .periodOrAge(isTa ? "வயது 5 முதல் 15 வரை (ஆரம்ப கல்வி & குடும்ப அடித்தளம்)" : "Age 5 to 15 (Early Foundation & Schooling)")
                .dasaBhukthi(dasas != null && !dasas.isEmpty() ? dasas.get(0).getPlanetName() + (isTa ? " திசா" : " Dasa") : (isTa ? "ஆரம்ப திசா" : "Initial Dasa"))
                .phaseTitle(isTa ? "அடிப்படை கல்வி & குடும்ப சூழல்" : "Foundational Learning & Family Environment")
                .livedExperience(isTa
                        ? "குடும்ப சூழல் மற்றும் தொடக்கக் கல்வி சார்ந்த அனுபவங்கள் உங்கள் அடிப்படை குணாதிசயங்களையும் ஒழுக்கத்தையும் வடிவமைத்தன."
                        : "Family environment and schooling shaped your core perseverance and analytical aptitude.")
                .astrologicalBasis(isTa ? "2-ஆம் வீடு (குடும்பம்/வாக்கு) மற்றும் தொடக்க திசா நாதனின் பலம்." : "2nd house of family and foundational Dasa lord placement.")
                .build());

        if (currentAge >= 18) {
            pastKeyPhases.add(PredictionResponseDTO.PastKeyPhase.builder()
                    .periodOrAge(isTa ? "வயது 16 முதல் 24 வரை (உயர் கல்வி & வாழ்க்கை திருப்புமுனை)" : "Age 16 to 24 (Higher Studies & Turning Point)")
                    .dasaBhukthi(dasas != null && dasas.size() > 1 ? dasas.get(1).getPlanetName() + (isTa ? " திசா" : " Dasa") : (isTa ? "திசா மாற்றம்" : "Transition Dasa"))
                    .phaseTitle(isTa ? "கல்வி மாற்றங்களும் சுயமாக முடிவெடுக்கும் ஆற்றலும்" : "Academic Transition & Independent Decision Making")
                    .livedExperience(isTa
                            ? "உயர் கல்வி மற்றும் தொழில் திசை தேர்வில் சவால்கள்; சுய உழைப்பால் தடைகளை கடந்து முன்னேறும் மனப்பக்குவம் உருவானது."
                            : "Navigating transitions in higher education and initial career choices, fostering resilience and independence.")
                    .astrologicalBasis(isTa ? "5-ஆம் வீடு (புத்தி/கல்வி) மற்றும் 9-ஆம் அதிபதியின் சுப பார்வை." : "5th house of intellect and 9th lord aspect.")
                    .build());
        }

        if (currentAge >= 28) {
            pastKeyPhases.add(PredictionResponseDTO.PastKeyPhase.builder()
                    .periodOrAge(isTa ? "வயது 25 முதல் " + currentAge + " வரை (தொழில் ஸ்திரத்தன்மை & அனுபவ முதிர்ச்சி)" : "Age 25 to " + currentAge + " (Career Settlement & Maturity)")
                    .dasaBhukthi(findDasaForYear(dasas, Math.max(birthYear + 25, currentYear - 2)))
                    .phaseTitle(isTa ? "பணியிட மாற்றங்களும் குடும்பப் பொறுப்புகளும்" : "Workplace Consolidation & Domestic Responsibilities")
                    .livedExperience(isTa
                            ? "பணியிடத்தில் புதிய பொறுப்புகள், நிதி நிர்வாகத்தில் அனுபவப் பாடம் மற்றும் குடும்பப் பொறுப்புகளை ஏற்கும் முதிர்ச்சி ஏற்பட்டது."
                            : "Career consolidation, practical financial lessons, and rising family responsibilities.")
                    .astrologicalBasis(isTa ? "10-ஆம் வீடு (கர்ம ஸ்தானம்) மற்றும் D10 தசாம்ச பலன்." : "10th house of career and D10 Dasamsa activation.")
                    .build());
        }

        List<PredictionResponseDTO.YearlyPrediction> predictions = new ArrayList<>();
        int maxForecastYears = Math.min(100 - currentAge, 30);
        for (int i = 0; i <= maxForecastYears; i++) {
            int yr = currentYear + i;
            int age = currentAge + i;
            String runningDasa = findDasaForYear(dasas, yr);
            predictions.add(generateDynamicYearlyPrediction(yr, age, runningDasa, isTa, i));
        }

        String summary = isTa ?
                "ஜாதகத்தில் லக்னாதிபதி மற்றும் சுப கிரகங்களின் அமைப்பால் நற்பலன்கள் உண்டாகும். திசா புக்தி காலங்களில் முறையான முயற்சியும் ஆன்மீக வழிபாடும் உயர்வைத் தரும்." :
                "The planetary alignment of Lagna lord and benefic yogas indicates a prosperous life trajectory. Auspicious Dasa periods bring growth and spiritual fulfillment.";

        return PredictionResponseDTO.builder()
                .enabled(true)
                .message("Generated using Vedic Astrological 12-Varga Synthesizer.")
                .overallSummary(summary)
                .nativePersonality(personality)
                .healthAnalysis(health)
                .aiYogas(aiYogas)
                .aiDoshams(aiDoshams)
                .pastKeyPhases(pastKeyPhases)
                .futurePredictions(predictions)
                .lifetimePredictions(predictions)
                .build();
    }

    private PredictionResponseDTO.YearlyPrediction generateDynamicYearlyPrediction(int yr, int age, String runningDasa, boolean isTa, int idx) {
        String dasa = runningDasa != null ? runningDasa.toLowerCase() : "";
        String theme;
        String astroBasis;
        String careerFin;
        String healthFam;
        String cautionsRem;

        if (dasa.contains("saturn") || dasa.contains("சனி") || dasa.contains("shani")) {
            theme = isTa ? "கடின உழைப்பு, புதிய பொறுப்புகள் மற்றும் நிதி மறுசீரமைப்பு ஆண்டு." : "High perseverance, structured workload, and financial consolidation.";
            astroBasis = isTa ? "சனி திசா/புக்தியின் கர்ம வினைகள் மற்றும் உழைப்புக்குரிய பலன்." : "Saturn's karmic discipline and structural realignment.";
            careerFin = isTa ? "பணியிடத்தில் கூடுதல் பொறுப்புகள். அவசர முடிவுகளையோ தேவையற்ற விவாதங்களையோ தவிர்க்கவும்; புதிய கடன் வாங்குவதை தவிர்க்கவும்." : "Heavy workplace deliverables; avoid friction with seniors. Guard against unbudgeted debts.";
            healthFam = isTa ? "மூட்டு வலி, நரம்பு அல்லது தூக்கமின்மை சோர்வு. மூத்த குடும்பத்தினர்/பெற்றோர் உடல்நலத்தில் மிகுந்த கவனம் தேவை." : "Joint/nervous fatigue; manage posture and sleep. Special attention needed for elderly parents' health.";
            cautionsRem = isTa ? "எச்சரிக்கை: அவசர வணிக முடிவுகள் வேண்டாம். பரிகாரம்: சனிக்கிழமை நல்லெண்ணெய் தீபம் ஏற்றி அனுமனை வழிபடவும்." : "Caution: Avoid hasty speculative investments. Remedy: Chant Hanuman Chalisa on Saturdays.";
        } else if (dasa.contains("rahu") || dasa.contains("ராகு")) {
            theme = isTa ? "திடீர் மாற்றங்கள், எதிர்பாராத பயணம் அல்லது தொழில் இடப்பெயர்ச்சி ஆண்டு." : "Unconventional expansion, sudden relocations, and digital/foreign prospects.";
            astroBasis = isTa ? "ராகுவின் மாயா சக்தியால் புதிய வாய்ப்புகளும் திடீர் ஏற்ற இறக்கங்களும்." : "Rahu triggering rapid paradigm shifts and unconventional growth.";
            careerFin = isTa ? "தொழில்நுட்பம் அல்லது புதிய துறையில் முன்னேற்றம்; ஆனால் வேலை மாற்றம் அல்லது பணி அமைப்பில் திடீர் மாற்றம் ஏற்படலாம். நிதி பரிவர்த்தனைகளில் ஏமாற்றங்களை தவிர்க்கவும்." : "Breakthrough in modern/tech avenues, but beware of sudden job disruption or deceptive financial contracts.";
            healthFam = isTa ? "அதிக சிந்தனையால் மன உளைச்சல் மற்றும் ஒவ்வாமை. குடும்பத்தில் வெளிப்படையான பேச்சை கடைபிடிக்கவும்." : "Restlessness, sleep disruption, and digestive allergies. Cultivate open communication at home.";
            cautionsRem = isTa ? "எச்சரிக்கை: புதிய முதலீடுகளில் ஆவணங்களை முழுமையாக சரிபார்க்கவும். பரிகாரம்: துர்க்கை அம்மன் வழிபாடு மற்றும் ராகுகால எலுமிச்சை தீபம்." : "Caution: Verify all legal documents before signing. Remedy: Offer prayers to Goddess Durga on Tuesdays.";
        } else if (dasa.contains("jupiter") || dasa.contains("குரு") || dasa.contains("guru")) {
            theme = isTa ? "பொருளாதார வளர்ச்சி, சுப காரியங்கள் மற்றும் நற்பெயர் உயரும் பொற்காலம்." : "Auspicious expansion, financial elevation, and family milestones.";
            astroBasis = isTa ? "சுப கிரகமான குருவின் தர்ம பார்வை மற்றும் கேந்திர யோகம்." : "Benefic Jupiter's expansive aspect and dharma trikona activation.";
            careerFin = isTa ? "பதவி உயர்வு, தொழில் விரிவாக்கம் மற்றும் D2 ஹோரா பலத்தால் நிலையான சொத்து/சேமிப்பு உயர்வு." : "Executive recognition, profitable venture expansion, and strong asset acquisition.";
            healthFam = isTa ? "குடும்பத்தில் திருமணம், புத்திர பாக்கியம் போன்ற மங்கல நிகழ்வுகள். உடல் ஆரோக்கியமும் மன அமைதியும் மேலோங்கும்." : "Domestic bliss, celebratory family milestones, and vibrant physical vitality.";
            cautionsRem = isTa ? "எச்சரிக்கை: அதிகப்படியான நம்பிக்கையால் மற்றவர்களுக்கு ஜாமீன் கொடுப்பதை தவிர்க்கவும். பரிகாரம்: வியாழக்கிழமை தட்சிணாமூர்த்தி வழிபாடு." : "Caution: Avoid standing guarantee for third-party loans. Remedy: Offer yellow flowers to Dakshinamurthy.";
        } else if (dasa.contains("mars") || dasa.contains("செவ்வாய்") || dasa.contains("kuja")) {
            theme = isTa ? "தைரியம், பூமி/சொத்து சேர்க்கை மற்றும் வேகமான முன்னேற்ற ஆண்டு." : "Bold enterprise, property acquisitions, and dynamic initiative.";
            astroBasis = isTa ? "செவ்வாயின் காரகத்துவமான பூமி பலம் மற்றும் உக்கிர சக்தி." : "Mars activating real estate prospects and assertive vitality.";
            careerFin = isTa ? "ரியல் எஸ்டேட், பொறியியல் துறைகளில் வெற்றி. அதிக ரிஸ்க் உள்ள ஊக வணிகங்களில் நஷ்ட அபாயம் உள்ளதால் நிதானம் தேவை." : "Advancement in property/engineering; exercise restraint in high-risk speculative trading.";
            healthFam = isTa ? "இரத்த அழுத்தம், உஷ்ணம் அல்லது சிறிய காயங்கள்/அறுவைசிகிச்சை கவனம் தேவை. உடன்பிறந்தோருடன் நல்லிணக்கம் காக்கவும்." : "Heat-related fatigue, blood pressure, or minor injury caution. Maintain cordial ties with siblings.";
            cautionsRem = isTa ? "எச்சரிக்கை: வாகன இயக்கத்திலும் முன்கோபத்திலும் கூடுதல் கவனம். பரிகாரம்: செவ்வாய்க்கிழமை முருகப்பெருமான் வழிபாடு." : "Caution: Drive defensively and manage impulsive anger. Remedy: Chant Kanda Sashti Kavasam on Tuesdays.";
        } else if (dasa.contains("venus") || dasa.contains("சுக்கிரன்") || dasa.contains("sukra")) {
            theme = isTa ? "கலை, ஆடம்பர பொருட்கள், புதிய வாகனம் மற்றும் தாம்பத்திய மகிழ்ச்சி ஆண்டு." : "Comforts, vehicle acquisition, creative achievements, and relationship joy.";
            astroBasis = isTa ? "சுக்கிரனின் சுப பார்வை மற்றும் களத்திர ஸ்தான அனுகூலம்." : "Venusian grace enhancing domestic harmony and lifestyle comforts.";
            careerFin = isTa ? "வணிகம், கலை, டிசைனிங் துறைகளில் நற்பலன்; புதிய வருமான வழிகளும் நகை/ஆபரண சேர்க்கையும் உண்டாகும்." : "Prosperity in design/creative commerce; new revenue streams and luxury acquisitions.";
            healthFam = isTa ? "குடும்பத்தில் சுப காரியங்கள் கூடிவரும். இனிப்பு மற்றும் உணவு உட்கொள்ளலில் கட்டுப்பாடு தேவை." : "Domestic celebrations and joyous milestones. Balance sugar and diet.";
            cautionsRem = isTa ? "எச்சரிக்கை: ஆடம்பர செலவுகளை திட்டமிட்டு கையாளவும். பரிகாரம்: வெள்ளிக்கிழமை மகாலட்சுமி வழிபாடு." : "Caution: Keep discretionary lifestyle spending budgeted. Remedy: Offer white flowers to Goddess Lakshmi.";
        } else if (dasa.contains("mercury") || dasa.contains("புதன்") || dasa.contains("budha")) {
            theme = isTa ? "புதிய அறிவு, தகவல் தொடர்பு, வியாபார லாபம் மற்றும் ஒப்பந்தங்கள் கூடிவரும் ஆண்டு." : "Intellectual excellence, commercial trade expansion, and profitable agreements.";
            astroBasis = isTa ? "புதனின் வித்யா காரகத்துவம் மற்றும் வியாபார ஸ்தான அனுகூலம்." : "Mercury activating commercial intellect and communication channels.";
            careerFin = isTa ? "புதிய தொழில் ஒப்பந்தங்கள், கணக்கியல்/ஐடி துறையில் அபார வளர்ச்சி மற்றும் வர்த்தக லாபம்." : "Signing profitable commercial agreements, IT/analytical success, and business growth.";
            healthFam = isTa ? "நரம்பு மண்டலம் மற்றும் கண் பார்வையில் அக்கறை தேவை. குடும்பத்தில் சுமுகமான பேச்சுவார்த்தை வெற்றி தரும்." : "Eye and nervous care; maintain balanced screen time. Intellectual harmony in family discussions.";
            cautionsRem = isTa ? "எச்சரிக்கை: வாய்மொழி ஒப்பந்தங்களை தவிர்த்து எழுத்துப்பூர்வ ஆவணங்களை பயன்படுத்தவும். பரிகாரம்: புதன்கிழமை பெருமாள் வழிபாடு." : "Caution: Rely on written contracts rather than verbal assurances. Remedy: Visit Lord Vishnu temple on Wednesdays.";
        } else if (dasa.contains("sun") || dasa.contains("சூரியன்") || dasa.contains("surya")) {
            theme = isTa ? "அரசு அனுகூலம், சமூக அங்கீகாரம், தலைமைப் பதவி மற்றும் அதிகார உயர்வு." : "Authoritative recognition, governmental support, and leadership elevation.";
            astroBasis = isTa ? "ஆத்மகாரகனான சூரியனின் பலம் மற்றும் 1-ஆம்/10-ஆம் அதிபதியின் பார்வை." : "Solar vitality activating executive authority and public recognition.";
            careerFin = isTa ? "நிர்வாகப் பொறுப்புகள், அரசு வழி காரிய அனுகூலம் மற்றும் கௌரவப் பதவி கிட்டும்." : "Promotions into senior managerial roles, official approvals, and career prestige.";
            healthFam = isTa ? "உடல் வலிமை கூடும்; கண் பார்வை மற்றும் தந்தையாரின் உடல்நலத்தில் கூடுதல் கவனம் செலுத்தவும்." : "High vitality and stamina; ensure regular health checkups for father/elders.";
            cautionsRem = isTa ? "எச்சரிக்கை: அதிகார மட்டத்தில் ஈகோ மோதல்களை தவிர்க்கவும். பரிகாரம்: தினமும் காலையில் ஆதித்ய ஹிருதய ஸ்தோத்திரம் பாராயணம்." : "Caution: Avoid ego clashes with higher authorities. Remedy: Chant Aditya Hridaya Stotram at sunrise.";
        } else if (dasa.contains("moon") || dasa.contains("சந்திரன்") || dasa.contains("chandra")) {
            theme = isTa ? "மன அமைதி, புதிய பயணங்கள், மக்கள் தொடர்பு மற்றும் தாய்வழி அனுகூலம்." : "Mental serenity, fruitful travels, public goodwill, and maternal harmony.";
            astroBasis = isTa ? "மனோகாரகனான சந்திரனின் சுப கதி மற்றும் 4-ஆம் வீட்டு பலம்." : "Lunar tranquility activating 4th house comforts and intuitive clarity.";
            careerFin = isTa ? "மக்கள் தொடர்பு, உணவு/மருந்து, வெளிநாட்டு வர்த்தகத்தில் நல்ல லாபம் மற்றும் புதிய தொடர்புகள்." : "Gains in public-facing, healthcare, or foreign trade ventures.";
            healthFam = isTa ? "மனத்தெளிவு உண்டாகும்; சளி, நீர் சம்பந்தமான தொந்தரவுகளில் எச்சரிக்கை. தாயாரின் ஆசி நலம் சேர்க்கும்." : "Emotional well-being; guard against respiratory congestion. Maternal support brings peace.";
            cautionsRem = isTa ? "எச்சரிக்கை: உணர்ச்சிவசப்பட்டு முக்கிய முடிவுகள் எடுப்பதை தவிர்க்கவும். பரிகாரம்: திங்கட்கிழமை சிவபெருமான் வழிபாடு." : "Caution: Avoid making major financial commitments purely on impulse. Remedy: Offer milk abhishekam to Lord Shiva.";
        } else { // Ketu or default
            theme = isTa ? "ஆராய்ச்சி, ஆன்மீக விழிப்புணர்வு, யோகா/தியான ஈடுபாடு மற்றும் தத்துவ சிந்தனைகள்." : "Spiritual depth, analytical research, and detachment from stressful materialism.";
            astroBasis = isTa ? "ஞானகாரகனான கேதுவின் 12-ஆம் வீட்டு தொடர்பு மற்றும் ஆத்ம சிந்தனை." : "Ketu's introspective energy granting research clarity and inner peace.";
            careerFin = isTa ? "ஆராய்ச்சி, மருத்துவம், ஆன்மீகம், ஆலோசனை துறைகளில் நல்ல முன்னேற்றம். மறைமுக எதிர்ப்புகள் விலகும்." : "Progress in research, consultancy, or specialized diagnostics; hidden obstacles dissolve.";
            healthFam = isTa ? "தோல், ஒவ்வாமை மற்றும் மன அமைதிக்கான தியானம் அவசியம். குடும்ப விவகாரங்களில் பக்குவமான அணுகுமுறை தேவை." : "Skin and allergy care; daily meditation enhances peace. Maintain balanced domestic relationships.";
            cautionsRem = isTa ? "எச்சரிக்கை: அவநம்பிக்கை மற்றும் தனிமை உணர்வை தவிர்த்து சுறுசுறுப்பாக இருக்கவும். பரிகாரம்: விநாயகர் வழிபாடு மற்றும் சங்கடஹர சதுர்த்தி விரதம்." : "Caution: Avoid excessive self-isolation. Remedy: Worship Lord Ganesha with Arugampul on Sankatahara Chaturthi.";
        }

        return PredictionResponseDTO.YearlyPrediction.builder()
                .year(yr)
                .age(age)
                .dasaBhukthi(runningDasa)
                .yearlyTheme(theme)
                .astrologicalBasis(astroBasis)
                .careerAndFinance(careerFin)
                .healthAndFamily(healthFam)
                .cautionsAndRemedies(cautionsRem)
                .personalMindset(theme)
                .careerProfession(careerFin)
                .careerFinance(careerFin)
                .wealthFinance(careerFin)
                .healthVitality(healthFam)
                .marriageFamily(healthFam)
                .familyMarriage(healthFam)
                .parentsKids(healthFam)
                .favorableVsCaution(cautionsRem)
                .remediesGuidance(cautionsRem)
                .build();
    }

    public DailyBalanDTO generateOfflineRuleBasedDailyBalan(DailyBalanRequestDTO req, DailyPanchangamDTO panchangam, LocalDate targetDate) {
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        boolean isTa = "ta".equalsIgnoreCase(lang);

        String rasi = req.getChartData() != null && req.getChartData().getBirthProfile() != null
                ? req.getChartData().getBirthProfile().getRashi() : "Rasi";
        String nakshatra = req.getChartData() != null && req.getChartData().getBirthProfile() != null
                ? req.getChartData().getBirthProfile().getNakshatra() : "Nakshatra";
        String runningDasa = findDasaForYear(req.getChartData() != null ? req.getChartData().getCurrentDasaTimeline() : null, targetDate.getYear());

        boolean chandrashtama = panchangam != null && panchangam.chandrastamamNakshatras() != null
                && panchangam.chandrastamamNakshatras().contains(nakshatra);

        return DailyBalanDTO.builder()
                .enabled(true)
                .targetDate(targetDate.toString())
                .rasi(rasi)
                .nakshatra(nakshatra)
                .runningDasaBhukthi(runningDasa)
                .chandrashtama(chandrashtama)
                .generalOutlook(isTa
                        ? (chandrashtama ? "இன்று சந்திராஷ்டம நாள். அமைதியும் விழிப்புணர்வும் தேவை. புதிய முயற்சிகளைத் தவிர்க்கவும்." : "இன்று உற்சாகமும் காரிய சித்தியும் தரும் இனிய நாள். எடுத்த காரியங்கள் வெற்றியாகும்.")
                        : (chandrashtama ? "Chandrashtama day. Exercise patience, avoid disputes, and defer major new starts." : "Auspicious, energetic day with favorable transit support for planned tasks."))
                .careerWork(isTa
                        ? "பணியிடத்தில் உங்கள் யோசனைகளுக்கு நல்ல வரவேற்பு இருக்கும். மேலதிகாரிகளின் பாராட்டு கிட்டும்."
                        : "Productive workday. Clear communication and supportive colleagues facilitate swift execution.")
                .financeWealth(isTa
                        ? "எதிர்பார்த்த தனவரவு உண்டு. வீண் செலவுகளைக் குறைப்பது நல்லது."
                        : "Favorable cashflow. Prudent spending ensures financial stability.")
                .healthVitality(isTa
                        ? "உடல் நலம் நன்று. போதுமான ஓய்வும் நீர் அருந்துதலும் புத்துணர்ச்சி தரும்."
                        : "Good vitality. Maintain hydration and take short mindful breaks.")
                .relationshipFamily(isTa
                        ? "குடும்பத்தினருடன் மனமகிழ்ச்சி தரும் உரையாடல்கள் அமையும்."
                        : "Pleasant interactions and domestic harmony with loved ones.")
                .luckyColor(isTa ? "மஞ்சள் / பொன்னிறம் (Yellow/Gold)" : "Gold / Yellow")
                .luckyNumber("3 & 7")
                .favorableDirection(isTa ? "வடகிழக்கு (North-East)" : "North-East")
                .bestTimeWindow(isTa ? "காலை 09:15 - 10:30 (உத்தம நேரம்)" : "09:15 AM - 10:30 AM (Auspicious)")
                .dailyRemedy(isTa
                        ? "ஸ்ரீ விநாயகர் வழிபாடு செய்து தீபம் ஏற்றுவது காரிய தடையை நீக்கும்."
                        : "Offer prayers to Lord Ganesha and chant Om Gam Ganapataye Namaha.")
                .message("Generated using Vedic Gochara Rule Synthesizer.")
                .build();
    }

    private String findDasaForYear(List<DasaPeriod> dasas, int year) {
        if (dasas == null || dasas.isEmpty()) return "Vedic Dasa Period";
        LocalDate date = LocalDate.of(year, 6, 15);
        for (DasaPeriod d : dasas) {
            if (!date.isBefore(d.getStartDate()) && !date.isAfter(d.getEndDate())) {
                if (d.getBhukthis() != null) {
                    for (DasaPeriod.BhukthiPeriod b : d.getBhukthis()) {
                        if (!date.isBefore(b.getStartDate()) && !date.isAfter(b.getEndDate())) {
                            return d.getPlanetName() + " - " + b.getPlanetName();
                        }
                    }
                }
                return d.getPlanetName() + " Mahadasa";
            }
        }
        return dasas.get(0).getPlanetName() + " Dasa";
    }

    /**
     * Returns [dasaLordName, bhukthiLordName] for a given year.
     * If no bhukthi is found, bhukthiLordName defaults to the dasa lord.
     */
    public static String[] findDasaAndBhukthiForYear(List<DasaPeriod> dasas, int year) {
        if (dasas == null || dasas.isEmpty()) return new String[]{"Dasa", "Bhukthi"};
        LocalDate date = LocalDate.of(year, 6, 15);
        for (DasaPeriod d : dasas) {
            if (d.getStartDate() != null && d.getEndDate() != null
                    && !date.isBefore(d.getStartDate()) && !date.isAfter(d.getEndDate())) {
                String dasaLord = d.getPlanetName();
                if (d.getBhukthis() != null) {
                    for (DasaPeriod.BhukthiPeriod b : d.getBhukthis()) {
                        if (b.getStartDate() != null && b.getEndDate() != null
                                && !date.isBefore(b.getStartDate()) && !date.isAfter(b.getEndDate())) {
                            return new String[]{dasaLord, b.getPlanetName()};
                        }
                    }
                }
                return new String[]{dasaLord, dasaLord};
            }
        }
        return new String[]{dasas.get(0).getPlanetName(), dasas.get(0).getPlanetName()};
    }

    /**
     * Builds a compact anchor object for a planet (Dasa Lord or Bhukthi Lord) containing:
     * planet name, placedInBhava, rulesHouses, isLagnaLord, d1Dignity.
     */
    public static Map<String, Object> buildPlanetAnchor(
            String planetName, int lagnaSign, String lagnaLord,
            Map<String, Map<String, Object>> planetLookup) {
        Map<String, Object> anchor = new LinkedHashMap<>();
        anchor.put("planet", planetName);

        Map<String, Object> matrixEntry = planetLookup.get(planetName.toLowerCase());
        if (matrixEntry != null) {
            anchor.put("placedInBhava", matrixEntry.get("placedInD1House"));
            anchor.put("rulesHouses", matrixEntry.get("rulesHouses"));
            anchor.put("d1Dignity", matrixEntry.get("d1Dignity"));
        } else {
            // Shadow nodes (Rahu/Ketu) or unresolved planets
            List<Integer> ruledHouses = getRuledHouses(planetName, lagnaSign);
            anchor.put("placedInBhava", 0);
            anchor.put("rulesHouses", ruledHouses);
            anchor.put("d1Dignity", "NEUTRAL");
        }

        anchor.put("isLagnaLord", planetName.equalsIgnoreCase(lagnaLord));
        return anchor;
    }

    private String getTamilPastMilestoneTitle(int age) {
        if (age <= 7) return "தொடக்கக் கல்வி & பால பருவம்";
        if (age <= 16) return "பள்ளிக் கல்வி & வளர்ச்சி";
        if (age <= 22) return "உயர்கல்வி & பட்டப்படிப்பு";
        if (age <= 26) return "தொழில் தொடக்கம் & முதல் வருமானம்";
        if (age <= 32) return "திருமணம் / குடும்ப வாழ்க்கை / இடமாற்றம்";
        if (age <= 38) return "பொருளாதார வளர்ச்சி & தொழில் முன்னேற்றம்";
        return "வாழ்வின் முக்கிய சாதனைகள் & ஆன்மீகம்";
    }

    private String getEnglishPastMilestoneTitle(int age) {
        if (age <= 7) return "Early Childhood & Schooling";
        if (age <= 16) return "Secondary Education & Key Growth";
        if (age <= 22) return "Higher Education & Graduation";
        if (age <= 26) return "Career Launch & Financial Independence";
        if (age <= 32) return "Marriage / Domestic Milestones / Relocation";
        if (age <= 38) return "Professional Growth & Financial Stability";
        return "Key Achievements & Maturity";
    }

    private String getTamilPastMilestoneDesc(int age, String dasa) {
        return "வயது " + age + " இல் " + dasa + " காலகட்டத்தில் அமைந்த முக்கிய வாழ்க்கை நிகழ்வு.";
    }

    private String getEnglishPastMilestoneDesc(int age, String dasa) {
        return "Significant milestone at age " + age + " influenced by " + dasa + " period.";
    }

    // ==========================================
    // AI-BASED MARRIAGE MATCHING ENGINE
    // ==========================================

    public MatchingAiPredictionDTO generateMarriageMatchingAiAnalysis(
            MatchingRequestDTO req,
            MatchingResponseDTO classicalResult,
            String lang,
            boolean forceRefresh) {

        if (req == null || req.boy() == null || req.girl() == null) {
            return MatchingAiPredictionDTO.builder()
                    .enabled(false)
                    .message("Invalid birth details provided for marriage compatibility analysis.")
                    .build();
        }

        String effectiveLang = lang != null ? lang : "ta";
        String cacheKey = cacheService.generateMatchingKey(req, effectiveLang);

        // Check 3-Hour Cache if not forced refresh
        if (!forceRefresh) {
            MatchingAiPredictionDTO cached = cacheService.getMatchingPrediction(cacheKey);
            if (cached != null) {
                log.info("Returning 3-hour cached AI Marriage Compatibility for {} & {}", req.boy().name(), req.girl().name());
                return cached;
            }
        }

        if (!geminiProperties.isMatchingEnabled()) {
            log.info("Gemini marriage matching is disabled or API key is absent.");
            return createUnavailableMatchingResponse(effectiveLang);
        }

        try {
            String systemInstruction = constructMatchingSystemInstruction(effectiveLang);
            String prompt = constructMatchingPrompt(req, classicalResult);
            String rawJson = callGeminiApi(systemInstruction, prompt);
            MatchingAiPredictionDTO parsed = parseMatchingGeminiResponse(rawJson, effectiveLang, req, classicalResult);
            if (parsed.isEnabled()) {
                cacheService.putMatchingPrediction(cacheKey, parsed);
            }
            return parsed;
        } catch (Exception e) {
            log.error("Failed to generate AI Marriage Compatibility via Gemini: {}", e.getMessage(), e);
            return createUnavailableMatchingResponse(effectiveLang);
        }
    }

    public String constructMatchingSystemInstruction(String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a revered Vedic Marriage Astrologer (Vivaha Jyotish Acharya) with masterful command of Brihat Parasara Hora Shastra, Prasna Marga, Muhurtha Chintamani, and Jathaka Porutham.\n")
          .append("Your task is to analyze the complete dual-horoscope structured JSON matrix (D1, D9 Navamsa, house lordships, and classical Koota results) for the Boy and Girl to provide an authoritative, deep, compassionate, and authentic Vedic Marriage Compatibility Analysis in language '").append(lang).append("'.\n\n")
          .append("CRITICAL REQUIREMENTS:\n")
          .append("- Write 100% of all JSON text values in the native script of '").append(lang).append("'.\n")
          .append("- Rigorously apply all authentic classical nullifications from the JSON matrix (e.g. Kuja Dosha cancellation if Mars is in own/exalted sign, in friendly signs, or if both charts possess balanced Kuja Dosha; Rajju exceptions when nakshatras have different padas; Gana Dosha cancellation if Rasi lords are identical or friendly).\n")
          .append("- Provide profound psychological, financial, health, progeny, and spiritual insight rather than generic cliches.\n")
          .append("- CRITICAL ASTROLOGICAL INTERPRETATION & LORDSHIP RULES:\n")
          .append("  * The input is provided in clean, structured JSON containing the Boy's and Girl's birth details, exact house lordships, D1 planetary placements with houses and dignities, D9 Navamsa positions, and classical Koota breakdown.\n")
          .append("  * 'Bhava' (House 1-12) refers to the HOUSE reckoned relative to Lagna (Ascendant = House 1).\n")
          .append("  * Strictly use Bhava (House) positions for all house-based lordships and functional analysis (7th house for marriage, 2nd/7th for maraka, 5th for progeny, 8th for longevity).\n")
          .append("- Return ONLY valid JSON matching the exact schema specified in the prompt.\n");
        return sb.toString();
    }

    public String constructMatchingPrompt(MatchingRequestDTO req, MatchingResponseDTO classicalResult) {
        Map<String, Object> matchingInput = new LinkedHashMap<>();

        // 1. Groom (Boy) Profile
        matchingInput.put("groomBoy", buildMatchingProfileJson(req.boy(), classicalResult.getBoyProfile()));

        // 2. Bride (Girl) Profile
        matchingInput.put("brideGirl", buildMatchingProfileJson(req.girl(), classicalResult.getGirlProfile()));

        // 3. Classical Scored Results & Warnings
        Map<String, Object> classical = new LinkedHashMap<>();
        classical.put("matchingSystem", req.matchingSystem());
        classical.put("strictness", req.strictness());
        classical.put("totalScore", classicalResult.getTotalScore());
        classical.put("maxScore", classicalResult.getMaxScore());
        classical.put("percentage", classicalResult.getPercentage());
        classical.put("verdict", classicalResult.getVerdict());
        if (classicalResult.getKootas() != null) {
            classical.put("kootaBreakdown", classicalResult.getKootas());
        }
        if (classicalResult.getWarnings() != null) {
            classical.put("warnings", classicalResult.getWarnings());
        }
        matchingInput.put("classicalKootaResults", classical);

        String matchingJson = "{}";
        try {
            matchingJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(matchingInput);
        } catch (Exception e) {
            log.error("Could not serialize matching input data to JSON: {}", e.getMessage());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== STRUCTURED DUAL HOROSCOPE MATCHING INPUT (JSON) ===\n")
          .append(matchingJson).append("\n\n")
          .append("=== GENERATION DIRECTIVES ===\n")
          .append("1. 'overallVerdict': EXCELLENT, VERY_GOOD, GOOD, AVERAGE, or NOT_RECOMMENDED.\n")
          .append("2. 'compatibilityPercentage': Numeric value between 0.0 and 100.0 synthesizing the total compatibility.\n")
          .append("3. 'executiveSummary': Comprehensive 2-3 paragraph synthesis explaining the fundamental karmic harmony, life path synergy, and long-term potential of this marriage.\n")
          .append("4. Five Detailed Domain Analysis objects (title, scoreOrStatus, analysis, astrologicalBasis):\n")
          .append("   - 'emotionalMentalHarmony': Moon-Rasi, Gana, and Vashya emotional dynamic.\n")
          .append("   - 'healthLongevityNadi': Nadi koota, Ayurvedic constitution (Vata/Pitta/Kapha), 8th house longevity and vitality balance.\n")
          .append("   - 'careerFinancialSynergy': Wealth alignment, 2nd/11th/10th houses interaction, mutual prosperity after marriage.\n")
          .append("   - 'progenyFamilyLineage': Saptamsa D7, 5th house, Jupiter, Rajju/Mahendra harmony and lineage continuation.\n")
          .append("   - 'doshaPapasamyaParity': Sevvai/Kuja Dosha, Rahu-Ketu, Shani balance, nullification evaluation, and Dasa Sandhi overlap.\n")
          .append("5. 'keyStrengths': 3-4 distinct pillars that fortify this relationship.\n")
          .append("6. 'growthAreasAndCautions': 2-3 specific behavioral or astrological cautions to be mindful of.\n")
          .append("7. 'authenticVedicRemedies': 2-3 targeted authentic Vedic remedies (temple visits, stotras, charity) to harmonize any minor friction.\n\n")
          .append("Return ONLY valid JSON matching this schema:\n")
          .append("{\n")
          .append("  \"overallVerdict\": \"EXCELLENT\",\n")
          .append("  \"compatibilityPercentage\": 86.5,\n")
          .append("  \"executiveSummary\": \"(Detailed narrative)\",\n")
          .append("  \"emotionalMentalHarmony\": {\n")
          .append("    \"title\": \"...\",\n")
          .append("    \"scoreOrStatus\": \"90%\",\n")
          .append("    \"analysis\": \"...\",\n")
          .append("    \"astrologicalBasis\": \"...\"\n")
          .append("  },\n")
          .append("  \"healthLongevityNadi\": {\n")
          .append("    \"title\": \"...\",\n")
          .append("    \"scoreOrStatus\": \"85%\",\n")
          .append("    \"analysis\": \"...\",\n")
          .append("    \"astrologicalBasis\": \"...\"\n")
          .append("  },\n")
          .append("  \"careerFinancialSynergy\": {\n")
          .append("    \"title\": \"...\",\n")
          .append("    \"scoreOrStatus\": \"82%\",\n")
          .append("    \"analysis\": \"...\",\n")
          .append("    \"astrologicalBasis\": \"...\"\n")
          .append("  },\n")
          .append("  \"progenyFamilyLineage\": {\n")
          .append("    \"title\": \"...\",\n")
          .append("    \"scoreOrStatus\": \"88%\",\n")
          .append("    \"analysis\": \"...\",\n")
          .append("    \"astrologicalBasis\": \"...\"\n")
          .append("  },\n")
          .append("  \"doshaPapasamyaParity\": {\n")
          .append("    \"title\": \"...\",\n")
          .append("    \"scoreOrStatus\": \"Balanced\",\n")
          .append("    \"analysis\": \"...\",\n")
          .append("    \"astrologicalBasis\": \"...\"\n")
          .append("  },\n")
          .append("  \"keyStrengths\": [\"...\", \"...\"],\n")
          .append("  \"growthAreasAndCautions\": [\"...\", \"...\"],\n")
          .append("  \"authenticVedicRemedies\": [\"...\", \"...\"]\n")
          .append("}\n");

        return sb.toString();
    }

    private Map<String, Object> buildMatchingProfileJson(BirthDetailsDTO b, ChartUiResponseDTO profile) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (b != null) {
            data.put("name", b.name());
            data.put("dob", String.format("%04d-%02d-%02d", b.year(), b.month(), b.day()));
            data.put("tob", String.format("%02d:%02d", b.hour(), b.minute()));
        }

        if (profile != null && profile.getBirthProfile() != null) {
            var bp = profile.getBirthProfile();
            data.put("lagna", bp.getLagna());
            data.put("rasi", bp.getRashi());
            data.put("nakshatra", bp.getNakshatra());
            data.put("nakshatraPada", bp.getNakshatraPada());
        }

        if (profile != null && profile.getD1Chart() != null && !profile.getD1Chart().isEmpty()) {
            int lagnaSign = 1;
            for (ChartResponseDTO.PositionDetail p : profile.getD1Chart()) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) {
                    lagnaSign = p.getSignNumber();
                    break;
                }
            }
            Map<String, String> d9Map = new HashMap<>();
            if (profile.getD9Chart() != null) {
                for (ChartResponseDTO.PositionDetail p : profile.getD9Chart()) {
                    d9Map.put(p.getPlanetKey().toUpperCase(), p.getRashiName());
                }
            }

            List<Map<String, Object>> d1List = new ArrayList<>();
            for (ChartResponseDTO.PositionDetail p : profile.getD1Chart()) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey()) || "ASCENDANT".equalsIgnoreCase(p.getPlanetKey())) continue;

                String pKey = capitalizePlanet(p.getPlanetKey());
                int sign = p.getSignNumber();
                int house = ((sign - lagnaSign + 12) % 12) + 1;
                String dignity = "NEUTRAL";
                if (PlanetDignityUtils.isExalted(pKey, sign)) dignity = "EXALTED";
                else if (PlanetDignityUtils.isDebilitated(pKey, sign)) dignity = "DEBILITATED";
                else if (PlanetDignityUtils.isOwnSign(pKey, sign)) dignity = "OWN_SIGN";

                String d9Rasi = d9Map.getOrDefault(p.getPlanetKey().toUpperCase(), "");
                List<Integer> ruledHouses = getRuledHouses(pKey, lagnaSign);
                String lordshipTitle = getLordshipTitle(pKey, lagnaSign);

                Map<String, Object> pObj = new LinkedHashMap<>();
                pObj.put("planet", p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey());
                pObj.put("placedInD1Sign", p.getRashiName());
                pObj.put("placedInD1House", house);
                pObj.put("placedInD9Sign", d9Rasi);
                pObj.put("rulesHouses", ruledHouses);
                pObj.put("lordshipTitle", lordshipTitle);
                pObj.put("dignity", dignity);
                d1List.add(pObj);
            }
            data.put("planetaryMatrix", d1List);

            // House lordships
            List<Map<String, Object>> houseLords = new ArrayList<>();
            for (int h = 1; h <= 12; h++) {
                int signNumber = ((lagnaSign - 1 + (h - 1)) % 12) + 1;
                Map<String, Object> hObj = new LinkedHashMap<>();
                hObj.put("houseNumber", h);
                hObj.put("signName", RASHIS[signNumber - 1]);
                hObj.put("houseLord", PlanetDignityUtils.getSignLord(signNumber));
                houseLords.add(hObj);
            }
            data.put("houseLordships", houseLords);
        }

        if (profile != null && profile.getD9Chart() != null && !profile.getD9Chart().isEmpty()) {
            Map<String, String> d9 = new LinkedHashMap<>();
            for (ChartResponseDTO.PositionDetail p : profile.getD9Chart()) {
                d9.put(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(), p.getRashiName());
            }
            data.put("d9Navamsa", d9);
        }

        return data;
    }

    public static List<Integer> getRuledHouses(String planet, int lagnaSign) {
        if (planet == null) return List.of();
        String p = planet.trim().toLowerCase();
        List<Integer> signs = switch (p) {
            case "sun", "surya" -> List.of(5);
            case "moon", "chandra" -> List.of(4);
            case "mars", "kuja", "sevvai", "mangal" -> List.of(1, 8);
            case "mercury", "budha" -> List.of(3, 6);
            case "jupiter", "guru" -> List.of(9, 12);
            case "venus", "shukra" -> List.of(2, 7);
            case "saturn", "shani" -> List.of(10, 11);
            default -> List.of();
        };
        List<Integer> houses = new ArrayList<>();
        for (int s : signs) {
            houses.add(((s - lagnaSign + 12) % 12) + 1);
        }
        Collections.sort(houses);
        return houses;
    }

    public static String getLordshipTitle(String planet, int lagnaSign) {
        List<Integer> houses = getRuledHouses(planet, lagnaSign);
        if (houses.isEmpty()) return "Shadow Node (Rahu/Ketu - no sign ownership)";
        if (houses.size() == 1) {
            int h = houses.get(0);
            return h + "th Lord (" + (h == 1 ? "Lagnesha" : "") + ")";
        }
        int h1 = houses.get(0);
        int h2 = houses.get(1);
        String extra = (h1 == 1 || h2 == 1) ? " / Lagnesha" : "";
        return h1 + "th & " + h2 + "th Lord" + extra;
    }

    private static String capitalizePlanet(String key) {
        if (key == null || key.isBlank()) return "";
        String lower = key.trim().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static final String[] NAKSHATRAS = {
            "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashirsha", "Ardra",
            "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni",
            "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
            "Moola", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha",
            "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"
    };

    private static final String[] RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Kataka", "Simha", "Kanya",
            "Tula", "Vrishchika", "Dhanus", "Makara", "Kumbha", "Meena"
    };

    public static int getNakshatraIndex(String nakName) {
        if (nakName == null || nakName.isBlank()) return 1;
        String clean = nakName.trim().toLowerCase();
        for (int i = 0; i < NAKSHATRAS.length; i++) {
            if (clean.contains(NAKSHATRAS[i].toLowerCase()) || NAKSHATRAS[i].toLowerCase().contains(clean)) {
                return i + 1;
            }
        }
        return 1;
    }

    public static int getRasiIndex(String rasiName) {
        if (rasiName == null || rasiName.isBlank()) return 1;
        String clean = rasiName.trim().toLowerCase();
        for (int i = 0; i < RASHIS.length; i++) {
            if (clean.contains(RASHIS[i].toLowerCase()) || RASHIS[i].toLowerCase().contains(clean)) {
                return i + 1;
            }
        }
        return 1;
    }

    public static String getHouseSignificance(int house) {
        return switch (house) {
            case 1 -> "Lagnesha / Self, Physical Body & Vitality (Trikona & Kendra)";
            case 2 -> "Dhana & Kutumba / Wealth, Speech, Family Assets (Maraka)";
            case 3 -> "Bhratru & Sahaya / Courage, Siblings, Short Travels, Initiative";
            case 4 -> "Sukha & Matru / Mother, Home, Inner Peace, Vehicles (Kendra)";
            case 5 -> "Purva Punya & Putra / Intellect, Children, Past Merit (Trikona)";
            case 6 -> "Roga, Rina, Shatru / Illness, Debts, Litigation, Work (Dusthana)";
            case 7 -> "Kalathra & Vivaha / Marriage, Spouse, Business Partners (Kendra / Maraka)";
            case 8 -> "Ayurdaya & Randhra / Longevity, Sudden Changes, Secrets (Dusthana)";
            case 9 -> "Bhagya & Pitru / Father, Higher Wisdom, Luck, Dharma (Trikona)";
            case 10 -> "Karma & Rajya / Career, Profession, Social Status, Fame (Kendra)";
            case 11 -> "Labha & Aaya / Gains, Elder Siblings, Aspirations, Income (Badhaka for Movable)";
            case 12 -> "Vyaya & Moksha / Expenditures, Losses, Foreign Residence, Sleep (Dusthana)";
            default -> "";
        };
    }

    public static String calculateTarabalam(int birthNak, int transitNak, String lang) {
        int tara = ((transitNak - birthNak + 27) % 9) + 1;
        boolean isTa = "ta".equalsIgnoreCase(lang);
        return switch (tara) {
            case 1 -> isTa ? "ஜன்ம தாரை (1/9 - எச்சரிக்கை / உடல் நலம் கவனம்)" : "Janma Tara (1/9 - Body Energy / Caution)";
            case 2 -> isTa ? "சம்பத்து தாரை (2/9 - தன லாபம் / அதிர்ஷ்டம்)" : "Sampat Tara (2/9 - Wealth & Prosperity)";
            case 3 -> isTa ? "விபத்து தாரை (3/9 - தடைகள் / விழிப்புணர்வு தேவை)" : "Vipat Tara (3/9 - Obstacles / Caution)";
            case 4 -> isTa ? "க்ஷேம தாரை (4/9 - சௌக்கியம் / நலம்)" : "Kshema Tara (4/9 - Wellbeing & Security)";
            case 5 -> isTa ? "பிரத்யக் தாரை (5/9 - தாமதங்கள் / சவால்கள்)" : "Pratyak Tara (5/9 - Resistance & Delays)";
            case 6 -> isTa ? "சாதக தாரை (6/9 - வெற்றி / காரிய சித்தி)" : "Sadhana Tara (6/9 - High Success & Achievement)";
            case 7 -> isTa ? "நைதன தாரை (7/9 - வீண் விரயம் / பெரும் கவனம்)" : "Naidhana Tara (7/9 - Heavy Caution / Restraint)";
            case 8 -> isTa ? "மித்ர தாரை (8/9 - நட்பு / சுப பலன்கள்)" : "Mitra Tara (8/9 - Friendship & Favorable)";
            case 9 -> isTa ? "பரம மித்ர தாரை (9/9 - உன்னத வெற்றி / பரம யோகம்)" : "Parama Mitra Tara (9/9 - Supreme Favor & Victory)";
            default -> isTa ? "சாதாரண தாரை" : "Neutral Tara";
        };
    }

    public static String getGocharaMoonHouseMeaning(int house, String lang) {
        boolean isTa = "ta".equalsIgnoreCase(lang);
        return switch (house) {
            case 1 -> isTa ? "1-ம் இடம் (உடல் சோர்வு / புதிய சிந்தனை)" : "1st House (Mental activity & self-focus)";
            case 2 -> isTa ? "2-ம் இடம் (பண வரவு / பேச்சு கவனம்)" : "2nd House (Financial transactions & speech care)";
            case 3 -> isTa ? "3-ம் இடம் (தைரியம் / முயற்சி வெற்றி)" : "3rd House (Courage & initiative success)";
            case 4 -> isTa ? "4-ம் இடம் (மன அமைதி / தாயார் நலம்)" : "4th House (Domestic matters & emotional peace)";
            case 5 -> isTa ? "5-ம் இடம் (புத்தி கூர்மை / பிள்ளைகள் நலம்)" : "5th House (Intellect & children focus)";
            case 6 -> isTa ? "6-ம் இடம் (எதிரிகள் வீழ்ச்சி / உடல் சுறுசுறுப்பு / உத்தம பலன்)" : "6th House (Victory, debts resolution & vitality)";
            case 7 -> isTa ? "7-ம் இடம் (மகிழ்ச்சியான உறவுகள் / தொழில் கூட்டு)" : "7th House (Partnerships & social harmony)";
            case 8 -> isTa ? "8-ம் இடம் (சந்திராஷ்டமம் / அமைதி காக்கவும்)" : "8th House (Chandrashtama / Mental restraint)";
            case 9 -> isTa ? "9-ம் இடம் (பாக்கிய வளர்ச்சி / ஆன்மீகம்)" : "9th House (Fortune, spirituality & guidance)";
            case 10 -> isTa ? "10-ம் இடம் (தொழில் மேன்மை / காரிய வெற்றி)" : "10th House (Career momentum & status)";
            case 11 -> isTa ? "11-ம் இடம் (லாபங்கள் / ஆசை நிறைவேறுதல் / மிக நன்று)" : "11th House (All-round gains & fulfillment)";
            case 12 -> isTa ? "12-ம் இடம் (சுப விரயம் / பயணங்கள்)" : "12th House (Expenditure & rest needed)";
            default -> "";
        };
    }

    private MatchingAiPredictionDTO parseMatchingGeminiResponse(
            String rawJson,
            String lang,
            MatchingRequestDTO req,
            MatchingResponseDTO classicalResult) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode parts = firstCandidate.path("content").path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String jsonText = parts.get(0).path("text").asText().trim();
                    if (jsonText.startsWith("```json")) {
                        jsonText = jsonText.substring(7);
                    }
                    if (jsonText.startsWith("```")) {
                        jsonText = jsonText.substring(3);
                    }
                    if (jsonText.endsWith("```")) {
                        jsonText = jsonText.substring(0, jsonText.length() - 3);
                    }
                    jsonText = jsonText.trim();

                    JsonNode usage = root.path("usageMetadata");
                    int promptTokens = usage.path("promptTokenCount").asInt(0);
                    int candidatesTokens = usage.path("candidatesTokenCount").asInt(0);
                    int totalTokens = usage.path("totalTokenCount").asInt(promptTokens + candidatesTokens);

                    double costUsd = ((promptTokens / 1_000_000.0) * 0.15) + ((candidatesTokens / 1_000_000.0) * 0.60);
                    double costInr = costUsd * 86.50;

                    PredictionResponseDTO.TokenUsage tokenUsage = PredictionResponseDTO.TokenUsage.builder()
                            .promptTokens(promptTokens)
                            .completionTokens(candidatesTokens)
                            .totalTokens(totalTokens)
                            .estimatedCostUsd(costUsd)
                            .estimatedCostInr(costInr)
                            .modelUsed(geminiProperties.getModel())
                            .build();

                    MatchingAiPredictionDTO parsed = objectMapper.readValue(jsonText, MatchingAiPredictionDTO.class);
                    parsed.setEnabled(true);
                    parsed.setTokenUsage(tokenUsage);
                    parsed.setMessage("AI Marriage Compatibility Report synthesized successfully via Google Gemini.");
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.error("Could not parse Matching Gemini JSON response: {}", e.getMessage(), e);
        }
        return createUnavailableMatchingResponse(lang);
    }

    public MatchingAiPredictionDTO createUnavailableMatchingResponse(String lang) {
        return MatchingAiPredictionDTO.builder()
                .enabled(false)
                .message(getLocalizedUnavailableMessage(lang))
                .build();
    }
}
