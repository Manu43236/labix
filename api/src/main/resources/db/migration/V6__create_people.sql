CREATE TABLE patients (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    patient_no  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    dob         DATE,
    age_years   INT,
    gender      VARCHAR(1)   NOT NULL,
    phone       VARCHAR(15),
    email       VARCHAR(100),
    address     TEXT,
    deleted_at  DATETIME(6),
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_patient_tenant_no (tenant_id, patient_no),
    INDEX idx_patient_tenant (tenant_id),
    INDEX idx_patient_phone (tenant_id, phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE doctors (
    id               VARCHAR(36)       NOT NULL,
    tenant_id        VARCHAR(36)       NOT NULL,
    name             VARCHAR(100)   NOT NULL,
    phone            VARCHAR(15),
    clinic           VARCHAR(100),
    commission_type  VARCHAR(10)    NOT NULL DEFAULT 'NONE',
    commission_value DECIMAL(10,2)  NOT NULL DEFAULT 0,
    created_at       DATETIME(6),
    updated_at       DATETIME(6),
    created_by       VARCHAR(36),
    updated_by       VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_doctor_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
