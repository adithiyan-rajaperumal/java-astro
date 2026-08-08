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
            return generateOfflineRuleBasedBalan(req);
        }

        try {
            String prompt = constructAstrologicalPrompt(req);
            String rawJson = callGeminiApi(prompt);
            return parseGeminiResponse(rawJson, req);
        } catch (Exception e) {
            log.error("Failed to generate AI predictions via Gemini: {}", e.getMessage(), e);
            // Graceful fallback to deterministic astrological balan
            PredictionResponseDTO fallback = generateOfflineRuleBasedBalan(req);
            fallback.setMessage("AI Service temporarily unavailable. Generated using deterministic Vedic rules.");
            return fallback;
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
        sb.append("You are an expert, classical Vedic Astrologer (Jyotish Guru). ")
          .append("Analyze the provided Vedic Horoscope and generate a highly accurate, structured Life Balan in the language code: '").append(lang).append("'.\n\n")
          .append("NATIVE DETAILS:\n")
          .append("Name: ").append(b.name()).append("\n")
          .append("Birth Date: ").append(b.day()).append("/").append(b.month()).append("/").append(b.year())
          .append(" Time: ").append(b.hour()).append(":").append(b.minute()).append("\n")
          .append("Current Age: ").append(currentAge).append(" (Current Year: ").append(currentYear).append(")\n")
          .append("Lagna (Ascendant): ").append(c.getBirthProfile() != null ? c.getBirthProfile().getLagna() : "").append("\n")
          .append("Rashi (Moon Sign): ").append(c.getBirthProfile() != null ? c.getBirthProfile().getRashi() : "").append("\n")
          .append("Nakshatra: ").append(c.getBirthProfile() != null ? c.getBirthProfile().getNakshatra() : "").append("\n")
          .append("Panchangam: ").append(c.getPanchangamSystem()).append(", Thithi: ").append(c.getThithi()).append(", Yogam: ").append(c.getYogam()).append(", Karanam: ").append(c.getKaranam()).append("\n\n");

        if (c.getStructuralDiagnostics() != null) {
            sb.append("ACTIVE YOGAS & DOSHAMS:\n");
            if (c.getStructuralDiagnostics().getActiveYogas() != null) {
                c.getStructuralDiagnostics().getActiveYogas().forEach(y -> sb.append("- Yoga: ").append(y.getName()).append(" (").append(y.getDescription()).append(")\n"));
            }
            if (c.getStructuralDiagnostics().getDiscoveredDoshams() != null) {
                c.getStructuralDiagnostics().getDiscoveredDoshams().forEach(d -> sb.append("- Dosham: ").append(d.getName()).append(" Detected=").append(d.isDetected()).append(" Nullified=").append(d.isNullified()).append(" Reason=").append(d.getNullificationReason()).append("\n"));
            }
            sb.append("\n");
        }

        if (c.getCurrentDasaTimeline() != null) {
            sb.append("VIMSHOTTARI DASA TIMELINE:\n");
            for (DasaPeriod d : c.getCurrentDasaTimeline()) {
                sb.append("Dasa: ").append(d.getPlanetName()).append(" from ").append(d.getStartDate()).append(" to ").append(d.getEndDate()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("OUTPUT REQUIREMENTS:\n")
          .append("Return ONLY valid JSON matching this schema:\n")
          .append("{\n")
          .append("  \"overallSummary\": \"(Detailed summary of the native's life path, lagna lord strength, major planetary blessings and advice in target language)\",\n")
          .append("  \"pastMilestones\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(birthYear + 5).append(",\n")
          .append("      \"age\": 5,\n")
          .append("      \"dasaBhukthi\": \"(e.g. Ketu - Venus)\",\n")
          .append("      \"milestoneTitle\": \"(Title of past event/milestone)\",\n")
          .append("      \"description\": \"(Accurate astrological indication for user verification e.g. schooling start, health recovery, relocation)\",\n")
          .append("      \"astrologicalFactor\": \"(Planetary influence explanation)\"\n")
          .append("    }\n")
          .append("  ],\n")
          .append("  \"futurePredictions\": [\n")
          .append("    {\n")
          .append("      \"year\": ").append(currentYear).append(",\n")
          .append("      \"age\": ").append(currentAge).append(",\n")
          .append("      \"dasaBhukthi\": \"(Running Dasa - Bhukthi)\",\n")
          .append("      \"careerFinance\": \"(Career and wealth prospects)\",\n")
          .append("      \"healthVitality\": \"(Health, vitality, wellbeing)\",\n")
          .append("      \"familyMarriage\": \"(Relationships, marriage, domestic peace)\",\n")
          .append("      \"remediesGuidance\": \"(Auspicious timings and practical Vedic remedies)\"\n")
          .append("    }\n")
          .append("  ]\n")
          .append("}\n")
          .append("Ensure pastMilestones has at least 5-8 key milestone years up to age ").append(currentAge).append(".\n")
          .append("Ensure futurePredictions covers consecutive years from year ").append(currentYear).append(" onwards covering key active phases.\n")
          .append("Translate all text into ").append(lang.equals("ta") ? "pure Tamil (தமிழ்)" : lang).append(".\n");

        return sb.toString();
    }

    private String callGeminiApi(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" 
                + geminiProperties.getModel() + ":generateContent?key=" + geminiProperties.getApiKey();

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentObj = Map.of("parts", List.of(textPart));
        Map<String, Object> generationConfig = Map.of(
                "temperature", 0.3,
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
                    parsed.setMessage("AI Balan successfully generated via Google Gemini.");
                    return parsed;
                }
            }
        } catch (Exception e) {
            log.warn("Could not parse Gemini JSON response, falling back to rule-based generation: {}", e.getMessage());
        }
        return generateOfflineRuleBasedBalan(req);
    }

    public PredictionResponseDTO generateOfflineRuleBasedBalan(PredictionRequestDTO req) {
        BirthDetailsDTO b = req.getBirthDetails();
        ChartUiResponseDTO c = req.getChartData();
        String lang = req.getLanguage() != null ? req.getLanguage() : "ta";
        boolean isTa = "ta".equalsIgnoreCase(lang);

        int birthYear = b != null ? b.year() : 1995;
        int currentYear = LocalDate.now().getYear();
        int currentAge = Math.max(0, currentYear - birthYear);

        List<PredictionResponseDTO.PastMilestone> pastMilestones = new ArrayList<>();
        List<PredictionResponseDTO.YearlyPrediction> futurePredictions = new ArrayList<>();

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
