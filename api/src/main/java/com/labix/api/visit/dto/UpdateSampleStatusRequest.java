package com.labix.api.visit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSampleStatusRequest {

    @NotBlank
    private String status;

    private String rejectReason;
}
