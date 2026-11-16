package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "results")
@Getter
@Setter
@NoArgsConstructor
public class Result extends UuidEntity {

    @Column(name = "visit_test_id", nullable = false, length = 36)
    private String visitTestId;

    @Column(name = "parameter_id", nullable = false, length = 36)
    private String parameterId;

    @Column(name = "value_text", length = 500)
    private String valueText;

    @Column(name = "value_number")
    private Double valueNumber;

    @Column(length = 30)
    private String unit;

    @Column(name = "range_low")
    private Double rangeLow;

    @Column(name = "range_high")
    private Double rangeHigh;

    @Column(name = "critical_low")
    private Double criticalLow;

    @Column(name = "critical_high")
    private Double criticalHigh;

    @Column(name = "range_text", length = 100)
    private String rangeText;

    @Column(length = 10)
    private String flag;

    @Column(name = "range_edited", nullable = false)
    private boolean rangeEdited;

    @Column(name = "entered_by", length = 36)
    private String enteredBy;

    @Column(name = "entered_at")
    private LocalDateTime enteredAt;
}
