package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_access_logs")
@Getter
@Setter
@NoArgsConstructor
public class SupportAccessLog extends UuidEntity {

    @Column(name = "super_admin_id", nullable = false, length = 36)
    private String superAdminId;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(length = 500)
    private String reason;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}
