package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "tenant_subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class TenantSubscription extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "plan_code", nullable = false, length = 20)
    private String planCode;

    @Column(name = "limits_override_json", columnDefinition = "TEXT")
    private String limitsOverrideJson;

    @Column(name = "features_override_json", columnDefinition = "TEXT")
    private String featuresOverrideJson;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, EXPIRED, CANCELLED

    @Column(name = "discount_note")
    private String discountNote;
}
