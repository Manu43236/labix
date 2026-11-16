package com.labix.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SuperAdminLoginRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
