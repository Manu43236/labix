package com.labix.api.labuser.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLabUserRequest {
    @NotBlank private String name;
    private String phone;
}
