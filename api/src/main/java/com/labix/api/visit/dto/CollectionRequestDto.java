package com.labix.api.visit.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CollectionRequestDto {

    @NotBlank
    private String address;

    @NotBlank
    private String contactPhone;

    private LocalDateTime slotStart;
    private LocalDateTime slotEnd;
    private String notes;

    // response fields
    private String id;
    private String visitId;
    private String collectorId;
    private String status;
}
