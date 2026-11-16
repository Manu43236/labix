package com.labix.api.catalogue.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RangeResponse {
    private String id;
    private String gender;
    private Integer ageMinDays;
    private Integer ageMaxDays;
    private BigDecimal low;
    private BigDecimal high;
    private BigDecimal criticalLow;
    private BigDecimal criticalHigh;
    private String rangeText;
}
