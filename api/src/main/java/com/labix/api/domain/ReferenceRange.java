package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "reference_ranges")
@Getter
@Setter
@NoArgsConstructor
public class ReferenceRange extends UuidEntity {

    @Column(name = "parameter_id", nullable = false, length = 36)
    private String parameterId;

    @Column(nullable = false, length = 3)
    private String gender = "ANY"; // M, F, ANY

    @Column(name = "age_min_days")
    private Integer ageMinDays;

    @Column(name = "age_max_days")
    private Integer ageMaxDays;

    @Column(precision = 10, scale = 3)
    private BigDecimal low;

    @Column(precision = 10, scale = 3)
    private BigDecimal high;

    @Column(name = "critical_low", precision = 10, scale = 3)
    private BigDecimal criticalLow;

    @Column(name = "critical_high", precision = 10, scale = 3)
    private BigDecimal criticalHigh;

    @Column(name = "range_text", length = 200)
    private String rangeText;
}
