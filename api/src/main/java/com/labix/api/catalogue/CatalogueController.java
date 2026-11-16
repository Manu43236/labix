package com.labix.api.catalogue;

import com.labix.api.catalogue.dto.*;
import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.dto.PageResponse;
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

import java.util.List;

@RestController
@RequestMapping("/catalogue")
@RequiredArgsConstructor
public class CatalogueController {

    private final CatalogueService service;

    // ── tests ─────────────────────────────────────────────────────────────────

    @GetMapping("/tests")
    public ResponseEntity<ApiResponse<PageResponse<TestResponse>>> listTests(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        String tenantId = tenantId(auth);
        PageRequest pageable = PageRequest.of(page, size, Sort.by("name"));
        return ResponseEntity.ok(ApiResponse.success(service.listTests(tenantId, q, category, pageable)));
    }

    @PostMapping("/tests")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','PATHOLOGIST')")
    public ResponseEntity<ApiResponse<TestResponse>> createTest(
            @Valid @RequestBody TestRequest req, Authentication auth) {
        TestResponse resp = service.createTest(tenantId(auth), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(resp));
    }

    @GetMapping("/tests/{id}")
    public ResponseEntity<ApiResponse<TestResponse>> getTest(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getTest(tenantId(auth), id)));
    }

    @PutMapping("/tests/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','PATHOLOGIST')")
    public ResponseEntity<ApiResponse<TestResponse>> updateTest(
            @PathVariable String id,
            @Valid @RequestBody TestRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updateTest(tenantId(auth), id, req)));
    }

    // ── parameters ────────────────────────────────────────────────────────────

    @PostMapping("/tests/{testId}/parameters")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','PATHOLOGIST')")
    public ResponseEntity<ApiResponse<ParameterResponse>> addParameter(
            @PathVariable String testId,
            @Valid @RequestBody ParameterRequest req,
            Authentication auth) {
        ParameterResponse resp = service.addParameter(tenantId(auth), testId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(resp));
    }

    @PutMapping("/tests/{testId}/parameters/{id}")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','PATHOLOGIST')")
    public ResponseEntity<ApiResponse<ParameterResponse>> updateParameter(
            @PathVariable String testId,
            @PathVariable String id,
            @Valid @RequestBody ParameterRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updateParameter(tenantId(auth), testId, id, req)));
    }

    // ── ranges ────────────────────────────────────────────────────────────────

    @GetMapping("/tests/{testId}/parameters/{id}/ranges")
    public ResponseEntity<ApiResponse<List<RangeResponse>>> getRanges(
            @PathVariable String testId,
            @PathVariable String id,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getRanges(tenantId(auth), testId, id)));
    }

    @PutMapping("/tests/{testId}/parameters/{id}/ranges")
    @PreAuthorize("hasAnyRole('LAB_ADMIN','PATHOLOGIST')")
    public ResponseEntity<ApiResponse<List<RangeResponse>>> replaceRanges(
            @PathVariable String testId,
            @PathVariable String id,
            @RequestBody List<RangeRequest> ranges,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.replaceRanges(tenantId(auth), testId, id, ranges)));
    }

    // ── packages ──────────────────────────────────────────────────────────────

    @GetMapping("/packages")
    public ResponseEntity<ApiResponse<PageResponse<PackageResponse>>> listPackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listPackages(tenantId(auth), PageRequest.of(page, size, Sort.by("name")))));
    }

    @PostMapping("/packages")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @Valid @RequestBody PackageRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createPackage(tenantId(auth), req)));
    }

    @GetMapping("/packages/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getPackage(tenantId(auth), id)));
    }

    @PutMapping("/packages/{id}")
    @PreAuthorize("hasRole('LAB_ADMIN')")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable String id,
            @Valid @RequestBody PackageRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updatePackage(tenantId(auth), id, req)));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private String tenantId(Authentication auth) {
        return ((LabixUserDetails) auth.getPrincipal()).getTenantId();
    }
}
