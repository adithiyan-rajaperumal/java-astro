package org.vedic.astro.panchangam.impl;

import de.thmac.swisseph.SweConst;
import de.thmac.swisseph.SweDate;
import de.thmac.swisseph.SwissEph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.model.AyanamsaType;
import org.vedic.astro.model.ChartResult;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.panchangam.PanchangamEngine;
import org.vedic.astro.panchangam.PanchangamType;
import org.vedic.astro.service.ChartOrchestrationService;
import org.vedic.astro.service.TimezoneService;
import org.vedic.astro.service.impl.VargaCalculationService;
import org.vedic.astro.service.impl.VargaCalculationService.VargaType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traditional Surya Siddhanta Panchangam Engine (சூரிய சித்தாந்தம்).
 * Implements Mahayuga revolution constants, Ujjain prime meridian Desantara
 * corrections,
 * and epicyclic equations of center (Manda and Sighra phala).
 */
@Service
@RequiredArgsConstructor
public class SuryaSiddhantaPanchangamEngine implements PanchangamEngine {

    private final SwissEph swissEph;
    private final TimezoneService timezoneService;
    private final ChartOrchestrationService orchestrationService;
    private final VargaCalculationService vargaService;

    // Kali Yuga Epoch: February 18, 3102 BCE (Julian Day = 588465.5)
    private static final double KALI_EPOCH_JD = 588465.5;

    // Surya Siddhanta Mahayuga Civil Days (1 Mahayuga = 4,320,000 Solar Years)
    private static final double MAHAYUGA_CIVIL_DAYS = 1577917828.0;

    // Ancient Prime Meridian of Ujjain
    private static final double UJJAIN_LONGITUDE_DEG = 75.768;

    // Mahayuga Revolutions
    private static final double REV_SUN = 4320000.0;
    private static final double REV_MOON = 57753336.0;
    private static final double REV_MARS = 2296832.0;
    private static final double REV_MERCURY_SIGHRA = 17937060.0;
    private static final double REV_JUPITER = 364220.0;
    private static final double REV_VENUS_SIGHRA = 7022376.0;
    private static final double REV_SATURN = 146568.0;
    private static final double REV_RAHU = 232238.0;

    /**
     * Fast calculation method for UI rendering. Returns ONLY D1 and D9.
     */
    @Override
    public ChartResult calculate(BirthDetailsDTO dto) {
        LocalDateTime localTime = LocalDateTime.of(dto.year(), dto.month(), dto.day(), dto.hour(), dto.minute(),
                dto.second());

        String resolvedZoneId = timezoneService.getTimezoneFromCoordinates(dto.latitude(), dto.longitude());
        ZoneId zoneId = ZoneId.of(resolvedZoneId);

        ZonedDateTime zonedBirthTime = ZonedDateTime.of(localTime, zoneId);
        LocalDateTime utcTime = zonedBirthTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        double hourFraction = utcTime.getHour() + (utcTime.getMinute() / 60.0) + (utcTime.getSecond() / 3600.0);
        SweDate sweDate = new SweDate(utcTime.getYear(), utcTime.getMonthValue(), utcTime.getDayOfMonth(),
                hourFraction);
        double julianDayUT = sweDate.getJulDay();

        // 1. Compute Desantara (Longitudinal time shift relative to Ujjain Prime
        // Meridian)
        double desantaraHours = ((dto.longitude() - UJJAIN_LONGITUDE_DEG) * 4.0) / 60.0;

        // 2. Compute Ahargana from Epoch
        double aharganaExact = (julianDayUT - KALI_EPOCH_JD) + (desantaraHours / 24.0);

        // 3. Calculate Ayanamsa Offset if configured
        double ayanamsaOffset = calculateAyanamsaOffset(julianDayUT, dto.ayanamsa());

        // 4. Compute Sun Longitude for Charakhanda Sunrise calculation
        double sunMean = calculateMeanLongitude(aharganaExact, REV_SUN);
        double sunTrue = (applyMandaCorrection(sunMean, 77.23, 14.0) + ayanamsaOffset + 360.0) % 360.0;

        // 5. Compute Pure Surya Siddhanta Sunrise LMT & Ghatikas
        double sunriseLmtHours = calculateSuryaSiddhantaSunriseLocalTime(sunTrue, dto.latitude(), dto.longitude());
        double ghatikasSinceSunrise = calculateUdayadiGhatikas(localTime.toLocalTime(), sunriseLmtHours);

        // 6. Compute Surya Siddhanta Longitudes
        Map<String, Double> longitudes = calculateSuryaSiddhantaLongitudes(aharganaExact, ghatikasSinceSunrise,
                dto.latitude(), ayanamsaOffset);

        // 6. Generate D1 Map
        Map<String, PlanetaryPosition> d1Map = vargaService.generateD1MapFromLongitudes(longitudes,
                this::getSuryaSiddhantaSpeed);

        // 7. Generate D9 (Navamsha) Map ONLY
        Map<String, PlanetaryPosition> d9Map = vargaService.generateVargaChart(d1Map, VargaType.D9_NAVAMSA);

        double longitudeOffsetMinutes = dto.longitude() * 4.0;
        LocalDateTime localMeanTime = utcTime.plusSeconds((long) (longitudeOffsetMinutes * 60));

        return ChartResult.builder()
                .name(dto.name())
                .localMeanTime(localMeanTime.toString())
                .julianDayUT(julianDayUT)
                .dateOfBirth(LocalDate.of(dto.year(), dto.month(), dto.day()).toString())
                .timeOfBirth(String.format("%02d:%02d:%02d", dto.hour(), dto.minute(), dto.second()))
                .d1Positions(d1Map)
                .d9Positions(d9Map)
                .build();
    }

    /**
     * Heavy report generation method for PDF export.
     * Computes Surya Siddhanta-consistent Equal House Cusps derived from SS Lagna.
     */
    @Override
    public ComprehensiveReportDTO generateComprehensiveReport(BirthDetailsDTO payload, ChartResult res) {
        // Compute Equal House Cusps derived from Surya Siddhanta Lagna Longitude
        double[] cusps = new double[13];
        PlanetaryPosition lagna = res.getD1Positions().get("Lagna");
        double lagnaLong = (lagna != null) ? lagna.getAbsoluteLongitude() : 0.0;

        for (int i = 1; i <= 12; i++) {
            cusps[i] = (lagnaLong + (i - 1) * 30.0) % 360.0;
        }

        // Pass calculated house cusps directly to orchestration service
        ComprehensiveReportDTO deepReportData = orchestrationService.compileComprehensivePdfData(res, payload, cusps);
        deepReportData.setResolvedTimezone(
                timezoneService.getTimezoneFromCoordinates(payload.latitude(), payload.longitude()));
        return deepReportData;
    }

    private Map<String, Double> calculateSuryaSiddhantaLongitudes(double ahargana, double ghatikas, double latitude,
            double ayanamsaOffset) {
        Map<String, Double> longitudes = new LinkedHashMap<>();

        // 1. Mean Longitudes (Madhyama Graha)
        double sunMean = calculateMeanLongitude(ahargana, REV_SUN);
        double moonMean = calculateMeanLongitude(ahargana, REV_MOON);

        // 2. Manda Correction (Equation of Center)
        double sunTrue = (applyMandaCorrection(sunMean, 77.23, 14.0) + ayanamsaOffset + 360.0) % 360.0;
        double moonTrue = (applyMandaCorrection(moonMean, 90.0, 31.83) + ayanamsaOffset + 360.0) % 360.0;

        longitudes.put("Sun", sunTrue);
        longitudes.put("Moon", moonTrue);

        // 3. Lagna via Udayadi Ghatikas
        double lagnaLong = (calculateSiddhantaLagna(sunTrue, ghatikas, latitude) + ayanamsaOffset + 360.0) % 360.0;
        longitudes.put("Lagna", lagnaLong);

        // 4. Taragrahas (Mars, Mercury, Jupiter, Venus, Saturn)
        longitudes.put("Mars",
                (calculateTaragraha(ahargana, REV_MARS, 130.0, 75.0, REV_SUN, 235.0, sunTrue) + ayanamsaOffset + 360.0)
                        % 360.0);
        longitudes.put("Mercury",
                (calculateTaragraha(ahargana, REV_SUN, 220.0, 30.0, REV_MERCURY_SIGHRA, 133.0, sunTrue) + ayanamsaOffset
                        + 360.0) % 360.0);
        longitudes.put("Jupiter", (calculateTaragraha(ahargana, REV_JUPITER, 170.0, 33.0, REV_SUN, 70.0, sunTrue)
                + ayanamsaOffset + 360.0) % 360.0);
        longitudes.put("Venus", (calculateTaragraha(ahargana, REV_SUN, 80.0, 11.0, REV_VENUS_SIGHRA, 262.0, sunTrue)
                + ayanamsaOffset + 360.0) % 360.0);
        longitudes.put("Saturn",
                (calculateTaragraha(ahargana, REV_SATURN, 240.0, 49.0, REV_SUN, 40.0, sunTrue) + ayanamsaOffset + 360.0)
                        % 360.0);

        // 5. Nodes (Rahu and Ketu move retrograde in SS)
        double rahu = (360.0 - calculateMeanLongitude(ahargana, REV_RAHU) + ayanamsaOffset + 360.0) % 360.0;
        double ketu = (rahu + 180.0) % 360.0;
        longitudes.put("Rahu", rahu);
        longitudes.put("Ketu", ketu);

        return longitudes;
    }

    private double calculateMeanLongitude(double ahargana, double totalRevolutions) {
        double totalRevs = (ahargana * totalRevolutions) / MAHAYUGA_CIVIL_DAYS;
        double fraction = totalRevs - Math.floor(totalRevs);
        return (fraction * 360.0 + 360.0) % 360.0;
    }

    private double applyMandaCorrection(double meanLong, double apogeeDeg, double epicycleCircumference) {
        double anomalyDeg = (meanLong - apogeeDeg + 360.0) % 360.0;
        double mandaEquationRad = Math.asin((epicycleCircumference / 360.0) * Math.sin(Math.toRadians(anomalyDeg)));
        return (meanLong - Math.toDegrees(mandaEquationRad) + 360.0) % 360.0;
    }

    private double calculateTaragraha(double ahargana, double meanRevs, double apogeeDeg, double mandaCirc,
            double sighraRevs, double sighraCirc, double sunTrue) {
        double meanLong = calculateMeanLongitude(ahargana, meanRevs);
        double mandaCorrected = applyMandaCorrection(meanLong, apogeeDeg, mandaCirc);

        double sighraMean = calculateMeanLongitude(ahargana, sighraRevs);
        double sighraAnomaly = (sighraMean - mandaCorrected + 360.0) % 360.0;

        double sighraEquationRad = Math.atan2(
                (sighraCirc / 360.0) * Math.sin(Math.toRadians(sighraAnomaly)),
                1.0 + (sighraCirc / 360.0) * Math.cos(Math.toRadians(sighraAnomaly)));

        return (mandaCorrected + Math.toDegrees(sighraEquationRad) + 360.0) % 360.0;
    }

    private double calculateSiddhantaLagna(double sunLongitude, double ghatikas, double latitude) {
        double[] rasimana = { 4.2, 4.8, 5.2, 5.4, 5.3, 5.1, 5.1, 5.3, 5.4, 5.2, 4.8, 4.2 };

        int currentRasiIdx = (int) (sunLongitude / 30.0);
        double remainingDegInSunRasi = 30.0 - (sunLongitude % 30.0);
        double ghatikasToClearSunRasi = (remainingDegInSunRasi / 30.0) * rasimana[currentRasiIdx];

        double g = ghatikas;
        if (g <= ghatikasToClearSunRasi) {
            return (sunLongitude + (g / rasimana[currentRasiIdx]) * 30.0) % 360.0;
        }

        g -= ghatikasToClearSunRasi;
        currentRasiIdx = (currentRasiIdx + 1) % 12;

        while (g > rasimana[currentRasiIdx]) {
            g -= rasimana[currentRasiIdx];
            currentRasiIdx = (currentRasiIdx + 1) % 12;
        }

        double degreeInLagna = (g / rasimana[currentRasiIdx]) * 30.0;
        return ((currentRasiIdx * 30.0) + degreeInLagna) % 360.0;
    }

    private double calculateAyanamsaOffset(double julianDayUT, String ayanamsaStr) {
        if ("NONE".equalsIgnoreCase(ayanamsaStr) || "PLAIN".equalsIgnoreCase(ayanamsaStr)) {
            return 0.0; // Standard Siddhantic Zero Epoch
        }

        synchronized (swissEph) {
            AyanamsaType selectedAyanamsa = AyanamsaType.SURYA_SIDDHANTA;

            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
            double lahiriVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            selectedAyanamsa.applyTo(swissEph, PanchangamType.SURYA_SIDDHANTA);
            double targetAyanamsaVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            return lahiriVal - targetAyanamsaVal;
        }
    }

    /**
     * Converts birth timestamp and pure engine sunrise into Udayadi Ghatikas.
     * 1 Hour = 2.5 Ghatikas (1 Ghatika / Nazhi = 24 minutes).
     */
    public double calculateUdayadiGhatikas(java.time.LocalTime birthTimeLmt, double sunriseLmtHours) {
        double birthTimeHours = birthTimeLmt.getHour() +
                (birthTimeLmt.getMinute() / 60.0) +
                (birthTimeLmt.getSecond() / 3600.0);

        double diffHours = birthTimeHours - sunriseLmtHours;

        // Handle birth before sunrise (previous day's Ghatikas)
        if (diffHours < 0) {
            diffHours += 24.0;
        }

        // 1 Hour = 2.5 Ghatikas (60 Ghatikas in 24 Hours)
        return diffHours * 2.5;
    }

    /**
     * Computes Pure Surya Siddhanta Sunrise Local Mean Time.
     * Incorporates Surya Siddhanta Chara-Sutra and Ujjain Prime Meridian Desantara.
     */
    private double calculateSuryaSiddhantaSunriseLocalTime(double sunLongitude, double latitude, double longitude) {
        // 1. Classical Surya Siddhanta Obliquity (24.0 degrees)
        double epsilonRad = Math.toRadians(24.0);
        double sunLongRad = Math.toRadians(sunLongitude);
        double latRad = Math.toRadians(latitude);

        // 2. Sun's Declination (Krantya)
        double sinDeclination = Math.sin(epsilonRad) * Math.sin(sunLongRad);
        double declinationRad = Math.asin(sinDeclination);

        // 3. Ascensional Difference (Chara)
        double sinChara = Math.tan(latRad) * Math.tan(declinationRad);
        sinChara = Math.max(-1.0, Math.min(1.0, sinChara));
        double charaRad = Math.asin(sinChara);
        double charaHours = Math.toDegrees(charaRad) / 15.0;

        // 4. Ujjain Prime Meridian Desantara Time Correction
        // Ancient Ujjain Longitude = 75.768°E
        double ujjainLongitude = 75.768;
        double desantaraHours = ((longitude - ujjainLongitude) * 4.0) / 60.0;

        // 5. Surya Siddhanta Sunrise LMT = 06:00 - Chara + Desantara Correction
        return (6.0 - charaHours) + desantaraHours;
    }

    private double getSuryaSiddhantaSpeed(String planetName) {
        return switch (planetName) {
            case "Sun" -> 0.9856;
            case "Moon" -> 13.1764;
            case "Mars" -> 0.5240;
            case "Jupiter" -> 0.0831;
            case "Saturn" -> 0.0335;
            case "Rahu", "Ketu" -> -0.0530;
            default -> 1.0;
        };
    }

    @Override
    public PanchangamType getType() {
        return PanchangamType.SURYA_SIDDHANTA;
    }
}