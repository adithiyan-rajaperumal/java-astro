import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * Custom hook for browser-native Text-to-Speech (SpeechSynthesis API).
 * Features:
 * - Robust sentence-level chunk queueing to bypass browser utterance length limits.
 * - Script-aware Indic language voice detection (ta-IN, hi-IN, te-IN, kn-IN, ml-IN, en-IN/en-US).
 * - Full audio controls: Play, Pause, Resume, Stop.
 * - Detection of native voice availability for warning notices.
 */
export function useTextToSpeech({ language = 'en' } = {}) {
  const [isSupported, setIsSupported] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [availableVoices, setAvailableVoices] = useState([]);
  const [hasVoiceForLanguage, setHasVoiceForLanguage] = useState(true);

  const queueRef = useRef([]);
  const activeUtteranceRef = useRef(null);
  const isPlayingRef = useRef(false);
  const isPausedRef = useRef(false);

  // Detect script from text content to ensure correct locale even if language setting differs
  const detectTargetLocale = useCallback((rawText, fallbackLang) => {
    if (!rawText) return getLocaleFromLang(fallbackLang);

    // Tamil
    if (/[\u0B80-\u0BFF]/.test(rawText)) return 'ta-IN';
    // Devanagari (Hindi)
    if (/[\u0900-\u097F]/.test(rawText)) return 'hi-IN';
    // Telugu
    if (/[\u0C00-\u0C7F]/.test(rawText)) return 'te-IN';
    // Kannada
    if (/[\u0C80-\u0CFF]/.test(rawText)) return 'kn-IN';
    // Malayalam
    if (/[\u0D00-\u0D7F]/.test(rawText)) return 'ml-IN';

    return getLocaleFromLang(fallbackLang);
  }, []);

  const getLocaleFromLang = (lang) => {
    switch (lang) {
      case 'ta': return 'ta-IN';
      case 'hi': return 'hi-IN';
      case 'te': return 'te-IN';
      case 'kn': return 'kn-IN';
      case 'ml': return 'ml-IN';
      case 'en': return 'en-IN';
      default: return 'en-US';
    }
  };

  // Clean text by stripping emojis, markdown symbols, and excess whitespace for clear speech synthesis
  const cleanSpeechText = (rawText) => {
    if (!rawText) return '';
    return rawText
      // Remove URLs
      .replace(/https?:\/\/\S+/gi, '')
      // Remove markdown links [text](url) -> text
      .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
      // Remove markdown bold/italics/code/headers
      .replace(/[*_`#~]/g, ' ')
      // Remove bullets/dashes and leading icons
      .replace(/^[\s*•\-–—✦✔⚠☸✨🧠🕰️⏳📜🌟🎯💾🤖💵⚡🎙️📅🌙⭐🪐]+/gm, '')
      // Remove general emojis
      .replace(/[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F1E6}-\u{1F1FF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}]/gu, ' ')
      // Normalize whitespace
      .replace(/\s+/g, ' ')
      .trim();
  };

  // Split long text into natural sentence chunks (~100-200 chars) to prevent Chrome 15s cutoff bug
  const splitIntoSentenceChunks = (text) => {
    if (!text) return [];
    // Split by sentence terminators (., !, ?, |, or linebreaks)
    const rawChunks = text.split(/(?<=[.!?।|\n])\s+/);
    const result = [];

    for (let chunk of rawChunks) {
      const trimmed = chunk.trim();
      if (!trimmed) continue;

      if (trimmed.length > 220) {
        // Sub-split by commas or phrases if exceptionally long
        const subParts = trimmed.split(/(?<=[,;])\s+/);
        let temp = '';
        for (let sub of subParts) {
          if ((temp + ' ' + sub).length > 200 && temp.length > 0) {
            result.push(temp.trim());
            temp = sub;
          } else {
            temp = temp ? temp + ' ' + sub : sub;
          }
        }
        if (temp.trim()) result.push(temp.trim());
      } else {
        result.push(trimmed);
      }
    }
    return result;
  };

  const findBestVoice = useCallback((targetLocale, voices) => {
    if (!voices || voices.length === 0) return null;
    const baseLang = targetLocale.split('-')[0].toLowerCase();

    // 1. Exact match (e.g. 'ta-IN')
    let match = voices.find(v => v.lang && v.lang.toLowerCase().replace('_', '-') === targetLocale.toLowerCase());
    if (match) return match;

    // 2. Base language prefix match (e.g. 'ta')
    match = voices.find(v => v.lang && v.lang.toLowerCase().startsWith(baseLang));
    if (match) return match;

    // 3. Indian English or general English if base is English
    if (baseLang === 'en') {
      match = voices.find(v => v.lang && v.lang.toLowerCase().includes('en-in'))
        || voices.find(v => v.lang && v.lang.toLowerCase().startsWith('en'));
      if (match) return match;
    }

    return null;
  }, []);

  useEffect(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      setIsSupported(true);
      const updateVoices = () => {
        const voices = window.speechSynthesis.getVoices();
        setAvailableVoices(voices);

        const targetLocale = getLocaleFromLang(language);
        const voice = findBestVoice(targetLocale, voices);
        setHasVoiceForLanguage(!!voice || language === 'en');
      };

      updateVoices();
      window.speechSynthesis.onvoiceschanged = updateVoices;

      return () => {
        if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
          window.speechSynthesis.cancel();
        }
      };
    }
  }, [language, findBestVoice]);

  const stop = useCallback(() => {
    queueRef.current = [];
    activeUtteranceRef.current = null;
    isPlayingRef.current = false;
    isPausedRef.current = false;
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    setIsPlaying(false);
    setIsPaused(false);
  }, []);

  const pause = useCallback(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window && isPlayingRef.current && !isPausedRef.current) {
      window.speechSynthesis.pause();
      isPausedRef.current = true;
      setIsPaused(true);
    }
  }, []);

  const resume = useCallback(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window && isPlayingRef.current && isPausedRef.current) {
      window.speechSynthesis.resume();
      isPausedRef.current = false;
      setIsPaused(false);
    }
  }, []);

  const speakNextInQueue = useCallback(() => {
    if (!isPlayingRef.current || isPausedRef.current) return;

    if (queueRef.current.length === 0) {
      isPlayingRef.current = false;
      isPausedRef.current = false;
      activeUtteranceRef.current = null;
      setIsPlaying(false);
      setIsPaused(false);
      return;
    }

    const { text, targetLocale, voice } = queueRef.current.shift();
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = targetLocale;
    if (voice) {
      utterance.voice = voice;
    }
    utterance.rate = 0.95;
    utterance.pitch = 1.0;

    utterance.onend = () => {
      if (isPlayingRef.current) {
        speakNextInQueue();
      }
    };

    utterance.onerror = (e) => {
      if (e.error !== 'interrupted' && e.error !== 'canceled') {
        console.warn('Speech synthesis error:', e);
      }
      if (isPlayingRef.current) {
        speakNextInQueue();
      }
    };

    activeUtteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, []);

  const speak = useCallback((textToRead) => {
    if (!isSupported || !textToRead) return;

    // Reset current speech
    stop();

    const cleaned = cleanSpeechText(textToRead);
    if (!cleaned) return;

    const chunks = splitIntoSentenceChunks(cleaned);
    if (chunks.length === 0) return;

    const targetLocale = detectTargetLocale(cleaned, language);
    const voice = findBestVoice(targetLocale, availableVoices);

    queueRef.current = chunks.map(chunk => ({
      text: chunk,
      targetLocale,
      voice
    }));

    isPlayingRef.current = true;
    isPausedRef.current = false;
    setIsPlaying(true);
    setIsPaused(false);

    speakNextInQueue();
  }, [isSupported, language, availableVoices, detectTargetLocale, findBestVoice, speakNextInQueue, stop]);

  return {
    isSupported,
    isPlaying,
    isPaused,
    hasVoiceForLanguage,
    speak,
    pause,
    resume,
    stop
  };
}
