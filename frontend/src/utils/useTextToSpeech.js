import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * Robust Multilingual Text-to-Speech Hook.
 * Features:
 * - Dual engine: Browser SpeechSynthesis + Backend High-Fidelity Indic Audio Stream (/api/v1/tts).
 * - Bypasses OS limitations (e.g. Windows machines missing Tamil/Hindi/Telugu/Kannada/Malayalam voice packs).
 * - Automatic sentence-level chunk queue with Play, Pause, Resume, and Stop controls.
 * - Perfect pronunciation of both words and numbers across all 6 languages.
 */
export function useTextToSpeech({ language = 'en' } = {}) {
  const [isSupported, setIsSupported] = useState(true);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [availableVoices, setAvailableVoices] = useState([]);
  const [hasVoiceForLanguage, setHasVoiceForLanguage] = useState(true);

  const queueRef = useRef([]);
  const activeUtteranceRef = useRef(null);
  const activeAudioRef = useRef(null);
  const isPlayingRef = useRef(false);
  const isPausedRef = useRef(false);

  // Script detection
  const detectTargetLocale = useCallback((rawText, fallbackLang) => {
    if (!rawText) return getLocaleFromLang(fallbackLang);
    if (/[\u0B80-\u0BFF]/.test(rawText)) return 'ta-IN';
    if (/[\u0900-\u097F]/.test(rawText)) return 'hi-IN';
    if (/[\u0C00-\u0C7F]/.test(rawText)) return 'te-IN';
    if (/[\u0C80-\u0CFF]/.test(rawText)) return 'kn-IN';
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

  const cleanSpeechText = (rawText) => {
    if (!rawText) return '';
    return rawText
      .replace(/https?:\/\/\S+/gi, '')
      .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
      .replace(/[*_`#~]/g, ' ')
      .replace(/^[\s*•\-–—✦✔⚠☸✨🧠🕰️⏳📜🌟🎯💾🤖💵⚡🎙️📅🌙⭐🪐]+/gm, '')
      .replace(/[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F1E6}-\u{1F1FF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}]/gu, ' ')
      .replace(/\s+/g, ' ')
      .trim();
  };

  const splitIntoSentenceChunks = (text) => {
    if (!text) return [];
    const rawChunks = text.split(/(?<=[.!?।|\n])\s+/);
    const result = [];

    for (let chunk of rawChunks) {
      const trimmed = chunk.trim();
      if (!trimmed) continue;

      if (trimmed.length > 180) {
        const subParts = trimmed.split(/(?<=[,;])\s+/);
        let temp = '';
        for (let sub of subParts) {
          if ((temp + ' ' + sub).length > 160 && temp.length > 0) {
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

    let match = voices.find(v => v.lang && v.lang.toLowerCase().replace('_', '-') === targetLocale.toLowerCase());
    if (match) return match;

    match = voices.find(v => v.lang && v.lang.toLowerCase().startsWith(baseLang));
    if (match) return match;

    if (baseLang === 'en') {
      match = voices.find(v => v.lang && v.lang.toLowerCase().includes('en-in'))
        || voices.find(v => v.lang && v.lang.toLowerCase().startsWith('en'));
      if (match) return match;
    }

    return null;
  }, []);

  useEffect(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      const updateVoices = () => {
        const voices = window.speechSynthesis.getVoices();
        setAvailableVoices(voices);

        const targetLocale = getLocaleFromLang(language);
        const voice = findBestVoice(targetLocale, voices);
        // We always have speech capability because of the backend audio fallback
        setHasVoiceForLanguage(true);
      };

      updateVoices();
      window.speechSynthesis.onvoiceschanged = updateVoices;

      return () => {
        if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
          window.speechSynthesis.cancel();
        }
        if (activeAudioRef.current) {
          activeAudioRef.current.pause();
          activeAudioRef.current.src = '';
        }
      };
    }
  }, [language, findBestVoice]);

  const stop = useCallback(() => {
    queueRef.current = [];
    activeUtteranceRef.current = null;
    if (activeAudioRef.current) {
      activeAudioRef.current.pause();
      activeAudioRef.current.src = '';
      activeAudioRef.current = null;
    }
    isPlayingRef.current = false;
    isPausedRef.current = false;
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    setIsPlaying(false);
    setIsPaused(false);
  }, []);

  const pause = useCallback(() => {
    if (isPlayingRef.current && !isPausedRef.current) {
      if (activeAudioRef.current) {
        activeAudioRef.current.pause();
      } else if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
        window.speechSynthesis.pause();
      }
      isPausedRef.current = true;
      setIsPaused(true);
    }
  }, []);

  const resume = useCallback(() => {
    if (isPlayingRef.current && isPausedRef.current) {
      if (activeAudioRef.current) {
        activeAudioRef.current.play().catch(e => console.warn('Audio play resume error', e));
      } else if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
        window.speechSynthesis.resume();
      }
      isPausedRef.current = false;
      setIsPaused(false);
    }
  }, []);

  const playNextInQueue = useCallback(() => {
    if (!isPlayingRef.current || isPausedRef.current) return;

    if (queueRef.current.length === 0) {
      isPlayingRef.current = false;
      isPausedRef.current = false;
      activeUtteranceRef.current = null;
      activeAudioRef.current = null;
      setIsPlaying(false);
      setIsPaused(false);
      return;
    }

    const { text, targetLocale, voice, langCode } = queueRef.current.shift();

    // If we have a native voice for this language and it's English or matched native voice
    const isIndic = ['ta', 'hi', 'te', 'kn', 'ml'].includes(langCode);
    const useNativeSpeechSynthesis = !isIndic && voice && typeof window !== 'undefined' && 'speechSynthesis' in window;

    if (useNativeSpeechSynthesis) {
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = targetLocale;
      utterance.voice = voice;
      utterance.rate = 0.95;
      utterance.pitch = 1.0;

      utterance.onend = () => {
        if (isPlayingRef.current) {
          playNextInQueue();
        }
      };

      utterance.onerror = (e) => {
        if (e.error !== 'interrupted' && e.error !== 'canceled') {
          console.warn('Speech synthesis error:', e);
        }
        if (isPlayingRef.current) {
          playNextInQueue();
        }
      };

      activeUtteranceRef.current = utterance;
      window.speechSynthesis.speak(utterance);
    } else {
      // Use high-fidelity audio stream from backend TTS proxy
      const audioUrl = `/api/v1/tts?text=${encodeURIComponent(text)}&lang=${encodeURIComponent(langCode)}`;
      const audio = new Audio(audioUrl);
      activeAudioRef.current = audio;

      audio.onended = () => {
        if (isPlayingRef.current) {
          playNextInQueue();
        }
      };

      audio.onerror = (e) => {
        console.warn('Audio streaming fallback error:', e);
        if (isPlayingRef.current) {
          playNextInQueue();
        }
      };

      audio.play().catch(e => {
        console.warn('Audio playback start error:', e);
        if (isPlayingRef.current) {
          playNextInQueue();
        }
      });
    }
  }, []);

  const speak = useCallback((textToRead) => {
    if (!textToRead) return;

    // Reset current speech
    stop();

    const cleaned = cleanSpeechText(textToRead);
    if (!cleaned) return;

    const chunks = splitIntoSentenceChunks(cleaned);
    if (chunks.length === 0) return;

    const targetLocale = detectTargetLocale(cleaned, language);
    const langCode = targetLocale.split('-')[0].toLowerCase();
    const currentVoices = typeof window !== 'undefined' && 'speechSynthesis' in window 
      ? window.speechSynthesis.getVoices() 
      : availableVoices;
    const voice = findBestVoice(targetLocale, currentVoices);

    queueRef.current = chunks.map(chunk => ({
      text: chunk,
      targetLocale,
      langCode,
      voice
    }));

    isPlayingRef.current = true;
    isPausedRef.current = false;
    setIsPlaying(true);
    setIsPaused(false);

    playNextInQueue();
  }, [language, availableVoices, detectTargetLocale, findBestVoice, playNextInQueue, stop]);

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

export default useTextToSpeech;
