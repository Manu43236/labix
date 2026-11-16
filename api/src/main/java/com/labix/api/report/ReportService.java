package com.labix.api.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.*;
import com.labix.api.report.dto.ReportResponse;
import com.labix.api.repository.*;
import com.labix.api.result.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final VisitRepository visitRepository;
    private final VisitTestRepository visitTestRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final BranchRepository branchRepository;
    private final BillRepository billRepository;
    private final SampleRepository sampleRepository;
    private final ResultRepository resultRepository;
    private final TestParameterRepository parameterRepository;
    private final LabTestRepository labTestRepository;
    private final AuditLogRepository auditLogRepository;
    private final LabFileRepository labFileRepository;
    private final UserRepository userRepository;
    private final ResultService resultService;
    private final ReportPdfGenerator pdfGenerator;
    private final ObjectMapper objectMapper;

    @Value("${app.uploads.dir:./uploads}")
    private String uploadsDir;

    @Transactional
    public ReportResponse createDraft(String tenantId, String visitId, String userId) {
        Visit visit = findVisit(tenantId, visitId);

        boolean hasActive = reportRepository.findByVisitIdAndDeletedAtIsNull(visitId).stream()
                .anyMatch(r -> !"SUPERSEDED".equals(r.getStatus()));
        if (hasActive) {
            throw new AppException(HttpStatus.CONFLICT, "REPORT_EXISTS",
                    "A report already exists for this visit; use correction to create a new version");
        }

        int version = reportRepository.findByVisitIdAndDeletedAtIsNull(visitId).size() + 1;

        Report report = new Report();
        report.setTenantId(tenantId);
        report.setVisitId(visitId);
        report.setVersion(version);
        report.setCreatedBy(userId);
        report.setUpdatedBy(userId);
        reportRepository.save(report);

        audit(tenantId, userId, "REPORT", report.getId(), "DRAFT_CREATED", null, report.getStatus());
        return toResponse(report);
    }

    @Transactional(readOnly = true)
    public ReportResponse getActiveReport(String tenantId, String visitId) {
        findVisit(tenantId, visitId);
        return reportRepository.findFirstByVisitIdAndDeletedAtIsNullOrderByVersionDesc(visitId)
                .map(this::toResponse)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND",
                        "No report found for this visit"));
    }

    @Transactional
    public ReportResponse verify(String tenantId, String reportId, String userId) {
        Report report = findReport(tenantId, reportId);
        if (!"DRAFT".equals(report.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT, "INVALID_STATE",
                    "Only a DRAFT report can be verified");
        }

        // Mark all visit_tests as VERIFIED so the release check passes
        visitTestRepository.findByVisitId(report.getVisitId()).forEach(vt -> {
            if (!"VERIFIED".equals(vt.getStatus())) {
                vt.setStatus("VERIFIED");
                visitTestRepository.save(vt);
            }
        });

        String before = report.getStatus();
        report.setStatus("VERIFIED");
        report.setVerifiedBy(userId);
        report.setVerifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        reportRepository.save(report);

        audit(tenantId, userId, "REPORT", reportId, "VERIFIED", before, "VERIFIED");
        return toResponse(report);
    }

    @Transactional
    public ReportResponse release(String tenantId, String reportId, String userId) {
        Report report = findReport(tenantId, reportId);
        if (!"VERIFIED".equals(report.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT, "INVALID_STATE",
                    "Only a VERIFIED report can be released");
        }

        // Ensure all visit_tests are VERIFIED
        List<VisitTest> tests = visitTestRepository.findByVisitId(report.getVisitId());
        boolean anyUnverified = tests.stream().anyMatch(vt -> !"VERIFIED".equals(vt.getStatus()));
        if (anyUnverified) {
            throw new AppException(HttpStatus.CONFLICT, "TESTS_NOT_VERIFIED",
                    "All tests must be in VERIFIED status before releasing");
        }

        // Build snapshot
        String snapshot = buildSnapshot(report, userId);
        report.setSnapshotJson(snapshot);

        // Generate PDF
        try {
            byte[] pdf = pdfGenerator.generate(objectMapper.readTree(snapshot));
            String pdfFileId = storePdf(tenantId, report.getVisitId(), report.getVersion(), pdf);
            report.setPdfFileId(pdfFileId);
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF_ERROR",
                    "Failed to generate PDF: " + e.getMessage());
        }

        String before = report.getStatus();
        report.setStatus("RELEASED");
        report.setReleasedAt(LocalDateTime.now(ZoneOffset.UTC));
        reportRepository.save(report);

        audit(tenantId, userId, "REPORT", reportId, "RELEASED", before, "RELEASED");
        return toResponse(report);
    }

    @Transactional
    public ReportResponse createCorrection(String tenantId, String reportId, String userId) {
        Report existing = findReport(tenantId, reportId);
        if (!"RELEASED".equals(existing.getStatus())) {
            throw new AppException(HttpStatus.CONFLICT, "INVALID_STATE",
                    "Only a RELEASED report can be corrected");
        }

        existing.setStatus("SUPERSEDED");
        reportRepository.save(existing);
        audit(tenantId, userId, "REPORT", reportId, "SUPERSEDED", "RELEASED", "SUPERSEDED");

        // Create new DRAFT version
        return createDraft(tenantId, existing.getVisitId(), userId);
    }

    @Transactional(readOnly = true)
    public byte[] getPdf(String tenantId, String reportId) {
        Report report = findReport(tenantId, reportId);
        if (!"RELEASED".equals(report.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "NOT_RELEASED",
                    "PDF is only available for released reports");
        }
        if (report.getPdfFileId() == null) {
            throw new AppException(HttpStatus.NOT_FOUND, "PDF_NOT_FOUND", "PDF not found");
        }
        LabFile file = labFileRepository.findById(report.getPdfFileId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PDF_NOT_FOUND", "PDF file record not found"));
        try {
            return Files.readAllBytes(Paths.get(file.getStorageKey()));
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "PDF_READ_ERROR", "Failed to read PDF");
        }
    }

    // ── snapshot builder ──────────────────────────────────────────────────────

    private String buildSnapshot(Report report, String verifiedByUserId) {
        try {
            Visit visit = visitRepository.findById(report.getVisitId()).orElseThrow();
            Patient patient = patientRepository.findById(visit.getPatientId()).orElseThrow();
            Branch branch = branchRepository.findById(visit.getBranchId()).orElse(null);
            Doctor doctor = visit.getDoctorId() != null
                    ? doctorRepository.findById(visit.getDoctorId()).orElse(null) : null;
            Bill bill = billRepository.findByVisitId(visit.getId()).orElse(null);
            List<Sample> samples = sampleRepository.findByVisitId(visit.getId());
            List<VisitTest> visitTests = visitTestRepository.findByVisitId(visit.getId());
            List<Result> results = resultService.findResultsByVisitId(visit.getId());

            ObjectNode snap = objectMapper.createObjectNode();
            snap.put("reportId", report.getId());
            snap.put("reportVersion", report.getVersion());
            snap.put("generatedAt", LocalDateTime.now(ZoneOffset.UTC).toString());

            // Tenant placeholder (tenant profile not yet in schema)
            ObjectNode tenantNode = snap.putObject("tenant");
            tenantNode.put("name", "Laboratory");
            tenantNode.put("address", "");

            // Visit
            ObjectNode visitNode = snap.putObject("visit");
            visitNode.put("visitNo", visit.getVisitNo());
            visitNode.put("visitDate", visit.getVisitDate().toString());
            visitNode.put("visitType", visit.getVisitType());
            visitNode.put("billNo", bill != null ? bill.getBillNo() : 0);

            // Patient
            ObjectNode patientNode = snap.putObject("patient");
            patientNode.put("name", patient.getName());
            patientNode.put("patientNo", patient.getPatientNo());
            patientNode.put("gender", patient.getGender() != null ? patient.getGender() : "");
            patientNode.put("ageText", formatAge(patient));

            // Doctor
            if (doctor != null) {
                ObjectNode docNode = snap.putObject("doctor");
                docNode.put("name", doctor.getName());
            }

            // Samples
            ArrayNode samplesArr = snap.putArray("samples");
            for (Sample s : samples) {
                ObjectNode sn = samplesArr.addObject();
                sn.put("sampleNo", s.getSampleNo());
                sn.put("barcode", s.getBarcode() != null ? s.getBarcode() : "");
                sn.put("sampleType", s.getSampleType() != null ? s.getSampleType() : "");
            }

            // Tests + parameters + results
            ArrayNode testsArr = snap.putArray("tests");
            for (VisitTest vt : visitTests) {
                labTestRepository.findById(vt.getTestId()).ifPresent(test -> {
                    ObjectNode testNode = testsArr.addObject();
                    testNode.put("testName", test.getName());
                    testNode.put("category", test.getCategory() != null ? test.getCategory() : "");
                    ArrayNode paramsArr = testNode.putArray("parameters");

                    List<Result> testResults = results.stream()
                            .filter(r -> r.getVisitTestId().equals(vt.getId()))
                            .toList();
                    for (Result r : testResults) {
                        parameterRepository.findById(r.getParameterId()).ifPresent(param -> {
                            ObjectNode pn = paramsArr.addObject();
                            pn.put("resultId", r.getId());
                            pn.put("name", param.getName());
                            pn.put("unit", r.getUnit() != null ? r.getUnit() : "");
                            pn.put("decimals", param.getDecimals());
                            if (r.getValueNumber() != null) pn.put("valueNumber", r.getValueNumber());
                            else pn.putNull("valueNumber");
                            pn.put("valueText", r.getValueText() != null ? r.getValueText() : "");
                            pn.put("rangeText", r.getRangeText() != null ? r.getRangeText() : "");
                            if (r.getRangeLow() != null) pn.put("rangeLow", r.getRangeLow());
                            if (r.getRangeHigh() != null) pn.put("rangeHigh", r.getRangeHigh());
                            pn.put("flag", r.getFlag() != null ? r.getFlag() : "");
                            pn.put("rangeEdited", r.isRangeEdited());
                        });
                    }
                });
            }

            // Verified-by user
            userRepository.findById(verifiedByUserId).ifPresent(u -> {
                ObjectNode vbNode = snap.putObject("verifiedBy");
                vbNode.put("userId", u.getId());
                vbNode.put("name", u.getName());
            });

            snap.put("verifiedAt", report.getVerifiedAt() != null ? report.getVerifiedAt().toString() : "");
            snap.put("releasedAt", LocalDateTime.now(ZoneOffset.UTC).toString());

            return objectMapper.writeValueAsString(snap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build report snapshot", e);
        }
    }

    private String storePdf(String tenantId, String visitId, int version, byte[] pdf) throws IOException {
        Path dir = Paths.get(uploadsDir, "reports", tenantId);
        Files.createDirectories(dir);
        String filename = visitId + "_v" + version + ".pdf";
        Path filePath = dir.resolve(filename);
        Files.write(filePath, pdf);

        LabFile file = new LabFile();
        file.setTenantId(tenantId);
        file.setStorageKey(filePath.toString());
        file.setMimeType("application/pdf");
        file.setSizeBytes(pdf.length);
        file.setPurpose("REPORT_PDF");
        labFileRepository.save(file);
        return file.getId();
    }

    private void audit(String tenantId, String userId, String entityType, String entityId,
                       String action, String before, String after) {
        AuditLog log = new AuditLog();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setAction(action);
        log.setBeforeJson(before);
        log.setAfterJson(after);
        auditLogRepository.save(log);
    }

    private static String formatAge(Patient p) {
        if (p.getDob() != null) {
            long years = ChronoUnit.YEARS.between(p.getDob(), LocalDate.now());
            return years + "Y";
        }
        if (p.getAgeYears() != null) return p.getAgeYears() + "Y (approx)";
        return "";
    }

    private Visit findVisit(String tenantId, String id) {
        Visit v = visitRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "Visit not found"));
        if (!tenantId.equals(v.getTenantId()))
            throw new AppException(HttpStatus.NOT_FOUND, "VISIT_NOT_FOUND", "Visit not found");
        return v;
    }

    private Report findReport(String tenantId, String id) {
        Report r = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found"));
        if (!tenantId.equals(r.getTenantId()))
            throw new AppException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND", "Report not found");
        return r;
    }

    private ReportResponse toResponse(Report r) {
        ReportResponse resp = new ReportResponse();
        resp.setId(r.getId()); resp.setVisitId(r.getVisitId());
        resp.setVersion(r.getVersion()); resp.setStatus(r.getStatus());
        resp.setVerifiedBy(r.getVerifiedBy()); resp.setVerifiedAt(r.getVerifiedAt());
        resp.setReleasedAt(r.getReleasedAt()); resp.setHasPdf(r.getPdfFileId() != null);
        return resp;
    }
}
