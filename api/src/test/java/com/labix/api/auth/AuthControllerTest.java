package com.labix.api.auth;

import com.labix.api.domain.SuperAdmin;
import com.labix.api.domain.Tenant;
import com.labix.api.domain.User;
import com.labix.api.domain.UserRole;
import com.labix.api.repository.*;
import com.labix.api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired TenantRepository tenantRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserRoleRepository userRoleRepository;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String tenantId;
    private String userId;

    @BeforeEach
    void setUp() {
        Tenant tenant = new Tenant();
        tenant.setName("Test Lab");
        tenant.setSlug("test-lab");
        tenant.setStatus("ACTIVE");
        tenantRepository.save(tenant);
        tenantId = tenant.getId();

        User user = new User();
        user.setTenantId(tenantId);
        user.setName("Test User");
        user.setEmail("user@testlab.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        user.setStatus("ACTIVE");
        userRepository.save(user);
        userId = user.getId();

        UserRole role = new UserRole();
        role.setUserId(userId);
        role.setRole("LAB_ADMIN");
        userRoleRepository.save(role);

        SuperAdmin admin = new SuperAdmin();
        admin.setName("Super Admin");
        admin.setEmail("admin@labix.com");
        admin.setPasswordHash(passwordEncoder.encode("adminpass"));
        superAdminRepository.save(admin);
    }

    @Test
    void userLoginReturnsTokens() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@testlab.com","password":"password123","tenantSlug":"test-lab"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void userLoginWrongPasswordReturns401() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@testlab.com","password":"wrong","tenantSlug":"test-lab"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void userLoginUnknownTenantReturns404() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@testlab.com","password":"password123","tenantSlug":"no-such-lab"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TENANT_NOT_FOUND"));
    }

    @Test
    void superAdminLoginReturnsTokens() throws Exception {
        mvc.perform(post("/auth/super/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@labix.com","password":"adminpass"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void superAdminLoginWrongPasswordReturns401() throws Exception {
        mvc.perform(post("/auth/super/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@labix.com","password":"wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshReturnsNewAccessToken() throws Exception {
        // Get a refresh token first
        var result = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"user@testlab.com","password":"password123","tenantSlug":"test-lab"}
                                """))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        // Extract refreshToken from JSON response
        String refreshToken = body.split("\"refreshToken\":\"")[1].split("\"")[0];

        mvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void meEndpointReturnsTenantContext() throws Exception {
        String token = jwtUtil.generateAccessToken(userId, "USER", tenantId, List.of("LAB_ADMIN"));

        mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                .andExpect(jsonPath("$.data.superAdmin").value(false));
    }

    @Test
    void protectedEndpointWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tenantIsolation_userFromTenant1CannotSpoofTenant2() throws Exception {
        // Create tenant 2
        Tenant tenant2 = new Tenant();
        tenant2.setName("Other Lab");
        tenant2.setSlug("other-lab");
        tenant2.setStatus("ACTIVE");
        tenantRepository.save(tenant2);

        // Token for tenant 1 — ME endpoint returns tenant 1's id, not tenant 2's
        String token = jwtUtil.generateAccessToken(userId, "USER", tenantId, List.of("LAB_ADMIN"));

        mvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tenantId").value(tenantId))
                // Confirm it does NOT contain tenant2's id
                .andExpect(jsonPath("$.data.tenantId").value(org.hamcrest.Matchers.not(tenant2.getId())));
    }
}
