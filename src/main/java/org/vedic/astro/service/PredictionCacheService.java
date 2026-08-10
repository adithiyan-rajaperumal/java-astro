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
    private final Map<String, CacheEntry<org.vedic.astro.matching.dto.MatchingAiPredictionDTO>> matchingCache = new ConcurrentHashMap<>();

    private record CacheEntry<T>(T data, LocalDateTime expiresAt) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }

    public String generateMatchingKey(org.vedic.astro.matching.dto.MatchingRequestDTO req, String lang) {
        if (req == null || req.boy() == null || req.girl() == null) return "unknown";
        String raw = String.format("MATCH_%s_%d_%d_%d_%s_%d_%d_%d_%s_%s_%s",
                req.boy().name(), req.boy().year(), req.boy().month(), req.boy().day(),
                req.girl().name(), req.girl().year(), req.girl().month(), req.girl().day(),
                req.matchingSystem(), req.strictness(), lang);
        return sha256(raw);
    }

    public org.vedic.astro.matching.dto.MatchingAiPredictionDTO getMatchingPrediction(String key) {
        CacheEntry<org.vedic.astro.matching.dto.MatchingAiPredictionDTO> entry = matchingCache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.data();
        }
        matchingCache.remove(key);
        return null;
    }

    public void putMatchingPrediction(String key, org.vedic.astro.matching.dto.MatchingAiPredictionDTO data) {
        if (data != null && data.isEnabled()) {
            matchingCache.put(key, new CacheEntry<>(data, LocalDateTime.now().plusHours(3)));
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
