package org.vedic.astro.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KootaResultDTO {
    public enum MatchStatus {
        MATCHED,
        MATCHED_VIA_NULLIFICATION,
        NOT_MATCHED
    }

    private String key;
    private String name;
    private double maxPoints;
    private double scoredPoints;
    private MatchStatus status;
    private String description;
    private String nullificationReason;
}
