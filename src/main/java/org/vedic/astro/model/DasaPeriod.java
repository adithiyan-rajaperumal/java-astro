package org.vedic.astro.model;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DasaPeriod {
    private String planetName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<BhukthiPeriod> bhukthis;

    @Data
    @Builder
    public static class BhukthiPeriod {
        private String planetName;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
