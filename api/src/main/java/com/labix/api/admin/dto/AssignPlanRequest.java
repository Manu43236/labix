package com.labix.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssignPlanRequest {

    @NotBlank
    private String planCode;

    private LocalDate startDate;
    private LocalDate endDate;
    private String discountNote;
}
