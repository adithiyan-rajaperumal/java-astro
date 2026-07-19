# Full-Stack UI + Docker Deployment to Render

## Overview

Bundle a React (Vite) frontend into the existing Spring Boot backend as a single deployable unit, add comprehensive dosham/yoga detection with nullification logic, and deploy the whole thing to Render via Docker from GitHub.

**Key decisions:**
- React (Vite) in `frontend/` folder, built output copied into Spring Boot's `static/` resources
- Single fat JAR serves both API and UI
- Multi-stage Dockerfile: Node → Maven → JRE runtime
- Render free tier (512 MB), auto-deploy from GitHub
- Dark theme with gold/saffron accents
- Multi-language UI (EN, TA, KN, HI, TE, ML), default: English + Chennai location
- Mobile-first with bottom tab bar; desktop switches to side navigation rail

---

## Architecture

```
java-astro/
├── frontend/                         # React (Vite) app
│   ├── public/
│   ├── src/
│   │   ├── pages/                    # Panchangam, Horoscope, Matching, Settings
│   │   ├── components/               # Shared UI components
│   │   ├── hooks/                    # API hooks, useLocalStorage
│   │   ├── i18n/                     # Frontend label translations
│   │   ├── utils/                    # Chart rendering helpers, planet dignity tables
│   │   └── theme/                    # CSS variables, dark theme tokens
│   ├── index.html
│   ├── package.json
│   └── vite.config.js                # Proxy /api → :8080 in dev mode
├── src/main/                         # Existing Spring Boot backend
│   ├── java/org/vedic/astro/
│   │   ├── controller/
│   │   │   ├── SpaForwardController.java  # [NEW] SPA fallback
│   │   │   └── HealthController.java      # [NEW] Health check endpoint
│   │   ├── scheduler/
│   │   │   └── KeepAliveScheduler.java    # [NEW] Self-ping to prevent Render sleep
│   │   ├── service/
│   │   │   └── AstrologyDiagnosticsService.java  # [MODIFY] Enhanced dosham engine
│   │   ├── dto/
│   │   │   └── DiagnosticsDTO.java   # [MODIFY] Enhanced DoshaDetail
│   │   └── util/
│   │       └── PlanetDignityUtils.java  # [NEW] Exaltation/debilitation tables
│   └── resources/
│       ├── static/                   # Built frontend output (auto-populated by Maven)
│       └── i18n/                     # [MODIFY] Add dosham/yoga i18n keys
├── Dockerfile                        # [NEW] Multi-stage build
├── render.yaml                       # [NEW] Render deployment config
└── pom.xml                           # [MODIFY] Add frontend-maven-plugin
```

### Build Pipeline

1. `frontend-maven-plugin` runs during `mvn package`:
   - Installs Node 18 (locally, not global)
   - Runs `npm ci` then `npm run build`
   - Copies `frontend/dist/` → `target/classes/static/`
2. Spring Boot packages everything into a single executable JAR
3. Docker multi-stage build encapsulates the whole pipeline

### API Surface (Existing + New)

| Endpoint | Method | Status |
|----------|--------|--------|
| `/api/v1/astrology/panchangam` | POST | Existing — no changes |
| `/api/v1/astrology/calculate` | POST | Existing — no changes |
| `/api/v1/astrology/comprehensive` | POST | **NEW** — returns ComprehensiveReportDTO as JSON |
| `/api/v1/astrology/download-pdf` | POST | Existing — update PDF to show enhanced dosham |
| `/api/v1/astrology/match` | POST | Existing — no changes |
| `/api/v1/astrology/match/download-pdf` | POST | Existing — no changes |
| `/api/v1/locations/autocomplete` | GET | Existing — no changes |

---

## UI Design

### Layout Structure

**Top Header Bar (all screen sizes):**
```
┌─────────────────────────────────────────────────┐
│ 🕉️ JyothishApp           [EN ▾]  [📍 Chennai ▾] │
└─────────────────────────────────────────────────┘
```
- Left: App logo + name
- Right: Language chip (inline dropdown) + Location chip (inline dropdown with autocomplete)
- Clicking either opens a compact popover — no page redirect

**Mobile (< 768px):**
- Bottom tab bar (fixed) with 4 icons + labels
- Single-column content layout
- Cards stack vertically

**Tablet (768px – 1024px):**
- Bottom tab bar
- 2-column card grid

**Desktop (> 1024px):**
- Left side navigation rail (vertical icons + labels)
- 2-3 column content grid
- Spacious layout with breathing room

### Bottom / Side Navigation Tabs

| # | Icon | Label | Route |
|---|------|-------|-------|
| 1 | 🏠 | Panchangam | `/` |
| 2 | 📜 | Horoscope | `/horoscope` |
| 3 | 💑 | Matching | `/matching` |
| 4 | ⚙️ | Settings | `/settings` |

### Visual Theme — Dark + Gold/Saffron

```css
--bg-primary: #0f0f1a;           /* Deep dark background */
--bg-card: #1a1a2e;              /* Card surfaces */
--bg-card-hover: #22223a;
--accent-gold: #d4a843;          /* Primary accent — saffron gold */
--accent-warm: #e8913a;          /* Secondary warm accent */
--text-primary: #f0e6d3;         /* Warm white text */
--text-secondary: #a09888;       /* Muted text */
--success: #4caf50;
--warning: #ff9800;
--danger: #f44336;
--border: #2a2a3e;
```

- Google Font: **Inter** (UI) + **Noto Sans Tamil/Devanagari/Kannada/Telugu/Malayalam** (Indic scripts)
- Glassmorphism cards with subtle backdrop blur
- Micro-animations on tab switches, card hover, button press
- Smooth page transitions

---

## Page Details

### Page 1: Panchangam (Home `/`)

**Defaults:** Today's date, Chennai (13.0827, 80.2707), English

**Top section:**
- Date display with ← → arrows for prev/next day
- Location subtitle (from settings)

**Content sections (scrollable cards):**
1. **Sun & Moon Timings** — Sunrise, Sunset, Moonrise, Moonset with icons
2. **Panchangam Elements** — Thithi, Nakshatra, Yogam, Karanam with end times
3. **Rashi & Chandrastamam** — Moon rashi, Chandrastamam rashi
4. **Muhurtham & Vasthu** — Day status badges
5. **Netram & Jeevan** — Numeric indicators
6. **Auspicious Times** — Nalla Neram, Gowri Nalla Neram as colored time bars
7. **Inauspicious Times** — Rahu Kalam, Emagandam, Kulikai as time bars
8. **Horai Table** — 24 rows: hour number, time range, planet name with colored dot, day/night separator

### Page 2: Horoscope (`/horoscope`)

**Birth Details Form:**
- Name, Date (date picker), Time (hour:minute picker), Location (autocomplete)
- Ayanamsa selector (defaults to user's Settings preference)
- "Generate Horoscope" button

**Results — Tabbed Interface:**

```
[ D1 Rasi ] [ D9 Navamsa ] [ Dasa-Bhukthi ] [ Shadbala ] [ Dosham & Yogas ]
```

**Tab: D1 Rasi / D9 Navamsa**
- Chart rendered as SVG:
  - **Hindi language selected** → North Indian diamond chart
  - **All other languages** → South Indian box chart
- Interactive: clicking a house draws drishti (aspect) lines to aspected houses
- Below chart: clicked planet details — name, sign, degree, dignity status (Uchcha / Neecham / Swakshetra / Moolatrikona)
- Planetary positions table

**Tab: Dasa-Bhukthi**
- Timeline / accordion view
- Each Maha Dasa expandable to show Bhukthi sub-periods with date ranges
- Current running dasa highlighted

**Tab: Shadbala**
- Table: Planet | Sthana | Dig | Kala | Cheshta | Total | Status
- Color-coded status: Very Strong (green), Optimum (gold), Weak (red)

**Tab: Dosham & Yogas**
- **Dosham section**: Each dosham as a card showing:
  - Dosham name
  - Status: Detected → Nullified / Active
  - Severity badge
  - Nullification reason (if cancelled)
  - Remedy suggestion (if active)
- **Yoga section**: Active yogas with description and impact level
- **Specialities**: Horoscopic specialities list

**PDF Download:** Floating "Download Report" button → calls `/download-pdf`

### Page 3: Matching (`/matching`)

**Input:**
- Two birth detail forms side-by-side (stacked on mobile)
- Matching System toggle: Ashta Koota / Dasa Porutham
- Strictness level selector
- "Calculate Match" button

**Results:**
- Score gauge — circular progress (e.g., 28/36)
- Verdict badge — color-coded (Excellent / Good / Average / Not Recommended)
- Warnings panel (Manglik mismatch etc.)
- Koota/Porutham table: Name | Max | Scored | Status (✅/🔄/❌) | Reason
- Boy & Girl birth profiles side by side
- "Download PDF" button

### Page 4: Settings (`/settings`)

- **Language** — card selector with script preview (English, தமிழ், हिंदी, ಕನ್ನಡ, తెలుగు, മലയാളം)
- **Default Location** — autocomplete search
- **Ayanamsa** — dropdown (Lahiri, Raman, KP)
- **About** — app name, version, methodology credits

All settings persisted in `localStorage`.

---

## Enhanced Dosham & Yoga Engine

### DiagnosticsDTO.DoshaDetail — Enhanced Structure

```java
@Data @Builder
public static class DoshaDetail {
    private String name;
    private boolean detected;           // was the dosha initially detected?
    private boolean active;             // still active after nullification?
    private boolean nullified;          // was it cancelled?
    private String severity;            // High / Medium / Cancelled
    private String nullificationReason; // why it was cancelled (localized)
    private String remedySuggestion;    // remedy if still active
}
```

### Planet Dignity Utility — `PlanetDignityUtils.java` [NEW]

Static lookup tables for Vedic planetary dignities:

| Planet | Exalted (Uchcha) | Debilitated (Neecham) | Own Signs (Swakshetra) | Moolatrikona |
|--------|------------------|-----------------------|------------------------|--------------|
| Sun | 1 (Mesha) | 7 (Tula) | 5 (Simha) | 5 (Simha) |
| Moon | 2 (Vrishabha) | 8 (Vrischika) | 4 (Karka) | 2 (Vrishabha) |
| Mars | 10 (Makara) | 4 (Karka) | 1, 8 (Mesha, Vrischika) | 1 (Mesha) |
| Mercury | 6 (Kanya) | 12 (Meena) | 3, 6 (Mithuna, Kanya) | 6 (Kanya) |
| Jupiter | 4 (Karka) | 10 (Makara) | 9, 12 (Dhanu, Meena) | 9 (Dhanu) |
| Venus | 12 (Meena) | 6 (Kanya) | 2, 7 (Vrishabha, Tula) | 7 (Tula) |
| Saturn | 7 (Tula) | 1 (Mesha) | 10, 11 (Makara, Kumbha) | 11 (Kumbha) |

Helper methods:
- `isExalted(planet, signNumber)` / `isDebilitated(planet, signNumber)`
- `isOwnSign(planet, signNumber)` / `isMoolatrikona(planet, signNumber)`
- `getDignityStatus(planet, signNumber)` → returns "Uchcha" / "Neecham" / "Swakshetra" / "Moolatrikona" / null
- `getHouseFromLagna(planetSign, lagnaSign)` → calculates bhava position
- `isAspecting(fromHouse, toHouse)` → checks standard Vedic aspects (7th for all, Mars: 4,8; Jupiter: 5,9; Saturn: 3,10)

### Dosham Detection + Nullification

#### 1. Sevvai Dosham (Manglik / Mars Affliction)
**Detection:** Mars in houses 1, 2, 4, 7, 8, 12 from Lagna AND from Moon AND from Venus (traditional triple check)

**Nullification (any one cancels):**
- Mars in own sign (Mesha, Vrischika) or exalted (Makara)
- Jupiter aspects or conjuncts Mars
- Venus aspects Mars
- Mars in houses 1 or 2 in benefic signs (Karka, Dhanu, Meena)

#### 2. Kala Sarpa Dosham
**Detection:** All 7 planets (Sun through Saturn) confined to one side of the Rahu-Ketu axis

**Nullification:**
- Any planet conjunct (same sign) Rahu or Ketu breaks the axis
- Jupiter aspects Rahu or Ketu
- Partial Kala Sarpa (not all 7 strictly between)

#### 3. Sarpam / Naga Dosham
**Detection:** Rahu or Ketu in houses 1, 2, 5, 7, 8 from Lagna

**Nullification:**
- Jupiter in Kendra (1, 4, 7, 10) from Rahu
- Jupiter aspects Rahu or Ketu
- Rahu in own sign (Kumbha) or exalted (Vrishabha)

#### 4. Pithru Dosham
**Detection:** Sun in 9th house AND afflicted by Rahu, Ketu, or Saturn (conjunction or aspect)

**Nullification:**
- Jupiter aspects 9th house
- Sun in own sign (Simha) or exalted (Mesha)
- Benefic planet (Jupiter, Venus, Mercury) in 9th house

#### 5. Putra Dosham [NEW]
**Detection:** 5th house from Lagna occupied or aspected by malefics (Saturn, Rahu, Ketu, Mars) AND 5th lord is weak (debilitated or in 6/8/12)

**Nullification:**
- Jupiter aspects 5th house
- 5th lord in own, exalted, or friendly sign
- Benefic planet in 5th house

#### 6. Kalathira Dosham [NEW]
**Detection:** 7th house/lord afflicted by malefics (Saturn, Mars, Rahu in 7th or aspecting 7th)

**Nullification:**
- Venus in own (Vrishabha/Tula) or exalted (Meena) sign
- Jupiter aspects 7th house
- 7th lord in own or friendly sign

#### 7. Shani Dosham [NEW]
**Detection:** Saturn in houses 1, 4, 7, 8, 10, 12 from Lagna

**Nullification:**
- Saturn in own sign (Makara/Kumbha) or exalted (Tula)
- Saturn aspected by Jupiter
- Saturn is Yogakaraka for the Lagna (Vrishabha/Tula Lagna)

### Yoga Detection [NEW]

| Yoga | Rule | Impact |
|------|------|--------|
| **Gajakesari** | Jupiter in Kendra (1,4,7,10) from Moon | High — Wisdom, fame, prosperity |
| **Budha-Aditya** | Sun & Mercury in same sign | Medium — Intelligence, communication |
| **Chandra-Mangal** | Moon & Mars in same sign | Medium — Wealth through effort |
| **Ruchaka** (Pancha Mahapurusha) | Mars in Kendra in own/exalted sign | High — Courage, leadership |
| **Bhadra** (Pancha Mahapurusha) | Mercury in Kendra in own/exalted sign | High — Intellect, business acumen |
| **Hamsa** (Pancha Mahapurusha) | Jupiter in Kendra in own/exalted sign | High — Righteousness, spiritual growth |
| **Malavya** (Pancha Mahapurusha) | Venus in Kendra in own/exalted sign | High — Luxury, artistic talent |
| **Sasa** (Pancha Mahapurusha) | Saturn in Kendra in own/exalted sign | High — Authority, discipline |
| **Neecha Bhanga Raja** | Debilitated planet's dispositor in Kendra from Lagna or Moon | High — Turns weakness to strength |

### i18n Keys Required

> [!WARNING]
> **Existing bug:** `panchangam.throughout_day` is referenced in `DailyPanchangamServiceImpl.java` line 412 but is missing from ALL i18n files. Must be added as part of this work.

New keys needed in all 6 language files for:
- 7 dosham names, severity levels, nullification reasons, remedies
- 9 yoga names, descriptions, impact levels
- Planet dignity statuses (Uchcha, Neecham, Swakshetra, Moolatrikona)
- UI labels (page titles, button labels, form labels, tab names)

---

## PDF Report Enhancement

The existing PDF report (generated by `PdfExportService`) needs a dosham section update:

- **Current:** Shows dosham name + active/inactive + remedy
- **Enhanced:** Shows dosham name + detected status + nullified status + nullification reason + severity + remedy
- Column layout: Dosham | Detected | Status | Reason | Remedy

---

## Deployment

### Dockerfile (Multi-Stage)

```dockerfile
# Stage 1: Build frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

# Stage 2: Build backend (with frontend bundled)
FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src/ src/
COPY --from=frontend-build /app/frontend/dist/ src/main/resources/static/
RUN mvn package -DskipTests -Dfrontend.skip=true

# Stage 3: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-XX:+UseStringDeduplication", "-jar", "app.jar"]
```

### render.yaml

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

### pom.xml Changes

Add `frontend-maven-plugin` to:
1. Install Node 18 locally
2. Run `npm ci` in `frontend/`
3. Run `npm run build`
4. Output lands in `frontend/dist/` which the existing resource copying handles

This plugin is skipped in Docker (frontend already built in Stage 1) via `-Dfrontend.skip=true`.

### SPA Fallback Controller [NEW]

```java
@Controller
public class SpaForwardController {
    @RequestMapping(value = "/{path:[^\\.]*}")
    public String forward() {
        return "forward:/index.html";
    }
}
```

Routes non-API, non-static paths to `index.html` for React Router.

### Spring Boot Configuration

- `server.port` reads from `PORT` env var for Render: `server.port: ${PORT:8080}`
- CORS already configured with `@CrossOrigin(origins = "*")`

### Keep-Alive Scheduler [NEW]

> [!IMPORTANT]
> Render free tier spins down web services after 15 minutes of inactivity. Cold start for this Spring Boot app takes 30-60 seconds. A self-ping scheduler prevents this.

**`HealthController.java`** — lightweight health endpoint:
```java
@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "timestamp", Instant.now().toString());
    }
}
```

**`KeepAliveScheduler.java`** — self-ping every 14 minutes:
```java
@Component
@Slf4j
public class KeepAliveScheduler {
    @Scheduled(fixedRate = 840000) // 14 minutes (under Render's 15-min inactivity threshold)
    public void keepAlive() {
        try {
            new RestTemplate().getForObject("http://localhost:{port}/api/health", String.class,
                System.getenv().getOrDefault("PORT", "8080"));
            log.debug("Keep-alive ping successful");
        } catch (Exception e) {
            log.warn("Keep-alive ping failed: {}", e.getMessage());
        }
    }
}
```

Add `@EnableScheduling` on the main `AstroEngineApplication` class.

---

## Shared UI Components

| Component | Purpose |
|-----------|---------|
| `Navbar` | Top header with logo, language/location chips |
| `BottomTabBar` | Mobile bottom navigation (4 tabs) |
| `SideNavRail` | Desktop side navigation |
| `LocationAutocomplete` | Debounced search hitting `/api/v1/locations/autocomplete` |
| `BirthDetailsForm` | Reusable form: name, date, time, location, ayanamsa |
| `TimeSlotBar` | Horizontal colored time range visualizer |
| `SouthIndianChart` | SVG South Indian box chart with interactive aspects |
| `NorthIndianChart` | SVG North Indian diamond chart with interactive aspects |
| `HoraiTable` | 24-row hora time slot display |
| `ScoreGauge` | Circular progress for matching scores |
| `DoshamCard` | Dosham detail card with detected/nullified/active states |
| `DasaTimeline` | Expandable Maha Dasa → Bhukthi accordion |

---

## Error Handling

- **Loading:** Skeleton shimmer placeholders while API calls are in flight
- **API errors:** Toast notification with retry option
- **Offline:** Show cached last-viewed panchangam from localStorage with "offline" badge
- **Form validation:** Inline field validation before submission
- **Location service failure:** Fallback to Chennai default

---

## Verification Plan

### Automated Tests
- `mvn test` — existing Spring Boot integration tests
- Verify new `/comprehensive` endpoint returns all dosham/yoga data
- Verify enhanced `AstrologyDiagnosticsService` detects and nullifies correctly

### Manual Verification
1. `mvn package` succeeds and produces a single JAR with frontend bundled
2. `java -jar target/*.jar` starts and serves UI at `http://localhost:8080`
3. All 4 pages render correctly on mobile and desktop viewports
4. Language switching updates all labels
5. Chart rendering: South Indian for non-Hindi, North Indian for Hindi
6. Dosham tab shows detected/nullified/active states with reasons
7. PDF download includes enhanced dosham section
8. Docker build succeeds: `docker build -t jyothish-app .`
9. Docker run works: `docker run -p 8080:8080 jyothish-app`
10. Render deployment via `render.yaml` auto-deploys from GitHub push
