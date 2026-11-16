package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bills")
@Getter
@Setter
@NoArgsConstructor
public class Bill extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "visit_id", nullable = false, length = 36)
    private String visitId;

    @Column(name = "bill_no", nullable = false)
    private int billNo;

    @Column(name = "subtotal_paise", nullable = false)
    private int subtotalPaise;

    @Column(name = "discount_paise", nullable = false)
    private int discountPaise;

    @Column(name = "tax_paise", nullable = false)
    private int taxPaise;

    @Column(name = "total_paise", nullable = false)
    private int totalPaise;

    @Column(name = "paid_paise", nullable = false)
    private int paidPaise;

    @Column(nullable = false, length = 10)
    private String status = "DUE";
}
