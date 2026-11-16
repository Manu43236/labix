package com.labix.api.result.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResultEntryItem {

    @NotBlank
    private String parameterId;

    private Double valueNumber;
    private String valueText;
}
