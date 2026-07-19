package org.vedic.astro.matching.chart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vedic.astro.matching.model.MatchingContext;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.service.TranslationService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChartAugmentedAnalysis {

    private final TranslationService ts;

    public List<String> runComparativeAnalysis(MatchingContext context) {
        List<String> warnings = new ArrayList<>();

        PlanetaryPosition boyMars = context.getBoyChart().getD1Positions().get("Mars");
        PlanetaryPosition girlMars = context.getGirlChart().getD1Positions().get("Mars");

        boolean boyManglik = isManglik(boyMars, context.getBoyLagnaSign());
        boolean girlManglik = isManglik(girlMars, context.getGirlLagnaSign());

        if (boyManglik && !girlManglik) {
            warnings.add(ts.getLabel("matching.warning.boymanglik"));
        } else if (!boyManglik && girlManglik) {
            warnings.add(ts.getLabel("matching.warning.girlmanglik"));
        }

        return warnings;
    }

    private boolean isManglik(PlanetaryPosition mars, int lagnaSign) {
        if (mars == null) return false;
        int marsHouse = ((mars.getSignNumber() - lagnaSign + 12) % 12) + 1;
        boolean hasMarsAffliction = (marsHouse == 1 || marsHouse == 2 || marsHouse == 4 || marsHouse == 7 || marsHouse == 8 || marsHouse == 12);
        boolean isCancelled = (mars.getSignNumber() == 4 || mars.getSignNumber() == 8 || mars.getSignNumber() == 10);
        return hasMarsAffliction && !isCancelled;
    }
}
