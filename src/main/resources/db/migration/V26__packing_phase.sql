-- =============================================================
-- V26: Packing Phase (G8, W19) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G8
--
-- 1 Order có thể có N packing_batches (đợt giao).
-- missing_items track hàng thiếu per batch.
-- Role PACKER cho mutations (đã seed ở V19).
-- =============================================================

-- Packing batches: per Order, nhiều đợt giao
CREATE TABLE packing_batches (
    packing_batch_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id                BIGINT NOT NULL,
    packer_user_id          BIGINT NULL,
    documents_received_date DATE,                            -- Ngày nhận hồ sơ
    packing_started_date    DATE,                            -- Ngày bắt đầu đóng
    packing_completed_date  DATE,                            -- Ngày đóng xong
    expected_delivery_date  DATE,                            -- Ngày dự kiến giao
    contract_delivery_date  DATE,                            -- Ngày giao theo HĐ
    actual_delivery_date    DATE,                            -- Ngày giao thực tế
    delivery_status         ENUM('NOT_DELIVERED','DELIVERED','PARTIAL')
                            NOT NULL DEFAULT 'NOT_DELIVERED',
    tick_file_url           VARCHAR(500),                    -- File tích hàng
    status                  ENUM('PREPARING','PACKED','SHIPPED','RETURNED')
                            NOT NULL DEFAULT 'PREPARING',
    note                    TEXT,
    seed_source             VARCHAR(50) NULL,
    created_at              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id)       REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (packer_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_pb_order (order_id),
    INDEX idx_pb_packer (packer_user_id),
    INDEX idx_pb_status (status),
    INDEX idx_pb_delivery (delivery_status),
    INDEX idx_pb_actual_delivery (actual_delivery_date),
    INDEX idx_pb_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Missing items: per batch (hàng thiếu khi đóng)
CREATE TABLE missing_items (
    missing_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_batch_id      BIGINT NOT NULL,
    order_item_id         BIGINT NOT NULL,
    missing_quantity      INT NOT NULL DEFAULT 0,
    missing_list_file_url VARCHAR(500),                      -- File DS nợ đồng phục
    resolved              BOOLEAN NOT NULL DEFAULT FALSE,
    note                  TEXT,
    seed_source           VARCHAR(50) NULL,
    created_at            DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (packing_batch_id) REFERENCES packing_batches(packing_batch_id) ON DELETE CASCADE,
    FOREIGN KEY (order_item_id)    REFERENCES order_items(order_item_id),
    INDEX idx_mi_batch (packing_batch_id),
    INDEX idx_mi_order_item (order_item_id),
    INDEX idx_mi_resolved (resolved),
    INDEX idx_mi_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
