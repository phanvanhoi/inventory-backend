-- =====================================================
-- V2: Partial Receipt (Nhập kho từng phần)
-- =====================================================

-- 1. Thêm trạng thái RECEIVING vào request_sets
ALTER TABLE request_sets MODIFY COLUMN status
    ENUM('PENDING', 'APPROVED', 'REJECTED', 'RECEIVING', 'EXECUTED') NOT NULL DEFAULT 'PENDING';

-- 2. Thêm action mới vào approval_history
ALTER TABLE approval_history MODIFY COLUMN action
    ENUM('SUBMIT', 'APPROVE', 'REJECT', 'EXECUTE', 'RECEIVE', 'COMPLETE', 'EDIT') NOT NULL;

-- 3. Bảng receipt_records (Mỗi lần nhận hàng = 1 record)
CREATE TABLE receipt_records (
    receipt_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT NOT NULL,
    received_by BIGINT NOT NULL,
    received_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    note TEXT,
    FOREIGN KEY (set_id) REFERENCES request_sets(set_id) ON DELETE CASCADE,
    FOREIGN KEY (received_by) REFERENCES users(user_id),
    INDEX idx_receipt_set (set_id),
    INDEX idx_receipt_received_by (received_by),
    INDEX idx_receipt_received_at (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng receipt_items (Chi tiết từng biến thể nhận trong mỗi lần)
CREATE TABLE receipt_items (
    receipt_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    received_quantity INT NOT NULL,
    FOREIGN KEY (receipt_id) REFERENCES receipt_records(receipt_id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES inventory_requests(request_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    INDEX idx_ri_receipt (receipt_id),
    INDEX idx_ri_request (request_id),
    INDEX idx_ri_variant (variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
