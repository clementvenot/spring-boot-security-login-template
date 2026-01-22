package com.template.config;

import com.template.repository.UserRepository;
import com.template.security.JwtCookieAuthFilter;
import com.template.security.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	
    @Bean    
    public PasswordEncoder passwordEncoder() {        
    	return new BCryptPasswordEncoder();    
    	}
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtService jwt,
                                           UserRepository userRepo) throws Exception {
        http
            .csrf(cs -> cs.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
			    .requestMatchers("/auth/login", "/auth/register", "/auth/logout",
			                     "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
			    .permitAll()
			    .anyRequest().authenticated()
			)
            .addFilterBefore(new JwtCookieAuthFilter(jwt, userRepo), UsernamePasswordAuthenticationFilter.class);
        http.cors(c -> c.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:8081")); 
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        cfg.setAllowCredentials(true); 
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
