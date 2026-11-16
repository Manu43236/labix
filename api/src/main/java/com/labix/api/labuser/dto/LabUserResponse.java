package com.labix.api.labuser.dto;

import lombok.Data;

import java.util.List;

@Data
public class LabUserResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String status;
    private String signatureFileId;
    private List<String> roles;
}
