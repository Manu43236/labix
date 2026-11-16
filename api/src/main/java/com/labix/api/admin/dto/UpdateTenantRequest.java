package com.labix.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTenantRequest {

    @NotBlank
    private String name;

    private String phone;
    private String email;
    private String address;
    private String gstin;
    private boolean gstEnabled;
}
