package com.labix.api.result.dto;

import lombok.Data;

@Data
public class UpdateRangeRequest {
    private Double rangeLow;
    private Double rangeHigh;
    private Double criticalLow;
    private Double criticalHigh;
    private String rangeText;
}
