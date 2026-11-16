package com.labix.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportAccessRequest {

    @NotBlank
    private String reason;
}
