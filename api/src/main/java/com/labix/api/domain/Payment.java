package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends UuidEntity {

    @Column(name = "bill_id", nullable = false, length = 36)
    private String billId;

    @Column(name = "amount_paise", nullable = false)
    private int amountPaise;

    @Column(nullable = false, length = 10)
    private String mode = "CASH";

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(length = 100)
    private String reference;
}
