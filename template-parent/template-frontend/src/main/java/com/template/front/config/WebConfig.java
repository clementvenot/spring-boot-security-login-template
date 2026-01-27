package com.template.front.config;

import com.template.front.web.LoginRequiredInterceptor;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    public static final String SESSION_JWT = "ACCESS_TOKEN";
    public static final String SESSION_USER = "CURRENT_USER";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginRequiredInterceptor())
                .addPathPatterns("/secure/**", "/secure");
        registry.addInterceptor(localeChangeInterceptor());
    }
    
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver clr = new CookieLocaleResolver();
        clr.setCookieName("LOCALE");
        clr.setDefaultLocale(Locale.FRENCH);
        clr.setCookieMaxAge(Duration.ofDays(365));
        clr.setCookieSecure(true);           
        clr.setCookieHttpOnly(false);    
        clr.setCookieSameSite("Lax");
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
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }
}
