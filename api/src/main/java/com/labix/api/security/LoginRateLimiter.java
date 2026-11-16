package com.labix.api.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter for login endpoints.
 * Allows MAX_ATTEMPTS per IP within WINDOW_SECONDS.
 * Good enough for a small pilot; replace with Redis Bucket4j for multi-instance.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 15 * 60; // 15 minutes

    // IP → timestamps of recent attempts
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

    public boolean isAllowed(String ip) {
        final Instant now = Instant.now();
        final Instant windowStart = now.minusSeconds(WINDOW_SECONDS);

        attempts.compute(ip, (key, q) -> {
            if (q == null) q = new ArrayDeque<>();
            // drop stale entries
            while (!q.isEmpty() && q.peekFirst().isBefore(windowStart)) q.pollFirst();
            q.addLast(now);
            return q;
        });

        Deque<Instant> window = attempts.get(ip);
        return window == null || window.size() <= MAX_ATTEMPTS;
    }

    /** Call on successful login to reset the counter for this IP. */
    public void reset(String ip) {
        attempts.remove(ip);
    }
}
