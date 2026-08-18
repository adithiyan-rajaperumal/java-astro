# Gemini 3.7 Flash, Multilingual TTS Queue, and Backup API Key Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade default AI model to `gemini-3.7-flash`, implement primary/backup dual API key automatic failover on rate limits, and enhance frontend Text-to-Speech with sentence chunking and Indic voice matching for full narrative playback.

**Architecture:** 
- In backend `GeminiProperties.java` and `GeminiPredictionService.java`, add `backupApiKey` support and a retry loop in `callGeminiApi()` to seamlessly handle HTTP 429 / 403 quota exhaustion.
- In frontend `useTextToSpeech.js`, implement script detection, exact Indic locale voice matching (`ta-IN`, `hi-IN`, `te-IN`, `kn-IN`, `ml-IN`, `en-IN`), and a sequential utterance queue to prevent browser audio cut-offs.

**Tech Stack:** Spring Boot 3.3.4, Java 17, Google Gemini 3.7 Flash API, React 18, Web Speech API (`SpeechSynthesis`), Vite 8.

## Global Constraints
- All 6 languages (`ta`, `hi`, `te`, `kn`, `ml`, `en`) must be seamlessly supported.
- `gemini-3.7-flash` must be the default model with full thinking budget support (1024 tokens).
- Dual API keys must support raw strings and `enc:` base64-encoded strings.
- All existing 97 backend unit tests and frontend build must pass without regressions.

---

### Task 1: Backend AI Model Upgrade & Backup API Key Failover

**Files:**
- Modify: `src/main/resources/application.yml:44`
- Modify: `src/main/java/org/vedic/astro/config/GeminiProperties.java`
- Modify: `src/main/java/org/vedic/astro/service/GeminiPredictionService.java`
- Test: `src/test/java/org/vedic/astro/GeminiPredictionServiceTest.java`

**Interfaces:**
- `GeminiProperties.getResolvedApiKeys()`: returns `List<String>` of decoded primary and backup API keys.
- `GeminiPredictionService.callGeminiApi(String systemInstruction, String prompt)`: tries available keys sequentially on HTTP 429/403/503.

- [ ] **Step 1: Update application.yml and GeminiProperties.java**

```yaml
# In application.yml:
gemini:
  api-key: ${GEMINI_API_KEY:}
  backup-api-key: ${GEMINI_BACKUP_API_KEY:}
  model: ${GEMINI_MODEL:gemini-3.7-flash}
```

```java
// In GeminiProperties.java:
private String apiKey = "";
private String backupApiKey = "";
private String model = "gemini-3.7-flash";

public List<String> getResolvedApiKeys() {
    List<String> keys = new ArrayList<>();
    String primary = resolveSingleKey(apiKey);
    if (!primary.isEmpty()) keys.add(primary);
    String backup = resolveSingleKey(backupApiKey);
    if (!backup.isEmpty() && !backup.equals(primary)) keys.add(backup);
    return keys;
}

private String resolveSingleKey(String key) {
    if (key == null || key.trim().isEmpty()) return "";
    String trimmed = key.trim();
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
```

- [ ] **Step 2: Update GeminiPredictionService callGeminiApi for multi-key failover and model pricing**

Update `callGeminiApi()` to iterate over `geminiProperties.getResolvedApiKeys()` and handle 429 / 403 / 503 status exceptions. Update `parseGeminiResponse()` token cost calculation to recognize `gemini-3.7-flash`.

- [ ] **Step 3: Run unit tests to verify backend compilation and failover handling**

Run: `mvn test -Dtest=GeminiPredictionServiceTest`
Expected: BUILD SUCCESS with all tests passing.

- [ ] **Step 4: Commit backend changes**

```bash
git add src/main/resources/application.yml src/main/java/org/vedic/astro/config/GeminiProperties.java src/main/java/org/vedic/astro/service/GeminiPredictionService.java
git commit -m "feat(ai): upgrade default model to gemini-3.7-flash and add backup API key failover"
```

---

### Task 2: Frontend Multilingual Text-to-Speech Sentence Queue & Voice Matching

**Files:**
- Modify: `frontend/src/utils/useTextToSpeech.js`
- Modify: `frontend/src/i18n/translations.js`

**Interfaces:**
- `useTextToSpeech({ language })`: returns `{ isSupported, isPlaying, isPaused, hasVoiceForLanguage, speak, pause, resume, stop }`.

- [ ] **Step 1: Enhance useTextToSpeech.js with Sentence Chunking & Queue Manager**

Implement:
1. `splitIntoSentenceChunks(text)`: splits by sentence boundaries while keeping sentences intact.
2. `getBestMatchingVoice(lang, availableVoices)`: matches exact Indic language voices (`ta-IN`, `hi-IN`, `te-IN`, `kn-IN`, `ml-IN`, `en-IN`/`en-US`).
3. Utterance queue manager: sequentially speaks each chunk via `utterance.onend`.
4. Expose `hasVoiceForLanguage` to indicate if the browser has the native voice.

- [ ] **Step 2: Add translation keys for TTS voice availability notice**

Add `ttsNoVoiceWarning` across all 6 languages in `translations.js`.

- [ ] **Step 3: Verify frontend build**

Run: `npm run build` in `frontend/`
Expected: Build succeeds cleanly.

- [ ] **Step 4: Commit TTS hook updates**

```bash
git add frontend/src/utils/useTextToSpeech.js frontend/src/i18n/translations.js
git commit -m "feat(tts): add sentence chunk queue and exact Indic voice matcher"
```

---

### Task 3: Views Integration for AI Balan and Daily Balan

**Files:**
- Modify: `frontend/src/components/AiPredictionsView.jsx`
- Modify: `frontend/src/components/DailyBalanView.jsx`

- [ ] **Step 1: Enhance AiPredictionsView.jsx audio text compilation & voice notice**

Ensure `buildSpeechText()` builds full formatted text of personality, milestones, longevity analysis rationale, and all yearly predictions in order.

- [ ] **Step 2: Enhance DailyBalanView.jsx audio text compilation & voice notice**

Ensure `buildSpeechText()` builds the full daily forecast narrative and all domain guidance cleanly in the active language.

- [ ] **Step 3: Test frontend build**

Run: `npm run build` in `frontend/`
Expected: Build succeeds cleanly.

- [ ] **Step 4: Commit view enhancements**

```bash
git add frontend/src/components/AiPredictionsView.jsx frontend/src/components/DailyBalanView.jsx
git commit -m "feat(ui): connect full prediction narratives to TTS sentence queue in AI and Daily Balan views"
```

---

### Task 4: Full System Verification

- [ ] **Step 1: Run complete maven test suite**

Run: `mvn clean test`
Expected: `BUILD SUCCESS`, 97+ tests passing with 0 failures.

- [ ] **Step 2: Run frontend production build**

Run: `npm run build` in `frontend/`
Expected: Vite build succeeds with 0 errors.
