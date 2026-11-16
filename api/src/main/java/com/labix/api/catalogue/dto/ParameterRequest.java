package com.labix.api.catalogue.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ParameterRequest {
    @NotBlank private String name;
    private String unit;
    private String valueType = "NUMBER"; // NUMBER, TEXT, CHOICE
    private int decimals = 1;
    private int sortOrder;
    private List<String> choices;
}
