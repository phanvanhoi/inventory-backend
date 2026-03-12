-- =====================================================
-- V5: Fabric Module — unit_employees + fabric fields
-- =====================================================

-- 1. Bảng unit_employees (nhân viên đơn vị — dùng cho xuất vải mẫu 2)
CREATE TABLE unit_employees (
    employee_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id       BIGINT NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    position_id   BIGINT NULL,
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (position_id) REFERENCES positions(position_id),
    INDEX idx_employee_unit (unit_id),
    INDEX idx_employee_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. inventory_requests: thêm request_status (track từng kho hoàn thành — mẫu 3)
ALTER TABLE inventory_requests
    ADD COLUMN request_status VARCHAR(20) DEFAULT 'PENDING';

-- 3. inventory_request_items: quantity INT → DECIMAL + thêm fabric fields
ALTER TABLE inventory_request_items
    MODIFY COLUMN quantity DECIMAL(10,2) NOT NULL DEFAULT 0,
    ADD COLUMN worker_note VARCHAR(200) NULL,
    ADD COLUMN fabric_note VARCHAR(200) NULL,
    ADD COLUMN employee_id BIGINT NULL,
    ADD COLUMN garment_quantity VARCHAR(10) NULL,
    ADD CONSTRAINT fk_item_employee
        FOREIGN KEY (employee_id) REFERENCES unit_employees(employee_id);

-- 4. receipt_items: received_quantity INT → DECIMAL (hỗ trợ vải mét/kg)
ALTER TABLE receipt_items
    MODIFY COLUMN received_quantity DECIMAL(10,2) NOT NULL;
