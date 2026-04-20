-- =============================================================
-- V23: Tailor Module (G5, W13-14) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G5
--
-- Excel có 2 sheet Production: "Đơn hàng" (tổng hợp) + "Thợ" (detail).
-- Model: tailors (master) + tailor_assignments (per order_item × tailor).
-- Role PRODUCTION (đã có) đảm nhiệm mutations.
-- =============================================================

-- Tailor master
CREATE TABLE tailors (
    tailor_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    type         ENUM('CUT_SEW','FINISHING','TAILOR_FULL') NULL,
    phone        VARCHAR(20),
    location     VARCHAR(255),
    active       BOOLEAN NOT NULL DEFAULT TRUE,
    note         TEXT,
    seed_source  VARCHAR(50) NULL,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME NULL,
    INDEX idx_tailor_active (active),
    INDEX idx_tailor_type (type),
    INDEX idx_tailor_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assignments: 1 order_item có thể có N tailor_assignments (nhiều thợ cùng làm)
CREATE TABLE tailor_assignments (
    assignment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id      BIGINT NOT NULL,
    tailor_id          BIGINT NOT NULL,
    qty_assigned       INT NOT NULL DEFAULT 0,
    qty_from_stock     INT NOT NULL DEFAULT 0,            -- Slg bốc tồn kho
    qty_returned       INT NOT NULL DEFAULT 0,
    appointment_date   DATE,
    returned_date      DATE,
    tailor_type        ENUM('CUT_SEW','FINISHING') NULL,  -- có thể khác với tailor.type (1 thợ làm nhiều role)
    npl_proposal_url   VARCHAR(500),
    status             ENUM('PLANNED','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PLANNED',
    note               TEXT,
    seed_source        VARCHAR(50) NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id) ON DELETE CASCADE,
    FOREIGN KEY (tailor_id)     REFERENCES tailors(tailor_id),
    INDEX idx_ta_order_item (order_item_id),
    INDEX idx_ta_tailor (tailor_id),
    INDEX idx_ta_status (status),
    INDEX idx_ta_appointment (appointment_date),
    INDEX idx_ta_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Order-level NPL proposal URL (bản đề xuất phụ liệu cho cả đơn)
ALTER TABLE orders ADD COLUMN npl_proposal_url VARCHAR(500);
