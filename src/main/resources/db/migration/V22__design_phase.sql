-- =============================================================
-- V22: Design Phase (G4, W11-12) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G4
--
-- KHÔNG thay đổi state machine enum (B4 decision).
-- Dùng flags sẵn có trong V19: skip_design (default TRUE), design_ready.
-- Khi tất cả design_samples của order có status='APPROVED' → set design_ready=TRUE.
-- =============================================================

-- Design samples: per OrderItem, nhiều mẫu cho 1 mặt hàng (qua các lần duyệt)
CREATE TABLE design_samples (
    design_sample_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id      BIGINT NOT NULL,
    sample_image_url   VARCHAR(500),
    fabric_code        VARCHAR(100),
    designer_user_id   BIGINT NULL,
    status             ENUM('DRAFT','PREPARING','APPROVED','REJECTED')
                       NOT NULL DEFAULT 'DRAFT',
    note               TEXT,
    seed_source        VARCHAR(50) NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id) ON DELETE CASCADE,
    FOREIGN KEY (designer_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_ds_order_item (order_item_id),
    INDEX idx_ds_status (status),
    INDEX idx_ds_designer (designer_user_id),
    INDEX idx_ds_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Design documents: per Order (tài liệu cấp đơn hàng, không theo mặt hàng)
CREATE TABLE design_documents (
    design_doc_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id             BIGINT NOT NULL,
    file_url             VARCHAR(500) NOT NULL,
    file_name            VARCHAR(255),
    uploaded_by_user_id  BIGINT NULL,
    uploaded_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note                 TEXT,
    seed_source          VARCHAR(50) NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_dd_order (order_id),
    INDEX idx_dd_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
