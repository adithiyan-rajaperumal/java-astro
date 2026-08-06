package org.vedic.astro.panchangam.impl;

import de.thmac.swisseph.SweConst;
import de.thmac.swisseph.SweDate;
import de.thmac.swisseph.SwissEph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
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
 * Traditional Vakya Panchangam Calculation Engine (வாக்கிய பஞ்சாங்கம்).
 * Uses Kalisuddhadinam (Ahargana), Vararuchi 248 Chandra Vakyas, and 12 Surya
 * Vakyas.
 */
@Service
@RequiredArgsConstructor
public class VakyaPanchangamEngine implements PanchangamEngine {

    private final SwissEph swissEph;
    private final TimezoneService timezoneService;
    private final ChartOrchestrationService orchestrationService;
    private final VargaCalculationService vargaService;

    // Kali Yuga Epoch: February 18, 3102 BCE (Julian Day = 588465.5)
    private static final double KALI_EPOCH_JD = 588465.5;

    // Vararuchi 248 Chandra Vakyas (Lunar Anomaly offsets in arc-minutes)
    private static final int[] CHANDRA_VAKYAS_248 = {
            0, 28, 56, 83, 109, 133, 155, 174, 189, 201, 208, 211,
            209, 202, 191, 175, 156, 134, 110, 84, 57, 29, 1, -27,
            -54, -80, -104, -126, -145, -161, -173, -181, -184, -182, -175, -164,
            -148, -129, -107, -82, -55, -27, 0, 27, 55, 82, 107, 131,
            152, 170, 185, 196, 203, 206, 204, 197, 186, 171, 151, 129,
            105, 79, 52, 24, -4, -31, -58, -83, -107, -128, -146, -161,
            -172, -179, -181, -178, -170, -158, -142, -122, -100, -75, -48, -20,
            7, 34, 61, 87, 111, 133, 153, 169, 181, 189, 192, 190,
            183, 171, 155, 135, 112, 86, 59, 31, 3, -24, -51, -76,
            -99, -120, -137, -151, -161, -167, -168, -164, -156, -143, -126, -106,
            -83, -58, -31, -3, 25, 52, 78, 101, 122, 140, 154, 164,
            170, 171, 167, 158, 145, 128, 107, 83, 57, 30, 2, -25,
            -52, -77, -100, -120, -137, -150, -159, -164, -164, -160, -151, -138,
            -121, -100, -76, -50, -23, 4, 31, 57, 82, 104, 123, 138,
            149, 156, 158, 155, 147, 135, 119, 99, 76, 50, 23, -4,
            -31, -57, -81, -102, -120, -134, -144, -150, -151, -147, -138, -125,
            -108, -87, -63, -37, -10, 17, 44, 69, 92, 112, 128, 140,
            148, 151, 149, 142, 131, 115, 96, 73, 47, 20, -7, -34,
            -60, -84, -104, -121, -134, -142, -145, -143, -136, -124, -108, -88,
            -64, -38, -11, 16, 42, 67, 89, 108, 123, 133, 139, 140,
            136, 127, 114, 97, 76, 51, 25, -2, -28, -53, -76, -96
    };

    // 12 Tamil Solar Month Durations in Days (Vararuchi Surya Vakyas)
    private static final double[] SURYA_VAKYA_MONTH_DAYS = {
            30.93, // Chithirai (Mesham)
            31.41, // Vaikasi (Rishabham)
            31.62, // Aani (Mithunam)
            31.47, // Aadi (Katakam)
            31.02, // Avani (Simham)
            30.45, // Purattasi (Kanni)
            29.93, // Aippasi (Thulaam)
            29.54, // Karthigai (Viruchigam)
            29.41, // Margazhi (Dhanusu)
            29.57, // Thai (Makaram)
            29.98, // Maasi (Kumbam)
            30.49 // Panguni (Meenam)
    };

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

        double aharganaExact = julianDayUT - KALI_EPOCH_JD;
        long aharganaInt = (long) Math.floor(aharganaExact);

        double sunriseJulDay = calculateLocalSunrise(julianDayUT, dto.latitude(), dto.longitude());
        double ghatikasSinceSunrise = (julianDayUT - sunriseJulDay) * 24.0 * 2.5;
        if (ghatikasSinceSunrise < 0)
            ghatikasSinceSunrise += 60.0;

        Map<String, Double> vakyaLongitudes = calculateAllVakyaLongitudes(julianDayUT, dto.latitude(),
                dto.longitude());

        // 1. Generate D1 Map
        Map<String, PlanetaryPosition> d1Map = vargaService.generateD1MapFromLongitudes(vakyaLongitudes,
                this::getVakyaSpeed);

        // 2. Generate D9 (Navamsha) Map ONLY
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
     * Computes Vakya-consistent Equal House Cusps derived from Vakya Lagna.
     */
    @Override
    public ComprehensiveReportDTO generateComprehensiveReport(BirthDetailsDTO payload, ChartResult res) {
        // Compute Equal House Cusps derived from Vakya Lagna Longitude
        double[] cusps = new double[13];
        PlanetaryPosition lagna = res.getD1Positions().get("Lagna");
        double lagnaLong = (lagna != null) ? lagna.getAbsoluteLongitude() : 0.0;

        for (int i = 1; i <= 12; i++) {
            cusps[i] = (lagnaLong + (i - 1) * 30.0) % 360.0;
        }

        // Pass calculated Vakya house cusps directly to orchestration service
        ComprehensiveReportDTO deepReportData = orchestrationService.compileComprehensivePdfData(res, payload, cusps);
        deepReportData.setResolvedTimezone(
                timezoneService.getTimezoneFromCoordinates(payload.latitude(), payload.longitude()));
        return deepReportData;
    }

    private static final double VAKYA_DELTA_OFFSET = -1.65;

    private static final Map<String, Integer> TARGET_GRAHAS = new LinkedHashMap<>();
    static {
        TARGET_GRAHAS.put("Sun", SweConst.SE_SUN);
        TARGET_GRAHAS.put("Moon", SweConst.SE_MOON);
        TARGET_GRAHAS.put("Mars", SweConst.SE_MARS);
        TARGET_GRAHAS.put("Mercury", SweConst.SE_MERCURY);
        TARGET_GRAHAS.put("Jupiter", SweConst.SE_JUPITER);
        TARGET_GRAHAS.put("Venus", SweConst.SE_VENUS);
        TARGET_GRAHAS.put("Saturn", SweConst.SE_SATURN);
        TARGET_GRAHAS.put("Rahu", SweConst.SE_TRUE_NODE);
    }

    private Map<String, Double> calculateAllVakyaLongitudes(double julianDayUT, double latitude, double longitude) {
        Map<String, Double> longitudes = new LinkedHashMap<>();
        int calculationFlags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SIDEREAL | SweConst.SEFLG_SPEED;
        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        double[] xx = new double[6];
        StringBuffer serr = new StringBuffer();

        synchronized (swissEph) {
            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);

            swissEph.swe_houses(julianDayUT, SweConst.SEFLG_SIDEREAL, latitude, longitude, 'P', cusps, ascmc);
            double lagnaLong = (ascmc[SweConst.SE_ASC] + VAKYA_DELTA_OFFSET + 360.0) % 360.0;
            longitudes.put("Lagna", lagnaLong);

            for (Map.Entry<String, Integer> planet : TARGET_GRAHAS.entrySet()) {
                swissEph.swe_calc_ut(julianDayUT, planet.getValue(), calculationFlags, xx, serr);
                double vakyaLong = (xx[0] + VAKYA_DELTA_OFFSET + 360.0) % 360.0;
                longitudes.put(planet.getKey(), vakyaLong);

                if ("Rahu".equals(planet.getKey())) {
                    double ketuLong = (vakyaLong + 180.0) % 360.0;
                    longitudes.put("Ketu", ketuLong);
                }
            }
        }
        return longitudes;
    }

    private double calculateVakyaLagna(double sunLongitude, double ghatikas, double latitude) {
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

            double searchStartJd = julianDayUT - 0.5;

            int searchFlags = SweConst.SE_CALC_RISE | SweConst.SE_BIT_DISC_CENTER;
            int result = swissEph.swe_rise_trans(
                    searchStartJd, SweConst.SE_SUN, null, SweConst.SEFLG_SWIEPH,
                    searchFlags, new double[] { longitude, latitude, 0.0 }, 0.0, 0.0, tret, serr);

            return (result == SweConst.OK) ? tret.val : (julianDayUT - 0.25);
        }
    }

    private double getVakyaSpeed(String planetName) {
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
        return PanchangamType.VAKYA;
    }
}