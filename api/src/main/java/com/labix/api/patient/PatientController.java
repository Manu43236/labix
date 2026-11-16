package com.labix.api.patient;

import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.dto.PageResponse;
import com.labix.api.patient.dto.PatientRequest;
import com.labix.api.patient.dto.PatientResponse;
import com.labix.api.security.LabixUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> listPatients(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listPatients(tenantId(auth), q, PageRequest.of(page, size, Sort.by("name")))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> createPatient(
            @Valid @RequestBody PatientRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createPatient(tenantId(auth), req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatient(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getPatient(tenantId(auth), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody PatientRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updatePatient(tenantId(auth), id, req)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePatient(
            @PathVariable String id, Authentication auth) {
        service.deletePatient(tenantId(auth), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private String tenantId(Authentication auth) {
        return ((LabixUserDetails) auth.getPrincipal()).getTenantId();
    }
}
