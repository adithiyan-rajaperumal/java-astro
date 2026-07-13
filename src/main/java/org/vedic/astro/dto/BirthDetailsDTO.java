package org.vedic.astro.dto;

public record BirthDetailsDTO(
        String name,        // Added identity tracking variable
        int year,
        int month,
        int day,
        int hour,
        int minute,
        int second,
        double latitude,
        double longitude
) {}
