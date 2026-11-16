package com.labix.api.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequest {
    @NotBlank private String name;
    @NotBlank private String gender; // M, F, O
    private LocalDate dob;
    private Integer ageYears;
    private String phone;
    private String email;
    private String address;
}
