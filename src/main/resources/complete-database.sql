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
    -- MEASUREMENT detail (V21, G3) - 6 mốc ngày chi tiết từ Excel
    customer_registration_sent_date     DATE,
    tech_book_return_date               DATE,
    measurement_received_from_tech_date DATE,
    list_sent_to_customer_date          DATE,
    list_finalized_date                 DATE,
    measurement_handover_date_v2        DATE,
    measurement_taker_user_id           BIGINT NULL,
    measurement_composer_user_id        BIGINT NULL,
    -- Files (V21, G3) - StorageService URLs
    contract_file_url             VARCHAR(500),
    handover_record_url           VARCHAR(500),
    liquidation_record_url        VARCHAR(500),
    customer_measurement_file_url VARCHAR(500),
    -- NPL proposal (V23, G5) — Bản đề xuất phụ liệu cấp đơn
    npl_proposal_url              VARCHAR(500),
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
    FOREIGN KEY (measurement_taker_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (measurement_composer_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
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
-- DESIGN PHASE (V22, G4, W11-12): Samples per item + Documents per order
-- Ref: docs/lark-integration-roadmap.md §G4
-- Auto design_ready flag when all items have APPROVED sample.
-- =====================================================

-- 2.27 Bảng design_samples (Mẫu thiết kế per OrderItem)
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

-- 2.28 Bảng design_documents (Tài liệu thiết kế cấp đơn)
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

-- =====================================================
-- TAILOR MODULE (V23, G5, W13-14)
-- Ref: docs/lark-integration-roadmap.md §G5
-- =====================================================

-- 2.29 Bảng tailors (Thợ may — master data)
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

-- 2.30 Bảng tailor_assignments (Giao thợ per OrderItem)
CREATE TABLE tailor_assignments (
    assignment_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id      BIGINT NOT NULL,
    tailor_id          BIGINT NOT NULL,
    qty_assigned       INT NOT NULL DEFAULT 0,
    qty_from_stock     INT NOT NULL DEFAULT 0,
    qty_returned       INT NOT NULL DEFAULT 0,
    appointment_date   DATE,
    returned_date      DATE,
    tailor_type        ENUM('CUT_SEW','FINISHING') NULL,
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

-- =====================================================
-- KCS PHASE (V25, G7, W16-17)
-- Ref: docs/lark-integration-roadmap.md §G7
-- Auto qc_passed flag when all items have PASSED quality check.
-- =====================================================

-- 2.31 Bảng quality_checks (Kiểm tra chất lượng per OrderItem)
CREATE TABLE quality_checks (
    qc_id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_item_id          BIGINT NOT NULL,
    tailor_assignment_id   BIGINT NULL,
    kcs_user_id            BIGINT NULL,
    received_date          DATE,
    completed_date         DATE,
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

-- =====================================================
-- PACKING PHASE (V26, G8, W19)
-- Ref: docs/lark-integration-roadmap.md §G8
-- =====================================================

-- 2.32 Bảng packing_batches (Đóng hàng per Order)
CREATE TABLE packing_batches (
    packing_batch_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id                BIGINT NOT NULL,
    packer_user_id          BIGINT NULL,
    documents_received_date DATE,
    packing_started_date    DATE,
    packing_completed_date  DATE,
    expected_delivery_date  DATE,
    contract_delivery_date  DATE,
    actual_delivery_date    DATE,
    delivery_status         ENUM('NOT_DELIVERED','DELIVERED','PARTIAL')
                            NOT NULL DEFAULT 'NOT_DELIVERED',
    tick_file_url           VARCHAR(500),
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

-- 2.33 Bảng missing_items (Hàng thiếu per batch)
CREATE TABLE missing_items (
    missing_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_batch_id      BIGINT NOT NULL,
    order_item_id         BIGINT NOT NULL,
    missing_quantity      INT NOT NULL DEFAULT 0,
    missing_list_file_url VARCHAR(500),
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

-- =====================================================
-- REPAIR MODULE (V27, G9, W20) — Hàng quay đầu
-- Ref: docs/lark-integration-roadmap.md §G9
-- =====================================================

-- 2.34 Bảng repair_requests
CREATE TABLE repair_requests (
    repair_id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_batch_id           BIGINT NULL,
    order_item_id              BIGINT NOT NULL,
    batch_number               VARCHAR(50),
    received_date              DATE,
    receiver_user_id           BIGINT NULL,
    receive_method             ENUM('POSTAL','DIRECT','COURIER') NULL,
    expected_completion_date   DATE,
    qty_repair                 INT NOT NULL DEFAULT 0,
    repair_details             TEXT,
    return_date                DATE,
    return_method              ENUM('POSTAL','DIRECT','COURIER') NULL,
    return_handler_user_id     BIGINT NULL,
    parent_batches             TEXT,
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

-- =====================================================
-- GUARANTEES (V28, G2b, W21)
-- Ref: docs/lark-integration-roadmap.md §G2b
-- Optional per order — chỉ khách đặc biệt.
-- =====================================================

-- 2.35 Bảng guarantees (Bảo lãnh — 3 loại: dự thầu / thực hiện HĐ / bảo hành)
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

-- =====================================================
-- WAREHOUSE LINK (V24, G6, W15) — DANGER ZONE
-- Nullable FK từ receipt_records → orders.order_items, receipt_items → tailor_assignments.
-- Phải ALTER sau khi order_items + tailor_assignments đã create.
-- Ref: docs/lark-integration-roadmap.md §G6
-- =====================================================
ALTER TABLE receipt_records
    ADD COLUMN order_item_id BIGINT NULL,
    ADD CONSTRAINT fk_receipt_order_item
        FOREIGN KEY (order_item_id) REFERENCES order_items(order_item_id)
        ON DELETE SET NULL,
    ADD INDEX idx_receipt_order_item (order_item_id);

ALTER TABLE receipt_items
    ADD COLUMN tailor_assignment_id BIGINT NULL,
    ADD CONSTRAINT fk_receipt_item_assignment
        FOREIGN KEY (tailor_assignment_id) REFERENCES tailor_assignments(assignment_id)
        ON DELETE SET NULL,
    ADD INDEX idx_ri_assignment (tailor_assignment_id);

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
('NHẬP XUẤT VẢI 2026', 'ITEM_BASED', '338 mã vải (CSV CÔNG TY)', '2026-01-01 00:00:00'),
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

-- Product 8: NHẬP XUẤT VẢI 2026 (338 mã — ITEM_BASED, nguồn CSV CÔNG TY)
INSERT INTO product_variants (product_id, item_code, item_name, unit) VALUES
(8, 'B1', 'Trắng kem nam', 'mét'),
(8, 'B11', 'Kaky ghi nam (2021 Grey)', 'mét'),
(8, 'B12', 'Ghi nữ', 'mét'),
(8, 'B13', 'Kaky ghi nữ', 'mét'),
(8, 'B15', 'Vải áo khai thác BĐ - Oxford ghi nhạt (chính)', 'mét'),
(8, 'B16', 'Vải áo khai thác BĐ - Oxford vàng (phối)', 'mét'),
(8, 'B17', 'Vải áo khoác gió vàng bưu điện (chính)', 'mét'),
(8, 'B18', 'Vải áo khoác gió ghi đậm bưu điện (phối)', 'mét'),
(8, 'B19', 'Vải áo khoác gió ghi nhạt bưu điện (chính)', 'mét'),
(8, 'B2', 'Trắng kem nữ', 'mét'),
(8, 'B22', 'Vải áo chống nắng màu vàng (chính)', 'kilogam'),
(8, 'B23', 'Vải áo chống nắng màu ghi (phối)', 'kilogam'),
(8, 'B24', 'Vải áo gile kaky màu vàng (chính)', 'mét'),
(8, 'B25', 'Vải áo gile kaky màu ghi (phối)', 'mét'),
(8, 'B26', 'Vải áo gile lưới', 'mét'),
(8, 'B27', 'Vải viền lé vàng', 'mét'),
(8, 'B28', 'Vải quần áo dài bưu điện (xanh)', 'mét'),
(8, 'B29', 'Ghi nam lãnh đạo bưu điện', 'mét'),
(8, 'B30', 'Ghi nữ lãnh đạo bưu điện', 'mét'),
(8, 'B31', 'Vải lót vàng áo khoác gió bưu điện', 'mét'),
(8, 'B32', 'Vải áo chống nắng bưu điện (Vĩnh Phúc, Ninh Bình)', 'mét'),
(8, 'B33', 'Vải lót lưới vàng', 'kilogam'),
(8, 'B34', 'Vải xanh tím đậm (may cho Bưu điện )', 'mét'),
(8, 'B35', 'Vải xanh bảo vệ bưu điện', 'mét'),
(8, 'B36', 'Vải áo khoác gió vàng bưu điện mã T400', 'mét'),
(8, 'B37', 'Vải áo khoác gió xám bưu điện mã T400 (Ghi xám nhạt)', 'mét'),
(8, 'B38', 'Vải áo khai thác BĐ - DT680 ghi nhạt (chính)', 'mét'),
(8, 'B39', 'Vải áo khai thác BĐ - DT680 vàng (phối)', 'mét'),
(8, 'B4', 'Trắng kem nam TCT BĐ (2020)', 'mét'),
(8, 'B40', 'Vải quần ghi mã TS - S9012', 'mét'),
(8, 'B7', 'Ghi nam', 'mét'),
(8, 'B41', 'Vải gió bưu điện mã T400 (ghi đậm)', 'mét'),
(8, 'B9', 'Ghi nam LĐ tổng Vnpost', 'mét'),
(8, 'H10', 'Vải quần tím than EMS nhóm II', 'mét'),
(8, 'H11', 'Hoa Vinh', 'mét'),
(8, 'H12', 'Mã 2021', 'mét'),
(8, 'H19', 'Mã 3311 (sơ mi trắng)', 'mét'),
(8, 'H2', 'Sơ mi trắng mã 498-1 (Dùng cho TCT Bưu điện)', 'mét'),
(8, 'H21', 'Mã 1816 (sơ mi trắng)', 'mét'),
(8, 'H24', 'Mã 5039 (sơ mi trắng)', 'mét'),
(8, 'H25', 'Vải Ông Sáng - Mã Y31129 - 113m', 'mét'),
(8, 'H26', 'Vải Ông Sáng - Mã 2051 - 129,9m', 'mét'),
(8, 'H27', 'Vải Ông Sáng - Mã Y31129 - 129,9m', 'mét'),
(8, 'H28', 'Vải Ông Sáng - Mã Y31191 - 112,9m', 'mét'),
(8, 'H29', 'Vải Ông Sáng - Mã Y31191 - 109,9m', 'mét'),
(8, 'H3', 'M1 - Vải quần nam IT (GA002-Q7T60)', 'mét'),
(8, 'H30', 'Vải Ông Sáng - Mã Y31129 - 131,9m', 'mét'),
(8, 'H31', 'Vải Ông Sáng - Mã 3129 - 112,6m', 'mét'),
(8, 'H32', 'Vải Ông Sáng - Mã Y31191 - 113,8m', 'mét'),
(8, 'H33', 'Vải Ông Sáng - Mã 3081 - 131,8m', 'mét'),
(8, 'H34', 'Vải Ông Sáng - Mã J2691 - 137,4m', 'mét'),
(8, 'H35', 'Vải Ông Sáng - Mã Y31191 - 106,2m', 'mét'),
(8, 'H36', 'Vải Ông Sáng - Mã Y31191 - 109,4m', 'mét'),
(8, 'H37', 'Vải Ông Sáng - Mã 3081 - 135,7m', 'mét'),
(8, 'H38', 'Vải Ông Sáng - Mã Y31191 - 112,9m', 'mét'),
(8, 'H39', 'Vải mới cô Hằng không có mã', 'mét'),
(8, 'H4', 'M2 - Vải quần nam IT (GA0001-08T79)', 'mét'),
(8, 'H40', 'Mã 3081', 'mét'),
(8, 'H41', 'Mã 300227 (Viettin bank)', 'mét'),
(8, 'H42', 'JP180252 (xanh nhạt)', 'mét'),
(8, 'H43', 'HTS03225-3 (xanh đậm)', 'mét'),
(8, 'H44', 'BT01791-3 (tím nhạt)', 'mét'),
(8, 'H45', 'Vải rủ tím than chị Sáng mua (đậm)', 'mét'),
(8, 'H46', 'Vải rủ tím than chị Sáng mua (nhạt)', 'mét'),
(8, 'H47', 'Vải quần mã T9069-1', 'mét'),
(8, 'H48', 'Vải phối caro (dùng cho Than Thống Nhất)', 'mét'),
(8, 'H49', 'Vải gió 75D-T400-55', 'mét'),
(8, 'H5', 'Áo khoác gió phòng máy  (50D-138)', 'mét'),
(8, 'H50', 'Vải quần nam Indovina 2025 (TSVT-GA0011-1)', 'mét'),
(8, 'H51', 'Vải quần nữ Indovina 2025 (TSVT-GA0014-1)', 'mét'),
(8, 'H52', 'Ghi nữ rủ (vải rủ BĐ cũ)', 'mét'),
(8, 'H53', 'Vải lẻ dùng cho VT Kon Tum', 'mét'),
(8, 'H54 - 0007', 'Vải kẻ dùng cho IT - mã GA0007 - X02 - 21 (M9)', 'mét'),
(8, 'H54 - 004-6', 'Vải dùng cho IT - mã GA0004-6 (M7)', 'mét'),
(8, 'H54 - 0201', 'Vải dùng cho IT - mã GA0001 - 0201 (M6)', 'mét'),
(8, 'H54 - 08T79', 'Vải dùng cho IT - mã GA0002 - 08T79 (M8)', 'mét'),
(8, 'H54 - Q7T60', 'Vải dùng cho IT - mã GA0001 - Q7T60 (M5)', 'mét'),
(8, 'H54 - W0101', 'Vải dùng cho IT - mã GA0009 - W0101(M1)', 'mét'),
(8, 'H54 - W0306', 'Vải dùng cho IT - mã GA0009 - W0306 (M2)', 'mét'),
(8, 'H54 - W04U5', 'Vải dùng cho IT - Ghi xám - mã GA0009 - W04U5 (M3)', 'mét'),
(8, 'H54 - W0508', 'Vải dùng cho IT - mã GA0009 - W0508 (M4)', 'mét'),
(8, 'H6', 'Vải gió tím than mã 1956-42', 'mét'),
(8, 'H7', 'Vải gió (VT Thái Nguyên) mã T600 - 64', 'mét'),
(8, 'H8', 'Mobiphone toàn cầu', 'mét'),
(8, 'K1', 'Áo dài than Nam Mẫu', 'mét'),
(8, 'K15', 'EMS (kaky nam xanh tím than)', 'mét'),
(8, 'K16', 'EMS (kaky nữ tím than mới)', 'mét'),
(8, 'K17', 'Vải áo budong EMS', 'mét'),
(8, 'K18', 'EMS (Vải áo gió tím than)', 'mét'),
(8, 'K2', 'Sơ mi nữ EMS 2025 (BT02572)', 'mét'),
(8, 'K9', 'Vải quần nữ Indovina mã S9023-1 (đen)', 'mét'),
(8, 'K10', 'Vải quần nam Indovina mã A600K-UNI-1 (đen)', 'mét'),
(8, 'K20', 'Vải áo dài cam Thái Tuấn', 'mét'),
(8, 'K23', 'HUD (Vải áo dài đỏ)', 'mét'),
(8, 'K27', 'Trắng SM nam Tập đoàn EVN 2020', 'mét'),
(8, 'K3', 'Than Nam Mẫu 2020 (xanh SM nam)', 'mét'),
(8, 'K31', 'SM nam NV Oceanbank (xanh)', 'mét'),
(8, 'K39', 'LS70083 - ZY23083', 'mét'),
(8, 'K40', 'S6036 - ZY23083', 'mét'),
(8, 'K41', 'EMS (xanh SM nam mới 2021)', 'mét'),
(8, 'K42', 'Vải gió IT 2021', 'mét'),
(8, 'K43', 'Vải quần nữ EVN Cao Bằng', 'mét'),
(8, 'K44', 'Trắng SM nam MTC 1309', 'mét'),
(8, 'K46', 'Vải quần nam Học viện Bưu chính', 'mét'),
(8, 'K47', 'Vải rủ đen', 'mét'),
(8, 'K48', 'Trắng SM nam mã 033 trắng kem', 'mét'),
(8, 'K49', 'Trắng SM nam mã 033 trắng tinh', 'mét'),
(8, 'K5', 'Than Uông Bí 2019 (xanh SM nam)', 'mét'),
(8, 'K51', 'Trắng SM nam mã LHT21063', 'mét'),
(8, 'K56', 'Vải quần nam mã HS-25067', 'mét'),
(8, 'K57', 'Vải SM mã JP180249', 'mét'),
(8, 'K58', 'Vải quần mã 2183 - ZY23081', 'mét'),
(8, 'K59', 'Vải quần mã 2183 - ZY23083', 'mét'),
(8, 'K6', 'Than Uông Bí 2019 (xanh SM nữ)', 'mét'),
(8, 'K60', 'Xanh SM mã BT1443', 'mét'),
(8, 'K61', 'Vải áo gió Ban KTM 2023', 'mét'),
(8, 'K62', 'Kaky cam Thủy điện Hòa Bình', 'mét'),
(8, 'K63', 'Vải trắng SM mã HS210420TP', 'mét'),
(8, 'K64', 'Vải quần nam Habeco (Mã 467T)', 'mét'),
(8, 'K65', 'Vải quần mã AP03X - UNI- Q801', 'mét'),
(8, 'K66', 'Xanh SM mã BT02564', 'mét'),
(8, 'K67', 'Trắng SM nữ mã 1817', 'mét'),
(8, 'K68', 'Trắng SM nữ mã 2235-1', 'mét'),
(8, 'K69', 'Vải quần đen mã 124', 'mét'),
(8, 'K70', 'Vải SM mã BA0053', 'mét'),
(8, 'K71', 'Vải SM mã BA0046', 'mét'),
(8, 'K72', 'Vải SM mã BTS03331-3', 'mét'),
(8, 'K74', 'Trắng SM mã 3590-1 (TTKD)', 'mét'),
(8, 'K75', 'Trắng SM nữ mã 03399SF', 'mét'),
(8, 'K76', 'Trắng SM nam mã MTS03400 - 1', 'mét'),
(8, 'K77', 'Vải quần mã TSL7715 - 10', 'mét'),
(8, 'K78', 'Vải áo sơ mi trắng (Chị Nga mua - chuyển sang làm vải áo sơ mi)', 'mét'),
(8, 'K79', 'Vải quần vest nam Thủy điện Hòa Bình', 'mét'),
(8, 'K8', 'EMS (sơ mi nữ mới)', 'mét'),
(8, 'K80', 'Vải áo dài Thủy điện Hòa Bình (Đỏ hơi đậm)', 'mét'),
(8, 'K81', 'Vải quần của áo dài Thủy điện Hòa Bình', 'mét'),
(8, 'K82', 'Vải áo dài cho Bệnh viện Bưu điện', 'mét'),
(8, 'K83', 'Vải áo dài Học viện Bưu chính Viễn thông', 'mét'),
(8, 'K84', 'Vải quần áo dài Học viện Bưu chính Viễn thông', 'mét'),
(8, 'K85', 'Vải quần áo dài Bệnh viện Bưu điện', 'mét'),
(8, 'K86', 'Vải SM trắng Indovina mã 241-1', 'mét'),
(8, 'K87', 'Vải trắng SM mã KT-31752-K1', 'mét'),
(8, 'K88', 'Vải SM trắng  mã SM-INDO 241-1', 'mét'),
(8, 'V1', 'Trắng tinh co giãn nữ SG', 'mét'),
(8, 'V11', 'Kẻ gân nam HLD', 'mét'),
(8, 'V16', 'C2 đậm', 'mét'),
(8, 'V17', 'C2 nhạt', 'mét'),
(8, 'V2', 'Kẻ co giãn tập đoàn (có gân) - Mã 02605', 'mét'),
(8, 'V20', 'Xanh tươi 4 chiều (đậm)', 'mét'),
(8, 'V25', 'Cháy 200', 'mét'),
(8, 'V28', 'Vải quần nam Nét 3', 'mét'),
(8, 'V29', 'C1 đậm', 'mét'),
(8, 'V3', 'Kẻ VP nữ giãn 1 mặt ông Sáng', 'mét'),
(8, 'V36', 'Gấm áo dài VNPT (loại thường)', 'mét'),
(8, 'V37', 'Quần áo dài', 'mét'),
(8, 'V39', 'NT 2721 đậm (2021)', 'mét'),
(8, 'V4', 'Kẻ Bamboo nữ giãn HDH39B', 'mét'),
(8, 'V40', 'C3 (2015) Tổng Nét', 'mét'),
(8, 'V43', 'Tím than 1301-35', 'mét'),
(8, 'V44', 'Tím than TR121-4', 'mét'),
(8, 'V47', 'Vải viền lé xanh', 'mét'),
(8, 'V48', 'Vải áo bảo vệ mới (2021)', 'mét'),
(8, 'V49', 'Vải áo chống nắng màu xanh', 'mét'),
(8, 'V5', 'Kẻ nữ sợi to - Mã BT02659', 'mét'),
(8, 'V50', 'Vải phối ngoài trời (mẫu mới 2021)', 'mét'),
(8, 'V51', 'Vải quần nữ tập đoàn (Mã 1 - chuẩn)', 'mét'),
(8, 'V52', 'Vải quần nữ C3 (Mã 2)', 'mét'),
(8, 'V53', 'LD60059-ZY23081', 'mét'),
(8, 'V54', 'Vải quần nam tím than (co giãn) - Mã 430', 'mét'),
(8, 'V55', 'Trắng tinh nam TTKD 2021', 'mét'),
(8, 'V57', 'S6036-ZY23081', 'mét'),
(8, 'V58', 'Vải áo phòng máy', 'mét'),
(8, 'V6', 'Vải rủ tím than dầy ( mã L72 cũ  )', 'mét'),
(8, 'V60', 'NT 2702 (2102)', 'mét'),
(8, 'V61', 'Vải quần nam IT 2022 (Mã 2183)', 'mét'),
(8, 'V62', 'Gấm áo dài VNPT (loại xịn)', 'mét'),
(8, 'V63', 'Vải áo tạp vụ', 'mét'),
(8, 'V65', 'Vải quần nam V6042', 'mét'),
(8, 'V66', 'Vải quần nữ V6030', 'mét'),
(8, 'V67', 'Vải rủ tím than mã 6135', 'mét'),
(8, 'V68', 'Trắng tinh nam TTKD mã 7100', 'mét'),
(8, 'V69', 'Trắng tinh nữ TTKD mã TRS03072', 'mét'),
(8, 'V7', 'Vải rủ tím than mỏng ( mã L90 cũ )', 'mét'),
(8, 'V70', 'Vải quần nam Tổng Nét 2023 mã L77101', 'mét'),
(8, 'V71', 'Vải quần nam V6046 - ZY23081', 'mét'),
(8, 'V72', 'Vải Kaky xanh NT Tổng Nét', 'mét'),
(8, 'V73', 'Kẻ nam mã HS-SMNVP-1', 'mét'),
(8, 'V74', 'Vải rủ tím than mã L9908', 'mét'),
(8, 'V75', 'Vải trắng nữ TTKD mã BA0052-1', 'mét'),
(8, 'V76', 'Vải quần nam mã T9010-UNI-10', 'mét'),
(8, 'V77', 'Vải quần nữ mã A634K-UNI-10', 'mét'),
(8, 'V78', 'Vải trắng nam TTKD mã BA0074', 'mét'),
(8, 'V79', 'Vải quần nam mã L88204-65', 'mét'),
(8, 'V8', 'Kẻ nam HDH24S', 'mét'),
(8, 'V80', 'NT 1202 (mẫu 2025)', 'mét'),
(8, 'V81', 'NT 6528 (vải áo điều hòa)', 'mét'),
(8, 'V82', 'Vải quần nữ mã AP02X-UNI-10', 'mét'),
(8, 'V83', 'Vải NT mã DT820 (may áo khoác NT)', 'mét'),
(8, 'V84', 'Vải sơ mi mã ATS03311-1', 'mét'),
(8, 'V85', 'Vải quần nam mã T8567J-UNI-1001', 'mét'),
(8, 'V86', 'Vải quần áo dài VNPT (loại xịn)', 'mét'),
(8, 'V87', 'Vải SM nam xanh gân mã SM150 (VT Hồ Chí Minh)', 'mét'),
(8, 'V88', 'Vải kẻ nam mã KT-30768', 'mét'),
(8, 'V89', 'Vải áo phông xanh mã KT-31088-1', 'mét'),
(8, 'V9', 'Kẻ nam HDH39C (mã cũ 2025 V7)', 'mét'),
(8, 'V10', 'Vải gió viễn thông loại 2 mã KT 31135-1', 'mét'),
(8, 'QU1', '33001-6-253', 'mét'),
(8, 'QU2', 'Y81909-8', 'mét'),
(8, 'QU3', '61665-Đen', 'mét'),
(8, 'QU4', '33001-2-253', 'mét'),
(8, 'QU5', 'Vải quần Kaky tím than', 'mét'),
(8, 'QU6', '33001-1-253', 'mét'),
(8, 'QU7', 'Vải quần xanh bộ đội', 'mét'),
(8, 'QU8', 'Kẻ ô ghi', 'mét'),
(8, 'QU9', '91665-1-253', 'mét'),
(8, 'QU10', 'ZJ232007-253', 'mét'),
(8, 'QU11', '91665-2-253', 'mét'),
(8, 'QU12', '91665-3-253', 'mét'),
(8, 'QU13', '91665-6-253', 'mét'),
(8, 'QU14', '91665-5-253', 'mét'),
(8, 'QU15', '91665-4-253', 'mét'),
(8, 'QU16', 'ZJ232010-253', 'mét'),
(8, 'QU17', '91665-21-253', 'mét'),
(8, 'QU18', 'Vải quần', 'mét'),
(8, 'QU19', 'Vải xanh', 'mét'),
(8, 'QU20', 'Kaky nâu nhạt', 'mét'),
(8, 'QU21', 'Quần nam màu rêu', 'mét'),
(8, 'QU22', 'Mã 70 đen', 'mét'),
(8, 'QU23', 'Vải quần tím than co giãn', 'mét'),
(8, 'QU24', 'Quần nam mã 70 Hà Long màu xanh đen', 'mét'),
(8, 'QU25', 'Cháy 200', 'mét'),
(8, 'QU26', 'Vải quần nữ tím than', 'mét'),
(8, 'QU27', 'Vải dạ tím than', 'mét'),
(8, 'QU28', 'Vải quần nam xanh', 'mét'),
(8, 'QU29', 'ZS9258-1 (Kaky)', 'mét'),
(8, 'QU30', 'ZS9258-12', 'mét'),
(8, 'QU31', 'ZS9258-14', 'mét'),
(8, 'QU32', '8319-J5125-27', 'mét'),
(8, 'QU33', '8319-J5125-9', 'mét'),
(8, 'QU34', '8319-J5125-14', 'mét'),
(8, 'QU35', '8319-J5125-1', 'mét'),
(8, 'QU36', '7851-13', 'mét'),
(8, 'QU37', '4542-3', 'mét'),
(8, 'QU38', '0610-5', 'mét'),
(8, 'QU39', '1265-11', 'mét'),
(8, 'QU40', '5360-8', 'mét'),
(8, 'QU41', '8319-J2125-32', 'mét'),
(8, 'QU42', '8319-J2125-21', 'mét'),
(8, 'QU43', '8319-J2125-31', 'mét'),
(8, 'QU44', '8319-J2125-2', 'mét'),
(8, 'QU45', '8319-J2125-10', 'mét'),
(8, 'QU46', '8319-J2125-0', 'mét'),
(8, 'QU47', '8319-J2125-23', 'mét'),
(8, 'QU48', '8319-J2125-11', 'mét'),
(8, 'QU49', '8319-J2125-5', 'mét'),
(8, 'QU50', '0396-10', 'mét'),
(8, 'QU51', '3609-7', 'mét'),
(8, 'QU52', '7268-10', 'mét'),
(8, 'QU53', '0-14', 'mét'),
(8, 'QU54', 'Quần nữ than Hồng Thái', 'mét'),
(8, 'QU55', 'P07209 (Kaky kem)', 'mét'),
(8, 'QU56', 'ZS 9258-4', 'mét'),
(8, 'QU57', 'ZS 9258-2', 'mét'),
(8, 'QU58', '8319-J2125', 'mét'),
(8, 'QU59', 'ZS 9258-6', 'mét'),
(8, 'QU60', 'ZS 9258-15', 'mét'),
(8, 'QU61', 'ZS 9258-16', 'mét'),
(8, 'QU62', '8319-J2125-4', 'mét'),
(8, 'QU63', '8319-J2125-8', 'mét'),
(8, 'QU64', 'ZS 9258-7', 'mét'),
(8, 'QU65', 'A290-5 (1)', 'mét'),
(8, 'QU66', '8319-J2125-13', 'mét'),
(8, 'QU67', 'A290-5 (2)', 'mét'),
(8, 'QU68', '8319-J2125-3', 'mét'),
(8, 'QU69', 'A290-20', 'mét'),
(8, 'QU70', 'A290-22', 'mét'),
(8, 'QU71', 'A290-6', 'mét'),
(8, 'QU72', '621-3-14 (Vải Golf Ghi đậm)', 'mét'),
(8, 'QU73', '621-2-22 (Vải Golf tím than)', 'mét'),
(8, 'QU74', '621-4-20 (Vải Golf Ghi nhạt)', 'mét'),
(8, 'QU75', '621-6-16 (Vải Golf Ghi sáng)', 'mét'),
(8, 'QU76', '621-1-18 (Vải Golf Đen)', 'mét'),
(8, 'QU77', 'ZJ232003 (Màu ghi)', 'mét'),
(8, 'SM1', 'Sơ mi ghi nam', 'mét'),
(8, 'SM2', 'Sơ mi trắng kẻ', 'mét'),
(8, 'SM3', 'Sơ mi nam mã 2021', 'mét'),
(8, 'SM4', '710-7# (trắng kem)', 'mét'),
(8, 'SM5', 'Sơ mi xanh', 'mét'),
(8, 'SM6', 'Sơ mi trắng tinh', 'mét'),
(8, 'SM7', 'Trắng xương cá', 'mét'),
(8, 'SM8', 'Sơ mi trắng kem', 'mét'),
(8, 'SM9', 'Sơ mi xanh tím nhạt', 'mét'),
(8, 'SM10', 'Sơ mi xanh khổ 1,25m (Viettin cũ)', 'mét'),
(8, 'SM11', 'Sơ mi kẻ nam - nữ (Tổng Nét)', 'mét'),
(8, 'SM12', 'Vải kẻ sọc thủy điện Sơn La', 'mét'),
(8, 'SM13', 'Sơ mi xanh than Nam Mẫu', 'mét'),
(8, 'SM14', 'Trắng xương cá to', 'mét'),
(8, 'SM15', 'Vải nâu nhạt', 'mét'),
(8, 'SM16', 'Sơ mi trắng tinh K150', 'mét'),
(8, 'SM17', 'Sơ mi kẻ nam', 'mét'),
(8, 'SM18', 'MTS 03400-34 (Nâu nhạt)', 'mét'),
(8, 'SM19', 'MTS 03400-71 (Ghi sáng)', 'mét'),
(8, 'SM20', 'MTS 03400-27 (Xanh nước biển)', 'mét'),
(8, 'SM21', 'MTS 03400-13 (Xanh lơ)', 'mét'),
(8, 'SM22', 'MTS 03400-5 (Đen)', 'mét'),
(8, 'SM23', 'MTS 03400-25', 'mét'),
(8, 'SM24', 'MTS 03400-14 (Xanh cổ vịt)', 'mét'),
(8, 'SM25', 'MTS 03400-24', 'mét'),
(8, 'SM26', 'MTS 03400-9', 'mét'),
(8, 'SM27', 'MTS 03400-23 (Đậm)', 'mét'),
(8, 'SM28', 'MTS 03400-23 (Nhạt)', 'mét'),
(8, 'SM29', 'MTS 03400-69', 'mét'),
(8, 'SM30', 'MTS 03400-6', 'mét'),
(8, 'SM31', 'MTS 03400-26', 'mét'),
(8, 'LO1', 'Lót nâu trơn', 'mét'),
(8, 'LO2', 'Lót ghi', 'mét'),
(8, 'LO3', '7122 (Khổ 1,2m) - Lót tím than co giãn hoa', 'mét'),
(8, 'LO4', 'Lót ghi sáng', 'mét'),
(8, 'LO5', '1051 (Khổ 1,2m)', 'mét'),
(8, 'LO6', 'JHL 899-9 (Khổ 1,2m) - Lót Hoa ghi', 'mét'),
(8, 'LO7', 'Lót ghi', 'mét'),
(8, 'LO8', '8132 (Khổ 1,2m)', 'mét'),
(8, 'LO9', '7673 (Khổ 1,2m) - Lót hoa ghi', 'mét'),
(8, 'LO10', '5975 (Khổ 1,2m)', 'mét'),
(8, 'LO11', '1575 (Khổ 1,2) - Lót ghi co giãn', 'mét'),
(8, 'LO12', 'Không có mã (Khổ 1,2m) - Lót hoa nâu', 'mét'),
(8, 'LO13', 'JHL 905-10 (Khổ 1,2m)', 'mét'),
(8, 'LO14', 'JHL 905-7 (Khổ 1,2m)', 'mét'),
(8, 'LO15', 'JHL 905-8 (Khổ 1,2m)', 'mét'),
(8, 'LO16', 'JHL 905-9 (Khổ 1,2m)', 'mét'),
(8, 'LO17', 'JHL 905-12 (Khổ 1,2m)', 'mét'),
(8, 'LO18', 'JHL 905-11 (Khổ 1,2m)', 'mét'),
(8, 'H1', 'Sơ mi mã 1840-1', 'mét'),
(8, 'H13', 'Mã 2012', 'mét'),
(8, 'H16', 'Mã 3188 (sơ mi trắng)', 'mét'),
(8, 'H9', 'HS 210420JP', 'mét'),
(8, 'H20', 'Mã 1918 (sơ mi trắng)', 'mét'),
(8, 'H55', 'HTS03225-2', 'mét');

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
('CÔNG TY', TRUE),
('DUNG', FALSE),
('HẰNG', FALSE),
('HÙNG', FALSE),
('HƯỜNG', FALSE),
('LẠNG', FALSE),
('LỤC', FALSE),
('LƯỠNG', FALSE),
('NGỌC GL', FALSE),
('PHƯỚC', FALSE),
('SÀI ĐỒNG', FALSE),
('THÔNG', FALSE),
('TRƯỜNG', FALSE);

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
-- PHẦN 4: DỮ LIỆU KHO VẢI — KHO THỢ (Product 8)
-- Nhập trực tiếp từng kho thợ (4b–4k). Không còn tồn tổng CÔNG TY (PHẦN 4 cũ đã bỏ).
-- =====================================================

-- =====================================================
-- PHẦN 4b: TỒN KHO BAN ĐẦU VẢI — SÀI ĐỒNG (Product 8)
-- Nhập trực tiếp kho thợ (7 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải SÀI ĐỒNG 2026',
    'Nhập tồn ban đầu trực tiếp kho SÀI ĐỒNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_sai_ong_set_id = LAST_INSERT_ID();
SET @fabric_sai_ong_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'SÀI ĐỒNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_sai_ong_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho SÀI ĐỒNG',
    '2026-01-01 00:00:00',
    @fabric_sai_ong_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_sai_ong_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity, fabric_note)
SELECT @fabric_sai_ong_request_id, pv.variant_id, v.qty, v.note
FROM (
SELECT 'B1'  AS item_code, 3295   AS qty, 'HDH22'   AS note
    UNION ALL SELECT 'K70',  295,   'HDH 66'
    UNION ALL SELECT 'V11',  9810.6, 'HDH55'
    UNION ALL SELECT 'V8',   1615.2, 'HDH24S'
    UNION ALL SELECT 'V87',  969.8,  'HDH67'
    UNION ALL SELECT 'V88',  1413.5, 'HDH68'
    UNION ALL SELECT 'V9',   73.3,   'HDH39C'
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4c: TỒN KHO BAN ĐẦU VẢI — TRƯỜNG (Product 8)
-- Nhập trực tiếp kho thợ (6 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải TRƯỜNG 2026',
    'Nhập tồn ban đầu trực tiếp kho TRƯỜNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_truong_set_id = LAST_INSERT_ID();
SET @fabric_truong_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'TRƯỜNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_truong_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho TRƯỜNG',
    '2026-01-01 00:00:00',
    @fabric_truong_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_truong_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_truong_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B2'  AS item_code, 5215.1 AS qty
    UNION ALL SELECT 'V2',  373.05
    UNION ALL SELECT 'V5',  3733
    UNION ALL SELECT 'B27', 186.7
    UNION ALL SELECT 'K70', 147.1
    UNION ALL SELECT 'V75', 302
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4d: TỒN KHO BAN ĐẦU VẢI — PHƯỚC (Product 8)
-- Nhập trực tiếp kho thợ (1 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải PHƯỚC 2026',
    'Nhập tồn ban đầu trực tiếp kho PHƯỚC',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_phuoc_set_id = LAST_INSERT_ID();
SET @fabric_phuoc_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'PHƯỚC' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_phuoc_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho PHƯỚC',
    '2026-01-01 00:00:00',
    @fabric_phuoc_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_phuoc_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_phuoc_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B12' AS item_code, 505.5 AS qty
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4e: TỒN KHO BAN ĐẦU VẢI — THÔNG (Product 8)
-- Nhập trực tiếp kho thợ (2 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải THÔNG 2026',
    'Nhập tồn ban đầu trực tiếp kho THÔNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_thong_set_id = LAST_INSERT_ID();
SET @fabric_thong_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'THÔNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_thong_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho THÔNG',
    '2026-01-01 00:00:00',
    @fabric_thong_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_thong_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_thong_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B12' AS item_code, 35 AS qty
    UNION ALL SELECT 'V20', 8
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4f: TỒN KHO BAN ĐẦU VẢI — NGỌC GL (Product 8)
-- Nhập trực tiếp kho thợ (6 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải NGỌC GL 2026',
    'Nhập tồn ban đầu trực tiếp kho NGỌC GL',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_ngoc_gl_set_id = LAST_INSERT_ID();
SET @fabric_ngoc_gl_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'NGỌC GL' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_ngoc_gl_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho NGỌC GL',
    '2026-01-01 00:00:00',
    @fabric_ngoc_gl_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_ngoc_gl_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_ngoc_gl_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B38' AS item_code, 210.4  AS qty
    UNION ALL SELECT 'B39',  112.2
    UNION ALL SELECT 'K17',  239.2
    UNION ALL SELECT 'V48',  727.4
    UNION ALL SELECT 'V50',  1228.14
    UNION ALL SELECT 'V80',  3363.78
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4g: TỒN KHO BAN ĐẦU VẢI — LỤC (Product 8)
-- Nhập trực tiếp kho thợ (8 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải LỤC 2026',
    'Nhập tồn ban đầu trực tiếp kho LỤC',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_luc_set_id = LAST_INSERT_ID();
SET @fabric_luc_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'LỤC' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_luc_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho LỤC',
    '2026-01-01 00:00:00',
    @fabric_luc_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_luc_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_luc_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B7'  AS item_code, 834.8  AS qty
    UNION ALL SELECT 'V53',  103.5
    UNION ALL SELECT 'V71',  1180.8
    UNION ALL SELECT 'V79',  800.1
    UNION ALL SELECT 'K58',  197
    UNION ALL SELECT 'V85',  954.3
    UNION ALL SELECT 'B40',  2218.9
    UNION ALL SELECT 'V76',  1633
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4h: TỒN KHO BAN ĐẦU VẢI — LẠNG (Product 8)
-- Nhập trực tiếp kho thợ (13 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải LẠNG 2026',
    'Nhập tồn ban đầu trực tiếp kho LẠNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_lang_set_id = LAST_INSERT_ID();
SET @fabric_lang_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'LẠNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_lang_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho LẠNG',
    '2026-01-01 00:00:00',
    @fabric_lang_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_lang_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_lang_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B11' AS item_code, 1076.2 AS qty
    UNION ALL SELECT 'B12',  1096.9
    UNION ALL SELECT 'B13',  998.8
    UNION ALL SELECT 'V57',  160.6
    UNION ALL SELECT 'K16',  755.8
    UNION ALL SELECT 'V20',  833.4
    UNION ALL SELECT 'V60',  6948
    UNION ALL SELECT 'V76',  196
    UNION ALL SELECT 'V67',  245
    UNION ALL SELECT 'V82',  316
    UNION ALL SELECT 'V80',  2781.9
    UNION ALL SELECT 'K15',  93.4
    UNION ALL SELECT 'K65',  305.1
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4i: TỒN KHO BAN ĐẦU VẢI — HƯỜNG (Product 8)
-- Nhập trực tiếp kho thợ (5 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải HƯỜNG 2026',
    'Nhập tồn ban đầu trực tiếp kho HƯỜNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_huong_set_id = LAST_INSERT_ID();
SET @fabric_huong_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'HƯỜNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_huong_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho HƯỜNG',
    '2026-01-01 00:00:00',
    @fabric_huong_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_huong_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_huong_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B2'  AS item_code, 1339.7 AS qty
    UNION ALL SELECT 'V2',  335.4
    UNION ALL SELECT 'V5',  812
    UNION ALL SELECT 'H19', 146.6
    UNION ALL SELECT 'V75', 316.9
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4j: TỒN KHO BAN ĐẦU VẢI — DUNG (Product 8)
-- Nhập trực tiếp kho thợ (9 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải DUNG 2026',
    'Nhập tồn ban đầu trực tiếp kho DUNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_dung_set_id = LAST_INSERT_ID();
SET @fabric_dung_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'DUNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_dung_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho DUNG',
    '2026-01-01 00:00:00',
    @fabric_dung_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_dung_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_dung_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B2'  AS item_code, 709.8  AS qty
    UNION ALL SELECT 'H1',  90
    UNION ALL SELECT 'H13', 121
    UNION ALL SELECT 'H16', 110
    UNION ALL SELECT 'H20', 117.5
    UNION ALL SELECT 'H21', 120.6
    UNION ALL SELECT 'V2',  385.5
    UNION ALL SELECT 'V5',  759
    UNION ALL SELECT 'V75', 224.6
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


-- =====================================================
-- PHẦN 4k: TỒN KHO BAN ĐẦU VẢI — HẰNG (Product 8)
-- Nhập trực tiếp kho thợ (5 mã), không chuyển từ CÔNG TY
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Vải HẰNG 2026',
    'Nhập tồn ban đầu trực tiếp kho HẰNG',
    'VAI_NHAP_KHO',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @fabric_hang_set_id = LAST_INSERT_ID();
SET @fabric_hang_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'HẰNG' LIMIT 1);

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @fabric_hang_set_id,
    u.unit_id,
    8,
    'IN',
    'EXECUTED',
    'Tồn kho ban đầu vải kho HẰNG',
    '2026-01-01 00:00:00',
    @fabric_hang_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @fabric_hang_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @fabric_hang_request_id, pv.variant_id, v.qty
FROM (
SELECT 'B11' AS item_code, 5842.2 AS qty
    UNION ALL SELECT 'K15',  169
    UNION ALL SELECT 'K58',  352.7
    UNION ALL SELECT 'V50',  151.7
    UNION ALL SELECT 'V80',  2995
) v
JOIN product_variants pv ON pv.product_id = 8 AND pv.item_code = v.item_code;


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
-- (Không còn procedure seed kho tạm thời)
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
