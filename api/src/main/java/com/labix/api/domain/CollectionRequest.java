package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "collection_requests")
@Getter
@Setter
@NoArgsConstructor
public class CollectionRequest extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "visit_id", nullable = false, length = 36)
    private String visitId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    @Column(name = "slot_start")
    private LocalDateTime slotStart;

    @Column(name = "slot_end")
    private LocalDateTime slotEnd;

    @Column(name = "collector_id", length = 36)
    private String collectorId;

    @Column(nullable = false, length = 15)
    private String status = "REQUESTED";

    @Column(columnDefinition = "TEXT")
    private String notes;
}
