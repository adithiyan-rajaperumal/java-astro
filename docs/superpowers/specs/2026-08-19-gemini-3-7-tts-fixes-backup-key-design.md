# Design Specification: Gemini 3.7 Flash, Multilingual TTS Queue, and Backup API Key Failover

**Date**: 2026-08-19  
**Status**: Approved  
**Scope**: Model upgrade to `gemini-3.7-flash`, dynamic SpeechSynthesis sentence queue for full Indic text-to-speech playback, and dual API key failover for quota exhaustion.

---

## 1. Overview & Problem Statement
1. **AI Model Version**: Upgrade default model to `gemini-3.7-flash` across backend configuration, controllers, and tests while preserving thinking budget and token cost estimations.
2. **Text-to-Speech (TTS) Full Balan Playback**: Address speech synthesis stopping after dates or skipping Indic characters by:
   - Chunking long narrative text into sentence-level utterances.
   - Enforcing exact matching of Indic voices (`ta-IN`, `hi-IN`, `te-IN`, `kn-IN`, `ml-IN`, `en-IN`/`en-US`).
   - Providing visual indicators if an Indic language voice is unavailable on the client OS.
3. **Backup API Key Failover**: Prevent service disruptions when rate limits (HTTP 429) or token quotas are exceeded by implementing automatic failover to a backup API key (`GEMINI_BACKUP_API_KEY`).

---

## 2. Architecture & Components

### 2.1 Backend AI Model Configuration
- Update `application.yml` and `GeminiProperties.java`:
  - `gemini.model: ${GEMINI_MODEL:gemini-3.7-flash}`
  - Default thinking budget: 1024 tokens.
- Update `PredictionResponseDTO` token usage calculations in `GeminiPredictionService.java` to support `gemini-3.7-flash`.

### 2.2 Dual API Key Failover System
- **Properties (`GeminiProperties.java`)**:
  - `apiKey`: Primary Gemini API Key (`GEMINI_API_KEY`)
  - `backupApiKey`: Backup/Secondary Gemini API Key (`GEMINI_BACKUP_API_KEY`)
  - `getResolvedApiKeys()`: Returns a prioritized list of available keys (resolving raw and `enc:` base64 strings).
- **Execution & Failover (`GeminiPredictionService.java`)**:
  - `callGeminiApi()` attempts the primary key.
  - On HTTP 429 (`RESOURCE_EXHAUSTED`), 403 (Quota Exceeded), or 503, logs a warning and retries with the secondary backup key.

### 2.3 Frontend Multilingual Text-to-Speech Engine
- **Custom Hook (`useTextToSpeech.js`)**:
  - **Sentence Chunking**: Splits input text into discrete sentence chunks based on punctuation delimiters (`.`, `!`, `?`, `\n`, `|`).
  - **Queue Manager**: Manages an internal queue of utterances, sequentially speaking each chunk using `onend` events.
  - **Voice Matching**: Prioritizes native language voices (`ta-IN`, `hi-IN`, `te-IN`, `kn-IN`, `ml-IN`, `en-IN`).
  - **Voice Availability Check**: Exposes `hasVoiceForLanguage` flag so the UI can notify the user if their browser lacks an Indic voice.
- **Views Integration (`AiPredictionsView.jsx`, `DailyBalanView.jsx`)**:
  - Formats the entire prediction payload (intro, domain analysis, planetary highlights, remedies) into structured speech text.

---

## 3. Verification Plan

### 3.1 Automated Tests
- Unit test in `GeminiPredictionServiceTest.java` verifying `gemini-3.7-flash` model usage.
- Unit test verifying multi-key failover handling when primary key encounters 429.
- Run `mvn clean test` across the full test suite.

### 3.2 Frontend Build & Manual Verification
- Run `npm run build` in `frontend/`.
- Verify TTS playback of full Tamil, Hindi, and English predictions without truncating after the date.
