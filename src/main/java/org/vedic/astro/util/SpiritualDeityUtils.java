package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.PlanetaryPosition;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spiritual Anchors & Deities Engine (Deiva Pulligal) based on Jaimini Sutras & Brihat Parasara Hora Shastra.
 * Computes Atmakaraka, Karakamsa in D9, Ishta Devata (12th from Karakamsa), Dharma Devata (9th from Karakamsa),
 * and Kula Devata 5th house/lord lineage status.
 */
public class SpiritualDeityUtils {

    private static final String[] RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Kataka", "Simha", "Kanya",
            "Tula", "Vrishchika", "Dhanus", "Makara", "Kumbha", "Meena"
    };

    private static final String[] RASHIS_TA = {
            "மேஷம்", "ரிஷபம்", "மிதுனம்", "கடகம்", "சிம்மம்", "கன்னி",
            "துலாம்", "விருச்சிகம்", "தனுசு", "மகரம்", "கும்பம்", "மீனம்"
    };

    public record SpiritualDeitiesResult(
            String atmakarakaPlanet,
            String karakamsaSignD9,
            String ishtaDevata,
            String ishtaDevataTamil,
            String ishtaDevataRationale,
            String ishtaDevataRationaleTamil,
            String ishtaDevataRationaleEnglish,
            String kulaDevataBlessingStatus,
            String kulaDevataRemedy,
            String dharmaDevata,
            String dharmaDevataTamil,
            String dharmaDevataRationale,
            String dharmaDevataRationaleTamil,
            String dharmaDevataRationaleEnglish
    ) {}

    public static SpiritualDeitiesResult calculateSpiritualDeities(
            Map<String, PlanetaryPosition> d1,
            List<ChartResponseDTO.PositionDetail> d9Navamsa) {

        if (d1 == null || d1.isEmpty()) {
            return new SpiritualDeitiesResult(
                    "Sun", "Mesha",
                    "Lord Shiva / Lord Rama", "ஸ்ரீ சிவன் / ராமர்",
                    "நவக்கிரகங்களின் தலைமை ஆத்மகாரகன்",
                    "நவக்கிரகங்களின் தலைமை ஆத்மகாரகன்",
                    "Default Atmakaraka anchor",
                    "BLESSED", null,
                    "Lord Vishnu / Lord Venkateshwara", "ஸ்ரீ மகாவிஷ்ணு / வேங்கடாஜலபதி",
                    "Default Dharma Devata anchor",
                    "Default Dharma Devata anchor",
                    "Default Dharma Devata anchor"
            );
        }

        // 1. Identify Atmakaraka (AK) - Planet with highest degree in sign (0-30 deg) among 7 classical planets
        String[] classicalPlanets = {"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"};
        String atmakaraka = "Sun";
        double maxDegree = -1.0;

        for (String p : classicalPlanets) {
            PlanetaryPosition pos = d1.get(p);
            if (pos != null) {
                double deg = pos.getDegreeInSign();
                if (deg > maxDegree) {
                    maxDegree = deg;
                    atmakaraka = p;
                }
            }
        }

        // 2. Locate Karakamsa (AK sign in D9 Navamsa)
        int karakamsaSignNum = 1;
        Map<String, Integer> d9Map = new HashMap<>();
        Map<Integer, List<String>> d9SignToPlanets = new HashMap<>();

        if (d9Navamsa != null) {
            for (var detail : d9Navamsa) {
                if (detail.getPlanetKey() != null) {
                    String key = detail.getPlanetKey().toUpperCase();
                    d9Map.put(key, detail.getSignNumber());
                    d9SignToPlanets.computeIfAbsent(detail.getSignNumber(), k -> new ArrayList<>()).add(key);
                }
            }
        }

        Integer akD9Sign = d9Map.get(atmakaraka.toUpperCase());
        if (akD9Sign != null) {
            karakamsaSignNum = akD9Sign;
        } else {
            PlanetaryPosition akPos = d1.get(atmakaraka);
            karakamsaSignNum = akPos != null ? akPos.getSignNumber() : 1;
        }
        String karakamsaSignName = RASHIS[karakamsaSignNum - 1];

        // 3. Ishta Devata: 12th House from Karakamsa in D9 (Moksha Sthana)
        int twelfthSignNum = ((karakamsaSignNum - 1 + 11) % 12) + 1;
        HouseAnalysis ishtaAnalysis = analyzeKarakamsaHouse(12, twelfthSignNum, karakamsaSignNum, d9SignToPlanets);
        String ishtaDevata = mapPlanetToIshtaDevata(ishtaAnalysis.dominantPlanet(), false);
        String ishtaDevataTa = mapPlanetToIshtaDevata(ishtaAnalysis.dominantPlanet(), true);

        // 4. Dharma Devata: 9th House from Karakamsa in D9 (Dharma Sthana)
        int ninthSignNum = ((karakamsaSignNum - 1 + 8) % 12) + 1;
        HouseAnalysis dharmaAnalysis = analyzeKarakamsaHouse(9, ninthSignNum, karakamsaSignNum, d9SignToPlanets);
        String dharmaDevata = mapPlanetToIshtaDevata(dharmaAnalysis.dominantPlanet(), false);
        String dharmaDevataTa = mapPlanetToIshtaDevata(dharmaAnalysis.dominantPlanet(), true);

        // 5. Kula Devata Status: 5th House & 5th Lord in D1
        int lagnaSign = d1.get("Lagna") != null ? d1.get("Lagna").getSignNumber() : 1;
        int fifthSign = ((lagnaSign - 1 + 4) % 12) + 1;
        String fifthLord = PlanetDignityUtils.getSignLord(fifthSign);

        boolean fifthAfflicted = false;
        PlanetaryPosition rahuPos = d1.get("Rahu");
        PlanetaryPosition ketuPos = d1.get("Ketu");
        PlanetaryPosition saturnPos = d1.get("Saturn");

        if (rahuPos != null && rahuPos.getSignNumber() == fifthSign) fifthAfflicted = true;
        if (ketuPos != null && ketuPos.getSignNumber() == fifthSign) fifthAfflicted = true;
        if (saturnPos != null && saturnPos.getSignNumber() == fifthSign) fifthAfflicted = true;

        PlanetaryPosition fifthLordPos = d1.get(fifthLord);
        if (fifthLordPos != null) {
            int flSign = fifthLordPos.getSignNumber();
            if (rahuPos != null && rahuPos.getSignNumber() == flSign) fifthAfflicted = true;
            if (ketuPos != null && ketuPos.getSignNumber() == flSign) fifthAfflicted = true;
        }

        String kulaStatus = fifthAfflicted ? "BLOCKED_ANCESTRAL_DOSHA" : "BLESSED";
        String kulaRemedy = fifthAfflicted ?
                "Ancestral blessings require attention. Lighting a ghee lamp at the Kula Devata temple and offering Pitru Tarpanam on Amavasya/Purnima is recommended." :
                "Kula Devata and ancestral blessings are strongly protective, promoting family harmony and progeny wellbeing.";

        return new SpiritualDeitiesResult(
                atmakaraka,
                karakamsaSignName,
                ishtaDevata,
                ishtaDevataTa,
                ishtaAnalysis.rationaleTamil(),
                ishtaAnalysis.rationaleTamil(),
                ishtaAnalysis.rationaleEnglish(),
                kulaStatus,
                kulaRemedy,
                dharmaDevata,
                dharmaDevataTa,
                dharmaAnalysis.rationaleTamil(),
                dharmaAnalysis.rationaleTamil(),
                dharmaAnalysis.rationaleEnglish()
        );
    }

    public record HouseAnalysis(
            String dominantPlanet,
            List<String> occupants,
            String signLord,
            boolean isOccupied,
            boolean hasMultipleOccupants,
            String rationaleTamil,
            String rationaleEnglish
    ) {}

    private static HouseAnalysis analyzeKarakamsaHouse(
            int houseNumber,
            int targetSignNum,
            int karakamsaSignNum,
            Map<Integer, List<String>> d9SignToPlanets
    ) {
        String signNameEn = RASHIS[targetSignNum - 1];
        String signNameTa = RASHIS_TA[targetSignNum - 1];
        String karakamsaNameEn = RASHIS[karakamsaSignNum - 1];
        String karakamsaNameTa = RASHIS_TA[karakamsaSignNum - 1];
        String signLord = PlanetDignityUtils.getSignLord(targetSignNum);
        String lordTa = getPlanetNameTamil(signLord);
        String houseLabelTa = (houseNumber == 12) ? "12-ஆம் வீடான (மோக்ஷ ஸ்தானம்)" : "9-ஆம் வீடான (தர்ம ஸ்தானம்)";
        String houseLabelEn = (houseNumber == 12) ? "12th house (Moksha Sthana)" : "9th house (Dharma Sthana)";

        List<String> rawOccupants = d9SignToPlanets.getOrDefault(targetSignNum, Collections.emptyList());
        List<String> occupants = rawOccupants.stream()
                .map(SpiritualDeityUtils::normalizePlanetName)
                .filter(p -> p != null && !p.equalsIgnoreCase("Lagna"))
                .distinct()
                .toList();

        if (occupants.isEmpty()) {
            // Scenario 1: Empty House -> Sign Lord takes precedence
            String rationaleTa = "ஜைமினி சூத்திரப்படி நவாம்ச காரகாம்ச ராசிக்கு (" + karakamsaNameTa +
                    ") " + houseLabelTa + " " + signNameTa + " -ல் கிரகங்கள் அமராததால், " +
                    "அந்த வீட்டின் அதிபதி " + lordTa + " மூலம் நிர்ணயிக்கப்பட்டது.";
            String rationaleEn = "According to Jaimini Sutras, since the " + houseLabelEn + " " + signNameEn +
                    " from Karakamsa (" + karakamsaNameEn + ") in D9 Navamsa is unoccupied, it is governed by its sign lord " + signLord + ".";
            return new HouseAnalysis(signLord, occupants, signLord, false, false, rationaleTa, rationaleEn);
        } else if (occupants.size() == 1) {
            // Scenario 2: Single Occupant Planet in D9
            String occ = occupants.get(0);
            String occTa = getPlanetNameTamil(occ);
            String ketuSpecialTa = occ.equalsIgnoreCase("Ketu") ? " (மோக்ஷ காரகரான கேது பகவான் நின்றதால் ஞான முக்தி அருள் கிட்டும்)" : "";
            String ketuSpecialEn = occ.equalsIgnoreCase("Ketu") ? " (Ketu as Moksha Karaka grants spiritual liberation/Kaivalya)" : "";

            String rationaleTa = "ஜைமினி சூத்திரப்படி நவாம்ச காரகாம்சத்திற்கு (" + karakamsaNameTa +
                    ") " + houseLabelTa + " " + signNameTa + " -ல் அமர்ந்துள்ள கிரகம் " +
                    occTa + " மூலம் நிர்ணயிக்கப்பட்டது. (ராசி அதிபதி: " + lordTa + ")." + ketuSpecialTa;
            String rationaleEn = "According to Jaimini Sutras, governed by " + occ + " occupying the " +
                    houseLabelEn + " " + signNameEn + " from Karakamsa (" + karakamsaNameEn + ") in D9 Navamsa (Sign lord: " + signLord + ")." + ketuSpecialEn;
            return new HouseAnalysis(occ, occupants, signLord, true, false, rationaleTa, rationaleEn);
        } else {
            // Scenario 3: Multiple Occupant Planets in D9 (Conjunction)
            String dominant = selectDominantOccupant(occupants, targetSignNum, houseNumber == 12);
            String domTa = getPlanetNameTamil(dominant);
            String allOccTa = occupants.stream().map(SpiritualDeityUtils::getPlanetNameTamil).collect(Collectors.joining(", "));
            String allOccEn = String.join(", ", occupants);

            String rationaleTa = "ஜைமினி சூத்திரப்படி நவாம்ச காரகாம்சத்திற்கு (" + karakamsaNameTa +
                    ") " + houseLabelTa + " " + signNameTa + " -ல் பல கிரகங்கள் (" + allOccTa +
                    ") இணைந்துள்ளன. இதில் முதன்மையான கிரகம் " + domTa + " வழியே நிர்ணயிக்கப்பட்டது. (ராசி அதிபதி: " + lordTa + ").";
            String rationaleEn = "According to Jaimini Sutras, governed by dominant planet " + dominant +
                    " among multiple occupants (" + allOccEn + ") in the " + houseLabelEn + " " + signNameEn +
                    " from Karakamsa (" + karakamsaNameEn + ") in D9 Navamsa (Sign lord: " + signLord + ").";
            return new HouseAnalysis(dominant, occupants, signLord, true, true, rationaleTa, rationaleEn);
        }
    }

    private static String selectDominantOccupant(List<String> occupants, int signNum, boolean is12thHouse) {
        // 1. Exalted planet in D9
        for (String p : occupants) {
            if (PlanetDignityUtils.isExalted(p, signNum)) {
                return p;
            }
        }
        // 2. Own sign in D9
        for (String p : occupants) {
            if (PlanetDignityUtils.isOwnSign(p, signNum)) {
                return p;
            }
        }
        // 3. For 12th house (Moksha), Ketu has special Jaimini importance (Kaivalya Karaka)
        if (is12thHouse && occupants.contains("Ketu")) {
            return "Ketu";
        }
        // 4. Natural planetary hierarchy: Sun > Jupiter > Mars > Venus > Mercury > Moon > Saturn > Rahu > Ketu
        List<String> naturalOrder = List.of("Sun", "Jupiter", "Mars", "Venus", "Mercury", "Moon", "Saturn", "Rahu", "Ketu");
        for (String p : naturalOrder) {
            if (occupants.contains(p)) {
                return p;
            }
        }
        return occupants.get(0);
    }

    public static String getPlanetNameTamil(String planet) {
        if (planet == null) return "";
        return switch (planet.trim().toUpperCase()) {
            case "SUN", "SURYA" -> "சூரியன்";
            case "MOON", "CHANDRA" -> "சந்திரன்";
            case "MARS", "KUJA", "SEVVAI", "MANGAL" -> "செவ்வாய்";
            case "MERCURY", "BUDHA" -> "புதன்";
            case "JUPITER", "GURU" -> "குரு";
            case "VENUS", "SHUKRA" -> "சுக்கிரன்";
            case "SATURN", "SHANI" -> "சனி";
            case "RAHU" -> "ராகு";
            case "KETU" -> "கேது";
            case "LAGNA", "ASC" -> "லக்னம்";
            default -> planet;
        };
    }

    public static String normalizePlanetName(String p) {
        if (p == null) return null;
        String s = p.trim().toUpperCase();
        return switch (s) {
            case "SUN", "SURYA" -> "Sun";
            case "MOON", "CHANDRA" -> "Moon";
            case "MARS", "KUJA", "SEVVAI", "MANGAL" -> "Mars";
            case "MERCURY", "BUDHA" -> "Mercury";
            case "JUPITER", "GURU" -> "Jupiter";
            case "VENUS", "SHUKRA" -> "Venus";
            case "SATURN", "SHANI" -> "Saturn";
            case "RAHU" -> "Rahu";
            case "KETU" -> "Ketu";
            case "LAGNA", "ASC" -> "Lagna";
            default -> p;
        };
    }

    public static String mapPlanetToIshtaDevata(String planet, boolean tamil) {
        if (planet == null) return tamil ? "சிவபெருமான்" : "Lord Shiva";
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya" -> tamil ? "ஸ்ரீ சிவன் / ராமர்" : "Lord Shiva / Lord Rama";
            case "moon", "chandra" -> tamil ? "ஸ்ரீ பார்வதி / கௌரி / கிருஷ்ணர்" : "Goddess Parvati / Goddess Gauri / Lord Krishna";
            case "mars", "kuja", "sevvai", "mangal" -> tamil ? "ஸ்ரீ முருகப்பெருமான் / நரசிம்மர் / சுப்பிரமணியர்" : "Lord Murugan / Lord Narasimha / Kartikeya";
            case "mercury", "budha" -> tamil ? "ஸ்ரீ மகாவிஷ்ணு / வேங்கடாஜலபதி / பெருமாள்" : "Lord Vishnu / Lord Venkateshwara";
            case "jupiter", "guru" -> tamil ? "ஸ்ரீ தக்ஷிணாமூர்த்தி / ஹயக்ரீவர் / பிரம்மா" : "Lord Dakshinamurthy / Lord Hayagriva";
            case "venus", "shukra" -> tamil ? "ஸ்ரீ மகாலட்சுமி / அன்னபூரணி / ஸ்ரீ வித்யா" : "Goddess Maha Lakshmi / Annapoorneshwari";
            case "saturn", "shani" -> tamil ? "ஸ்ரீ அனுமான் / சனி பகவான் / கருப்பணசுவாமி / சாஸ்தா" : "Lord Hanuman / Shani Deva / Lord Ayyappan";
            case "rahu" -> tamil ? "ஸ்ரீ துர்க்கை / வாராஹி அம்மன் / பத்ரகாளி" : "Goddess Durga / Goddess Varahi / Bhadrakali";
            case "ketu" -> tamil ? "ஸ்ரீ விநாயகர் (மகா கணபதி) / ஞான காரகன்" : "Lord Ganesha (Maha Ganapati)";
            default -> tamil ? "ஸ்ரீ மகாவிஷ்ணு" : "Lord Vishnu";
        };
    }
}
