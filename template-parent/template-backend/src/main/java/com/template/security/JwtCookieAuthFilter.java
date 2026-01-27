package com.template.security;

import com.template.repository.UserRepository;
import com.template.security.jwt.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtCookieAuthFilter.class);

    private final JwtService jwt;
    private final UserRepository userRepo;

    public JwtCookieAuthFilter(JwtService jwt, UserRepository userRepo) {
        this.jwt = jwt;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, jakarta.servlet.ServletException {

        String token = resolveTokenFromCookies(request, "access_token");

        if (StringUtils.hasText(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                var jws = jwt.parse(token);

                String email = jws.getBody().getSubject();
                if (email != null) {
                    var user = userRepo.findByEmail(email).orElse(null);
                    if (user != null) {
                        var authorities = user.getRoles().stream()
                                .map(r -> "ROLE_" + r)
                                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                .toList();
                        var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } else {
                        logger.debug("No user found for email in token: {}", email);
                    }
                }
            } catch (ExpiredJwtException e) {
                // Token expire 
                logger.debug("Expired JWT");
            } catch (JwtException e) {
                // token invalid
                logger.debug("JWT invalid (signature/malformed/issuer/audience)", e);
            } catch (Exception e) {
                // Prudence 
                logger.warn("An unexpected error occurred during JWT authentication", e);
            }
        }
        chain.doFilter(request, response);
    }

    private String resolveTokenFromCookies(HttpServletRequest request, String name) {
        var cookies = request.getCookies();
        if (cookies == null) return null;
        for (var c : cookies) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
