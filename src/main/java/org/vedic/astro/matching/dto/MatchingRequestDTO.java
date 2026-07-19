package org.vedic.astro.matching.dto;

import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.matching.MatchingType;
import org.vedic.astro.matching.StrictnessLevel;

public record MatchingRequestDTO(
        BirthDetailsDTO boy,
        BirthDetailsDTO girl,
        MatchingType matchingSystem,
        StrictnessLevel strictness
) {}
