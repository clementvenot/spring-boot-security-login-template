package com.template.service;

import com.template.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class PasswordResetCleanupService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetCleanupService.class);

    private final PasswordResetTokenRepository tokenRepository;
    private final RateLimiterService rateLimiterService;
    private final int usedRetentionDays;

    public PasswordResetCleanupService(PasswordResetTokenRepository tokenRepository,
                                       RateLimiterService rateLimiterService,
                                       @Value("${app.password-reset.cleanup.used-retention-days}") int usedRetentionDays) {
        this.tokenRepository = tokenRepository;
        this.rateLimiterService = rateLimiterService;
        this.usedRetentionDays = usedRetentionDays;
    }

    @Scheduled(cron = "${app.password-reset.cleanup.cron}")
    public void cleanupTokensAndLimiter() {
        Instant now = Instant.now();
        int expired = tokenRepository.deleteAllExpired(now);

        Instant threshold = now.minus(usedRetentionDays, ChronoUnit.DAYS);
        int used = tokenRepository.deleteAllUsedBefore(threshold);

        rateLimiterService.cleanup();

        log.info("Password reset cleanup done: expired={}, used(old)={}", expired, used);
    }
}