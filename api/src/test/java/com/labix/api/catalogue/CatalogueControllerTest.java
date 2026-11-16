package com.labix.api.catalogue;

import com.labix.api.domain.Plan;
import com.labix.api.domain.SuperAdmin;
import com.labix.api.repository.PlanRepository;
import com.labix.api.repository.SuperAdminRepository;
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
class CatalogueControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PlanRepository planRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String tenantId;
    private String labAdminToken;
    private String technicianToken;

    @BeforeEach
    void setUp() throws Exception {
        if (!planRepository.existsById("WEB")) {
            Plan web = new Plan(); web.setCode("WEB"); web.setName("Web Only"); web.setPricePaise(120000);
            Plan mob = new Plan(); mob.setCode("MOB_COLLECT"); mob.setName("Web+Mobile Collect"); mob.setPricePaise(150000);
            Plan full = new Plan(); full.setCode("MOB_FULL"); full.setName("Web+Mobile Full"); full.setPricePaise(200000);
            planRepository.saveAll(List.of(web, mob, full));
        }

        SuperAdmin admin = new SuperAdmin();
        admin.setName("Admin"); admin.setEmail("admin@labix.com");
        admin.setPasswordHash(passwordEncoder.encode("adminpass"));
        superAdminRepository.save(admin);
        adminToken = jwtUtil.generateAccessToken(admin.getId(), "SUPER_ADMIN", null, List.of("SUPER_ADMIN"));

        // Create tenant (seeds catalogue)
        MvcResult tenantResult = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Cat Lab","slug":"cat-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        tenantId = extractId(tenantResult.getResponse().getContentAsString());

        // Create lab admin
        MvcResult labAdminResult = mvc.perform(post("/admin/tenants/" + tenantId + "/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab Admin","email":"admin@cat.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String labAdminId = extractId(labAdminResult.getResponse().getContentAsString());
        labAdminToken = jwtUtil.generateAccessToken(labAdminId, "USER", tenantId, List.of("LAB_ADMIN"));

        // Technician token (read-only)
        technicianToken = jwtUtil.generateAccessToken("tech-1", "USER", tenantId, List.of("TECHNICIAN"));
    }

    // ── seed ──────────────────────────────────────────────────────────────────

    @Test
    void seededTestsAreVisibleAfterTenantCreation() throws Exception {
        mvc.perform(get("/catalogue/tests").header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[*].code", hasItem("CBC")));
    }

    @Test
    void seededPackagesAreVisible() throws Exception {
        mvc.perform(get("/catalogue/packages").header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Test
    void createTestReturns201() throws Exception {
        mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"CUSTOM1","name":"Custom Test","category":"Biochemistry","pricePaise":5000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.code").value("CUSTOM1"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void duplicateCodeReturns409() throws Exception {
        mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"DUPCODE","name":"Test A","pricePaise":0}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"DUPCODE","name":"Test B","pricePaise":0}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CODE_TAKEN"));
    }

    @Test
    void getTestReturnsParameters() throws Exception {
        // Create a test with a known parameter
        MvcResult testResult = mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"PARAM_TEST","name":"Param Test","pricePaise":0}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String testId = extractId(testResult.getResponse().getContentAsString());

        mvc.perform(post("/catalogue/tests/" + testId + "/parameters")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Glucose","unit":"mg/dL","valueType":"NUMBER","decimals":0,"sortOrder":1}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/catalogue/tests/" + testId)
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parameters", hasSize(1)))
                .andExpect(jsonPath("$.data.parameters[0].name").value("Glucose"));
    }

    @Test
    void addAndReplaceRanges() throws Exception {
        // Create test + parameter
        MvcResult testResult = mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"RANGE_TEST","name":"Range Test","pricePaise":0}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String testId = extractId(testResult.getResponse().getContentAsString());

        MvcResult paramResult = mvc.perform(post("/catalogue/tests/" + testId + "/parameters")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Glucose","unit":"mg/dL","valueType":"NUMBER","decimals":0,"sortOrder":1}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String paramId = extractId(paramResult.getResponse().getContentAsString());

        mvc.perform(put("/catalogue/tests/" + testId + "/parameters/" + paramId + "/ranges")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [{"gender":"ANY","low":70,"high":99},{"gender":"ANY","low":100,"high":125,"rangeText":"Pre-diabetic"}]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // ── packages ──────────────────────────────────────────────────────────────

    @Test
    void createPackageReturns201() throws Exception {
        mvc.perform(post("/catalogue/packages")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Custom Package","pricePaise":50000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Custom Package"));
    }

    // ── role enforcement ──────────────────────────────────────────────────────

    @Test
    void technicianCannotCreateTest() throws Exception {
        mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"TECH1","name":"Tech Test","pricePaise":0}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianCanReadTests() throws Exception {
        mvc.perform(get("/catalogue/tests")
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk());
    }

    // ── tenant isolation ──────────────────────────────────────────────────────

    @Test
    void tenantIsolationOnTestCreate() throws Exception {
        // Create second tenant
        MvcResult t2 = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Other Lab","slug":"other-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String tenant2Id = extractId(t2.getResponse().getContentAsString());
        String token2 = jwtUtil.generateAccessToken("user-t2", "USER", tenant2Id, List.of("LAB_ADMIN"));

        // Both tenants can create a test with the same code (different tenants)
        mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ISOLTEST","name":"Test","pricePaise":0}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + token2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ISOLTEST","name":"Test","pricePaise":0}
                                """))
                .andExpect(status().isCreated()); // no conflict — different tenant

        // Each tenant only sees their own ISOLTEST
        mvc.perform(get("/catalogue/tests?q=isoltest")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));

        mvc.perform(get("/catalogue/tests?q=isoltest")
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
