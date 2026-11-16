package com.labix.api.branch;

import com.labix.api.branch.dto.BranchRequest;
import com.labix.api.branch.dto.BranchResponse;
import com.labix.api.common.dto.ApiResponse;
import com.labix.api.security.LabixUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/branches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAB_ADMIN')")
public class BranchController {

    private final BranchService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BranchResponse>>> listBranches(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.listBranches(tenantId(auth))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
            @Valid @RequestBody BranchRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createBranch(tenantId(auth), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
            @PathVariable String id,
            @Valid @RequestBody BranchRequest req,
            Authentication auth) {
        return ResponseEntity.ok(ApiResponse.success(service.updateBranch(tenantId(auth), id, req)));
    }

    private String tenantId(Authentication auth) {
        return ((LabixUserDetails) auth.getPrincipal()).getTenantId();
    }
}
