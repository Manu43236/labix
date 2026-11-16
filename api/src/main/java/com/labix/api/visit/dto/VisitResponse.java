package com.labix.api.visit.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VisitResponse {
    private String id;
    private int visitNo;
    private String visitType;
    private LocalDate visitDate;
    private String status;
    private String patientId;
    private String patientName;
    private String branchId;
    private String branchName;
    private String doctorId;
    private String doctorName;
    private List<VisitTestResponse> tests;
    private BillSummary bill;
    private List<SampleResponse> samples;
}
