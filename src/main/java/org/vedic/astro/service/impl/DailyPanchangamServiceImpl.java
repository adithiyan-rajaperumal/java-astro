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

    // 0=Amirdha, 1=Siddha, 2=Marana, 3=Prabalarishta
    private static final int[][] NAKSHATRA_VARA_YOGAMS = {
        // Sun (0): Amirdha=Hastam(12); Marana=Bharani(1), Arudra(5), Ashlesha(8), Visakha(15), Dhanishta(22)
        {1, 2, 1, 1, 1, 2, 1, 1, 2, 1, 1, 1, 0, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1},
        // Mon (1): Marana=Aswini(0), Krittika(2), Arudra(5), Ashlesha(8), P.Phalguni(10), Visakha(15), Jyeshta(17), Dhanishta(22), P.Bhadra(24); Prabalarishta=Hasta(12); Siddha=Sravana(21)
        {2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 3, 1, 1, 2, 1, 2, 1, 1, 1, 1, 2, 1, 2, 1, 1},
        // Tue (2): Amirdha=Aswini(0); Marana=Mrigashira(4), Chitra(13), Dhanishta(22); Prabalarishta=P.Ashadha(19)
        {0, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 3, 1, 1, 2, 1, 1, 1, 1},
        // Wed (3): Amirdha=Anuradha(16); Marana=Arudra(5), Ashlesha(8), Jyeshta(17), Shatabhisha(23); Prabalarishta=Krittika(2)
        {1, 1, 3, 1, 1, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 0, 2, 1, 1, 1, 1, 1, 2, 1, 1, 1},
        // Thu (4): Amirdha=Pushya(7); Marana=Arudra(5), Ashlesha(8), Visakha(15), Jyeshta(17)
        {1, 1, 1, 1, 1, 2, 1, 0, 2, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1},
        // Fri (5): Amirdha=Revati(26); Marana=Arudra(5), Ashlesha(8); Prabalarishta=Mrigashira(4)
        {1, 1, 1, 1, 3, 2, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0},
        // Sat (6): Amirdha=Rohini(3); Marana=Ashlesha(8), Visakha(15), Jyeshta(17); Prabalarishta=P.Ashadha(19)
        {1, 1, 1, 0, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 2, 1, 2, 1, 3, 1, 1, 1, 1, 1, 1, 1}
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

        int dayOfWeekVal = date.getDayOfWeek().getValue(); // Monday=1, ..., Sunday=7
        int dayOfWeek0 = (dayOfWeekVal == 7) ? 0 : dayOfWeekVal; // Sunday=0, Monday=1, ..., Saturday=6

        // Rahu Kalam, Emagandam, Kulikai
        double dayDurationHours = (jdSunset - jdSunrise) * 24.0;
        List<TimeSlotDTO> raghuKalam = calculateKalam(zdtSunrise, dayDurationHours, getRahuPart(dayOfWeek0), translationService.getLabel("panchangam.raghu_kalam"));
        List<TimeSlotDTO> emagandam = calculateKalam(zdtSunrise, dayDurationHours, getYamagandamPart(dayOfWeek0), translationService.getLabel("panchangam.emagandam"));
        List<TimeSlotDTO> kulikai = calculateKalam(zdtSunrise, dayDurationHours, getKulikaiPart(dayOfWeek0), translationService.getLabel("panchangam.kulikai"));

        // Dynamic Nalla Neram (derived from Gowri + non-overlapping inauspicious Kalam parts)
        List<TimeSlotDTO> nallaNeram = calculateDynamicNallaNeram(jdSunrise, jdSunset, jdNextSunrise, dayOfWeek0, zoneId);

        List<TimeSlotDTO> gowriNallaNeram = calculateGowriNallaNeram(jdSunrise, jdSunset, jdNextSunrise, dayOfWeek0, zoneId);

        List<TimeSlotDTO> nakshatraYogams = calculateNakshatraYogams(nakIdx, jdSunrise, jdNextSunrise, dayOfWeek0, zoneId);

        // Horais
        List<HoraTimeSlotDTO> horais = calculateHorais(jdSunrise, jdSunset, jdNextSunrise, dayOfWeek0, zoneId);

        // Abhijit Muhurtham (8th of 15 daytime Muhurthams)
        TimeSlotDTO abhijitMuhurtham = calculateAbhijitMuhurtham(jdSunrise, jdSunset, dayOfWeek0, zoneId);

        // Precise Chandrastamam Nakshatra(s) (8th house Nakshatra count relative to today's transiting Moon Nakshatra)
        List<String> chandrastamamNakshatras = getExactChandrastamamNakshatras(nakshatraDTO);

        // Netram and Jeevan
        double[] coordinatesSun = getSunMoonLongitude(jdSunrise); // reload coordinates just in case
        int sunNakNum = (int) (coordinatesSun[0] / (360.0 / 27.0)) + 1;
        int dDiff = (nakIdx - sunNakNum + 27) % 27;

        int netram = calculateNetram(dDiff);
        double jeevan = calculateJeevan(dDiff);

        // Nakshatra-Vara Yogam type at sunrise (0=Amrita, 1=Siddha, 2=Marana, 3=Prabalarishta)
        int yogamTypeAtSunrise = NAKSHATRA_VARA_YOGAMS[dayOfWeek0][nakIdx - 1];

        // Agni Nakshathiram (Sun in Krittika Nakshatra = 3rd Nakshatra)
        boolean isAgniNakshathiram = (sunNakNum == 3);

        // Auspicious Thithi Expansion (Include Dwadashi 12 & Purnima 15; exclude Rikta 4,9,14,19,24,29 & Amavasya 30)
        boolean isAuspiciousThithi = (thithiIdx == 2 || thithiIdx == 3 || thithiIdx == 5 || thithiIdx == 7 || thithiIdx == 10 
                || thithiIdx == 11 || thithiIdx == 12 || thithiIdx == 13 || thithiIdx == 15 || thithiIdx == 17 
                || thithiIdx == 18 || thithiIdx == 20 || thithiIdx == 22 || thithiIdx == 25 || thithiIdx == 26 || thithiIdx == 28);

        // Auspicious Nakshatra
        boolean isAuspiciousNakshatra = (nakIdx == 1 || nakIdx == 4 || nakIdx == 5 || nakIdx == 8 || nakIdx == 12 
                || nakIdx == 13 || nakIdx == 14 || nakIdx == 15 || nakIdx == 17 || nakIdx == 21 
                || nakIdx == 22 || nakIdx == 23 || nakIdx == 24 || nakIdx == 26 || nakIdx == 27);

        // Karanam check (Exclude Vishti Karanam = 7)
        boolean isAuspiciousKaranam = (karanamIdx != 7);

        // Solar Rashi (Exclude Margazhi = 9)
        int solarRashi = (int) (coordinatesSun[0] / 30.0) + 1;
        boolean isAuspiciousMonth = (solarRashi != 9);

        // Subha Muhurtham Day Calculation
        boolean isMuhurthamDay = (date.getDayOfWeek() != DayOfWeek.TUESDAY && date.getDayOfWeek() != DayOfWeek.SATURDAY)
                && isAuspiciousThithi
                && isAuspiciousNakshatra
                && isAuspiciousKaranam
                && isAuspiciousMonth
                && (yogamTypeAtSunrise == 0 || yogamTypeAtSunrise == 1) // Must be Amrita or Siddha Yogam
                && (netram > 0 && jeevan > 0);

        // Vasthu Result
        VasthuResult vasthu = calculateVasthuDetails(jdSunrise, jdSunset, coordinatesSun[0], dayOfWeek0, yogamTypeAtSunrise, zoneId);

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
            nakshatraYogams,
            raghuKalam,
            emagandam,
            kulikai,
            horais,
            abhijitMuhurtham,
            chandrastamamNakshatras,
            netram,
            jeevan,
            isMuhurthamDay,
            vasthu.isVasthuDay(),
            vasthu.isVasthuAuspicious(),
            isAgniNakshathiram,
            vasthu.vasthuNeram(),
            vasthu.vasthuPujaNeram()
        );
    }

    private record VasthuResult(
        boolean isVasthuDay,
        boolean isVasthuAuspicious,
        TimeSlotDTO vasthuNeram,
        TimeSlotDTO vasthuPujaNeram
    ) {}

    private VasthuResult calculateVasthuDetails(double jdSunrise, double jdSunset, double sunLong, int dayOfWeek, int yogamType, ZoneId zoneId) {
        int solarRashi = (int) (sunLong / 30.0) + 1; // 1=Chithirai, 2=Vaikasi, ..., 12=Panguni
        double targetVal = (solarRashi - 1) * 30.0;

        // Find exact Sankranti ingress time for active Solar Rashi
        double jdSankranti = findTransitionTime(jdSunrise - 32.0, jdSunrise, targetVal, this::getSunLongitude);
        int solarDay = (int) Math.floor(jdSunrise - jdSankranti) + 1;

        // Canonical Vasthu Solar Days & Awake Nazhigai (after sunrise)
        int targetSolarDay = -1;
        double awakeNazhigai = -1;

        switch (solarRashi) {
            case 1:  targetSolarDay = 10; awakeNazhigai = 7.25;  break; // Chithirai 10
            case 2:  targetSolarDay = 21; awakeNazhigai = 10.25; break; // Vaikasi 21
            case 4:  targetSolarDay = 11; awakeNazhigai = 4.25;  break; // Aadi 11
            case 5:  targetSolarDay = 6;  awakeNazhigai = 3.75;  break; // Avani 6
            case 7:  targetSolarDay = 11; awakeNazhigai = 4.25;  break; // Aippasi 11
            case 8:  targetSolarDay = 8;  awakeNazhigai = 13.25; break; // Karthigai 8
            case 10: targetSolarDay = 12; awakeNazhigai = 11.50; break; // Thai 12
            case 11: targetSolarDay = 22; awakeNazhigai = 11.25; break; // Masi 22
            default: break; // No Vasthu days in 3 (Aani), 6 (Purattasi), 9 (Margazhi), 12 (Panguni)
        }

        if (jdSankranti < 0 || targetSolarDay == -1 || Math.abs(solarDay - targetSolarDay) > 0 || awakeNazhigai < 0) {
            return new VasthuResult(false, false, null, null);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);

        double dayDurationDays = (jdSunset - jdSunrise);
        // Strict Swiss Ephemeris Drik calculation: 1 daytime Nazhigai = (jdSunset - jdSunrise) / 30.0
        double startAwakeJd = jdSunrise + (awakeNazhigai / 30.0) * dayDurationDays;
        double endAwakeJd = startAwakeJd + (3.75 / 30.0) * dayDurationDays; // 3.75 Nazhigai = 90 mins

        double startPujaJd = startAwakeJd + (1.5 / 30.0) * dayDurationDays; // 1.5 Nazhigai (36 mins) after awake start
        double endPujaJd = startPujaJd + (1.5 / 30.0) * dayDurationDays;     // 1.5 Nazhigai (36 mins) Puja duration

        ZonedDateTime awakeStart = jdToZonedDateTime(startAwakeJd, zoneId);
        ZonedDateTime awakeEnd = jdToZonedDateTime(endAwakeJd, zoneId);
        ZonedDateTime pujaStart = jdToZonedDateTime(startPujaJd, zoneId);
        ZonedDateTime pujaEnd = jdToZonedDateTime(endPujaJd, zoneId);

        String vasthuLabel = translationService.getLabel("panchangam.vasthu_neram");
        if (vasthuLabel == null || vasthuLabel.startsWith("panchangam.")) vasthuLabel = "Vasthu Neram";
        String pujaLabel = translationService.getLabel("panchangam.vasthu_puja_neram");
        if (pujaLabel == null || pujaLabel.startsWith("panchangam.")) pujaLabel = "Vasthu Puja Neram";

        TimeSlotDTO vasthuNeram = createTimeSlotDTO(awakeStart, awakeEnd, vasthuLabel, zdtSunrise, formatter);
        TimeSlotDTO vasthuPujaNeram = createTimeSlotDTO(pujaStart, pujaEnd, pujaLabel, zdtSunrise, formatter);

        boolean isAuspicious = (dayOfWeek != 2 && dayOfWeek != 6) && (yogamType != 2);

        return new VasthuResult(true, isAuspicious, vasthuNeram, vasthuPujaNeram);
    }

    private List<String> getExactChandrastamamNakshatras(PanchangamElementDTO nakshatraDTO) {
        List<String> list = new ArrayList<>();
        if (nakshatraDTO == null) return list;

        int currentNakIdx = nakshatraDTO.number();
        int exactNakIdx = ((currentNakIdx + 12 - 1) % 27) + 1;
        String localized = translationService.getLocalizedNakshatra(exactNakIdx);
        list.add(localized);

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

    private List<TimeSlotDTO> calculateDynamicNallaNeram(double jdSunrise, double jdSunset, double jdNextSunrise, int dayOfWeek, ZoneId zoneId) {
        List<TimeSlotDTO> list = new ArrayList<>();
        double dayDuration = jdSunset - jdSunrise;
        if (dayDuration <= 0 || Double.isNaN(dayDuration)) {
            return list;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        // Dynamic standard offsets scaled by actual day duration (assuming 12h standard day)
        double mStartOffset, mEndOffset, eStartOffset, eEndOffset;
        switch (dayOfWeek) {
            case 0: // Sun (07:30 - 08:30 & 04:30 - 05:30)
                mStartOffset = 1.5; mEndOffset = 2.5; eStartOffset = 10.5; eEndOffset = 11.5; break;
            case 1: // Mon (06:00 - 07:30 & 04:30 - 05:30)
                mStartOffset = 0.0; mEndOffset = 1.5; eStartOffset = 10.5; eEndOffset = 11.5; break;
            case 2: // Tue (07:30 - 08:30 & 04:30 - 05:30)
                mStartOffset = 1.5; mEndOffset = 2.5; eStartOffset = 10.5; eEndOffset = 11.5; break;
            case 3: // Wed (09:00 - 10:00 & 04:30 - 05:30)
                mStartOffset = 3.0; mEndOffset = 4.0; eStartOffset = 10.5; eEndOffset = 11.5; break;
            case 4: // Thu (09:00 - 10:00 & 04:30 - 05:30 -> 04:23 PM - 05:27 PM dynamic for local solar geometry)
                mStartOffset = 3.0; mEndOffset = 4.0; eStartOffset = 9.75; eEndOffset = 10.75; break;
            case 5: // Fri (09:00 - 10:00 & 04:30 - 05:30)
                mStartOffset = 3.0; mEndOffset = 4.0; eStartOffset = 10.5; eEndOffset = 11.5; break;
            default: // Sat (07:30 - 08:30 & 05:00 - 06:00)
                mStartOffset = 1.5; mEndOffset = 2.5; eStartOffset = 11.0; eEndOffset = 12.0; break;
        }

        double stdMStartJd = jdSunrise + (mStartOffset / 12.0) * dayDuration;
        double stdMEndJd   = jdSunrise + (mEndOffset / 12.0) * dayDuration;

        double stdEStartJd = jdSunrise + (eStartOffset / 12.0) * dayDuration;
        double stdEEndJd   = jdSunrise + (eEndOffset / 12.0) * dayDuration;

        // Inauspicious Kalam dynamic ranges for today
        double dayPartDuration = dayDuration / 8.0;
        int rahuP = getRahuPart(dayOfWeek);
        int yamaP = getYamagandamPart(dayOfWeek);
        int kulikaiP = getKulikaiPart(dayOfWeek);

        double rahuStartJd = jdSunrise + (rahuP - 1) * dayPartDuration;
        double rahuEndJd = jdSunrise + rahuP * dayPartDuration;

        double yamaStartJd = jdSunrise + (yamaP - 1) * dayPartDuration;
        double yamaEndJd = jdSunrise + yamaP * dayPartDuration;

        double kulikaiStartJd = jdSunrise + (kulikaiP - 1) * dayPartDuration;
        double kulikaiEndJd = jdSunrise + kulikaiP * dayPartDuration;

        // Check if Standard Morning Slot collides with Rahu, Yamagandam, or Kulikai
        boolean mCollides = isColliding(stdMStartJd, stdMEndJd, rahuStartJd, rahuEndJd) ||
                            isColliding(stdMStartJd, stdMEndJd, yamaStartJd, yamaEndJd) ||
                            isColliding(stdMStartJd, stdMEndJd, kulikaiStartJd, kulikaiEndJd);

        boolean eCollides = isColliding(stdEStartJd, stdEEndJd, rahuStartJd, rahuEndJd) ||
                            isColliding(stdEStartJd, stdEEndJd, yamaStartJd, yamaEndJd) ||
                            isColliding(stdEStartJd, stdEEndJd, kulikaiStartJd, kulikaiEndJd);

        record SlotCandidate(double startJd, double endJd, TimeSlotDTO dto) {}
        List<SlotCandidate> finalSlots = new ArrayList<>();

        if (!mCollides) {
            ZonedDateTime s = jdToZonedDateTime(stdMStartJd, zoneId);
            ZonedDateTime e = jdToZonedDateTime(stdMEndJd, zoneId);
            finalSlots.add(new SlotCandidate(stdMStartJd, stdMEndJd, new TimeSlotDTO(s.format(formatter), e.format(formatter), translationService.getLabel("panchangam.nalla_neram"))));
        } else {
            GowriSlot gowriFallback = getBestCleanGowriSlot(jdSunrise, jdSunset, dayOfWeek, zoneId, true, rahuP, yamaP, kulikaiP);
            if (gowriFallback != null) {
                ZonedDateTime s = jdToZonedDateTime(gowriFallback.startJd(), zoneId);
                ZonedDateTime e = jdToZonedDateTime(gowriFallback.endJd(), zoneId);
                String stateLabel = translationService.getLabel("gowri." + gowriFallback.state().toLowerCase());
                finalSlots.add(new SlotCandidate(gowriFallback.startJd(), gowriFallback.endJd(), new TimeSlotDTO(s.format(formatter), e.format(formatter), stateLabel)));
            }
        }

        if (!eCollides) {
            ZonedDateTime s = jdToZonedDateTime(stdEStartJd, zoneId);
            ZonedDateTime e = jdToZonedDateTime(stdEEndJd, zoneId);
            finalSlots.add(new SlotCandidate(stdEStartJd, stdEEndJd, new TimeSlotDTO(s.format(formatter), e.format(formatter), translationService.getLabel("panchangam.nalla_neram"))));
        } else {
            GowriSlot gowriFallback = getBestCleanGowriSlot(jdSunrise, jdSunset, dayOfWeek, zoneId, false, rahuP, yamaP, kulikaiP);
            if (gowriFallback != null) {
                ZonedDateTime s = jdToZonedDateTime(gowriFallback.startJd(), zoneId);
                ZonedDateTime e = jdToZonedDateTime(gowriFallback.endJd(), zoneId);
                String stateLabel = translationService.getLabel("gowri." + gowriFallback.state().toLowerCase());
                finalSlots.add(new SlotCandidate(gowriFallback.startJd(), gowriFallback.endJd(), new TimeSlotDTO(s.format(formatter), e.format(formatter), stateLabel)));
            }
        }

        // If ANY collision occurred, check if Amirdha Gowri is available in a clean slot and not already included
        if (mCollides || eCollides) {
            String[] dayStates = GOWRI_DAY_STATES[dayOfWeek];
            for (int i = 0; i < 8; i++) {
                int partNum = i + 1;
                if ("Amirdha".equals(dayStates[i]) && partNum != rahuP && partNum != yamaP && partNum != kulikaiP) {
                    double amStartJd = jdSunrise + i * dayPartDuration;
                    double amEndJd   = jdSunrise + (i + 1) * dayPartDuration;

                    boolean alreadyPresent = finalSlots.stream().anyMatch(sc -> isColliding(sc.startJd(), sc.endJd(), amStartJd, amEndJd));
                    if (!alreadyPresent) {
                        ZonedDateTime s = jdToZonedDateTime(amStartJd, zoneId);
                        ZonedDateTime e = jdToZonedDateTime(amEndJd, zoneId);
                        String stateLabel = translationService.getLabel("gowri.amirdha");
                        finalSlots.add(new SlotCandidate(amStartJd, amEndJd, new TimeSlotDTO(s.format(formatter), e.format(formatter), stateLabel)));
                    }
                }
            }
        }

        // Sort final slots chronologically by startJd
        finalSlots.sort(java.util.Comparator.comparingDouble(SlotCandidate::startJd));

        return finalSlots.stream().map(SlotCandidate::dto).collect(java.util.stream.Collectors.toList());
    }

    private boolean isColliding(double s1, double e1, double s2, double e2) {
        return Math.max(s1, s2) < Math.min(e1, e2);
    }

    private record GowriSlot(String state, int rank, double startJd, double endJd) {}

    private GowriSlot getBestCleanGowriSlot(double jdSunrise, double jdSunset, int dayOfWeek, ZoneId zoneId, boolean isMorning, int rahuP, int yamaP, int kulikaiP) {
        double dayPartDuration = (jdSunset - jdSunrise) / 8.0;
        String[] dayStates = GOWRI_DAY_STATES[dayOfWeek];

        List<GowriSlot> cands = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            int partNum = i + 1;
            String state = dayStates[i];
            int rank = getGowriRank(state);

            if (rank > 0 && partNum != rahuP && partNum != yamaP && partNum != kulikaiP) {
                double sJd = jdSunrise + i * dayPartDuration;
                double eJd = jdSunrise + (i + 1) * dayPartDuration;
                ZonedDateTime sZdt = jdToZonedDateTime(sJd, zoneId);
                int hour = sZdt.getHour();

                if (isMorning && hour < 12) {
                    cands.add(new GowriSlot(state, rank, sJd, eJd));
                } else if (!isMorning && hour >= 12) {
                    cands.add(new GowriSlot(state, rank, sJd, eJd));
                }
            }
        }

        if (cands.isEmpty()) {
            for (int i = 0; i < 8; i++) {
                int partNum = i + 1;
                String state = dayStates[i];
                int rank = getGowriRank(state);
                if (rank > 0 && partNum != rahuP && partNum != yamaP && partNum != kulikaiP) {
                    double sJd = jdSunrise + i * dayPartDuration;
                    double eJd = jdSunrise + (i + 1) * dayPartDuration;
                    cands.add(new GowriSlot(state, rank, sJd, eJd));
                }
            }
        }

        return cands.stream()
                .min(java.util.Comparator.comparingInt(GowriSlot::rank))
                .orElse(null);
    }

    private int getGowriRank(String state) {
        return switch (state) {
            case "Amirdha" -> 1;
            case "Laabam" -> 2;
            case "Dhanam" -> 3;
            case "Sugam" -> 4;
            case "Uthi" -> 5;
            default -> 0; // Inauspicious
        };
    }

    private TimeSlotDTO calculateAbhijitMuhurtham(double jdSunrise, double jdSunset, int dayOfWeek, ZoneId zoneId) {
        double muhurthamDuration = (jdSunset - jdSunrise) / 15.0;
        double startJd = jdSunrise + 7 * muhurthamDuration;
        double endJd = jdSunrise + 8 * muhurthamDuration;

        ZonedDateTime start = jdToZonedDateTime(startJd, zoneId);
        ZonedDateTime end = jdToZonedDateTime(endJd, zoneId);
        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");

        String label = translationService.getLabel("panchangam.abhijit_muhurtham");
        if (dayOfWeek == 3) { // Wednesday - afflicted / nullified
            label += " (" + translationService.getLabel("panchangam.nullified") + ")";
        }

        return createTimeSlotDTO(start, end, label, zdtSunrise, formatter);
    }

    private List<TimeSlotDTO> calculateKalam(ZonedDateTime sunrise, double dayDurationHours, int part, String label) {
        double partDurationHours = dayDurationHours / 8.0;
        double startHour = (part - 1) * partDurationHours;
        double endHour = part * partDurationHours;

        ZonedDateTime start = sunrise.plusMinutes((long) (startHour * 60.0));
        ZonedDateTime end = sunrise.plusMinutes((long) (endHour * 60.0));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        List<TimeSlotDTO> list = new ArrayList<>();
        list.add(createTimeSlotDTO(start, end, label, sunrise, formatter));
        return list;
    }

    private TimeSlotDTO createTimeSlotDTO(ZonedDateTime start, ZonedDateTime end, String label, ZonedDateTime zdtSunrise, DateTimeFormatter formatter) {
        LocalDate panchangamDate = zdtSunrise.toLocalDate();
        boolean startNextDay = start.toLocalDate().isAfter(panchangamDate);
        boolean endNextDay = end.toLocalDate().isAfter(panchangamDate);
        return new TimeSlotDTO(start.format(formatter), end.format(formatter), label, startNextDay, endNextDay);
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
        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);

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
                list.add(createTimeSlotDTO(start, end, stateLabel, zdtSunrise, formatter));
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
                list.add(createTimeSlotDTO(start, end, stateLabel, zdtSunrise, formatter));
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

    private List<TimeSlotDTO> calculateNakshatraYogams(int nakIdx, double jdSunrise, double jdNextSunrise, int dayOfWeek, ZoneId zoneId) {
        List<TimeSlotDTO> list = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        ZonedDateTime zdtSunrise = jdToZonedDateTime(jdSunrise, zoneId);

        double targetVal = nakIdx * (360.0 / 27.0);
        double endJd = findTransitionTime(jdSunrise, jdSunrise + 1.2, targetVal, this::getMoonLongitude);

        double span1EndJd = (endJd > 0 && endJd < jdNextSunrise) ? endJd : jdNextSunrise;
        ZonedDateTime s1 = jdToZonedDateTime(jdSunrise, zoneId);
        ZonedDateTime e1 = jdToZonedDateTime(span1EndJd, zoneId);

        int yogamType1 = NAKSHATRA_VARA_YOGAMS[dayOfWeek][nakIdx - 1];
        String key1 = switch (yogamType1) {
            case 0 -> "gowri.amirdha_yogam";
            case 1 -> "gowri.siddha_yogam";
            case 2 -> "gowri.marana_yogam";
            default -> "gowri.prabalarishta_yogam";
        };
        list.add(createTimeSlotDTO(s1, e1, translationService.getLabel(key1), zdtSunrise, formatter));

        if (endJd > 0 && endJd < jdNextSunrise) {
            int nextNakIdx = (nakIdx % 27) + 1;
            ZonedDateTime s2 = jdToZonedDateTime(endJd, zoneId);

            double targetVal2 = nextNakIdx * (360.0 / 27.0);
            double endJd2 = findTransitionTime(endJd + 0.01, endJd + 1.2, targetVal2, this::getMoonLongitude);
            double span2EndJd = (endJd2 > 0 && endJd2 < jdNextSunrise) ? endJd2 : jdNextSunrise;
            ZonedDateTime e2 = jdToZonedDateTime(span2EndJd, zoneId);

            int yogamType2 = NAKSHATRA_VARA_YOGAMS[dayOfWeek][nextNakIdx - 1];
            String key2 = switch (yogamType2) {
                case 0 -> "gowri.amirdha_yogam";
                case 1 -> "gowri.siddha_yogam";
                case 2 -> "gowri.marana_yogam";
                default -> "gowri.prabalarishta_yogam";
            };
            list.add(createTimeSlotDTO(s2, e2, translationService.getLabel(key2), zdtSunrise, formatter));

            if (endJd2 > 0 && endJd2 < jdNextSunrise) {
                int thirdNakIdx = (nextNakIdx % 27) + 1;
                ZonedDateTime s3 = jdToZonedDateTime(endJd2, zoneId);
                ZonedDateTime e3 = jdToZonedDateTime(jdNextSunrise, zoneId);

                int yogamType3 = NAKSHATRA_VARA_YOGAMS[dayOfWeek][thirdNakIdx - 1];
                String key3 = switch (yogamType3) {
                    case 0 -> "gowri.amirdha_yogam";
                    case 1 -> "gowri.siddha_yogam";
                    case 2 -> "gowri.marana_yogam";
                    default -> "gowri.prabalarishta_yogam";
                };
                list.add(createTimeSlotDTO(s3, e3, translationService.getLabel(key3), zdtSunrise, formatter));
            }
        }

        return list;
    }
}
