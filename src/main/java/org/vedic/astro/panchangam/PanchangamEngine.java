package org.vedic.astro.panchangam;

import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.ComprehensiveReportDTO;
import org.vedic.astro.model.ChartResult;

public interface PanchangamEngine {
    ChartResult calculate(BirthDetailsDTO details);
    PanchangamType getType();
    ComprehensiveReportDTO generateComprehensiveReport(BirthDetailsDTO details,  ChartResult res);
}
