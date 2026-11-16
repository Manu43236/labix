package com.labix.api.branch.dto;

import lombok.Data;

@Data
public class BranchResponse {
    private String id;
    private String name;
    private String address;
    private String phone;
    private boolean main;
}
