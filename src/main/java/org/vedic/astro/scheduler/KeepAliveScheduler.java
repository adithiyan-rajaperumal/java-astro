package org.vedic.astro.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KeepAliveScheduler {
    @Scheduled(fixedRate = 840000) // 14 minutes
    public void keepAlive() {
        try {
            String port = System.getenv().getOrDefault("PORT", "8080");
            new RestTemplate().getForObject("http://localhost:" + port + "/api/health", String.class);
            log.debug("Keep-alive ping successful");
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
