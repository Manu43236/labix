package com.labix.api.admin.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantResponse {
    private String id;
    private String name;
    private String slug;
    private String status;
    private String phone;
    private String email;
    private String address;
    private String gstin;
    private boolean gstEnabled;
    private String currentPlanCode;
    private LocalDateTime createdAt;
}
