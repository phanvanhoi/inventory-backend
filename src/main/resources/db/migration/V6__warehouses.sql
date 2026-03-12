-- 1. Bảng warehouses (Kho)
CREATE TABLE warehouses (
    warehouse_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(255) NOT NULL UNIQUE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_warehouse_name (warehouse_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Dữ liệu mặc định
INSERT INTO warehouses (warehouse_name, is_default) VALUES
('Kho Chính', TRUE),
('Kho Trường', FALSE);

-- 3. Thêm warehouse_id vào inventory_requests (default = 1 = Kho Chính)
ALTER TABLE inventory_requests
    ADD COLUMN warehouse_id BIGINT NOT NULL DEFAULT 1,
    ADD CONSTRAINT fk_request_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    ADD INDEX idx_request_warehouse (warehouse_id);
