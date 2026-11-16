package com.labix.api.admin.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SubscriptionResponse {
    private String id;
    private String planCode;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String discountNote;
    private LocalDateTime createdAt;
}
