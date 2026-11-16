package com.labix.api.visit.dto;

import lombok.Data;

@Data
public class SampleResponse {
    private String id;
    private int sampleNo;
    private String barcode;
    private String sampleType;
    private String status;
    private String rejectReason;
}
