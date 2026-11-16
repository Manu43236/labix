package com.labix.api.admin.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserResponse {
    private String id;
    private String tenantId;
    private String name;
    private String email;
    private String phone;
    private String status;
    private List<String> roles;
}
