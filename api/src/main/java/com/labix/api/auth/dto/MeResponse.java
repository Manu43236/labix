package com.labix.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MeResponse {
    private String userId;
    private String tenantId;
    private boolean superAdmin;
    private List<String> roles;
}
