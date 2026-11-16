package com.labix.api.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateTenantRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase letters, numbers and hyphens only")
    private String slug;

    private String phone;
    private String email;
    private String address;
    private String gstin;
    private boolean gstEnabled;

    @NotBlank
    private String planCode; // WEB, MOB_COLLECT, MOB_FULL
}
