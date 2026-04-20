-- =====================================================
-- HANGFASHION INVENTORY MANAGEMENT SYSTEM
-- Complete Database Script
-- Version: 1.0
-- Description: Schema + Tables + Data Import
-- =====================================================

-- =====================================================
-- PHẦN 1: KHỞI TẠO DATABASE
-- =====================================================
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

DROP DATABASE IF EXISTS hangfashion_inventory;
CREATE DATABASE hangfashion_inventory CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hangfashion_inventory;

-- =====================================================
-- PHẦN 2: TẠO BẢNG (TABLES)

-- =====================================================

-- 2.1 Bảng styles (Kiểu dáng áo)
CREATE TABLE styles (
    style_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    style_name VARCHAR(50) NOT NULL UNIQUE,
    INDEX idx_style_name (style_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.2 Bảng sizes (Kích cỡ - hỗ trợ cả số '35' và chữ 'XS')
CREATE TABLE sizes (
    size_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    size_value VARCHAR(10) NOT NULL UNIQUE,
    size_order INT NOT NULL DEFAULT 0,
    INDEX idx_size_value (size_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3 Bảng products (Sản phẩm) — phải tạo trước product_variants vì FK
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    variant_type ENUM('STRUCTURED', 'ITEM_BASED') NOT NULL DEFAULT 'STRUCTURED',
    parent_product_id BIGINT NULL,
    note TEXT,
    min_stock INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    INDEX idx_product_name (product_name),
    INDEX idx_parent_product (parent_product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Self-referencing FK for parent-child products
ALTER TABLE products ADD CONSTRAINT fk_product_parent
    FOREIGN KEY (parent_product_id) REFERENCES products(product_id);

-- 2.4 Bảng length_types (Loại độ dài: Cộc/Dài)
CREATE TABLE length_types (
    length_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    INDEX idx_length_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.4 Bảng product_variants (Biến thể sản phẩm)
-- STRUCTURED: product_id + style/size/length/gender (nullable dimensions)
-- ITEM_BASED: product_id + item_code/item_name/unit
CREATE TABLE product_variants (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    style_id BIGINT NULL,
    size_id BIGINT NULL,
    length_type_id BIGINT NULL,
    gender ENUM('NAM', 'NU') NULL,
    item_code VARCHAR(50) NULL,
    item_name VARCHAR(255) NULL,
    unit VARCHAR(50) NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (style_id) REFERENCES styles(style_id),
    FOREIGN KEY (size_id) REFERENCES sizes(size_id),
    FOREIGN KEY (length_type_id) REFERENCES length_types(length_type_id),
    UNIQUE KEY uk_variant_structured (product_id, style_id, size_id, length_type_id, gender),
    UNIQUE KEY uk_variant_item (product_id, item_code),
    INDEX idx_variant_product (product_id),
    INDEX idx_variant_style (style_id),
    INDEX idx_variant_size (size_id),
    INDEX idx_variant_length (length_type_id),
    INDEX idx_variant_gender (gender),
    INDEX idx_variant_item_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.5a Bảng warehouses (Kho)
CREATE TABLE warehouses (
    warehouse_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_name VARCHAR(255) NOT NULL UNIQUE,
    is_default BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_warehouse_name (warehouse_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.5b Bảng units (Đơn vị/Khách hàng)
CREATE TABLE units (
    unit_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_name VARCHAR(255) NOT NULL UNIQUE,
    INDEX idx_unit_name (unit_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.6 Bảng positions (Chức danh: GDV, VHX, ...)
CREATE TABLE positions (
    position_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    position_code VARCHAR(50) NOT NULL UNIQUE,
    position_name VARCHAR(100),
    INDEX idx_position_code (position_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.7 Bảng roles (Vai trò người dùng)
CREATE TABLE roles (
    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    INDEX idx_role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.7 Bảng users (Người dùng)
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    warehouse_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL,
    INDEX idx_username (username),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.8 Bảng user_roles (Liên kết user với role - many-to-many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.9 Bảng request_sets (Bộ phiếu - gộp các requests có cùng tên)
-- Status: PENDING (chờ duyệt), APPROVED (đã duyệt), REJECTED (từ chối), EXECUTED (đã thực hiện)
-- Luồng: PENDING → APPROVED → EXECUTED
--        PENDING → REJECTED
-- Lưu ý: Đã bỏ DRAFT - khi tạo mới sẽ tự động là PENDING
CREATE TABLE request_sets (
    set_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_name VARCHAR(255) NOT NULL,
    description TEXT,
    category ENUM('VAI_NHAP_KHO', 'VAI_NHAP_KHO_THO', 'VAI_GIAO_THO', 'VAI_TRA_KHACH', 'PHU_LIEU', 'PHU_LIEU_KHO_THO', 'PHU_KIEN', 'HANG_MAY_SAN') NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'RECEIVING', 'EXECUTED') NOT NULL DEFAULT 'PENDING',
    executed_by BIGINT NULL,
    executed_at DATETIME NULL,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    submitted_at DATETIME NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (executed_by) REFERENCES users(user_id),
    INDEX idx_set_name (set_name),
    INDEX idx_set_created_by (created_by),
    INDEX idx_set_status (status),
    INDEX idx_set_category (category),
    INDEX idx_set_executed_by (executed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.10 Bảng approval_history (Lịch sử duyệt/từ chối)
CREATE TABLE approval_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT NOT NULL,
    action ENUM('SUBMIT', 'APPROVE', 'REJECT', 'EXECUTE', 'RECEIVE', 'COMPLETE', 'EDIT', 'EDIT_AND_RECEIVE') NOT NULL,
    performed_by BIGINT NOT NULL,
    reason TEXT,
    metadata TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (set_id) REFERENCES request_sets(set_id) ON DELETE CASCADE,
    FOREIGN KEY (performed_by) REFERENCES users(user_id),
    INDEX idx_history_set (set_id),
    INDEX idx_history_action (action),
    INDEX idx_history_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.11 Bảng notifications (Thông báo)
CREATE TABLE notifications (
    notification_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    is_urgent BOOLEAN NOT NULL DEFAULT FALSE,
    related_set_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_set_id) REFERENCES request_sets(set_id) ON DELETE SET NULL,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.12 Device tokens (push notifications for mobile admin app)
CREATE TABLE device_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    push_token VARCHAR(255) NOT NULL,
    platform VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_push_token (push_token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.13 (products đã tạo ở trên trước product_variants)

-- 2.14 Bảng inventory_requests (Phiếu xuất/nhập kho)
-- expected_date: Ngày dự kiến (bắt buộc cho ADJUST_IN, ADJUST_OUT)
-- position_id: Chức danh (GDV, VHX, ...) - optional
CREATE TABLE inventory_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT,
    unit_id BIGINT,
    position_id BIGINT NULL,
    product_id BIGINT,
    request_type ENUM('IN', 'OUT', 'ADJUST_IN', 'ADJUST_OUT') NOT NULL,
    expected_date DATE NULL,
    request_status VARCHAR(20) DEFAULT 'PENDING',
    warehouse_id BIGINT NOT NULL DEFAULT 1,
    note TEXT,
    fabric_metadata TEXT NULL COMMENT 'JSON state cho fabric templates (norms, workers, warehouses...)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (set_id) REFERENCES request_sets(set_id),
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (position_id) REFERENCES positions(position_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(warehouse_id),
    INDEX idx_request_set (set_id),
    INDEX idx_request_unit (unit_id),
    INDEX idx_request_position (position_id),
    INDEX idx_request_product (product_id),
    INDEX idx_request_warehouse (warehouse_id),
    INDEX idx_request_type (request_type),
    INDEX idx_request_expected_date (expected_date),
    INDEX idx_request_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.14a Bảng unit_employees (Nhân viên đơn vị — dùng cho xuất vải Mẫu 2)
CREATE TABLE unit_employees (
    employee_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    position_id BIGINT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (position_id) REFERENCES positions(position_id),
    INDEX idx_employee_unit (unit_id),
    INDEX idx_employee_name (full_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.14b Bảng inventory_request_items (Chi tiết từng item trong phiếu)
CREATE TABLE inventory_request_items (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity DECIMAL(10,2) NOT NULL DEFAULT 0,
    worker_note VARCHAR(200) NULL,
    fabric_note VARCHAR(200) NULL,
    employee_id BIGINT NULL,
    garment_quantity VARCHAR(10) NULL,
    rate DECIMAL(10,4) NULL,
    FOREIGN KEY (request_id) REFERENCES inventory_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    FOREIGN KEY (employee_id) REFERENCES unit_employees(employee_id),
    INDEX idx_item_request (request_id),
    INDEX idx_item_variant (variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.15 Bảng receipt_records (Mỗi lần nhận hàng từng phần = 1 record)
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

-- 2.16 Bảng receipt_items (Chi tiết từng biến thể nhận trong mỗi lần)
CREATE TABLE receipt_items (
    receipt_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    request_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    received_quantity DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (receipt_id) REFERENCES receipt_records(receipt_id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES inventory_requests(request_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    INDEX idx_ri_receipt (receipt_id),
    INDEX idx_ri_request (request_id),
    INDEX idx_ri_variant (variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- LARK INTEGRATION (V19, 2026-04-19): Order + Customer + OrderItem + OrderHistory
-- Thay thế cho contract_reports + contract_report_history cũ.
-- Ref: docs/lark-integration-roadmap.md §G1
-- =====================================================

-- 2.17 Bảng customers (Khách hàng - mở rộng từ Unit với MST, người ký HĐ)
CREATE TABLE customers (
    customer_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id             BIGINT NOT NULL,
    parent_customer_id  BIGINT NULL,                  -- Parent/child (VT Bắc Ninh → TT VT Tiên Du...)
    tax_code            VARCHAR(20),
    signer_name         VARCHAR(255),
    customer_type       ENUM('TRADITIONAL','NEW') NOT NULL DEFAULT 'NEW',
    province            VARCHAR(100),
    contract_year       INT,
    note                TEXT,
    seed_source         VARCHAR(50) NULL,             -- LARK_TEST | MIGRATED_FROM_CR | NULL
    lark_legacy_id      VARCHAR(50) NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at          DATETIME NULL,
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (parent_customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL,
    INDEX idx_cust_unit (unit_id),
    INDEX idx_cust_year (contract_year),
    INDEX idx_cust_seed (seed_source),
    INDEX idx_cust_parent (parent_customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.18 Bảng orders (Đơn hàng - entity root, thay thế contract_reports)
CREATE TABLE orders (
    order_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_code             VARCHAR(100) UNIQUE,
    customer_id            BIGINT NOT NULL,
    status ENUM('NEW','NEGOTIATION','CONTRACT_SIGNED','DESIGNING',
                'MEASURING','PRODUCING','QC','PACKING',
                'DELIVERED','SUCCESS','LIQUIDATED','CANCELLED')
           NOT NULL DEFAULT 'NEW',
    current_phase ENUM('SALES_INPUT','MEASUREMENT_INPUT','PRODUCTION_INPUT',
                       'STOCKKEEPER_INPUT','COMPLETED')
           NOT NULL DEFAULT 'SALES_INPUT',
    sales_person_user_id   BIGINT NULL,
    sales_person_name      VARCHAR(100) NULL,
    unit_type              VARCHAR(30),
    contract_year          INT,
    total_before_vat       DECIMAL(18,2) DEFAULT 0,
    vat_amount             DECIMAL(18,2) DEFAULT 0,
    total_after_vat        DECIMAL(18,2) DEFAULT 0,
    -- Phase: SALES
    expected_delivery_date       DATE,
    finalized_list_sent_date     DATE,
    finalized_list_received_date DATE,
    delivery_method              VARCHAR(50),
    extra_payment_date           DATE,
    extra_payment_amount         DECIMAL(15,0) DEFAULT 0,
    -- Phase: MEASUREMENT
    measurement_start              DATE,
    measurement_end                DATE,
    technician_name                VARCHAR(100),
    measurement_received_date      DATE,
    measurement_handler            VARCHAR(100),
    skip_measurement               BOOLEAN DEFAULT FALSE,
    production_handover_date       DATE,
    -- Phase: PRODUCTION
    tailor_start_date         DATE,
    tailor_expected_return    DATE,
    tailor_actual_return      DATE,
    packing_return_date       DATE,
    -- Phase: STOCKKEEPER
    actual_shipping_date      DATE,
    -- Flags (G4, G7+)
    skip_design               BOOLEAN DEFAULT TRUE,
    design_ready              BOOLEAN DEFAULT FALSE,
    skip_kcs                  BOOLEAN DEFAULT TRUE,
    qc_passed                 BOOLEAN DEFAULT FALSE,
    has_repair                BOOLEAN DEFAULT FALSE,
    cancelled                 BOOLEAN DEFAULT FALSE,
    note                      TEXT,
    -- Migration tracking
    legacy_report_id  BIGINT NULL,
    seed_source       VARCHAR(50) NULL,
    lark_legacy_id    VARCHAR(50) NULL,
    -- Metadata
    created_by   BIGINT,
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at   DATETIME NULL,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (sales_person_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_order_customer (customer_id),
    INDEX idx_order_status (status),
    INDEX idx_order_phase (current_phase),
    INDEX idx_order_legacy (legacy_report_id),
    INDEX idx_order_seed (seed_source),
    INDEX idx_order_sales_person (sales_person_user_id),
    UNIQUE KEY uk_legacy_report (legacy_report_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.19 Bảng order_items (Mặt hàng trong đơn)
CREATE TABLE order_items (
    order_item_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id           BIGINT NOT NULL,
    product_id         BIGINT NULL,
    product_name       VARCHAR(255),
    qty_contract       INT NOT NULL DEFAULT 0,
    qty_settlement     INT NULL,
    unit_price         DECIMAL(18,2) DEFAULT 0,
    amount_contract    DECIMAL(18,2) GENERATED ALWAYS AS (qty_contract * unit_price) STORED,
    amount_settlement  DECIMAL(18,2) GENERATED ALWAYS AS (COALESCE(qty_settlement, 0) * unit_price) STORED,
    note               TEXT,
    seed_source        VARCHAR(50) NULL,
    lark_legacy_id     VARCHAR(50) NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted_at         DATETIME NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE SET NULL,
    INDEX idx_oi_order (order_id),
    INDEX idx_oi_product (product_id),
    INDEX idx_oi_seed (seed_source)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.20 Bảng order_history (Audit trail, thay thế contract_report_history)
CREATE TABLE order_history (
    history_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    changed_by  BIGINT NOT NULL,
    changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action      VARCHAR(30) NOT NULL,           -- EDIT | ADVANCE | RETURN | STATUS_CHANGE
    field_name  VARCHAR(100),
    old_value   TEXT,
    new_value   TEXT,
    reason      TEXT,
    FOREIGN KEY (order_id)   REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id),
    INDEX idx_oh_order (order_id),
    INDEX idx_oh_changed_by (changed_by),
    INDEX idx_oh_action (action),
    INDEX idx_oh_changed_at (changed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.21 Bảng refresh_tokens (Refresh token cho gia hạn session)
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(36) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_refresh_token (token),
    INDEX idx_refresh_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.22 Bảng accessory_templates (BOM template phụ liệu)
CREATE TABLE accessory_templates (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_by BIGINT       NULL,
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    deleted_at DATETIME     NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.23 Bảng accessory_template_items (Chi tiết từng mặt hàng trong template)
CREATE TABLE accessory_template_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT        NOT NULL,
    variant_id  BIGINT        NULL,
    item_code   VARCHAR(50)   NULL,
    item_name   VARCHAR(255)  NOT NULL,
    rate        DECIMAL(10,4) NOT NULL,
    unit        VARCHAR(50)   NULL,
    sort_order  INT           DEFAULT 0,
    FOREIGN KEY (template_id) REFERENCES accessory_templates(id),
    FOREIGN KEY (variant_id)  REFERENCES product_variants(variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- FINANCIAL (V20, G2a, W6): Advance + Payment + Invoice
-- Ref: docs/lark-integration-roadmap.md §G2a
-- Guarantees (G2b) hoãn đến V28 / W21
-- =====================================================

-- 2.24 Bảng advances (Tạm ứng)
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

-- 2.25 Bảng payments (Thanh toán)
CREATE TABLE payments (
    payment_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL,
    amount         DECIMAL(18,2) NOT NULL DEFAULT 0,
    scheduled_date DATE,
    actual_date    DATE,
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

-- 2.26 Bảng invoices (Hoá đơn)
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

-- =====================================================
-- PHẦN 3: MASTER DATA
-- =====================================================

-- 3.1 Roles (11 vai trò - thêm 4 role mới cho Lark integration G1+)
INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'Quản trị viên - duyệt/từ chối bộ phiếu, xem/sửa tất cả báo cáo HĐ'),
('USER', 'Người dùng thông thường - chỉ tạo được phiếu IN/OUT (ảnh hưởng tồn kho thực tế)'),
('STOCKKEEPER', 'Kiểm kho - thực hiện nhập/xuất kho, nhập ngày giao hàng báo cáo HĐ'),
('PURCHASER', 'Thu mua - tạo được cả 4 loại phiếu (ADJUST_IN/OUT ảnh hưởng dự kiến, IN/OUT ảnh hưởng thực tế)'),
('SALES', 'Kinh doanh - tạo và quản lý hợp đồng, nhập thông tin HĐ'),
('MEASUREMENT', 'Phụ trách số đo - nhập thông tin đo và bàn giao SX'),
('PRODUCTION', 'Quản lý kế hoạch SX - nhập thông tin sản xuất, thợ triển khai'),
('DESIGNER', 'Phòng thiết kế - duyệt hàng mẫu, mã vải, tài liệu thiết kế (G4)'),
('KCS', 'Kiểm tra chất lượng - kiểm tra thành phẩm trước đóng hàng (G7)'),
('PACKER', 'Đóng hàng - đóng gói, tích hàng, giao hàng (G8)'),
('REPAIRER', 'Sửa chữa - xử lý hàng quay đầu (G9)');

-- 3.2 Styles (4 kiểu dáng)
INSERT INTO styles (style_name) VALUES
('CỔ ĐIỂN'),
('CỔ ĐIỂN NGẮN'),
('SLIM'),
('SLIM Ngắn');

-- 3.3 Sizes (số: 35-45 cho sơ mi, chữ: XS-6XL cho áo khoác/phông/len...)
INSERT INTO sizes (size_value, size_order) VALUES
('35', 35), ('36', 36), ('37', 37), ('38', 38), ('39', 39),
('40', 40), ('41', 41), ('42', 42), ('43', 43), ('44', 44), ('45', 45),
('XS', 1), ('S', 2), ('M', 3), ('L', 4), ('XL', 5),
('2XL', 6), ('3XL', 7), ('4XL', 8), ('5XL', 9), ('6XL', 10);

-- 3.4 Length Types (2 loại độ dài)
-- Mapping: Cộc = COC (length_type_id = 1), Dài = DAI (length_type_id = 2)
INSERT INTO length_types (code) VALUES
('COC'),
('DAI');

-- 3.5 Positions (Chức danh)
-- Dữ liệu từ ảnh: GDV, VHX
INSERT INTO positions (position_code, position_name) VALUES
('GDV', 'Giao dịch viên'),
('VHX', 'Vận hành xưởng');

-- 3.6 Products (10 sản phẩm)
INSERT INTO products (product_name, variant_type, note, created_at) VALUES
('HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU)', 'STRUCTURED', 'Sơ mi nam 2025 - SM1', '2025-06-20 00:00:00'),
('SƠ MI NAM 2026', 'STRUCTURED', 'Style + Size(35-45) + Length(Cộc/Dài)', '2026-01-01 00:00:00'),
('ÁO KHOÁC 2026', 'STRUCTURED', 'Size(XS-6XL) + Gender(NAM/NỮ)', '2026-01-01 00:00:00'),
('ÁO PHÔNG 2026', 'STRUCTURED', 'Size(XS-6XL) + Gender(NAM/NỮ) + Length(Cộc/Dài)', '2026-01-01 00:00:00'),
('ÁO LEN + GILE LEN 2026', 'STRUCTURED', 'Size(XS-6XL) + Gender(NAM/NỮ)', '2026-01-01 00:00:00'),
('GILE BẢO HỘ 2026', 'STRUCTURED', 'Size(XS-6XL) + Gender(NAM/NỮ)', '2026-01-01 00:00:00'),
('BẢO HỘ LAO ĐỘNG CÓ SIZE 2026', 'STRUCTURED', 'Parent: Giày BH + Áo mưa', '2026-01-01 00:00:00'),
('NHẬP XUẤT VẢI 2026', 'ITEM_BASED', '31 mã vải', '2026-01-01 00:00:00'),
('PHỤ KIỆN 2026', 'ITEM_BASED', '49 mã phụ kiện', '2026-01-01 00:00:00'),
('PHỤ LIỆU 2026', 'ITEM_BASED', '~250 mã phụ liệu', '2026-01-01 00:00:00');

-- SP1 là child của SP2 (SƠ MI NAM 2026)
UPDATE products SET parent_product_id = 2 WHERE product_id = 1;

-- Child products của SP3 (ÁO KHOÁC 2026)
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('Áo khoác Bưu điện - ghi phối vàng', 'STRUCTURED', 3, 'Áo khoác 2026 - Bưu điện', NOW());
-- product_id = 12
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('Áo phông Bưu điện (vàng phối ghi) - Bưu tá', 'STRUCTURED', 4, 'Áo phông 2026 - Bưu điện Bưu tá', NOW());
-- product_id = 13
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('Áo Gile len VNPT', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - VNPT', NOW());
-- product_id = 14
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('Áo Gile bảo hộ Bưu điện - Kaky vàng', 'STRUCTURED', 6, 'Gile BH 2026 - Bưu điện', NOW());
-- product_id = 15
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('Giày BH', 'STRUCTURED', 7, 'Giày bảo hộ size 38-45', NOW());
-- product_id = 16
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('Bộ áo mưa', 'STRUCTURED', 7, 'Áo mưa size S-4XL', NOW());
-- product_id = 17
INSERT INTO products (product_name, variant_type, note, created_at) VALUES
('BẢO HỘ LAO ĐỘNG 2026', 'ITEM_BASED', 'Mũ, túi, balo...', NOW());

-- 3.7 Product Variants
-- ====== Product 1: SƠ MI NAM 2025 (88 biến thể = 4 styles x 11 sizes x 2 lengths) ======
-- size_id: 1=35, 2=36, 3=37, 4=38, 5=39, 6=40, 7=41, 8=42, 9=43, 10=44, 11=45
-- length_type_id: 1=COC, 2=DAI
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id) VALUES
-- CỔ ĐIỂN (style_id = 1)
(1, 1, 1, 1), (1, 1, 1, 2), (1, 1, 2, 1), (1, 1, 2, 2), (1, 1, 3, 1), (1, 1, 3, 2),
(1, 1, 4, 1), (1, 1, 4, 2), (1, 1, 5, 1), (1, 1, 5, 2), (1, 1, 6, 1), (1, 1, 6, 2),
(1, 1, 7, 1), (1, 1, 7, 2), (1, 1, 8, 1), (1, 1, 8, 2), (1, 1, 9, 1), (1, 1, 9, 2),
(1, 1, 10, 1), (1, 1, 10, 2), (1, 1, 11, 1), (1, 1, 11, 2),
-- CỔ ĐIỂN NGẮN (style_id = 2)
(1, 2, 1, 1), (1, 2, 1, 2), (1, 2, 2, 1), (1, 2, 2, 2), (1, 2, 3, 1), (1, 2, 3, 2),
(1, 2, 4, 1), (1, 2, 4, 2), (1, 2, 5, 1), (1, 2, 5, 2), (1, 2, 6, 1), (1, 2, 6, 2),
(1, 2, 7, 1), (1, 2, 7, 2), (1, 2, 8, 1), (1, 2, 8, 2), (1, 2, 9, 1), (1, 2, 9, 2),
(1, 2, 10, 1), (1, 2, 10, 2), (1, 2, 11, 1), (1, 2, 11, 2),
-- SLIM (style_id = 3)
(1, 3, 1, 1), (1, 3, 1, 2), (1, 3, 2, 1), (1, 3, 2, 2), (1, 3, 3, 1), (1, 3, 3, 2),
(1, 3, 4, 1), (1, 3, 4, 2), (1, 3, 5, 1), (1, 3, 5, 2), (1, 3, 6, 1), (1, 3, 6, 2),
(1, 3, 7, 1), (1, 3, 7, 2), (1, 3, 8, 1), (1, 3, 8, 2), (1, 3, 9, 1), (1, 3, 9, 2),
(1, 3, 10, 1), (1, 3, 10, 2), (1, 3, 11, 1), (1, 3, 11, 2),
-- SLIM Ngắn (style_id = 4)
(1, 4, 1, 1), (1, 4, 1, 2), (1, 4, 2, 1), (1, 4, 2, 2), (1, 4, 3, 1), (1, 4, 3, 2),
(1, 4, 4, 1), (1, 4, 4, 2), (1, 4, 5, 1), (1, 4, 5, 2), (1, 4, 6, 1), (1, 4, 6, 2),
(1, 4, 7, 1), (1, 4, 7, 2), (1, 4, 8, 1), (1, 4, 8, 2), (1, 4, 9, 1), (1, 4, 9, 2),
(1, 4, 10, 1), (1, 4, 10, 2), (1, 4, 11, 1), (1, 4, 11, 2);

-- ====== Product 3: ÁO KHOÁC 2026 → Parent (variants thuộc child product_id=11) ======
-- ====== Product 11: Áo khoác Bưu điện - ghi phối vàng (child của SP3) ======
-- size_id: 12=XS, 13=S, 14=M, 15=L, 16=XL, 17=2XL, 18=3XL, 19=4XL, 20=5XL, 21=6XL
INSERT INTO product_variants (product_id, size_id, gender) VALUES
(11, 12, 'NAM'), (11, 13, 'NAM'), (11, 14, 'NAM'), (11, 15, 'NAM'), (11, 16, 'NAM'),
(11, 17, 'NAM'), (11, 18, 'NAM'), (11, 19, 'NAM'), (11, 20, 'NAM'), (11, 21, 'NAM'),
(11, 12, 'NU'), (11, 13, 'NU'), (11, 14, 'NU'), (11, 15, 'NU'), (11, 16, 'NU'),
(11, 17, 'NU'), (11, 18, 'NU'), (11, 19, 'NU'), (11, 20, 'NU'), (11, 21, 'NU');

-- ====== Product 4: ÁO PHÔNG 2026 → Parent (variants thuộc child product_id=12) ======
-- ====== Product 12: Áo phông Bưu điện (vàng phối ghi) - Bưu tá (child của SP4) ======
INSERT INTO product_variants (product_id, size_id, length_type_id, gender) VALUES
(12, 12, 1, 'NAM'), (12, 12, 2, 'NAM'), (12, 13, 1, 'NAM'), (12, 13, 2, 'NAM'),
(12, 14, 1, 'NAM'), (12, 14, 2, 'NAM'), (12, 15, 1, 'NAM'), (12, 15, 2, 'NAM'),
(12, 16, 1, 'NAM'), (12, 16, 2, 'NAM'), (12, 17, 1, 'NAM'), (12, 17, 2, 'NAM'),
(12, 18, 1, 'NAM'), (12, 18, 2, 'NAM'), (12, 19, 1, 'NAM'), (12, 19, 2, 'NAM'),
(12, 20, 1, 'NAM'), (12, 20, 2, 'NAM'), (12, 21, 1, 'NAM'), (12, 21, 2, 'NAM'),
(12, 12, 1, 'NU'), (12, 12, 2, 'NU'), (12, 13, 1, 'NU'), (12, 13, 2, 'NU'),
(12, 14, 1, 'NU'), (12, 14, 2, 'NU'), (12, 15, 1, 'NU'), (12, 15, 2, 'NU'),
(12, 16, 1, 'NU'), (12, 16, 2, 'NU'), (12, 17, 1, 'NU'), (12, 17, 2, 'NU'),
(12, 18, 1, 'NU'), (12, 18, 2, 'NU'), (12, 19, 1, 'NU'), (12, 19, 2, 'NU'),
(12, 20, 1, 'NU'), (12, 20, 2, 'NU'), (12, 21, 1, 'NU'), (12, 21, 2, 'NU');

-- ====== Product 5: ÁO LEN + GILE LEN 2026 → Parent (variants thuộc child product_id=13) ======
-- ====== Product 13: Áo Gile len VNPT (child của SP5) ======
INSERT INTO product_variants (product_id, size_id, gender) VALUES
(13, 12, 'NAM'), (13, 13, 'NAM'), (13, 14, 'NAM'), (13, 15, 'NAM'), (13, 16, 'NAM'),
(13, 17, 'NAM'), (13, 18, 'NAM'), (13, 19, 'NAM'), (13, 20, 'NAM'), (13, 21, 'NAM'),
(13, 12, 'NU'), (13, 13, 'NU'), (13, 14, 'NU'), (13, 15, 'NU'), (13, 16, 'NU'),
(13, 17, 'NU'), (13, 18, 'NU'), (13, 19, 'NU'), (13, 20, 'NU'), (13, 21, 'NU');

-- ====== Product 6: GILE BẢO HỘ 2026 → Parent (variants thuộc child product_id=14) ======
-- ====== Product 14: Áo Gile bảo hộ Bưu điện - Kaky vàng (child của SP6) ======
INSERT INTO product_variants (product_id, size_id, gender) VALUES
(14, 12, 'NAM'), (14, 13, 'NAM'), (14, 14, 'NAM'), (14, 15, 'NAM'), (14, 16, 'NAM'),
(14, 17, 'NAM'), (14, 18, 'NAM'), (14, 19, 'NAM'), (14, 20, 'NAM'), (14, 21, 'NAM'),
(14, 12, 'NU'), (14, 13, 'NU'), (14, 14, 'NU'), (14, 15, 'NU'), (14, 16, 'NU'),
(14, 17, 'NU'), (14, 18, 'NU'), (14, 19, 'NU'), (14, 20, 'NU'), (14, 21, 'NU');

-- ====== Product 7: BẢO HỘ LAO ĐỘNG CÓ SIZE 2026 → Parent ======
-- ====== Product 15: Giày BH (child của SP7) — STRUCTURED, size 38-45 (no gender) ======
-- size_id: 4=38, 5=39, 6=40, 7=41, 8=42, 9=43, 10=44, 11=45
INSERT INTO product_variants (product_id, size_id) VALUES
(15, 4), (15, 5), (15, 6), (15, 7), (15, 8), (15, 9), (15, 10), (15, 11);

-- ====== Product 16: Bộ áo mưa (child của SP7) — STRUCTURED, size S-4XL (no gender) ======
-- size_id: 13=S, 14=M, 15=L, 16=XL, 17=2XL, 18=3XL, 19=4XL
INSERT INTO product_variants (product_id, size_id) VALUES
(16, 13), (16, 14), (16, 15), (16, 16), (16, 17), (16, 18), (16, 19);

-- ====== Product 17: BẢO HỘ LAO ĐỘNG 2026 (ITEM_BASED — 3 items) ======
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(17, 'M1', 'Mũ BHLĐ', 'chiếc'),
(17, 'TUI1', 'Túi đựng dụng cụ', 'chiếc'),
(17, 'BL1', 'Balo VNPT', 'chiếc');

-- Product 8: NHẬP XUẤT VẢI 2026 (31 mã — ITEM_BASED)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(8, 'B1', 'Trắng kem nam', 'mét'),
(8, 'B2', 'Trắng kem nữ', 'mét'),
(8, 'B4', 'Trắng kem nam TCT BĐ (2020)', 'mét'),
(8, 'B7', 'Ghi nam', 'mét'),
(8, 'B9', 'Ghi nam LĐ tổng Vnpost', 'mét'),
(8, 'B11', 'Kaky ghi nam (2021 Grey)', 'mét'),
(8, 'B12', 'Ghi nữ', 'mét'),
(8, 'B13', 'Kaky ghi nữ', 'mét'),
(8, 'B15', 'Xanh biển nam', 'mét'),
(8, 'B16', 'Xanh biển nữ', 'mét'),
(8, 'B17', 'Xanh biển dài nam', 'mét'),
(8, 'B18', 'Xanh biển LĐ tổng Vnpost', 'mét'),
(8, 'B19', 'Cam nam', 'mét'),
(8, 'B20', 'Cam nữ', 'mét'),
(8, 'B21', 'Cam LĐ tổng Vnpost', 'mét'),
(8, 'B22', 'Ghi áo khoác', 'mét'),
(8, 'B23', 'Ghi áo len', 'mét'),
(8, 'B24', 'Vải HQ 01', 'mét'),
(8, 'B25', 'Vải HQ 02', 'mét'),
(8, 'B26', 'Lót trắng', 'mét'),
(8, 'B27', 'Lót ghi', 'mét'),
(8, 'B28', 'Lót xanh', 'mét'),
(8, 'B29', 'Lót cam', 'mét'),
(8, 'B30', 'Mex 4090', 'mét'),
(8, 'B31', 'Mex 3040', 'mét'),
(8, 'B32', 'Mex 6090', 'mét'),
(8, 'B33', 'Dựng cứng', 'mét'),
(8, 'B34', 'Dựng mềm', 'mét'),
(8, 'B35', 'Bông', 'kilogam'),
(8, 'B37', 'Vải TC 01', 'mét'),
(8, 'B39', 'Vải TC 02', 'mét');

-- Product 9: PHỤ KIỆN (47 mã — ITEM_BASED)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(9, 'PK1', 'Cavat văn phòng bưu điện', 'chiếc'),
(9, 'PK2', 'Cavat giao dịch bưu điện', 'chiếc'),
(9, 'PK3', 'Cavat giao dịch bưu điện (loại cũ)', 'chiếc'),
(9, 'PK4', 'Nơ kẻ vàng văn phòng bưu điện', 'chiếc'),
(9, 'PK5', 'Nơ sao văn phòng bưu điện', 'chiếc'),
(9, 'PK6', 'Nơ giao dịch viên bưu điện', 'chiếc'),
(9, 'PK8', 'Bộ vải áo dài bưu điện', 'bộ'),
(9, 'PK9', 'Cavat chấm VNPT', 'chiếc'),
(9, 'PK10', 'Nơ tím than dài văn phòng VNPT', 'chiếc'),
(9, 'PK11', 'Nơ giao dịch viên in logo VNPT', 'chiếc'),
(9, 'PK13', 'Khăn dài VNPT', 'chiếc'),
(9, 'PK14', 'Khăn vuông VNPT', 'chiếc'),
(9, 'PK15', 'Bộ vải áo dài VNPT loại thường', 'bộ'),
(9, 'PK16', 'Bộ vải áo dài VNPT loại xịn', 'bộ'),
(9, 'PK17', 'Cavat EMS', 'chiếc'),
(9, 'PK18', 'Nơ EMS', 'chiếc'),
(9, 'PK19', 'Cavat EMS (loại cũ)', 'chiếc'),
(9, 'PK20', 'Nơ EMS (loại cũ)', 'chiếc'),
(9, 'PK21', 'Cavat Bưu điện Trung Ương', 'chiếc'),
(9, 'PK22', 'Thắt lưng nam Bưu điện Trung Ương (CPT)', 'chiếc'),
(9, 'PK23', 'Khăn vuông lãnh đạo Bưu điện Trung Ương (CPT)', 'chiếc'),
(9, 'PK24', 'Khăn vuông nhân viên Bưu điện Trung Ương (CPT)', 'chiếc'),
(9, 'PK25', 'Thắt lưng nữ Bưu điện Trung Ương (CPT)', 'chiếc'),
(9, 'PK26', 'Cavat Habeco', 'chiếc'),
(9, 'PK27', 'Khăn Habeco', 'chiếc'),
(9, 'PK28', 'Cavat Oceanbank', 'chiếc'),
(9, 'PK29', 'Cavat Indovina (145cm)', 'chiếc'),
(9, 'PK30', 'Cavat Indovina (150cm)', 'chiếc'),
(9, 'PK31', 'Cavat Indovina (155cm)', 'chiếc'),
(9, 'PK32', 'Cavat Indovina (160cm)', 'chiếc'),
(9, 'PK33', 'Khăn Indovina', 'chiếc'),
(9, 'PK34', 'Cavat ngân hàng BIDV', 'chiếc'),
(9, 'PK35', 'Cavat Mobiphone toàn cầu', 'chiếc'),
(9, 'PK36', 'Cavat HUD', 'chiếc'),
(9, 'PK37', 'Cavat Vietin bank tím than', 'chiếc'),
(9, 'PK38', 'Cavat Vietin bank rêu', 'chiếc'),
(9, 'PK39', 'Khăn Vietin bank nền đỏ', 'chiếc'),
(9, 'PK40', 'Khăn Vietin bank nền trắng', 'chiếc'),
(9, 'PK41', 'Cavat ngân hàng nông nghiệp xanh', 'chiếc'),
(9, 'PK42', 'Cavat ngân hàng nông nghiệp đỏ', 'chiếc'),
(9, 'PK43', 'Nơ ngân hàng nông nghiệp xanh', 'chiếc'),
(9, 'PK44', 'Nơ ngân hàng nông nghiệp đỏ', 'chiếc'),
(9, 'PK45', 'Cavat đỏ EVN', 'chiếc'),
(9, 'PK46', 'Cavat Học viện bưu chính viễn thông', 'chiếc'),
(9, 'PK47', 'Khăn Học viện bưu chính viễn thông', 'chiếc'),
(9, 'PK48', 'Cavat Học viện bưu chính viễn thông 2025 - Xanh', 'chiếc'),
(9, 'PK49', 'Nơ Học viện bưu chính viễn thông (Mẫu mới 2025)', 'chiếc');

-- Product 10: PHỤ LIỆU (258 mã — ITEM_BASED)
-- Nhóm KHOA (77 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'KHOA1', 'Khóa quần tím than', 'chiếc'),
(10, 'KHOA2', 'Khóa quần tím than nhạt (EMS)', 'chiếc'),
(10, 'KHOA3', 'Khóa quần xanh tươi', 'chiếc'),
(10, 'KHOA4', 'Khóa quần ghi', 'chiếc'),
(10, 'KHOA5', 'Khóa quần đen', 'chiếc'),
(10, 'KHOA6', 'Khóa váy tím than', 'chiếc'),
(10, 'KHOA7', 'Khóa váy xanh tươi', 'chiếc'),
(10, 'KHOA8', 'Khóa váy ghi', 'chiếc'),
(10, 'KHOA9', 'Khóa váy đen', 'chiếc'),
(10, 'KHOA30', 'Khóa áo khoác bưu điện (vàng) - Khóa 18cm (Cước R3) (Khóa túi hông)', 'chiếc'),
(10, 'KHOA31', 'Khóa áo khoác bưu điện (vàng) - Khóa 15cm (Cước R3) (Khóa túi ngực)', 'chiếc'),
(10, 'KHOA32', 'Khóa áo chống nắng bưu điện - Khóa 70cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA33', 'Khóa áo chống nắng bưu điện - Khóa 80cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA34', 'Khóa áo chống nắng bưu điện - Khóa 18cm (Cước R3) (Khóa túi hông)', 'chiếc'),
(10, 'KHOA35', 'Khóa áo gile kaky vàng bảo hộ (ghi nhạt) - Khóa 75cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA36', 'Khóa áo gile lưới bảo hộ (ghi nhạt) - Khóa 50cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA37', 'Khóa áo khoác ngoài trời - Khóa 70cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA38', 'Khóa áo khoác ngoài trời - Khóa 75cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA39', 'Khóa áo khoác ngoài trời - Khóa 80cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA40', 'Khóa áo khoác ngoài trời - Khóa 85cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA47', 'Khóa áo khoác ngoài trời - Khóa 16cm (Cá sấu R5) (Khóa túi)', 'chiếc'),
(10, 'KHOA48', 'Khóa áo gile bảo hộ ngoài trời - Khóa 42cm (Cá sấu R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA49', 'Khóa áo gile bảo hộ ngoài trời - Khóa 16cm (Cá sấu R5) (Khóa túi)', 'chiếc'),
(10, 'KHOA50', 'Khóa áo khoác EMS - Khóa 70cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA51', 'Khóa áo khoác EMS - Khóa 75cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA52', 'Khóa áo khoác EMS - Khóa 80cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA53', 'Khóa áo khoác EMS - Khóa 85cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA54', 'Khóa áo khoác EMS - Khóa 70cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA55', 'Khóa áo khoác EMS - Khóa 75cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA56', 'Khóa áo khoác EMS - Khóa 80cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA57', 'Khóa áo khoác EMS - Khóa 18cm (Cước R3) (Khóa túi)', 'chiếc'),
(10, 'KHOA58', 'Khóa áo khoác IT - Khóa 70cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA59', 'Khóa áo khoác IT - Khóa 75cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA60', 'Khóa áo khoác IT - Khóa 80cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA61', 'Khóa áo khoác IT - Khóa 85cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA62', 'Khóa áo khoác IT - Khóa 70cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA63', 'Khóa áo khoác IT - Khóa 75cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA64', 'Khóa áo khoác IT - Khóa 40cm (Cước R3) (Khóa cổ)', 'chiếc'),
(10, 'KHOA65', 'Khóa áo khoác IT - Khóa 18cm (Cước R3) (Khóa túi)', 'chiếc'),
(10, 'KHOA66', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 69cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA67', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 71cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA68', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 73cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA69', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 79cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA70', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 62cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA71', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 64cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA72', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 68cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA73', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 72cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA74', 'Khóa áo gió màu tím than - Khóa 70cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA75', 'Khóa áo gió màu tím than - Khóa 75cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA76', 'Khóa áo gió màu tím than - Khóa 80cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA77', 'Khóa áo gió màu tím than - Khóa 85cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA78', 'Khóa áo gió màu tím than - Khóa 70cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA79', 'Khóa áo gió màu tím than - Khóa 75cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA80', 'Khóa áo gió màu tím than - Khóa 80cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA81', 'Khóa áo gió màu tím than - Khóa 18cm (Cước R3) (Khóa túi hông)', 'chiếc'),
(10, 'KHOA82', 'Khóa áo gió màu tím than - Khóa 40cm (Cước R3) (Khóa cổ)', 'chiếc'),
(10, 'KHOA83', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 67cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA84', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 75cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA85', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 77cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA86', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 60cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA87', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 66cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA88', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 70cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA89', 'Khóa áo chống nắng xanh VNPT - Khóa 70cm (Cước R5 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA90', 'Khóa áo chống nắng xanh VNPT - Khóa 75cm (Cước R5 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA91', 'Khóa áo chống nắng xanh VNPT - Khóa 80cm (Cước R5 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA92', 'Khóa áo chống nắng xanh VNPT - Khóa 85cm (Cước R5 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA93', 'Khóa áo chống nắng xanh VNPT - Khóa 70cm (Cước R3 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA94', 'Khóa áo chống nắng xanh VNPT - Khóa 75cm (Cước R3 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA95', 'Khóa áo chống nắng xanh VNPT - Khóa 80cm (Cước R3 ngược) (Khóa chính)', 'chiếc'),
(10, 'KHOA96', 'Khóa áo chống nắng xanh VNPT - Khóa 18cm (Cước R3) (Khóa túi nam)', 'chiếc'),
(10, 'KHOA97', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 81cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA98', 'Khóa áo khoác bưu điện (ghi đậm) - Khóa 85cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA99', 'Khóa áo chống nắng xanh VNPT - Khóa 25cm (Cước giọt lệ R3) (Khóa túi nữ)', 'chiếc'),
(10, 'KHOA100', 'Khóa áo khoác EMS - Khóa 90cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA101', 'Khóa áo khoác EMS - Khóa 85cm (Cước R3) (Khóa chính)', 'chiếc'),
(10, 'KHOA102', 'Khóa áo gió màu tím than - Khóa 90cm (Cước R5) (Khóa chính)', 'chiếc'),
(10, 'KHOA103', 'Khóa áo gió màu tím than - Khóa 85cm (Cước R3) (Khóa chính)', 'chiếc');

-- Nhóm MAC (67 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'MAC1', 'Mác sơ mi nam Hằng', 'chiếc'),
(10, 'MAC2', 'Mác sơ mi nữ Hằng', 'chiếc'),
(10, 'MAC3', 'Mác to Hằng màu đen', 'chiếc'),
(10, 'MAC4', 'Mác bé Hằng màu đen', 'chiếc'),
(10, 'MAC5', 'Mác to Hằng màu tím than', 'chiếc'),
(10, 'MAC6', 'Mác bé Hằng màu tím than', 'chiếc'),
(10, 'MAC7', 'Mác sơ mi nam Vnpost', 'chiếc'),
(10, 'MAC8', 'Mác bé Vnpost', 'chiếc'),
(10, 'MAC9', 'Mác sơ mi nam Greensea', 'chiếc'),
(10, 'MAC10', 'Mác bé Greensea', 'chiếc'),
(10, 'MAC11', 'Mác to Greensea', 'chiếc'),
(10, 'MAC12', 'Dây treo Hằng', 'chiếc'),
(10, 'MAC13', 'Mác cỡ nam - 35 (xám)', 'chiếc'),
(10, 'MAC14', 'Mác cỡ nam - 36 (xám)', 'chiếc'),
(10, 'MAC15', 'Mác cỡ nam - 37 (xám)', 'chiếc'),
(10, 'MAC16', 'Mác cỡ nam - 38 (xám)', 'chiếc'),
(10, 'MAC17', 'Mác cỡ nam - 39 (xám)', 'chiếc'),
(10, 'MAC18', 'Mác cỡ nam - 40 (xám)', 'chiếc'),
(10, 'MAC19', 'Mác cỡ nam - 41 (xám)', 'chiếc'),
(10, 'MAC20', 'Mác cỡ nam - 42 (xám)', 'chiếc'),
(10, 'MAC21', 'Mác cỡ nam - 43 (xám)', 'chiếc'),
(10, 'MAC22', 'Mác cỡ nam - 44 (xám)', 'chiếc'),
(10, 'MAC23', 'Mác cỡ nam - 45 (xám)', 'chiếc'),
(10, 'MAC24', 'Mác cỡ nam - XS', 'chiếc'),
(10, 'MAC25', 'Mác cỡ nam - S', 'chiếc'),
(10, 'MAC26', 'Mác cỡ nam - M', 'chiếc'),
(10, 'MAC27', 'Mác cỡ nam - L', 'chiếc'),
(10, 'MAC28', 'Mác cỡ nam - XL', 'chiếc'),
(10, 'MAC29', 'Mác cỡ nam - 2XL', 'chiếc'),
(10, 'MAC30', 'Mác cỡ nam - 3XL', 'chiếc'),
(10, 'MAC31', 'Mác cỡ nữ - S', 'chiếc'),
(10, 'MAC32', 'Mác cỡ nữ - M', 'chiếc'),
(10, 'MAC33', 'Mác cỡ nữ - L', 'chiếc'),
(10, 'MAC34', 'Mác cỡ nữ - XL', 'chiếc'),
(10, 'MAC35', 'Mác cỡ nữ - 2XL', 'chiếc'),
(10, 'MAC36', 'Mác cỡ tím than - S', 'chiếc'),
(10, 'MAC37', 'Mác cỡ tím than - M', 'chiếc'),
(10, 'MAC38', 'Mác cỡ tím than - L', 'chiếc'),
(10, 'MAC40', 'Mác Hằng Slim Fit', 'chiếc'),
(10, 'MAC41', 'Mác cỡ nam - 37 (đen)', 'chiếc'),
(10, 'MAC42', 'Mác cỡ nam - 38 (đen)', 'chiếc'),
(10, 'MAC43', 'Mác cỡ nam - 39 (đen)', 'chiếc'),
(10, 'MAC44', 'Mác cỡ nam - 40 (đen)', 'chiếc'),
(10, 'MAC45', 'Mác cỡ nam - 41 (đen)', 'chiếc'),
(10, 'MAC46', 'Mác cỡ nam - 42 (đen)', 'chiếc'),
(10, 'MAC47', 'Mác bảo vệ VNPT', 'chiếc'),
(10, 'MAC48', 'Mác logo VNPT', 'chiếc'),
(10, 'MAC49', 'Mác cỡ nam - 36 (đen)', 'chiếc'),
(10, 'MAC50', 'Mác cỡ nữ - 3XL', 'chiếc'),
(10, 'MAC51', 'Mác cỡ nam - 43 (đen)', 'chiếc'),
(10, 'MAC52', 'Mác VNPT tím than gập chéo 2 đầu', 'chiếc'),
(10, 'MAC53', 'Mác Made in Hằng Fashion', 'chiếc'),
(10, 'MAC54', 'Mác IDC nam', 'bộ'),
(10, 'MAC55', 'Mác IDC nữ', 'bộ'),
(10, 'MAC56', 'Mác nhựa VNPT tròn', 'chiếc'),
(10, 'MAC57', 'Mác nhựa VNPT chữ nhật', 'chiếc'),
(10, 'MAC58', 'Mác nhựa Vnpost tròn', 'chiếc'),
(10, 'MAC59', 'Mác nhựa Vnpost chữ nhật', 'chiếc'),
(10, 'MAC60', 'Mác cỡ nam - 4XL', 'chiếc'),
(10, 'MAC61', 'Mác cỡ nữ - 4XL', 'chiếc'),
(10, 'MAC62', 'Mác Lộ Trí (than Thống Nhất)', 'chiếc'),
(10, 'MAC63', 'Mác bảo vệ vàng BĐ', 'chiếc'),
(10, 'MAC64', 'Mác cỡ nam - 44 (đen)', 'chiếc'),
(10, 'MAC65', 'Mác cỡ nam - 45 (đen)', 'chiếc'),
(10, 'MAC66', 'Mác Logo EVN ngực áo', 'chiếc'),
(10, 'MAC67', 'Mác Logo EVN tay áo', 'chiếc'),
(10, 'MAC68', 'Mác Logo EVN sau lưng áo', 'chiếc');

-- Nhóm KHUY (21 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'KHUY1', 'Khuy áo budong ngoài trời', 'gói'),
(10, 'KHUY2', 'Khuy quần ngoài trời', 'gói'),
(10, 'KHUY3', 'Khuy áo khai thác bưu điện', 'gói'),
(10, 'KHUY4', 'Khuy quần bảo hộ bưu điện', 'gói'),
(10, 'KHUY5', 'Khuy áo phông vàng bưu điện', 'gói'),
(10, 'KHUY6', 'Khuy sơ mi nam Hằng', 'gói'),
(10, 'KHUY7', 'Khuy ve sơ mi nam Hằng', 'gói'),
(10, 'KHUY8', 'Khuy sơ mi nữ Hằng', 'gói'),
(10, 'KHUY9', 'Khuy quần Hằng màu đen', 'gói'),
(10, 'KHUY10', 'Khuy vest Hằng màu đen', 'gói'),
(10, 'KHUY11', 'Khuy sơ mi nam Vnpost', 'gói'),
(10, 'KHUY12', 'Khuy ve sơ mi nam Vnpost', 'gói'),
(10, 'KHUY13', 'Khuy sơ mi nữ Vnpost', 'gói'),
(10, 'KHUY14', 'Khuy quần văn phòng bưu điện', 'gói'),
(10, 'KHUY15', 'Khuy vest Vnpost', 'gói'),
(10, 'KHUY16', 'Khuy quần văn phòng bưu điện - Mới', 'gói'),
(10, 'KHUY17', 'Khuy vest Vnpost (1 khuy) - Mới', 'gói'),
(10, 'KHUY18', 'Khuy vest Vnpost (2 khuy) - Mới', 'gói'),
(10, 'KHUY19', 'Khuy quần Hằng màu xanh tươi', 'gói'),
(10, 'KHUY20', 'Khuy vest Hằng màu xanh tươi', 'gói'),
(10, 'KHUY21', 'Khuy trắng trong (dùng cho quần nữ)', 'gói');

-- Nhóm MEX (9 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'MEX1', 'Keo mùng trắng', 'mét'),
(10, 'MEX2', 'Keo mùng đen', 'mét'),
(10, 'MEX3', 'Keo mè sơ mi (Khổ 1m)', 'cây'),
(10, 'MEX4', 'Keo cổ + măng séc', 'cây'),
(10, 'MEX5', 'Mex mè đen', 'mét'),
(10, 'MEX6', 'Mex cạp quần nam', 'mét'),
(10, 'MEX7', 'Mex cạp quần nam (cắt sẵn)', 'bộ'),
(10, 'MEX8', 'Mex cạp quần nữ', 'mét'),
(10, 'MEX9', 'Mex ô', 'cây');

-- Nhóm ĐV, NI, LQ (11 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'ĐV1', 'Đệm vai nam', 'đôi'),
(10, 'ĐV2', 'Đệm vai nữ', 'đôi'),
(10, 'NI1', 'Nỉ cổ ghi', 'mét'),
(10, 'NI2', 'Nỉ cổ đen', 'mét'),
(10, 'NI3', 'Nỉ cổ tím than', 'mét'),
(10, 'NI4', 'Nỉ bông đệm ngực', 'mét'),
(10, 'NI5', 'Nỉ bông đệm ngực (Cắt sẵn) - Vest nam', 'bộ'),
(10, 'LQ1', 'Lưng quần Hằng đen trơn', 'mét'),
(10, 'LQ3', 'Lưng quần Hằng đen xương cá', 'mét'),
(10, 'LQ4', 'Lưng quần Hằng tím than xương cá', 'mét'),
(10, 'LQ5', 'Lưng quần Hằng màu trắng', 'mét');

-- Nhóm LOT (39 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'LOT1', 'Lót habutai đen nữ', 'mét'),
(10, 'LOT2', 'Lót habutai tím than nữ', 'mét'),
(10, 'LOT3', 'Lót đen nam', 'mét'),
(10, 'LOT4', 'Lót tím than nam', 'mét'),
(10, 'LOT5', 'Lót xanh tươi áo khoác ngoài trời', 'mét'),
(10, 'LOT6', 'Lót túi kate đen trơn (Dùng làm Tsy)', 'mét'),
(10, 'LOT7', 'Lót túi kate đen trơn (cắt sẵn) - Tsy nam', 'bộ'),
(10, 'LOT8', 'Lót túi kate đen trơn (cắt sẵn) - Quần váy nữ', 'bộ'),
(10, 'LOT9', 'Lót túi kate đen trơn (cắt sẵn) - Vest nam', 'bộ'),
(10, 'LOT10', 'Lót túi kate đen trơn (Dùng làm BH)', 'mét'),
(10, 'LOT11', 'Lót túi kate trắng (Dùng làm Tsy)', 'mét'),
(10, 'LOT12', 'Lót túi kate trắng (Dùng làm BH)', 'mét'),
(10, 'LOT13', 'Lót túi kate trắng (cắt sẵn) - NT nam', 'bộ'),
(10, 'LOT14', 'Lót túi kate trắng (cắt sẵn) - Quần váy nữ', 'bộ'),
(10, 'LOT15', 'Lót túi kate đen xương cá', 'mét'),
(10, 'LOT16', 'Lót túi kate đen xương cá (cắt sẵn) - Tsy nam', 'bộ'),
(10, 'LOT17', 'Lót túi kate đen xương cá (cắt sẵn) - Quần váy nữ', 'bộ'),
(10, 'LOT18', 'Lót túi kate đen xương cá (cắt sẵn) - Vest nam', 'bộ'),
(10, 'LOT19', 'Lót túi kate tím than xương cá', 'mét'),
(10, 'LOT20', 'Lót túi kate tím than xương cá (cắt sẵn) - Tsy nam', 'bộ'),
(10, 'LOT21', 'Lót túi kate tím than xương cá (cắt sẵn) - Quần váy nữ', 'bộ'),
(10, 'LOT22', 'Lót túi kate tím than xương cá (cắt sẵn) - Vest nam', 'bộ'),
(10, 'LOT23', 'Lót viền túi đen (Khổ 1.5m)', 'mét'),
(10, 'LOT24', 'Lót viền túi tím than (Khổ 1.5m)', 'mét'),
(10, 'LOT25', 'Lót tím than nam Thủy điện Hòa Bình (Lót vest nam xịn có co giãn)', 'mét'),
(10, 'LOT26', 'Lót cam áo khoác Thủy điện Hòa Bình', 'mét'),
(10, 'LOT27', 'Lót kẻ caro áo khoác Ban KTM 2023', 'mét'),
(10, 'LOT28', 'Lót tím than áo khoác Ban KTM 2023', 'mét'),
(10, 'LOT29', 'Lót lưới tím than (ô nhỏ)', 'mét'),
(10, 'LOT30', 'Lót lưới tím than (ô to)', 'kilogam'),
(10, 'LOT31', 'Lót lưới xanh tươi (ô nhỏ)', 'kilogam'),
(10, 'LOT32', 'Vải làm khăn/nơ', 'mét'),
(10, 'LOT33', 'Lót kate tím than (Dùng làm BH)', 'mét'),
(10, 'LOT34', 'Lót habutai xanh tươi nữ', 'mét'),
(10, 'LOT35', 'Lót gối tím than', 'mét'),
(10, 'LOT36', 'Lót túi kate tím than (cắt sẵn) - Quần váy nữ GDV', 'bộ'),
(10, 'LOT37', 'Lót gối đen', 'mét'),
(10, 'LOT38', 'Lót túi kate đen quần BH nữ (cắt sẵn)', 'chiếc'),
(10, 'LOT39', 'Lót túi kate đen trơn quần BH nam (cắt sẵn)', 'bộ');

-- Nhóm NHAM, TB, K (34 mã)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(10, 'NHAM1', 'Nhám dính ghi', 'cặp'),
(10, 'NHAM2', 'Nhám dính tím than', 'cặp'),
(10, 'NHAM3', 'Nhám dính xanh tươi', 'mét'),
(10, 'TB1', 'Thẻ bài to', 'chiếc'),
(10, 'TB2', 'Thẻ bài bé', 'chiếc'),
(10, 'TB3', 'Dây treo thẻ bài', 'chiếc'),
(10, 'K1', 'Móc quần', 'bộ'),
(10, 'K2', 'Canh tóc', 'mét'),
(10, 'K3', 'Dây phản quang 2cm không in', 'cuộn'),
(10, 'K4', 'Dây phản quang 2cm in VNPT', 'cuộn'),
(10, 'K5', 'Chun 3F', 'cuộn'),
(10, 'K6', 'Chun 2F', 'cuộn'),
(10, 'K7', 'Chun 5F', 'cuộn'),
(10, 'K8', 'Bút gile', 'chiếc'),
(10, 'K9', 'Cầu vai bảo vệ VNPT', 'đôi'),
(10, 'K10', 'Ve áo bảo vệ VNPT', 'đôi'),
(10, 'K11', 'Canh tóc (cắt sẵn) - Vest nam', 'bộ'),
(10, 'K12', 'Chốt gấu bé (nhựa)', 'chiếc'),
(10, 'K13', 'Chốt gấu to (nhựa)', 'chiếc'),
(10, 'K14', 'Chốt gấu bé (kim loại)', 'chiếc'),
(10, 'K15', 'Chốt gấu to (kim loại)', 'chiếc'),
(10, 'K16', 'Mác trắng số 3', 'chiếc'),
(10, 'K17', 'Mác trắng số 4', 'chiếc'),
(10, 'K18', 'Mác trắng số 5', 'chiếc'),
(10, 'K19', 'Mác trắng số 6', 'chiếc'),
(10, 'K20', 'Mác trắng số 7', 'chiếc'),
(10, 'K21', 'Mác trắng số 8', 'chiếc'),
(10, 'K22', 'Chốt gấu bé (màu đen - nhựa dẹt)', 'chiếc'),
(10, 'K23', 'Chốt gấu to (màu đen - nhựa dẹt)', 'chiếc'),
(10, 'K24', 'Chốt gấu bé (màu đen - nhựa tròn)', 'chiếc'),
(10, 'K25', 'Chốt gấu to (màu đen - nhựa tròn)', 'chiếc'),
(10, 'K26', 'Dựng', 'cây'),
(10, 'K27', 'Dựng vải', 'mét'),
(10, 'K28', 'Dựng giấy (đen)', 'mét');

-- 3.7a Warehouses (Kho)
INSERT INTO warehouses (warehouse_name, is_default) VALUES
('Kho Chính', TRUE),
('Kho Trường', FALSE);

-- 3.7b Units (72 đơn vị/khách hàng)
INSERT INTO units (unit_name) VALUES
('BĐ Hà Nội'),
('BĐ Hà Nội, BĐ Hải Phòng'),
('BĐ Hà Nội, BĐ Vĩnh Phúc'),
('BĐ Hải Phòng'),
('BĐ Khánh Hòa'),
('BĐ lô lẻ của Thương'),
('BĐ Nghệ An'),
('BĐ Ninh Bình'),
('BĐ Quảng Bình'),
('BĐ Sơn La'),
('BĐ Vĩnh Phúc'),
('BĐTT Sài Gòn - BĐ Hồ Chí Minh'),
('Bưu điện Bến Tre'),
('Bưu điện Bình Định'),
('Bưu điện Bình Phước'),
('Bưu điện Cà Mau'),
('Bưu điện Cần Thơ'),
('Bưu điện Cầu Giấy'),
('Bưu điện Chương Mỹ'),
('Bưu điện Đắk Lắk'),
('Bưu điện Đông Anh'),
('Bưu điện Đồng Nai'),
('Bưu điện Hà Đông'),
('Bưu điện Hà Tĩnh'),
('Bưu điện Hải Dương'),
('Bưu điện Hoàn Kiếm'),
('Bưu điện Huế'),
('Bưu điện Hưng Yên'),
('Bưu điện Kiên Giang'),
('Bưu điện Lạng Sơn'),
('Bưu điện Lào Cai'),
('Bưu điện Long Biên'),
('Bưu điện Nam Định'),
('Bưu điện Ninh Bình'),
('Bưu điện Phú Thọ'),
('Bưu điện Quảng Bình'),
('Bưu điện Quảng Trị'),
('Bưu điện Sơn La'),
('Bưu điện Sơn Tây'),
('Bưu điện Thái Nguyên'),
('Bưu điện Thanh Trì'),
('Bưu điện Thanh Trì - Đợt 2'),
('Bưu điện Từ Liêm'),
('Bưu điện Tuyên Quang'),
('Bưu điện Vĩnh Phúc'),
('Bưu điện Yên Bái'),
('Công ty Du lịch bưu điện'),
('Công ty Logistics'),
('Đạt - con chị Liên Hương - Công đoàn Tct Bưu điện'),
('Khách lẻ sếp Hằng'),
('Khách lẻ, BĐ An Giang, BĐ Lạng Sơn, TCT Bưu điện, BĐ Hải Phòng'),
('Kho'),
('Kho vận'),
('Lấy áo đi đo Than Thống Nhất đợt 3'),
('Logistic, BĐ HCM, BĐ Tuyên Quang'),
('Mẫu BĐ Huế - Hằng fashion'),
('Nhập lại kho đồ mượn đi đo và đồ khách trả lại'),
('Phát hành báo chí'),
('Tạm loại vì bị ố. Nếu tẩy được sẽ nhập lại'),
('Tcty Bưu điện'),
('TCT Bưu điện'),
('Thùy'),
('Toàn Huế đi đo'),
('Tổng Nét'),
('Tổng Nét, VT Hòa Bình'),
('Trường Định Công'),
('TT Đào tạo và Bồi dưỡng nghiệp vụ Bưu điện'),
('TTKD Hà Nội + BĐ Gia Lai'),
('TTKD Lai Châu, VT Thái Nguyên, TTKD Đồng Tháp, TTKD Tuyên Quang'),
('VT An Giang, BĐ Kon Tum, Cty dịch vụ số'),
('VT Hà Nội, TTKD Bắc Kạn, Cty DV Số');

-- 3.8 Users (Người dùng hệ thống)
-- Password mặc định: password (BCrypt hash)
INSERT INTO users (username, password, full_name) VALUES
('hoi', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Hội'),
('cat', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Cát'),
('thanh', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Thanh'),
('thuy', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Thúy'),
('huong', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Hương'),
('nga', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Nga'),
('khoa', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Khoa'),
('tra', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Trà'),
('tthuy', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Thùy');

-- 3.9 User Roles (Gán role cho users)
-- user_id: 1=hoi, 2=cat, 3=thanh, 4=thuy, 5=huong, 6=nga, 7=khoa, 8=tra
-- role_id: 1=ADMIN, 2=USER, 3=STOCKKEEPER, 4=PURCHASER, 5=SALES, 6=MEASUREMENT, 7=PRODUCTION
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- hoi: ADMIN
(1, 2), -- hoi: USER
(2, 1), -- cat: ADMIN
(2, 2), -- cat: USER
(3, 1), -- thanh: ADMIN
(3, 2), -- thanh: USER
(3, 3), -- thanh: STOCKKEEPER
(4, 4), -- thuy: PURCHASER
(4, 7), -- thuy: PRODUCTION
(4, 2), -- thuy: USER
(5, 2), -- huong: USER
(6, 2), -- nga: USER
(7, 3), -- khoa: STOCKKEEPER
(8, 6), -- tra: MEASUREMENT
(9, 5); -- tthuy: SALES

-- =====================================================
-- PHẦN 4: REQUEST SETS (52 bộ phiếu - gộp theo tên)
-- =====================================================
INSERT INTO request_sets (set_name, created_by, created_at) VALUES
-- set_id = 1: Nhập kho ban đầu
('Nhập kho ban đầu', NULL, '2025-06-20 00:00:00');
-- =====================================================
-- PHẦN 5: INVENTORY REQUESTS (89 phiếu xuất/nhập)
-- =====================================================
-- Requests nhập kho ban đầu cho TẤT CẢ sản phẩm (set_id = 1, unit = 'Kho')
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 1, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 11, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 12, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 13, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 14, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 15, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 16, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 17, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 8, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 9, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 10, 'IN', NULL, '2025-06-20 00:00:00' FROM units u WHERE u.unit_name = 'Kho';

-- Set status = 'EXECUTED' và submitted_at cho tất cả request_sets cũ (dữ liệu lịch sử đã được thực hiện)
UPDATE request_sets SET status = 'EXECUTED', submitted_at = created_at WHERE set_id > 0;

-- =====================================================
-- PHẦN 6: STORED PROCEDURE ĐỂ INSERT ITEMS
-- =====================================================
DELIMITER //

DROP PROCEDURE IF EXISTS insert_item_by_variant//
CREATE PROCEDURE insert_item_by_variant(
    IN p_request_id BIGINT,
    IN p_style_name VARCHAR(50),
    IN p_size_value VARCHAR(10),
    IN p_length_code VARCHAR(10),
    IN p_quantity INT
)
BEGIN
    DECLARE v_variant_id BIGINT;

    IF p_quantity > 0 THEN
        SELECT pv.variant_id INTO v_variant_id
        FROM product_variants pv
        JOIN styles s ON s.style_id = pv.style_id
        JOIN sizes sz ON sz.size_id = pv.size_id
        JOIN length_types lt ON lt.length_type_id = pv.length_type_id
        WHERE s.style_name = p_style_name
          AND sz.size_value = p_size_value
          AND lt.code = p_length_code;

        IF v_variant_id IS NOT NULL THEN
            INSERT INTO inventory_request_items (request_id, variant_id, quantity)
            VALUES (p_request_id, v_variant_id, p_quantity);
        END IF;
    END IF;
END//

-- Procedure cho STRUCTURED với gender + size (không có length) — SP3,5,6
DROP PROCEDURE IF EXISTS insert_item_by_gender//
CREATE PROCEDURE insert_item_by_gender(
    IN p_product_id BIGINT,
    IN p_size_value VARCHAR(10),
    IN p_gender VARCHAR(10),
    IN p_quantity INT
)
BEGIN
    DECLARE v_variant_id BIGINT;
    DECLARE v_request_id BIGINT;

    SELECT r.request_id INTO v_request_id
    FROM inventory_requests r WHERE r.set_id = 1 AND r.product_id = p_product_id;

    IF p_quantity > 0 AND v_request_id IS NOT NULL THEN
        SELECT pv.variant_id INTO v_variant_id
        FROM product_variants pv
        JOIN sizes sz ON sz.size_id = pv.size_id
        WHERE pv.product_id = p_product_id
          AND sz.size_value = p_size_value
          AND pv.gender = p_gender;

        IF v_variant_id IS NOT NULL THEN
            INSERT INTO inventory_request_items (request_id, variant_id, quantity)
            VALUES (v_request_id, v_variant_id, p_quantity);
        END IF;
    END IF;
END//

-- Procedure cho STRUCTURED với gender + size + length — SP4
DROP PROCEDURE IF EXISTS insert_item_by_gender_length//
CREATE PROCEDURE insert_item_by_gender_length(
    IN p_product_id BIGINT,
    IN p_size_value VARCHAR(10),
    IN p_length_code VARCHAR(10),
    IN p_gender VARCHAR(10),
    IN p_quantity INT
)
BEGIN
    DECLARE v_variant_id BIGINT;
    DECLARE v_request_id BIGINT;

    SELECT r.request_id INTO v_request_id
    FROM inventory_requests r WHERE r.set_id = 1 AND r.product_id = p_product_id;

    IF p_quantity > 0 AND v_request_id IS NOT NULL THEN
        SELECT pv.variant_id INTO v_variant_id
        FROM product_variants pv
        JOIN sizes sz ON sz.size_id = pv.size_id
        JOIN length_types lt ON lt.length_type_id = pv.length_type_id
        WHERE pv.product_id = p_product_id
          AND sz.size_value = p_size_value
          AND lt.code = p_length_code
          AND pv.gender = p_gender;

        IF v_variant_id IS NOT NULL THEN
            INSERT INTO inventory_request_items (request_id, variant_id, quantity)
            VALUES (v_request_id, v_variant_id, p_quantity);
        END IF;
    END IF;
END//

-- Procedure cho STRUCTURED với size only (không gender, không length) — SP15,16
DROP PROCEDURE IF EXISTS insert_item_by_size//
CREATE PROCEDURE insert_item_by_size(
    IN p_product_id BIGINT,
    IN p_size_value VARCHAR(10),
    IN p_quantity INT
)
BEGIN
    DECLARE v_variant_id BIGINT;
    DECLARE v_request_id BIGINT;

    SELECT r.request_id INTO v_request_id
    FROM inventory_requests r WHERE r.set_id = 1 AND r.product_id = p_product_id;

    IF p_quantity > 0 AND v_request_id IS NOT NULL THEN
        SELECT pv.variant_id INTO v_variant_id
        FROM product_variants pv
        JOIN sizes sz ON sz.size_id = pv.size_id
        WHERE pv.product_id = p_product_id
          AND sz.size_value = p_size_value
          AND pv.gender IS NULL;

        IF v_variant_id IS NOT NULL THEN
            INSERT INTO inventory_request_items (request_id, variant_id, quantity)
            VALUES (v_request_id, v_variant_id, p_quantity);
        END IF;
    END IF;
END//

-- Procedure cho ITEM_BASED — SP17,8,9,10
DROP PROCEDURE IF EXISTS insert_item_by_code//
CREATE PROCEDURE insert_item_by_code(
    IN p_product_id BIGINT,
    IN p_item_code VARCHAR(50),
    IN p_quantity INT
)
BEGIN
    DECLARE v_variant_id BIGINT;
    DECLARE v_request_id BIGINT;

    SELECT r.request_id INTO v_request_id
    FROM inventory_requests r WHERE r.set_id = 1 AND r.product_id = p_product_id;

    IF p_quantity > 0 AND v_request_id IS NOT NULL THEN
        SELECT pv.variant_id INTO v_variant_id
        FROM product_variants pv
        WHERE pv.product_id = p_product_id AND pv.item_code = p_item_code;

        IF v_variant_id IS NOT NULL THEN
            INSERT INTO inventory_request_items (request_id, variant_id, quantity)
            VALUES (v_request_id, v_variant_id, p_quantity);
        END IF;
    END IF;
END//

DELIMITER ;

-- =====================================================
-- PHẦN 7: INVENTORY REQUEST ITEMS (CHI TIẾT)
-- =====================================================

-- REQUEST 1: Nhập kho ban đầu (20/06/2025) - Kho
-- Dữ liệu từ CSV: SƠ MI NAM 2025 - SM1 - HDH22 - không lé không thêu.csv (line 5)
-- CỔ ĐIỂN (Tổng: Cộc=110, Dài=91)
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 35, 'COC', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 35, 'DAI', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 36, 'COC', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 36, 'DAI', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 37, 'COC', 11);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 37, 'DAI', 20);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 38, 'COC', 21);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 38, 'DAI', 4);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 39, 'COC', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 39, 'DAI', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 40, 'COC', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 40, 'DAI', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 41, 'COC', 21);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 41, 'DAI', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 42, 'COC', 15);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 42, 'DAI', 12);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 43, 'COC', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 43, 'DAI', 4);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 44, 'COC', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 44, 'DAI', 11);
-- CỔ ĐIỂN NGẮN (Tổng: Cộc=149, Dài=143)
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 35, 'COC', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 35, 'DAI', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 36, 'COC', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 36, 'DAI', 4);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 37, 'COC', 24);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 37, 'DAI', 21);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 38, 'COC', 25);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 38, 'DAI', 20);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 39, 'COC', 24);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 39, 'DAI', 16);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 40, 'COC', 11);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 40, 'DAI', 22);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 41, 'COC', 22);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 41, 'DAI', 27);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 42, 'COC', 25);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 42, 'DAI', 21);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 43, 'COC', 8);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 43, 'DAI', 7);
-- SLIM (Tổng: Cộc=141, Dài=114)
CALL insert_item_by_variant(1, 'SLIM', 37, 'COC', 14);
CALL insert_item_by_variant(1, 'SLIM', 37, 'DAI', 10);
CALL insert_item_by_variant(1, 'SLIM', 38, 'COC', 25);
CALL insert_item_by_variant(1, 'SLIM', 38, 'DAI', 22);
CALL insert_item_by_variant(1, 'SLIM', 39, 'COC', 24);
CALL insert_item_by_variant(1, 'SLIM', 39, 'DAI', 17);
CALL insert_item_by_variant(1, 'SLIM', 40, 'COC', 17);
CALL insert_item_by_variant(1, 'SLIM', 40, 'DAI', 7);
CALL insert_item_by_variant(1, 'SLIM', 41, 'COC', 19);
CALL insert_item_by_variant(1, 'SLIM', 41, 'DAI', 21);
CALL insert_item_by_variant(1, 'SLIM', 42, 'COC', 12);
CALL insert_item_by_variant(1, 'SLIM', 42, 'DAI', 14);
CALL insert_item_by_variant(1, 'SLIM', 43, 'COC', 20);
CALL insert_item_by_variant(1, 'SLIM', 43, 'DAI', 16);
CALL insert_item_by_variant(1, 'SLIM', 44, 'COC', 10);
CALL insert_item_by_variant(1, 'SLIM', 44, 'DAI', 7);
-- SLIM Ngắn (Tổng: Cộc=123, Dài=115)
CALL insert_item_by_variant(1, 'SLIM Ngắn', 37, 'COC', 13);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 37, 'DAI', 15);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 38, 'COC', 15);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 38, 'DAI', 17);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 39, 'COC', 26);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 39, 'DAI', 19);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 40, 'COC', 21);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 40, 'DAI', 16);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 41, 'COC', 20);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 41, 'DAI', 20);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 42, 'COC', 18);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 42, 'DAI', 18);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 44, 'COC', 10);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 44, 'DAI', 10);

-- =====================================================
-- Product 11: Áo khoác Bưu điện - ghi phối vàng (child của SP3)
-- STRUCTURED: gender + size (no length)
-- =====================================================
-- NAM
CALL insert_item_by_gender(11, 'XS', 'NAM', 3);
CALL insert_item_by_gender(11, 'S', 'NAM', 8);
CALL insert_item_by_gender(11, 'M', 'NAM', 18);
CALL insert_item_by_gender(11, 'L', 'NAM', 22);
CALL insert_item_by_gender(11, 'XL', 'NAM', 20);
CALL insert_item_by_gender(11, '2XL', 'NAM', 15);
CALL insert_item_by_gender(11, '3XL', 'NAM', 10);
CALL insert_item_by_gender(11, '4XL', 'NAM', 6);
CALL insert_item_by_gender(11, '5XL', 'NAM', 3);
CALL insert_item_by_gender(11, '6XL', 'NAM', 2);
-- NỮ
CALL insert_item_by_gender(11, 'XS', 'NU', 4);
CALL insert_item_by_gender(11, 'S', 'NU', 10);
CALL insert_item_by_gender(11, 'M', 'NU', 16);
CALL insert_item_by_gender(11, 'L', 'NU', 14);
CALL insert_item_by_gender(11, 'XL', 'NU', 12);
CALL insert_item_by_gender(11, '2XL', 'NU', 8);
CALL insert_item_by_gender(11, '3XL', 'NU', 5);
CALL insert_item_by_gender(11, '4XL', 'NU', 3);
CALL insert_item_by_gender(11, '5XL', 'NU', 2);
CALL insert_item_by_gender(11, '6XL', 'NU', 1);

-- =====================================================
-- Product 12: Áo phông Bưu điện (vàng phối ghi) - Bưu tá (child của SP4)
-- STRUCTURED: gender + size + length (COC/DAI)
-- =====================================================
-- NAM
CALL insert_item_by_gender_length(12, 'XS', 'COC', 'NAM', 2);
CALL insert_item_by_gender_length(12, 'XS', 'DAI', 'NAM', 2);
CALL insert_item_by_gender_length(12, 'S', 'COC', 'NAM', 6);
CALL insert_item_by_gender_length(12, 'S', 'DAI', 'NAM', 5);
CALL insert_item_by_gender_length(12, 'M', 'COC', 'NAM', 15);
CALL insert_item_by_gender_length(12, 'M', 'DAI', 'NAM', 12);
CALL insert_item_by_gender_length(12, 'L', 'COC', 'NAM', 18);
CALL insert_item_by_gender_length(12, 'L', 'DAI', 'NAM', 15);
CALL insert_item_by_gender_length(12, 'XL', 'COC', 'NAM', 16);
CALL insert_item_by_gender_length(12, 'XL', 'DAI', 'NAM', 14);
CALL insert_item_by_gender_length(12, '2XL', 'COC', 'NAM', 12);
CALL insert_item_by_gender_length(12, '2XL', 'DAI', 'NAM', 10);
CALL insert_item_by_gender_length(12, '3XL', 'COC', 'NAM', 8);
CALL insert_item_by_gender_length(12, '3XL', 'DAI', 'NAM', 6);
CALL insert_item_by_gender_length(12, '4XL', 'COC', 'NAM', 4);
CALL insert_item_by_gender_length(12, '4XL', 'DAI', 'NAM', 3);
CALL insert_item_by_gender_length(12, '5XL', 'COC', 'NAM', 2);
CALL insert_item_by_gender_length(12, '5XL', 'DAI', 'NAM', 2);
CALL insert_item_by_gender_length(12, '6XL', 'COC', 'NAM', 1);
CALL insert_item_by_gender_length(12, '6XL', 'DAI', 'NAM', 1);
-- NỮ
CALL insert_item_by_gender_length(12, 'XS', 'COC', 'NU', 3);
CALL insert_item_by_gender_length(12, 'XS', 'DAI', 'NU', 2);
CALL insert_item_by_gender_length(12, 'S', 'COC', 'NU', 8);
CALL insert_item_by_gender_length(12, 'S', 'DAI', 'NU', 6);
CALL insert_item_by_gender_length(12, 'M', 'COC', 'NU', 14);
CALL insert_item_by_gender_length(12, 'M', 'DAI', 'NU', 11);
CALL insert_item_by_gender_length(12, 'L', 'COC', 'NU', 12);
CALL insert_item_by_gender_length(12, 'L', 'DAI', 'NU', 10);
CALL insert_item_by_gender_length(12, 'XL', 'COC', 'NU', 10);
CALL insert_item_by_gender_length(12, 'XL', 'DAI', 'NU', 8);
CALL insert_item_by_gender_length(12, '2XL', 'COC', 'NU', 6);
CALL insert_item_by_gender_length(12, '2XL', 'DAI', 'NU', 5);
CALL insert_item_by_gender_length(12, '3XL', 'COC', 'NU', 4);
CALL insert_item_by_gender_length(12, '3XL', 'DAI', 'NU', 3);
CALL insert_item_by_gender_length(12, '4XL', 'COC', 'NU', 2);
CALL insert_item_by_gender_length(12, '4XL', 'DAI', 'NU', 2);
CALL insert_item_by_gender_length(12, '5XL', 'COC', 'NU', 1);
CALL insert_item_by_gender_length(12, '5XL', 'DAI', 'NU', 1);
CALL insert_item_by_gender_length(12, '6XL', 'COC', 'NU', 1);
CALL insert_item_by_gender_length(12, '6XL', 'DAI', 'NU', 1);

-- =====================================================
-- Product 13: Áo Gile len VNPT (child của SP5) — Nhập kho ban đầu
-- STRUCTURED: gender + size (no length)
-- =====================================================
-- NAM
CALL insert_item_by_gender(13, 'XS', 'NAM', 2);
CALL insert_item_by_gender(13, 'S', 'NAM', 6);
CALL insert_item_by_gender(13, 'M', 'NAM', 14);
CALL insert_item_by_gender(13, 'L', 'NAM', 18);
CALL insert_item_by_gender(13, 'XL', 'NAM', 16);
CALL insert_item_by_gender(13, '2XL', 'NAM', 12);
CALL insert_item_by_gender(13, '3XL', 'NAM', 8);
CALL insert_item_by_gender(13, '4XL', 'NAM', 5);
CALL insert_item_by_gender(13, '5XL', 'NAM', 3);
CALL insert_item_by_gender(13, '6XL', 'NAM', 2);
-- NỮ
CALL insert_item_by_gender(13, 'XS', 'NU', 3);
CALL insert_item_by_gender(13, 'S', 'NU', 8);
CALL insert_item_by_gender(13, 'M', 'NU', 12);
CALL insert_item_by_gender(13, 'L', 'NU', 10);
CALL insert_item_by_gender(13, 'XL', 'NU', 8);
CALL insert_item_by_gender(13, '2XL', 'NU', 6);
CALL insert_item_by_gender(13, '3XL', 'NU', 4);
CALL insert_item_by_gender(13, '4XL', 'NU', 2);
CALL insert_item_by_gender(13, '5XL', 'NU', 1);
CALL insert_item_by_gender(13, '6XL', 'NU', 1);

-- =====================================================
-- Product 14: Áo Gile bảo hộ Bưu điện - Kaky vàng (child của SP6) — Nhập kho ban đầu
-- STRUCTURED: gender + size (no length)
-- =====================================================
-- NAM
CALL insert_item_by_gender(14, 'XS', 'NAM', 4);
CALL insert_item_by_gender(14, 'S', 'NAM', 10);
CALL insert_item_by_gender(14, 'M', 'NAM', 20);
CALL insert_item_by_gender(14, 'L', 'NAM', 25);
CALL insert_item_by_gender(14, 'XL', 'NAM', 22);
CALL insert_item_by_gender(14, '2XL', 'NAM', 16);
CALL insert_item_by_gender(14, '3XL', 'NAM', 10);
CALL insert_item_by_gender(14, '4XL', 'NAM', 6);
CALL insert_item_by_gender(14, '5XL', 'NAM', 3);
CALL insert_item_by_gender(14, '6XL', 'NAM', 2);
-- NỮ
CALL insert_item_by_gender(14, 'XS', 'NU', 5);
CALL insert_item_by_gender(14, 'S', 'NU', 12);
CALL insert_item_by_gender(14, 'M', 'NU', 18);
CALL insert_item_by_gender(14, 'L', 'NU', 15);
CALL insert_item_by_gender(14, 'XL', 'NU', 12);
CALL insert_item_by_gender(14, '2XL', 'NU', 8);
CALL insert_item_by_gender(14, '3XL', 'NU', 5);
CALL insert_item_by_gender(14, '4XL', 'NU', 3);
CALL insert_item_by_gender(14, '5XL', 'NU', 2);
CALL insert_item_by_gender(14, '6XL', 'NU', 1);

-- =====================================================
-- Product 15: Giày BH (child của SP7) — Nhập kho ban đầu
-- STRUCTURED: size only (38-45)
-- =====================================================
CALL insert_item_by_size(15, '38', 15);
CALL insert_item_by_size(15, '39', 25);
CALL insert_item_by_size(15, '40', 35);
CALL insert_item_by_size(15, '41', 40);
CALL insert_item_by_size(15, '42', 38);
CALL insert_item_by_size(15, '43', 20);
CALL insert_item_by_size(15, '44', 12);
CALL insert_item_by_size(15, '45', 8);

-- =====================================================
-- Product 16: Bộ áo mưa (child của SP7) — Nhập kho ban đầu
-- STRUCTURED: size only (S-4XL)
-- =====================================================
CALL insert_item_by_size(16, 'S', 10);
CALL insert_item_by_size(16, 'M', 20);
CALL insert_item_by_size(16, 'L', 25);
CALL insert_item_by_size(16, 'XL', 22);
CALL insert_item_by_size(16, '2XL', 15);
CALL insert_item_by_size(16, '3XL', 10);
CALL insert_item_by_size(16, '4XL', 5);

-- =====================================================
-- Product 17: BẢO HỘ LAO ĐỘNG 2026 — Nhập kho ban đầu
-- ITEM_BASED
-- =====================================================
CALL insert_item_by_code(17, 'M1', 50);
CALL insert_item_by_code(17, 'TUI1', 30);
CALL insert_item_by_code(17, 'BL1', 20);

-- =====================================================
-- Product 8: NHẬP XUẤT VẢI 2026 — Nhập kho ban đầu
-- ITEM_BASED (đơn vị: mét, kilogam)
-- =====================================================
CALL insert_item_by_code(8, 'B1', 520);
CALL insert_item_by_code(8, 'B2', 380);
CALL insert_item_by_code(8, 'B4', 150);
CALL insert_item_by_code(8, 'B7', 450);
CALL insert_item_by_code(8, 'B9', 200);
CALL insert_item_by_code(8, 'B11', 180);
CALL insert_item_by_code(8, 'B12', 320);
CALL insert_item_by_code(8, 'B13', 160);
CALL insert_item_by_code(8, 'B15', 400);
CALL insert_item_by_code(8, 'B16', 280);
CALL insert_item_by_code(8, 'B17', 350);
CALL insert_item_by_code(8, 'B18', 150);
CALL insert_item_by_code(8, 'B19', 300);
CALL insert_item_by_code(8, 'B20', 220);
CALL insert_item_by_code(8, 'B21', 120);
CALL insert_item_by_code(8, 'B22', 250);
CALL insert_item_by_code(8, 'B23', 180);
CALL insert_item_by_code(8, 'B24', 100);
CALL insert_item_by_code(8, 'B25', 80);
CALL insert_item_by_code(8, 'B26', 600);
CALL insert_item_by_code(8, 'B27', 450);
CALL insert_item_by_code(8, 'B28', 400);
CALL insert_item_by_code(8, 'B29', 300);
CALL insert_item_by_code(8, 'B30', 350);
CALL insert_item_by_code(8, 'B31', 250);
CALL insert_item_by_code(8, 'B32', 200);
CALL insert_item_by_code(8, 'B33', 300);
CALL insert_item_by_code(8, 'B34', 280);
CALL insert_item_by_code(8, 'B35', 45);
CALL insert_item_by_code(8, 'B37', 150);
CALL insert_item_by_code(8, 'B39', 120);

-- =====================================================
-- Product 9: PHỤ KIỆN — Nhập kho ban đầu
-- ITEM_BASED (đơn vị: chiếc, bộ)
-- =====================================================
CALL insert_item_by_code(9, 'PK1', 120);
CALL insert_item_by_code(9, 'PK2', 85);
CALL insert_item_by_code(9, 'PK3', 40);
CALL insert_item_by_code(9, 'PK4', 65);
CALL insert_item_by_code(9, 'PK5', 55);
CALL insert_item_by_code(9, 'PK6', 70);
CALL insert_item_by_code(9, 'PK8', 25);
CALL insert_item_by_code(9, 'PK9', 90);
CALL insert_item_by_code(9, 'PK10', 60);
CALL insert_item_by_code(9, 'PK11', 75);
CALL insert_item_by_code(9, 'PK13', 45);
CALL insert_item_by_code(9, 'PK14', 50);
CALL insert_item_by_code(9, 'PK15', 20);
CALL insert_item_by_code(9, 'PK16', 15);
CALL insert_item_by_code(9, 'PK17', 80);
CALL insert_item_by_code(9, 'PK18', 60);
CALL insert_item_by_code(9, 'PK19', 30);
CALL insert_item_by_code(9, 'PK20', 25);
CALL insert_item_by_code(9, 'PK21', 35);
CALL insert_item_by_code(9, 'PK22', 40);
CALL insert_item_by_code(9, 'PK23', 20);
CALL insert_item_by_code(9, 'PK24', 30);
CALL insert_item_by_code(9, 'PK25', 25);
CALL insert_item_by_code(9, 'PK26', 50);
CALL insert_item_by_code(9, 'PK27', 35);
CALL insert_item_by_code(9, 'PK28', 45);
CALL insert_item_by_code(9, 'PK29', 20);
CALL insert_item_by_code(9, 'PK30', 25);
CALL insert_item_by_code(9, 'PK31', 15);
CALL insert_item_by_code(9, 'PK32', 10);
CALL insert_item_by_code(9, 'PK33', 30);
CALL insert_item_by_code(9, 'PK34', 55);
CALL insert_item_by_code(9, 'PK35', 40);
CALL insert_item_by_code(9, 'PK36', 25);
CALL insert_item_by_code(9, 'PK37', 35);
CALL insert_item_by_code(9, 'PK38', 30);
CALL insert_item_by_code(9, 'PK39', 20);
CALL insert_item_by_code(9, 'PK40', 25);
CALL insert_item_by_code(9, 'PK41', 45);
CALL insert_item_by_code(9, 'PK42', 40);
CALL insert_item_by_code(9, 'PK43', 30);
CALL insert_item_by_code(9, 'PK44', 25);
CALL insert_item_by_code(9, 'PK45', 60);
CALL insert_item_by_code(9, 'PK46', 35);
CALL insert_item_by_code(9, 'PK47', 20);
CALL insert_item_by_code(9, 'PK48', 50);
CALL insert_item_by_code(9, 'PK49', 40);

-- =====================================================
-- Product 10: PHỤ LIỆU — Nhập kho ban đầu (dữ liệu thực tế)
-- ITEM_BASED (258 mã, chỉ insert những mã có tồn kho > 0)
-- =====================================================
-- Nhóm KHOA
CALL insert_item_by_code(10, 'KHOA1', 9250);
CALL insert_item_by_code(10, 'KHOA3', 1300);
CALL insert_item_by_code(10, 'KHOA4', 7200);
CALL insert_item_by_code(10, 'KHOA5', 87);
CALL insert_item_by_code(10, 'KHOA6', 2080);
CALL insert_item_by_code(10, 'KHOA7', 605);
CALL insert_item_by_code(10, 'KHOA8', 5000);
CALL insert_item_by_code(10, 'KHOA9', 536);
CALL insert_item_by_code(10, 'KHOA30', 2630);
CALL insert_item_by_code(10, 'KHOA31', 2400);
CALL insert_item_by_code(10, 'KHOA32', 982);
CALL insert_item_by_code(10, 'KHOA33', 830);
CALL insert_item_by_code(10, 'KHOA36', 499);
CALL insert_item_by_code(10, 'KHOA37', 3);
CALL insert_item_by_code(10, 'KHOA38', 9);
CALL insert_item_by_code(10, 'KHOA39', 15);
CALL insert_item_by_code(10, 'KHOA47', 1602);
CALL insert_item_by_code(10, 'KHOA48', 17);
CALL insert_item_by_code(10, 'KHOA49', 35);
CALL insert_item_by_code(10, 'KHOA50', 430);
CALL insert_item_by_code(10, 'KHOA51', 8);
CALL insert_item_by_code(10, 'KHOA52', 21);
CALL insert_item_by_code(10, 'KHOA53', 83);
CALL insert_item_by_code(10, 'KHOA54', 183);
CALL insert_item_by_code(10, 'KHOA55', 61);
CALL insert_item_by_code(10, 'KHOA56', 49);
CALL insert_item_by_code(10, 'KHOA57', 811);
CALL insert_item_by_code(10, 'KHOA58', 56);
CALL insert_item_by_code(10, 'KHOA61', 35);
CALL insert_item_by_code(10, 'KHOA64', 100);
CALL insert_item_by_code(10, 'KHOA65', 218);
CALL insert_item_by_code(10, 'KHOA69', 158);
CALL insert_item_by_code(10, 'KHOA73', 148);
CALL insert_item_by_code(10, 'KHOA74', 500);
CALL insert_item_by_code(10, 'KHOA75', 231);
CALL insert_item_by_code(10, 'KHOA76', 215);
CALL insert_item_by_code(10, 'KHOA77', 158);
CALL insert_item_by_code(10, 'KHOA78', 43);
CALL insert_item_by_code(10, 'KHOA79', 1215);
CALL insert_item_by_code(10, 'KHOA80', 357);
CALL insert_item_by_code(10, 'KHOA103', 50);
CALL insert_item_by_code(10, 'KHOA81', 4980);
CALL insert_item_by_code(10, 'KHOA82', 1600);
CALL insert_item_by_code(10, 'KHOA83', 72);
CALL insert_item_by_code(10, 'KHOA84', 112);
CALL insert_item_by_code(10, 'KHOA85', 260);
CALL insert_item_by_code(10, 'KHOA86', 77);
CALL insert_item_by_code(10, 'KHOA88', 70);
CALL insert_item_by_code(10, 'KHOA90', 329);
CALL insert_item_by_code(10, 'KHOA91', 182);
CALL insert_item_by_code(10, 'KHOA92', 106);
CALL insert_item_by_code(10, 'KHOA95', 42);
CALL insert_item_by_code(10, 'KHOA96', 1110);
CALL insert_item_by_code(10, 'KHOA97', 42);
CALL insert_item_by_code(10, 'KHOA98', 48);
CALL insert_item_by_code(10, 'KHOA99', 83);
-- Nhóm MAC
CALL insert_item_by_code(10, 'MAC1', 13288);
CALL insert_item_by_code(10, 'MAC2', 18200);
CALL insert_item_by_code(10, 'MAC3', 1941);
CALL insert_item_by_code(10, 'MAC4', 3500);
CALL insert_item_by_code(10, 'MAC5', 2026);
CALL insert_item_by_code(10, 'MAC6', 5412);
CALL insert_item_by_code(10, 'MAC7', 6705);
CALL insert_item_by_code(10, 'MAC8', 5601);
CALL insert_item_by_code(10, 'MAC9', 3000);
CALL insert_item_by_code(10, 'MAC10', 11500);
CALL insert_item_by_code(10, 'MAC11', 5460);
CALL insert_item_by_code(10, 'MAC12', 8700);
CALL insert_item_by_code(10, 'MAC13', 900);
CALL insert_item_by_code(10, 'MAC14', 1870);
CALL insert_item_by_code(10, 'MAC15', 2250);
CALL insert_item_by_code(10, 'MAC16', 3870);
CALL insert_item_by_code(10, 'MAC17', 2960);
CALL insert_item_by_code(10, 'MAC18', 1840);
CALL insert_item_by_code(10, 'MAC19', 2200);
CALL insert_item_by_code(10, 'MAC20', 2290);
CALL insert_item_by_code(10, 'MAC21', 1160);
CALL insert_item_by_code(10, 'MAC22', 1460);
CALL insert_item_by_code(10, 'MAC23', 1170);
CALL insert_item_by_code(10, 'MAC24', 1100);
CALL insert_item_by_code(10, 'MAC25', 1057);
CALL insert_item_by_code(10, 'MAC26', 3010);
CALL insert_item_by_code(10, 'MAC27', 2160);
CALL insert_item_by_code(10, 'MAC28', 1720);
CALL insert_item_by_code(10, 'MAC29', 3340);
CALL insert_item_by_code(10, 'MAC30', 1590);
CALL insert_item_by_code(10, 'MAC31', 1346);
CALL insert_item_by_code(10, 'MAC32', 1050);
CALL insert_item_by_code(10, 'MAC33', 861);
CALL insert_item_by_code(10, 'MAC34', 1922);
CALL insert_item_by_code(10, 'MAC35', 1553);
CALL insert_item_by_code(10, 'MAC36', 284);
CALL insert_item_by_code(10, 'MAC37', 1404);
CALL insert_item_by_code(10, 'MAC38', 645);
CALL insert_item_by_code(10, 'MAC40', 2590);
CALL insert_item_by_code(10, 'MAC41', 1050);
CALL insert_item_by_code(10, 'MAC42', 2430);
CALL insert_item_by_code(10, 'MAC43', 2100);
CALL insert_item_by_code(10, 'MAC44', 2624);
CALL insert_item_by_code(10, 'MAC45', 2900);
CALL insert_item_by_code(10, 'MAC46', 2533);
CALL insert_item_by_code(10, 'MAC47', 700);
CALL insert_item_by_code(10, 'MAC48', 360);
CALL insert_item_by_code(10, 'MAC49', 900);
CALL insert_item_by_code(10, 'MAC50', 798);
CALL insert_item_by_code(10, 'MAC51', 1042);
CALL insert_item_by_code(10, 'MAC52', 727);
CALL insert_item_by_code(10, 'MAC53', 5519);
CALL insert_item_by_code(10, 'MAC54', 1567);
CALL insert_item_by_code(10, 'MAC55', 2118);
CALL insert_item_by_code(10, 'MAC56', 4292);
CALL insert_item_by_code(10, 'MAC57', 4800);
CALL insert_item_by_code(10, 'MAC58', 5000);
CALL insert_item_by_code(10, 'MAC59', 4930);
CALL insert_item_by_code(10, 'MAC60', 789);
CALL insert_item_by_code(10, 'MAC61', 1310);
CALL insert_item_by_code(10, 'MAC62', 2090);
CALL insert_item_by_code(10, 'MAC63', 11);
CALL insert_item_by_code(10, 'MAC64', 1000);
CALL insert_item_by_code(10, 'MAC65', 1000);
CALL insert_item_by_code(10, 'MAC66', 709);
CALL insert_item_by_code(10, 'MAC67', 709);
CALL insert_item_by_code(10, 'MAC68', 709);
-- Nhóm KHUY
CALL insert_item_by_code(10, 'KHUY1', 22);
CALL insert_item_by_code(10, 'KHUY3', 16);
CALL insert_item_by_code(10, 'KHUY5', 56);
CALL insert_item_by_code(10, 'KHUY6', 31);
CALL insert_item_by_code(10, 'KHUY7', 34);
CALL insert_item_by_code(10, 'KHUY11', 14);
CALL insert_item_by_code(10, 'KHUY12', 8);
-- Nhóm MEX
CALL insert_item_by_code(10, 'MEX1', 52);
CALL insert_item_by_code(10, 'MEX2', 190);
CALL insert_item_by_code(10, 'MEX3', 9);
CALL insert_item_by_code(10, 'MEX4', 3);
CALL insert_item_by_code(10, 'MEX5', 415);
CALL insert_item_by_code(10, 'MEX7', 2230);
CALL insert_item_by_code(10, 'MEX8', 470);
-- Nhóm ĐV, NI, LQ
CALL insert_item_by_code(10, 'ĐV1', 531);
CALL insert_item_by_code(10, 'ĐV2', 56);
CALL insert_item_by_code(10, 'NI1', 105);
CALL insert_item_by_code(10, 'NI2', 308);
CALL insert_item_by_code(10, 'NI3', 118);
CALL insert_item_by_code(10, 'NI4', 200);
CALL insert_item_by_code(10, 'NI5', 1252);
CALL insert_item_by_code(10, 'LQ1', 280);
CALL insert_item_by_code(10, 'LQ3', 250);
CALL insert_item_by_code(10, 'LQ4', 4300);
CALL insert_item_by_code(10, 'LQ5', 2700);
-- Nhóm LOT
CALL insert_item_by_code(10, 'LOT1', 1188);
CALL insert_item_by_code(10, 'LOT2', 2061);
CALL insert_item_by_code(10, 'LOT3', 1500);
CALL insert_item_by_code(10, 'LOT4', 654);
CALL insert_item_by_code(10, 'LOT5', 1480);
CALL insert_item_by_code(10, 'LOT10', 757);
CALL insert_item_by_code(10, 'LOT11', 155);
CALL insert_item_by_code(10, 'LOT12', 3064);
CALL insert_item_by_code(10, 'LOT13', 2900);
CALL insert_item_by_code(10, 'LOT15', 5727);
CALL insert_item_by_code(10, 'LOT16', 2250);
CALL insert_item_by_code(10, 'LOT17', 830);
CALL insert_item_by_code(10, 'LOT18', 1017);
CALL insert_item_by_code(10, 'LOT19', 4910);
CALL insert_item_by_code(10, 'LOT20', 950);
CALL insert_item_by_code(10, 'LOT21', 1650);
CALL insert_item_by_code(10, 'LOT22', 519);
CALL insert_item_by_code(10, 'LOT23', 454);
CALL insert_item_by_code(10, 'LOT25', 53);
CALL insert_item_by_code(10, 'LOT26', 174);
CALL insert_item_by_code(10, 'LOT27', 1011);
CALL insert_item_by_code(10, 'LOT29', 285);
CALL insert_item_by_code(10, 'LOT30', 26);
CALL insert_item_by_code(10, 'LOT32', 615);
CALL insert_item_by_code(10, 'LOT34', 1070);
CALL insert_item_by_code(10, 'LOT37', 1890);
-- Nhóm NHAM, TB, K
CALL insert_item_by_code(10, 'NHAM1', 41);
CALL insert_item_by_code(10, 'NHAM2', 9);
CALL insert_item_by_code(10, 'K1', 7000);
CALL insert_item_by_code(10, 'K2', 300);
CALL insert_item_by_code(10, 'K3', 7);
CALL insert_item_by_code(10, 'K5', 8);
CALL insert_item_by_code(10, 'K6', 2);
CALL insert_item_by_code(10, 'K7', 5);
CALL insert_item_by_code(10, 'K8', 195);
CALL insert_item_by_code(10, 'K9', 700);
CALL insert_item_by_code(10, 'K10', 700);
CALL insert_item_by_code(10, 'K11', 585);
CALL insert_item_by_code(10, 'K12', 7400);
CALL insert_item_by_code(10, 'K13', 1600);
CALL insert_item_by_code(10, 'K14', 230);
CALL insert_item_by_code(10, 'K15', 1489);
CALL insert_item_by_code(10, 'K16', 940);
CALL insert_item_by_code(10, 'K17', 750);
CALL insert_item_by_code(10, 'K18', 800);
CALL insert_item_by_code(10, 'K19', 818);
CALL insert_item_by_code(10, 'K20', 863);
CALL insert_item_by_code(10, 'K21', 828);
CALL insert_item_by_code(10, 'K22', 100);
CALL insert_item_by_code(10, 'K23', 189);
CALL insert_item_by_code(10, 'K24', 200);
CALL insert_item_by_code(10, 'K25', 200);
CALL insert_item_by_code(10, 'K26', 2);

-- =====================================================
-- PHẦN 8: ORDERS (Dữ liệu mẫu - Lark integration G1+)
-- =====================================================
-- 7 seed orders cũ (contract_reports) đã được thay bằng Lark Excel import data.
--
-- Để import 19 đơn hàng test từ Lark Excel:
--   cd scripts/lark-import
--   npm install
--   node generate-import-sql.js
--   mysql -u root -p hangfashion_inventory < lark-test-data.sql
--
-- Sau import: 25 customers + 19 orders + 237 order_items với seed_source='LARK_TEST'.
-- Rollback: mysql ... < lark-test-rollback.sql
--
-- Tham khảo: scripts/lark-import/README.md

-- =====================================================
-- PHẦN 9: STORED PROCEDURE - TẠO CHILD PRODUCT
-- =====================================================
DELIMITER //

DROP PROCEDURE IF EXISTS create_child_product//
CREATE PROCEDURE create_child_product(
    IN p_parent_id BIGINT,
    IN p_product_name VARCHAR(255),
    IN p_note TEXT
)
BEGIN
    DECLARE v_new_product_id BIGINT;
    DECLARE v_sibling_id BIGINT;

    -- Tìm sibling đầu tiên (child cùng parent đã có variants)
    SELECT p.product_id INTO v_sibling_id
    FROM products p
    WHERE p.parent_product_id = p_parent_id
      AND EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.product_id)
    LIMIT 1;

    IF v_sibling_id IS NULL THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No sibling with variants found for cloning';
    END IF;

    -- Tạo product mới
    INSERT INTO products (product_name, variant_type, note, parent_product_id, created_at)
    VALUES (p_product_name, 'STRUCTURED', p_note, p_parent_id, NOW());

    SET v_new_product_id = LAST_INSERT_ID();

    -- Clone variants từ sibling (thay product_id, giữ nguyên style/size/length/gender)
    INSERT INTO product_variants (product_id, style_id, size_id, length_type_id, gender)
    SELECT v_new_product_id, pv.style_id, pv.size_id, pv.length_type_id, pv.gender
    FROM product_variants pv
    WHERE pv.product_id = v_sibling_id;

    SELECT v_new_product_id AS new_product_id;
END//

DELIMITER ;

-- =====================================================
-- PHẦN 9b: NHẬP KHO TRƯỜNG — Clone dữ liệu Vải (Product 8)
-- =====================================================

-- Tạo request set cho nhập kho Trường
INSERT INTO request_sets (set_name, created_by, created_at) VALUES
('Nhập kho Trường - Vải ban đầu', NULL, '2025-06-20 00:00:00');

SET @truong_set_id = LAST_INSERT_ID();

-- Tạo inventory request cho product 8, warehouse_id = 2 (Kho Trường)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at, warehouse_id)
VALUES (@truong_set_id, NULL, 8, 'IN', 'Nhập kho Trường ban đầu', '2025-06-20 00:00:00', 2);

SET @truong_request_id = LAST_INSERT_ID();

-- Clone items từ Kho Chính sang Kho Trường (số lượng nhỏ hơn ~30-50%)
INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @truong_request_id, pv.variant_id, CASE pv.item_code
    WHEN 'B1'  THEN 180
    WHEN 'B2'  THEN 120
    WHEN 'B4'  THEN 50
    WHEN 'B7'  THEN 150
    WHEN 'B9'  THEN 70
    WHEN 'B11' THEN 60
    WHEN 'B12' THEN 100
    WHEN 'B13' THEN 50
    WHEN 'B15' THEN 130
    WHEN 'B16' THEN 90
    WHEN 'B17' THEN 120
    WHEN 'B18' THEN 50
    WHEN 'B19' THEN 100
    WHEN 'B20' THEN 70
    WHEN 'B21' THEN 40
    WHEN 'B22' THEN 80
    WHEN 'B23' THEN 60
    WHEN 'B24' THEN 35
    WHEN 'B25' THEN 25
    WHEN 'B26' THEN 200
    WHEN 'B27' THEN 150
    WHEN 'B28' THEN 130
    WHEN 'B29' THEN 100
    WHEN 'B30' THEN 120
    WHEN 'B31' THEN 80
    WHEN 'B32' THEN 65
    WHEN 'B33' THEN 100
    WHEN 'B34' THEN 90
    WHEN 'B35' THEN 15
    WHEN 'B37' THEN 50
    WHEN 'B39' THEN 40
    ELSE 0
END
FROM product_variants pv
WHERE pv.product_id = 8
  AND pv.item_code IN ('B1','B2','B4','B7','B9','B11','B12','B13','B15','B16','B17','B18','B19','B20','B21','B22','B23','B24','B25','B26','B27','B28','B29','B30','B31','B32','B33','B34','B35','B37','B39');

-- Set status EXECUTED
UPDATE request_sets SET status = 'EXECUTED', submitted_at = created_at WHERE set_id = @truong_set_id;

-- =====================================================
-- PHẦN 10: ACCESSORY TEMPLATES (V15)
-- =====================================================

-- Bổ sung variants còn thiếu (INSERT IGNORE: bỏ qua nếu đã có)
INSERT IGNORE INTO product_variants (product_id, item_code, item_name, unit)
VALUES
    (10, 'TUIBONG1', 'Túi bóng kính', 'chiếc');

-- 3 accessory templates
INSERT INTO accessory_templates (name, created_by, created_at)
VALUES
    ('Quần BH nam',      NULL, NOW()),
    ('Áo budong nam NT', NULL, NOW()),
    ('Quần NT nam',      NULL, NOW());

-- Template items: Quần BH nam
INSERT INTO accessory_template_items (template_id, variant_id, item_code, item_name, rate, unit, sort_order)
SELECT t.id, pv.variant_id, pv.item_code, v.item_name, v.rate, v.unit, v.sort_order
FROM accessory_templates t
JOIN (
    SELECT 'KHOA4' AS code, 'Khóa quần ghi'                           AS item_name, 1.0000 AS rate, 'chiếc' AS unit, 0 AS sort_order
    UNION ALL SELECT 'MEX5',     'Mex mè đen (Khổ 1m)',               0.0100, 'mét', 1
    UNION ALL SELECT 'MEX6',     'Mex cạp quần nam',                  0.0350, 'mét', 2
    UNION ALL SELECT 'LOT10',    'Lót túi kate đen (Làm BH) (khổ 1.5m)', 0.3500, 'mét', 3
    UNION ALL SELECT 'NHAM1',    'Nhám dính ghi',                     0.0600, 'mét', 4
    UNION ALL SELECT 'K3',       'Dây phản quang 2cm không in',       0.3600, 'mét', 5
    UNION ALL SELECT 'K5',       'Chun 3F',                           0.1800, 'mét', 6
) v ON TRUE
JOIN product_variants pv ON pv.product_id = 10 AND pv.item_code = v.code
WHERE t.name = 'Quần BH nam';

-- Template items: Áo budong nam NT
INSERT INTO accessory_template_items (template_id, variant_id, item_code, item_name, rate, unit, sort_order)
SELECT t.id, pv.variant_id, pv.item_code, v.item_name, v.rate, v.unit, v.sort_order
FROM accessory_templates t
JOIN (
    SELECT 'MAC1'      AS code, 'Mác sơ mi nam Hằng'        AS item_name, 1.0000  AS rate, 'chiếc' AS unit, 0 AS sort_order
    UNION ALL SELECT 'KHUY1',   'Khuy áo ngoài trời',        19.0000, 'chiếc', 1
    UNION ALL SELECT 'K4',      'Dây phản quang có in VNPT',  1.1000, 'mét',   2
    UNION ALL SELECT 'TUIBONG1','Túi bóng kính',              1.0000, 'chiếc', 3
) v ON TRUE
JOIN product_variants pv ON pv.product_id = 10 AND pv.item_code = v.code
WHERE t.name = 'Áo budong nam NT';

-- Template items: Quần NT nam
INSERT INTO accessory_template_items (template_id, variant_id, item_code, item_name, rate, unit, sort_order)
SELECT t.id, pv.variant_id, pv.item_code, v.item_name, v.rate, v.unit, v.sort_order
FROM accessory_templates t
JOIN (
    SELECT 'KHOA3' AS code, 'Khóa quần xanh tươi'                        AS item_name, 1.0000 AS rate, 'chiếc' AS unit, 0 AS sort_order
    UNION ALL SELECT 'MEX6',     'Mex cạp quần nam',                      0.0350, 'mét', 1
    UNION ALL SELECT 'MEX5',     'Mex mè đen',                            0.0100, 'mét', 2
    UNION ALL SELECT 'LOT13',    'Lót túi kate trắng (cắt sẵn) - NT nam', 1.0000, 'bộ',  3
    UNION ALL SELECT 'K4',       'Dây phản quang có in VNPT',             0.4000, 'mét', 4
    UNION ALL SELECT 'K5',       'Chun 3F',                               0.1800, 'mét', 5
    UNION ALL SELECT 'NHAM3',    'Nhám dính xanh tươi',                   0.0600, 'mét', 6
) v ON TRUE
JOIN product_variants pv ON pv.product_id = 10 AND pv.item_code = v.code
WHERE t.name = 'Quần NT nam';

-- =====================================================
-- PHẦN 11: CLEANUP - XÓA PROCEDURE SAU KHI IMPORT
-- =====================================================
DROP PROCEDURE IF EXISTS insert_item_by_variant;
DROP PROCEDURE IF EXISTS insert_item_by_gender;
DROP PROCEDURE IF EXISTS insert_item_by_gender_length;
DROP PROCEDURE IF EXISTS insert_item_by_code;
-- Lưu ý: KHÔNG xóa create_child_product vì cần dùng runtime

-- =====================================================
-- HOÀN TẤT IMPORT DATABASE
-- =====================================================
SELECT 'Import completed successfully!' AS status;
SELECT COUNT(*) AS total_styles FROM styles;
SELECT COUNT(*) AS total_sizes FROM sizes;
SELECT COUNT(*) AS total_length_types FROM length_types;
SELECT COUNT(*) AS total_variants FROM product_variants;
SELECT COUNT(*) AS total_units FROM units;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_request_sets FROM request_sets;
SELECT COUNT(*) AS total_requests FROM inventory_requests;
SELECT COUNT(*) AS total_items FROM inventory_request_items;
SELECT COUNT(*) AS total_customers FROM customers;
SELECT COUNT(*) AS total_orders FROM orders;
SELECT COUNT(*) AS total_order_items FROM order_items;

-- END OF FILE
