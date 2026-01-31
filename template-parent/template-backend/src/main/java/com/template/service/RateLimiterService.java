package com.template.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//In-memory sliding‑window rate limiter that tracks request timestamps per email and per IP.
//Removes old events and blocks when the max count is exceeded within the configured time window.
@Service
public class RateLimiterService {

    private final Duration window;
    private final int maxPerEmail;
    private final int maxPerIp;

    private final Map<String, Deque<Instant>> emailEvents = new ConcurrentHashMap<>();
    private final Map<String, Deque<Instant>> ipEvents = new ConcurrentHashMap<>();

    public RateLimiterService(
            @Value("${app.limiter.forgot.window-seconds}") long windowSeconds,
            @Value("${app.limiter.forgot.max-per-email}") int maxPerEmail,
            @Value("${app.limiter.forgot.max-per-ip}") int maxPerIp
    ) {
        this.window = Duration.ofSeconds(windowSeconds);
        this.maxPerEmail = maxPerEmail;
        this.maxPerIp = maxPerIp;
    }

    public boolean allowEmail(String email) {
        return allow(emailEvents, email, maxPerEmail);
    }

    public boolean allowIp(String ip) {
        return allow(ipEvents, ip, maxPerIp);
    }

    private boolean allow(Map<String, Deque<Instant>> store, String key, int max) {
        final Instant now = Instant.now();
        final Deque<Instant> deque = store.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (deque) {
            prune(deque, now);
            if (deque.size() >= max) {
                return false;
            }
            deque.addLast(now);
            return true;
        }
    }

    private void prune(Deque<Instant> deque, Instant now) {
        Instant threshold = now.minus(window);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(threshold)) {
            deque.pollFirst();
        }
    }

    /** Scheduled cleanup can call this periodically to reduce memory footprint. */
    public void cleanup() {
        final Instant now = Instant.now();
        emailEvents.values().forEach(d -> { synchronized (d) { prune(d, now); } });
        ipEvents.values().forEach(d -> { synchronized (d) { prune(d, now); } });
    }
}