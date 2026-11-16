package com.labix.api.catalogue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    private String category;
    private String sampleType;
    private int pricePaise;
    private Integer tatHours;
    private boolean active = true;
}
