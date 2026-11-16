package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
public class Patient extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "patient_no", nullable = false)
    private long patientNo;

    @Column(nullable = false)
    private String name;

    private LocalDate dob;

    @Column(name = "age_years")
    private Integer ageYears;

    @Column(nullable = false, length = 1)
    private String gender; // M, F, O

    @Column(length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
