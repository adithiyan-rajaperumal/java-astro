package org.vedic.astro.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosticsDTO {
    private List<YogaDetail> activeYogas;
    private List<DoshaDetail> discoveredDoshams;
    private List<String> horoscopicSpecialities;

    @Data
    @Builder
    public static class YogaDetail {
        private String name;
        private String description;
        private String impactLevel;
    }

    @Data
    @Builder
    public static class DoshaDetail {
        private String name;
        private boolean detected;
        private boolean active;
        private boolean nullified;
        private String severity;
        private String nullificationReason;
        private String remedySuggestion;
    }
}
