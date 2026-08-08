package org.vedic.astro.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosticsDTO {
    private List<YogaDetail> activeYogas;
    private List<DoshaDetail> discoveredDoshams;
    private List<String> horoscopicSpecialities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YogaDetail {
        private String name;
        private String description;
        private String impactLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
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
