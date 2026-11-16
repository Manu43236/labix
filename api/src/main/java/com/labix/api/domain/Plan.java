package com.labix.api.domain;

import com.labix.api.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
public class Plan extends BaseEntity {

    @Id
    @Column(length = 20)
    private String code; // WEB, MOB_COLLECT, MOB_FULL

    @Column(nullable = false)
    private String name;

    @Column(name = "price_paise", nullable = false)
    private long pricePaise;

    @Column(name = "limits_json", columnDefinition = "TEXT")
    private String limitsJson;

    @Column(name = "features_json", columnDefinition = "TEXT")
    private String featuresJson;
}
