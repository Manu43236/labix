CREATE TABLE users (
    id               VARCHAR(36)     NOT NULL,
    tenant_id        VARCHAR(36)     NOT NULL,
    name             VARCHAR(255) NOT NULL,
    phone            VARCHAR(20),
    email            VARCHAR(255),
    password_hash    VARCHAR(255) NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    signature_file_id VARCHAR(36),
    created_at       DATETIME(6)  NOT NULL,
    updated_at       DATETIME(6)  NOT NULL,
    created_by       VARCHAR(36),
    updated_by       VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    INDEX idx_user_tenant_email (tenant_id, email),
    INDEX idx_user_tenant_phone (tenant_id, phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE user_roles (
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    VARCHAR(36)    NOT NULL,
    role       VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    created_by VARCHAR(36),
    updated_by VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE KEY uk_user_role (user_id, role)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
