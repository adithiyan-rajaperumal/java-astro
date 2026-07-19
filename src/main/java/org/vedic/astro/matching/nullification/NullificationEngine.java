package org.vedic.astro.matching.nullification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vedic.astro.matching.StrictnessLevel;
import org.vedic.astro.matching.dto.KootaResultDTO;
import org.vedic.astro.matching.dto.KootaResultDTO.MatchStatus;
import org.vedic.astro.matching.model.MatchingContext;
import org.vedic.astro.service.TranslationService;

@Component
@RequiredArgsConstructor
public class NullificationEngine {

    private final TranslationService ts;

    public KootaResultDTO evaluateNullification(KootaResultDTO result, MatchingContext context) {
        if (result.getStatus() == MatchStatus.MATCHED) {
            return result;
        }

        StrictnessLevel strict = context.getStrictness();
        String key = result.getKey();
        boolean nullified = false;
        String reasonKey = "";

        if ("nadi".equals(key)) {
            // Same Rasi, different Nakshatras
            if (context.getBoyRashi() == context.getGirlRashi() && context.getBoyNakshatra() != context.getGirlNakshatra()) {
                nullified = true;
                reasonKey = "nullification.nadi.samerashi";
            }
            // Nakshatra lords are friends or identical
            else if (strict != StrictnessLevel.STRICT) {
                String boyNakLord = getNakshatraLord(context.getBoyNakshatra());
                String girlNakLord = getNakshatraLord(context.getGirlNakshatra());
                if (boyNakLord.equals(girlNakLord) || areFriends(boyNakLord, girlNakLord)) {
                    nullified = true;
                    reasonKey = "nullification.nadi.friends";
                }
            }
        } else if ("bhakut".equals(key) || "rasi".equals(key)) {
            String boyRashiLord = getRashiLord(context.getBoyRashi());
            String girlRashiLord = getRashiLord(context.getGirlRashi());
            
            // Rashi lords same
            if (boyRashiLord.equals(girlRashiLord)) {
                nullified = true;
                reasonKey = "nullification.bhakut.samelord";
            }
            // Rashi lords are friends
            else if (strict != StrictnessLevel.STRICT && areFriends(boyRashiLord, girlRashiLord)) {
                nullified = true;
                reasonKey = "nullification.bhakut.friends";
            }
        } else if ("gana".equals(key)) {
            int boyGana = getNakshatraGana(context.getBoyNakshatra());
            int girlGana = getNakshatraGana(context.getGirlNakshatra());
            if (boyGana == 1 && girlGana == 2) {
                nullified = true;
                reasonKey = "nullification.gana.deva_manushya";
            }
        } else if ("rajju".equals(key)) {
            if (strict != StrictnessLevel.STRICT) {
                if (context.getBoyPada() != context.getGirlPada()) {
                    nullified = true;
                    reasonKey = "nullification.rajju.diffpada";
                }
            }
        }

        if (nullified) {
            result.setStatus(MatchStatus.MATCHED_VIA_NULLIFICATION);
            result.setScoredPoints(result.getMaxPoints());
            result.setNullificationReason(ts.getLabel(reasonKey));
        }

        return result;
    }

    public static String getRashiLord(int rashi) {
        return switch (rashi) {
            case 1, 8 -> "Mars";
            case 2, 7 -> "Venus";
            case 3, 6 -> "Mercury";
            case 4 -> "Moon";
            case 5 -> "Sun";
            case 9, 12 -> "Jupiter";
            case 10, 11 -> "Saturn";
            default -> "";
        };
    }

    public static boolean areFriends(String p1, String p2) {
        if (p1.equals(p2)) return true;
        return switch (p1) {
            case "Sun" -> p2.equals("Moon") || p2.equals("Mars") || p2.equals("Jupiter");
            case "Moon" -> p2.equals("Sun") || p2.equals("Mercury");
            case "Mars" -> p2.equals("Sun") || p2.equals("Moon") || p2.equals("Jupiter");
            case "Mercury" -> p2.equals("Sun") || p2.equals("Venus");
            case "Jupiter" -> p2.equals("Sun") || p2.equals("Moon") || p2.equals("Mars");
            case "Venus" -> p2.equals("Mercury") || p2.equals("Saturn");
            case "Saturn" -> p2.equals("Mercury") || p2.equals("Venus");
            default -> false;
        };
    }

    public static boolean areFriendlyOrNeutral(String p1, String p2) {
        if (p1.equals(p2)) return true;
        if (areFriends(p1, p2)) return true;
        boolean areEnemies = switch (p1) {
            case "Sun" -> p2.equals("Venus") || p2.equals("Saturn");
            case "Moon" -> false;
            case "Mars" -> p2.equals("Mercury");
            case "Mercury" -> p2.equals("Moon");
            case "Jupiter" -> p2.equals("Mercury") || p2.equals("Venus");
            case "Venus" -> p2.equals("Sun") || p2.equals("Moon");
            case "Saturn" -> p2.equals("Sun") || p2.equals("Moon") || p2.equals("Mars");
            default -> false;
        };
        return !areEnemies;
    }

    public static String getNakshatraLord(int nak) {
        String[] lords = {"Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"};
        return lords[(nak - 1) % 9];
    }

    public static int getNakshatraGana(int nak) {
        int[] ganaMap = {
            1, 2, 3, 2, 1, 2, 1, 1, 3,
            3, 2, 2, 1, 1, 1, 3, 1, 3,
            3, 2, 2, 1, 3, 3, 2, 2, 1
        };
        return ganaMap[nak - 1];
    }
}
