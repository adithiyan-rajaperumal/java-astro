package org.vedic.astro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TranslationService {
    private final MessageSource messageSource;

    public String getLocalizedRashi(int signNumber) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage("rashi." + signNumber, null, String.valueOf(signNumber), currentLocale);
    }

    public String getLocalizedNakshatra(int nakshatraNumber) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage("nakshatra." + nakshatraNumber, null, "Star-" + nakshatraNumber, currentLocale);
    }

    public String getLabel(String key) {
        Locale currentLocale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, currentLocale);
    }
}
