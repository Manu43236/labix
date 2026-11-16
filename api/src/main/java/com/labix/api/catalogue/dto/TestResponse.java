package com.labix.api.catalogue.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestResponse {
    private String id;
    private String code;
    private String name;
    private String category;
    private String sampleType;
    private int pricePaise;
    private Integer tatHours;
    private boolean active;
    private List<ParameterResponse> parameters;
}
