package org.vedic.astro.service;

import org.springframework.stereotype.Service;
import org.vedic.astro.dto.DiagnosticsDTO;
import org.vedic.astro.model.PlanetaryPosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AstrologyDiagnosticsService {
    public DiagnosticsDTO runHoroscopeDiagnostics(Map<String, PlanetaryPosition> d1Map) {
        List<DiagnosticsDTO.YogaDetail> yogas = new ArrayList<>();
        List<DiagnosticsDTO.DoshaDetail> doshams = new ArrayList<>();
        List<String> specs = new ArrayList<>();

        PlanetaryPosition lagna = d1Map.get("Lagna"); PlanetaryPosition sun = d1Map.get("Sun");
        PlanetaryPosition moon = d1Map.get("Moon"); PlanetaryPosition mars = d1Map.get("Mars");
        PlanetaryPosition rahu = d1Map.get("Rahu"); PlanetaryPosition ketu = d1Map.get("Ketu");
        PlanetaryPosition saturn = d1Map.get("Saturn"); int lagnaSign = lagna.getSignNumber();

        // 1. Sevvai Dosham
        int marsH = ((mars.getSignNumber() - lagnaSign + 12) % 12) + 1;
        boolean hasMars = (marsH == 1 || marsH == 2 || marsH == 4 || marsH == 7 || marsH == 8 || marsH == 12);
        boolean marsCan = (mars.getSignNumber() == 4 || mars.getSignNumber() == 8 || mars.getSignNumber() == 10);
        doshams.add(DiagnosticsDTO.DoshaDetail.builder().name("Sevvai Dosham (Mars Affliction)").active(hasMars && !marsCan)
                .severity(marsCan ? "Cancelled" : "High").remedySuggestion("Perform prayers at Vaideeswaran Koil.").build());

        // 2. Sarpam Dosham
        int rH = ((rahu.getSignNumber() - lagnaSign + 12) % 12) + 1; int kH = ((ketu.getSignNumber() - lagnaSign + 12) % 12) + 1;
        boolean hasSarpam = (rH == 1 || rH == 2 || rH == 5 || rH == 7 || rH == 8 || kH == 1 || kH == 2 || kH == 5 || kH == 7 || kH == 8);
        doshams.add(DiagnosticsDTO.DoshaDetail.builder().name("Sarpam / Naga Dosham").active(hasSarpam).severity(hasSarpam ? "Medium" : "None")
                .remedySuggestion("Offer prayers during Rahu Kalam.").build());

        // 3. Kala Sarpa Dosham
        double rLong = rahu.getAbsoluteLongitude(); double kLong = ketu.getAbsoluteLongitude();
        double mn = Math.min(rLong, kLong); double mx = Math.max(rLong, kLong);
        boolean inside = true; boolean outside = true;
        for (var e : d1Map.entrySet()) {
            if ("Lagna".equals(e.getKey()) || "Rahu".equals(e.getKey()) || "Ketu".equals(e.getKey())) continue;
            double p = e.getValue().getAbsoluteLongitude();
            if (p < mn || p > mx) inside = false; if (p > mn && p < mx) outside = false;
        }
        boolean hasKala = (inside || outside);
        doshams.add(DiagnosticsDTO.DoshaDetail.builder().name("Kala Sarpa Dosham").active(hasKala).severity(hasKala ? "High" : "None")
                .remedySuggestion("Cultivate patience and discipline workflows.").build());

        // 4. Pithru Dosham
        int sH = ((sun.getSignNumber() - lagnaSign + 12) % 12) + 1;
        boolean sAff = (sun.getSignNumber() == rahu.getSignNumber() || sun.getSignNumber() == ketu.getSignNumber() || sun.getSignNumber() == saturn.getSignNumber());
        boolean hasPithru = (sH == 9 && sAff);
        doshams.add(DiagnosticsDTO.DoshaDetail.builder().name("Pithru Dosham").active(hasPithru).severity(hasPithru ? "Medium" : "None")
                .remedySuggestion("Honor parental lineages and care for elders.").build());

        return DiagnosticsDTO.builder().activeYogas(yogas).discoveredDoshams(doshams).horoscopicSpecialities(specs).build();
    }
}
