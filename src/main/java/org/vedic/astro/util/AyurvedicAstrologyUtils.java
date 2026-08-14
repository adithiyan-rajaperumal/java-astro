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
            List<String> dietaryAndLifestyleDirectives,
            String agniType,
            String bodyBuild,
            String primaryDhatu,
            String recommendedRasayana
    ) {
        // Backwards-compatible constructor for 7-arg callers
        public AyurvedicHealthProfile(
                String dominantPrakriti,
                Map<String, Integer> doshaPercentages,
                String lagnaElement,
                String rogaSthanaSign,
                String rogaLord,
                List<String> calculatedOrganVulnerabilities,
                List<String> dietaryAndLifestyleDirectives) {
            this(
                    dominantPrakriti,
                    doshaPercentages,
                    lagnaElement,
                    rogaSthanaSign,
                    rogaLord,
                    calculatedOrganVulnerabilities,
                    dietaryAndLifestyleDirectives,
                    "Samagni (Balanced & Optimal Digestive Fire)",
                    "Madhya Deha (Balanced Proportions)",
                    "Rasa & Rakta Dhatu (Vital Plasma & Fluids)",
                    "Chyawanprash & Triphala"
            );
        }
    }

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

        // 1. Lagna Sign Tattva (Weight: 3.0) - Fundamental Deha Matrix
        double[] lagnaTattva = getTattvaDoshaVector(lagnaSign);
        vataScore += lagnaTattva[0] * 3.0;
        pittaScore += lagnaTattva[1] * 3.0;
        kaphaScore += lagnaTattva[2] * 3.0;

        // 2. Lagna Lord Planetary Dosha (Weight: 3.0) - Physical Constitution Ruler
        String lagnaLord = PlanetDignityUtils.getSignLord(lagnaSign);
        double[] lagnaLordDosha = getPlanetDoshaVector(lagnaLord);
        vataScore += lagnaLordDosha[0] * 3.0;
        pittaScore += lagnaLordDosha[1] * 3.0;
        kaphaScore += lagnaLordDosha[2] * 3.0;

        // 3. Planets posited in Lagna (House 1) - Direct Physical Modification (Weight: 2.0 each)
        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                if (p.getSignNumber() == lagnaSign && !"LAGNA".equalsIgnoreCase(p.getPlanetKey())) {
                    double[] occupantDosha = getPlanetDoshaVector(p.getPlanetKey());
                    vataScore += occupantDosha[0] * 2.0;
                    pittaScore += occupantDosha[1] * 2.0;
                    kaphaScore += occupantDosha[2] * 2.0;
                }
            }
        }

        // 4. Moon Sign Tattva (Weight: 2.0) - Manas & Fluid Constitution
        double[] moonTattva = getTattvaDoshaVector(moonSign);
        vataScore += moonTattva[0] * 2.0;
        pittaScore += moonTattva[1] * 2.0;
        kaphaScore += moonTattva[2] * 2.0;

        // 5. Moon Planetary Dosha (Weight: 1.0)
        double[] moonPlanetDosha = getPlanetDoshaVector("Moon");
        vataScore += moonPlanetDosha[0] * 1.0;
        pittaScore += moonPlanetDosha[1] * 1.0;
        kaphaScore += moonPlanetDosha[2] * 1.0;

        // 6. Sun Sign Tattva (Weight: 1.0) - Vital Prana & Metabolic Heat
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

        // 7. 6th House (Roga Sthana) Sign Tattva & 6th Lord Dosha (Weight: 2.0)
        int rogaSign = ((lagnaSign - 1 + 5) % 12) + 1;
        String rogaLord = PlanetDignityUtils.getSignLord(rogaSign);
        double[] rogaTattva = getTattvaDoshaVector(rogaSign);
        double[] rogaLordDosha = getPlanetDoshaVector(rogaLord);

        vataScore += (rogaTattva[0] + rogaLordDosha[0]) * 1.0;
        pittaScore += (rogaTattva[1] + rogaLordDosha[1]) * 1.0;
        kaphaScore += (rogaTattva[2] + rogaLordDosha[2]) * 1.0;

        // 8. Planets posited in 6th House (Roga Sthana Occupants) - Active Pathological Modifiers (Weight: 1.5 each)
        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                if (p.getSignNumber() == rogaSign && !"LAGNA".equalsIgnoreCase(p.getPlanetKey())) {
                    double[] rogaOccupantDosha = getPlanetDoshaVector(p.getPlanetKey());
                    vataScore += rogaOccupantDosha[0] * 1.5;
                    pittaScore += rogaOccupantDosha[1] * 1.5;
                    kaphaScore += rogaOccupantDosha[2] * 1.5;
                }
            }
        }

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

        // Classical Determinations
        String dominantPrakriti = determineDominantPrakriti(vataPct, pittaPct, kaphaPct);
        String agniType = determineAgniType(dominantPrakriti, vataPct, pittaPct, kaphaPct);
        String bodyBuild = determineBodyBuild(dominantPrakriti, lagnaSign);
        String moonSignLord = PlanetDignityUtils.getSignLord(moonSign);
        String primaryDhatu = determinePrimaryDhatu(lagnaLord, moonSignLord, dominantPrakriti);
        String recommendedRasayana = determineRecommendedRasayana(dominantPrakriti);

        // Organ Vulnerabilities based on 6th, 8th, 12th and active planetary afflictions
        List<String> organVulnerabilities = calculateOrganVulnerabilities(lagnaSign, rogaSign, rogaLord, d1Chart);

        // Dietary and Lifestyle Directives (including Shad Rasa guidelines)
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
                lifestyleDirectives,
                agniType,
                bodyBuild,
                primaryDhatu,
                recommendedRasayana
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
        int min = Math.min(vata, Math.min(pitta, kapha));

        // 1. Tridoshic Equilibrium / Sama Prakriti (Charaka Samhita Sutrasthana 1.57-58)
        if ((max - min) <= 8 && vata >= 28 && pitta >= 28 && kapha >= 28) {
            return "Sama Prakriti (Tridosha Balanced)";
        }

        // 2. Dual / Dwandvaja Prakriti (Top two within 12% and both >= 30%)
        if (pitta >= 38 && vata >= 30 && Math.abs(pitta - vata) <= 12) return "Pitta-Vata";
        if (vata >= 38 && pitta >= 30 && Math.abs(vata - pitta) <= 12) return "Vata-Pitta";
        if (pitta >= 38 && kapha >= 30 && Math.abs(pitta - kapha) <= 12) return "Pitta-Kapha";
        if (kapha >= 38 && pitta >= 30 && Math.abs(kapha - pitta) <= 12) return "Kapha-Pitta";
        if (vata >= 38 && kapha >= 30 && Math.abs(vata - kapha) <= 12) return "Vata-Kapha";
        if (kapha >= 38 && vata >= 30 && Math.abs(kapha - vata) <= 12) return "Kapha-Vata";

        // 3. Ekadoshaja / Mono-dominant
        if (max == pitta) return "Pitta Dominant";
        if (max == vata) return "Vata Dominant";
        return "Kapha Dominant";
    }

    private static String determineAgniType(String dominantPrakriti, int vata, int pitta, int kapha) {
        if (dominantPrakriti.contains("Sama")) {
            return "Samagni (Balanced & Optimal Digestive Fire)";
        }
        if (pitta >= vata && pitta >= kapha && pitta >= 40) {
            return "Tikshnagni (Intense & Hyper-Metabolic Fire)";
        }
        if (vata >= pitta && vata >= kapha && vata >= 38) {
            return "Vishamagni (Irregular & Fluctuating Metabolism)";
        }
        if (kapha >= pitta && kapha >= vata && kapha >= 38) {
            return "Mandagni (Sluggish & Slow Metabolic Agni)";
        }
        if (dominantPrakriti.contains("Pitta")) {
            return "Tikshnagni (Intense & Hyper-Metabolic Fire)";
        }
        if (dominantPrakriti.contains("Vata")) {
            return "Vishamagni (Irregular & Fluctuating Metabolism)";
        }
        return "Mandagni (Sluggish & Slow Metabolic Agni)";
    }

    private static String determineBodyBuild(String dominantPrakriti, int lagnaSign) {
        if (dominantPrakriti.contains("Sama")) {
            return "Sama Deha (Harmonious & Proportionate Athletic Frame)";
        }
        if (dominantPrakriti.equals("Vata Dominant")) {
            return "Krisa Deha (Slender / Lean Frame, Quick Movements & Dry Skin)";
        }
        if (dominantPrakriti.equals("Pitta Dominant")) {
            return "Madhya Deha (Medium Athletic Frame, High Vitality & Warm Complexion)";
        }
        if (dominantPrakriti.equals("Kapha Dominant")) {
            return "Sthula Deha (Solid / Broad Frame, High Endurance & Smooth Complexion)";
        }
        if (dominantPrakriti.contains("Vata-Pitta") || dominantPrakriti.contains("Pitta-Vata")) {
            return "Vata-Pitta Frame (Lean-Athletic, Quick Reflexes & Energetic Stamina)";
        }
        if (dominantPrakriti.contains("Kapha-Pitta") || dominantPrakriti.contains("Pitta-Kapha")) {
            return "Kapha-Pitta Frame (Strong Muscular Build, High Stamina & Solid Structure)";
        }
        if (dominantPrakriti.contains("Vata-Kapha") || dominantPrakriti.contains("Kapha-Vata")) {
            return "Vata-Kapha Frame (Variable Bone Structure, Cold Sensitivity & Steady Endurance)";
        }
        return "Madhya Deha (Balanced Proportions)";
    }

    private static String determinePrimaryDhatu(String lagnaLord, String moonSignLord, String dominantPrakriti) {
        String keyLord = lagnaLord != null ? lagnaLord.toLowerCase() : "sun";
        return switch (keyLord) {
            case "sun" -> "Asthi Dhatu (Bone Density & Skeletal Structural Strength)";
            case "moon" -> "Rakta & Rasa Dhatu (Blood Plasma, Bodily Fluids & Lymphatic Flow)";
            case "mars" -> "Majja & Mamsa Dhatu (Bone Marrow, Muscle Tone & Vital Red Blood Cells)";
            case "mercury" -> "Tvak & Rasa Dhatu (Skin Barrier, Plasma & Neural Fluid Channels)";
            case "jupiter" -> "Meda Dhatu (Adipose Tissue, Healthy Fats & Glandular Nourishment)";
            case "venus" -> "Shukra Dhatu (Reproductive Tissue, Vitality & Ojas Immunity)";
            case "saturn" -> "Snayu & Asthi Dhatu (Nerves, Tendons, Ligaments & Joint Lubrication)";
            default -> "Rasa & Rakta Dhatu (Vital Plasma & Fluids)";
        };
    }

    private static String determineRecommendedRasayana(String dominantPrakriti) {
        if (dominantPrakriti.contains("Vata")) {
            return "Ashwagandha, Warm Sesame Oil massage, Bala, and Dashamoola";
        }
        if (dominantPrakriti.contains("Pitta")) {
            return "Amalaki (Amla), Guduchi (Giloy), Shatavari, and Brahmi Ghee";
        }
        if (dominantPrakriti.contains("Kapha")) {
            return "Triphala, Trikatu (Dry Ginger/Black Pepper/Pippali), Tulsi, and Guggulu";
        }
        return "Chyawanprash, Brahmi, Amalaki, and Triphala for holistic Tridosha Rasayana";
    }

    private static List<String> calculateOrganVulnerabilities(
            int lagnaSign,
            int rogaSign,
            String rogaLord,
            List<ChartResponseDTO.PositionDetail> d1Chart) {

        List<String> vulnerabilities = new ArrayList<>();

        // 1. Roga Sign based vulnerability (House 6)
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

        // 2. Roga Lord based vulnerability
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

        // 3. Dynamic Dusthana Occupants (House 6, House 8, House 12)
        int eighthSign = ((lagnaSign - 1 + 7) % 12) + 1;
        int twelfthSign = ((lagnaSign - 1 + 11) % 12) + 1;

        if (d1Chart != null) {
            for (ChartResponseDTO.PositionDetail p : d1Chart) {
                if ("LAGNA".equalsIgnoreCase(p.getPlanetKey())) continue;
                int sign = p.getSignNumber();
                String name = p.getDisplayName() != null ? p.getDisplayName() : p.getPlanetKey();

                if (sign == rogaSign) {
                    vulnerabilities.add(getDusthanaPlanetPathology(name, 6));
                } else if (sign == eighthSign) {
                    vulnerabilities.add(getDusthanaPlanetPathology(name, 8));
                } else if (sign == twelfthSign) {
                    vulnerabilities.add(getDusthanaPlanetPathology(name, 12));
                }
            }
        }

        // 4. 8th House (Ayurdaya / Chronic resilience anchor)
        String eighthLord = PlanetDignityUtils.getSignLord(eighthSign);
        vulnerabilities.add("Longevity resilience & chronic vitality maintenance governed by 8th Lord " + eighthLord + " in " + RASHIS[eighthSign - 1]);

        return vulnerabilities;
    }

    private static String getDusthanaPlanetPathology(String planet, int house) {
        String p = planet.trim().toLowerCase();
        String houseLabel = "House " + house;
        return switch (p) {
            case "mars", "kuja", "sevvai", "mangal" -> "Acute inflammatory spikes, muscular strain & bile heat sensitivity (" + planet + " in " + houseLabel + ")";
            case "saturn", "shani" -> "Joint stiffness, chronic dryness, sciatica or tendon fatigue (" + planet + " in " + houseLabel + ")";
            case "rahu" -> "Environmental allergies, food sensitivities & psychosomatic sleep disturbances (" + planet + " in " + houseLabel + ")";
            case "ketu" -> "Sharp intestinal heat, unexpected digestive hypersensitivity & subtle energy depletion (" + planet + " in " + houseLabel + ")";
            case "sun", "surya" -> "Cardiovascular stamina under stress, eyesight sensitivity & bone calcium absorption (" + planet + " in " + houseLabel + ")";
            case "moon", "chandra" -> "Lymphatic sluggishness, fluid retention & emotional psychosomatic digestion (" + planet + " in " + houseLabel + ")";
            case "venus", "shukra" -> "Renal hydration balance, endocrine equilibrium & urinary tract health (" + planet + " in " + houseLabel + ")";
            case "jupiter", "guru" -> "Hepatic liver metabolism, lipid balance & arterial circulation (" + planet + " in " + houseLabel + ")";
            case "mercury", "budha" -> "Enteric nervous system, skin barrier resilience & respiratory bronchial reactivity (" + planet + " in " + houseLabel + ")";
            default -> "Metabolic sensitivity and immune caution (" + planet + " in " + houseLabel + ")";
        };
    }

    private static List<String> calculateDietaryDirectives(String prakriti) {
        List<String> directives = new ArrayList<>();
        if (prakriti.contains("Sama")) {
            directives.add("Maintain balanced intake of all 6 tastes (Shad Rasas: Sweet, Sour, Salty, Pungent, Bitter, Astringent) according to seasonal transitions (Ritu Sandhi)");
            directives.add("Favor freshly prepared Sattvic whole grains, organic vegetables, pure cow's ghee, and seasonal fruits to preserve natural equilibrium");
        } else if (prakriti.contains("Pitta")) {
            directives.add("Favor cooling, grounding, fresh whole foods with natural sweet, bitter, and astringent tastes (Shad Rasa: Madhura, Tikta, Kashaya)");
            directives.add("Limit pungent spices, sour citrus excess, fried oils, and late-night heavy meals during high Pitta seasons");
        } else if (prakriti.contains("Vata")) {
            directives.add("Favor warm, nourishing, easily digestible cooked meals with healthy fats (ghee, sesame oil) and sweet, sour, salty tastes (Shad Rasa: Madhura, Amla, Lavana)");
            directives.add("Maintain consistent meal schedules; avoid dry, cold, raw, and highly carbonated items");
        } else {
            directives.add("Favor light, warm, dry, and mildly spiced preparations with pungent, bitter, and astringent tastes (Shad Rasa: Katu, Tikta, Kashaya) to stimulate metabolic Agni");
            directives.add("Minimize heavy dairy, refined sugars, cold beverages, and sedentary post-meal habits");
        }
        directives.add("Incorporate gentle daily Pranayama (Nadi Shodhana / Sheetali) and rhythmic sleep cycles to protect Ojas (vital immunity)");
        return directives;
    }
}
