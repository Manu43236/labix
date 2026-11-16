CREATE TABLE visits (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    branch_id   VARCHAR(36)     NOT NULL,
    patient_id  VARCHAR(36)     NOT NULL,
    doctor_id   VARCHAR(36),
    visit_no    INT          NOT NULL,
    visit_type  VARCHAR(10)  NOT NULL DEFAULT 'WALK_IN',
    visit_date  DATE         NOT NULL,
    status      VARCHAR(15)  NOT NULL DEFAULT 'REGISTERED',
    deleted_at  DATETIME(6),
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_visit_no (tenant_id, visit_no),
    INDEX idx_visit_tenant_date (tenant_id, visit_date),
    INDEX idx_visit_patient (tenant_id, patient_id),
    INDEX idx_visit_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE visit_tests (
    id          VARCHAR(36)     NOT NULL,
    visit_id    VARCHAR(36)     NOT NULL,
    test_id     VARCHAR(36)     NOT NULL,
    package_id  VARCHAR(36),
    price_paise INT          NOT NULL DEFAULT 0,
    status      VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_visit_test_visit (visit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
