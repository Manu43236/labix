package com.labix.api.doctor;

import com.labix.api.common.dto.PageResponse;
import com.labix.api.common.exception.AppException;
import com.labix.api.domain.Doctor;
import com.labix.api.doctor.dto.DoctorRequest;
import com.labix.api.doctor.dto.DoctorResponse;
import com.labix.api.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> listDoctors(String tenantId, String q, Pageable pageable) {
        Page<Doctor> page = (q != null && !q.isBlank())
                ? doctorRepository.search(tenantId, q.toLowerCase(), pageable)
                : doctorRepository.findByTenantId(tenantId, pageable);
        return PageResponse.of(page.map(this::toResponse));
    }

    @Transactional
    public DoctorResponse createDoctor(String tenantId, DoctorRequest req) {
        Doctor d = new Doctor();
        d.setTenantId(tenantId);
        d.setName(req.getName());
        d.setPhone(req.getPhone());
        d.setClinic(req.getClinic());
        d.setCommissionType(req.getCommissionType() != null ? req.getCommissionType() : "NONE");
        d.setCommissionValue(req.getCommissionValue() != null ? req.getCommissionValue() : java.math.BigDecimal.ZERO);
        doctorRepository.save(d);
        return toResponse(d);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctor(String tenantId, String id) {
        return toResponse(findDoctor(tenantId, id));
    }

    @Transactional
    public DoctorResponse updateDoctor(String tenantId, String id, DoctorRequest req) {
        Doctor d = findDoctor(tenantId, id);
        d.setName(req.getName());
        d.setPhone(req.getPhone());
        d.setClinic(req.getClinic());
        d.setCommissionType(req.getCommissionType() != null ? req.getCommissionType() : "NONE");
        d.setCommissionValue(req.getCommissionValue() != null ? req.getCommissionValue() : java.math.BigDecimal.ZERO);
        doctorRepository.save(d);
        return toResponse(d);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Doctor findDoctor(String tenantId, String id) {
        Doctor d = doctorRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND", "Doctor not found"));
        if (!d.getTenantId().equals(tenantId)) {
            throw new AppException(HttpStatus.NOT_FOUND, "DOCTOR_NOT_FOUND", "Doctor not found");
        }
        return d;
    }

    private DoctorResponse toResponse(Doctor d) {
        DoctorResponse r = new DoctorResponse();
        r.setId(d.getId());
        r.setName(d.getName());
        r.setPhone(d.getPhone());
        r.setClinic(d.getClinic());
        r.setCommissionType(d.getCommissionType());
        r.setCommissionValue(d.getCommissionValue());
        return r;
    }
}
