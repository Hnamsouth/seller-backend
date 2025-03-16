package com.vtp.vipo.seller.common.constants;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
public enum LanguageCode {
    VIETNAMESE(new Locale(Constants.LANGUAGE_VI)),
    ENGLISH(new Locale(Constants.LANGUAGE_EN)),
    CHINESE(new Locale(Constants.CHINESE_LANGUAGE));

    // add more language codes as needed
    public static final List<String> LOCALES = Arrays.stream(LanguageCode.values())
            .map(languageCode -> languageCode.getLocale().getLanguage())
            .collect(Collectors.toList());
    private final Locale locale;

    LanguageCode(Locale locale) {
        this.locale = locale;
    }

    public static boolean isSupported(String code) {
        return LOCALES.stream().anyMatch(locale -> locale.equalsIgnoreCase(code));
    }

    public static Locale getDefaultLocale() {
        return VIETNAMESE.getLocale();
    }

    public static Locale getLocaleFromString(String code) {
        if (StringUtils.isBlank(code)) return null;
        if (isSupported(code)) {
            return Arrays.stream(LanguageCode.values())
                    .filter(languageCode -> languageCode.getLocale().getLanguage().equalsIgnoreCase(code))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported locale code: " + code))
                    .getLocale();
        }
        return null;
    }

}