package com.labix.api.admin;

import com.labix.api.domain.Plan;
import com.labix.api.domain.SuperAdmin;
import com.labix.api.repository.PlanRepository;
import com.labix.api.repository.SuperAdminRepository;
import com.labix.api.repository.TenantRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TenantAdminControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PlanRepository planRepository;
    @Autowired TenantRepository tenantRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String adminId;

    @BeforeEach
    void setUp() {
        // Seed plans (Flyway is off in test; seed manually)
        if (!planRepository.existsById("WEB")) {
            Plan web = new Plan(); web.setCode("WEB"); web.setName("Web Only"); web.setPricePaise(120000);
            Plan mob = new Plan(); mob.setCode("MOB_COLLECT"); mob.setName("Web+Mobile Collect"); mob.setPricePaise(150000);
            Plan full = new Plan(); full.setCode("MOB_FULL"); full.setName("Web+Mobile Full"); full.setPricePaise(200000);
            planRepository.saveAll(List.of(web, mob, full));
        }

        SuperAdmin admin = new SuperAdmin();
        admin.setName("Admin");
        admin.setEmail("admin@labix.com");
        admin.setPasswordHash(passwordEncoder.encode("adminpass"));
        superAdminRepository.save(admin);
        adminId = admin.getId();

        adminToken = jwtUtil.generateAccessToken(adminId, "SUPER_ADMIN", null, List.of("SUPER_ADMIN"));
    }

    // ── auth guard ────────────────────────────────────────────────────────────

    @Test
    void endpointsRequireSuperAdminRole() throws Exception {
        String userToken = jwtUtil.generateAccessToken("user-1", "USER", "tenant-1", List.of("LAB_ADMIN"));
        mvc.perform(get("/admin/tenants").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void endpointsRequireAuth() throws Exception {
        mvc.perform(get("/admin/tenants"))
                .andExpect(status().isUnauthorized());
    }

    // ── create tenant ─────────────────────────────────────────────────────────

    @Test
    void createTenantReturns201() throws Exception {
        mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"City Lab","slug":"city-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.slug").value("city-lab"))
                .andExpect(jsonPath("$.data.currentPlanCode").value("WEB"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void createTenantDuplicateSlugReturns409() throws Exception {
        mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab A","slug":"dup-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab B","slug":"dup-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SLUG_TAKEN"));
    }

    @Test
    void createTenantInvalidPlanReturns400() throws Exception {
        mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab","slug":"lab-x","planCode":"INVALID"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── list / get ────────────────────────────────────────────────────────────

    @Test
    void listTenantsReturnsPage() throws Exception {
        mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab 1","slug":"lab-1","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/admin/tenants").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void getTenantNotFoundReturns404() throws Exception {
        mvc.perform(get("/admin/tenants/nonexistent-id")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("TENANT_NOT_FOUND"));
    }

    // ── suspend / activate ────────────────────────────────────────────────────

    @Test
    void suspendAndActivateTenant() throws Exception {
        MvcResult created = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Suspend Lab","slug":"suspend-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = extractId(created.getResponse().getContentAsString());

        mvc.perform(post("/admin/tenants/" + id + "/suspend")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

        mvc.perform(post("/admin/tenants/" + id + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    // ── plan assignment ───────────────────────────────────────────────────────

    @Test
    void assignPlanChangesSubscription() throws Exception {
        MvcResult created = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Plan Lab","slug":"plan-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = extractId(created.getResponse().getContentAsString());

        mvc.perform(post("/admin/tenants/" + id + "/subscriptions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"planCode":"MOB_FULL"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.planCode").value("MOB_FULL"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mvc.perform(get("/admin/tenants/" + id + "/subscriptions")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2))); // original WEB + new MOB_FULL
    }

    // ── create lab admin ──────────────────────────────────────────────────────

    @Test
    void createLabAdminReturns201() throws Exception {
        MvcResult created = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Admin Lab","slug":"admin-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = extractId(created.getResponse().getContentAsString());

        mvc.perform(post("/admin/tenants/" + id + "/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dr. Admin","email":"admin@lab.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("admin@lab.com"))
                .andExpect(jsonPath("$.data.roles[0]").value("LAB_ADMIN"));
    }

    // ── support access ────────────────────────────────────────────────────────

    @Test
    void supportAccessLogLifecycle() throws Exception {
        MvcResult created = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Support Lab","slug":"support-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String tenantId = extractId(created.getResponse().getContentAsString());

        MvcResult logResult = mvc.perform(post("/admin/tenants/" + tenantId + "/support-access")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Investigating billing issue"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.endedAt").doesNotExist())
                .andReturn();

        String logId = extractId(logResult.getResponse().getContentAsString());

        mvc.perform(post("/admin/support-access/" + logId + "/end")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.endedAt").isNotEmpty());
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
