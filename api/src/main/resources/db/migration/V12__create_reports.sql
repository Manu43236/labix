CREATE TABLE reports (
    id              VARCHAR(36)     NOT NULL,
    tenant_id       VARCHAR(36)     NOT NULL,
    visit_id        VARCHAR(36)     NOT NULL,
    version         INT          NOT NULL DEFAULT 1,
    status          VARCHAR(15)  NOT NULL DEFAULT 'DRAFT',
    snapshot_json   LONGTEXT,
    pdf_file_id     VARCHAR(36),
    verified_by     VARCHAR(36),
    verified_at     DATETIME(6),
    released_at     DATETIME(6),
    deleted_at      DATETIME(6),
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_report_visit (visit_id),
    INDEX idx_report_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE report_templates (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    name        VARCHAR(100) NOT NULL,
    config_json JSON,
    is_default  TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_template_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_logs (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36),
    user_id     VARCHAR(36),
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   VARCHAR(36)     NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    before_json LONGTEXT,
    after_json  LONGTEXT,
    at          DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
