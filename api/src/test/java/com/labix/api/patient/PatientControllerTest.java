package com.labix.api.patient;

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
class PatientControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PlanRepository planRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String tenantId;
    private String labAdminToken;
    private String receptionistToken;
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

        MvcResult tenantResult = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Patient Lab","slug":"patient-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        tenantId = extractId(tenantResult.getResponse().getContentAsString());

        MvcResult userResult = mvc.perform(post("/admin/tenants/" + tenantId + "/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab Admin","email":"admin@patient.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String adminUserId = extractId(userResult.getResponse().getContentAsString());
        labAdminToken = jwtUtil.generateAccessToken(adminUserId, "USER", tenantId, List.of("LAB_ADMIN"));

        receptionistToken = jwtUtil.generateAccessToken("recept-1", "USER", tenantId, List.of("RECEPTIONIST"));
        technicianToken = jwtUtil.generateAccessToken("tech-1", "USER", tenantId, List.of("TECHNICIAN"));
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    @Test
    void createPatientReturns201WithPatientNo() throws Exception {
        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ravi Kumar","gender":"M","phone":"9876543210"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.patientNo").value(1))
                .andExpect(jsonPath("$.data.name").value("Ravi Kumar"));
    }

    @Test
    void secondPatientGetsIncrementedPatientNo() throws Exception {
        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Patient One","gender":"F","phone":"9000000001"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.patientNo").value(1));

        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Patient Two","gender":"M","phone":"9000000002"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.patientNo").value(2));
    }

    @Test
    void searchByNameFindsPatient() throws Exception {
        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Sita Devi","gender":"F","phone":"9000000010"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/patients?q=sita")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.items[0].name").value("Sita Devi"));
    }

    @Test
    void searchByPhoneFindsPatient() throws Exception {
        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Phone Patient","gender":"M","phone":"8888888888"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/patients?q=8888888888")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void softDeleteExcludesPatientFromList() throws Exception {
        MvcResult created = mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"To Delete","gender":"M","phone":"7777777777"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String patientId = extractId(created.getResponse().getContentAsString());

        mvc.perform(delete("/patients/" + patientId)
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk());

        mvc.perform(get("/patients?q=7777777777")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // ── role enforcement ──────────────────────────────────────────────────────

    @Test
    void receptionistCanCreatePatient() throws Exception {
        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Recept Patient","gender":"F","phone":"6666666666"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void technicianCannotCreatePatient() throws Exception {
        mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Tech Patient","gender":"M","phone":"5555555555"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianCanReadPatients() throws Exception {
        mvc.perform(get("/patients").header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isOk());
    }

    @Test
    void receptionistCannotDeletePatient() throws Exception {
        MvcResult created = mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"No Delete","gender":"M","phone":"4444444444"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String patientId = extractId(created.getResponse().getContentAsString());

        mvc.perform(delete("/patients/" + patientId)
                        .header("Authorization", "Bearer " + receptionistToken))
                .andExpect(status().isForbidden());
    }

    // ── tenant isolation ──────────────────────────────────────────────────────

    @Test
    void cannotReadAnotherTenantsPatient() throws Exception {
        // Create patient in tenant 1
        MvcResult created = mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Tenant 1 Patient","gender":"M","phone":"3333333333"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String patientId = extractId(created.getResponse().getContentAsString());

        // Tenant 2 tries to access it
        MvcResult t2 = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Other Lab","slug":"other-lab2","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String tenant2Id = extractId(t2.getResponse().getContentAsString());
        String token2 = jwtUtil.generateAccessToken("user-t2", "USER", tenant2Id, List.of("TECHNICIAN"));

        mvc.perform(get("/patients/" + patientId).header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
