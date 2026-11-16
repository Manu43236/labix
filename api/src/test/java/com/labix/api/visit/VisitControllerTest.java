package com.labix.api.visit;

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
class VisitControllerTest {

    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwtUtil;
    @Autowired SuperAdminRepository superAdminRepository;
    @Autowired PlanRepository planRepository;
    @Autowired PasswordEncoder passwordEncoder;

    private String adminToken;
    private String tenantId;
    private String labAdminToken;
    private String receptionistToken;
    private String patientId;
    private String branchId;
    private String testId;

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

        // Create tenant
        MvcResult tr = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Visit Lab","slug":"visit-lab","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        tenantId = extractId(tr.getResponse().getContentAsString());

        // Lab admin token
        MvcResult lar = mvc.perform(post("/admin/tenants/" + tenantId + "/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lab Admin","email":"admin@visit.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String labAdminId = extractId(lar.getResponse().getContentAsString());
        labAdminToken = jwtUtil.generateAccessToken(labAdminId, "USER", tenantId, List.of("LAB_ADMIN"));
        receptionistToken = jwtUtil.generateAccessToken("rec-1", "USER", tenantId, List.of("RECEPTIONIST"));

        // Create a branch
        MvcResult br = mvc.perform(post("/branches")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Main","isMain":true}
                                """))
                .andExpect(status().isCreated()).andReturn();
        branchId = extractId(br.getResponse().getContentAsString());

        // Create a patient
        MvcResult pr = mvc.perform(post("/patients")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"John Doe","gender":"M","phone":"9876543210"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        patientId = extractId(pr.getResponse().getContentAsString());

        // Create a test in the catalogue
        MvcResult testR = mvc.perform(post("/catalogue/tests")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"GLUCOSE","name":"Glucose","category":"Biochemistry","sampleType":"Serum","pricePaise":25000}
                                """))
                .andExpect(status().isCreated()).andReturn();
        testId = extractId(testR.getResponse().getContentAsString());
    }

    @Test
    void createVisitReturns201WithBillAndSamples() throws Exception {
        mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visitNo").value(1))
                .andExpect(jsonPath("$.data.status").value("REGISTERED"))
                .andExpect(jsonPath("$.data.patientName").value("John Doe"))
                .andExpect(jsonPath("$.data.bill.subtotalPaise").value(25000))
                .andExpect(jsonPath("$.data.bill.totalPaise").value(25000))
                .andExpect(jsonPath("$.data.bill.status").value("DUE"))
                .andExpect(jsonPath("$.data.samples[0].sampleType").value("Serum"))
                .andExpect(jsonPath("$.data.samples[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data.tests[0].testName").value("Glucose"))
                .andExpect(jsonPath("$.data.tests[0].pricePaise").value(25000));
    }

    @Test
    void visitNoIncrementsPerTenant() throws Exception {
        mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visitNo").value(1));

        mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.visitNo").value(2));
    }

    @Test
    void discountReducesTotalAndKeepsStatusDue() throws Exception {
        mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"],"discountPaise":5000}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bill.subtotalPaise").value(25000))
                .andExpect(jsonPath("$.data.bill.discountPaise").value(5000))
                .andExpect(jsonPath("$.data.bill.totalPaise").value(20000))
                .andExpect(jsonPath("$.data.bill.status").value("DUE"));
    }

    @Test
    void recordPaymentUpdatesBillStatus() throws Exception {
        MvcResult vr = mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        String visitId = extractId(vr.getResponse().getContentAsString());

        // Partial payment
        mvc.perform(post("/visits/" + visitId + "/payments")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amountPaise":10000,"mode":"CASH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paidPaise").value(10000))
                .andExpect(jsonPath("$.data.status").value("PARTIAL"));

        // Full payment
        mvc.perform(post("/visits/" + visitId + "/payments")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amountPaise":15000,"mode":"UPI","reference":"txn123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paidPaise").value(25000))
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.payments", hasSize(2)));
    }

    @Test
    void getBillIncludesItems() throws Exception {
        MvcResult vr = mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        String visitId = extractId(vr.getResponse().getContentAsString());

        mvc.perform(get("/visits/" + visitId + "/bill")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].description").value("Glucose"))
                .andExpect(jsonPath("$.data.items[0].amountPaise").value(25000));
    }

    @Test
    void sampleStatusUpdate() throws Exception {
        MvcResult vr = mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        String visitId = extractId(vr.getResponse().getContentAsString());

        // Get sample id
        MvcResult samplesResult = mvc.perform(get("/visits/" + visitId + "/samples")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1))).andReturn();
        String sampleId = extractId(samplesResult.getResponse().getContentAsString());

        mvc.perform(put("/samples/" + sampleId + "/status")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"COLLECTED"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COLLECTED"));
    }

    @Test
    void rejectSampleRequiresReason() throws Exception {
        MvcResult vr = mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        String visitId = extractId(vr.getResponse().getContentAsString());

        MvcResult samplesResult = mvc.perform(get("/visits/" + visitId + "/samples")
                        .header("Authorization", "Bearer " + labAdminToken))
                .andReturn();
        String sampleId = extractId(samplesResult.getResponse().getContentAsString());

        mvc.perform(put("/samples/" + sampleId + "/status")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"REJECTED"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("REJECT_REASON_REQUIRED"));
    }

    @Test
    void noTestsReturns400() throws Exception {
        mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":[]}
                                """, patientId, branchId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("NO_TESTS"));
    }

    @Test
    void homeCollectionRequest() throws Exception {
        MvcResult vr = mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","visitType":"HOME","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        String visitId = extractId(vr.getResponse().getContentAsString());

        mvc.perform(post("/visits/" + visitId + "/collection-request")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"address":"123 Main St","contactPhone":"9876543210"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("REQUESTED"))
                .andExpect(jsonPath("$.data.address").value("123 Main St"));
    }

    @Test
    void tenantIsolationOnVisit() throws Exception {
        // Create second tenant
        MvcResult t2 = mvc.perform(post("/admin/tenants")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Other Lab","slug":"other-lab2","planCode":"WEB"}
                                """))
                .andExpect(status().isCreated()).andReturn();
        String tenant2Id = extractId(t2.getResponse().getContentAsString());
        String token2 = jwtUtil.generateAccessToken("admin-t2", "USER", tenant2Id, List.of("LAB_ADMIN"));

        // Create visit for tenant 1
        MvcResult vr = mvc.perform(post("/visits")
                        .header("Authorization", "Bearer " + labAdminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"patientId":"%s","branchId":"%s","testIds":["%s"]}
                                """, patientId, branchId, testId)))
                .andExpect(status().isCreated()).andReturn();
        String visitId = extractId(vr.getResponse().getContentAsString());

        // Tenant 2 cannot access tenant 1's visit
        mvc.perform(get("/visits/" + visitId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String extractId(String json) {
        return json.split("\"id\":\"")[1].split("\"")[0];
    }
}
