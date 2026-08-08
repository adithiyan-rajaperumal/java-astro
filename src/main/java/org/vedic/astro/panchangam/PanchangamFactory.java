package org.vedic.astro.panchangam;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.vedic.astro.panchangam.impl.DrikPanchangamEngine;

@Component
@RequiredArgsConstructor
public class PanchangamFactory {

    private final DrikPanchangamEngine drikPanchangamEngine;

    public PanchangamEngine getEngine(PanchangamType type) {
        return drikPanchangamEngine;
    }
}
