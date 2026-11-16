package com.labix.api.visit.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RecordPaymentRequest {

    @Min(1)
    private int amountPaise;

    private String mode = "CASH";

    private String reference;
}
