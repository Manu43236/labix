CREATE TABLE bills (
    id              VARCHAR(36) NOT NULL,
    tenant_id       VARCHAR(36) NOT NULL,
    visit_id        VARCHAR(36) NOT NULL,
    bill_no         INT      NOT NULL,
    subtotal_paise  INT      NOT NULL DEFAULT 0,
    discount_paise  INT      NOT NULL DEFAULT 0,
    tax_paise       INT      NOT NULL DEFAULT 0,
    total_paise     INT      NOT NULL DEFAULT 0,
    paid_paise      INT      NOT NULL DEFAULT 0,
    status          VARCHAR(10) NOT NULL DEFAULT 'DUE',
    created_at      DATETIME(6),
    updated_at      DATETIME(6),
    created_by      VARCHAR(36),
    updated_by      VARCHAR(36),
    PRIMARY KEY (id),
    UNIQUE KEY uk_bill_no (tenant_id, bill_no),
    UNIQUE KEY uk_bill_visit (visit_id),
    INDEX idx_bill_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bill_items (
    id          VARCHAR(36)     NOT NULL,
    bill_id     VARCHAR(36)     NOT NULL,
    description VARCHAR(200) NOT NULL,
    amount_paise INT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_bill_item_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
    id          VARCHAR(36)    NOT NULL,
    bill_id     VARCHAR(36)    NOT NULL,
    amount_paise INT        NOT NULL,
    mode        VARCHAR(10) NOT NULL DEFAULT 'CASH',
    paid_at     DATETIME(6) NOT NULL,
    reference   VARCHAR(100),
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    PRIMARY KEY (id),
    INDEX idx_payment_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
