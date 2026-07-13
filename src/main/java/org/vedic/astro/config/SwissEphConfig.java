package org.vedic.astro.config;

import de.thmac.swisseph.SweConst;
import de.thmac.swisseph.SwissEph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Configuration
@Slf4j
public class SwissEphConfig {

    @Bean
    public SwissEph swissEph() {
        try {
            // 1. Generate an isolated temporary directory on the host system
            File tempEpheDir = Files.createTempDirectory("swisseph-ephe").toFile();
            tempEpheDir.deleteOnExit(); // Automatically clean up directory files when JVM shuts down

            // 2. Scan for all internal target ephemeris files inside the classpath container
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:ephe/*.se1");

            if (resources.length == 0) {
                log.warn("CRITICAL: No .se1 files detected inside src/main/resources/ephe/! Falling back to analytical Moshier calculations.");
            }

            // 3. Stream resource blocks out of the packed JAR archive onto the real disk path
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename != null) {
                    File targetFile = new File(tempEpheDir, filename);
                    try (InputStream is = resource.getInputStream()) {
                        Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        log.info("Successfully extracted ephemeris sheet to temp engine path: {}", targetFile.getAbsolutePath());
                    }
                }
            }

            // 4. Instantiate the engine with the dynamic disk file system path reference
            SwissEph modernEph = new SwissEph(tempEpheDir.getAbsolutePath());
            modernEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);

            log.info("Swiss Ephemeris calculation pipeline successfully bound to internal classpath resources via temp mirror.");
            return modernEph;

        } catch (Exception e) {
            log.error("Failed to initialize embedded ephemeris data. Initializing default baseline constructor.", e);
            SwissEph fallbackEph = new SwissEph();
            fallbackEph.swe_set_sid_mode(SweConst.SE_SIDM_LAHIRI, 0, 0);
            return fallbackEph;
        }
    }
}