package com.labix.api.labuser;

import com.labix.api.common.dto.ApiResponse;
import com.labix.api.common.dto.PageResponse;
import com.labix.api.labuser.dto.CreateLabUserRequest;
import com.labix.api.labuser.dto.LabUserResponse;
import com.labix.api.labuser.dto.UpdateLabUserRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAB_ADMIN')")
public class LabUserController {

    private final LabUserService service;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LabUserResponse>>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(
                service.listUsers(tenantId(auth), PageRequest.of(page, size, Sort.by("name")))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabUserResponse>> createUser(
            @Valid @RequestBody CreateLabUserRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createUser(tenantId(auth), req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabUserResponse>> getUser(
            @PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.getUser(tenantId(auth), id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabUserResponse>> updateUser(
            @PathVariable String id,
            @Valid @RequestBody UpdateLabUserRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updateUser(tenantId(auth), id, req)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LabUserResponse>> setStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String status = body.getOrDefault("status", "ACTIVE");
        return ResponseEntity.ok(ApiResponse.success(service.setStatus(tenantId(auth), id, status)));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<LabUserResponse>> addRole(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String role = body.get("role");
        return ResponseEntity.ok(ApiResponse.success(service.addRole(tenantId(auth), id, role)));
    }

    @DeleteMapping("/{id}/roles/{role}")
    public ResponseEntity<ApiResponse<LabUserResponse>> removeRole(
            @PathVariable String id,
            @PathVariable String role,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.removeRole(tenantId(auth), id, role)));
    }

    @PostMapping("/{id}/signature")
    public ResponseEntity<ApiResponse<LabUserResponse>> uploadSignature(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.uploadSignature(tenantId(auth), id, file)));
    }

    private String tenantId(Authentication auth) {
        return ((LabixUserDetails) auth.getPrincipal()).getTenantId();
    }
}
