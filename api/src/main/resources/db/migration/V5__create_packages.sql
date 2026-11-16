CREATE TABLE packages (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    name        VARCHAR(100) NOT NULL,
    price_paise INT          NOT NULL DEFAULT 0,
    is_active   TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_package_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE package_tests (
    id          VARCHAR(36) NOT NULL,
    package_id  VARCHAR(36) NOT NULL,
    test_id     VARCHAR(36) NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_package_test (package_id, test_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
