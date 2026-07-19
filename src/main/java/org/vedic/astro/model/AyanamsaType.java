package org.vedic.astro.model;

import de.thmac.swisseph.SweConst;

public enum AyanamsaType {
    LAHIRI(SweConst.SE_SIDM_LAHIRI),
    RAMAN(SweConst.SE_SIDM_RAMAN),
    KP(SweConst.SE_SIDM_KRISHNAMURTI);

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
        try {
            return AyanamsaType.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return LAHIRI;
        }
    }
}
