package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tests")
@Getter
@Setter
@NoArgsConstructor
public class LabTest extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String category;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(name = "price_paise", nullable = false)
    private int pricePaise;

    @Column(name = "tat_hours")
    private Integer tatHours;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
