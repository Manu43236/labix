package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "test_parameters")
@Getter
@Setter
@NoArgsConstructor
public class TestParameter extends UuidEntity {

    @Column(name = "test_id", nullable = false, length = 36)
    private String testId;

    @Column(nullable = false)
    private String name;

    @Column(length = 30)
    private String unit;

    @Column(name = "value_type", nullable = false, length = 10)
    private String valueType = "NUMBER"; // NUMBER, TEXT, CHOICE

    @Column(name = "choices_json", columnDefinition = "TEXT")
    private String choicesJson;

    @Column(nullable = false)
    private int decimals = 1;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
