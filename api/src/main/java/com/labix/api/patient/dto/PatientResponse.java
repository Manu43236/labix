package com.labix.api.patient.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientResponse {
    private String id;
    private long patientNo;
    private String name;
    private String gender;
    private LocalDate dob;
    private Integer ageYears;
    private String phone;
    private String email;
    private String address;
}
