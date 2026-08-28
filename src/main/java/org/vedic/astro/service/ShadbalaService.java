package org.vedic.astro.service;

import org.springframework.stereotype.Service;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.PlanetDignityUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Authentic Classical Shadbala (Six-Fold Planetary Strength) Calculation Engine
 * based on Brihat Parasara Hora Shastra (BPHS Chapters 27-29).
 *
 * All values are computed in Virupas (60 Virupas = 1 Rupa).
 */
@Service
public class ShadbalaService {

    // Classical BPHS Deep Exaltation Points (in Absolute Longitude 0-360 degrees)
    private static final Map<String, Double> DEEP_EXALTATION_POINTS = Map.of(
            "Sun", 10.0,        // 10 deg Aries
            "Moon", 33.0,       // 3 deg Taurus
            "Mars", 298.0,      // 28 deg Capricorn
            "Mercury", 165.0,   // 15 deg Virgo
            "Jupiter", 95.0,    // 5 deg Cancer
            "Venus", 357.0,     // 27 deg Pisces
            "Saturn", 200.0     // 20 deg Libra
    );

    // Minimum Required Shadbala in Rupas for planetary potency per BPHS
    private static final Map<String, Double> MINIMUM_SHADBALA_RUPAS = Map.of(
            "Sun", 6.5,
            "Moon", 6.0,
            "Mars", 5.0,
            "Mercury", 7.0,
            "Jupiter", 6.5,
            "Venus", 5.5,
            "Saturn", 5.0
    );

    public ShadbalaDTO calculateShadbala(Map<String, PlanetaryPosition> d1Map) {
        if (d1Map == null || d1Map.isEmpty()) {
            return ShadbalaDTO.builder().planetStrengths(Map.of()).build();
        }

        PlanetaryPosition lagna = d1Map.get("Lagna");
        int lagnaSign = lagna != null ? lagna.getSignNumber() : 1;
        double lagnaLong = lagna != null ? lagna.getAbsoluteLongitude() : 0.0;

        PlanetaryPosition sunPos = d1Map.get("Sun");
        PlanetaryPosition moonPos = d1Map.get("Moon");
        double sunLong = sunPos != null ? sunPos.getAbsoluteLongitude() : 0.0;
        double moonLong = moonPos != null ? moonPos.getAbsoluteLongitude() : 0.0;

        // Determine Day/Night birth (Sun above horizon / in houses 7-12 relative to Lagna)
        int sunHouse = sunPos != null ? PlanetDignityUtils.getHouseFromLagna(sunPos.getSignNumber(), lagnaSign) : 1;
        boolean isDayBirth = (sunHouse >= 7 && sunHouse <= 12);

        Map<String, ShadbalaDTO.PlanetaryStrength> results = new LinkedHashMap<>();

        for (String planet : new String[]{"Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"}) {
            PlanetaryPosition pos = d1Map.get(planet);
            if (pos == null) continue;

            double sthanaBala = computeSthanaBala(planet, pos, lagnaSign);
            double digBala = computeDigBala(planet, pos, lagnaLong);
            double kalaBala = computeKalaBala(planet, pos, sunLong, moonLong, isDayBirth);
            double cheshtaBala = computeCheshtaBala(planet, pos, sunLong);
            double naisargikaBala = getNaisargikaBala(planet);
            double drigBala = computeDrigBala(planet, pos, d1Map);

            double totalVirupas = sthanaBala + digBala + kalaBala + cheshtaBala + naisargikaBala + drigBala;
            double totalRupas = Math.round((totalVirupas / 60.0) * 100.0) / 100.0;

            double minRequired = MINIMUM_SHADBALA_RUPAS.getOrDefault(planet, 5.0);
            String category = evaluateStrengthCategory(totalRupas, minRequired);

            results.put(planet, ShadbalaDTO.PlanetaryStrength.builder()
                    .sthanaBala(Math.round(sthanaBala * 10.0) / 10.0)
                    .digBala(Math.round(digBala * 10.0) / 10.0)
                    .kalaBala(Math.round(kalaBala * 10.0) / 10.0)
                    .cheshtaBala(Math.round(cheshtaBala * 10.0) / 10.0)
                    .drigBala(Math.round(drigBala * 10.0) / 10.0)
                    .totalShadbalaRupas(totalRupas)
                    .strengthCategory(category)
                    .build());
        }

        return ShadbalaDTO.builder().planetStrengths(results).build();
    }

    // ==========================================
    // 1. STHANA BALA (Positional Strength)
    // ==========================================
    private double computeSthanaBala(String planet, PlanetaryPosition pos, int lagnaSign) {
        int sign = pos.getSignNumber();
        double absLong = pos.getAbsoluteLongitude();
        double degInSign = pos.getDegreeInSign();
        int houseFromLagna = PlanetDignityUtils.getHouseFromLagna(sign, lagnaSign);

        // A. Uchcha Bala (Exaltation strength: 0 to 60 Virupas)
        double uchchaPoint = DEEP_EXALTATION_POINTS.getOrDefault(planet, 0.0);
        double neechaPoint = (uchchaPoint + 180.0) % 360.0;
        double diffFromNeecha = Math.abs(absLong - neechaPoint);
        if (diffFromNeecha > 180.0) diffFromNeecha = 360.0 - diffFromNeecha;
        double uchchaBala = (diffFromNeecha / 180.0) * 60.0;

        // B. Saptavargiya Bala (Approximate composite dignity across primary vargas: D1, D9, etc.)
        double saptavargiyaBala = 7.5; // Neutral baseline
        if (PlanetDignityUtils.isExalted(planet, sign)) {
            saptavargiyaBala = 45.0; // Moolatrikona / Exalted tier
        } else if (PlanetDignityUtils.isOwnSign(planet, sign)) {
            saptavargiyaBala = 30.0; // Own sign
        } else if (PlanetDignityUtils.isDebilitated(planet, sign)) {
            saptavargiyaBala = 1.875; // Great enemy / debilitation
        } else {
            // Friendly vs Enemy sign evaluation
            saptavargiyaBala = 15.0; // Friend sign average
        }
        // D9 Navamsa contribution
        int navamsaSign = ((sign - 1) * 9 + (int) (degInSign / 3.333333)) % 12 + 1;
        if (PlanetDignityUtils.isOwnSign(planet, navamsaSign) || PlanetDignityUtils.isExalted(planet, navamsaSign)) {
            saptavargiyaBala += 22.5;
        } else {
            saptavargiyaBala += 11.25;
        }

        // C. Ojhayugmarasyamsa Bala (Odd/Even sign and navamsa strength: 15 or 30 Virupas)
        double ojhaBala = 0.0;
        boolean isMalePlanet = "Sun".equals(planet) || "Mars".equals(planet) || "Jupiter".equals(planet) || "Mercury".equals(planet);
        boolean isOddSign = (sign % 2 != 0);
        boolean isOddNavamsa = (navamsaSign % 2 != 0);
        if (isMalePlanet) {
            if (isOddSign) ojhaBala += 15.0;
            if (isOddNavamsa) ojhaBala += 15.0;
        } else {
            if (!isOddSign) ojhaBala += 15.0;
            if (!isOddNavamsa) ojhaBala += 15.0;
        }

        // D. Kendradi Bala (Kendra=60, Panaphara=30, Apoklima=15 Virupas)
        double kendradiBala = switch (houseFromLagna) {
            case 1, 4, 7, 10 -> 60.0;
            case 2, 5, 8, 11 -> 30.0;
            case 3, 6, 9, 12 -> 15.0;
            default -> 30.0;
        };

        // E. Drekkana Bala (Decanate gender strength: 15 Virupas)
        double drekkanaBala = 0.0;
        int decanate = (int) (degInSign / 10.0) + 1; // 1 (0-10), 2 (10-20), 3 (20-30)
        if (isMalePlanet && decanate == 1) drekkanaBala = 15.0;
        else if (("Mercury".equals(planet) || "Saturn".equals(planet)) && decanate == 2) drekkanaBala = 15.0;
        else if (("Moon".equals(planet) || "Venus".equals(planet)) && decanate == 3) drekkanaBala = 15.0;

        return uchchaBala + saptavargiyaBala + ojhaBala + kendradiBala + drekkanaBala;
    }

    // ==========================================
    // 2. DIG BALA (Directional Strength)
    // ==========================================
    private double computeDigBala(String planet, PlanetaryPosition pos, double lagnaLong) {
        // Digbala power points reckoned from Lagna Longitude:
        // Jupiter & Mercury -> 1st House Cusp (Lagna Longitude)
        // Sun & Mars -> 10th House Cusp (Lagna + 270 deg)
        // Saturn -> 7th House Cusp (Lagna + 180 deg)
        // Moon & Venus -> 4th House Cusp (Lagna + 90 deg)
        double powerPoint = switch (planet) {
            case "Jupiter", "Mercury" -> lagnaLong;
            case "Sun", "Mars" -> (lagnaLong + 270.0) % 360.0;
            case "Saturn" -> (lagnaLong + 180.0) % 360.0;
            case "Moon", "Venus" -> (lagnaLong + 90.0) % 360.0;
            default -> lagnaLong;
        };

        // Powerless point is exactly opposite (180 deg away)
        double powerlessPoint = (powerPoint + 180.0) % 360.0;
        double arc = Math.abs(pos.getAbsoluteLongitude() - powerlessPoint);
        if (arc > 180.0) arc = 360.0 - arc;

        // Dig Bala in Virupas = Arc / 3.0 (Range: 0.0 to 60.0 Virupas)
        return Math.min(60.0, Math.max(0.0, arc / 3.0));
    }

    // ==========================================
    // 3. KALA BALA (Temporal Strength)
    // ==========================================
    private double computeKalaBala(String planet, PlanetaryPosition pos, double sunLong, double moonLong, boolean isDayBirth) {
        // A. Nathonatha Bala (Diurnal / Nocturnal)
        double nathoBala = 0.0;
        switch (planet) {
            case "Sun", "Jupiter", "Venus" -> nathoBala = isDayBirth ? 60.0 : 0.0;
            case "Moon", "Mars", "Saturn" -> nathoBala = isDayBirth ? 0.0 : 60.0;
            case "Mercury" -> nathoBala = 60.0; // Mercury is always strong
        }

        // B. Paksha Bala (Lunar Phase Strength)
        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        double beneficPaksha = elongation / 3.0; // 0 to 60 Virupas in Shukla Paksha
        double pakshaBala = switch (planet) {
            case "Jupiter", "Venus" -> beneficPaksha;
            case "Moon" -> elongation > 120.0 && elongation < 240.0 ? 60.0 : beneficPaksha;
            case "Mercury" -> beneficPaksha;
            case "Sun", "Mars", "Saturn" -> 60.0 - beneficPaksha;
            default -> 30.0;
        };

        // C. Tribhaga Bala (1/3 Day/Night Segment Lord)
        double tribhagaBala = ("Jupiter".equals(planet)) ? 60.0 : 20.0;

        // D. Ayana Bala (Equinoctial / Declination Strength: approx 15 to 45 Virupas)
        double ayanaBala = 30.0;
        if ("Sun".equals(planet) || "Mars".equals(planet) || "Jupiter".equals(planet) || "Venus".equals(planet)) {
            ayanaBala = (pos.getSignNumber() <= 6) ? 45.0 : 15.0; // Uttarayana favor
        } else {
            ayanaBala = (pos.getSignNumber() > 6) ? 45.0 : 15.0; // Dakshinayana favor
        }

        // E. Dina & Hora Bala (Day & Hour Lord components: baseline 30 Virupas)
        double dinaHoraBala = 30.0;

        return nathoBala + pakshaBala + tribhagaBala + ayanaBala + dinaHoraBala;
    }

    // ==========================================
    // 4. CHESHTA BALA (Motional Strength)
    // ==========================================
    private double computeCheshtaBala(String planet, PlanetaryPosition pos, double sunLong) {
        // For Sun & Moon: Ayana Bala is taken as Cheshta Bala per BPHS
        if ("Sun".equals(planet) || "Moon".equals(planet)) {
            return (pos.getSignNumber() <= 6) ? 45.0 : 25.0;
        }

        // For Mars, Mercury, Jupiter, Venus, Saturn: Motional Speed & Retrogradation
        double speed = pos.getSpeed();
        if (speed < 0.0) {
            // Retrograde (Vakra): Full 60 Virupas
            return 60.0;
        } else if (speed < 0.05) {
            // Stationary / Anuvakra: 30 Virupas
            return 30.0;
        } else {
            // Direct motion: proportional to speed vs elongation from Sun
            double elongation = Math.abs(pos.getAbsoluteLongitude() - sunLong);
            if (elongation > 180.0) elongation = 360.0 - elongation;
            return Math.min(50.0, Math.max(15.0, 15.0 + (elongation / 180.0) * 35.0));
        }
    }

    // ==========================================
    // 5. NAISARGIKA BALA (Natural Fixed Strength)
    // ==========================================
    public static double getNaisargikaBala(String planet) {
        if (planet == null) return 0.0;
        return switch (planet.trim().toLowerCase()) {
            case "sun", "surya" -> 60.00;
            case "moon", "chandra" -> 51.43;
            case "venus", "shukra" -> 42.86;
            case "jupiter", "guru" -> 34.29;
            case "mercury", "budha" -> 25.71;
            case "mars", "kuja", "sevvai", "mangal" -> 17.14;
            case "saturn", "shani" -> 8.57;
            default -> 0.0;
        };
    }

    // ==========================================
    // 6. DRIG BALA (Aspectual Strength / Drishti)
    // ==========================================
    private double computeDrigBala(String planet, PlanetaryPosition pos, Map<String, PlanetaryPosition> d1Map) {
        double netAspect = 0.0;
        int targetSign = pos.getSignNumber();

        for (var entry : d1Map.entrySet()) {
            String aspectingPlanet = entry.getKey();
            if (aspectingPlanet.equalsIgnoreCase("Lagna") || aspectingPlanet.equalsIgnoreCase(planet)) continue;

            PlanetaryPosition aspector = entry.getValue();
            if (aspector == null) continue;

            int sourceSign = aspector.getSignNumber();
            if (PlanetDignityUtils.isAspecting(aspectingPlanet, sourceSign, targetSign)) {
                boolean isBenefic = "Jupiter".equals(aspectingPlanet) || "Venus".equals(aspectingPlanet);
                if (isBenefic) {
                    netAspect += 15.0; // Positive Drishti
                } else if ("Mars".equals(aspectingPlanet) || "Saturn".equals(aspectingPlanet) || "Sun".equals(aspectingPlanet)) {
                    netAspect -= 7.5; // Malefic Aspect reduction
                }
            }
        }
        return Math.max(-30.0, Math.min(45.0, netAspect));
    }

    private String evaluateStrengthCategory(double totalRupas, double minRequired) {
        double ratio = totalRupas / minRequired;
        if (ratio >= 1.25) return "VERY_STRONG";
        if (ratio >= 1.0) return "STRONG";
        if (ratio >= 0.8) return "MODERATE";
        return "WEAK";
    }
}
