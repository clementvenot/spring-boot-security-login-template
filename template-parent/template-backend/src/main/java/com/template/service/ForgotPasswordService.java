// com.template.service.ForgotPasswordService.java
package com.template.service;

import com.template.entity.PasswordResetToken;
import com.template.entity.User;
import com.template.repository.PasswordResetTokenRepository;
import com.template.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ForgotPasswordService {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final RateLimiterService rateLimiter;

    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    public ForgotPasswordService(UserRepository userRepository,
                                 PasswordResetTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 MailService mailService,
                                 RateLimiterService rateLimiter) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.rateLimiter = rateLimiter;
    }

    public void requestReset(String email, String requesterIp, String frontResetUrlBase, Locale locale) {
        boolean ipAllowed = rateLimiter.allowIp(requesterIp);
        boolean emailAllowed = rateLimiter.allowEmail(email);
        if (!ipAllowed || !emailAllowed) {
            return; // anti-enumération
        }

        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            return; // anti-disclosure
        }

        User user = optUser.get();
        String token = generateSecureToken(48);
        Instant now = Instant.now();
        Instant expires = now.plus(TOKEN_TTL);

        PasswordResetToken prt = new PasswordResetToken(user, token, now, expires);
        tokenRepository.save(prt);

        String resetLink = frontResetUrlBase + "?token=" + token;
        // Envoi i18n
        mailService.sendPasswordResetEmail(user.getEmail(), resetLink, locale);
    }

    public boolean resetPassword(String token, String rawNewPassword) {
        Optional<PasswordResetToken> opt = tokenRepository.findByToken(token);
        if (opt.isEmpty()) {
            log.warn("Reset failed: token not found");
            return false;
        }

        PasswordResetToken prt = opt.get();
        if (prt.isUsed()) {
            log.warn("Reset failed: token already used");
            return false;
        }
        if (prt.isExpired()) {
            log.warn("Reset failed: token expired");
            return false;
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(rawNewPassword));
        userRepository.save(user);

        prt.setUsedAt(Instant.now());
        tokenRepository.save(prt);
        return true;
    }

    private String generateSecureToken(int numBytes) {
        byte[] bytes = new byte[numBytes];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
