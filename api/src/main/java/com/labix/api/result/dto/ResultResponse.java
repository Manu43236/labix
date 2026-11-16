package com.labix.api.result.dto;

import lombok.Data;

@Data
public class ResultResponse {
    private String id;
    private String visitTestId;
    private String parameterId;
    private String parameterName;
    private String unit;
    private Double valueNumber;
    private String valueText;
    private Double rangeLow;
    private Double rangeHigh;
    private Double criticalLow;
    private Double criticalHigh;
    private String rangeText;
    private String flag;
    private boolean rangeEdited;
}
