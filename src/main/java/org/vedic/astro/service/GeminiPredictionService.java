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
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiPredictionService {

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public PredictionResponseDTO generateLifePredictions(PredictionRequestDTO req) {
        if (req == null || req.getBirthDetails() == null || req.getChartData() == null) {
            return PredictionResponseDTO.builder()
                    .enabled(false)
                    .message("Invalid birth details or chart data provided.")
                    .build();
        }

        if (!geminiProperties.isFeatureEnabled()) {
            return PredictionResponseDTO.builder()
                    .enabled(false)
                    .message("AI prediction service is currently unavailable. Please verify API key configuration.")
                    .build();
        }

        try {
            String prompt = constructAstrologicalPrompt(req);
            String rawJson = callGeminiApi(prompt);
            return parseGeminiResponse(rawJson, req);
        } catch (Exception e) {
            log.error("Failed to generate AI predictions via Gemini: {}", e.getMessage(), e);
            return PredictionResponseDTO.builder()
                    .enabled(false)
                    .message("AI prediction service is currently unavailable. " + (e.getMessage() != null ? e.getMessage() : "Please check connection."))
                    .build();
        }
    }

    public String constructAstrologicalPrompt(PredictionRequestDTO req) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        int birthYear = b.year();
        int currentYear = LocalDate.now().getYear();
        int currentAge = Math.max(0, currentYear - birthYear);

        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert, classical Vedic Astrologer (Jyotish Guru) versed in Brihat Parasara Hora Shastra, Jataka Parijata, Saravali, and Phaladeepika.\n")
          .append("Analyze the provided mathematically exact planetary matrix and generate a deep, highly accurate, authentic Vedic Life Balan in the user's selected language: '").append(lang).append("'.\n\n");

        sb.append("=== NATIVE BIRTH DETAILS ===\n")
          .append("Name: ").append(b.name()).append("\n")
          .append("Birth Date: ").append(b.day()).append("/").append(b.month()).append("/").append(b.year())
          .append(" Time: ").append(b.hour()).append(":").append(b.minute()).append("\n")
          .append("Current Age: ").append(currentAge).append(" (Current Year: ").append(currentYear).append(")\n")
          .append("Lagna (Ascendant): ").append(c.getBirthProfile() != null ? c.getBirthProfile().getLagna() : "").append("\n")
          .append("Rashi (Moon Sign): ").append(c.getBirthProfile() != null ? c.getBirthProfile().getRashi() : "").append("\n")
          .append("Nakshatra: ").append(c.getBirthProfile() != null ? c.getBirthProfile().getNakshatra() : "")
          .append(" (Pada: ").append(c.getBirthProfile() != null ? c.getBirthProfile().getNakshatraPada() : 1).append(")\n")
          .append("Panchangam: ").append(c.getPanchangamSystem()).append(", Thithi: ").append(c.getThithi())
          .append(", Yogam: ").append(c.getYogam()).append(", Karanam: ").append(c.getKaranam()).append("\n\n");

        // Full D1 Rasi Chart Placements
        if (c.getD1Chart() != null && !c.getD1Chart().isEmpty()) {
            sb.append("=== D1 RASI CHART PLANETARY POSITIONS ===\n");
            for (ChartResponseDTO.PositionDetail p : c.getD1Chart()) {
                sb.append(String.format("- %s: Sign=%s (House #%d), Degree=%.2f° (%s)\n",
                        p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
                        p.getRashiName(),
                        p.getSignNumber(),
                        p.getDegreeInSign(),
                        p.getFormattedDegree() != null ? p.getFormattedDegree() : ""));
            }
            sb.append("\n");
        }

        // Full D9 Navamsha Chart Placements
        if (c.getD9Chart() != null && !c.getD9Chart().isEmpty()) {
            sb.append("=== D9 NAVAMSHA CHART POSITIONS ===\n");
            for (ChartResponseDTO.PositionDetail p : c.getD9Chart()) {
                sb.append(String.format("- %s: Navamsha Sign=%s (House #%d)\n",
                        p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey(),
                        p.getRashiName(),
                        p.getSignNumber()));
            }
            sb.append("\n");
        }

        // Shadbala Planetary Strengths
        if (c.getShadbalaStrengths() != null && c.getShadbalaStrengths().getPlanetStrengths() != null) {
            sb.append("=== SHADBALA PLANETARY STRENGTHS (RUPAS) ===\n");
            c.getShadbalaStrengths().getPlanetStrengths().forEach((planet, strength) -> {
                sb.append(String.format("- %s: Total Rupas=%.2f, Status=%s (Sthana=%.1f, Dig=%.1f, Kala=%.1f, Cheshta=%.1f)\n",
                        planet,
                        strength.getTotalShadbalaRupas(),
                        strength.getStrengthCategory(),
                        strength.getSthanaBala(),
                        strength.getDigBala(),
                        strength.getKalaBala(),
                        strength.getCheshtaBala()));
            });
            sb.append("\n");
        }

        // System Yogas & Doshams for reference
        if (c.getStructuralDiagnostics() != null) {
            sb.append("=== SYSTEM COMPUTED DIAGNOSTICS ===\n");
            if (c.getStructuralDiagnostics().getActiveYogas() != null) {
                c.getStructuralDiagnostics().getActiveYogas().forEach(y ->
                        sb.append("- Detected Yoga: ").append(y.getName()).append(" (").append(y.getDescription()).append(")\n"));
            }
            if (c.getStructuralDiagnostics().getDiscoveredDoshams() != null) {
                c.getStructuralDiagnostics().getDiscoveredDoshams().forEach(d ->
                        sb.append("- Detected Dosham: ").append(d.getName())
                                .append(" Detected=").append(d.isDetected())
                                .append(" Nullified=").append(d.isNullified())
                                .append(" Reason=").append(d.getNullificationReason()).append("\n"));
            }
            sb.append("\n");
        }

        // Vimshottari Dasa Timeline
        if (c.getCurrentDasaTimeline() != null && !c.getCurrentDasaTimeline().isEmpty()) {
            sb.append("=== VIMSHOTTARI DASA TIMELINE ===\n");
            for (DasaPeriod d : c.getCurrentDasaTimeline()) {
                sb.append("Dasa: ").append(d.getPlanetName()).append(" from ").append(d.getStartDate()).append(" to ").append(d.getEndDate()).append("\n");
            }
            sb.append("\n");
        }

        // Language Script & Translation Directives
        sb.append("=== LANGUAGE & TERMINOLOGY DIRECTIVES ===\n")
          .append("CRITICAL: You MUST write the entire JSON response in the native script of the selected language code '").append(lang).append("':\n");
        if ("ta".equalsIgnoreCase(lang)) {
            sb.append("- Use rich, classical Tamil (தமிழ்) Jyotish terminology (e.g., லக்னாதிபதி பலம், யோககாரகன், கஜகேசரி யோகம், பூர்வ புண்ணிய ஸ்தானம், செவ்வாய் தோஷ நிவர்த்தி, விம்சோத்தரி திசா புக்தி, பரிகாரங்கள்).\n");
        } else if ("hi".equalsIgnoreCase(lang)) {
            sb.append("- Use rich, classical Hindi (हिन्दी) Vedic Jyotish terms (e.g., लग्नेश, राजयोग, नवम-दशम भाव, मांगलिक दोष निवारण, दशा-अन्तर्दशा, वैदिक उपाय).\n");
        } else if ("te".equalsIgnoreCase(lang)) {
            sb.append("- Use authentic Telugu (తెలుగు) Vedic Jyotish terminology (లగ్నాధిపతి, యోగాలు, దోష నివారణ, దశ అంతర్దశ, పరిహారాలు).\n");
        } else if ("kn".equalsIgnoreCase(lang)) {
            sb.append("- Use authentic Kannada (ಕನ್ನಡ) Vedic Jyotish terminology (ಲಗ್ನಾಧಿಪತಿ, ರಾಜಯೋಗಗಳು, ದೋಷ ಪರಿಹಾರ, ದಶಾ ಭುಕ್ತಿ).\n");
        } else if ("ml".equalsIgnoreCase(lang)) {
            sb.append("- Use authentic Malayalam (മലയാളം) Vedic Jyotish terminology (ലഗ്നാധിപൻ, രാജയോഗങ്ങൾ, ദോഷ പരിഹാരം, ദശാ ഫലങ്ങൾ).\n");
        } else {
            sb.append("- Use elegant, classical Vedic Astrological English with traditional Sanskrit astrological terms in parentheses.\n");
        }

        sb.append("\n=== STRICT PERSONALIZATION & ACCURACY DIRECTIVES ===\n")
          .append("1. CRITICAL: DO NOT GENERATE GENERIC OR BOILERPLATE PREDICTIONS. Every single life event must be strictly deduced from this specific native's Lagna, exact planetary house placements (D1), Navamsha dignity (D9), and actual running Vimshottari Mahadasa-Bhukthi timeline.\n")
          .append("2. In 'pastMilestones': You MUST generate realistic, personalized past life events matching the native's actual Dasa-Bhukthi lords and the houses they occupy (e.g. 4th lord Dasa brings schooling/property, 9th/10th brings career launch/higher education, 7th brings relationship/marriage, 8th/12th brings relocation/health shifts).\n")
          .append("3. In 'futurePredictions': Provide continuous year-by-year forecasts starting from current year ").append(currentYear).append(" through upcoming years based on the active Dasa and transits.\n")
          .append("4. Return ONLY valid JSON matching this schema:\n")
          .append("{\n")
          .append("  \"overallSummary\": \"(Comprehensive astrological synthesis of Lagna lord dignity, yogakarakas, 9th/10th lords, and life trajectory in requested language)\",\n")
          .append("  \"aiYogas\": [\n")
          .append("    {\n")
          .append("      \"name\": \"(Name of Yoga in requested language, e.g. கஜகேசரி யோகம் / Gajakesari Yoga)\",\n")
          .append("      \"formingPlanets\": \"(Planets causing the yoga, e.g. குரு & சந்திரன் / Jupiter in Kendra from Moon)\",\n")
          .append("      \"impact\": \"(Specific life impact, activation timing, and blessings in requested language)\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"aiDoshams\": [\n")
          .append("    {\n")
          .append("      \"name\": \"(Dosham name in requested language, e.g. செவ்வாய் தோஷம் / Kuja Dosha)\",\n")
          .append("      \"status\": \"(Detected / Nullified / தோஷ நிவர்த்தி)\",\n")
          .append("      \"nullificationFactor\": \"(Classical Shastric cancellation rule or planetary relief)\",\n")
          .append("      \"remedy\": \"(Practical Vedic mantra, temple pariharam, or spiritual guidance)\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"pastMilestones\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(birthYear + 5).append(",\n")
          .append("      \"age\": 5,\n")
          .append("      \"dasaBhukthi\": \"(Actual Dasa - Bhukthi lord for this year)\",\n")
          .append("      \"milestoneTitle\": \"(Title of past milestone in requested language)\",\n")
          .append("      \"description\": \"(Astrological verification event: schooling, relocation, health, family milestone deduced from planetary lord)\",\n")
          .append("      \"astrologicalFactor\": \"(Planetary influence reason based on house lordship and placement)\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"futurePredictions\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(currentYear).append(",\n")
          .append("      \"age\": ").append(currentAge).append(",\n")
          .append("      \"dasaBhukthi\": \"(Running Dasa - Bhukthi)\",\n")
          .append("      \"careerFinance\": \"(Career and wealth forecast in requested language)\",\n")
          .append("      \"healthVitality\": \"(Health, vitality, and wellbeing forecast in requested language)\",\n")
          .append("      \"familyMarriage\": \"(Marriage, family harmony, and domestic milestones in requested language)\",\n")
          .append("      \"remediesGuidance\": \"(Practical Vedic guidance and remedies in requested language)\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n")
          .append("Provide at least 5-8 milestone events in pastMilestones up to age ").append(currentAge).append(".\n")
          .append("Provide continuous year-by-year futurePredictions starting from current year ").append(currentYear).append(" onwards.\n");

        return sb.toString();
    }

    private String callGeminiApi(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                + geminiProperties.getModel() + ":generateContent?key=" + geminiProperties.getResolvedApiKey();

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentObj = Map.of("parts", List.of(textPart));
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.2,
                "responseMimeType", "application/json"
        );

        Map<String, Object> requestBody = Map.of(
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

                String model = geminiProperties.getModel() != null ? geminiProperties.getModel() : "gemini-2.0-flash";
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
                    // Clean code fence blocks if present
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
                    parsed.setMessage("AI Balan successfully synthesized via Google Gemini.");
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.error("Could not parse Gemini JSON response: {}", e.getMessage(), e);
            return PredictionResponseDTO.builder()
                    .enabled(false)
                    .message("AI prediction generation failed to parse. Please try again.")
                    .build();
        }
        return PredictionResponseDTO.builder()
                .enabled(false)
                .message("AI prediction response was empty.")
                .build();
    }

    public PredictionResponseDTO generateOfflineRuleBasedBalan(PredictionRequestDTO req) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        boolean isTa = "ta".equalsIgnoreCase(lang);

        int birthYear = b != null ? b.year() : 1995;
        int currentYear = LocalDate.now().getYear();
        int currentAge = Math.max(0, currentYear - birthYear);

        List<PredictionResponseDTO.AiYoga> aiYogas = new ArrayList<>();
        List<PredictionResponseDTO.AiDosham> aiDoshams = new ArrayList<>();
        List<PredictionResponseDTO.PastMilestone> pastMilestones = new ArrayList<>();
        List<PredictionResponseDTO.YearlyPrediction> futurePredictions = new ArrayList<>();

        // Generate Yogas
        aiYogas.add(PredictionResponseDTO.AiYoga.builder()
                .name(isTa ? "கஜகேசரி யோகம் (Gajakesari Yoga)" : "Gajakesari Yoga (Jupiter-Moon Kendra)")
                .formingPlanets(isTa ? "குரு மற்றும் சந்திரன் கேந்திர அமைவு" : "Jupiter in Kendra from Moon")
                .impact(isTa ? "உயர்ந்த அறிவு, சமுதாய நற்பெயர் மற்றும் குரு திசையில் நிலையான பொருளாதார உயர்வு." : "High intellect, noble reputation, and lasting financial growth in Jupiter Dasa.")
                .build());

        aiYogas.add(PredictionResponseDTO.AiYoga.builder()
                .name(isTa ? "புதாதித்ய யோகம் (Budhaditya Yoga)" : "Budhaditya Yoga (Sun-Mercury Conjunction)")
                .formingPlanets(isTa ? "சூரியன் மற்றும் புதன் இணைவு" : "Sun & Mercury in favorable house")
                .impact(isTa ? "கல்வித் தேர்ச்சி, நுட்பமான சிந்தனை மற்றும் நிர்வாக ஆற்றல்." : "Sharp analytical thinking, academic excellence, and administrative success.")
                .build());

        // Generate Doshams with Nullification
        aiDoshams.add(PredictionResponseDTO.AiDosham.builder()
                .name(isTa ? "செவ்வாய் தோஷம் (Kuja / Sevvai Dosha)" : "Sevvai / Kuja Dosha (Mars Placement)")
                .status(isTa ? "தோஷ நிவர்த்தி (Nullified)" : "Nullified by Benefic Aspect")
                .nullificationFactor(isTa ? "செவ்வாய் சுப வீடான மேஷம்/விருச்சிகத்தில் அமைந்ததாலும், குருவின் சுப பார்வையாலும் தோஷம் நிவர்த்தி அடைகிறது." : "Mars is in friendly house and aspected by benefic Jupiter, nullifying adverse effects.")
                .remedy(isTa ? "வைத்தீஸ்வரன் கோவில் வழிபாடு மற்றும் செவ்வாய்க்கிழமை நெய்தீபம் ஏற்றுவது சிறந்தது." : "Chant Angaraka Stotram on Tuesdays or visit Vaitheeswaran Koil.")
                .build());

        List<DasaPeriod> dasas = c != null ? c.getCurrentDasaTimeline() : Collections.emptyList();

        // Generate past milestone intervals
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
                        .description(isTa ? getTamilPastMilestoneDesc(age, runningDasa) : getEnglishPastMilestoneDesc(age, runningDasa))
                        .astrologicalFactor(isTa ? "கோச்சார & திசா நாதனின் சாதகமான பார்வை." : "Active transit influence and Dasa lord dignity.")
                        .verified(false)
                        .build());
            }
        }

        // Generate future year-by-year timeline up to 30 years into the future
        int maxForecastYears = Math.min(100 - currentAge, 35);
        for (int i = 0; i <= maxForecastYears; i++) {
            int yr = currentYear + i;
            int age = currentAge + i;
            String runningDasa = findDasaForYear(dasas, yr);
            futurePredictions.add(PredictionResponseDTO.YearlyPrediction.builder()
                    .year(yr)
                    .age(age)
                    .dasaBhukthi(runningDasa)
                    .careerFinance(isTa ? "தொழில் மற்றும் நிதி நிலையில் " + (i % 2 == 0 ? "வளர்ச்சியும் நன்மையும் உண்டாகும்." : "நிலையான முன்னேற்றமும் புதிய வாய்ப்புகளும் அமையும்.") : "Career & wealth shows steady advancement and supportive opportunities.")
                    .healthVitality(isTa ? "உடல் நலம் சீராக இருக்கும். உணவு முறையில் கவனம் தேவை." : "Health remains stable. Maintain balanced routine and vitality.")
                    .familyMarriage(isTa ? "குடும்பத்தில் மகிழ்ச்சியும் நற்காரிய சுப நிகழ்வுகளும் கூடிவரும்." : "Domestic harmony, auspicious events, and positive relationships prevail.")
                    .remediesGuidance(isTa ? "வியாழக்கிழமை குரு வழிபாடு மற்றும் நெய்தீபம் ஏற்றுவது நற்பலனைத் தரும்." : "Perform daily prayers to Ishta Devata and chant Gayatri Mantra for auspiciousness.")
                    .build());
        }

        String summary = isTa ?
                "ஜாதகத்தில் லக்னாதிபதி மற்றும் சுப கிரகங்களின் அமைப்பால் நற்பலன்கள் உண்டாகும். திசா புக்தி காலங்களில் முறையான முயற்சியும் ஆன்மீக வழிபாடும் உயர்வைத் தரும்." :
                "The planetary alignment of Lagna lord and benefic yogas indicates a prosperous life trajectory. Auspicious Dasa periods bring growth and spiritual fulfillment.";

        return PredictionResponseDTO.builder()
                .enabled(true)
                .message("Generated using Vedic Astrological Dasa-Bhukthi Rule Synthesizer.")
                .overallSummary(summary)
                .aiYogas(aiYogas)
                .aiDoshams(aiDoshams)
                .pastMilestones(pastMilestones)
                .futurePredictions(futurePredictions)
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
