package org.vedic.astro.dto;

public record PanchangamRequestDTO(
    String date,
    double latitude,
    double longitude,
    String language,
    String ayanamsa
) {}
