package com.labix.api.catalogue.dto;

import lombok.Data;

@Data
public class ParameterResponse {
    private String id;
    private String name;
    private String unit;
    private String valueType;
    private int decimals;
    private int sortOrder;
    private String choicesJson;
}
