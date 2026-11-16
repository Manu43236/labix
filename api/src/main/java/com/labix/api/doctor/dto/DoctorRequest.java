package com.labix.api.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorRequest {
    @NotBlank private String name;
    private String phone;
    private String clinic;
    private String commissionType = "NONE"; // NONE, PERCENT, FLAT
    private BigDecimal commissionValue = BigDecimal.ZERO;
}
