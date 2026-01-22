package com.template.security;

import com.template.security.jwt.JwtService;
import com.template.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepository userRepo;

    public JwtCookieAuthFilter(JwtService jwt, UserRepository userRepo) {
        this.jwt = jwt;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, jakarta.servlet.ServletException {

        String token = resolveTokenFromCookies(request, "access_token");
        if (StringUtils.hasText(token)) {
            try {
                String email = jwt.getSubject(token);

                var user = userRepo.findByEmail(email).orElse(null);
                if (user != null) {
                    var auth = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            user.getRoles().stream().map(r -> "ROLE_" + r)
                                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                .toList()
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // token invalide/expiré: on laisse passer non authentifié
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
