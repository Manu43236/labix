package com.labix.api.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupportAccessResponse {
    private String id;
    private String superAdminId;
    private String tenantId;
    private String reason;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
