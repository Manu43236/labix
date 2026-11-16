package com.labix.api.catalogue.dto;

import lombok.Data;

import java.util.List;

@Data
public class PackageResponse {
    private String id;
    private String name;
    private int pricePaise;
    private boolean active;
    private List<String> testIds;
}
