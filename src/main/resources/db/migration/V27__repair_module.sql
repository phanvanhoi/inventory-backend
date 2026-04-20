-- =============================================================
-- V27: Repair Module (G9, W20) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G9
--
-- Hàng quay đầu: khách trả lại do lỗi, cần sửa chữa.
-- Auto orders.has_repair=TRUE khi có repair active (status != SHIPPED_BACK).
-- Role REPAIRER (đã seed ở V19).
-- =============================================================

CREATE TABLE repair_requests (
    repair_id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_batch_id           BIGINT NULL,
    order_item_id              BIGINT NOT NULL,
    batch_number               VARCHAR(50),                     -- Đợt 1, Đợt 2...
    received_date              DATE,
    receiver_user_id           BIGINT NULL,
    receive_method             ENUM('POSTAL','DIRECT','COURIER') NULL,
    expected_completion_date   DATE,
    qty_repair                 INT NOT NULL DEFAULT 0,
    repair_details             TEXT,                            -- Nội dung xử lý
    return_date                DATE,
    return_method              ENUM('POSTAL','DIRECT','COURIER') NULL,
    return_handler_user_id     BIGINT NULL,
    parent_batches             TEXT,                            -- "Các mục mẹ" - link batch parent
    reason_for_return          TEXT,
    status                     ENUM('RECEIVED','REPAIRING','COMPLETED','SHIPPED_BACK')
                               NOT NULL DEFAULT 'RECEIVED',
    note                       TEXT,
    seed_source                VARCHAR(50) NULL,
    created_at                 DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (packing_batch_id)       REFERENCES packing_batches(packing_batch_id) ON DELETE SET NULL,
    FOREIGN KEY (order_item_id)          REFERENCES order_items(order_item_id),
    FOREIGN KEY (receiver_user_id)       REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (return_handler_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_rr_packing_batch (packing_batch_id),
    INDEX idx_rr_order_item (order_item_id),
    INDEX idx_rr_receiver (receiver_user_id),
    INDEX idx_rr_status (status),
    INDEX idx_rr_received_date (received_date),
    INDEX idx_rr_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
