package org.vedic.astro.panchangam.impl;

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
import org.vedic.astro.util.SamvatsaraTable;
import org.vedic.astro.util.TamilCalendarUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pure Mathematical Vakya Panchangam Engine (வாக்கிய பஞ்சாங்கம்).
 * Integrates TamilCalendarUtils.getTamilDateFromAhargana & calculateInnerEpicycles.
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

    @Override
    public ChartResult calculate(BirthDetailsDTO dto) {
        LocalDateTime localTime = LocalDateTime.of(dto.year(), dto.month(), dto.day(), dto.hour(), dto.minute(), dto.second());
        String resolvedZoneId = timezoneService.getTimezoneFromCoordinates(dto.latitude(), dto.longitude());
        ZoneId zoneId = ZoneId.of(resolvedZoneId);

        ZonedDateTime zonedBirthTime = ZonedDateTime.of(localTime, zoneId);
        LocalDateTime utcTime = zonedBirthTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        double hourFraction = utcTime.getHour() + (utcTime.getMinute() / 60.0) + (utcTime.getSecond() / 3600.0);
        SweDate sweDate = new SweDate(utcTime.getYear(), utcTime.getMonthValue(), utcTime.getDayOfMonth(), hourFraction);
        double julianDayUT = sweDate.getJulDay();

        double aharganaExact = julianDayUT - KALI_EPOCH_JD;
        long aharganaInt = (long) Math.floor(aharganaExact);

        // 1. Resolve Samvatsara Baseline for Year
        SamvatsaraTable.SamvatsaraBase base = SamvatsaraTable.getBaseForYear(dto.year(), dto.month());

        // 2. Use TamilCalendarUtils utility to get Tamil Date from Ahargana
        TamilCalendarUtils.TamilDate tDate = TamilCalendarUtils.getTamilDateFromAhargana(aharganaExact, base.chithirai1Ahargana());

        // 3. Calculate Sun Longitude
        double sunLong = calculateVakyaSunLongitude(tDate);

        // 4. Calculate Sunrise & Ghatikas
        double sunriseIstHours = calculateVakyaSunriseIstHours(sunLong, dto.latitude(), dto.longitude());
        double birthHourFraction = dto.hour() + (dto.minute() / 60.0) + (dto.second() / 3600.0);
        double ghatikasSinceSunrise = calculateUdayadiGhatikas(birthHourFraction, sunriseIstHours);

        // 5. Calculate All Longitudes
        Map<String, Double> vakyaLongitudes = calculateAllVakyaLongitudes(
                aharganaInt, tDate, sunLong, ghatikasSinceSunrise, dto.latitude(), base
        );

        // 6. Build D1 and D9 Charts
        Map<String, PlanetaryPosition> d1Map = vargaService.generateD1MapFromLongitudes(vakyaLongitudes, this::getVakyaSpeed);
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

    @Override
    public ComprehensiveReportDTO generateComprehensiveReport(BirthDetailsDTO payload, ChartResult res) {
        double[] cusps = new double[13];
        PlanetaryPosition lagna = res.getD1Positions().get("Lagna");
        double lagnaLong = (lagna != null) ? lagna.getAbsoluteLongitude() : 0.0;

        for (int i = 1; i <= 12; i++) {
            cusps[i] = (lagnaLong + (i - 1) * 30.0) % 360.0;
        }

        ComprehensiveReportDTO deepReportData = orchestrationService.compileComprehensivePdfData(res, payload, cusps);
        deepReportData.setResolvedTimezone(timezoneService.getTimezoneFromCoordinates(payload.latitude(), payload.longitude()));
        return deepReportData;
    }

    private Map<String, Double> calculateAllVakyaLongitudes(
            long aharganaInt, TamilCalendarUtils.TamilDate tDate,
            double sunLong, double ghatikas, double latitude,
            SamvatsaraTable.SamvatsaraBase base) {

        Map<String, Double> longitudes = new LinkedHashMap<>();

        // 1. Sun
        longitudes.put("Sun", sunLong);

        // 2. Lagna
        double lagnaLong = calculateVakyaLagna(sunLong, ghatikas, latitude);
        longitudes.put("Lagna", lagnaLong);

        double daysFromChithirai1 = tDate.totalDaysFromChithirai1();

        // 3. Moon (Chandra) via Vararuchi 248 Vakyas + Samvatsara Base
        int vakyaIndex = (int) Math.floorMod(aharganaInt, 248);
        double anomalyOffsetDeg = CHANDRA_VAKYAS_248[vakyaIndex] / 60.0;
        double meanMoon = TamilCalendarUtils.normalize(base.moonMeanBase() + (daysFromChithirai1 * 13.1763965));
        double moonLong = TamilCalendarUtils.normalize(meanMoon + anomalyOffsetDeg);
        longitudes.put("Moon", moonLong);

        // 4. Outer Planets
        double mars = TamilCalendarUtils.normalize(base.marsBase() + (daysFromChithirai1 * 0.524033));
        double jupiter = TamilCalendarUtils.normalize(base.jupiterBase() + (daysFromChithirai1 * 0.083091));
        double saturn = TamilCalendarUtils.normalize(base.saturnBase() + (daysFromChithirai1 * 0.033459));

        // 5. Inner Planets via TamilCalendarUtils.calculateInnerEpicycles(...)
        TamilCalendarUtils.EpicycleState epicycles = TamilCalendarUtils.calculateInnerEpicycles(sunLong, daysFromChithirai1);

        longitudes.put("Mars", mars);
        longitudes.put("Mercury", epicycles.mercuryLongitude());
        longitudes.put("Jupiter", jupiter);
        longitudes.put("Venus", epicycles.venusLongitude());
        longitudes.put("Saturn", saturn);

        // 6. Nodes
        double rahu = TamilCalendarUtils.normalize(base.rahuBase() - (daysFromChithirai1 * 0.0529539));
        double ketu = TamilCalendarUtils.normalize(rahu + 180.0);

        longitudes.put("Rahu", rahu);
        longitudes.put("Ketu", ketu);

        return longitudes;
    }

    private double calculateVakyaSunLongitude(TamilCalendarUtils.TamilDate tDate) {
        int monthIdx = tDate.monthIndex();
        double dayOffset = tDate.dayFractionInMonth();
        double monthDuration = TamilCalendarUtils.SURYA_VAKYA_MONTH_DAYS[monthIdx];

        double baseRasiDegree = monthIdx * 30.0;
        double meanSunInSign = (dayOffset / monthDuration) * 30.0;
        double meanSun = baseRasiDegree + meanSunInSign;

        double mandaCorrection = 2.14 * Math.sin(Math.toRadians(meanSun - 78.0));
        return TamilCalendarUtils.normalize(meanSun - mandaCorrection);
    }

    private double calculateVakyaLagna(double sunLongitude, double ghatikas, double latitude) {
        double[] rasimana = { 4.2, 4.8, 5.2, 5.4, 5.3, 5.1, 5.1, 5.3, 5.4, 5.2, 4.8, 4.2 };

        int currentRasiIdx = (int) (sunLongitude / 30.0);
        double remainingDegInSunRasi = 30.0 - (sunLongitude % 30.0);
        double ghatikasToClearSunRasi = (remainingDegInSunRasi / 30.0) * rasimana[currentRasiIdx];

        double g = ghatikas;
        if (g <= ghatikasToClearSunRasi) {
            return TamilCalendarUtils.normalize(sunLongitude + (g / rasimana[currentRasiIdx]) * 30.0);
        }

        g -= ghatikasToClearSunRasi;
        currentRasiIdx = (currentRasiIdx + 1) % 12;

        while (g > rasimana[currentRasiIdx]) {
            g -= rasimana[currentRasiIdx];
            currentRasiIdx = (currentRasiIdx + 1) % 12;
        }

        double degreeInLagna = (g / rasimana[currentRasiIdx]) * 30.0;
        return TamilCalendarUtils.normalize((currentRasiIdx * 30.0) + degreeInLagna);
    }

    public double calculateUdayadiGhatikas(double birthIstHours, double sunriseIstHours) {
        double diffHours = birthIstHours - sunriseIstHours;
        if (diffHours < 0) {
            diffHours += 24.0;
        }
        return diffHours * 2.5;
    }

    private double calculateVakyaSunriseIstHours(double sunLongitude, double latitude, double longitude) {
        double epsilonRad = Math.toRadians(24.0);
        double sunLongRad = Math.toRadians(sunLongitude);
        double latRad = Math.toRadians(latitude);

        double sinDeclination = Math.sin(epsilonRad) * Math.sin(sunLongRad);
        double declinationRad = Math.asin(sinDeclination);

        double tanLat = Math.tan(latRad);
        double tanDec = Math.tan(declinationRad);
        double sinChara = tanLat * tanDec;

        sinChara = Math.max(-1.0, Math.min(1.0, sinChara));
        double charaRad = Math.asin(sinChara);
        double charaHours = Math.toDegrees(charaRad) / 15.0;

        double sunriseLmtHours = 6.0 - charaHours;
        double istMeridian = 82.5;
        double longitudeCorrectionHours = ((istMeridian - longitude) * 4.0) / 60.0;

        return sunriseLmtHours + longitudeCorrectionHours;
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