package org.vedic.astro.model;

import de.thmac.swisseph.SweConst;

public enum AyanamsaType {
    LAHIRI(SweConst.SE_SIDM_LAHIRI),
    RAMAN(SweConst.SE_SIDM_RAMAN),
    KP(SweConst.SE_SIDM_KRISHNAMURTI),
    SURYA_SIDDHANTA(21),
    PUSHYAPAKSHA(29);

    private final int mode;

    AyanamsaType(int mode) {
        this.mode = mode;
    }

    public int getMode() {
        return mode;
    }

    public static AyanamsaType fromString(String val) {
        if (val == null || val.trim().isEmpty()) {
            return LAHIRI;
        }
        String clean = val.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        if (clean.contains("SURYA") || clean.contains("SIDDHANT")) {
            return SURYA_SIDDHANTA;
        }
        if (clean.contains("PUSHYA")) {
            return PUSHYAPAKSHA;
        }
        if (clean.contains("KP") || clean.contains("KRISHNAMURTI")) {
            return KP;
        }
        if (clean.contains("RAMAN")) {
            return RAMAN;
        }
        try {
            return AyanamsaType.valueOf(clean);
        } catch (IllegalArgumentException e) {
            return LAHIRI;
        }
    }
}
