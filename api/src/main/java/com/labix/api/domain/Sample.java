package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "samples")
@Getter
@Setter
@NoArgsConstructor
public class Sample extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "visit_id", nullable = false, length = 36)
    private String visitId;

    @Column(name = "sample_no", nullable = false)
    private int sampleNo;

    @Column(length = 50)
    private String barcode;

    @Column(name = "sample_type", length = 50)
    private String sampleType;

    @Column(nullable = false, length = 15)
    private String status = "PENDING";

    @Column(name = "collected_by", length = 36)
    private String collectedBy;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "received_by", length = 36)
    private String receivedBy;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "reject_reason", length = 300)
    private String rejectReason;
}
