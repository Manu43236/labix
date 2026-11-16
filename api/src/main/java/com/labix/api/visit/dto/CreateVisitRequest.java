package com.labix.api.visit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateVisitRequest {

    @NotBlank
    private String patientId;

    @NotBlank
    private String branchId;

    private String doctorId;

    private String visitType = "WALK_IN";

    private List<String> testIds;

    private List<String> packageIds;

    private int discountPaise;
}
