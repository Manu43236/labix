package com.labix.api.visit.dto;

import lombok.Data;

import java.util.List;

@Data
public class BillResponse {
    private String id;
    private int billNo;
    private int subtotalPaise;
    private int discountPaise;
    private int taxPaise;
    private int totalPaise;
    private int paidPaise;
    private String status;
    private List<BillItemResponse> items;
    private List<PaymentResponse> payments;

    @Data
    public static class BillItemResponse {
        private String id;
        private String description;
        private int amountPaise;
    }

    @Data
    public static class PaymentResponse {
        private String id;
        private int amountPaise;
        private String mode;
        private String paidAt;
        private String reference;
    }
}
