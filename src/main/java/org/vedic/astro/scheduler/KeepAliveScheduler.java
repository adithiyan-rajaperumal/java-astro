package org.vedic.astro.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class KeepAliveScheduler {

    private final RestTemplate restTemplate = new RestTemplate();

    // Ping every 5 minutes (300,000 ms) to comfortably stay within Render's 15-minute inactivity window
    @Scheduled(fixedRate = 300000)
    public void keepAlive() {
        try {
            String externalUrl = System.getenv("RENDER_EXTERNAL_URL");
            String targetUrl;
            if (externalUrl != null && !externalUrl.isBlank()) {
                targetUrl = externalUrl.replaceAll("/+$", "") + "/api/health";
            } else {
                String port = System.getenv().getOrDefault("PORT", "8080");
                targetUrl = "http://localhost:" + port + "/api/health";
            }
            
            restTemplate.getForObject(targetUrl, String.class);
            log.info("Keep-alive ping successful to {}", targetUrl);
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
