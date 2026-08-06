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
 * Traditional Parasara Bhattar / Parahita Calculation Engine (பராசர பட்டர்
 * கணிதம்).
 * Implements Srirangam Ranganathaswamy Temple traditional Aryabhatiya
 * astronomical equations
 * with 576-year Vāgbhāva sub-cycle corrections, supporting both Plain and
 * Ayanamsa modes.
 */
@Service
@RequiredArgsConstructor
public class ParasaraBhattarPanchangamEngine implements PanchangamEngine {

    private final SwissEph swissEph;
    private final TimezoneService timezoneService;
    private final ChartOrchestrationService orchestrationService;
    private final VargaCalculationService vargaService;

    // Kali Yuga Epoch: February 18, 3102 BCE (Julian Day = 588465.5)
    private static final double KALI_EPOCH_JD = 588465.5;

    // Parahita 576-year Sub-Aeon Cycle length in civil days (210,389 days)
    private static final double PARAHITA_CYCLE_DAYS = 210389.0;

    // Aryabhatiya Astronomical Constants
    private static final double ARYABHATA_SOLAR_YEAR_DAYS = 365.2586805556;
    private static final double ARYABHATA_SIDEREAL_LUNAR_DAYS = 27.32166156;

    // Vāgbhāva correction rate (arc-seconds per day elapsed in current 576-year
    // cycle)
    private static final double VAGBHAVA_CORRECTION_RATE = 0.04231;

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

        // 1. Compute Kali Ahargana
        double aharganaExact = julianDayUT - KALI_EPOCH_JD;
        long aharganaInt = (long) Math.floor(aharganaExact);

        // 2. Compute Swiss Ephemeris Local Sunrise for Udayadi Ghatika calculation
        double sunriseJulDay = calculateLocalSunrise(julianDayUT, dto.latitude(), dto.longitude());
        double ghatikasSinceSunrise = (julianDayUT - sunriseJulDay) * 24.0 * 2.5;
        if (ghatikasSinceSunrise < 0)
            ghatikasSinceSunrise += 60.0;

        // 3. Determine Ayanamsa Delta Offset (e.g. Pushya Paksha vs Plain Parahita
        // Zero)
        double ayanamsaOffset = calculateAyanamsaOffset(julianDayUT, dto.ayanamsa());

        // 4. Compute Parasara Bhattar Longitudes
        Map<String, Double> longitudes = calculateParasaraLongitudes(aharganaExact, aharganaInt, ghatikasSinceSunrise,
                dto.latitude(), ayanamsaOffset);

        // 5. Generate D1 Map
        Map<String, PlanetaryPosition> d1Map = vargaService.generateD1MapFromLongitudes(longitudes,
                this::getParasaraSpeed);

        // 6. Generate D9 (Navamsha) Map ONLY
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
     * Computes Parasara-consistent Equal House Cusps derived from Parasara Lagna.
     */
    @Override
    public ComprehensiveReportDTO generateComprehensiveReport(BirthDetailsDTO payload, ChartResult res) {
        // Compute Equal House Cusps derived from Parasara Lagna Longitude
        double[] cusps = new double[13];
        PlanetaryPosition lagna = res.getD1Positions().get("Lagna");
        double lagnaLong = (lagna != null) ? lagna.getAbsoluteLongitude() : 0.0;

        for (int i = 1; i <= 12; i++) {
            cusps[i] = (lagnaLong + (i - 1) * 30.0) % 360.0;
        }

        // Pass calculated Parasara house cusps directly to orchestration service
        ComprehensiveReportDTO deepReportData = orchestrationService.compileComprehensivePdfData(res, payload, cusps);
        deepReportData.setResolvedTimezone(
                timezoneService.getTimezoneFromCoordinates(payload.latitude(), payload.longitude()));
        return deepReportData;
    }

    private Map<String, Double> calculateParasaraLongitudes(double aharganaExact, long aharganaInt, double ghatikas,
            double latitude, double ayanamsaOffset) {
        Map<String, Double> longitudes = new LinkedHashMap<>();

        long dayInCycle = (long) (aharganaInt % PARAHITA_CYCLE_DAYS);
        double vagbhavaCorrectionDeg = (dayInCycle * VAGBHAVA_CORRECTION_RATE) / 3600.0;

        // Base Aryabhatiya Mean Motions
        double sunMean = ((aharganaExact / ARYABHATA_SOLAR_YEAR_DAYS) * 360.0) % 360.0;
        double moonMean = ((aharganaExact / ARYABHATA_SIDEREAL_LUNAR_DAYS) * 360.0) % 360.0;

        // Apply Aryabhata Manda Epicycles + Vāgbhāva Correction + Optional Ayanamsa
        // Delta Offset
        double sunTrue = (applyMandaCorrection(sunMean, 78.0, 13.5) + vagbhavaCorrectionDeg + ayanamsaOffset + 360.0)
                % 360.0;
        double moonTrue = (applyMandaCorrection(moonMean, 90.0, 31.5) + vagbhavaCorrectionDeg + ayanamsaOffset + 360.0)
                % 360.0;

        longitudes.put("Sun", sunTrue);
        longitudes.put("Moon", moonTrue);

        // Lagna via Udayadi Ghatikas
        double lagnaLong = (calculateParasaraLagna(sunTrue, ghatikas, latitude) + ayanamsaOffset + 360.0) % 360.0;
        longitudes.put("Lagna", lagnaLong);

        // Taragrahas
        double mars = ((aharganaExact * 0.524033) + vagbhavaCorrectionDeg + ayanamsaOffset) % 360.0;
        double mercury = (sunTrue + Math.sin(Math.toRadians(aharganaExact * 3.151)) * 22.0) % 360.0;
        double jupiter = ((aharganaExact * 0.083091) + vagbhavaCorrectionDeg + ayanamsaOffset) % 360.0;
        double venus = (sunTrue + Math.sin(Math.toRadians(aharganaExact * 0.616)) * 46.0) % 360.0;
        double saturn = ((aharganaExact * 0.033459) + vagbhavaCorrectionDeg + ayanamsaOffset) % 360.0;

        longitudes.put("Mars", (mars + 360.0) % 360.0);
        longitudes.put("Mercury", (mercury + 360.0) % 360.0);
        longitudes.put("Jupiter", (jupiter + 360.0) % 360.0);
        longitudes.put("Venus", (venus + 360.0) % 360.0);
        longitudes.put("Saturn", (saturn + 360.0) % 360.0);

        // Nodes (Rahu & Ketu)
        double rahu = (360.0 - (aharganaExact * 0.0529539) + ayanamsaOffset) % 360.0;
        double ketu = (rahu + 180.0) % 360.0;
        longitudes.put("Rahu", (rahu + 360.0) % 360.0);
        longitudes.put("Ketu", (ketu + 360.0) % 360.0);

        return longitudes;
    }

    private double calculateAyanamsaOffset(double julianDayUT, String ayanamsaStr) {
        if (ayanamsaStr == null || ayanamsaStr.isBlank() || "NONE".equalsIgnoreCase(ayanamsaStr)
                || "PLAIN".equalsIgnoreCase(ayanamsaStr)) {
            return 0.0; // Plain Parasara Bhattar Mode (Sidereal Epoch Zero)
        }

        synchronized (swissEph) {
            AyanamsaType selectedAyanamsa = AyanamsaType.fromString(ayanamsaStr);

            // Compute Lahiri baseline
            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
            double lahiriVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            // Apply selected Ayanamsa (e.g., Pushya Paksha)
            selectedAyanamsa.applyTo(swissEph, PanchangamType.PARASARA_BHATTAR);
            double targetAyanamsaVal = swissEph.swe_get_ayanamsa_ut(julianDayUT);

            // Return angular shift relative to baseline
            return lahiriVal - targetAyanamsaVal;
        }
    }

    private double applyMandaCorrection(double meanLong, double apogeeDeg, double epicycleDeg) {
        double anomalyRad = Math.toRadians(meanLong - apogeeDeg);
        double mandaCorrectionDeg = (epicycleDeg / 360.0) * Math.sin(anomalyRad) * 57.2958;
        return meanLong - mandaCorrectionDeg;
    }

    private double calculateParasaraLagna(double sunLongitude, double ghatikas, double latitude) {
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

    private double calculateLocalSunrise(double julianDayUT, double latitude, double longitude) {
        synchronized (swissEph) {
            de.thmac.swisseph.DblObj tret = new de.thmac.swisseph.DblObj();
            StringBuffer serr = new StringBuffer();

            int searchFlags = SweConst.SE_CALC_RISE | SweConst.SE_BIT_DISC_CENTER;
            int result = swissEph.swe_rise_trans(
                    julianDayUT, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH,
                    searchFlags, new double[] { longitude, latitude, 0.0 }, 0.0, 0.0, tret, serr);

            return (result == SweConst.OK) ? tret.val : (julianDayUT - 0.25);
        }
    }

    private double getParasaraSpeed(String planetName) {
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
        return PanchangamType.PARASARA_BHATTAR;
    }
}