package org.vedic.astro.dto;

import org.vedic.astro.util.NumerologyUtils;
import org.vedic.astro.util.SpiritualDeityUtils;
import org.vedic.astro.util.GemologyEngineUtils;
import org.vedic.astro.util.StructuralAnchorsUtils;

/**
 * Immutable Data Record aggregating all Personal Elements, Deities & Life Anchors.
 */
public record LifeAnchorsProfile(
        NumerologyUtils.NumerologyResult numerology,
        StructuralAnchorsUtils.LuckyDayResult luckyDay,
        NumerologyUtils.LuckyDatesResult luckyDates,
        StructuralAnchorsUtils.AuspiciousDirectionsResult directions,
        SpiritualDeityUtils.SpiritualDeitiesResult deities,
        GemologyEngineUtils.GemologyResult gemology,
        StructuralAnchorsUtils.StructuralAnchorsResult structuralAnchors
) {}
