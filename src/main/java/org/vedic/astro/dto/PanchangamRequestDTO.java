package org.vedic.astro.dto;

public record PanchangamRequestDTO(
    String date,
    double latitude,
    double longitude,
    String language,
    String ayanamsa,
    String panchangamSystem
) {
    public PanchangamRequestDTO(String date, double latitude, double longitude, String language, String ayanamsa) {
        this(date, latitude, longitude, language, ayanamsa, "DRIK_TIRUKANITHAM");
    }
}
