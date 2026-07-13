package org.vedic.astro.service;

import org.springframework.stereotype.Service;
import org.vedic.astro.dto.ShadbalaDTO;
import org.vedic.astro.model.PlanetaryPosition;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShadbalaService {
    public ShadbalaDTO calculateShadbala(Map<String, PlanetaryPosition> d1Map) {
        Map<String, ShadbalaDTO.PlanetaryStrength> results = new LinkedHashMap<>();
        d1Map.forEach((name, pos) -> {
            if ("Lagna".equalsIgnoreCase(name)) return;
            double sthana = 150.0 + (pos.getSignNumber() % 3 * 20);
            double dig = 50.0 + (pos.getAbsoluteLongitude() % 10 * 3.5);
            double total = sthana + dig + 240.0;
            double rupas = total / 60.0;
            results.put(name, ShadbalaDTO.PlanetaryStrength.builder()
                    .sthanaBala(sthana).digBala(dig).kalaBala(60.0).cheshtaBala(45.0).drigBala(15.0)
                    .totalShadbalaRupas(rupas).strengthCategory(rupas > 6.5 ? "Very Strong" : "Optimum").build());
        });
        return ShadbalaDTO.builder().planetStrengths(results).build();
    }
}
