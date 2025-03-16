package com.vtp.vipo.seller.common.utils;

import com.vtp.vipo.seller.common.constants.Constants;
import com.vtp.vipo.seller.common.constants.LanguageCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public final class VipoLanguageUtils {

    private final MessageSource messageSource;

    /**
     * Get current language
     */
    public String getCurrentLanguage() {

        String currentLanguage = LocaleContextHolder.getLocale().getLanguage();

        /* If the language is not supported, return the default Language */
        if (!LanguageCode.isSupported(currentLanguage))
            return LanguageCode.getDefaultLocale().getLanguage();
        else
            return currentLanguage;
    }

    /**
     * Get label from message source
     */
    public String getMessageFromSource(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.error(Constants.FAILED_TO_FIND_IN_MESSAGE_SOURCE, code);
            return null;
        }
    }

    /**
     * Get label form message source with arguments
     */
    public String getMessageFromSource(String code, Object[] args) {
        try {
            return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.error(Constants.FAILED_TO_FIND_IN_MESSAGE_SOURCE, code);
            return null;
        }
    }

}
