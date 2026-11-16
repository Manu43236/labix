package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bill_items")
@Getter
@Setter
@NoArgsConstructor
public class BillItem extends UuidEntity {

    @Column(name = "bill_id", nullable = false, length = 36)
    private String billId;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(name = "amount_paise", nullable = false)
    private int amountPaise;
}
