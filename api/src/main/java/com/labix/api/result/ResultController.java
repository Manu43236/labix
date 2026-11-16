package com.labix.api.result;

import com.labix.api.common.dto.ApiResponse;
import com.labix.api.result.dto.ResultEntryItem;
import com.labix.api.result.dto.ResultResponse;
import com.labix.api.result.dto.UpdateRangeRequest;
import com.labix.api.security.LabixUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResultController {

    private final ResultService service;

    @PostMapping("/visits/{visitId}/results")
    @PreAuthorize("hasAnyRole('TECHNICIAN','PATHOLOGIST','LAB_ADMIN')")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> enterResults(
            @PathVariable String visitId,
            @RequestBody List<@Valid ResultEntryItem> items,
            Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                service.enterResults(p.getTenantId(), visitId, items, p.getUserId())));
    }

    @GetMapping("/visits/{visitId}/results")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> listResults(
            @PathVariable String visitId, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                service.listResults(p.getTenantId(), visitId)));
    }

    @PutMapping("/results/{id}/range")
    @PreAuthorize("hasAnyRole('PATHOLOGIST','LAB_ADMIN')")
    public ResponseEntity<ApiResponse<ResultResponse>> updateRange(
            @PathVariable String id,
            @RequestBody UpdateRangeRequest req,
            Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(
                service.updateRange(p.getTenantId(), id, req)));
    }
}
