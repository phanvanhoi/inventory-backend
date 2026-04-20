-- =============================================================
-- V28: Guarantees (G2b, W21) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G2b
--
-- Bảo lãnh 3 loại (dự thầu / thực hiện HĐ / bảo hành).
-- OPTIONAL per order — chỉ khách đặc biệt mới có (quyết định #4, 2026-04-18).
-- Không required, không nhúng vào Order — tách bảng riêng.
-- =============================================================

CREATE TABLE guarantees (
    guarantee_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    type           ENUM('BIDDING','PERFORMANCE','WARRANTY') NOT NULL,
    form           ENUM('NONE','BANK','CASH') NOT NULL DEFAULT 'NONE',
    amount         DECIMAL(18,2) DEFAULT 0,
    expiry_date    DATE,
    bank           VARCHAR(100),
    note           TEXT,
    seed_source    VARCHAR(50) NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    INDEX idx_g_order (order_id),
    INDEX idx_g_type (type),
    INDEX idx_g_expiry (expiry_date),
    INDEX idx_g_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
