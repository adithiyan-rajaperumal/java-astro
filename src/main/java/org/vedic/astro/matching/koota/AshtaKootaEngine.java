package org.vedic.astro.matching.koota;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vedic.astro.matching.MatchingEngine;
import org.vedic.astro.matching.MatchingType;
import org.vedic.astro.matching.dto.KootaResultDTO;
import org.vedic.astro.matching.dto.KootaResultDTO.MatchStatus;
import org.vedic.astro.matching.dto.MatchingResponseDTO;
import org.vedic.astro.matching.model.MatchingContext;
import org.vedic.astro.matching.nullification.NullificationEngine;
import org.vedic.astro.service.TranslationService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AshtaKootaEngine implements MatchingEngine {

    private final TranslationService ts;
    private final NullificationEngine nullificationEngine;

    @Override
    public MatchingType getType() {
        return MatchingType.ASHTA_KOOTA;
    }

    @Override
    public MatchingResponseDTO calculateCompatibility(MatchingContext context) {
        List<KootaResultDTO> kootas = new ArrayList<>();

        kootas.add(nullificationEngine.evaluateNullification(calculateVarna(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateVashya(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateTara(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateYoni(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateGrahaMaitri(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateGana(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateBhakut(context), context));
        kootas.add(nullificationEngine.evaluateNullification(calculateNadi(context), context));

        double totalScore = kootas.stream().mapToDouble(KootaResultDTO::getScoredPoints).sum();
        double maxScore = 36.0;
        double percentage = (totalScore / maxScore) * 100.0;

        String verdict;
        if (percentage >= 80.0) {
            verdict = ts.getLabel("matching.verdict.excellent");
        } else if (percentage >= 50.0) {
            verdict = ts.getLabel("matching.verdict.good");
        } else if (percentage >= 36.0) {
            verdict = ts.getLabel("matching.verdict.average");
        } else {
            verdict = ts.getLabel("matching.verdict.notrecommended");
        }

        return MatchingResponseDTO.builder()
                .totalScore(totalScore)
                .maxScore(maxScore)
                .percentage(percentage)
                .verdict(verdict)
                .kootas(kootas)
                .warnings(new ArrayList<>())
                .build();
    }

    private KootaResultDTO calculateVarna(MatchingContext context) {
        int boyVarna = getVarnaClass(context.getBoyRashi());
        int girlVarna = getVarnaClass(context.getGirlRashi());
        double points = (boyVarna >= girlVarna) ? 1.0 : 0.0;
        return KootaResultDTO.builder()
                .key("varna")
                .name(ts.getLabel("koota.varna"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.varna.desc"))
                .build();
    }

    private KootaResultDTO calculateVashya(MatchingContext context) {
        int boyGroup = getVashyaGroup(context.getBoyRashi(), context.getBoyChart().getD1Positions().get("Moon").getDegreeInSign());
        int girlGroup = getVashyaGroup(context.getGirlRashi(), context.getGirlChart().getD1Positions().get("Moon").getDegreeInSign());

        double[][] matrix = {
            {2, 1, 1, 0, 1},
            {1, 2, 1, 0, 1},
            {1, 1, 2, 1, 1},
            {0, 0, 1, 2, 0},
            {1, 1, 1, 0, 2}
        };

        double points = matrix[boyGroup][girlGroup];
        return KootaResultDTO.builder()
                .key("vashya")
                .name(ts.getLabel("koota.vashya"))
                .maxPoints(2.0)
                .scoredPoints(points)
                .status(points == 2.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.vashya.desc"))
                .build();
    }

    private KootaResultDTO calculateTara(MatchingContext context) {
        int diff1 = (context.getBoyNakshatra() - context.getGirlNakshatra() + 27) % 9;
        int diff2 = (context.getGirlNakshatra() - context.getBoyNakshatra() + 27) % 9;

        boolean boyGood = (diff1 != 3 && diff1 != 5 && diff1 != 7);
        boolean girlGood = (diff2 != 3 && diff2 != 5 && diff2 != 7);

        double points = 0.0;
        if (boyGood && girlGood) points = 3.0;
        else if (boyGood || girlGood) points = 1.5;

        return KootaResultDTO.builder()
                .key("tara")
                .name(ts.getLabel("koota.tara"))
                .maxPoints(3.0)
                .scoredPoints(points)
                .status(points == 3.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.tara.desc"))
                .build();
    }

    private KootaResultDTO calculateYoni(MatchingContext context) {
        int boyAnimal = getYoniAnimal(context.getBoyNakshatra());
        int girlAnimal = getYoniAnimal(context.getGirlNakshatra());

        int[][] matrix = {
            {4, 2, 2, 3, 2, 2, 2, 2, 0, 1, 1, 2, 2, 3},
            {2, 4, 3, 3, 2, 2, 2, 2, 2, 1, 0, 2, 1, 2},
            {2, 3, 4, 2, 1, 2, 2, 2, 2, 1, 1, 2, 0, 3},
            {3, 3, 2, 4, 2, 1, 1, 1, 1, 2, 2, 0, 2, 2},
            {2, 2, 1, 2, 4, 2, 1, 2, 2, 1, 1, 2, 1, 0},
            {2, 2, 2, 1, 2, 4, 0, 2, 2, 1, 1, 2, 2, 1},
            {2, 2, 2, 1, 1, 0, 4, 2, 2, 2, 2, 1, 2, 1},
            {2, 2, 2, 1, 2, 2, 2, 4, 3, 0, 1, 2, 2, 2},
            {0, 2, 2, 1, 2, 2, 2, 3, 4, 1, 1, 2, 2, 2},
            {1, 1, 1, 2, 1, 1, 2, 0, 1, 4, 2, 2, 1, 2},
            {1, 0, 1, 2, 1, 1, 2, 1, 1, 2, 4, 2, 1, 2},
            {2, 2, 2, 0, 2, 2, 1, 2, 2, 2, 2, 4, 2, 2},
            {2, 1, 0, 2, 1, 2, 2, 2, 2, 1, 1, 2, 4, 2},
            {3, 2, 3, 2, 0, 1, 1, 2, 2, 2, 2, 2, 2, 4}
        };

        double points = matrix[boyAnimal][girlAnimal];
        return KootaResultDTO.builder()
                .key("yoni")
                .name(ts.getLabel("koota.yoni"))
                .maxPoints(4.0)
                .scoredPoints(points)
                .status(points == 4.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.yoni.desc"))
                .build();
    }

    private KootaResultDTO calculateGrahaMaitri(MatchingContext context) {
        String boyLord = NullificationEngine.getRashiLord(context.getBoyRashi());
        String girlLord = NullificationEngine.getRashiLord(context.getGirlRashi());
        double points = getGrahaMaitriPoints(boyLord, girlLord);
        return KootaResultDTO.builder()
                .key("grahamaitri")
                .name(ts.getLabel("koota.grahamaitri"))
                .maxPoints(5.0)
                .scoredPoints(points)
                .status(points == 5.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.grahamaitri.desc"))
                .build();
    }

    private KootaResultDTO calculateGana(MatchingContext context) {
        int boyGana = NullificationEngine.getNakshatraGana(context.getBoyNakshatra());
        int girlGana = NullificationEngine.getNakshatraGana(context.getGirlNakshatra());

        double points = 0.0;
        if (boyGana == girlGana) points = 6.0;
        else if ((boyGana == 1 && girlGana == 2) || (boyGana == 2 && girlGana == 1)) points = 5.0;
        else if (boyGana == 3 && girlGana == 2) points = 1.0;

        return KootaResultDTO.builder()
                .key("gana")
                .name(ts.getLabel("koota.gana"))
                .maxPoints(6.0)
                .scoredPoints(points)
                .status(points == 6.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.gana.desc"))
                .build();
    }

    private KootaResultDTO calculateBhakut(MatchingContext context) {
        int diff = (context.getBoyRashi() - context.getGirlRashi() + 12) % 12 + 1;

        boolean isBad = (diff == 2 || diff == 12 || diff == 6 || diff == 8 || diff == 5 || diff == 9);
        double points = isBad ? 0.0 : 7.0;

        return KootaResultDTO.builder()
                .key("bhakut")
                .name(ts.getLabel("koota.bhakut"))
                .maxPoints(7.0)
                .scoredPoints(points)
                .status(points == 7.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.bhakut.desc"))
                .build();
    }

    private KootaResultDTO calculateNadi(MatchingContext context) {
        int boyNadi = (context.getBoyNakshatra() - 1) % 3 + 1;
        int girlNadi = (context.getGirlNakshatra() - 1) % 3 + 1;

        double points = (boyNadi != girlNadi) ? 8.0 : 0.0;
        return KootaResultDTO.builder()
                .key("nadi")
                .name(ts.getLabel("koota.nadi"))
                .maxPoints(8.0)
                .scoredPoints(points)
                .status(points == 8.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("koota.nadi.desc"))
                .build();
    }

    private int getVarnaClass(int rashi) {
        return switch (rashi) {
            case 4, 8, 12 -> 4;
            case 1, 5, 9 -> 3;
            case 2, 6, 10 -> 2;
            case 3, 7, 11 -> 1;
            default -> 1;
        };
    }

    private int getVashyaGroup(int rashi, double degree) {
        return switch (rashi) {
            case 1, 2 -> 0;
            case 3, 6, 7, 11 -> 1;
            case 4, 12 -> 2;
            case 5 -> 3;
            case 8 -> 4;
            case 9 -> (degree < 15.0) ? 0 : 1;
            case 10 -> (degree < 15.0) ? 2 : 0;
            default -> 1;
        };
    }

    private int getYoniAnimal(int nak) {
        int[] animalMap = {
            0, 1, 2, 3, 3, 4, 5, 2, 5,
            6, 6, 7, 8, 9, 8, 10, 13, 13,
            4, 12, 11, 12, 9, 0, 10, 7, 1
        };
        return animalMap[nak - 1];
    }

    private double getGrahaMaitriPoints(String l1, String l2) {
        if (l1.equals(l2)) return 5.0;
        boolean f1 = NullificationEngine.areFriends(l1, l2);
        boolean f2 = NullificationEngine.areFriends(l2, l1);
        boolean n1 = isNeutral(l1, l2);
        boolean n2 = isNeutral(l2, l1);

        if (f1 && f2) return 5.0;
        if ((f1 && n2) || (f2 && n1)) return 4.0;
        if (n1 && n2) return 3.0;
        if ((f1 && !f2 && !n2) || (f2 && !f1 && !n1)) return 2.0;
        if ((n1 && !f2 && !n2) || (n2 && !f1 && !n1)) return 1.0;
        return 0.0;
    }

    private boolean isNeutral(String p1, String p2) {
        return !NullificationEngine.areFriends(p1, p2) && !isEnemy(p1, p2);
    }

    private boolean isEnemy(String p1, String p2) {
        return switch (p1) {
            case "Sun" -> p2.equals("Venus") || p2.equals("Saturn");
            case "Moon" -> false;
            case "Mars" -> p2.equals("Mercury");
            case "Mercury" -> p2.equals("Moon");
            case "Jupiter" -> p2.equals("Mercury") || p2.equals("Venus");
            case "Venus" -> p2.equals("Sun") || p2.equals("Moon");
            case "Saturn" -> p2.equals("Sun") || p2.equals("Moon") || p2.equals("Mars");
            default -> false;
        };
    }
}
