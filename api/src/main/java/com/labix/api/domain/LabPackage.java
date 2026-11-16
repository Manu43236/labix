package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "packages")
@Getter
@Setter
@NoArgsConstructor
public class LabPackage extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column(name = "price_paise", nullable = false)
    private int pricePaise;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
