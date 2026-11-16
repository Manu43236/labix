package com.labix.api.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil("test-secret-key-that-is-long-enough-32ch", 900_000L, 604_800_000L);
    }

    @Test
    void accessTokenContainsCorrectClaims() {
        String token = jwtUtil.generateAccessToken("user-1", "USER", "tenant-1", List.of("LAB_ADMIN"));
        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(claims.get("type")).isEqualTo("USER");
        assertThat(claims.get("tenantId")).isEqualTo("tenant-1");
    }

    @Test
    void refreshTokenHasRefreshMarker() {
        String token = jwtUtil.generateRefreshToken("user-1", "USER", "tenant-1");
        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims.get("tokenType")).isEqualTo("REFRESH");
        assertThat(claims.getSubject()).isEqualTo("user-1");
    }

    @Test
    void superAdminTokenHasNoTenantId() {
        String token = jwtUtil.generateAccessToken("admin-1", "SUPER_ADMIN", null, List.of("SUPER_ADMIN"));
        Claims claims = jwtUtil.parseToken(token);

        assertThat(claims.get("tenantId")).isNull();
        assertThat(claims.get("type")).isEqualTo("SUPER_ADMIN");
    }

    @Test
    void invalidTokenThrows() {
        assertThatThrownBy(() -> jwtUtil.parseToken("not.a.valid.jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void expiredTokenThrows() {
        JwtUtil shortLived = new JwtUtil("test-secret-key-that-is-long-enough-32ch", 1L, 1L);
        String token = shortLived.generateAccessToken("user-1", "USER", "tenant-1", List.of());

        // Sleep 2ms to ensure expiry
        try { Thread.sleep(2); } catch (InterruptedException ignored) {}

        assertThatThrownBy(() -> shortLived.parseToken(token))
                .isInstanceOf(Exception.class);
    }

    @Test
    void planCodeIncludesFeatureCorrectly() {
        assertThat(PlanCode.WEB.includes(PlanFeature.MOBILE_ACCESS)).isFalse();
        assertThat(PlanCode.MOB_COLLECT.includes(PlanFeature.MOBILE_ACCESS)).isTrue();
        assertThat(PlanCode.MOB_COLLECT.includes(PlanFeature.MOBILE_FULL_ACCESS)).isFalse();
        assertThat(PlanCode.MOB_FULL.includes(PlanFeature.MOBILE_FULL_ACCESS)).isTrue();
    }
}
