package org.vedic.astro.util;

/**
 * Dynamic Samvatsara Baseline Resolver.
 * Computes Chithirai 1 Ahargana and annual planetary starting longitudes.
 */
public class SamvatsaraTable {

    public record SamvatsaraBase(
            String name,
            int index60,
            double chithirai1Ahargana,
            double moonMeanBase,
            double marsBase,
            double jupiterBase,
            double saturnBase,
            double rahuBase
    ) {}

    public static final String[] SAMVATSARA_NAMES = {
            "Prabhava", "Vibhava", "Sukla", "Pramodoota", "Prajothpatti", "Aangirasa",
            "Srimukha", "Bhava", "Yuva", "Dhaatu", "Eesvara", "Vehudhanya",
            "Pramathi", "Vikrama", "Visha", "Chithirabanu", "Subhanu", "Thaarana",
            "Parthaiva", "Vyaya", "Sarvajith", "Sarvadhari", "Virodhi", "Vikruthi",
            "Khara", "Nandhana", "Vijaya", "Jaya", "Manmatha", "Dhurmukhi",
            "Hevilambi", "Vilambi", "Vikari", "Sarvari", "Plava", "Subhakruth",
            "Sobhakruth", "Krodhi", "Visvavasu", "Parabhavha", "Plavanga", "Keelaka",
            "Soumya", "Sadharana", "Virodhikruth", "Paridhavi", "Pramadheecha", "Aananda",
            "Rakshasa", "Anala", "Pingala", "Kalayukthi", "Siddharthi", "Roudhri",
            "Dhurmathi", "Dhundhubhi", "Rudhrodhgari", "Raktakshi", "Krodhana", "Akshaya"
    };

    private static final double VAKYA_SOLAR_YEAR = 365.2586805556;

    public static SamvatsaraBase getBaseForYear(int gregorianYear, int month) {
        int tamilYearIndex = gregorianYear - 1987;
        if (month < 4) {
            tamilYearIndex--;
        }
        int index60 = Math.floorMod(tamilYearIndex, 60);
        String name = SAMVATSARA_NAMES[index60];

        int kaliYear = gregorianYear + 3101;
        if (month < 4) kaliYear--;

        double chithirai1Ahargana = kaliYear * VAKYA_SOLAR_YEAR;

        double moonMeanBase = TamilCalendarUtils.normalize(257.80 + (chithirai1Ahargana * 13.1763964823));
        double marsBase     = TamilCalendarUtils.normalize(154.00 + ((chithirai1Ahargana - 1861453.0) * 0.524033));
        double jupiterBase  = TamilCalendarUtils.normalize(223.86 + ((chithirai1Ahargana - 1861453.0) * 0.083091));
        double saturnBase   = TamilCalendarUtils.normalize(321.18 + ((chithirai1Ahargana - 1861453.0) * 0.033459));
        double rahuBase     = TamilCalendarUtils.normalize(186.32 - ((chithirai1Ahargana - 1861453.0) * 0.0529539));

        return new SamvatsaraBase(name, index60, chithirai1Ahargana, moonMeanBase, marsBase, jupiterBase, saturnBase, rahuBase);
    }
}