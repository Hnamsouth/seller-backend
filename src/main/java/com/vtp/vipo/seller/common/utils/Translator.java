package com.vtp.vipo.seller.common.utils;

import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.lang.Nullable;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

@Configuration
@NoArgsConstructor
public class Translator {
    private static final Logger log = LoggerFactory.getLogger(Translator.class);

    private static final MessageSource messageSource = messageSource();

    public static String toLocaleWithDefault(String msgCode, String defaultMessage, @Nullable Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, args, defaultMessage, locale);
    }

    public static String toLocaleWithDefault(String msgCode, String defaultMessage) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, null, defaultMessage, locale);
    }

    public static String toLocale(String msgCode, @Nullable Object[] args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(msgCode, args, locale);
    }

    public static String toLocale(String msgCode, String... params) {
        try {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(msgCode, null, locale);
            try {
                message = String.format(message, params);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                return message;
            }
            return message;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return "";
        }
    }

    private static MessageSource messageSource() {
        ReloadableResourceBundleMessageSource resource = new ReloadableResourceBundleMessageSource();
        resource.setBasenames("classpath:messages");
        resource.setDefaultEncoding("UTF-8");
        resource.setUseCodeAsDefaultMessage(true);
        resource.setFallbackToSystemLocale(false);
        return resource;
    }

    /**
     * Validate message file
     */
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource);
        return bean;
    }
}
