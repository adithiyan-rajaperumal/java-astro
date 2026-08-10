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
import org.vedic.astro.model.DasaPeriod;

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
            log.info("Gemini life predictions disabled or API key absent. Generating rule-based synthesis.");
            PredictionResponseDTO offline = generateOfflineRuleBasedBalan(req);
            cacheService.putLifetimePrediction(cacheKey, offline);
            return offline;
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
            PredictionResponseDTO fallback = generateOfflineRuleBasedBalan(req);
            fallback.setMessage("Generated via Vedic Rule Synthesizer (AI Service temporarily offline).");
            return fallback;
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
                    req.getBirthDetails().latitude(),
                    req.getBirthDetails().longitude(),
                    req.getBirthDetails().location() != null ? req.getBirthDetails().location().toString() : "Chennai",
                    req.getBirthDetails().ayanamsa() != null ? req.getBirthDetails().ayanamsa() : "LAHIRI",
                    lang
            );
            panchangam = dailyPanchangamService.calculateDailyPanchangam(pReq);
        } catch (Exception e) {
            log.warn("Could not calculate daily panchangam for daily balan: {}", e.getMessage());
        }

        if (!geminiProperties.isDailyBalanEnabled()) {
            DailyBalanDTO offline = generateOfflineRuleBasedDailyBalan(req, panchangam, targetDate);
            cacheService.putDailyBalan(cacheKey, offline, targetDate);
            return offline;
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
            DailyBalanDTO fallback = generateOfflineRuleBasedDailyBalan(req, panchangam, targetDate);
            fallback.setMessage("Generated via Vedic Gochara Synthesizer (AI Service temporarily offline).");
            return fallback;
        }
    }

    public String constructSystemInstruction(String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an elite, classical Vedic Astrologer (Jyotish Guru) versed in Brihat Parasara Hora Shastra, Jataka Parijata, Saravali, and Phaladeepika.\n")
          .append("Your task is to analyze the provided mathematically exact 12-Varga planetary matrix and generate a deep, 100% personalized, authentic Vedic Life Balan in the user's selected language: '").append(lang).append("'.\n\n")
          .append("CRITICAL LANGUAGE & SCRIPT DIRECTIVES:\n")
          .append("- You MUST write 100% of all JSON text fields in the native script of language code '").append(lang).append("':\n");
        if ("ta".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Tamil (தமிழ்). Use classical terminology: லக்னாதிபதி, பூர்வ புண்ணியம், யோககாரகன், விம்சோத்தரி திசா புக்தி, கஜகேசரி யோகம், ரோக ஸ்தானம், பரிகாரங்கள்.\n");
        } else if ("hi".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Hindi (हिन्दी). Use classical Vedic terms: लग्नेश, राजयोग, पूर्व पुण्य, दशा-अन्तर्दशा, षष्ठ भाव रोग, वैदिक उपाय.\n");
        } else if ("te".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Telugu (తెలుగు). Use authentic terms: లగ్నాధిపతి, రాజయోగాలు, పూర్వ పుణ్యం, దశ అంతర్దశ, రోగ స్థానం, పరిహారాలు.\n");
        } else if ("kn".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Kannada (ಕನ್ನಡ). Use authentic terms: ಲಗ್ನಾಧಿಪತಿ, ರಾಜಯೋಗಗಳು, ಪೂರ್ವ ಪುಣ್ಯ, ದಶಾ ಭುಕ್ತಿ, ಪರಿಹಾರಗಳು.\n");
        } else if ("ml".equalsIgnoreCase(lang)) {
            sb.append("  * Language: Malayalam (മലയാളം). Use authentic terms: ലഗ്നാധിപൻ, രാജയോഗങ്ങൾ, പൂർവ്വ പുണ്യം, ദശാ ഫലങ്ങൾ, പരിഹാരങ്ങൾ.\n");
        } else {
            sb.append("  * Language: English with classical Sanskrit astrological terms in parentheses.\n");
        }
        sb.append("- Output dense, punchy, actionable astrological readings. Avoid generic filler statements.\n")
          .append("- Return ONLY valid JSON matching the exact schema specified in the prompt.\n");
        return sb.toString();
    }

    public String constructAstrologicalPrompt(PredictionRequestDTO req) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        int birthYear = b.year();
        int currentYear = LocalDate.now().getYear();
        int currentAge = Math.max(0, currentYear - birthYear);

        StringBuilder sb = new StringBuilder();
        sb.append("=== COMPRESSED VEDIC ASTROLOGICAL MATRIX ===\n")
          .append("Native: ").append(b.name()).append(" | DOB: ").append(b.day()).append("/").append(b.month()).append("/").append(b.year())
          .append(" ").append(b.hour()).append(":").append(b.minute())
          .append(" | Age: ").append(currentAge).append(" (Current: ").append(currentYear).append(")\n")
          .append("Lagna: ").append(c.getBirthProfile() != null ? c.getBirthProfile().getLagna() : "").append("\n")
          .append("Rasi: ").append(c.getBirthProfile() != null ? c.getBirthProfile().getRashi() : "")
          .append(" | Star: ").append(c.getBirthProfile() != null ? c.getBirthProfile().getNakshatra() : "")
          .append(" (Pada ").append(c.getBirthProfile() != null ? c.getBirthProfile().getNakshatraPada() : 1).append(")\n")
          .append("Panchangam: ").append(c.getPanchangamSystem()).append(" | Tithi: ").append(c.getThithi())
          .append(" | Yoga: ").append(c.getYogam()).append(" | Karana: ").append(c.getKaranam()).append("\n\n");

        // D1 Rasi Positions
        if (c.getD1Chart() != null && !c.getD1Chart().isEmpty()) {
            sb.append("D1[Rasi]: ");
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                sb.append(String.format("%s:%s(H%d@%.1f°) ",
                        p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
                        p.getRashiName(), p.getSignNumber(), p.getDegreeInSign()));
            }
            sb.append("\n");
        }

        // 12-Varga Planetary Dignities (D2 Hora, D3 Drekkana, D7 Saptamsa, D9 Navamsa, D10 Dasamsa, D12 Dwadasamsa, D30 Trimsamsa)
        if (c.getD1Chart() != null && !c.getD1Chart().isEmpty()) {
            sb.append("D2[Hora-Wealth]: ");
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                int d2Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(2, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : (p.getSignNumber() % 2 != 0 ? (p.getDegreeInSign() < 15.0 ? 5 : 4) : (p.getDegreeInSign() < 15.0 ? 4 : 5));
                sb.append(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey()).append(":").append(d2Sign == 5 ? "Leo(Sun)" : "Can(Moon)").append(" ");
            }
            sb.append("\n");

            sb.append("D9[Navamsa-Inner/Dharma]: ");
            if (c.getD9Chart() != null && !c.getD9Chart().isEmpty()) {
                for (ChartResponseDTO.PositionDetail p : c.getD9Chart()) {
                    sb.append(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey()).append(":").append(p.getRashiName()).append(" ");
                }
            }
            sb.append("\n");

            sb.append("D10[Dasamsa-Career]: ");
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                int d10Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(10, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : ((p.getSignNumber() - 1 + (int)(p.getDegreeInSign() / 3.0)) % 12 + 1);
                sb.append(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey()).append(":S").append(d10Sign).append(" ");
            }
            sb.append("\n");

            sb.append("D30[Trimsamsa-Health/Affliction]: ");
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                int d30Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(30, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : 1;
                sb.append(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey()).append(":S").append(d30Sign).append(" ");
            }
            sb.append("\n\n");
        }

        // Shadbala
        if (c.getShadbalaStrengths() != null && c.getShadbalaStrengths().getPlanetStrengths() != null) {
            sb.append("Shadbala: ");
            c.getShadbalaStrengths().getPlanetStrengths().forEach((planet, strength) -> {
                sb.append(String.format("%s:%.2fR(%s) ", planet, strength.getTotalShadbalaRupas(), strength.getStrengthCategory()));
            });
            sb.append("\n\n");
        }

        // Diagnostics Yogas & Doshas
        if (c.getStructuralDiagnostics() != null) {
            sb.append("Diagnostics: ");
            if (c.getStructuralDiagnostics().getActiveYogas() != null) {
                c.getStructuralDiagnostics().getActiveYogas().forEach(y -> sb.append("Yoga[").append(y.getName()).append("] "));
            }
            if (c.getStructuralDiagnostics().getDiscoveredDoshams() != null) {
                c.getStructuralDiagnostics().getDiscoveredDoshams().forEach(d -> sb.append("Dosha[").append(d.getName()).append(":").append(d.isNullified() ? "Nullified" : "Active").append("] "));
            }
            sb.append("\n\n");
        }

        // Dasa Timeline
        if (c.getCurrentDasaTimeline() != null && !c.getCurrentDasaTimeline().isEmpty()) {
            sb.append("Vimshottari Dasa Timeline:\n");
            for (DasaPeriod d : c.getCurrentDasaTimeline()) {
                sb.append(String.format("- %s Dasa: %s to %s\n", d.getPlanetName(), d.getStartDate(), d.getEndDate()));
            }
            sb.append("\n");
        }

        sb.append("=== GENERATION DIRECTIVES ===\n")
          .append("1. 'nativePersonality': Deep core psychological temperament, 3-4 key strengths, and 2-3 vulnerabilities/karmic lessons.\n")
          .append("2. 'healthAnalysis': Ayurvedic constitution (Vata/Pitta/Kapha balance), 2-4 organ vulnerabilities deduced from 6th/8th/12th houses & D30, vitality summary, and diet/lifestyle advice.\n")
          .append("3. 'pastMilestones': 5-8 verified past milestone events up to age ").append(currentAge).append(" (year, age, dasaBhukthi, milestoneTitle, nature ['POSITIVE'|'CHALLENGING'|'NEUTRAL'], description, astrologicalFactor).\n")
          .append("4. 'lifetimePredictions': Continuous year-by-year forecasts starting from current year ").append(currentYear).append(" for at least 15-20 upcoming key years (year, age, dasaBhukthi, personalMindset, careerProfession, wealthFinance [using D2], healthVitality [using D30], marriageFamily, parentsKids, favorableVsCaution, remediesGuidance).\n\n")
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
          .append("  \"pastMilestones\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(birthYear + 5).append(",\n")
          .append("      \"age\": 5,\n")
          .append("      \"dasaBhukthi\": \"(Dasa-Bhukthi)\",\n")
          .append("      \"milestoneTitle\": \"...\",\n")
          .append("      \"nature\": \"POSITIVE\",\n")
          .append("      \"description\": \"...\",\n")
          .append("      \"astrologicalFactor\": \"...\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"lifetimePredictions\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(currentYear).append(",\n")
          .append("      \"age\": ").append(currentAge).append(",\n")
          .append("      \"dasaBhukthi\": \"(Dasa-Bhukthi)\",\n")
          .append("      \"personalMindset\": \"...\",\n")
          .append("      \"careerProfession\": \"...\",\n")
          .append("      \"wealthFinance\": \"...\",\n")
          .append("      \"healthVitality\": \"...\",\n")
          .append("      \"marriageFamily\": \"...\",\n")
          .append("      \"parentsKids\": \"...\",\n")
          .append("      \"favorableVsCaution\": \"...\",\n")
          .append("      \"remediesGuidance\": \"...\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n");

        return sb.toString();
    }

    public String constructDailySystemInstruction(String lang) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert Vedic Astrologer specializing in Gochara (daily planetary transits) and Panchangam synthesis.\n")
          .append("Analyze the native's natal Moon/Lagna matrix and today's planetary transit to generate a precise, actionable Daily Balan (இன்றைய ராசி பலன்) in language: '").append(lang).append("'.\n")
          .append("Write 100% of all JSON text fields in the native script of '").append(lang).append("'. Output concise, practical, empowering guidance.\n")
          .append("Return ONLY valid JSON matching the schema.\n");
        return sb.toString();
    }

    public String constructDailyAstrologicalPrompt(DailyBalanRequestDTO req, DailyPanchangamDTO panchangam, LocalDate targetDate) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String rasi = c.getBirthProfile() != null ? c.getBirthProfile().getRashi() : "Mesha";
        String nakshatra = c.getBirthProfile() != null ? c.getBirthProfile().getNakshatra() : "Ashwini";
        String runningDasa = findDasaForYear(c.getCurrentDasaTimeline(), targetDate.getYear());

        String todayMoonRasi = panchangam != null ? panchangam.rashi() : "Transit Moon";
        String todayNakshatra = panchangam != null && panchangam.nakshatra() != null ? panchangam.nakshatra().name() : "";
        String todayTithi = panchangam != null && panchangam.thithi() != null ? panchangam.thithi().name() : "";
        String todayYoga = panchangam != null && panchangam.yogam() != null ? panchangam.yogam().name() : "";

        boolean chandrashtama = panchangam != null && panchangam.chandrastamamNakshatras() != null
                && panchangam.chandrastamamNakshatras().contains(nakshatra);

        StringBuilder sb = new StringBuilder();
        sb.append("=== DAILY GOCHARA & TRANSIT MATRIX ===\n")
          .append("Date: ").append(targetDate).append("\n")
          .append("Native: ").append(b.name()).append(" | Janma Rasi: ").append(rasi).append(" | Janma Nakshatra: ").append(nakshatra).append("\n")
          .append("Running Dasa-Bhukthi: ").append(runningDasa).append("\n")
          .append("Today Transit Moon Sign: ").append(todayMoonRasi).append(" | Transit Nakshatra: ").append(todayNakshatra).append("\n")
          .append("Today Tithi: ").append(todayTithi).append(" | Yoga: ").append(todayYoga).append("\n")
          .append("Chandrashtama Active: ").append(chandrashtama).append("\n\n")
          .append("Return ONLY valid JSON matching this schema:\n")
          .append("{\n")
          .append("  \"generalOutlook\": \"(1-2 sentence overall energy & mood for the day)\",\n")
          .append("  \"careerWork\": \"(Career and workplace opportunities/cautions)\",\n")
          .append("  \"financeWealth\": \"(Financial transactions, expenses, gains)\",\n")
          .append("  \"healthVitality\": \"(Physical stamina and mental wellbeing)\",\n")
          .append("  \"relationshipFamily\": \"(Family and relationship harmony)\",\n")
          .append("  \"luckyColor\": \"(e.g. Yellow / மஞ்சள்)\",\n")
          .append("  \"luckyNumber\": \"(e.g. 3)\",\n")
          .append("  \"favorableDirection\": \"(e.g. North-East / வடகிழக்கு)\",\n")
          .append("  \"bestTimeWindow\": \"(e.g. 10:30 AM - 12:00 PM)\",\n")
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
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.2,
                "responseMimeType", "application/json"
        );

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
        return generateOfflineRuleBasedBalan(req);
    }

    public DailyBalanDTO parseDailyGeminiResponse(String rawApiResponse, DailyBalanRequestDTO req, DailyPanchangamDTO panchangam, String targetDateStr) {
        try {
            JsonNode root = objectMapper.readTree(rawApiResponse);
            JsonNode usageNode = root.path("usageMetadata");
            PredictionResponseDTO.TokenUsage tokenUsage = null;
            if (!usageNode.isMissingNode()) {
                int promptTokens = usageNode.path("promptTokenCount").asInt(0);
                int completionTokens = usageNode.path("candidatesTokenCount").asInt(0);
                int totalTokens = usageNode.path("totalTokenCount").asInt(promptTokens + completionTokens);

                String model = geminiProperties.getModel() != null ? geminiProperties.getModel() : "gemini-3.6-flash";
                tokenUsage = PredictionResponseDTO.TokenUsage.builder()
                        .promptTokens(promptTokens)
                        .completionTokens(completionTokens)
                        .totalTokens(totalTokens)
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
                    parsed.setRunningDasaBhukthi(findDasaForYear(req.getChartData().getCurrentDasaTimeline(), LocalDate.parse(targetDateStr).getYear()));
                    parsed.setChandrashtama(panchangam != null && panchangam.chandrastamamNakshatras() != null
                            && panchangam.chandrastamamNakshatras().contains(parsed.getNakshatra()));
                    parsed.setTokenUsage(tokenUsage);
                    parsed.setMessage("Daily Balan synthesized successfully via Google Gemini.");
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.error("Could not parse Daily Gemini JSON response: {}", e.getMessage(), e);
        }
        return generateOfflineRuleBasedDailyBalan(req, panchangam, LocalDate.parse(targetDateStr));
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

        List<PredictionResponseDTO.PastMilestone> pastMilestones = new ArrayList<>();
        int[] samplePastAges = {5, 10, 16, 21, 25, 28, 32, 35, 40};
        for (int age : samplePastAges) {
            if (age <= currentAge) {
                int yr = birthYear + age;
                String runningDasa = findDasaForYear(dasas, yr);
                pastMilestones.add(PredictionResponseDTO.PastMilestone.builder()
                        .year(yr)
                        .age(age)
                        .dasaBhukthi(runningDasa)
                        .milestoneTitle(isTa ? getTamilPastMilestoneTitle(age) : getEnglishPastMilestoneTitle(age))
                        .nature(age % 2 == 0 ? "POSITIVE" : "NEUTRAL")
                        .description(isTa ? getTamilPastMilestoneDesc(age, runningDasa) : getEnglishPastMilestoneDesc(age, runningDasa))
                        .astrologicalFactor(isTa ? "கோச்சார & திசா நாதனின் சாதகமான பார்வை." : "Active transit influence and Dasa lord dignity.")
                        .verified(false)
                        .build());
            }
        }

        List<PredictionResponseDTO.YearlyPrediction> predictions = new ArrayList<>();
        int maxForecastYears = Math.min(100 - currentAge, 35);
        for (int i = 0; i <= maxForecastYears; i++) {
            int yr = currentYear + i;
            int age = currentAge + i;
            String runningDasa = findDasaForYear(dasas, yr);
            predictions.add(PredictionResponseDTO.YearlyPrediction.builder()
                    .year(yr)
                    .age(age)
                    .dasaBhukthi(runningDasa)
                    .personalMindset(isTa ? "மன அமைதியும் ஆக்கப்பூர்வமான புதிய சிந்தனைகளும் மேலோங்கும்." : "Mental clarity, renewed optimism, and personal growth.")
                    .careerProfession(isTa ? "தொழில் மற்றும் பணியிடத்தில் புதிய பொறுப்புகளும் அங்கீகாரமும் கிட்டும்." : "Career advancements, supportive colleagues, and steady recognition.")
                    .careerFinance(isTa ? "தொழில் & நிதி உயர்வும் நன்மையும் உண்டாகும்." : "Career & financial progression.")
                    .wealthFinance(isTa ? "D2 ஹோரா பலத்தால் நிலையான சேமிப்பும் பூமி/நகை சேர்க்கையும் உண்டாகும்." : "D2 Hora indicates solid asset accumulation and growing savings.")
                    .healthVitality(isTa ? "உடல் நலம் சீராக இருக்கும். உணவு முறையில் கவனம் தேவை." : "Vitality remains high. Maintain balanced sleep and hydration.")
                    .marriageFamily(isTa ? "குடும்பத்தில் மகிழ்ச்சியும் நற்காரிய சுப நிகழ்வுகளும் கூடிவரும்." : "Domestic peace, supportive family bonds, and harmonious milestones.")
                    .familyMarriage(isTa ? "குடும்ப சுப காரியங்கள் கூடிவரும்." : "Family harmony and auspicious domestic events.")
                    .parentsKids(isTa ? "பெற்றோர் ஆசியும் குழந்தைகளின் கல்வி/வளர்ச்சியில் மகிழ்ச்சியும் அமையும்." : "Parental blessings and proud moments regarding children's progress.")
                    .favorableVsCaution(isTa ? "முதல் 6 மாதங்கள் மிகச் சிறந்த சுப காலம். பெரிய முதலீடுகளில் கவனம் தேவை." : "H1 is highly auspicious. Exercise caution during major legal paperwork.")
                    .remediesGuidance(isTa ? "வியாழக்கிழமை குரு வழிபாடு மற்றும் நெய்தீபம் ஏற்றுவது நற்பலனைத் தரும்." : "Chant Gayatri Mantra daily and offer prayers to Ishta Devata on Thursdays.")
                    .build());
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
                .pastMilestones(pastMilestones)
                .futurePredictions(predictions)
                .lifetimePredictions(predictions)
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
}
