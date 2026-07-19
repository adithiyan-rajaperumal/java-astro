# Jyothish Application

A premium, full-stack Vedic Astrology Matching & Diagnostics Engine. This platform enables users to compute horoscopic features, analyze complex doshams and yogas (with built-in nullification logic), view real-time Panchangam details, and export premium PDF reports.

The system is built on Spring Boot 3 & React 18, integrated with Swiss Ephemeris calculations, and is configured for rapid Docker/Render cloud deployment.

---

## Key Functionalities

- **Advanced Dosham Engine:** Diagnostic scanning for 7 major doshams:
  - *Sevvai Dosham* (Mars Affliction)
  - *Kala Sarpa Dosham*
  - *Sarpam / Naga Dosham*
  - *Pithru Dosham*
  - *Putra Dosham*
  - *Kalathira Dosham*
  - *Shani Dosham*
  - **Nullification Logic:** Checks aspects, exaltation, and benefic configurations (such as Mars in own sign or aspected by Jupiter) to cancel/reduce dosham severity.
- **Yoga Calculator:** Identifies key planetary yogas including Gajakesari, Budha-Aditya, Chandra-Mangala, and Pancha Mahapurusha yogas (Ruchaka, Bhadra, Hamsa, Malavya, Sasa).
- **Daily Panchangam:** Evaluates Tithi, Nakshatram, Yogam, Karanam, and calculates Rahu Kalam, Yamagandam, and Gulika Kalam.
- **PDF Export:** Generates customizable PDF report files.
- **Multi-language Support:** Ready for English, Tamil, Kannada, Hindi, Telugu, and Malayalam translations.

---

## UI Design & Aesthetics

The React frontend features:
- **Sleek Dark Theme:** Modern deep navy background (`#0f0f1a`) paired with accent gold (`#d4a843`) and saffron warm tones.
- **Responsive Design:** Auto-aligns dashboard items, chart layouts, and forms on mobile, tablet, and desktop viewports.
- **Interactive Charts:** Clear visuals showing planetary positions, house listings, and dignity evaluations.

---

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.3.4, Swiss Ephemeris (`swisseph` port), OpenPDF (for exports).
- **Frontend:** React 18, Vite, CSS (Vanilla Custom Theme).
- **Database/Storage:** In-memory configuration / static resources.
- **DevOps:** Docker, Multi-Stage Builds, Render cloud automation (`render.yaml`).

---

## API Reference & Swagger Testing

For detailed, interactive testing of all REST APIs, run the application and navigate to the **Swagger UI**:
- URL: `http://localhost:8080/swagger-ui.html`

### Primary API Endpoint Sample
`POST /api/v1/astrology/calculate`

Exposed to generate complete natal horoscope dashboard details.

#### Sample Request Payload:
```json
{
  "name": "Adithiyan",
  "year": 1995,
  "month": 5,
  "day": 10,
  "hour": 8,
  "minute": 30,
  "longitude": 80.2707,
  "latitude": 13.0827,
  "timezone": "Asia/Kolkata"
}
```

#### Sample Response Shape:
```json
{
  "birthDetails": {
    "name": "Adithiyan",
    "year": 1995,
    "month": 5,
    "day": 10,
    "hour": 8,
    "minute": 30,
    "longitude": 80.2707,
    "latitude": 13.0827,
    "timezone": "Asia/Kolkata"
  },
  "lagna": "Gemini",
  "rasi": "Virgo",
  "nakshatra": "Uttara Phalguni",
  "planetaryPositions": [
    {
      "planet": "Sun",
      "sign": "Aries",
      "longitude": 25.4,
      "house": 11,
      "isRetrograde": false,
      "dignity": "EXALTED"
    }
  ],
  "doshas": [
    {
      "doshaName": "Sevvai Dosham",
      "detected": true,
      "active": false,
      "nullified": true,
      "nullificationReason": "Mars is in own or exalted sign.",
      "severity": "Cancelled",
      "remedy": "Perform prayers at Vaideeswaran Koil."
    }
  ],
  "yogas": [
    {
      "yogaName": "Gajakesari Yoga",
      "description": "Jupiter is in Kendra from Moon. Brings wealth, intelligence, and virtue."
    }
  ]
}
```

---

## How to Run Locally

### Prerequisites
- JDK 17
- Maven 3+
- Node.js 18+

### Backend & Frontend Combined Dev
1. Build the frontend (optional manual build):
   ```bash
   cd frontend
   npm install
   npm run build
   cd ..
   ```
2. Launch Spring Boot:
   ```bash
   mvn spring-boot:run
   ```
   *Note: The frontend is embedded via the Maven build plugin, which auto-installs Node.js packages and builds assets into `/classes/static` during maven packaging.*

### Production Docker Runner
Build the combined container:
```bash
docker build -t jyothish-app .
docker run -p 8080:8080 jyothish-app
```
Visit the application at `http://localhost:8080`.
