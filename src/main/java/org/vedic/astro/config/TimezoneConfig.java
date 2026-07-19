package org.vedic.astro.config;

import lombok.extern.slf4j.Slf4j;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;

@Configuration
@Slf4j
public class TimezoneConfig {

    @Bean
    public TimeZoneEngine timeZoneEngine() {
        log.info("Loading subset of South Asian timezone polygons into JVM memory...");
        long startTime = System.currentTimeMillis();

        Set<ZoneId> targetZones = new HashSet<>();
        targetZones.add(ZoneId.of("Asia/Kolkata"));
        targetZones.add(ZoneId.of("Asia/Colombo"));
        targetZones.add(ZoneId.of("Asia/Kathmandu"));
        targetZones.add(ZoneId.of("Asia/Dhaka"));
        targetZones.add(ZoneId.of("Asia/Karachi"));

        TimeZoneEngine engine = TimeZoneEngine.initialize(targetZones, true);

        log.info("TimeZoneEngine fully initialized in {} ms.", (System.currentTimeMillis() - startTime));
        return engine;
    }
}
