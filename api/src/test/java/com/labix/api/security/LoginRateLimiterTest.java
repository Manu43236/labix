package com.labix.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRateLimiterTest {

    private LoginRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new LoginRateLimiter();
    }

    @Test
    void allowsFirstTenAttempts() {
        for (int i = 0; i < 10; i++) {
            assertThat(limiter.isAllowed("1.2.3.4")).isTrue();
        }
    }

    @Test
    void blocksEleventhAttempt() {
        for (int i = 0; i < 10; i++) limiter.isAllowed("1.2.3.4");
        assertThat(limiter.isAllowed("1.2.3.4")).isFalse();
    }

    @Test
    void differentIpsAreIndependent() {
        for (int i = 0; i < 10; i++) limiter.isAllowed("1.1.1.1");
        // 1.1.1.1 is blocked but 2.2.2.2 is fresh
        assertThat(limiter.isAllowed("1.1.1.1")).isFalse();
        assertThat(limiter.isAllowed("2.2.2.2")).isTrue();
    }

    @Test
    void resetClearsCounter() {
        for (int i = 0; i < 10; i++) limiter.isAllowed("1.2.3.4");
        limiter.reset("1.2.3.4");
        assertThat(limiter.isAllowed("1.2.3.4")).isTrue();
    }
}
