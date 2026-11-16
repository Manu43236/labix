package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report extends UuidEntity {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "visit_id", nullable = false, length = 36)
    private String visitId;

    @Column(nullable = false)
    private int version = 1;

    @Column(nullable = false, length = 15)
    private String status = "DRAFT";

    @Column(name = "snapshot_json", columnDefinition = "LONGTEXT")
    private String snapshotJson;

    @Column(name = "pdf_file_id", length = 36)
    private String pdfFileId;

    @Column(name = "verified_by", length = 36)
    private String verifiedBy;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
