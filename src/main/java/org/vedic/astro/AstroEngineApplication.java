package org.vedic.astro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AstroEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstroEngineApplication.class, args);
    }
}