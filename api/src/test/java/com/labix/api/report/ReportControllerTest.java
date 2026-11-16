package com.labix.api.report;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PlanRepository planRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String tenantId;
    private String labAdminToken;
    private String pathologistToken;
    private String technicianToken;
    private String visitId;
    private String paramId;

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

        MvcResult tr = mvc.perform(post("/admin/tenants")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Report Lab","slug":"report-lab","planCode":"WEB"}
                        """))
                .andExpect(status().isCreated()).andReturn();
        tenantId = extractId(tr.getResponse().getContentAsString());

        MvcResult lar = mvc.perform(post("/admin/tenants/" + tenantId + "/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Lab Admin","email":"admin@report.com","password":"secret123"}
                        """))
                .andExpect(status().isCreated()).andReturn();
        String labAdminId = extractId(lar.getResponse().getContentAsString());
        labAdminToken = jwtUtil.generateAccessToken(labAdminId, "USER", tenantId, List.of("LAB_ADMIN"));
        pathologistToken = jwtUtil.generateAccessToken(labAdminId, "USER", tenantId, List.of("PATHOLOGIST"));
        technicianToken = jwtUtil.generateAccessToken(labAdminId, "USER", tenantId, List.of("TECHNICIAN"));

        // Branch
        MvcResult br = mvc.perform(post("/branches")
                .header("Authorization", "Bearer " + labAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Main","isMain":true}
                        """))
                .andExpect(status().isCreated()).andReturn();
        String branchId = extractId(br.getResponse().getContentAsString());

        // Patient
        MvcResult pr = mvc.perform(post("/patients")
                .header("Authorization", "Bearer " + labAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Test Patient","gender":"M","phone":"9876543210","ageYears":30}
                        """))
                .andExpect(status().isCreated()).andReturn();
        String patientId = extractId(pr.getResponse().getContentAsString());

        // Test with parameter
        MvcResult testR = mvc.perform(post("/catalogue/tests")
                .header("Authorization", "Bearer " + labAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"code":"HB","name":"Haemoglobin","category":"Haematology","sampleType":"Blood","pricePaise":15000}
                        """))
                .andExpect(status().isCreated()).andReturn();
        String testId = extractId(testR.getResponse().getContentAsString());

        MvcResult paramR = mvc.perform(post("/catalogue/tests/" + testId + "/parameters")
                .header("Authorization", "Bearer " + labAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Haemoglobin","unit":"g/dL","decimals":1,"sortOrder":1}
                        """))
                .andExpect(status().isCreated()).andReturn();
        paramId = extractId(paramR.getResponse().getContentAsString());

        // Visit
        MvcResult vr = mvc.perform(post("/visits")
                .header("Authorization", "Bearer " + labAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                        """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        visitId = extractId(vr.getResponse().getContentAsString());
    }

    @Test
    void createDraftReturns201() throws Exception {
        mvc.perform(post("/visits/" + visitId + "/report")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.version").value(1));
    }

    @Test
    void technicianCannotCreateReport() throws Exception {
        mvc.perform(post("/visits/" + visitId + "/report")
                .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void verifyTransitionsToDraftToVerified() throws Exception {
        String reportId = createDraft();

        mvc.perform(put("/reports/" + reportId + "/verify")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VERIFIED"))
                .andExpect(jsonPath("$.data.verifiedBy").isNotEmpty());
    }

    @Test
    void cannotVerifyAlreadyVerifiedReport() throws Exception {
        String reportId = createDraft();
        // First verify
        mvc.perform(put("/reports/" + reportId + "/verify")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isOk());
        // Second verify should fail
        mvc.perform(put("/reports/" + reportId + "/verify")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isConflict());
    }

    @Test
    void releaseGeneratesSnapshotAndSetsPdfFlag() throws Exception {
        enterResult();
        String reportId = createDraftAndVerify();

        mvc.perform(put("/reports/" + reportId + "/release")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RELEASED"))
                .andExpect(jsonPath("$.data.hasPdf").value(true));
    }

    @Test
    void getPdfReturnsBytesForReleasedReport() throws Exception {
        enterResult();
        String reportId = createDraftAndVerify();

        mvc.perform(put("/reports/" + reportId + "/release")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isOk());

        mvc.perform(get("/reports/" + reportId + "/pdf")
                .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    void getPdfFailsForDraftReport() throws Exception {
        String reportId = createDraft();

        mvc.perform(get("/reports/" + reportId + "/pdf")
                .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void correctionSupersededAndCreatesNewDraft() throws Exception {
        enterResult();
        String reportId = createDraftAndVerify();

        mvc.perform(put("/reports/" + reportId + "/release")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isOk());

        MvcResult corrR = mvc.perform(post("/reports/" + reportId + "/correction")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.version").value(2))
                .andReturn();
        String newReportId = extractId(corrR.getResponse().getContentAsString());

        // GET active report should return the new DRAFT
        mvc.perform(get("/visits/" + visitId + "/report")
                .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(newReportId))
                .andExpect(jsonPath("$.data.status").value("DRAFT"));
    }

    @Test
    void cannotCorrectNonReleasedReport() throws Exception {
        String reportId = createDraft();

        mvc.perform(post("/reports/" + reportId + "/correction")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateDraftIsRejected() throws Exception {
        createDraft();

        mvc.perform(post("/visits/" + visitId + "/report")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isConflict());
    }

    @Test
    void tenantIsolationOnReport() throws Exception {
        // Create a second tenant and visit
        MvcResult t2 = mvc.perform(post("/admin/tenants")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Other Lab","slug":"other-lab","planCode":"WEB"}
                        """))
                .andExpect(status().isCreated()).andReturn();
        String tenant2Id = extractId(t2.getResponse().getContentAsString());
        String tenant2Token = jwtUtil.generateAccessToken("user2", "USER", tenant2Id, List.of("PATHOLOGIST"));

        String reportId = createDraft();

        // Other tenant should get 404 on the same report
        mvc.perform(put("/reports/" + reportId + "/verify")
                .header("Authorization", "Bearer " + tenant2Token))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String createDraft() throws Exception {
        MvcResult r = mvc.perform(post("/visits/" + visitId + "/report")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isCreated()).andReturn();
        return extractId(r.getResponse().getContentAsString());
    }

    private String createDraftAndVerify() throws Exception {
        String reportId = createDraft();
        mvc.perform(put("/reports/" + reportId + "/verify")
                .header("Authorization", "Bearer " + pathologistToken))
                .andExpect(status().isOk());
        return reportId;
    }

    private void enterResult() throws Exception {
        mvc.perform(post("/visits/" + visitId + "/results")
                .header("Authorization", "Bearer " + technicianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                        [{"parameterId":"%s","valueNumber":13.5}]
                        """, paramId)))
                .andExpect(status().isOk());
    }

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
