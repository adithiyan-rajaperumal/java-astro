package org.vedic.astro.util;

import org.vedic.astro.dto.ChartResponseDTO;
import org.vedic.astro.model.PlanetaryPosition;

import java.util.*;

/**
 * Spiritual Anchors & Deities Engine (Deiva Pulligal) based on Jaimini Sutras & Brihat Parasara Hora Shastra.
 */
public class SpiritualDeityUtils {

    private static final String[] RASHIS = {
            "Mesha", "Vrishabha", "Mithuna", "Kataka", "Simha", "Kanya",
            "Tula", "Vrishchika", "Dhanus", "Makara", "Kumbha", "Meena"
    };

    public record SpiritualDeitiesResult(
            String atmakarakaPlanet,
            String karakamsaSignD9,
            String ishtaDevata,
            String ishtaDevataTamil,
            String ishtaDevataRationale,
            String kulaDevataBlessingStatus,
            String kulaDevataRemedy,
            String dharmaDevata,
            String dharmaDevataTamil
    ) {}

    public static SpiritualDeitiesResult calculateSpiritualDeities(
            Map<String, PlanetaryPosition> d1,
            List<ChartResponseDTO.PositionDetail> d9Navamsa) {

        if (d1 == null || d1.isEmpty()) {
            return new SpiritualDeitiesResult(
                    "Sun", "Mesha",
                    "Lord Shiva", "சிவபெருமான்",
                    "நவக்கிரகங்களின் தலைமை ஆத்மகாரகன்",
                    "BLESSED", null,
                    "Lord Vishnu", "மகாவிஷ்ணு"
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
            // Fallback to D1 sign
            PlanetaryPosition akPos = d1.get(atmakaraka);
            karakamsaSignNum = akPos != null ? akPos.getSignNumber() : 1;
        }
        String karakamsaSignName = RASHIS[karakamsaSignNum - 1];

        // 3. Ishta Devata: 12th House from Karakamsa in D9
        int twelfthSignNum = ((karakamsaSignNum - 1 + 11) % 12) + 1;
        String ishtaPlanet = getDominantPlanetForHouse(twelfthSignNum, d9SignToPlanets);
        String ishtaDevata = mapPlanetToIshtaDevata(ishtaPlanet, false);
        String ishtaDevataTa = mapPlanetToIshtaDevata(ishtaPlanet, true);
        String ishtaRationale = "ஜைமினி சூத்திரப்படி நவாம்ச காரகாம்ச ராசிக்கு (" + karakamsaSignName +
                ") 12-ஆம் வீடான " + RASHIS[twelfthSignNum - 1] + " அதிபதி/கிரகம் " + ishtaPlanet + " மூலம் நிர்ணயிக்கப்பட்டது.";

        // 4. Dharma Devata: 9th House from Karakamsa in D9
        int ninthSignNum = ((karakamsaSignNum - 1 + 8) % 12) + 1;
        String dharmaPlanet = getDominantPlanetForHouse(ninthSignNum, d9SignToPlanets);
        String dharmaDevata = mapPlanetToIshtaDevata(dharmaPlanet, false);
        String dharmaDevataTa = mapPlanetToIshtaDevata(dharmaPlanet, true);

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
                "குலதெய்வ வழிபாட்டில் குறை அல்லது பித்ரு தோஷ தாக்கம் தென்படுவதால், பௌர்ணமி/அமாவாசை தினங்களில் குலதெய்வக் கோயிலுக்குச் சென்று நெய்தீபம் ஏற்றி வழிபடுதல் மற்றும் எள் தீபம் ஏற்றி பித்ரு தர்ப்பணம் செய்தல் நலம் பயக்கும்." :
                "குலதெய்வ அருளும் பித்ரு ஆசிகளும் பரிபூரணமாக உள்ளது. குடும்பத்தில் ஒற்றுமையும் சந்தான விருத்தியும் தழைக்கும்.";

        return new SpiritualDeitiesResult(
                atmakaraka,
                karakamsaSignName,
                ishtaDevata,
                ishtaDevataTa,
                ishtaRationale,
                kulaStatus,
                kulaRemedy,
                dharmaDevata,
                dharmaDevataTa
        );
    }

    private static String getDominantPlanetForHouse(int signNumber, Map<Integer, List<String>> d9SignToPlanets) {
        List<String> occupants = d9SignToPlanets.get(signNumber);
        if (occupants != null && !occupants.isEmpty()) {
            return occupants.get(0); // Occupant takes precedence
        }
        return PlanetDignityUtils.getSignLord(signNumber);
    }

    public static String mapPlanetToIshtaDevata(String planet, boolean tamil) {
        if (planet == null) return tamil ? "சிவபெருமான்" : "Lord Shiva";
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya" -> tamil ? "ஸ்ரீ சிவன் / ராமர்" : "Lord Shiva / Lord Rama";
            case "moon", "chandra" -> tamil ? "ஸ்ரீ பார்வதி / கௌரி / கிருஷ்ணர்" : "Goddess Parvati / Lord Krishna";
            case "mars", "kuja", "sevvai", "mangal" -> tamil ? "ஸ்ரீ முருகப்பெருமான் / நரசிம்மர்" : "Lord Murugan / Lord Narasimha";
            case "mercury", "budha" -> tamil ? "ஸ்ரீ மகாவிஷ்ணு / வேங்கடாஜலபதி" : "Lord Vishnu / Lord Venkateshwara";
            case "jupiter", "guru" -> tamil ? "ஸ்ரீ தக்ஷிணாமூர்த்தி / ஹயக்ரீவர்" : "Lord Dakshinamurthy / Lord Hayagriva";
            case "venus", "shukra" -> tamil ? "ஸ்ரீ மகாலட்சுமி / அன்னபூரணி" : "Goddess Maha Lakshmi / Annapoorneshwari";
            case "saturn", "shani" -> tamil ? "ஸ்ரீ அனுமான் / சனி பகவான் / கருப்பணசுவாமி" : "Lord Hanuman / Shani Deva / Karuppanasamy";
            case "rahu" -> tamil ? "ஸ்ரீ துர்க்கை / வாராஹி அம்மன்" : "Goddess Durga / Goddess Varahi";
            case "ketu" -> tamil ? "ஸ்ரீ விநாயகர் (கணபதி)" : "Lord Ganesha";
            default -> tamil ? "ஸ்ரீ மகாவிஷ்ணு" : "Lord Vishnu";
        };
    }
}
