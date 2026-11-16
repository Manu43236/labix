package com.labix.api.report;

import com.labix.api.common.dto.ApiResponse;
import com.labix.api.report.dto.ReportResponse;
import com.labix.api.security.LabixUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService service;

    @PostMapping("/visits/{visitId}/report")
    @PreAuthorize("hasAnyRole('PATHOLOGIST','LAB_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> createDraft(
            @PathVariable String visitId, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createDraft(p.getTenantId(), visitId, p.getUserId())));
    }

    @GetMapping("/visits/{visitId}/report")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(
            @PathVariable String visitId, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(service.getActiveReport(p.getTenantId(), visitId)));
    }

    @PutMapping("/reports/{id}/verify")
    @PreAuthorize("hasAnyRole('PATHOLOGIST','LAB_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> verify(
            @PathVariable String id, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(service.verify(p.getTenantId(), id, p.getUserId())));
    }

    @PutMapping("/reports/{id}/release")
    @PreAuthorize("hasAnyRole('PATHOLOGIST','LAB_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> release(
            @PathVariable String id, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(service.release(p.getTenantId(), id, p.getUserId())));
    }

    @PostMapping("/reports/{id}/correction")
    @PreAuthorize("hasAnyRole('PATHOLOGIST','LAB_ADMIN')")
    public ResponseEntity<ApiResponse<ReportResponse>> correction(
            @PathVariable String id, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.createCorrection(p.getTenantId(), id, p.getUserId())));
    }

    @GetMapping("/reports/{id}/pdf")
    public ResponseEntity<byte[]> getPdf(@PathVariable String id, Authentication auth) {
        LabixUserDetails p = (LabixUserDetails) auth.getPrincipal();
        byte[] pdf = service.getPdf(p.getTenantId(), id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
