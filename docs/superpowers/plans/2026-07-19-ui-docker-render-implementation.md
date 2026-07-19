# Full-Stack UI + Docker Deployment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a React UI, enhance the dosham engine, and bundle them into a single Spring Boot executable JAR deployed via Docker to Render.

**Architecture:** Spring Boot backend with an embedded React (Vite) frontend. A multi-stage Dockerfile builds both and runs a memory-constrained JRE. Keep-alive scheduler prevents free-tier cold starts.

**Tech Stack:** Java 17, Spring Boot 3.3.4, React 18, Vite, Maven, Docker.

## Global Constraints

- Java version: 17
- Spring Boot version: 3.3.4
- Node version: 18 (via frontend-maven-plugin)
- No TailwindCSS unless requested (use Vanilla CSS/CSS Modules)
- Render Free Tier memory limit: 512MB
- Encoding: UTF-8

---

### Task 1: Keep-Alive Scheduler & SPA Fallback

**Files:**
- Create: `src/main/java/org/vedic/astro/controller/HealthController.java`
- Create: `src/main/java/org/vedic/astro/scheduler/KeepAliveScheduler.java`
- Create: `src/main/java/org/vedic/astro/controller/SpaForwardController.java`
- Modify: `src/main/java/org/vedic/astro/AstroEngineApplication.java`

**Interfaces:**
- Produces: `/api/health` endpoint
- Produces: Scheduled task running every 14 minutes
- Produces: SPA fallback route `/{path:[^\\.]*}`

- [ ] **Step 1: Enable Scheduling**

Modify `src/main/java/org/vedic/astro/AstroEngineApplication.java`:
```java
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
```

- [ ] **Step 2: Create HealthController**

Create `src/main/java/org/vedic/astro/controller/HealthController.java`:
```java
package org.vedic.astro.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "timestamp", Instant.now().toString());
    }
}
```

- [ ] **Step 3: Create KeepAliveScheduler**

Create `src/main/java/org/vedic/astro/scheduler/KeepAliveScheduler.java`:
```java
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
```

- [ ] **Step 4: Create SpaForwardController**

Create `src/main/java/org/vedic/astro/controller/SpaForwardController.java`:
```java
package org.vedic.astro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}
```

- [ ] **Step 5: Verify & Commit**

Run: `mvn clean compile`
Expected: BUILD SUCCESS
Run: `git add src/main/java/org/vedic/astro/AstroEngineApplication.java src/main/java/org/vedic/astro/controller/HealthController.java src/main/java/org/vedic/astro/scheduler/KeepAliveScheduler.java src/main/java/org/vedic/astro/controller/SpaForwardController.java && git commit -m "feat: add keep-alive scheduler and SPA fallback"`

---

### Task 2: Planet Dignity Utils & Diagnostics Enhancement

**Files:**
- Create: `src/main/java/org/vedic/astro/util/PlanetDignityUtils.java`
- Modify: `src/main/java/org/vedic/astro/dto/DiagnosticsDTO.java`
- Modify: `src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java`
- Modify: `src/main/resources/i18n/messages_*.properties`

**Interfaces:**
- Produces: Enhanced `DoshaDetail` structure
- Produces: Additional Doshams (Putra, Kalathira, Shani) and Yogas

- [ ] **Step 1: Create PlanetDignityUtils**

Create `src/main/java/org/vedic/astro/util/PlanetDignityUtils.java`:
```java
package org.vedic.astro.util;

public class PlanetDignityUtils {
    public static boolean isExalted(String planet, int sign) {
        return switch (planet) {
            case "Sun" -> sign == 1;
            case "Moon" -> sign == 2;
            case "Mars" -> sign == 10;
            case "Mercury" -> sign == 6;
            case "Jupiter" -> sign == 4;
            case "Venus" -> sign == 12;
            case "Saturn" -> sign == 7;
            default -> false;
        };
    }
    public static boolean isDebilitated(String planet, int sign) {
        return switch (planet) {
            case "Sun" -> sign == 7;
            case "Moon" -> sign == 8;
            case "Mars" -> sign == 4;
            case "Mercury" -> sign == 12;
            case "Jupiter" -> sign == 10;
            case "Venus" -> sign == 6;
            case "Saturn" -> sign == 1;
            default -> false;
        };
    }
    public static boolean isOwnSign(String planet, int sign) {
        return switch (planet) {
            case "Sun" -> sign == 5;
            case "Moon" -> sign == 4;
            case "Mars" -> sign == 1 || sign == 8;
            case "Mercury" -> sign == 3 || sign == 6;
            case "Jupiter" -> sign == 9 || sign == 12;
            case "Venus" -> sign == 2 || sign == 7;
            case "Saturn" -> sign == 10 || sign == 11;
            default -> false;
        };
    }
    public static int getHouseFromLagna(int planetSign, int lagnaSign) {
        return ((planetSign - lagnaSign + 12) % 12) + 1;
    }
}
```

- [ ] **Step 2: Enhance DiagnosticsDTO**

Modify `src/main/java/org/vedic/astro/dto/DiagnosticsDTO.java`:
```java
package org.vedic.astro.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiagnosticsDTO {
    private List<YogaDetail> activeYogas;
    private List<DoshaDetail> discoveredDoshams;
    private List<String> horoscopicSpecialities;

    @Data
    @Builder
    public static class YogaDetail {
        private String name;
        private String description;
        private String impactLevel;
    }

    @Data
    @Builder
    public static class DoshaDetail {
        private String name;
        private boolean detected;
        private boolean active;
        private boolean nullified;
        private String severity;
        private String nullificationReason;
        private String remedySuggestion;
    }
}
```

- [ ] **Step 3: Update AstrologyDiagnosticsService**

Modify `src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java` to use the enhanced structure (using a simplified version of the logic defined in the spec, implementer must expand to include all rules):
```java
package org.vedic.astro.service;

import org.springframework.stereotype.Service;
import org.vedic.astro.dto.DiagnosticsDTO;
import org.vedic.astro.model.PlanetaryPosition;
import org.vedic.astro.util.PlanetDignityUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AstrologyDiagnosticsService {
    public DiagnosticsDTO runHoroscopeDiagnostics(Map<String, PlanetaryPosition> d1Map) {
        List<DiagnosticsDTO.YogaDetail> yogas = new ArrayList<>();
        List<DiagnosticsDTO.DoshaDetail> doshams = new ArrayList<>();
        List<String> specs = new ArrayList<>();

        PlanetaryPosition lagna = d1Map.get("Lagna");
        PlanetaryPosition mars = d1Map.get("Mars");
        
        int lagnaSign = lagna.getSignNumber();
        int marsHouse = PlanetDignityUtils.getHouseFromLagna(mars.getSignNumber(), lagnaSign);
        
        boolean hasMars = (marsHouse == 1 || marsHouse == 2 || marsHouse == 4 || marsHouse == 7 || marsHouse == 8 || marsHouse == 12);
        boolean marsOwnOrExalted = PlanetDignityUtils.isOwnSign("Mars", mars.getSignNumber()) || PlanetDignityUtils.isExalted("Mars", mars.getSignNumber());
        
        doshams.add(DiagnosticsDTO.DoshaDetail.builder()
                .name("Sevvai Dosham (Mars Affliction)")
                .detected(hasMars)
                .nullified(hasMars && marsOwnOrExalted)
                .active(hasMars && !marsOwnOrExalted)
                .severity(hasMars ? (marsOwnOrExalted ? "Cancelled" : "High") : "None")
                .nullificationReason(marsOwnOrExalted ? "Mars is in own or exalted sign" : null)
                .remedySuggestion("Perform prayers at Vaideeswaran Koil.")
                .build());

        // Note to implementer: Add other doshams (Kala Sarpa, Sarpam, Pithru, Putra, Kalathira, Shani)
        // and Yogas (Gajakesari, Budha-Aditya, etc.) following the spec.

        return DiagnosticsDTO.builder().activeYogas(yogas).discoveredDoshams(doshams).horoscopicSpecialities(specs).build();
    }
}
```
*(Implementer to complete full dosham logic as per spec)*

- [ ] **Step 4: Update i18n files**

Add missing key to `src/main/resources/i18n/messages_en.properties` (and other language files as needed):
```properties
panchangam.throughout_day=Throughout the day
```

- [ ] **Step 5: Verify & Commit**

Run: `mvn compile`
Expected: BUILD SUCCESS
Run: `git add src/main/java/org/vedic/astro/util/PlanetDignityUtils.java src/main/java/org/vedic/astro/dto/DiagnosticsDTO.java src/main/java/org/vedic/astro/service/AstrologyDiagnosticsService.java src/main/resources/i18n/ && git commit -m "feat: enhance dosham engine and add dignity utils"`

---

### Task 3: Comprehensive API Endpoint

**Files:**
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java`

**Interfaces:**
- Produces: `POST /api/v1/astrology/comprehensive` endpoint

- [ ] **Step 1: Add Endpoint in ChartController**

Modify `src/main/java/org/vedic/astro/controller/ChartController.java`:
```java
// Add inside ChartController class
    @PostMapping(path = "/comprehensive", produces = "application/json;charset=UTF-8")
    public ResponseEntity<ComprehensiveReportDTO> getComprehensiveData(
            @RequestBody BirthDetailsDTO payload,
            @RequestParam(defaultValue = "DRIK_TIRUKANITHAM") PanchangamType systemType) {
        
        PanchangamEngine engine = panchangamFactory.getEngine(systemType);
        ChartResult res = engine.calculate(payload);
        
        ComprehensiveReportDTO report = engine.generateComprehensiveReport(payload, res);
        return ResponseEntity.ok(report);
    }
```

- [ ] **Step 2: Verify & Commit**

Run: `mvn compile`
Expected: BUILD SUCCESS
Run: `git add src/main/java/org/vedic/astro/controller/ChartController.java && git commit -m "feat: add comprehensive API endpoint"`

---

### Task 4: Deployment Config & Build Integration

**Files:**
- Modify: `pom.xml`
- Create: `Dockerfile`
- Create: `render.yaml`

- [ ] **Step 1: Update pom.xml for Frontend Build**

Add `frontend-maven-plugin` to `pom.xml` in `<plugins>`:
```xml
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.15.0</version>
    <configuration>
        <workingDirectory>frontend</workingDirectory>
        <installDirectory>target</installDirectory>
    </configuration>
    <executions>
        <execution>
            <id>install node and npm</id>
            <goals>
                <goal>install-node-and-npm</goal>
            </goals>
            <configuration>
                <nodeVersion>v18.17.1</nodeVersion>
                <npmVersion>9.6.7</npmVersion>
            </configuration>
        </execution>
        <execution>
            <id>npm install</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>install</arguments>
            </configuration>
        </execution>
        <execution>
            <id>npm run build</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>run build</arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <artifactId>maven-resources-plugin</artifactId>
    <version>3.3.1</version>
    <executions>
        <execution>
            <id>copy-frontend</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <outputDirectory>${project.build.directory}/classes/static</outputDirectory>
                <resources>
                    <resource>
                        <directory>frontend/dist</directory>
                        <filtering>false</filtering>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 2: Create Dockerfile**

Create `Dockerfile`:
```dockerfile
# Stage 1: Build frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci || echo "No frontend yet"
COPY frontend/ ./
RUN npm run build || echo "No frontend yet"

# Stage 2: Build backend
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src/ src/
# Create static directory in case frontend build failed/skipped
RUN mkdir -p src/main/resources/static/
COPY --from=frontend-build /app/frontend/dist/ src/main/resources/static/ || true
RUN mvn package -DskipTests -Dfrontend.skip=true

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-jar", "app.jar"]
```

- [ ] **Step 3: Create render.yaml**

Create `render.yaml`:
```yaml
services:
  - type: web
    name: jyothish-app
    runtime: docker
    plan: free
    envVars:
      - key: PORT
        value: 8080
```

- [ ] **Step 4: Commit**

Run: `git add pom.xml Dockerfile render.yaml && git commit -m "build: add docker, render config and frontend maven plugin"`

---

### Task 5: Frontend Scaffolding

**Files:**
- Create: `frontend/` directory with Vite React template
- Modify: `frontend/vite.config.js`
- Create: `frontend/src/theme/colors.css`

- [ ] **Step 1: Initialize Vite**

Run:
```bash
npx -y create-vite@latest frontend --template react
```

- [ ] **Step 2: Configure Proxy**

Modify `frontend/vite.config.js`:
```javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

- [ ] **Step 3: Setup Theme**

Create `frontend/src/theme/colors.css`:
```css
:root {
  --bg-primary: #0f0f1a;
  --bg-card: #1a1a2e;
  --bg-card-hover: #22223a;
  --accent-gold: #d4a843;
  --accent-warm: #e8913a;
  --text-primary: #f0e6d3;
  --text-secondary: #a09888;
  --success: #4caf50;
  --warning: #ff9800;
  --danger: #f44336;
  --border: #2a2a3e;
}
body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-family: 'Inter', sans-serif;
  margin: 0;
}
```

- [ ] **Step 4: Commit**

Run: `git add frontend/ && git commit -m "feat: initialize react frontend with vite"`

---

### Implementer Notes for Next Steps:
Following the scaffold, the implementer will build the specific pages (`Panchangam`, `Horoscope`, `Matching`, `Settings`) inside `frontend/src/pages/` and shared components in `frontend/src/components/`, consuming the `/api/v1/astrology/*` endpoints setup in Task 3.
