package com.labix.api.labsetup;

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
import org.springframework.mock.web.MockMultipartFile;
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
class LabSetupControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PlanRepository planRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String tenantId;
    private String labAdminToken;

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

        MvcResult tenantResult = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Setup Lab","slug":"setup-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        tenantId = extractId(tenantResult.getResponse().getContentAsString());

        MvcResult userResult = mvc.perform(post("/admin/tenants/" + tenantId + "/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab Admin","email":"admin@setup.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String adminUserId = extractId(userResult.getResponse().getContentAsString());
        labAdminToken = jwtUtil.generateAccessToken(adminUserId, "USER", tenantId, List.of("LAB_ADMIN"));
    }

    // ── doctors ───────────────────────────────────────────────────────────────

    @Test
    void createDoctorReturns201() throws Exception {
        mvc.perform(post("/doctors")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dr. Sharma","phone":"9876543210","clinic":"City Clinic","commissionType":"PERCENT","commissionValue":10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.name").value("Dr. Sharma"))
                .andExpect(jsonPath("$.data.commissionType").value("PERCENT"));
    }

    @Test
    void searchDoctorByName() throws Exception {
        mvc.perform(post("/doctors")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Dr. Patel","commissionType":"NONE"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/doctors?q=patel").header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Dr. Patel"));
    }

    // ── branches ──────────────────────────────────────────────────────────────

    @Test
    void createBranchReturns201() throws Exception {
        mvc.perform(post("/branches")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Main Branch","address":"123 Main St","phone":"0891-123456","main":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.main").value(true));
    }

    @Test
    void secondMainBranchReturns409() throws Exception {
        mvc.perform(post("/branches")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Main Branch","main":true}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/branches")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Another Main Branch","main":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("MAIN_BRANCH_EXISTS"));
    }

    @Test
    void nonLabAdminCannotCreateBranch() throws Exception {
        String receptionistToken = jwtUtil.generateAccessToken("recept-1", "USER", tenantId, List.of("RECEPTIONIST"));
        mvc.perform(post("/branches")
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test Branch","main":false}
                                """))
                .andExpect(status().isForbidden());
    }

    // ── lab users ─────────────────────────────────────────────────────────────

    @Test
    void createLabUserReturns201() throws Exception {
        mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John Tech","email":"tech@setup.com","password":"secret123","roles":["TECHNICIAN"]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.roles[0]").value("TECHNICIAN"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"User A","email":"dup@setup.com","password":"secret123","roles":["RECEPTIONIST"]}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"User B","email":"dup@setup.com","password":"secret123","roles":["TECHNICIAN"]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("EMAIL_TAKEN"));
    }

    @Test
    void createdUserCanLogin() throws Exception {
        mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pathologist","email":"path@setup.com","password":"secret123","roles":["PATHOLOGIST"]}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"path@setup.com","password":"secret123","tenantSlug":"setup-lab"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.roles[0]").value("PATHOLOGIST"));
    }

    @Test
    void deactivateAndReactivateUser() throws Exception {
        MvcResult created = mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Temp User","email":"temp@setup.com","password":"secret123","roles":["RECEPTIONIST"]}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String userId = extractId(created.getResponse().getContentAsString());

        mvc.perform(put("/users/" + userId + "/status")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"INACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void signatureUploadReturns201() throws Exception {
        MvcResult created = mvc.perform(post("/users")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pathologist","email":"sig@setup.com","password":"secret123","roles":["PATHOLOGIST"]}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String userId = extractId(created.getResponse().getContentAsString());

        // 1x1 PNG bytes
        byte[] png = new byte[]{
            (byte)0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,
            0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52,
            0x00,0x00,0x00,0x01,0x00,0x00,0x00,0x01,
            0x08,0x02,0x00,0x00,0x00,(byte)0x90,0x77,0x53,
            (byte)0xde,0x00,0x00,0x00,0x0c,0x49,0x44,0x41,
            0x54,0x08,(byte)0xd7,0x63,(byte)0xf8,(byte)0xcf,(byte)0xc0,0x00,0x00,
            0x00,0x02,0x00,0x01,(byte)0xe2,0x21,(byte)0xbc,0x33,
            0x00,0x00,0x00,0x00,0x49,0x45,0x4e,0x44,
            (byte)0xae,0x42,0x60,(byte)0x82
        };
        MockMultipartFile signatureFile = new MockMultipartFile("file", "sig.png", "image/png", png);

        mvc.perform(multipart("/users/" + userId + "/signature")
                        .file(signatureFile)
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.signatureFileId").isNotEmpty());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
