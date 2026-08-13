package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;

import java.util.*;

/**
 * Utility for classical Ayur-Jyotish (Vedic Medical Astrology) calculations based on
 * Brihat Parasara Hora Shastra and Charaka Samhita.
 */
public class AyurvedicAstrologyUtils {

    private static final String[] RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Kataka", "Simha", "Kanya",
            "Tula", "Vrishchika", "Dhanus", "Makara", "Kumbha", "Meena"
    };

    public record AyurvedicHealthProfile(
            String dominantPrakriti,
            Map<String, Integer> doshaPercentages,
            String lagnaElement,
            String rogaSthanaSign,
            String rogaLord,
            List<String> calculatedOrganVulnerabilities,
            List<String> dietaryAndLifestyleDirectives
    ) {}

    public static String getPlanetaryPrimaryDosha(String planet) {
        if (planet == null) return "Tridosha";
        String p = planet.trim();
        return switch (p.toLowerCase()) {
            case "sun", "surya" -> "Pitta (Fire)";
            case "moon", "chandra" -> "Kapha & Vata (Water/Air)";
            case "mars", "kuja", "sevvai", "mangal" -> "Pitta (Fire)";
            case "mercury", "budha" -> "Tridosha (Adaptable)";
            case "jupiter", "guru" -> "Kapha (Water/Ether)";
            case "venus", "shukra" -> "Kapha & Vata (Water/Air)";
            case "saturn", "shani" -> "Vata (Air/Dryness)";
            case "rahu" -> "Vata (Amplified Air)";
            case "ketu" -> "Pitta (Internal Fire)";
            default -> "Tridosha";
        };
    }

    public static String getPlanetaryTissueSignification(String planet) {
        if (planet == null) return "";
        String p = planet.trim().toLowerCase();
        return switch (p) {
            case "sun", "surya" -> "Bone density, heart, vital heat, bile";
            case "moon", "chandra" -> "Blood, bodily fluids, lymph, mental balance";
            case "mars", "kuja", "sevvai", "mangal" -> "Blood, muscles, bone marrow, inflammatory response";
            case "mercury", "budha" -> "Skin, nervous system, speech, transport channels";
            case "jupiter", "guru" -> "Fat, liver, brain tissue, expansion/fluid retention";
            case "venus", "shukra" -> "Reproductive fluids, kidneys, glandular system";
            case "saturn", "shani" -> "Nerves, joints, tendons, chronic dryness, decay";
            case "rahu" -> "Nervous tremors, illusions, toxicities, allergies";
            case "ketu" -> "Sharp fevers, surgical conditions, skin breakouts";
            default -> "";
        };
    }

    public static String getRashiTattva(int sign) {
        return switch (sign) {
            case 1, 5, 9 -> "Agni (Fire)";
            case 2, 6, 10 -> "Prithvi (Earth)";
            case 3, 7, 11 -> "Vayu (Air)";
            case 4, 8, 12 -> "Jala (Water)";
            default -> "Agni (Fire)";
        };
    }

    public static AyurvedicHealthProfile calculateHealthProfile(
            int lagnaSign,
            int moonSign,
            List<ChartResponseDTO.PositionDetail> d1Chart) {

        double vataScore = 0.0;
        double pittaScore = 0.0;
        double kaphaScore = 0.0;

        // 1. Lagna Sign Tattva (Weight: 3.0)
        double[] lagnaTattva = getTattvaDoshaVector(lagnaSign);
        vataScore += lagnaTattva[0] * 3.0;
        pittaScore += lagnaTattva[1] * 3.0;
        kaphaScore += lagnaTattva[2] * 3.0;

        // 2. Lagna Lord Planetary Dosha (Weight: 3.0)
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        double[] lagnaLordDosha = getPlanetDoshaVector(lagnaLord);
        vataScore += lagnaLordDosha[0] * 3.0;
        pittaScore += lagnaLordDosha[1] * 3.0;
        kaphaScore += lagnaLordDosha[2] * 3.0;

        // 3. Moon Sign Tattva (Weight: 2.0)
        double[] moonTattva = getTattvaDoshaVector(moonSign);
        vataScore += moonTattva[0] * 2.0;
        pittaScore += moonTattva[1] * 2.0;
        kaphaScore += moonTattva[2] * 2.0;

        // 4. Moon Planetary Dosha (Weight: 1.0)
        double[] moonPlanetDosha = getPlanetDoshaVector("Moon");
        vataScore += moonPlanetDosha[0] * 1.0;
        pittaScore += moonPlanetDosha[1] * 1.0;
        kaphaScore += moonPlanetDosha[2] * 1.0;

        // 5. Sun Sign Tattva (Weight: 1.0)
        int sunSign = 1;
        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                if ("SUN".equalsIgnoreCase(p.getPlanetKey()) || "SURYA".equalsIgnoreCase(p.getPlanetKey())) {
                    sunSign = p.getSignNumber();
                    break;
                }
            }
        }
        double[] sunTattva = getTattvaDoshaVector(sunSign);
        vataScore += sunTattva[0] * 1.0;
        pittaScore += sunTattva[1] * 1.0;
        kaphaScore += sunTattva[2] * 1.0;

        // 6. 6th House (Roga Sthana) Sign Tattva & 6th Lord Dosha (Weight: 2.0)
        int rogaSign = ((lagnaSign - 1 + 5) % 12) + 1;
        String rogaLord = PlanetDignityUtils.getSignLord(rogaSign);
        double[] rogaTattva = getTattvaDoshaVector(rogaSign);
        double[] rogaLordDosha = getPlanetDoshaVector(rogaLord);

        vataScore += (rogaTattva[0] + rogaLordDosha[0]) * 1.0;
        pittaScore += (rogaTattva[1] + rogaLordDosha[1]) * 1.0;
        kaphaScore += (rogaTattva[2] + rogaLordDosha[2]) * 1.0;

        // Calculate Percentages
        double total = vataScore + pittaScore + kaphaScore;
        if (total <= 0.0) total = 1.0;
        int vataPct = (int) Math.round((vataScore / total) * 100.0);
        int pittaPct = (int) Math.round((pittaScore / total) * 100.0);
        int kaphaPct = 100 - (vataPct + pittaPct); // Ensure sum is exactly 100

        Map<String, Integer> doshaPctMap = new LinkedHashMap<>();
        doshaPctMap.put("Pitta", pittaPct);
        doshaPctMap.put("Vata", vataPct);
        doshaPctMap.put("Kapha", kaphaPct);

        // Determine Dominant Prakriti String
        String dominantPrakriti = determineDominantPrakriti(vataPct, pittaPct, kaphaPct);

        // Organ Vulnerabilities based on 6th, 8th, 12th and planetary afflictions
        List<String> organVulnerabilities = calculateOrganVulnerabilities(lagnaSign, rogaSign, rogaLord, d1Chart);

        // Dietary and Lifestyle Directives
        List<String> lifestyleDirectives = calculateDietaryDirectives(dominantPrakriti);

        String lagnaElemStr = getRashiTattva(lagnaSign) + " / " + RASHIS[lagnaSign - 1];
        String rogaSignStr = RASHIS[rogaSign - 1] + " (House 6)";

        return new AyurvedicHealthProfile(
                dominantPrakriti,
                doshaPctMap,
                lagnaElemStr,
                rogaSignStr,
                rogaLord,
                organVulnerabilities,
                lifestyleDirectives
        );
    }

    private static double[] getTattvaDoshaVector(int sign) {
        // Returns [Vata, Pitta, Kapha] normalized vector
        return switch (sign) {
            case 1, 5, 9 -> new double[]{0.0, 1.0, 0.0};       // Fire -> 100% Pitta
            case 2, 6, 10 -> new double[]{0.5, 0.0, 0.5};      // Earth -> 50% Vata, 50% Kapha
            case 3, 7, 11 -> new double[]{1.0, 0.0, 0.0};      // Air -> 100% Vata
            case 4, 8, 12 -> new double[]{0.0, 0.0, 1.0};      // Water -> 100% Kapha
            default -> new double[]{0.33, 0.33, 0.34};
        };
    }

    private static double[] getPlanetDoshaVector(String planet) {
        if (planet == null) return new double[]{0.33, 0.33, 0.34};
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya", "mars", "kuja", "sevvai", "mangal", "ketu" -> new double[]{0.0, 1.0, 0.0}; // Pitta
            case "moon", "chandra", "venus", "shukra" -> new double[]{0.5, 0.0, 0.5};                      // Kapha-Vata
            case "saturn", "shani", "rahu" -> new double[]{1.0, 0.0, 0.0};                                   // Vata
            case "jupiter", "guru" -> new double[]{0.0, 0.0, 1.0};                                           // Kapha
            case "mercury", "budha" -> new double[]{0.33, 0.33, 0.34};                                       // Tridosha
            default -> new double[]{0.33, 0.33, 0.34};
        };
    }

    private static String determineDominantPrakriti(int vata, int pitta, int kapha) {
        int max = Math.max(vata, Math.max(pitta, kapha));
        if (pitta >= 40 && vata >= 30) return "Pitta-Vata";
        if (vata >= 40 && pitta >= 30) return "Vata-Pitta";
        if (pitta >= 40 && kapha >= 30) return "Pitta-Kapha";
        if (kapha >= 40 && pitta >= 30) return "Kapha-Pitta";
        if (vata >= 40 && kapha >= 30) return "Vata-Kapha";
        if (kapha >= 40 && vata >= 30) return "Kapha-Vata";
        if (max == pitta) return "Pitta Dominant";
        if (max == vata) return "Vata Dominant";
        return "Kapha Dominant";
    }

    private static List<String> calculateOrganVulnerabilities(
            int lagnaSign,
            int rogaSign,
            String rogaLord,
            List<ChartResponseDTO.PositionDetail> d1Chart) {

        List<String> vulnerabilities = new ArrayList<>();

        // Roga Sign based vulnerability
        String rogaSignVuln = switch (rogaSign) {
            case 1 -> "Head region, cerebral circulation & acute inflammatory headaches (Mesha / Aries in 6th)";
            case 2 -> "Throat, vocal cords, thyroid & facial tissue sensitivity (Vrishabha / Taurus in 6th)";
            case 3 -> "Respiratory bronchi, nervous reflexes & shoulder/arm nerve tension (Mithuna / Gemini in 6th)";
            case 4 -> "Digestive chest/stomach, mucosal lining & emotional psychosomatic stress (Kataka / Cancer in 6th)";
            case 5 -> "Upper abdomen, digestive fire (Jatharagni), cardiovascular vitality (Simha / Leo in 6th)";
            case 6 -> "Lower gastrointestinal tract, assimilation, intestinal microbiome (Kanya / Virgo in 6th)";
            case 7 -> "Renal system, lumbar spine, lower back & fluid filtration balance (Tula / Libra in 6th)";
            case 8 -> "Pelvic region, excretory channels & reproductive tissue vitality (Vrishchika / Scorpio in 6th)";
            case 9 -> "Liver metabolism, arterial circulation & thigh/hip muscular stamina (Dhanus / Sagittarius in 6th)";
            case 10 -> "Knee joints, skeletal bone density & synovial fluid regulation (Makara / Capricorn in 6th)";
            case 11 -> "Calves, nervous circulation & peripheral blood flow (Kumbha / Aquarius in 6th)";
            case 12 -> "Lymphatic drainage, sleep equilibrium & foot reflexology stamina (Meena / Pisces in 6th)";
            default -> "Digestive metabolism and immune vitality";
        };
        vulnerabilities.add(rogaSignVuln);

        // Roga Lord based vulnerability
        String rogaLordVuln = switch (rogaLord.toLowerCase()) {
            case "sun" -> "Cardiac stamina, eyesight clarity and bone mineral absorption (Sun as Roga Lord)";
            case "moon" -> "Body fluid balance, lymphatic regulation and mental equilibrium (Moon as Roga Lord)";
            case "mars" -> "Blood purification, muscular inflammation and bile overheating (Mars as Roga Lord)";
            case "mercury" -> "Skin barrier resilience, enteric nervous system and digestive enzyme balance (Mercury as Roga Lord)";
            case "jupiter" -> "Hepatic/liver lipid metabolism and arterial expansion (Jupiter as Roga Lord)";
            case "venus" -> "Kidney hydration, endocrine balance and reproductive tissue health (Venus as Roga Lord)";
            case "saturn" -> "Joint mobility, chronic dryness, tendon flexibility and nerve wear (Saturn as Roga Lord)";
            default -> "Metabolic assimilation and stamina maintenance";
        };
        vulnerabilities.add(rogaLordVuln);

        // 8th House (Ayurdaya / Chronic) check
        int eighthSign = ((lagnaSign - 1 + 7) % 12) + 1;
        String eighthLord = PlanetDignityUtils.getSignLord(eighthSign);
        vulnerabilities.add("Longevity resilience & chronic vitality maintenance governed by 8th Lord " + eighthLord + " in " + RASHIS[eighthSign - 1]);

        return vulnerabilities;
    }

    private static List<String> calculateDietaryDirectives(String prakriti) {
        List<String> directives = new ArrayList<>();
        if (prakriti.contains("Pitta")) {
            directives.add("Favor cooling, grounding, fresh whole foods with natural sweet, bitter, and astringent tastes");
            directives.add("Limit pungent spices, sour citrus excess, fried oils, and late-night heavy meals during high Pitta seasons");
        } else if (prakriti.contains("Vata")) {
            directives.add("Favor warm, nourishing, easily digestible cooked meals with healthy fats (ghee, sesame oil)");
            directives.add("Maintain consistent meal schedules; avoid dry, cold, raw, and highly carbonated items");
        } else {
            directives.add("Favor light, warm, dry, and mildly spiced preparations to stimulate metabolic Agni");
            directives.add("Minimize heavy dairy, refined sugars, cold beverages, and sedentary post-meal habits");
        }
        directives.add("Incorporate gentle daily Pranayama (Nadi Shodhana / Sheetali) and rhythmic sleep cycles to protect Ojas (vital immunity)");
        return directives;
    }
}
