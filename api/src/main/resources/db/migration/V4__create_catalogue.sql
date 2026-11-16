CREATE TABLE tests (
    id              VARCHAR(36)     NOT NULL,
    tenant_id       VARCHAR(36)     NOT NULL,
    code            VARCHAR(20)  NOT NULL,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(50),
    sample_type     VARCHAR(50),
    price_paise     INT          NOT NULL DEFAULT 0,
    tat_hours       INT,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_test_tenant_code (tenant_id, code),
    INDEX idx_test_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE test_parameters (
    id              VARCHAR(36)     NOT NULL,
    test_id         VARCHAR(36)     NOT NULL,
    name            VARCHAR(100) NOT NULL,
    unit            VARCHAR(30),
    value_type      VARCHAR(10)  NOT NULL DEFAULT 'NUMBER',
    choices_json    TEXT,
    decimals        INT          NOT NULL DEFAULT 1,
    sort_order      INT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_param_test (test_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reference_ranges (
    id              VARCHAR(36)         NOT NULL,
    parameter_id    VARCHAR(36)         NOT NULL,
    gender          VARCHAR(3)       NOT NULL DEFAULT 'ANY',
    age_min_days    INT,
    age_max_days    INT,
    low             DECIMAL(10,3),
    high            DECIMAL(10,3),
    critical_low    DECIMAL(10,3),
    critical_high   DECIMAL(10,3),
    range_text      VARCHAR(200),
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_range_param (parameter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
