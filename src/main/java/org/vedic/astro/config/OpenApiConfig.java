package org.vedic.astro.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Jyothish Application API",
                version = "v1.0",
                description = "API documentation for the Vedic Astrology Matching & Diagnostics Engine. Offers calculations for Panchangam, Dosha analysis, Yogas, and PDF Kundali reports.",
                contact = @Contact(
                        name = "Adithiyan Rajaperumal"
                )
        )
)
public class OpenApiConfig {
}
