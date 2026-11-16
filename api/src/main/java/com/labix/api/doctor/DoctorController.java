package com.labix.api.doctor;

import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.dto.PageResponse;
import com.labix.api.doctor.dto.DoctorRequest;
import com.labix.api.doctor.dto.DoctorResponse;
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
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DoctorResponse>>> listDoctors(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listDoctors(tenantId(auth), q, PageRequest.of(page, size, Sort.by("name")))));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(
            @Valid @RequestBody DoctorRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createDoctor(tenantId(auth), req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctor(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getDoctor(tenantId(auth), id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable String id,
            @Valid @RequestBody DoctorRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updateDoctor(tenantId(auth), id, req)));
    }

    private String tenantId(Authentication auth) {
        return ((LabixUserDetails) auth.getPrincipal()).getTenantId();
    }
}
