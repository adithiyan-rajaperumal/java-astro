import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * Custom hook for browser-native Text-to-Speech (SpeechSynthesis API).
 * Supports Indian multilingual voice selection (ta-IN, hi-IN, te-IN, kn-IN, ml-IN, en-IN/en-US),
 * text normalization (stripping markdown/emojis), and playback controls (Play, Pause, Resume, Stop).
 */
export function useTextToSpeech({ language = 'en' } = {}) {
  const [isSupported, setIsSupported] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [availableVoices, setAvailableVoices] = useState([]);
  const utteranceRef = useRef(null);

  // Clean text by stripping emojis, markdown symbols, and excess whitespace for clear speech synthesis
  const cleanSpeechText = (rawText) => {
    if (!rawText) return '';
    return rawText
      // Remove URLs
      .replace(/https?:\/\/\S+/gi, '')
      // Remove markdown links [text](url) -> text
      .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
      // Remove markdown bold/italics/code
      .replace(/[*_`#~]/g, ' ')
      // Remove bullets/dashes at start of lines
      .replace(/^[\s*•\-–—✦✔⚠☸✨🧠🕰️⏳📜🌟🎯💾🤖💵⚡]+/gm, '')
      // Remove general emojis
      .replace(/[\u{1F300}-\u{1F9FF}\u{2600}-\u{26FF}\u{2700}-\u{27BF}\u{1F1E6}-\u{1F1FF}\u{1F600}-\u{1F64F}\u{1F680}-\u{1F6FF}]/gu, ' ')
      // Normalize whitespace
      .replace(/\s+/g, ' ')
      .trim();
  };

  const getTargetLocale = (lang) => {
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

  useEffect(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      setIsSupported(true);
      const updateVoices = () => {
        const voices = window.speechSynthesis.getVoices();
        setAvailableVoices(voices);
      };
      updateVoices();
      window.speechSynthesis.onvoiceschanged = updateVoices;
      return () => {
        window.speechSynthesis.cancel();
      };
    }
  }, []);

  const stop = useCallback(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
    setIsPlaying(false);
    setIsPaused(false);
  }, []);

  const pause = useCallback(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window && isPlaying && !isPaused) {
      window.speechSynthesis.pause();
      setIsPaused(true);
    }
  }, [isPlaying, isPaused]);

  const resume = useCallback(() => {
    if (typeof window !== 'undefined' && 'speechSynthesis' in window && isPlaying && isPaused) {
      window.speechSynthesis.resume();
      setIsPaused(false);
    }
  }, [isPlaying, isPaused]);

  const speak = useCallback((textToRead) => {
    if (!isSupported || !textToRead) return;

    // Cancel existing utterance before starting new one
    window.speechSynthesis.cancel();

    const cleaned = cleanSpeechText(textToRead);
    if (!cleaned) return;

    const utterance = new SpeechSynthesisUtterance(cleaned);
    const targetLocale = getTargetLocale(language);
    utterance.lang = targetLocale;

    // Pick best matching voice
    if (availableVoices.length > 0) {
      const match = availableVoices.find(v => v.lang === targetLocale || v.lang.startsWith(language))
        || availableVoices.find(v => v.lang.startsWith('en'))
        || availableVoices[0];
      if (match) {
        utterance.voice = match;
      }
    }

    utterance.rate = 0.95; // Slightly measured pace for clarity in astrological texts
    utterance.pitch = 1.0;

    utterance.onstart = () => {
      setIsPlaying(true);
      setIsPaused(false);
    };

    utterance.onend = () => {
      setIsPlaying(false);
      setIsPaused(false);
    };

    utterance.onerror = (e) => {
      // Ignore interruption/cancellation errors
      if (e.error !== 'interrupted' && e.error !== 'canceled') {
        console.warn('Speech synthesis error:', e);
      }
      setIsPlaying(false);
      setIsPaused(false);
    };

    utteranceRef.current = utterance;
    window.speechSynthesis.speak(utterance);
  }, [isSupported, language, availableVoices]);

  // Clean up when unmounting
  useEffect(() => {
    return () => {
      if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
        window.speechSynthesis.cancel();
      }
    };
  }, []);

  return {
    isSupported,
    isPlaying,
    isPaused,
    speak,
    pause,
    resume,
    stop
  };
}
