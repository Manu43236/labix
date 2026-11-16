package com.labix.api.domain;

import com.labix.api.common.domain.UuidEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "package_tests")
@Getter
@Setter
@NoArgsConstructor
public class PackageTest extends UuidEntity {

    @Column(name = "package_id", nullable = false, length = 36)
    private String packageId;

    @Column(name = "test_id", nullable = false, length = 36)
    private String testId;
}
