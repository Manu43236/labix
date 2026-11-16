package com.labix.api.branch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BranchRequest {
    @NotBlank private String name;
    private String address;
    private String phone;
    private boolean main;
}
