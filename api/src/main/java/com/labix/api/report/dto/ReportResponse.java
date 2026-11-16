package com.labix.api.report.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportResponse {
    private String id;
    private String visitId;
    private int version;
    private String status;
    private String verifiedBy;
    private LocalDateTime verifiedAt;
    private LocalDateTime releasedAt;
    private boolean hasPdf;
}
