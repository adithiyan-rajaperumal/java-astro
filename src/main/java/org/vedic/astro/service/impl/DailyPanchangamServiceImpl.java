package org.vedic.astro.service.impl;

import de.thmac.swisseph.SweConst;
import de.thmac.swisseph.SweDate;
import de.thmac.swisseph.SwissEph;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.DailyPanchangamDTO.HoraTimeSlotDTO;
import org.vedic.astro.dto.DailyPanchangamDTO.PanchangamElementDTO;
import org.vedic.astro.dto.DailyPanchangamDTO.TimeSlotDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;
import org.vedic.astro.service.DailyPanchangamService;
import org.vedic.astro.service.TimezoneService;
import org.vedic.astro.service.TranslationService;
import org.vedic.astro.util.ZodiacUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DailyPanchangamServiceImpl implements DailyPanchangamService {

    private final SwissEph swissEph;
    private final TimezoneService timezoneService;
    private final TranslationService translationService;

    private static final String[][] GOWRI_DAY_STATES = {
        {"Uthi", "Amirdha", "Rogam", "Laabam", "Dhanam", "Sugam", "Soram", "Visham"}, // Sun
        {"Amirdha", "Visham", "Rogam", "Laabam", "Dhanam", "Sugam", "Soram", "Uthi"}, // Mon
        {"Rogam", "Laabam", "Dhanam", "Sugam", "Soram", "Uthi", "Visham", "Amirdha"}, // Tue
        {"Laabam", "Dhanam", "Sugam", "Soram", "Visham", "Uthi", "Amirdha", "Rogam"}, // Wed
        {"Dhanam", "Sugam", "Soram", "Uthi", "Amirdha", "Visham", "Rogam", "Laabam"}, // Thu
        {"Sugam", "Soram", "Uthi", "Visham", "Amirdha", "Rogam", "Laabam", "Dhanam"}, // Fri
        {"Soram", "Uthi", "Visham", "Amirdha", "Rogam", "Laabam", "Dhanam", "Sugam"}  // Sat
    };

    private static final String[][] GOWRI_NIGHT_STATES = {
        {"Dhanam", "Sugam", "Soram", "Visham", "Uthi", "Amirdha", "Rogam", "Laabam"}, // Sun
        {"Sugam", "Soram", "Uthi", "Amirdha", "Visham", "Rogam", "Laabam", "Dhanam"}, // Mon
        {"Soram", "Uthi", "Visham", "Amirdha", "Rogam", "Laabam", "Dhanam", "Sugam"}, // Tue
        {"Uthi", "Amirdha", "Rogam", "Laabam", "Dhanam", "Sugam", "Soram", "Visham"}, // Wed
        {"Amirdha", "Visham", "Rogam", "Laabam", "Dhanam", "Sugam", "Soram", "Uthi"}, // Thu
        {"Rogam", "Laabam", "Dhanam", "Sugam", "Soram", "Uthi", "Visham", "Amirdha"}, // Fri
        {"Laabam", "Dhanam", "Sugam", "Soram", "Uthi", "Visham", "Amirdha", "Rogam"}  // Sat
    };

    private static final String[] HORA_PLANETS = {
        "SUN", "VENUS", "MERCURY", "MOON", "SATURN", "JUPITER", "MARS"
    };

    @Override
    public DailyPanchangamDTO calculateDailyPanchangam(PanchangamRequestDTO request) {
        // Set dynamic locale context
        if (request.language() != null && !request.language().isBlank()) {
            LocaleContextHolder.setLocale(Locale.forLanguageTag(request.language()));
        }

        LocalDate date = LocalDate.parse(request.date());
        String zoneIdStr = timezoneService.getTimezoneFromCoordinates(request.latitude(), request.longitude());
        ZoneId zoneId = ZoneId.of(zoneIdStr);

        // Convert local midnight to Julian Day UT
        ZonedDateTime localMidnight = date.atStartOfDay(zoneId);
        ZonedDateTime utcMidnight = localMidnight.withZoneSameInstant(ZoneOffset.UTC);
        double hourFraction = utcMidnight.getHour() + (utcMidnight.getMinute() / 60.0) + (utcMidnight.getSecond() / 3600.0);
        SweDate sweDate = new SweDate(utcMidnight.getYear(), utcMidnight.getMonthValue(), utcMidnight.getDayOfMonth(), hourFraction);
        double jdMidnight = sweDate.getJulDay();

        // Calculate Sunrise and Sunset
        double[] geopos = {request.longitude(), request.latitude(), 0};
        de.thmac.swisseph.DblObj tretRise = new de.thmac.swisseph.DblObj();
        de.thmac.swisseph.DblObj tretSet = new de.thmac.swisseph.DblObj();
        StringBuffer serr = new StringBuffer();

        synchronized (swissEph) {
            String ayanamsa = request.ayanamsa() != null ? request.ayanamsa() : "LAHIRI";
            org.vedic.astro.model.AyanamsaType ayanamsaType = org.vedic.astro.model.AyanamsaType.fromString(ayanamsa);
            swissEph.swe_set_sid_mode(ayanamsaType.getMode(), 0, 0);

            // Search for Sunrise starting at midnight
            swissEph.swe_rise_trans(
                jdMidnight,
                SweConst.SE_SUN,
                null,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_RISE,
                geopos,
                1013.25,
                15.0,
                tretRise,
                serr
            );

            // Search for Sunset starting at Sunrise time
            swissEph.swe_rise_trans(
                tretRise.val,
                SweConst.SE_SUN,
                null,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_SET,
                geopos,
                1013.25,
                15.0,
                tretSet,
                serr
            );
        }

        double jdSunrise = tretRise.val;
        double jdSunset = tretSet.val;

        // Calculate Moonrise and Moonset
        de.thmac.swisseph.DblObj tretMoonRise = new de.thmac.swisseph.DblObj();
        de.thmac.swisseph.DblObj tretMoonSet = new de.thmac.swisseph.DblObj();
        synchronized (swissEph) {
            swissEph.swe_rise_trans(
                jdMidnight,
                SweConst.SE_MOON,
                null,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_RISE,
                geopos,
                1013.25,
                15.0,
                tretMoonRise,
                serr
            );
            swissEph.swe_rise_trans(
                jdMidnight,
                SweConst.SE_MOON,
                null,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_SET,
                geopos,
                1013.25,
                15.0,
                tretMoonSet,
                serr
            );
        }

        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);
        ZonedDateTime zdtSunset = jdToZonedDateTime(jdSunset, zoneId);
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String sunriseStr = zdtSunrise.format(timeFormatter);
        String sunsetStr = zdtSunset.format(timeFormatter);
        
        String moonriseStr = tretMoonRise.val > 0 ? jdToZonedDateTime(tretMoonRise.val, zoneId).format(timeFormatter) : "--:--";
        String moonsetStr = tretMoonSet.val > 0 ? jdToZonedDateTime(tretMoonSet.val, zoneId).format(timeFormatter) : "--:--";

        // Fetch planetary positions at Sunrise
        double[] coordinates = getSunMoonLongitude(jdSunrise);
        double sunLong = coordinates[0];
        double moonLong = coordinates[1];

        // Panchangam elements at Sunrise
        double elongation = (moonLong - sunLong + 720.0) % 360.0;
        int thithiIdx = (int) (elongation / 12.0) + 1;
        thithiIdx = Math.min(30, Math.max(1, thithiIdx));

        int nakIdx = (int) (moonLong / (360.0 / 27.0)) + 1;
        nakIdx = Math.min(27, Math.max(1, nakIdx));

        double totalYogaLong = (sunLong + moonLong + 720.0) % 360.0;
        int yogamIdx = (int) (totalYogaLong / (360.0 / 27.0)) + 1;
        yogamIdx = Math.min(27, Math.max(1, yogamIdx));

        int karanamIdx = (int) (elongation / 6.0) + 1;
        karanamIdx = Math.min(60, Math.max(1, karanamIdx));

        // Gowri Nalla Neram & Next Sunrise calculation
        double jdNextSunrise = jdSunrise + 1.0; // approximation or search next day
        synchronized (swissEph) {
            swissEph.swe_rise_trans(
                jdSunset,
                SweConst.SE_SUN,
                null,
                SweConst.SEFLG_SWIEPH,
                SweConst.SE_CALC_RISE,
                geopos,
                1013.25,
                15.0,
                tretRise,
                serr
            );
            jdNextSunrise = tretRise.val;
        }

        // Format element names (only include next element if current ends before next sunrise)
        PanchangamElementDTO thithiDTO = buildThithiDTO(thithiIdx, jdSunrise, jdNextSunrise, zoneId);
        PanchangamElementDTO nakshatraDTO = buildNakshatraDTO(nakIdx, jdSunrise, jdNextSunrise, zoneId);
        PanchangamElementDTO yogamDTO = buildYogamDTO(yogamIdx, jdSunrise, jdNextSunrise, zoneId);
        PanchangamElementDTO karanamDTO = buildKaranamDTO(karanamIdx, jdSunrise, jdNextSunrise, zoneId);

        int rashiNum = (int) (moonLong / 30.0) + 1;
        String rashiName = translationService.getLocalizedRashi(rashiNum);

        // Nalla Neram
        int dayOfWeekVal = date.getDayOfWeek().getValue(); // Monday=1, ..., Sunday=7
        int dayOfWeek0 = (dayOfWeekVal == 7) ? 0 : dayOfWeekVal; // Sunday=0, Monday=1, ..., Saturday=6
        List<TimeSlotDTO> nallaNeram = calculateNallaNeram(zdtSunrise, dayOfWeek0);

        // Rahu Kalam, Emagandam, Kulikai
        double dayDurationHours = (jdSunset - jdSunrise) * 24.0;
        List<TimeSlotDTO> raghuKalam = calculateKalam(zdtSunrise, dayDurationHours, getRahuPart(dayOfWeek0), translationService.getLabel("panchangam.raghu_kalam"));
        List<TimeSlotDTO> emagandam = calculateKalam(zdtSunrise, dayDurationHours, getYamagandamPart(dayOfWeek0), translationService.getLabel("panchangam.emagandam"));
        List<TimeSlotDTO> kulikai = calculateKalam(zdtSunrise, dayDurationHours, getKulikaiPart(dayOfWeek0), translationService.getLabel("panchangam.kulikai"));

        List<TimeSlotDTO> gowriNallaNeram = calculateGowriNallaNeram(jdSunrise, jdSunset, jdNextSunrise, dayOfWeek0, zoneId);

        // Horais
        List<HoraTimeSlotDTO> horais = calculateHorais(jdSunrise, jdSunset, jdNextSunrise, dayOfWeek0, zoneId);

        // Chandrastamam Nakshatras (Nakshatras of the Janma Rashi for which today's Moon is in the 8th house)
        // If today's Moon is in sign M, Janma Sign J is (M + 5 - 1) % 12 + 1 so that J + 7 = M (8th house count)
        int chandrastamamSign = (rashiNum + 5 - 1) % 12 + 1;
        List<String> chandrastamamNakshatras = getChandrastamamNakshatras(chandrastamamSign);

        // Netram and Jeevan
        double[] coordinatesSun = getSunMoonLongitude(jdSunrise); // reload coordinates just in case
        int sunNakNum = (int) (coordinatesSun[0] / (360.0 / 27.0)) + 1;
        int dDiff = (nakIdx - sunNakNum + 27) % 27;

        int netram = calculateNetram(dDiff);
        double jeevan = calculateJeevan(dDiff);

        // Muhurtham and Vasthu Days
        boolean isAuspiciousThithi = (thithiIdx == 2 || thithiIdx == 3 || thithiIdx == 5 || thithiIdx == 7 || thithiIdx == 10 
                || thithiIdx == 11 || thithiIdx == 13 || thithiIdx == 17 || thithiIdx == 18 || thithiIdx == 20 
                || thithiIdx == 22 || thithiIdx == 25 || thithiIdx == 26 || thithiIdx == 28);
                
        boolean isAuspiciousNakshatra = (nakIdx == 1 || nakIdx == 4 || nakIdx == 5 || nakIdx == 8 || nakIdx == 12 
                || nakIdx == 13 || nakIdx == 14 || nakIdx == 15 || nakIdx == 17 || nakIdx == 21 
                || nakIdx == 22 || nakIdx == 23 || nakIdx == 24 || nakIdx == 26 || nakIdx == 27);

        boolean isMuhurthamDay = (date.getDayOfWeek() != DayOfWeek.TUESDAY && date.getDayOfWeek() != DayOfWeek.SATURDAY)
                && isAuspiciousThithi
                && isAuspiciousNakshatra
                && (netram == 2 && jeevan == 1.0);

        boolean isVasthuDay = (date.getMonthValue() == 1 && date.getDayOfMonth() == 25)
                || (date.getMonthValue() == 4 && date.getDayOfMonth() == 22)
                || (date.getMonthValue() == 6 && date.getDayOfMonth() == 4)
                || (date.getMonthValue() == 7 && date.getDayOfMonth() == 27)
                || (date.getMonthValue() == 8 && date.getDayOfMonth() == 21)
                || (date.getMonthValue() == 9 && date.getDayOfMonth() == 22)
                || (date.getMonthValue() == 10 && date.getDayOfMonth() == 27)
                || (date.getMonthValue() == 11 && date.getDayOfMonth() == 23);

        return new DailyPanchangamDTO(
            date.toString(),
            sunriseStr,
            sunsetStr,
            moonriseStr,
            moonsetStr,
            thithiDTO,
            nakshatraDTO,
            yogamDTO,
            karanamDTO,
            rashiName,
            nallaNeram,
            gowriNallaNeram,
            raghuKalam,
            emagandam,
            kulikai,
            horais,
            chandrastamamNakshatras,
            netram,
            jeevan,
            isMuhurthamDay,
            isVasthuDay
        );
    }

    private List<String> getChandrastamamNakshatras(int sign) {
        List<String> list = new ArrayList<>();
        int[][] signNakMap = {
            {1, 2, 3},    // Sign 1: Ashwini, Bharani, Krittika
            {3, 4, 5},    // Sign 2: Krittika, Rohini, Mrigashira
            {5, 6, 7},    // Sign 3: Mrigashira, Ardra, Punarvasu
            {7, 8, 9},    // Sign 4: Punarvasu, Pushya, Ashlesha
            {10, 11, 12}, // Sign 5: Magha, Purva Phalguni, Uttara Phalguni
            {12, 13, 14}, // Sign 6: Uttara Phalguni, Hasta, Chitra
            {14, 15, 16}, // Sign 7: Chitra, Swati, Vishakha
            {16, 17, 18}, // Sign 8: Vishakha, Anuradha, Jyeshtha
            {19, 20, 21}, // Sign 9: Mula, Purva Ashadha, Uttara Ashadha
            {21, 22, 23}, // Sign 10: Uttara Ashadha, Shravana, Dhanishta
            {23, 24, 25}, // Sign 11: Dhanishta, Shatabhisha, Purva Bhadrapada
            {25, 26, 27}  // Sign 12: Purva Bhadrapada, Uttara Bhadrapada, Revati
        };
        int idx = (sign - 1 + 12) % 12;
        for (int nakIdx : signNakMap[idx]) {
            String localized = translationService.getLocalizedNakshatra(nakIdx);
            if (!list.contains(localized)) {
                list.add(localized);
            }
        }
        return list;
    }

    private ZonedDateTime jdToZonedDateTime(double jd, ZoneId zoneId) {
        long epochMs = Math.round((jd - 2440587.5) * 86400000.0);
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMs), zoneId);
    }

    private double[] getSunMoonLongitude(double jd) {
        int calculationFlags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SIDEREAL;
        double[] xx = new double[6];
        StringBuffer serr = new StringBuffer();
        
        double sunLong, moonLong;
        synchronized (swissEph) {
            swissEph.swe_calc_ut(jd, SweConst.SE_SUN, calculationFlags, xx, serr);
            sunLong = xx[0];
            swissEph.swe_calc_ut(jd, SweConst.SE_MOON, calculationFlags, xx, serr);
            moonLong = xx[0];
        }
        return new double[]{sunLong, moonLong};
    }

    private double getMoonLongitude(double jd) {
        int calculationFlags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SIDEREAL;
        double[] xx = new double[6];
        StringBuffer serr = new StringBuffer();
        synchronized (swissEph) {
            swissEph.swe_calc_ut(jd, SweConst.SE_MOON, calculationFlags, xx, serr);
        }
        return xx[0];
    }

    private double getSunLongitude(double jd) {
        int calculationFlags = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SIDEREAL;
        double[] xx = new double[6];
        StringBuffer serr = new StringBuffer();
        synchronized (swissEph) {
            swissEph.swe_calc_ut(jd, SweConst.SE_SUN, calculationFlags, xx, serr);
        }
        return xx[0];
    }

    private PanchangamElementDTO buildThithiDTO(int thithiIdx, double jdSunrise, double jdNextSunrise, ZoneId zoneId) {
        double targetVal = thithiIdx * 12.0;
        double endJd = findTransitionTime(jdSunrise, jdSunrise + 1.2, targetVal, jd -> {
            double[] coords = getSunMoonLongitude(jd);
            return (coords[1] - coords[0] + 720.0) % 360.0;
        });
        
        String endTimeStr = formatTransitionTime(endJd, jdSunrise, zoneId);
        String computedThithi = formatThithiName(thithiIdx);

        String nextName = null, nextLocalized = null, nextEndTime = null;
        if (endJd > 0 && endJd < jdNextSunrise) {
            int nextIdx = (thithiIdx % 30) + 1;
            nextName = "Thithi " + nextIdx;
            nextLocalized = formatThithiName(nextIdx);
            double nextEndJd = findTransitionTime(endJd + 0.01, endJd + 1.2, nextIdx * 12.0, jd -> {
                double[] coords = getSunMoonLongitude(jd);
                return (coords[1] - coords[0] + 720.0) % 360.0;
            });
            nextEndTime = formatTransitionTime(nextEndJd, jdSunrise, zoneId);
        }

        return new PanchangamElementDTO(thithiIdx, "Thithi " + thithiIdx, computedThithi, endTimeStr, nextName, nextLocalized, nextEndTime);
    }

    private String formatThithiName(int idx) {
        String paksha = (idx <= 15) ? translationService.getLabel("panchangam.shukla") : translationService.getLabel("panchangam.krishna");
        int localizedThithiNum = (idx > 15) ? idx - 15 : idx;
        String rawThithiLabel = translationService.getLabel("thithi." + localizedThithiNum);
        if (idx == 15 && rawThithiLabel.contains("/")) {
            return rawThithiLabel.split("/")[0].trim();
        } else if (idx == 30 && rawThithiLabel.contains("/")) {
            return rawThithiLabel.split("/")[1].trim();
        } else {
            return paksha + " - " + rawThithiLabel;
        }
    }

    private PanchangamElementDTO buildNakshatraDTO(int nakIdx, double jdSunrise, double jdNextSunrise, ZoneId zoneId) {
        double targetVal = nakIdx * (360.0 / 27.0);
        double endJd = findTransitionTime(jdSunrise, jdSunrise + 1.2, targetVal, this::getMoonLongitude);
        
        String endTimeStr = formatTransitionTime(endJd, jdSunrise, zoneId);
        String name = translationService.getLocalizedNakshatra(nakIdx);

        String nextName = null, nextLocalized = null, nextEndTime = null;
        if (endJd > 0 && endJd < jdNextSunrise) {
            int nextIdx = (nakIdx % 27) + 1;
            nextName = ZodiacUtils.getNakshatraName((nextIdx * (360.0 / 27.0)) - 1.0);
            nextLocalized = translationService.getLocalizedNakshatra(nextIdx);
            double nextEndJd = findTransitionTime(endJd + 0.01, endJd + 1.2, nextIdx * (360.0 / 27.0), this::getMoonLongitude);
            nextEndTime = formatTransitionTime(nextEndJd, jdSunrise, zoneId);
        }

        return new PanchangamElementDTO(nakIdx, ZodiacUtils.getNakshatraName(targetVal - 1.0), name, endTimeStr, nextName, nextLocalized, nextEndTime);
    }

    private PanchangamElementDTO buildYogamDTO(int yogamIdx, double jdSunrise, double jdNextSunrise, ZoneId zoneId) {
        double targetVal = yogamIdx * (360.0 / 27.0);
        double endJd = findTransitionTime(jdSunrise, jdSunrise + 1.2, targetVal, jd -> {
            double[] coords = getSunMoonLongitude(jd);
            return (coords[0] + coords[1] + 720.0) % 360.0;
        });

        String endTimeStr = formatTransitionTime(endJd, jdSunrise, zoneId);
        String name = translationService.getLabel("yogam." + yogamIdx);

        String nextName = null, nextLocalized = null, nextEndTime = null;
        if (endJd > 0 && endJd < jdNextSunrise) {
            int nextIdx = (yogamIdx % 27) + 1;
            nextName = "Yogam " + nextIdx;
            nextLocalized = translationService.getLabel("yogam." + nextIdx);
            double nextEndJd = findTransitionTime(endJd + 0.01, endJd + 1.2, nextIdx * (360.0 / 27.0), jd -> {
                double[] coords = getSunMoonLongitude(jd);
                return (coords[0] + coords[1] + 720.0) % 360.0;
            });
            nextEndTime = formatTransitionTime(nextEndJd, jdSunrise, zoneId);
        }

        return new PanchangamElementDTO(yogamIdx, "Yogam " + yogamIdx, name, endTimeStr, nextName, nextLocalized, nextEndTime);
    }

    private PanchangamElementDTO buildKaranamDTO(int karanamIdx, double jdSunrise, double jdNextSunrise, ZoneId zoneId) {
        double targetVal = karanamIdx * 6.0;
        double endJd = findTransitionTime(jdSunrise, jdSunrise + 1.2, targetVal, jd -> {
            double[] coords = getSunMoonLongitude(jd);
            return (coords[1] - coords[0] + 720.0) % 360.0;
        });

        String endTimeStr = formatTransitionTime(endJd, jdSunrise, zoneId);
        String name = translationService.getLabel("karanam." + resolveKaranamId(karanamIdx));

        String nextName = null, nextLocalized = null, nextEndTime = null;
        if (endJd > 0 && endJd < jdNextSunrise) {
            int nextIdx = (karanamIdx % 60) + 1;
            nextName = "Karanam " + nextIdx;
            nextLocalized = translationService.getLabel("karanam." + resolveKaranamId(nextIdx));
            double nextEndJd = findTransitionTime(endJd + 0.01, endJd + 1.2, nextIdx * 6.0, jd -> {
                double[] coords = getSunMoonLongitude(jd);
                return (coords[1] - coords[0] + 720.0) % 360.0;
            });
            nextEndTime = formatTransitionTime(nextEndJd, jdSunrise, zoneId);
        }

        return new PanchangamElementDTO(karanamIdx, "Karanam " + karanamIdx, name, endTimeStr, nextName, nextLocalized, nextEndTime);
    }

    private int resolveKaranamId(int idx) {
        if (idx == 1) return 1;
        if (idx >= 58 && idx <= 60) return idx - 49;
        return ((idx - 2) % 7) + 2;
    }

    private double findTransitionTime(double tStart, double tEnd, double targetVal, java.util.function.Function<Double, Double> func) {
        double low = tStart;
        double high = tEnd;
        double mid = low;

        double diffLow = angularDiff(func.apply(low), targetVal);
        double diffHigh = angularDiff(func.apply(high), targetVal);

        if (diffLow * diffHigh > 0) {
            return -1; // No transition
        }

        for (int i = 0; i < 20; i++) {
            mid = (low + high) / 2.0;
            double diffMid = angularDiff(func.apply(mid), targetVal);
            if (diffMid * diffLow < 0) {
                high = mid;
                diffHigh = diffMid;
            } else {
                low = mid;
                diffLow = diffMid;
            }
        }
        return mid;
    }

    private double angularDiff(double val, double target) {
        double diff = (val - target + 720.0) % 360.0;
        if (diff > 180.0) {
            diff -= 360.0;
        }
        return diff;
    }

    private String formatTransitionTime(double jd, double jdSunrise, ZoneId zoneId) {
        if (jd <= 0) {
            return translationService.getLabel("panchangam.throughout_day");
        }
        ZonedDateTime zdt = jdToZonedDateTime(jd, zoneId);
        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formatted = zdt.format(formatter);

        if (zdt.toLocalDate().isAfter(zdtSunrise.toLocalDate())) {
            String nextDayText = translationService.getLabel("panchangam.nextDay");
            if (nextDayText == null || nextDayText.startsWith("panchangam.")) {
                nextDayText = translationService.getLabel("nextDay");
            }
            if (nextDayText == null || nextDayText.isEmpty() || nextDayText.equals("nextDay")) {
                nextDayText = "Next Day";
            }
            return formatted + " (" + nextDayText + ")";
        }
        return formatted;
    }

    private List<TimeSlotDTO> calculateNallaNeram(ZonedDateTime sunrise, int dayOfWeek) {
        List<TimeSlotDTO> list = new ArrayList<>();
        // Morning and Evening offsets
        double mStart, mEnd, eStart, eEnd;
        switch (dayOfWeek) {
            case 0: // Sunday
                mStart = 1.5; mEnd = 3.0;
                eStart = 9.5; eEnd = 10.5;
                break;
            case 1: // Monday
                mStart = 3.25; mEnd = 4.25;
                eStart = 10.75; eEnd = 11.75;
                break;
            case 2: // Tuesday
                mStart = 1.5; mEnd = 2.5;
                eStart = 10.5; eEnd = 11.5;
                break;
            case 3: // Wednesday
                mStart = 3.25; mEnd = 4.25;
                eStart = 10.75; eEnd = 11.75;
                break;
            case 4: // Thursday
                mStart = 3.25; mEnd = 4.25;
                eStart = 10.75; eEnd = 11.75;
                break;
            case 5: // Friday
                mStart = 3.25; mEnd = 4.25;
                eStart = 10.75; eEnd = 11.75;
                break;
            default: // Saturday (6)
                mStart = 1.5; mEnd = 2.5;
                eStart = 10.75; eEnd = 11.75;
                break;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        ZonedDateTime mStartZdt = sunrise.plusMinutes((long) (mStart * 60.0));
        ZonedDateTime mEndZdt = sunrise.plusMinutes((long) (mEnd * 60.0));
        list.add(new TimeSlotDTO(mStartZdt.format(formatter), mEndZdt.format(formatter), translationService.getLabel("panchangam.nalla_neram")));

        ZonedDateTime eStartZdt = sunrise.plusMinutes((long) (eStart * 60.0));
        ZonedDateTime eEndZdt = sunrise.plusMinutes((long) (eEnd * 60.0));
        list.add(new TimeSlotDTO(eStartZdt.format(formatter), eEndZdt.format(formatter), translationService.getLabel("panchangam.nalla_neram")));

        return list;
    }

    private List<TimeSlotDTO> calculateKalam(ZonedDateTime sunrise, double dayDurationHours, int part, String label) {
        double partDurationHours = dayDurationHours / 8.0;
        double startHour = (part - 1) * partDurationHours;
        double endHour = part * partDurationHours;

        ZonedDateTime start = sunrise.plusMinutes((long) (startHour * 60.0));
        ZonedDateTime end = sunrise.plusMinutes((long) (endHour * 60.0));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        List<TimeSlotDTO> list = new ArrayList<>();
        list.add(new TimeSlotDTO(start.format(formatter), end.format(formatter), label));
        return list;
    }

    private int getRahuPart(int dayOfWeek) {
        // Sunday=8, Monday=2, Tuesday=7, Wednesday=5, Thursday=6, Friday=4, Saturday=3
        int[] table = {8, 2, 7, 5, 6, 4, 3};
        return table[dayOfWeek];
    }

    private int getYamagandamPart(int dayOfWeek) {
        // Sunday=5, Monday=4, Tuesday=3, Wednesday=2, Thursday=1, Friday=7, Saturday=6
        int[] table = {5, 4, 3, 2, 1, 7, 6};
        return table[dayOfWeek];
    }

    private int getKulikaiPart(int dayOfWeek) {
        // Sunday=7, Monday=6, Tuesday=5, Wednesday=4, Thursday=3, Friday=2, Saturday=1
        int[] table = {7, 6, 5, 4, 3, 2, 1};
        return table[dayOfWeek];
    }

    private List<TimeSlotDTO> calculateGowriNallaNeram(double jdSunrise, double jdSunset, double jdNextSunrise, int dayOfWeek, ZoneId zoneId) {
        List<TimeSlotDTO> list = new ArrayList<>();
        double dayPartDuration = (jdSunset - jdSunrise) / 8.0;
        double nightPartDuration = (jdNextSunrise - jdSunset) / 8.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Day Gowri
        String[] dayStates = GOWRI_DAY_STATES[dayOfWeek];
        for (int i = 0; i < 8; i++) {
            String state = dayStates[i];
            if (isGowriAuspicious(state)) {
                double startJd = jdSunrise + i * dayPartDuration;
                double endJd = jdSunrise + (i + 1) * dayPartDuration;
                ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
                ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
                String stateLabel = translationService.getLabel("gowri." + state.toLowerCase());
                list.add(new TimeSlotDTO(start.format(formatter), end.format(formatter), stateLabel));
            }
        }

        // Night Gowri
        String[] nightStates = GOWRI_NIGHT_STATES[dayOfWeek];
        for (int i = 0; i < 8; i++) {
            String state = nightStates[i];
            if (isGowriAuspicious(state)) {
                double startJd = jdSunset + i * nightPartDuration;
                double endJd = jdSunset + (i + 1) * nightPartDuration;
                ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
                ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
                String stateLabel = translationService.getLabel("gowri." + state.toLowerCase());
                list.add(new TimeSlotDTO(start.format(formatter), end.format(formatter), stateLabel));
            }
        }

        return list;
    }

    private boolean isGowriAuspicious(String state) {
        return "Amirdha".equals(state) || "Dhanam".equals(state) || "Uthi".equals(state) || "Laabam".equals(state) || "Sugam".equals(state);
    }

    private List<HoraTimeSlotDTO> calculateHorais(double jdSunrise, double jdSunset, double jdNextSunrise, int dayOfWeek, ZoneId zoneId) {
        List<HoraTimeSlotDTO> list = new ArrayList<>();
        double dayHourDuration = (jdSunset - jdSunrise) / 12.0;
        double nightHourDuration = (jdNextSunrise - jdSunset) / 12.0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        int planetIndex = (dayOfWeek * 3) % 7;

        // 12 Day Horais
        for (int i = 0; i < 12; i++) {
            String planet = HORA_PLANETS[planetIndex];
            double startJd = jdSunrise + i * dayHourDuration;
            double endJd = jdSunrise + (i + 1) * dayHourDuration;
            ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
            ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
            String planetLabel = translationService.getLabel("planet." + planet);

            list.add(new HoraTimeSlotDTO(i + 1, start.format(formatter), end.format(formatter), planet, planetLabel));
            planetIndex = (planetIndex + 1) % 7;
        }

        // 12 Night Horais
        for (int i = 0; i < 12; i++) {
            String planet = HORA_PLANETS[planetIndex];
            double startJd = jdSunset + i * nightHourDuration;
            double endJd = jdSunset + (i + 1) * nightHourDuration;
            ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
            ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
            String planetLabel = translationService.getLabel("planet." + planet);

            list.add(new HoraTimeSlotDTO(i + 13, start.format(formatter), end.format(formatter), planet, planetLabel));
            planetIndex = (planetIndex + 1) % 7;
        }

        return list;
    }

    private int calculateNetram(int d) {
        // If distance is 0, 1, 2, 3, 24, 25, 26: value is 0
        if (d == 0 || d == 1 || d == 2 || d == 3 || d == 24 || d == 25 || d == 26) return 0;
        // If distance is 4, 5, 6, 7, 20, 21, 22, 23: value is 1
        if (d == 4 || d == 5 || d == 6 || d == 7 || d == 20 || d == 21 || d == 22 || d == 23) return 1;
        // Otherwise: value is 2
        return 2;
    }

    private double calculateJeevan(int d) {
        // If distance is 0, 1, 9, 18, 26: value is 0.0
        if (d == 0 || d == 1 || d == 9 || d == 18 || d == 26) return 0.0;
        // If distance is 2..8, 19..25: value is 0.5
        if ((d >= 2 && d <= 8) || (d >= 19 && d <= 25)) return 0.5;
        // Otherwise: value is 1.0
        return 1.0;
    }
}
