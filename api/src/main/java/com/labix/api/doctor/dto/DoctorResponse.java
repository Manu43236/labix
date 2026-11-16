package com.labix.api.doctor.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorResponse {
    private String id;
    private String name;
    private String phone;
    private String clinic;
    private String commissionType;
    private BigDecimal commissionValue;
}
