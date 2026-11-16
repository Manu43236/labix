package com.labix.api.admin;

import com.labix.api.admin.dto.*;
import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.dto.PageResponse;
import com.labix.api.security.LabixUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class TenantAdminController {

    private final TenantAdminService service;

    // ── Tenants ───────────────────────────────────────────────────────────────

    @GetMapping("/tenants")
    public ResponseEntity<ApiResponse<PageResponse<TenantResponse>>> listTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = service.listTenants(PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(result)));
    }

    @PostMapping("/tenants")
    public ResponseEntity<ApiResponse<TenantResponse>> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createTenant(request)));
    }

    @GetMapping("/tenants/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> getTenant(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.getTenant(id)));
    }

    @PutMapping("/tenants/{id}")
    public ResponseEntity<ApiResponse<TenantResponse>> updateTenant(
            @PathVariable String id,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.updateTenant(id, request)));
    }

    @PostMapping("/tenants/{id}/suspend")
    public ResponseEntity<ApiResponse<TenantResponse>> suspendTenant(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.suspendTenant(id)));
    }

    @PostMapping("/tenants/{id}/activate")
    public ResponseEntity<ApiResponse<TenantResponse>> activateTenant(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.activateTenant(id)));
    }

    // ── Subscriptions ─────────────────────────────────────────────────────────

    @GetMapping("/tenants/{id}/subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> listSubscriptions(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.listSubscriptions(id)));
    }

    @PostMapping("/tenants/{id}/subscriptions")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> assignPlan(
            @PathVariable String id,
            @Valid @RequestBody AssignPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.assignPlan(id, request)));
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    @PostMapping("/tenants/{id}/users")
    public ResponseEntity<ApiResponse<UserResponse>> createLabAdmin(
            @PathVariable String id,
            @Valid @RequestBody CreateLabAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createLabAdmin(id, request)));
    }

    // ── Support access ────────────────────────────────────────────────────────

    @PostMapping("/tenants/{id}/support-access")
    public ResponseEntity<ApiResponse<SupportAccessResponse>> startSupportAccess(
            @PathVariable String id,
            @Valid @RequestBody SupportAccessRequest request,
            Authentication auth) {
        String superAdminId = ((LabixUserDetails) auth.getPrincipal()).getUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.startSupportAccess(superAdminId, id, request)));
    }

    @PostMapping("/support-access/{logId}/end")
    public ResponseEntity<ApiResponse<SupportAccessResponse>> endSupportAccess(
            @PathVariable String logId,
            Authentication auth) {
        String superAdminId = ((LabixUserDetails) auth.getPrincipal()).getUserId();
        return ResponseEntity.ok(ApiResponse.success(service.endSupportAccess(logId, superAdminId)));
    }

    @GetMapping("/tenants/{id}/support-access")
    public ResponseEntity<ApiResponse<List<SupportAccessResponse>>> listSupportAccess(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(service.listSupportAccess(id)));
    }
}
