package com.template.config;

import java.util.List;
import java.util.Locale;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class I18nConfigBackend extends AcceptHeaderLocaleResolver {

    private static final List<Locale> SUPPORTED = List.of(Locale.FRENCH, Locale.ENGLISH);

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String header = request.getHeader("Accept-Language");

        if (header == null || header.isBlank()) {
            return Locale.FRENCH;
        }
        Locale matched = Locale.lookup(Locale.LanguageRange.parse(header), SUPPORTED);
        if (matched != null) {
            return matched;
        }
        String lower = header.toLowerCase();
        if (lower.startsWith("fr")) {
            return Locale.FRENCH;
        }
        return Locale.ENGLISH;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:messages/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        return ms;
    }
}
