package com.template.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final String issuer;
    private final String audience;
    private final long clockSkewSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.audience:}") String audience,
            @Value("${security.jwt.clock-skew-seconds:30}") long clockSkewSeconds
    ) {
        this.key = buildKey(secret);
        this.issuer = issuer;
        this.audience = (audience != null && !audience.isBlank()) ? audience : null;
        this.clockSkewSeconds = Math.max(0, clockSkewSeconds);
    }

    private Key buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("security.jwt.secret missed");
        }
        // 1) try Base64 
        try {
            byte[] decoded = Decoders.BASE64.decode(secret);
            if (decoded.length >= 32) {
                return Keys.hmacShaKeyFor(decoded);
            }
        } catch (Exception ignored) {
            //no base64
        }
        // else key text (UTF-8)
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret should do minimum 32 bytes (256 bits)");
        }
        return Keys.hmacShaKeyFor(raw);
    }

    public String generateToken(String subject, long ttlSeconds, Map<String, Object> claims) {
        Instant now = Instant.now();

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims != null ? claims : Jwts.claims())
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key, SignatureAlgorithm.HS256);

        if (audience != null) {
            builder.setAudience(audience);
        }

        return builder.compact();
    }

    public Jws<Claims> parse(String token) {
        JwtParserBuilder parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .setAllowedClockSkewSeconds(clockSkewSeconds);

        if (audience != null) {
            parser.requireAudience(audience);
        }

        return parser.build().parseClaimsJws(token);
    }

    public String getSubject(String token) {
        return parse(token).getBody().getSubject();
    }
}
