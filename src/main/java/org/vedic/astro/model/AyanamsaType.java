package org.vedic.astro.model;

import de.thmac.swisseph.SweConst;
import de.thmac.swisseph.SwissEph;

public enum AyanamsaType {
    LAHIRI(SweConst.SE_SIDM_LAHIRI),
    RAMAN(SweConst.SE_SIDM_RAMAN),
    KP(SweConst.SE_SIDM_KRISHNAMURTI),
    SURYA_SIDDHANTA(21),
    PUSHYAPAKSHA(SweConst.SE_SIDM_USER);

    private final int mode;

    AyanamsaType(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public void applyTo(SwissEph swissEph) {
        if (this == PUSHYAPAKSHA) {
            // Standard True Pushyapaksha (JHora match 22°39'36.55" / 22-39-34.95)
            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_USER, 2451545.0, 22.72238333);
        } else if (this == SURYA_SIDDHANTA) {
            // Surya Siddhanta Ayanamsa (JHora exact match 22°26'37.88")
            swissEph.swe_set_sid_mode(SweConst.SE_SIDM_USER, 2451545.0, 22.50608611);
        } else {
            swissEph.swe_set_sid_mode(this.mode, 0, 0);
        }
    }

    public void applyTo(SwissEph swissEph, org.vedic.astro.panchangam.PanchangamType pType) {
        applyTo(swissEph);
    }

    public static AyanamsaType fromString(String val) {
        if (val == null || val.trim().isEmpty()) {
            return LAHIRI;
        }
        String clean = val.trim().toUpperCase().replace("-", "_").replace(" ", "_").replace(".", "");
        if (clean.contains("SURYA") || clean.contains("SIDDHANT")) {
            return SURYA_SIDDHANTA;
        }
        if (clean.contains("PUSHYA")) {
            return PUSHYAPAKSHA;
        }
        if (clean.contains("KP") || clean.contains("KRISHNAMURTI")) {
            return KP;
        }
        if (clean.contains("RAMAN") || clean.contains("BV")) {
            return RAMAN;
        }
        try {
            return AyanamsaType.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return LAHIRI;
        }
    }
}
