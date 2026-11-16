package com.labix.api.patient;

import com.labix.api.common.dto.PageResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.Patient;
import com.labix.api.patient.dto.PatientRequest;
import com.labix.api.patient.dto.PatientResponse;
import com.labix.api.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> listPatients(String tenantId, String q, Pageable pageable) {
        Page<Patient> page = (q != null && !q.isBlank())
                ? patientRepository.search(tenantId, q.toLowerCase(), pageable)
                : patientRepository.findByTenantId(tenantId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public PatientResponse createPatient(String tenantId, PatientRequest req) {
        Long maxNo = patientRepository.findMaxPatientNo(tenantId);
        long nextNo = (maxNo != null ? maxNo : 0L) + 1L;

        Patient p = new Patient();
        p.setTenantId(tenantId);
        p.setPatientNo(nextNo);
        p.setName(req.getName());
        p.setGender(req.getGender());
        p.setDob(req.getDob());
        p.setAgeYears(req.getAgeYears());
        p.setPhone(req.getPhone());
        p.setEmail(req.getEmail());
        p.setAddress(req.getAddress());
        patientRepository.save(p);
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatient(String tenantId, String id) {
        return toResponse(findPatient(tenantId, id));
    }

    @Transactional
    public PatientResponse updatePatient(String tenantId, String id, PatientRequest req) {
        Patient p = findPatient(tenantId, id);
        p.setName(req.getName());
        p.setGender(req.getGender());
        p.setDob(req.getDob());
        p.setAgeYears(req.getAgeYears());
        p.setPhone(req.getPhone());
        p.setEmail(req.getEmail());
        p.setAddress(req.getAddress());
        patientRepository.save(p);
        return toResponse(p);
    }

    @Transactional
    public void deletePatient(String tenantId, String id) {
        Patient p = findPatient(tenantId, id);
        p.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));
        patientRepository.save(p);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Patient findPatient(String tenantId, String id) {
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found"));
        if (!p.getTenantId().equals(tenantId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", "Patient not found");
        }
        return p;
    }

    private PatientResponse toResponse(Patient p) {
        PatientResponse r = new PatientResponse();
        r.setId(p.getId());
        r.setPatientNo(p.getPatientNo());
        r.setName(p.getName());
        r.setGender(p.getGender());
        r.setDob(p.getDob());
        r.setAgeYears(p.getAgeYears());
        r.setPhone(p.getPhone());
        r.setEmail(p.getEmail());
        r.setAddress(p.getAddress());
        return r;
    }
}
