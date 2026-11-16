CREATE TABLE plans (
    code        VARCHAR(20)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    price_paise BIGINT       NOT NULL,
    limits_json TEXT,
    features_json TEXT,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE tenants (
    id              VARCHAR(36)     NOT NULL,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    phone           VARCHAR(20),
    email           VARCHAR(255),
    address         TEXT,
    gstin           VARCHAR(15),
    gst_enabled     BOOLEAN      NOT NULL DEFAULT FALSE,
    logo_file_id    VARCHAR(36),
    letterhead_json TEXT,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_slug (slug)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE tenant_subscriptions (
    id                    VARCHAR(36)    NOT NULL,
    tenant_id             VARCHAR(36)    NOT NULL,
    plan_code             VARCHAR(20) NOT NULL,
    limits_override_json  TEXT,
    features_override_json TEXT,
    start_date            DATE,
    end_date              DATE,
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    discount_note         VARCHAR(500),
    created_at            DATETIME(6) NOT NULL,
    updated_at            DATETIME(6) NOT NULL,
    created_by            VARCHAR(36),
    updated_by            VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_sub_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_sub_plan FOREIGN KEY (plan_code) REFERENCES plans (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE super_admins (
    id            VARCHAR(36)     NOT NULL,
    name          VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME(6)  NOT NULL,
    updated_at    DATETIME(6)  NOT NULL,
    created_by    VARCHAR(36),
    updated_by    VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_super_admin_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE support_access_logs (
    id             VARCHAR(36)    NOT NULL,
    super_admin_id VARCHAR(36)    NOT NULL,
    tenant_id      VARCHAR(36)    NOT NULL,
    reason         VARCHAR(500),
    started_at     DATETIME(6),
    ended_at       DATETIME(6),
    created_at     DATETIME(6) NOT NULL,
    updated_at     DATETIME(6) NOT NULL,
    created_by     VARCHAR(36),
    updated_by     VARCHAR(36),
    PRIMARY KEY (id),
    CONSTRAINT fk_sal_admin FOREIGN KEY (super_admin_id) REFERENCES super_admins (id),
    CONSTRAINT fk_sal_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
