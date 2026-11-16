package com.labix.api.visit.dto;

import lombok.Data;

@Data
public class VisitTestResponse {
    private String id;
    private String testId;
    private String testName;
    private String testCode;
    private String packageId;
    private int pricePaise;
    private String status;
}
