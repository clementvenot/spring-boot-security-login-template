package com.template.front.config;

import com.template.front.web.LoginRequiredInterceptor;
import java.time.Duration;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String SESSION_JWT = "ACCESS_TOKEN";
    public static final String SESSION_USER = "CURRENT_USER";

    // Make this configurable if possible (false for HTTP in dev, true for HTTPS in production)
    @Value("${app.cookies.secure:false}")
    private boolean cookiesSecure;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1) i18n first
        registry.addInterceptor(localeChangeInterceptor());

        // 2) Secure the entire website by default, and exclude publicly accessible pages
        registry.addInterceptor(new LoginRequiredInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // Public pages & actions
                        "/login",
                        "/register",
                        "/logout", 
                        "/error",
                        "/forgot-password",
                        "/reset-password/**",

                        // Static resources
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico",
                        "/assets/**",    
                        "/static/**"     
                );
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver clr = new CookieLocaleResolver();
        clr.setCookieName("LOCALE");
        clr.setDefaultLocale(Locale.FRENCH);
        clr.setCookieMaxAge(Duration.ofDays(365));
        clr.setCookieSecure(cookiesSecure);     // true in production HTTPS, false in dev HTTP
        clr.setCookieHttpOnly(false);           // false for a UI preference cookie
        clr.setCookieSameSite("Lax");           // Lax = ideal for a non-sensitive UX cookie
        return clr;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // ex: ?lang=en or ?lang=fr
        return lci;
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource =
                new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);

        return messageSource;
    }
}