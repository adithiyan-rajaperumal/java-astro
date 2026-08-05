package org.vedic.astro.panchangam;

public enum PanchangamType {
    DRIK_TIRUKANITHAM,
    VAKYA,
    PARASARA_BHATTAR,
    SURYA_SIDDHANTA;

    public static PanchangamType fromString(String val) {
        if (val == null || val.isBlank()) return DRIK_TIRUKANITHAM;
        String clean = val.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (clean.contains("VAKYA") || clean.contains("VAKKIYAM")) return VAKYA;
        if (clean.contains("PARASARA") || clean.contains("BHATTAR") || clean.contains("BATTAR")) return PARASARA_BHATTAR;
        if (clean.contains("SURYA") || clean.contains("SIDDHANT")) return SURYA_SIDDHANTA;
        return DRIK_TIRUKANITHAM;
    }
}
