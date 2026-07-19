package org.vedic.astro.matching.porutham;

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
public class DasaPorutthamEngine implements MatchingEngine {

    private final TranslationService ts;
    private final NullificationEngine nullificationEngine;

    @Override
    public MatchingType getType() {
        return MatchingType.DASA_PORUTHAM;
    }

    @Override
    public MatchingResponseDTO calculateCompatibility(MatchingContext context) {
        List<KootaResultDTO> poruthams = new ArrayList<>();

        poruthams.add(nullificationEngine.evaluateNullification(calculateDinam(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateGanam(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateMahendram(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateStreeDeergham(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateYoni(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateRasi(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateRajju(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateVedha(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateVasya(context), context));
        poruthams.add(nullificationEngine.evaluateNullification(calculateNadi(context), context));

        double totalScore = poruthams.stream().mapToDouble(KootaResultDTO::getScoredPoints).sum();
        double maxScore = 10.0;
        double percentage = (totalScore / maxScore) * 100.0;

        String verdict;
        if (percentage >= 80.0) {
            verdict = ts.getLabel("matching.verdict.excellent");
        } else if (percentage >= 60.0) {
            verdict = ts.getLabel("matching.verdict.good");
        } else if (percentage >= 40.0) {
            verdict = ts.getLabel("matching.verdict.average");
        } else {
            verdict = ts.getLabel("matching.verdict.notrecommended");
        }

        return MatchingResponseDTO.builder()
                .totalScore(totalScore)
                .maxScore(maxScore)
                .percentage(percentage)
                .verdict(verdict)
                .kootas(poruthams)
                .warnings(new ArrayList<>())
                .build();
    }

    private KootaResultDTO calculateDinam(MatchingContext context) {
        int diff = (context.getBoyNakshatra() - context.getGirlNakshatra() + 27) % 9;
        boolean matches = (diff == 2 || diff == 4 || diff == 6 || diff == 8 || diff == 0);
        double points = matches ? 1.0 : 0.0;

        return KootaResultDTO.builder()
                .key("dinam")
                .name(ts.getLabel("porutham.dinam"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.dinam.desc"))
                .build();
    }

    private KootaResultDTO calculateGanam(MatchingContext context) {
        int boyGana = NullificationEngine.getNakshatraGana(context.getBoyNakshatra());
        int girlGana = NullificationEngine.getNakshatraGana(context.getGirlNakshatra());

        double points = 0.0;
        if (boyGana == girlGana) points = 1.0;
        else if ((boyGana == 1 && girlGana == 2) || (boyGana == 2 && girlGana == 1)) points = 1.0;
        else if (boyGana == 1 && girlGana == 3) points = 0.0;
        else if (boyGana == 3 && girlGana == 1) points = 0.5;
        else if (boyGana == 3 && girlGana == 2) points = 0.5;

        return KootaResultDTO.builder()
                .key("gana")
                .name(ts.getLabel("porutham.gana"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.gana.desc"))
                .build();
    }

    private KootaResultDTO calculateMahendram(MatchingContext context) {
        int diff = (context.getBoyNakshatra() - context.getGirlNakshatra() + 27) % 27 + 1;
        boolean matches = (diff == 4 || diff == 7 || diff == 10 || diff == 13 || diff == 16 || diff == 19 || diff == 22 || diff == 25);
        double points = matches ? 1.0 : 0.0;

        return KootaResultDTO.builder()
                .key("mahendram")
                .name(ts.getLabel("porutham.mahendram"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.mahendram.desc"))
                .build();
    }

    private KootaResultDTO calculateStreeDeergham(MatchingContext context) {
        int diff = (context.getBoyNakshatra() - context.getGirlNakshatra() + 27) % 27 + 1;
        double points = (diff > 9) ? 1.0 : 0.0;

        return KootaResultDTO.builder()
                .key("streedeergham")
                .name(ts.getLabel("porutham.streedeergham"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.streedeergham.desc"))
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

        int score = matrix[boyAnimal][girlAnimal];
        double points = 0.0;
        if (score >= 3) points = 1.0;
        else if (score == 2) points = 0.5;

        return KootaResultDTO.builder()
                .key("yoni")
                .name(ts.getLabel("porutham.yoni"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.yoni.desc"))
                .build();
    }

    private KootaResultDTO calculateRasi(MatchingContext context) {
        int diff = (context.getBoyRashi() - context.getGirlRashi() + 12) % 12 + 1;
        boolean isBad = (diff == 2 || diff == 12 || diff == 6 || diff == 8 || diff == 5 || diff == 9);
        double points = isBad ? 0.0 : 1.0;

        return KootaResultDTO.builder()
                .key("rasi")
                .name(ts.getLabel("porutham.rasi"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.rasi.desc"))
                .build();
    }

    private KootaResultDTO calculateRajju(MatchingContext context) {
        int boyRajju = getRajju(context.getBoyNakshatra());
        int girlRajju = getRajju(context.getGirlNakshatra());

        double points = (boyRajju != girlRajju) ? 1.0 : 0.0;
        return KootaResultDTO.builder()
                .key("rajju")
                .name(ts.getLabel("porutham.rajju"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.rajju.desc"))
                .build();
    }

    private KootaResultDTO calculateVedha(MatchingContext context) {
        boolean hasVedha = hasVedha(context.getBoyNakshatra(), context.getGirlNakshatra());
        double points = hasVedha ? 0.0 : 1.0;

        return KootaResultDTO.builder()
                .key("vedha")
                .name(ts.getLabel("porutham.vedha"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.vedha.desc"))
                .build();
    }

    private KootaResultDTO calculateVasya(MatchingContext context) {
        double points = getVasyaPoints(context.getBoyRashi(), context.getGirlRashi());
        return KootaResultDTO.builder()
                .key("vasya")
                .name(ts.getLabel("porutham.vasya"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.vasya.desc"))
                .build();
    }

    private KootaResultDTO calculateNadi(MatchingContext context) {
        int boyNadi = (context.getBoyNakshatra() - 1) % 3 + 1;
        int girlNadi = (context.getGirlNakshatra() - 1) % 3 + 1;

        double points = (boyNadi != girlNadi) ? 1.0 : 0.0;
        return KootaResultDTO.builder()
                .key("nadi")
                .name(ts.getLabel("porutham.nadi"))
                .maxPoints(1.0)
                .scoredPoints(points)
                .status(points == 1.0 ? MatchStatus.MATCHED : MatchStatus.NOT_MATCHED)
                .description(ts.getLabel("porutham.nadi.desc"))
                .build();
    }

    private int getYoniAnimal(int nak) {
        int[] animalMap = {
            0, 1, 2, 3, 3, 4, 5, 2, 5,
            6, 6, 7, 8, 9, 8, 10, 13, 13,
            4, 12, 11, 12, 9, 0, 10, 7, 1
        };
        return animalMap[nak - 1];
    }

    private int getRajju(int nak) {
        return switch (nak) {
            case 5, 14, 23 -> 4; // Siras
            case 4, 6, 13, 15, 22, 24 -> 3; // Kantham
            case 3, 7, 12, 16, 21, 25 -> 2; // Nabhi
            case 2, 8, 11, 17, 20, 26 -> 1; // Uru
            case 1, 9, 10, 18, 19, 27 -> 0; // Padam
            default -> 0;
        };
    }

    private boolean hasVedha(int nak1, int nak2) {
        int[][] pairs = {
            {1, 18}, {2, 17}, {3, 16}, {4, 15},
            {6, 22}, {7, 21}, {8, 20}, {9, 19},
            {10, 27}, {11, 26}, {12, 25}, {13, 24}
        };
        for (int[] p : pairs) {
            if ((nak1 == p[0] && nak2 == p[1]) || (nak1 == p[1] && nak2 == p[0])) {
                return true;
            }
        }
        if (nak1 == 14 && nak2 == 14) return true;
        if (nak1 == 5 && nak2 == 23) return true;
        if (nak1 == 23 && nak2 == 5) return true;
        return false;
    }

    private double getVasyaPoints(int boyRashi, int girlRashi) {
        boolean bToG = isVasyaTo(boyRashi, girlRashi);
        boolean gToB = isVasyaTo(girlRashi, boyRashi);
        if (bToG && gToB) return 1.0;
        if (bToG || gToB) return 0.5;
        return 0.0;
    }

    private boolean isVasyaTo(int r1, int r2) {
        return switch (r1) {
            case 1 -> r2 == 5 || r2 == 8;
            case 2 -> r2 == 4 || r2 == 7;
            case 3 -> r2 == 6;
            case 4 -> r2 == 8 || r2 == 9;
            case 5 -> r2 == 7;
            case 6 -> r2 == 12 || r2 == 3;
            case 7 -> r2 == 10 || r2 == 6;
            case 8 -> r2 == 2;
            case 9 -> r2 == 12;
            case 10 -> r2 == 1 || r2 == 11;
            case 11 -> r2 == 1;
            case 12 -> r2 == 10;
            default -> false;
        };
    }
}
