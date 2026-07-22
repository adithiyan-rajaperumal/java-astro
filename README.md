# DrikVedic - Vedic Astrology & Panchangam Platform 🪔

A premium, full-stack Vedic Astrology Matching, Panchangam & Diagnostic Engine. **DrikVedic** enables users to calculate high-precision daily Panchangam, analyze natal horoscopes, evaluate classical Rajayogas and Doshams (with comprehensive nullification logic), view South Indian and North Indian charts, and generate multi-language PDF reports.

The platform is built on **Spring Boot 3 & React 18**, powered by **Swiss Ephemeris (`swisseph`)** astronomical calculations, and styled with a mobile-first warm saffron theme.

---

## 🌟 Key Features & Functionalities

### 1. 📅 Precision Daily Panchangam
- **Core Panchangam Elements**: Accurate computation of Tithi, Nakshatram, Yogam, and Karanam with exact transition end times and next element listing.
- **Natural Language Formatting**: Clean transition strings (e.g. `Shukla Paksha Ekadashi until 04:19 AM (Next Day), then Dvadashi until 06:21 AM (Next Day)`) eliminating redundant start-time ranges.
- **Exact Astronomical Chandrastamam**: Computes the exact 8th-house Chandrastamam Janma Nakshatra based on the active sunrise Moon Nakshatra using exact distance math (`((N + 12 - 1) % 27) + 1`).
- **Auspicious & Inauspicious Timings**:
  - Sunrise, Sunset, Moonrise, Moonset.
  - Nalla Neram & Gowri Nalla Neram (Day & Night).
  - Rahu Kalam, Yamagandam, Kulikai.
  - Full 24-Hour Planetary Horai Table (12 Day Horais & 12 Night Horais).
- **Special Day Badges**: Automatic detection and highlighting of **Muhurtham Days** (`netram > 0 && jeevan > 0`) and **Vasthu Days**.

---

### 2. 🛡️ Advanced Dosha Diagnostic & Nullification Engine
Diagnostic scanning for **7 major Doshams** with comprehensive classical nullification rules (*Brihat Parashara Hora Shastra*, *Phaladeepika*, *Deva Keralam*):

1. **Sevvai (Manglik) Dosham**:
   - Scans 1st, 2nd, 4th, 7th, 8th, and 12th houses from Lagna/Moon.
   - **Exemptions**: Mars in own sign/exalted sign, conjunct/aspected by Jupiter or Venus, conjunct Moon (*Chandra-Mangala*), Mars in Leo or Cancer, and house-sign exemptions (2nd in Gemini/Virgo/Capricorn/Aquarius, 4th in Aries/Scorpio/Cancer, 7th in Cancer/Capricorn/Taurus/Libra, 8th in Sagittarius/Pisces/Gemini/Virgo, 12th in Taurus/Libra/Sagittarius/Pisces).
2. **Kala Sarpa Dosham**: Hemmed planet detection with conjunct/aspect exemptions by Jupiter or Rahu/Ketu in Kendra/Trikona.
3. **Sarpam / Naga Dosham**: Rahu/Ketu in houses 1, 2, 5, 7, 8 with Jupiter Kendra aspect and benefic conjunction nullifications.
4. **Pithru Dosham**: Sun afflicted in 9th house with Jupiter aspect and own sign/exaltation cancellations.
5. **Putra Dosham**: Malefics in 5th house with Jupiter/benefic aspect and fertile sign exemptions.
6. **Kalathira Dosham**: Malefics in 7th house with Venus strength and benefic aspect cancellations.
7. **Shani Dosham**: Saturn in non-Upachaya houses with Yogakaraka (Taurus/Libra Lagna), own sign/exaltation, and sign exemptions.

---

### 3. 👑 Classical Rajayoga Engine
Identifies active auspicious Yogas and planetary combinations:

- **Full 4-Rule Parashari Neechabhanga Rajayoga**: Evaluates dispositor in Kendra from Lagna/Moon, exaltation lord in Kendra from Lagna/Moon, or direct aspect/conjunction by sign/exaltation lords.
- **Vipareeta Rajayoga**: Lords of Dusthana houses (6th, 8th, 12th) situated in Dusthana houses (*Harsha*, *Sarala*, *Vimala* Yogas).
- **Dharma-Karmadhipati Yoga**: Conjunction or mutual aspect of 9th lord (Dharma) and 10th lord (Karma).
- **Kendra-Trikona Rajayoga**: Conjunction of a Kendra lord (1, 4, 7, 10) and a Trikona lord (5, 9).
- **Pancha Mahapurusha Yogas**: *Ruchaka* (Mars), *Bhadra* (Mercury), *Hamsa* (Jupiter), *Malavya* (Venus), *Sasa* (Saturn).
- **Additional Yogas**: *Gajakesari Yoga*, *Budha-Aditya Yoga*, *Chandra-Mangala Yoga*.

---

### 4. 🗺️ Dual Chart Visualizer (South & North Indian)
- **South Indian Square Chart**: Classical grid with planet badges, retrograde indicators, and 4-sided active selection borders.
- **North Indian Diamond Chart**: Interactive SVG diamond Kundali renderer (`renderNorthIndian`) displaying 12 house triangular/diamond polygons, sign numbers, planet short names, and an interactive house details panel.

---

### 5. 🌐 Mobile-First Responsive Design & i18n
- **Mobile-First UX**: Responsive tab scroll navigation, full viewport containment (`max-width: 100vw; overflow-x: hidden;`), and zero horizontal overflow.
- **Modern Warm Saffron Aesthetic**: Warm saffron accents (`#ff6b00`, `#d48806`) on light cards (`#ffffff`, `#fffaf4`) with custom vector SVG branding (`AstroLogo`).
- **Clickable Header Redirect**: Quick location button in header (`📍 Chennai`) with instant redirect to the Settings page.
- **Multi-Language Support**: Full translations across 6 languages:
  - English (`en`), Tamil (`ta`), Hindi (`hi`), Kannada (`kn`), Telugu (`te`), Malayalam (`ml`).
- **Indic Typography**: Native font stack integration (`'Mukta Malar'`, `'Catamaran'`, `'Noto Sans Tamil'`) for clean Tamil script rendering without line clipping.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.3.4, Swiss Ephemeris (`swisseph`), OpenPDF (for PDF exports), Lombok, Jackson.
- **Frontend**: React 18, Vite 8, Vanilla CSS Design System, Lucide/SVG Icons.
- **DevOps & Infrastructure**: Docker, Multi-Stage Maven Build, Render cloud automation (`render.yaml`).

---

## 🚀 How to Run Locally

### Prerequisites
- JDK 17+
- Maven 3.8+
- Node.js 18+

### Launching Dev Application
1. **Frontend Build & Dev**:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
2. **Spring Boot Backend**:
   ```bash
   mvn spring-boot:run
   ```
   Access the frontend at `http://localhost:5173` (Vite) or the combined Spring Boot app at `http://localhost:8080`.

### Production Docker Container
```bash
docker build -t drikvedic-app .
docker run -p 8080:8080 drikvedic-app
```
Visit `http://localhost:8080`.

---

## 📖 API Documentation & Swagger UI

For interactive testing of all REST API endpoints, open Swagger UI:
- **Swagger URL**: `http://localhost:8080/swagger-ui.html`

### Key Endpoints
- `GET /api/v1/panchangam/daily` — Computes complete daily Panchangam, Horais, and Nalla Neram for specified date & coordinates.
- `POST /api/v1/astrology/calculate` — Generates natal birth chart, planetary positions, Doshams, and Yogas.
- `POST /api/v1/astrology/match` — Evaluates 10-Porutham marriage matching score between two charts.
- `POST /api/v1/astrology/export-pdf` — Generates a downloadable PDF horoscope report.

---

## 📄 License
Distributed under the MIT License. Built for Vedic Astrology research and calculation.
