package com.labix.api.visit.dto;

import lombok.Data;

@Data
public class BillSummary {
    private String id;
    private int billNo;
    private int subtotalPaise;
    private int discountPaise;
    private int totalPaise;
    private int paidPaise;
    private String status;
}
