package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String clinic;

    @Column(name = "commission_type", nullable = false, length = 10)
    private String commissionType = "NONE"; // NONE, PERCENT, FLAT

    @Column(name = "commission_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal commissionValue = BigDecimal.ZERO;
}
