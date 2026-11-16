package com.labix.api.labuser.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateLabUserRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    private String phone;
    @NotBlank @Size(min = 6) private String password;
    private List<String> roles; // LAB_ADMIN, TECHNICIAN, PATHOLOGIST, RECEPTIONIST, COLLECTOR
}
