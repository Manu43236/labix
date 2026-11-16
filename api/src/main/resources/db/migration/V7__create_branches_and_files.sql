CREATE TABLE branches (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    name        VARCHAR(100) NOT NULL,
    address     TEXT,
    phone       VARCHAR(15),
    is_main     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_branch_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_branches (
    id          VARCHAR(36) NOT NULL,
    user_id     VARCHAR(36) NOT NULL,
    branch_id   VARCHAR(36) NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_branch (user_id, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE files (
    id          VARCHAR(36)     NOT NULL,
    tenant_id   VARCHAR(36)     NOT NULL,
    storage_key VARCHAR(500) NOT NULL,
    mime_type   VARCHAR(100),
    size_bytes  BIGINT       NOT NULL DEFAULT 0,
    purpose     VARCHAR(30)  NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_file_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
