# Swagger and README Integration Design

## Overview
This document outlines the design for integrating Swagger (OpenAPI 3) for API testing and documenting the full application stack in the `README.md`.

## 1. Swagger Configuration
- **Dependency:** Add `springdoc-openapi-starter-webmvc-ui` (v2.6.0 or latest stable) to `pom.xml`.
- **Java Configuration:** Create `org.vedic.astro.config.OpenApiConfig`.
  - Use `@Configuration` and `@OpenAPIDefinition`.
  - Set Title to "Jyothish Application API".
  - Set Version to "v1.0".
  - Set Description summarizing the astrology calculations, dosham matching, and panchangam engine.
- **Access:** Swagger UI will be hosted natively by Spring Boot at `http://localhost:8080/swagger-ui.html`.

## 2. README.md Structure
The README will be heavily overhauled to serve as a complete project entry point, structured as follows:

### Title & Overview
- "Jyothish Application"
- Brief description of the full-stack nature of the app (Spring Boot + React) and its purpose (Vedic astrology engine with multi-language support).

### Features & Functionalities
- **Dosham & Yoga Engine:** Detection and nullification logic for 7 doshams and 9 yogas.
- **Panchangam Calculations:** Accurate planetary positions using Swiss Ephemeris.
- **PDF Export:** Generation of Kundali reports.
- **Responsive UI:** Built with React/Vite.
- **Multi-language Support:** English, Tamil, Kannada, Hindi, Telugu, Malayalam.

### Tech Stack
- Backend: Java 17, Spring Boot 3.3.4, Swiss Ephemeris.
- Frontend: React 18, Vite.
- Infrastructure: Docker, Render.

### UI Details
- Description of the Dark Theme (navy backgrounds with gold/saffron accents).
- High-level overview of the dashboard flow.

### API Reference (High-Level)
- Explicit callout: "For full interactive API schemas and testing, run the application and visit `/swagger-ui.html`."
- Detailed Primary Example: `POST /api/v1/astrology/calculate`
  - Sample Request (JSON representing `BirthDetailsDTO`).
  - Sample Response (JSON representing `ChartUiResponseDTO`).

### Setup & Execution
- **Local Development:** Instructions for running via Maven wrapper (`./mvnw clean compile spring-boot:run`) and Node.js (`npm run dev`).
- **Docker Production Build:** Instructions for building and running the unified Docker image.

## Scope
- This work is strictly limited to updating `pom.xml`, creating the `OpenApiConfig.java` file, and completely rewriting `README.md`. No changes will be made to core domain logic or React components.
