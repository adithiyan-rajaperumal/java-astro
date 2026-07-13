package org.vedic.astro.service;

import org.springframework.stereotype.Service;
import org.vedic.astro.model.DasaPeriod;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DasaEngineService {
    private static final String[] DASA_LORDS = {"Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"};
    private static final double[] DASA_YEARS = {7, 20, 6, 10, 7, 18, 16, 19, 17};
    private static final double NAKSHATRA_ARC = 360.0 / 27.0;

    public List<DasaPeriod> calculateVimshottariTimeline(double moonAbsoluteLong, LocalDate dob) {
        List<DasaPeriod> timeline = new ArrayList<>();
        double posInNak = moonAbsoluteLong % NAKSHATRA_ARC;
        int startingLordIdx = ((int) (moonAbsoluteLong / NAKSHATRA_ARC)) % 9;
        double balanceRemainingYears = DASA_YEARS[startingLordIdx] * (1.0 - (posInNak / NAKSHATRA_ARC));

        LocalDate runningDate = dob.minusDays((long) ((DASA_YEARS[startingLordIdx] - balanceRemainingYears) * 365.25));
        int currentLordIdx = startingLordIdx;

        for (int i = 0; i < 9; i++) {
            double duration = DASA_YEARS[currentLordIdx];
            LocalDate nextDate = runningDate.plusDays((long) (duration * 365.25));
            timeline.add(DasaPeriod.builder()
                    .planetName(DASA_LORDS[currentLordIdx])
                    .startDate(runningDate).endDate(nextDate)
                    .bhukthis(calculateBhukthis(DASA_LORDS[currentLordIdx], duration, runningDate))
                    .build());
            runningDate = nextDate;
            currentLordIdx = (currentLordIdx + 1) % 9;
        }
        return timeline;
    }

    private List<DasaPeriod.BhukthiPeriod> calculateBhukthis(String lord, double years, LocalDate start) {
        List<DasaPeriod.BhukthiPeriod> list = new ArrayList<>();
        LocalDate running = start;
        int idx = 0;
        for (int i = 0; i < 9; i++) { if (DASA_LORDS[i].equals(lord)) { idx = i; break; } }
        for (int i = 0; i < 9; i++) {
            int bLordIdx = (idx + i) % 9;
            double bDuration = years * (DASA_YEARS[bLordIdx] / 120.0);
            LocalDate next = running.plusDays((long) (bDuration * 365.25));
            list.add(DasaPeriod.BhukthiPeriod.builder().planetName(DASA_LORDS[bLordIdx]).startDate(running).endDate(next).build());
            running = next;
        }
        return list;
    }
}
