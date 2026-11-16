package com.labix.api.visit;

import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.dto.PageResponse;
import com.labix.api.security.LabixUserDetails;
import com.labix.api.visit.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class VisitController {

    private final VisitService service;

    // ── Visits ────────────────────────────────────────────────────────────────

    @GetMapping("/visits")
    public ResponseEntity<ApiResponse<PageResponse<VisitResponse>>> listVisits(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String patientId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listVisits(tenantId(auth), status, patientId, date,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "visitDate")))));
    }

    @PostMapping("/visits")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<VisitResponse>> createVisit(
            @Valid @RequestBody CreateVisitRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createVisit(tenantId(auth), userId(auth), req)));
    }

    @GetMapping("/visits/{id}")
    public ResponseEntity<ApiResponse<VisitResponse>> getVisit(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getVisit(tenantId(auth), id)));
    }

    @PutMapping("/visits/{id}/status")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<VisitResponse>> updateVisitStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updateStatus(tenantId(auth), id, body.get("status"))));
    }

    // ── Bills ─────────────────────────────────────────────────────────────────

    @GetMapping("/visits/{visitId}/bill")
    public ResponseEntity<ApiResponse<BillResponse>> getBill(
            @PathVariable String visitId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getBill(tenantId(auth), visitId)));
    }

    @PutMapping("/visits/{visitId}/bill/discount")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<ApiResponse<BillResponse>> updateDiscount(
            @PathVariable String visitId,
            @RequestBody Map<String, Integer> body,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updateDiscount(tenantId(auth), visitId, body.getOrDefault("discountPaise", 0))));
    }

    @PostMapping("/visits/{visitId}/payments")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<BillResponse>> recordPayment(
            @PathVariable String visitId,
            @Valid @RequestBody RecordPaymentRequest req,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.recordPayment(tenantId(auth), visitId, req)));
    }

    // ── Samples ───────────────────────────────────────────────────────────────

    @GetMapping("/visits/{visitId}/samples")
    public ResponseEntity<ApiResponse<List<SampleResponse>>> listSamples(
            @PathVariable String visitId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.listSamples(tenantId(auth), visitId)));
    }

    @PutMapping("/samples/{id}/status")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','TECHNICIAN','COLLECTOR')")
    public ResponseEntity<ApiResponse<SampleResponse>> updateSampleStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateSampleStatusRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updateSampleStatus(tenantId(auth), id, req)));
    }

    // ── Collection requests ───────────────────────────────────────────────────

    @PostMapping("/visits/{visitId}/collection-request")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','RECEPTIONIST')")
    public ResponseEntity<ApiResponse<CollectionRequestDto>> createCollectionRequest(
            @PathVariable String visitId,
            @Valid @RequestBody CollectionRequestDto req,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createCollectionRequest(tenantId(auth), visitId, req)));
    }

    @GetMapping("/collection-requests")
    public ResponseEntity<ApiResponse<PageResponse<CollectionRequestDto>>> listCollectionRequests(
            @RequestParam(required = false) String collectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        LabixUserDetails principal = principal(auth);
        // COLLECTOR can only see their own requests
        String effectiveCollectorId = principal.getAuthorities().stream()
                .anyMatch(a -> "ROLE_LAB_ADMIN".equals(a.getAuthority()))
                ? collectorId
                : principal.getUserId();
        return ResponseEntity.ok(ApiResponse.success(
                service.listCollectionRequests(tenantId(auth), effectiveCollectorId,
                        PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))));
    }

    @PutMapping("/collection-requests/{id}/assign")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<ApiResponse<CollectionRequestDto>> assignCollector(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.assignCollector(tenantId(auth), id, body.get("collectorUserId"))));
    }

    @PutMapping("/collection-requests/{id}/status")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','COLLECTOR')")
    public ResponseEntity<ApiResponse<CollectionRequestDto>> updateCrStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.updateCrStatus(tenantId(auth), id, body.get("status"))));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String tenantId(Authentication auth) {
        return principal(auth).getTenantId();
    }

    private String userId(Authentication auth) {
        return principal(auth).getUserId();
    }

    private LabixUserDetails principal(Authentication auth) {
        return (LabixUserDetails) auth.getPrincipal();
    }
}
