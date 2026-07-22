package org.vedic.astro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.vedic.astro.util.IndicPreShaper;

@Service
@RequiredArgsConstructor
public class TranslationService {
    private final MessageSource messageSource;

    public String getLocalizedRashi(int sign) {
        String value = messageSource.getMessage("rashi." + sign, null, String.valueOf(sign), LocaleContextHolder.getLocale());
        return IndicPreShaper.shape(value);
    }

    public String getLocalizedNakshatra(int nak) {
        String value = messageSource.getMessage("nakshatra." + nak, null, "Star-" + nak, LocaleContextHolder.getLocale());
        return IndicPreShaper.shape(value);
    }

    public String getLabel(String key) {
        if (key == null) return "";
        String value = messageSource.getMessage(key, null, null, LocaleContextHolder.getLocale());
        if (value == null) {
            int dotIdx = key.lastIndexOf('.');
            if (dotIdx >= 0) {
                String prefix = key.substring(0, dotIdx + 1);
                String suffix = key.substring(dotIdx + 1);
                value = messageSource.getMessage(prefix + suffix.toUpperCase(), null, null, LocaleContextHolder.getLocale());
                if (value == null) {
                    value = messageSource.getMessage(prefix + suffix.toLowerCase(), null, key, LocaleContextHolder.getLocale());
                }
            } else {
                value = key;
            }
        }
        return IndicPreShaper.shape(value);
    }
}
