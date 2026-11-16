package com.labix.api.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public abstract class UuidEntity extends BaseEntity {

    @Id
    @Column(length = 36)
    private String id;

    @PrePersist
    @Override
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        super.onCreate();
    }
}
