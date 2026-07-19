package org.vedic.astro.matching;

import org.vedic.astro.matching.dto.MatchingResponseDTO;
import org.vedic.astro.matching.model.MatchingContext;

public interface MatchingEngine {
    MatchingResponseDTO calculateCompatibility(MatchingContext context);
    MatchingType getType();
}
