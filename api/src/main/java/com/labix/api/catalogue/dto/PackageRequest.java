package com.labix.api.catalogue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class PackageRequest {
    @NotBlank private String name;
    private int pricePaise;
    private boolean active = true;
    private List<String> testIds;
}
