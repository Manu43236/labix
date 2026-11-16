package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visit_tests")
@Getter
@Setter
@NoArgsConstructor
public class VisitTest extends UuidEntity {

    @Column(name = "visit_id", nullable = false, length = 36)
    private String visitId;

    @Column(name = "test_id", nullable = false, length = 36)
    private String testId;

    @Column(name = "package_id", length = 36)
    private String packageId;

    @Column(name = "price_paise", nullable = false)
    private int pricePaise;

    @Column(nullable = false, length = 10)
    private String status = "PENDING";
}
