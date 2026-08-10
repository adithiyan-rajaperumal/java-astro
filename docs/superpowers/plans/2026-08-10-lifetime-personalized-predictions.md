# Lifetime Personalized Vedic AI Predictions, Health Engine, Daily Balan & Dual Caching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a 100% personalized, high-precision Vedic AI prediction engine featuring lifetime year-by-year forecasts across 7 life pillars, an in-depth Ayurvedic & astrological health engine, real-time Gochara Daily Balan, zero-cost 30-day/daily dual-layer caching, granular YAML feature flags, and complete 6-language native localization.

**Architecture:** Spring Boot backend with `GeminiPredictionService` orchestrating 12-Varga chart matrices (D1, D2 Hora, D3, D7, D9, D10, D12, D30), Shadbala, and Vimshottari timelines via compressed telegraphic prompts to Google Gemini REST API; `PredictionCacheService` providing in-memory 30-day and end-of-day TTL caching integrated with PDF exports; React frontend with `localStorage` client caching, `AiPredictionsView`, `DailyBalanView`, and `HoroscopePage` tab state management.

**Tech Stack:** Java 17, Spring Boot 3.2, Google Gemini API (`gemini-2.0-flash`), Jackson, React 18, Vite, Swiss Ephemeris / JHour, OpenPDF / iText.

## Global Constraints

- Never use Redis; use zero-cost client `localStorage` + JVM in-memory caching for Render Free Tier.
- 30-Day TTL for Lifetime Predictions; 11:59:59 PM Same-Day TTL for Daily Balan.
- All AI features must be gated by YAML configuration flags (`gemini.life-predictions-enabled`, `gemini.daily-balan-enabled`, `gemini.pdf-predictions-enabled`).
- PDF downloads must never trigger redundant Gemini API calls; use cache when enabled.
- 100% native language support for `ta` (Tamil), `hi` (Hindi), `te` (Telugu), `kn` (Kannada), `ml` (Malayalam), and `en` (English).
- Subtab navigation must reset to `charts` when creating new charts or switching profiles.

---

### Task 1: Configuration & Feature Flagging in Backend

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/org/vedic/astro/config/GeminiProperties.java`
- Modify: `src/main/java/org/vedic/astro/dto/AppConfigDTO.java`
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java:32-43`
- Test: `src/test/java/org/vedic/astro/AppConfigControllerTest.java`

**Interfaces:**
- `GeminiProperties.isLifePredictionsEnabled() -> boolean`
- `GeminiProperties.isDailyBalanEnabled() -> boolean`
- `GeminiProperties.isPdfPredictionsEnabled() -> boolean`
- `AppConfigDTO` contains `boolean aiPredictionsEnabled`, `boolean lifePredictionsEnabled`, `boolean dailyBalanEnabled`, `boolean pdfPredictionsEnabled`, `String geminiModel`.

- [ ] **Step 1: Update `src/main/resources/application.yml` with granular flags**

```yaml
gemini:
  api-key: ${GEMINI_API_KEY:enc:QVEuQWI4Uk42SXZvc2QwSktCUF90UTZma0ZDOEhvSHk2anY5Y2JuMGdtTDRVc21JNW9acUE=}
  enabled: ${GEMINI_ENABLED:true}
  life-predictions-enabled: ${GEMINI_LIFE_PREDICTIONS_ENABLED:true}
  daily-balan-enabled: ${GEMINI_DAILY_BALAN_ENABLED:true}
  pdf-predictions-enabled: ${GEMINI_PDF_PREDICTIONS_ENABLED:true}
  model: ${GEMINI_MODEL:gemini-3.6-flash}
```

- [ ] **Step 2: Update `GeminiProperties.java`**

```java
package org.vedic.astro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Data
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey = "";
    private boolean enabled = true;
    private boolean lifePredictionsEnabled = true;
    private boolean dailyBalanEnabled = true;
    private boolean pdfPredictionsEnabled = true;
    private String model = "gemini-3.6-flash";

    public String getResolvedApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "";
        }
        String trimmed = apiKey.trim();
        if (trimmed.startsWith("enc:")) {
            try {
                byte[] decoded = Base64.getDecoder().decode(trimmed.substring(4));
                return new String(decoded, StandardCharsets.UTF_8).trim();
            } catch (Exception e) {
                return trimmed;
            }
        }
        return trimmed;
    }

    public boolean isFeatureEnabled() {
        String key = getResolvedApiKey();
        return enabled && !key.isEmpty();
    }

    public boolean isLifePredictionsEnabled() {
        return isFeatureEnabled() && lifePredictionsEnabled;
    }

    public boolean isDailyBalanEnabled() {
        return isFeatureEnabled() && dailyBalanEnabled;
    }

    public boolean isPdfPredictionsEnabled() {
        return isFeatureEnabled() && pdfPredictionsEnabled;
    }
}
```

- [ ] **Step 3: Update `AppConfigDTO.java` and `ChartController.java`**

```java
// AppConfigDTO.java
package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppConfigDTO {
    private boolean aiPredictionsEnabled;
    private boolean lifePredictionsEnabled;
    private boolean dailyBalanEnabled;
    private boolean pdfPredictionsEnabled;
    private String geminiModel;
}
```

In `ChartController.java`:
```java
    @GetMapping("/config")
    public ResponseEntity<AppConfigDTO> getApplicationConfig() {
        boolean enabled = geminiProperties != null && geminiProperties.isFeatureEnabled();
        boolean lifeEnabled = geminiProperties != null && geminiProperties.isLifePredictionsEnabled();
        boolean dailyEnabled = geminiProperties != null && geminiProperties.isDailyBalanEnabled();
        boolean pdfEnabled = geminiProperties != null && geminiProperties.isPdfPredictionsEnabled();
        String model = geminiProperties != null ? geminiProperties.getModel() : null;

        return ResponseEntity.ok(AppConfigDTO.builder()
                .aiPredictionsEnabled(lifeEnabled)
                .lifePredictionsEnabled(lifeEnabled)
                .dailyBalanEnabled(dailyEnabled)
                .pdfPredictionsEnabled(pdfEnabled)
                .geminiModel(model)
                .build());
    }
```

- [ ] **Step 4: Update and run `AppConfigControllerTest.java`**

Run: `mvn test -Dtest=AppConfigControllerTest`  
Expected: PASS (all config fields verified).

- [ ] **Step 5: Commit configuration changes**

```bash
git add src/main/resources/application.yml src/main/java/org/vedic/astro/config/GeminiProperties.java src/main/java/org/vedic/astro/dto/AppConfigDTO.java src/main/java/org/vedic/astro/controller/ChartController.java src/test/java/org/vedic/astro/AppConfigControllerTest.java
git commit -m "feat(config): add granular feature flags for life predictions, daily balan, and pdf export"
```

---

### Task 2: Data Models for Lifetime Predictions & Daily Balan

**Files:**
- Modify: `src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java`
- Create: `src/main/java/org/vedic/astro/dto/DailyBalanDTO.java`
- Create: `src/main/java/org/vedic/astro/dto/DailyBalanRequestDTO.java`

- [ ] **Step 1: Expand `PredictionResponseDTO.java` with Personality, Health Analysis, and 7-Pillar Yearly Predictions**

```java
package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictionResponseDTO {
    private boolean enabled;
    private String message;
    private String overallSummary;
    private TokenUsage tokenUsage;
    private NativePersonality nativePersonality;
    private HealthAnalysis healthAnalysis;
    private List<AiYoga> aiYogas;
    private List<AiDosham> aiDoshams;
    private List<PastMilestone> pastMilestones;
    private List<YearlyPrediction> futurePredictions;
    private List<YearlyPrediction> lifetimePredictions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NativePersonality {
        private String coreTemperament;
        private List<String> keyStrengths;
        private List<String> vulnerabilitiesAndKarmicLessons;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HealthAnalysis {
        private String ayurvedicConstitution;
        private List<String> organVulnerabilities;
        private String longevityVitalitySummary;
        private List<String> recommendedDietAndLifestyle;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenUsage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private double estimatedCostUsd;
        private double estimatedCostInr;
        private String modelUsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiYoga {
        private String name;
        private String formingPlanets;
        private String impact;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AiDosham {
        private String name;
        private String status;
        private String nullificationFactor;
        private String remedy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PastMilestone {
        private int year;
        private int age;
        private String dasaBhukthi;
        private String milestoneTitle;
        private String nature; // POSITIVE, CHALLENGING, NEUTRAL
        private String description;
        private String astrologicalFactor;
        private boolean verified;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class YearlyPrediction {
        private int year;
        private int age;
        private String dasaBhukthi;
        private String personalMindset;
        private String careerProfession;
        private String careerFinance; // Backwards compatible fallback
        private String wealthFinance;
        private String healthVitality;
        private String marriageFamily;
        private String familyMarriage; // Backwards compatible fallback
        private String parentsKids;
        private String favorableVsCaution;
        private String remediesGuidance;
    }
}
```

- [ ] **Step 2: Create `DailyBalanDTO.java` and `DailyBalanRequestDTO.java`**

```java
// DailyBalanDTO.java
package org.vedic.astro.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyBalanDTO {
    private boolean enabled;
    private String message;
    private String targetDate;
    private String rasi;
    private String nakshatra;
    private String runningDasaBhukthi;
    private boolean chandrashtama;
    private String generalOutlook;
    private String careerWork;
    private String financeWealth;
    private String healthVitality;
    private String relationshipFamily;
    private String luckyColor;
    private String luckyNumber;
    private String favorableDirection;
    private String bestTimeWindow;
    private String dailyRemedy;
    private PredictionResponseDTO.TokenUsage tokenUsage;
}

// DailyBalanRequestDTO.java
package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyBalanRequestDTO {
    private BirthDetailsDTO birthDetails;
    private ChartUiResponseDTO chartData;
    private String targetDate; // YYYY-MM-DD
    private String language;
    private boolean forceRefresh;
}
```

- [ ] **Step 3: Compile and verify data models**

Run: `mvn test-compile`  
Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit data models**

```bash
git add src/main/java/org/vedic/astro/dto/PredictionResponseDTO.java src/main/java/org/vedic/astro/dto/DailyBalanDTO.java src/main/java/org/vedic/astro/dto/DailyBalanRequestDTO.java
git commit -m "feat(dto): add rich personality, health, lifetime and daily balan data models"
```

---

### Task 3: In-Memory Dual-Layer Caching Engine (`PredictionCacheService`)

**Files:**
- Create: `src/main/java/org/vedic/astro/service/PredictionCacheService.java`
- Create: `src/test/java/org/vedic/astro/PredictionCacheServiceTest.java`

- [ ] **Step 1: Create `PredictionCacheService.java` with 30-day and same-day TTL**

```java
package org.vedic.astro.service;

import org.springframework.stereotype.Service;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.DailyBalanDTO;
import org.vedic.astro.dto.PredictionResponseDTO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PredictionCacheService {

    private final Map<String, CacheEntry<PredictionResponseDTO>> lifetimeCache = new ConcurrentHashMap<>();
    private final Map<String, CacheEntry<DailyBalanDTO>> dailyCache = new ConcurrentHashMap<>();

    private record CacheEntry<T>(T data, LocalDateTime expiresAt) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    public String generateLifetimeKey(BirthDetailsDTO b, String lang) {
        if (b == null) return "unknown";
        String raw = String.format("%s_%d_%d_%d_%d_%d_%.4f_%.4f_%s_%s",
                b.name(), b.year(), b.month(), b.day(), b.hour(), b.minute(),
                b.latitude(), b.longitude(), b.ayanamsa(), lang);
        return sha256(raw);
    }

    public String generateDailyKey(BirthDetailsDTO b, String targetDate, String lang) {
        if (b == null) return "unknown";
        String raw = String.format("%s_%d_%d_%d_%s_%s",
                b.name(), b.year(), b.month(), b.day(), targetDate, lang);
        return sha256(raw);
    }

    public PredictionResponseDTO getLifetimePrediction(String key) {
        CacheEntry<PredictionResponseDTO> entry = lifetimeCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.data();
        }
        lifetimeCache.remove(key);
        return null;
    }

    public void putLifetimePrediction(String key, PredictionResponseDTO data) {
        if (data != null && data.isEnabled()) {
            lifetimeCache.put(key, new CacheEntry<>(data, LocalDateTime.now().plusDays(30)));
        }
    }

    public DailyBalanDTO getDailyBalan(String key) {
        CacheEntry<DailyBalanDTO> entry = dailyCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.data();
        }
        dailyCache.remove(key);
        return null;
    }

    public void putDailyBalan(String key, DailyBalanDTO data, LocalDate targetDate) {
        if (data != null && data.isEnabled()) {
            LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);
            dailyCache.put(key, new CacheEntry<>(data, endOfDay));
        }
    }

    public void invalidateLifetime(String key) {
        lifetimeCache.remove(key);
    }

    public void invalidateDaily(String key) {
        dailyCache.remove(key);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }
}
```

- [ ] **Step 2: Create unit tests in `src/test/java/org/vedic/astro/PredictionCacheServiceTest.java`**

```java
package org.vedic.astro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vedic.astro.dto.BirthDetailsDTO;
import org.vedic.astro.dto.DailyBalanDTO;
import org.vedic.astro.dto.PredictionResponseDTO;
import org.vedic.astro.service.PredictionCacheService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class PredictionCacheServiceTest {

    private PredictionCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new PredictionCacheService();
    }

    @Test
    void testLifetimeCacheStorageAndRetrieval() {
        BirthDetailsDTO b = new BirthDetailsDTO("Adithiyan", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "Chennai", "LAHIRI");
        String key = cacheService.generateLifetimeKey(b, "ta");

        assertNull(cacheService.getLifetimePrediction(key));

        PredictionResponseDTO resp = PredictionResponseDTO.builder().enabled(true).overallSummary("Test Summary").build();
        cacheService.putLifetimePrediction(key, resp);

        PredictionResponseDTO cached = cacheService.getLifetimePrediction(key);
        assertNotNull(cached);
        assertEquals("Test Summary", cached.getOverallSummary());
    }

    @Test
    void testDailyCacheEndOfDayStorage() {
        BirthDetailsDTO b = new BirthDetailsDTO("Adithiyan", 1995, 8, 15, 10, 30, 0, 13.0827, 80.2707, "Chennai", "LAHIRI");
        String key = cacheService.generateDailyKey(b, "2026-08-10", "ta");

        DailyBalanDTO daily = DailyBalanDTO.builder().enabled(true).generalOutlook("Great Day").build();
        cacheService.putDailyBalan(key, daily, LocalDate.of(2026, 8, 10));

        DailyBalanDTO cached = cacheService.getDailyBalan(key);
        assertNotNull(cached);
        assertEquals("Great Day", cached.getGeneralOutlook());
    }
}
```

- [ ] **Step 3: Run test to verify cache behavior**

Run: `mvn test -Dtest=PredictionCacheServiceTest`  
Expected: PASS.

- [ ] **Step 4: Commit caching service**

```bash
git add src/main/java/org/vedic/astro/service/PredictionCacheService.java src/test/java/org/vedic/astro/PredictionCacheServiceTest.java
git commit -m "feat(cache): implement thread-safe 30-day and end-of-day in-memory prediction cache"
```

---

### Task 4: 12-Varga Synthesis & Telegraphic Prompt Engine in `GeminiPredictionService`

**Files:**
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Modify: `src/main/java/org/vedic/astro/controller/PredictionController.java`
- Modify: `src/main/java/org/vedic/astro/dto/PredictionRequestDTO.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

- [ ] **Step 1: Update `PredictionRequestDTO.java` to support `forceRefresh`**

```java
package org.vedic.astro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequestDTO {
    private BirthDetailsDTO birthDetails;
    private ChartUiResponseDTO chartData;
    private String language;
    private boolean forceRefresh;
}
```

- [ ] **Step 2: Implement 12-Varga telegraphic notation, health engine, and daily balan in `GeminiPredictionService.java`**
  - Integrate `PredictionCacheService` to check cache prior to calling Gemini.
  - Implement telegraphic chart notation for D1, D2 Hora, D3, D7, D9, D10, D12, D30, and Shadbala.
  - Separate system instructions into Gemini's `system_instruction` parameter for KV-cache acceleration.
  - Implement `generateDailyBalan(DailyBalanRequestDTO req)` computing Gochara transits relative to Janma Rasi/Nakshatra and Chandrashtama.

- [ ] **Step 3: Update `PredictionController.java` to expose `/generate` and `/daily` endpoints**

```java
package org.vedic.astro.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vedic.astro.dto.*;
import org.vedic.astro.service.GeminiPredictionService;

@RestController
@RequestMapping("/api/v1/astrology/predictions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PredictionController {

    private final GeminiPredictionService predictionService;

    @PostMapping(path = "/generate", produces = "application/json;charset=UTF-8")
    public ResponseEntity<PredictionResponseDTO> generateLifePredictions(@RequestBody PredictionRequestDTO request) {
        PredictionResponseDTO response = predictionService.generateLifePredictions(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/daily", produces = "application/json;charset=UTF-8")
    public ResponseEntity<DailyBalanDTO> generateDailyBalan(@RequestBody DailyBalanRequestDTO request) {
        DailyBalanDTO response = predictionService.generateDailyBalan(request);
        return ResponseEntity.ok(response);
    }
}
```

- [ ] **Step 4: Run test to verify prompt generation, caching, and fallback**

Run: `mvn test -Dtest=GeminiPredictionServiceTest`  
Expected: PASS.

- [ ] **Step 5: Commit prompt engine and controller changes**

```bash
git add src/main/java/org/vedic/astro/service/GeminiPredictionService.java src/main/java/org/vedic/astro/controller/PredictionController.java src/main/java/org/vedic/astro/dto/PredictionRequestDTO.java src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java
git commit -m "feat(ai): integrate 12-varga matrix, health engine, daily balan, and kv-cache acceleration"
```

---

### Task 5: PDF Export Token Shield & Feature Flag Integration

**Files:**
- Modify: `src/main/java/org/vedic/astro/controller/ChartController.java:55-87`
- Modify: `src/main/java/org/vedic/astro/service/PdfExportService.java`

- [ ] **Step 1: Update `ChartController.java` to respect `pdfPredictionsEnabled` and use cached predictions**

In `downloadComprehensiveAstrologyReport`:
```java
if (geminiProperties != null && geminiProperties.isPdfPredictionsEnabled() && geminiPredictionService != null) {
    try {
        ChartUiResponseDTO uiResp = orchestrationService.convertToUiDashboardResponse(res, payload);
        String lang = org.springframework.context.i18n.LocaleContextHolder.getLocale().getLanguage();
        org.vedic.astro.dto.PredictionRequestDTO predReq = org.vedic.astro.dto.PredictionRequestDTO.builder()
                .birthDetails(payload)
                .chartData(uiResp)
                .language(lang)
                .forceRefresh(false) // Never force refresh on PDF export
                .build();
        deepReportData.setAiPredictions(geminiPredictionService.generateLifePredictions(predReq));
    } catch (Exception ignored) {}
}
```

- [ ] **Step 2: Update `PdfExportService.java` to format health analysis and lifetime predictions**
  - Render Ayurvedic constitution, organ vulnerabilities, and lifetime predictions cleanly in PDF tables.

- [ ] **Step 3: Run existing PDF export tests**

Run: `mvn test -Dtest=PdfExportServiceTest` (or full test suite `mvn test`)  
Expected: PASS.

- [ ] **Step 4: Commit PDF token shield**

```bash
git add src/main/java/org/vedic/astro/controller/ChartController.java src/main/java/org/vedic/astro/service/PdfExportService.java
git commit -m "feat(pdf): integrate pdf feature flag and zero-token cached prediction reuse"
```

---

### Task 6: Complete I18n Translations Across All 6 Languages

**Files:**
- Modify: `frontend/src/i18n/translations.js`

- [ ] **Step 1: Add missing prediction, health, daily balan, and UI keys to `en`, `ta`, `hi`, `te`, `kn`, `ml` in `translations.js`**
  - Keys: `dailyBalanTab`, `generateDailyBalan`, `generatingDailyBalan`, `regenerateDailyBalan`, `personalityTitle`, `healthAnalysisTitle`, `ayurvedicConstitution`, `organVulnerabilities`, `luckyColor`, `luckyNumber`, `favorableDirection`, `bestTimeWindow`, `dailyRemedy`, `chandrashtamaAlert`, `filterPersonal`, `filterCareer`, `filterWealth`, `filterHealth`, `filterMarriage`, `filterParentsKids`, `filterRemedies`, `favorablePeriod`, `cautionPeriod`, `verifiedCheck`, `confirmMatch`.

- [ ] **Step 2: Verify `translations.js` has no syntax errors and clean fallbacks**

- [ ] **Step 3: Commit translations update**

```bash
git add frontend/src/i18n/translations.js
git commit -m "feat(i18n): complete 100% translations for all 6 languages across lifetime and daily predictions"
```

---

### Task 7: Enhanced Lifetime Predictions UI (`AiPredictionsView.jsx`)

**Files:**
- Modify: `frontend/src/components/AiPredictionsView.jsx`

- [ ] **Step 1: Replace hardcoded ternary text with `t(key, language)`**
- [ ] **Step 2: Add Personality & Core Temperament card**
- [ ] **Step 3: Add Ayurvedic & Astrological Health Analysis card**
- [ ] **Step 4: Add 7-Pillar Lifetime Year-by-Year Grid with interactive filter chips**
- [ ] **Step 5: Add localStorage 30-day caching on client-side**
- [ ] **Step 6: Commit `AiPredictionsView.jsx`**

```bash
git add frontend/src/components/AiPredictionsView.jsx
git commit -m "feat(ui): enhance ai life balan with personality, health diagnostics, and 7-pillar lifetime cards"
```

---

### Task 8: Daily Balan Frontend View (`DailyBalanView.jsx`)

**Files:**
- Create: `frontend/src/components/DailyBalanView.jsx`

- [ ] **Step 1: Build `DailyBalanView.jsx`**
  - Shows date picker (defaults to today).
  - Displays Chandrashtama warning badge if active.
  - Displays General Outlook, Career, Wealth, Health, and Relationship cards.
  - Displays Auspicious Daily Metrics banner (Lucky Color, Lucky Number, Direction, Best Time, Micro-Remedy).
  - Implements client-side `localStorage` caching valid until 11:59:59 PM.
  - Provides "🔄 Regenerate Daily Balan" button.
- [ ] **Step 2: Commit `DailyBalanView.jsx`**

```bash
git add frontend/src/components/DailyBalanView.jsx
git commit -m "feat(ui): add daily balan view with gochara metrics, lucky factors, and same-day caching"
```

---

### Task 9: UI Subtab Navigation, Feature Flag Wiring & Bug Fix in `HoroscopePage.jsx`

**Files:**
- Modify: `frontend/src/pages/HoroscopePage.jsx`

- [ ] **Step 1: Fetch and store `lifePredictionsEnabled` and `dailyBalanEnabled` from `/api/v1/astrology/config`**
- [ ] **Step 2: Fix subtab sticking bug**: Reset `activeSubTab('charts')` inside `setReport(null)` and `handleLoadSavedProfile`.
- [ ] **Step 3: Render `🔮 AI Life Balan` tab when `lifePredictionsEnabled` is true**
- [ ] **Step 4: Render `📅 Daily Balan` tab when `dailyBalanEnabled` is true**
- [ ] **Step 5: Wire `DailyBalanView` and `AiPredictionsView` components**
- [ ] **Step 6: Commit `HoroscopePage.jsx`**

```bash
git add frontend/src/pages/HoroscopePage.jsx
git commit -m "feat(ui): wire feature flags, daily balan tab, and fix tab sticking navigation bug"
```

---

### Task 10: End-to-End Verification & Walkthrough

**Files:**
- Create/Update: Artifact walkthrough

- [ ] **Step 1: Run full Maven test suite**
```bash
mvn clean test
```
Expected: All unit and integration tests PASS.

- [ ] **Step 2: Verify React build**
```bash
cd frontend && npm run build
```
Expected: Build passes with 0 errors.

- [ ] **Step 3: End-to-end verification of UI, caching, flag controls, and language switching**
- [ ] **Step 4: Final commit & walkthrough artifact update**
