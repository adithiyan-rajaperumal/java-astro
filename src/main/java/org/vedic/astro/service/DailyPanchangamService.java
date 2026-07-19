package org.vedic.astro.service;

import org.vedic.astro.dto.DailyPanchangamDTO;
import org.vedic.astro.dto.PanchangamRequestDTO;

public interface DailyPanchangamService {
    DailyPanchangamDTO calculateDailyPanchangam(PanchangamRequestDTO request);
}
