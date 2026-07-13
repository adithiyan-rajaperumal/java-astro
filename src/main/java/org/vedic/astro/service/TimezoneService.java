package org.vedic.astro.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.ZoneId;
import java.util.Optional;

@Service
@Slf4j
public class TimezoneService {
    private final TimeZoneEngine localTzEngine;
    private final RestClient restClient;

    public TimezoneService(TimeZoneEngine localTzEngine) {
        this.localTzEngine = localTzEngine;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.open-meteo.com/v1")
                .build();
    }

    public String getTimezoneFromCoordinates(double latitude, double longitude) {
        try {
            Optional<ZoneId> resolvedZone = localTzEngine.query(latitude, longitude);
            if (resolvedZone.isPresent()) {
                return resolvedZone.get().getId();
            }
        } catch (Exception e) {
            log.error("Offline spatial boundary resolution faulted. Dropping to API routing.", e);
        }

        try {
            OpenMeteoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("latitude", latitude)
                            .queryParam("longitude", longitude)
                            .queryParam("timezone", "auto")
                            .build())
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (response != null && response.timezone() != null) {
                return response.timezone();
            }
        } catch (Exception e) {
            log.error("Cloud Timezone API gateway call failed.", e);
        }

        return "Asia/Kolkata";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record OpenMeteoResponse(String timezone) {}
}
