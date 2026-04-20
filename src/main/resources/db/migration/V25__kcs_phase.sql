-- =============================================================
-- V25: KCS (Quality Check) Phase (G7, W16-17) — Lark Integration
-- Ref: docs/lark-integration-roadmap.md §G7
--
-- KHÔNG thay đổi state machine enum (B4 decision).
-- Dùng flags sẵn có trong V19: skip_kcs (default TRUE), qc_passed.
-- Auto: qc_passed = TRUE khi tất cả order_items có ít nhất 1 QC PASSED.
-- =============================================================

CREATE TABLE quality_checks (
    qc_id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id          BIGINT NOT NULL,
    tailor_assignment_id   BIGINT NULL,               -- optional: QC batch nào của thợ
    kcs_user_id            BIGINT NULL,
    received_date          DATE,                       -- Ngày nhận hàng KCS
    completed_date         DATE,                       -- Ngày KCS xong
    full_documents_received BOOLEAN NOT NULL DEFAULT FALSE,
    full_variants_received BOOLEAN NOT NULL DEFAULT FALSE,
    status                 ENUM('PENDING','IN_PROGRESS','PASSED','FAILED','RETURNED')
                           NOT NULL DEFAULT 'PENDING',
    notes                  TEXT,
    seed_source            VARCHAR(50) NULL,
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_item_id)        REFERENCES order_items(order_item_id) ON DELETE CASCADE,
    FOREIGN KEY (tailor_assignment_id) REFERENCES tailor_assignments(assignment_id) ON DELETE SET NULL,
    FOREIGN KEY (kcs_user_id)          REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_qc_order_item (order_item_id),
    INDEX idx_qc_tailor_assignment (tailor_assignment_id),
    INDEX idx_qc_kcs_user (kcs_user_id),
    INDEX idx_qc_status (status),
    INDEX idx_qc_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
