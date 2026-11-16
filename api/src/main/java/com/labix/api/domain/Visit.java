package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
public class Visit extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "branch_id", nullable = false, length = 36)
    private String branchId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "doctor_id", length = 36)
    private String doctorId;

    @Column(name = "visit_no", nullable = false)
    private int visitNo;

    @Column(name = "visit_type", nullable = false, length = 10)
    private String visitType = "WALK_IN";

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false, length = 15)
    private String status = "REGISTERED";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
