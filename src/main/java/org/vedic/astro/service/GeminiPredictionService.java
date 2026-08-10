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
        sb.append("- Output dense, punchy, actionable astrological readings. FORBID repetitive boilerplate or generic optimistic filler across years.\n")
          .append("- TRUTHFULLY AND ACCURATELY predict potential difficulties (job loss, career disruption, acute/chronic illness, surgeries, parental health decline/bereavement, debts) when Maraka/Dusthana/Badhaka/afflicted lords are active.\n")
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

            sb.append("D12[Dwadasamsa-Parents/Heritage]: ");
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                int d12Sign = vargaEngineService != null
                        ? vargaEngineService.calculateVargaSign(12, p.getSignNumber(), p.getDegreeInSign(), p.getSignNumber() * 30.0 + p.getDegreeInSign())
                        : ((p.getSignNumber() - 1 + (int)(p.getDegreeInSign() / 2.5)) % 12 + 1);
                sb.append(p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey()).append(":S").append(d12Sign).append(" ");
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

        // Dasa Timeline
        if (c.getCurrentDasaTimeline() != null && !c.getCurrentDasaTimeline().isEmpty()) {
            sb.append("Vimshottari Dasa Timeline:\n");
            for (DasaPeriod d : c.getCurrentDasaTimeline()) {
                sb.append(String.format("- %s Dasa: %s to %s\n", d.getPlanetName(), d.getStartDate(), d.getEndDate()));
            }
            sb.append("\n");
        }

        sb.append("=== GENERATION DIRECTIVES ===\n")
          .append("1. 'nativePersonality': Deep core psychological temperament, 3-4 key strengths, and 2-3 vulnerabilities/karmic patterns.\n")
          .append("2. 'healthAnalysis': Ayurvedic constitution (Vata/Pitta/Kapha balance), 2-4 organ vulnerabilities deduced from 6th/8th/12th houses & D30, vitality summary, and diet/lifestyle advice.\n")
          .append("3. 'aiYogas': Analyze the complete planetary positions (D1, D9, D10, kendras/trikonas/parivarthanas) to independently identify ALL classical Vedic Yogas present in this horoscope (e.g. Gajakesari, Raja Yoga, Dhana Yoga, Vipareeta Raja Yoga, Budhaditya, Neechabhanga, Pancha Mahapurusha Yogas, Parivarthana) with name, forming planets, and lifelong impact.\n")
          .append("4. 'aiDoshams': Independently evaluate all major doshams (Sevvai/Kuja Dosha, Kala Sarpa Dosha, Pitru Dosha, Papakarthari, Rahu-Ketu afflictions), determining whether they are active or nullified, the exact astrological nullification factors, and authentic Vedic remedies.\n")
          .append("5. 'pastKeyPhases': 2-3 pivotal life-defining turning points/phases from birth to present age ").append(currentAge).append(" (periodOrAge, dasaBhukthi, phaseTitle, livedExperience, astrologicalBasis).\n")
          .append("6. 'lifetimePredictions': Exhaustive, year-by-year forecasts covering the native's FULL REMAINING LIFESPAN starting from current year ").append(currentYear).append(" (Age ").append(currentAge).append(") continuously through age 85-90+ (or up to age 100) across all remaining Vimshottari Mahadasas and Antardasas.\n")
          .append("   - For EACH year, you MUST provide 'yearlyTheme', 'detailedPrediction', 'astrologicalBasis' (explicit planetary combinations from D1/D9/D10/D12/D30 & running Dasa-Bhukthi), and 'cautionsAndRemedies'.\n")
          .append("   - 'detailedPrediction' MUST be a rich, deeply articulated narrative (150-250 words per year) systematically covering ALL 4 core life pillars with unbroken continuity:\n")
          .append("     (a) Career, Business & Wealth: Promotions, job transitions, business ventures, salary trajectory, property/vehicle purchases, debts or wealth accumulation.\n")
          .append("     (b) Health & Vitality Realities: Specific physical energies, vulnerable organs deduced from 6th/8th/12th & D30, surgical or hospitalization alerts, fatigue/recovery phases.\n")
          .append("     (c) Family, Marriage & Progeny: Marital dynamics, spouse's milestones, relationship harmony, children's birth/education/achievements.\n")
          .append("     (d) Parents, Elders & Inner Mindset: Wellbeing of father/mother (D12), elder transitions, bereavement if indicated in Maraka/Dusthana dasas, and spiritual/psychological mindset.\n\n")
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
          .append("Analyze the native's natal Moon/Lagna matrix and today's planetary transit to generate a precise, actionable Daily Balan (இன்றைய ராசி பலன்) in language: '").append(lang).append("'.\n")
          .append("Write 100% of all JSON text fields in the native script of '").append(lang).append("'. Output concise, practical, empowering guidance.\n")
          .append("Return ONLY valid JSON matching the schema.\n");
        return sb.toString();
    }

    public String constructDailyAstrologicalPrompt(DailyBalanRequestDTO req, DailyPanchangamDTO panchangam, LocalDate targetDate) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
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

        StringBuilder sb = new StringBuilder();
        sb.append("=== DAILY GOCHARA & TRANSIT MATRIX ===\n")
          .append("Date: ").append(targetDate).append(" (Weekday: ").append(targetDate.getDayOfWeek()).append(")\n")
          .append("Native: ").append(b.name()).append(" | Janma Rasi: ").append(rasi).append(" | Janma Nakshatra: ").append(nakshatra).append("\n")
          .append("Running Dasa-Bhukthi: ").append(runningDasa).append("\n")
          .append("Today Transit Moon Sign: ").append(todayMoonRasi).append(" | Transit Nakshatra: ").append(todayNakshatra).append("\n")
          .append("Today Tithi: ").append(todayTithi).append(" | Yoga: ").append(todayYoga).append("\n")
          .append("Chandrashtama Active: ").append(chandrashtama).append("\n")
          .append("Fixed Astrological Anchors: Vara Lord=").append(anchors.varaLord)
          .append(", Lucky Color=").append(anchors.luckyColor)
          .append(", Lucky Number=").append(anchors.luckyNumber)
          .append(", Favorable Direction=").append(anchors.favorableDirection)
          .append(", Best Time Window=").append(anchors.auspiciousTimeWindow).append("\n\n")
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
          .append("Your task is to analyze the complete dual-horoscope planetary matrix (D1, D9 Navamsa, Shadbala, Kuja Dosha, Papasamya, Dasa Sandhi, and classical Koota results) for the Boy and Girl to provide an authoritative, deep, compassionate, and authentic Vedic Marriage Compatibility Analysis in language '").append(lang).append("'.\n\n")
          .append("CRITICAL REQUIREMENTS:\n")
          .append("- Write 100% of all JSON text values in the native script of '").append(lang).append("'.\n")
          .append("- Rigorously apply all authentic classical nullifications (e.g. Kuja Dosha cancellation if Mars is in own/exalted sign, in 2nd/4th/7th/8th/12th in friendly signs, or if both charts possess balanced Kuja Dosha; Rajju exceptions when nakshatras have different padas or lords are friends; Gana Dosha cancellation if Rasi lords are identical or friendly).\n")
          .append("- Provide profound psychological, financial, health, progeny, and spiritual insight rather than generic cliches.\n")
          .append("- Return ONLY valid JSON matching the exact schema specified in the prompt.\n");
        return sb.toString();
    }

    public String constructMatchingPrompt(MatchingRequestDTO req, MatchingResponseDTO classicalResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== VEDIC DUAL HOROSCOPE MARRIAGE MATCHING REQUEST ===\n\n");

        sb.append("--- GROOM (BOY) DETAILS ---\n")
          .append("Name: ").append(req.boy().name()).append("\n")
          .append("Date of Birth: ").append(req.boy().day()).append("/").append(req.boy().month()).append("/").append(req.boy().year()).append("\n")
          .append("Time of Birth: ").append(req.boy().hour()).append(":").append(req.boy().minute()).append("\n");
        if (classicalResult.getBoyProfile() != null && classicalResult.getBoyProfile().getBirthProfile() != null) {
            var p = classicalResult.getBoyProfile().getBirthProfile();
            sb.append("Lagna: ").append(p.getLagna()).append(" | Rasi: ").append(p.getRashi()).append(" | Nakshatra: ").append(p.getNakshatra()).append(" (Pada: ").append(p.getNakshatraPada()).append(")\n");
        }
        sb.append("\n");

        sb.append("--- BRIDE (GIRL) DETAILS ---\n")
          .append("Name: ").append(req.girl().name()).append("\n")
          .append("Date of Birth: ").append(req.girl().day()).append("/").append(req.girl().month()).append("/").append(req.girl().year()).append("\n")
          .append("Time of Birth: ").append(req.girl().hour()).append(":").append(req.girl().minute()).append("\n");
        if (classicalResult.getGirlProfile() != null && classicalResult.getGirlProfile().getBirthProfile() != null) {
            var p = classicalResult.getGirlProfile().getBirthProfile();
            sb.append("Lagna: ").append(p.getLagna()).append(" | Rasi: ").append(p.getRashi()).append(" | Nakshatra: ").append(p.getNakshatra()).append(" (Pada: ").append(p.getNakshatraPada()).append(")\n");
        }
        sb.append("\n");

        sb.append("--- CLASSICAL MATCHING RESULTS ---\n")
          .append("Matching System: ").append(req.matchingSystem()).append("\n")
          .append("Strictness: ").append(req.strictness()).append("\n")
          .append("Classical Scored Points: ").append(classicalResult.getTotalScore()).append(" / ").append(classicalResult.getMaxScore())
          .append(" (").append(String.format("%.1f", classicalResult.getPercentage())).append("%)\n")
          .append("Classical Verdict: ").append(classicalResult.getVerdict()).append("\n");

        if (classicalResult.getKootas() != null && !classicalResult.getKootas().isEmpty()) {
            sb.append("Koota/Porutham Breakdown:\n");
            for (KootaResultDTO k : classicalResult.getKootas()) {
                sb.append(String.format("- %s: %s (Scored: %.1f/%.1f) - %s\n", k.getName(), k.getStatus(), k.getScoredPoints(), k.getMaxPoints(), k.getDescription()));
            }
            sb.append("\n");
        }

        if (classicalResult.getWarnings() != null && !classicalResult.getWarnings().isEmpty()) {
            sb.append("Algorithmic Warnings:\n");
            for (String w : classicalResult.getWarnings()) {
                sb.append("- ").append(w).append("\n");
            }
            sb.append("\n");
        }

        sb.append("=== GENERATION DIRECTIVES ===\n")
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
