-- =============================================================
-- V20: Financial Core (G2a, W6) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G2a
-- 3 tables: advances (tạm ứng), payments (thanh toán), invoices (hoá đơn)
-- Bảo lãnh (guarantees) hoãn đến G2b (V28, W21, optional cho khách đặc biệt)
-- =============================================================

-- -------------------------------------------------------------
-- Advances (Tạm ứng)
-- -------------------------------------------------------------
CREATE TABLE advances (
    advance_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT NOT NULL,
    amount       DECIMAL(18,2) NOT NULL DEFAULT 0,
    advance_date DATE,
    bank         VARCHAR(100),
    note         TEXT,
    seed_source  VARCHAR(50) NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_adv_order (order_id),
    INDEX idx_adv_date (advance_date),
    INDEX idx_adv_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Payments (Thanh toán)
-- -------------------------------------------------------------
CREATE TABLE payments (
    payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    amount         DECIMAL(18,2) NOT NULL DEFAULT 0,
    scheduled_date DATE,                                    -- Ngày thanh toán theo HĐ
    actual_date    DATE,                                    -- Ngày thanh toán thực tế
    bank           VARCHAR(100),
    status         ENUM('PENDING','PAID','CONFIRMED') NOT NULL DEFAULT 'PENDING',
    note           TEXT,
    seed_source    VARCHAR(50) NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_pay_order (order_id),
    INDEX idx_pay_status (status),
    INDEX idx_pay_scheduled (scheduled_date),
    INDEX idx_pay_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- Invoices (Hoá đơn - nhập tay, không cần API VNPT/Viettel)
-- -------------------------------------------------------------
CREATE TABLE invoices (
    invoice_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    status         ENUM('NOT_ISSUED','ISSUED') NOT NULL DEFAULT 'NOT_ISSUED',
    issued_date    DATE,
    invoice_number VARCHAR(100),
    note           TEXT,
    seed_source    VARCHAR(50) NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_inv_order (order_id),
    INDEX idx_inv_status (status),
    INDEX idx_inv_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
