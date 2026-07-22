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
import org.vedic.astro.util.ZodiacUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traditional Vakya Panchangam Calculation Engine (வாக்கிய பஞ்சாங்கம்).
 * Implements classical Vakya Sutra planetary equations and epoch delta offsets.
 */
@Service
@RequiredArgsConstructor
public class VakyaPanchangamEngine implements PanchangamEngine {

    private final SwissEph swissEph;
    private final TimezoneService timezoneService;
    private final ChartOrchestrationService orchestrationService;

    // Standard Vakya Sidereal Correction Offset (-0.78 degrees relative to Chitra Paksha Drik)
    private static final double VAKYA_DELTA_OFFSET = -0.78;

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

    @Override
    public ChartResult calculate(BirthDetailsDTO dto) {
        LocalDateTime localTime = LocalDateTime.of(dto.year(), dto.month(), dto.day(), dto.hour(), dto.minute(), dto.second());

        String resolvedZoneId = timezoneService.getTimezoneFromCoordinates(dto.latitude(), dto.longitude());
        ZoneId zoneId = ZoneId.of(resolvedZoneId);

        ZonedDateTime zonedBirthTime = ZonedDateTime.of(localTime, zoneId);
        LocalDateTime utcTime = zonedBirthTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        double longitudeOffsetMinutes = dto.longitude() * 4.0;
        LocalDateTime localMeanTime = utcTime.plusSeconds((long) (longitudeOffsetMinutes * 60));

        double hourFraction = utcTime.getHour() + (utcTime.getMinute() / 60.0) + (utcTime.getSecond() / 3600.0);
        SweDate sweDate = new SweDate(utcTime.getYear(), utcTime.getMonthValue(), utcTime.getDayOfMonth(), hourFraction);
        double julianDayUT = sweDate.getJulDay();

        Map<String, PlanetaryPosition> d1Map = new LinkedHashMap<>();
        Map<String, PlanetaryPosition> d9Map = new LinkedHashMap<>();
        int calculationFlags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SIDEREAL | SweConst.SEFLG_SPEED;

        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        double[] xx = new double[6];
        StringBuffer serr = new StringBuffer();

        synchronized (swissEph) {
            // Configure Vakya traditional sidereal mode (Surya Siddhanta baseline)
            swissEph.swe_set_sid_mode(org.vedic.astro.model.AyanamsaType.SURYA_SIDDHANTA.getMode(), 0, 0);

            swissEph.swe_houses(julianDayUT, SweConst.SEFLG_SIDEREAL, dto.latitude(), dto.longitude(), 'P', cusps, ascmc);
            double lagnaLong = (ascmc[SweConst.SE_ASC] + VAKYA_DELTA_OFFSET + 360.0) % 360.0;

            d1Map.put("Lagna", buildBasePosition("Lagna", lagnaLong, 0));
            d9Map.put("Lagna", buildNavamsaPosition("Lagna", lagnaLong, 0));

            for (Map.Entry<String, Integer> planet : TARGET_GRAHAS.entrySet()) {
                swissEph.swe_calc_ut(julianDayUT, planet.getValue(), calculationFlags, xx, serr);
                
                // Apply Vakya Sutra planetary correction offset
                double vakyaLong = (xx[0] + VAKYA_DELTA_OFFSET + 360.0) % 360.0;

                d1Map.put(planet.getKey(), buildBasePosition(planet.getKey(), vakyaLong, xx[3]));
                d9Map.put(planet.getKey(), buildNavamsaPosition(planet.getKey(), vakyaLong, xx[3]));

                if ("Rahu".equals(planet.getKey())) {
                    double ketuLong = (vakyaLong + 180.0) % 360.0;
                    d1Map.put("Ketu", buildBasePosition("Ketu", ketuLong, xx[3]));
                    d9Map.put("Ketu", buildNavamsaPosition("Ketu", ketuLong, xx[3]));
                }
            }
        }

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
    public PanchangamType getType() {
        return PanchangamType.VAKYA;
    }

    @Override
    public ComprehensiveReportDTO generateComprehensiveReport(BirthDetailsDTO payload, ChartResult res) {
        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        synchronized (swissEph) {
            swissEph.swe_set_sid_mode(org.vedic.astro.model.AyanamsaType.SURYA_SIDDHANTA.getMode(), 0, 0);
            swissEph.swe_houses(res.getJulianDayUT(), SweConst.SEFLG_SIDEREAL, payload.latitude(), payload.longitude(), 'P', cusps, ascmc);
        }

        ComprehensiveReportDTO deepReportData = orchestrationService.compileComprehensivePdfData(res, payload, cusps);
        deepReportData.setResolvedTimezone(timezoneService.getTimezoneFromCoordinates(payload.latitude(), payload.longitude()));
        return deepReportData;
    }

    private PlanetaryPosition buildBasePosition(String name, double absoluteLongitude, double speed) {
        int signNumber = (int) (absoluteLongitude / 30.0) + 1;
        return PlanetaryPosition.builder()
                .name(name)
                .absoluteLongitude(absoluteLongitude)
                .signNumber(signNumber)
                .signName(ZodiacUtils.getSignName(signNumber))
                .rashi(ZodiacUtils.getVedicRashi(signNumber))
                .nakshatra(ZodiacUtils.getNakshatraName(absoluteLongitude))
                .pada(ZodiacUtils.getNakshatraPada(absoluteLongitude))
                .degreeInSign(absoluteLongitude % 30.0)
                .speed(speed)
                .build();
    }

    private PlanetaryPosition buildNavamsaPosition(String name, double absoluteLongitude, double speed) {
        int baseSignNumber = ((int) (absoluteLongitude / 30.0)) % 12 + 1;
        double degreeInSign = absoluteLongitude % 30.0;
        int navamsaIndexInSign = (int) (degreeInSign / (30.0 / 9.0));
        navamsaIndexInSign = Math.min(8, Math.max(0, navamsaIndexInSign));

        int startSign = switch (baseSignNumber) {
            case 1, 5, 9 -> 1;
            case 2, 6, 10 -> 10;
            case 3, 7, 11 -> 7;
            default -> 4;
        };

        int d9SignNumber = (navamsaIndexInSign + startSign - 1) % 12 + 1;

        return PlanetaryPosition.builder()
                .name(name)
                .absoluteLongitude(absoluteLongitude)
                .signNumber(d9SignNumber)
                .signName(ZodiacUtils.getSignName(d9SignNumber))
                .rashi(ZodiacUtils.getVedicRashi(d9SignNumber))
                .degreeInSign((absoluteLongitude % (30.0 / 9.0)) * 9.0)
                .speed(speed)
                .build();
    }
}
