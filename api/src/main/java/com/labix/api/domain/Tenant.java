package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant extends UuidEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE"; // ACTIVE, SUSPENDED

    @Column(length = 20)
    private String phone;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 15)
    private String gstin;

    @Column(name = "gst_enabled")
    private boolean gstEnabled = false;

    @Column(name = "logo_file_id", length = 36)
    private String logoFileId;

    @Column(name = "letterhead_json", columnDefinition = "TEXT")
    private String letterheadJson;
}
