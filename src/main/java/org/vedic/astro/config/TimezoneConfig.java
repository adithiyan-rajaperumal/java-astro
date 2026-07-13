package org.vedic.astro.config;

import lombok.extern.slf4j.Slf4j;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TimezoneConfig {

    @Bean
    public TimeZoneEngine timeZoneEngine() {
        log.info("Loading global OpenStreetMap timezone polygon charts into JVM memory...");
        long startTime = System.currentTimeMillis();

        TimeZoneEngine engine = TimeZoneEngine.initialize();

        log.info("TimeZoneEngine fully initialized in {} ms.", (System.currentTimeMillis() - startTime));
        return engine;
    }
}
