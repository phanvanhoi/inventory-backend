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

-- 2.2 Bảng sizes (Kích cỡ)
CREATE TABLE sizes (
    size_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    size_value INT NOT NULL UNIQUE,
    INDEX idx_size_value (size_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.3 Bảng length_types (Loại độ dài: Cộc/Dài)
CREATE TABLE length_types (
    length_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    INDEX idx_length_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.4 Bảng product_variants (Biến thể sản phẩm: style + size + length)
CREATE TABLE product_variants (
    variant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    style_id BIGINT NOT NULL,
    size_id BIGINT NOT NULL,
    length_type_id BIGINT NOT NULL,
    FOREIGN KEY (style_id) REFERENCES styles(style_id),
    FOREIGN KEY (size_id) REFERENCES sizes(size_id),
    FOREIGN KEY (length_type_id) REFERENCES length_types(length_type_id),
    UNIQUE KEY uk_variant (style_id, size_id, length_type_id),
    INDEX idx_variant_style (style_id),
    INDEX idx_variant_size (size_id),
    INDEX idx_variant_length (length_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.5 Bảng units (Đơn vị/Khách hàng)
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
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
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
    INDEX idx_set_executed_by (executed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.10 Bảng approval_history (Lịch sử duyệt/từ chối)
CREATE TABLE approval_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT NOT NULL,
    action ENUM('SUBMIT', 'APPROVE', 'REJECT', 'EXECUTE', 'RECEIVE', 'COMPLETE', 'EDIT') NOT NULL,
    performed_by BIGINT NOT NULL,
    reason TEXT,
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
    related_set_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (related_set_id) REFERENCES request_sets(set_id) ON DELETE SET NULL,
    INDEX idx_notification_user (user_id),
    INDEX idx_notification_read (is_read),
    INDEX idx_notification_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.12 Bảng products (Sản phẩm)
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_name (product_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (set_id) REFERENCES request_sets(set_id),
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (position_id) REFERENCES positions(position_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_request_set (set_id),
    INDEX idx_request_unit (unit_id),
    INDEX idx_request_position (position_id),
    INDEX idx_request_product (product_id),
    INDEX idx_request_type (request_type),
    INDEX idx_request_expected_date (expected_date),
    INDEX idx_request_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.14 Bảng inventory_request_items (Chi tiết từng item trong phiếu)
CREATE TABLE inventory_request_items (
    item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL,
    variant_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    FOREIGN KEY (request_id) REFERENCES inventory_requests(request_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
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
    received_quantity INT NOT NULL,
    FOREIGN KEY (receipt_id) REFERENCES receipt_records(receipt_id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES inventory_requests(request_id),
    FOREIGN KEY (variant_id) REFERENCES product_variants(variant_id),
    INDEX idx_ri_receipt (receipt_id),
    INDEX idx_ri_request (request_id),
    INDEX idx_ri_variant (variant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.17 Bảng contract_reports (Báo cáo hợp đồng - workflow theo role)
CREATE TABLE contract_reports (
    report_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    current_phase ENUM('SALES_INPUT','MEASUREMENT_INPUT','PRODUCTION_INPUT','STOCKKEEPER_INPUT','COMPLETED')
                  NOT NULL DEFAULT 'SALES_INPUT',
    -- SALES fields
    unit_id                 BIGINT NOT NULL,
    sales_person            VARCHAR(100),
    expected_delivery_date  DATE,
    finalized_list_sent_date     DATE,
    finalized_list_received_date DATE,
    delivery_method          VARCHAR(50),
    extra_payment_date       DATE,
    extra_payment_amount     DECIMAL(15,0) DEFAULT 0,
    note                     TEXT,
    -- MEASUREMENT fields
    measurement_start         DATE,
    measurement_end           DATE,
    technician_name           VARCHAR(100),
    measurement_received_date DATE,
    measurement_handler       VARCHAR(100),
    skip_measurement          BOOLEAN DEFAULT FALSE,
    production_handover_date  DATE,
    -- PRODUCTION fields
    packing_return_date      DATE,
    tailor_start_date        DATE,
    tailor_expected_return    DATE,
    tailor_actual_return      DATE,
    -- STOCKKEEPER fields
    actual_shipping_date     DATE,
    -- Metadata
    created_by  BIGINT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2.18 Bảng contract_report_history (Lịch sử chỉnh sửa)
CREATE TABLE contract_report_history (
    history_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_id   BIGINT NOT NULL,
    changed_by  BIGINT NOT NULL,
    changed_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action      VARCHAR(30) NOT NULL,
    field_name  VARCHAR(50),
    old_value   TEXT,
    new_value   TEXT,
    reason      TEXT,
    FOREIGN KEY (report_id) REFERENCES contract_reports(report_id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(user_id),
    INDEX idx_crh_report (report_id),
    INDEX idx_crh_changed_by (changed_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PHẦN 3: MASTER DATA
-- =====================================================

-- 3.1 Roles (7 vai trò)
INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'Quản trị viên - duyệt/từ chối bộ phiếu, xem/sửa tất cả báo cáo HĐ'),
('USER', 'Người dùng thông thường - chỉ tạo được phiếu IN/OUT (ảnh hưởng tồn kho thực tế)'),
('STOCKKEEPER', 'Kiểm kho - thực hiện nhập/xuất kho, nhập ngày giao hàng báo cáo HĐ'),
('PURCHASER', 'Thu mua - tạo được cả 4 loại phiếu (ADJUST_IN/OUT ảnh hưởng dự kiến, IN/OUT ảnh hưởng thực tế)'),
('SALES', 'Kinh doanh - tạo và quản lý hợp đồng, nhập thông tin HĐ'),
('MEASUREMENT', 'Phụ trách số đo - nhập thông tin đo và bàn giao SX'),
('PRODUCTION', 'Quản lý kế hoạch SX - nhập thông tin sản xuất, thợ triển khai');

-- 3.2 Styles (4 kiểu dáng)
INSERT INTO styles (style_name) VALUES
('CỔ ĐIỂN'),
('CỔ ĐIỂN NGẮN'),
('SLIM'),
('SLIM Ngắn');

-- 3.3 Sizes (11 kích cỡ: 35-45)
INSERT INTO sizes (size_value) VALUES
(35), (36), (37), (38), (39), (40), (41), (42), (43), (44), (45);

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

-- 3.6 Product Variants (88 biến thể = 4 styles x 11 sizes x 2 lengths)
INSERT INTO product_variants (style_id, size_id, length_type_id) VALUES
-- CỔ ĐIỂN (style_id = 1)
(1, 1, 1), (1, 1, 2), (1, 2, 1), (1, 2, 2), (1, 3, 1), (1, 3, 2),
(1, 4, 1), (1, 4, 2), (1, 5, 1), (1, 5, 2), (1, 6, 1), (1, 6, 2),
(1, 7, 1), (1, 7, 2), (1, 8, 1), (1, 8, 2), (1, 9, 1), (1, 9, 2),
(1, 10, 1), (1, 10, 2), (1, 11, 1), (1, 11, 2),
-- CỔ ĐIỂN NGẮN (style_id = 2)
(2, 1, 1), (2, 1, 2), (2, 2, 1), (2, 2, 2), (2, 3, 1), (2, 3, 2),
(2, 4, 1), (2, 4, 2), (2, 5, 1), (2, 5, 2), (2, 6, 1), (2, 6, 2),
(2, 7, 1), (2, 7, 2), (2, 8, 1), (2, 8, 2), (2, 9, 1), (2, 9, 2),
(2, 10, 1), (2, 10, 2), (2, 11, 1), (2, 11, 2),
-- SLIM (style_id = 3)
(3, 1, 1), (3, 1, 2), (3, 2, 1), (3, 2, 2), (3, 3, 1), (3, 3, 2),
(3, 4, 1), (3, 4, 2), (3, 5, 1), (3, 5, 2), (3, 6, 1), (3, 6, 2),
(3, 7, 1), (3, 7, 2), (3, 8, 1), (3, 8, 2), (3, 9, 1), (3, 9, 2),
(3, 10, 1), (3, 10, 2), (3, 11, 1), (3, 11, 2),
-- SLIM Ngắn (style_id = 4)
(4, 1, 1), (4, 1, 2), (4, 2, 1), (4, 2, 2), (4, 3, 1), (4, 3, 2),
(4, 4, 1), (4, 4, 2), (4, 5, 1), (4, 5, 2), (4, 6, 1), (4, 6, 2),
(4, 7, 1), (4, 7, 2), (4, 8, 1), (4, 8, 2), (4, 9, 1), (4, 9, 2),
(4, 10, 1), (4, 10, 2), (4, 11, 1), (4, 11, 2);

-- 3.6 Products (Sản phẩm)
INSERT INTO products (product_name, note, created_at) VALUES
('HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU)', 'Sơ mi nam 2025 - SM1', '2025-06-20 00:00:00');

-- 3.7 Units (72 đơn vị/khách hàng)
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
(8, 6); -- tra: MEASUREMENT
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
-- Request 1: Nhập kho ban đầu (dòng 5) - set_id = 1
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, note, created_at)
SELECT 1, u.unit_id, 1, 'IN', NULL, '2025-06-20 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

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
    IN p_size_value INT,
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

DELIMITER ;

-- =====================================================
-- PHẦN 7: INVENTORY REQUEST ITEMS (CHI TIẾT)
-- =====================================================

-- REQUEST 1: Nhập kho ban đầu (20/06/2025) - Kho
-- Dữ liệu từ CSV: SƠ MI NAM 2025 - SM1 - HDH22 - không lé không thêu.csv (line 5)
-- CỔ ĐIỂN (Size 35-45, Cộc/Dài)
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 35, 'COC', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 35, 'DAI', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 36, 'COC', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 36, 'DAI', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 37, 'COC', 13);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 37, 'DAI', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 38, 'COC', 14);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 40, 'COC', 1);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 40, 'DAI', 1);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 41, 'COC', 10);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 42, 'COC', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 42, 'DAI', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 43, 'COC', 7);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 43, 'DAI', 3);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 44, 'COC', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN', 44, 'DAI', 14);
-- CỔ ĐIỂN NGẮN (Size 35-45, Cộc/Dài)
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 35, 'COC', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 35, 'DAI', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 36, 'COC', 5);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 36, 'DAI', 4);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 37, 'COC', 16);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 37, 'DAI', 13);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 38, 'COC', 17);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 38, 'DAI', 14);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 39, 'COC', 15);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 39, 'DAI', 8);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 40, 'COC', 6);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 40, 'DAI', 13);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 41, 'COC', 12);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 41, 'DAI', 21);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 42, 'COC', 15);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 42, 'DAI', 13);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 43, 'COC', 8);
CALL insert_item_by_variant(1, 'CỔ ĐIỂN NGẮN', 43, 'DAI', 7);
-- SLIM (Size 35-45, Cộc/Dài)
CALL insert_item_by_variant(1, 'SLIM', 36, 'COC', 15);
CALL insert_item_by_variant(1, 'SLIM', 36, 'DAI', 15);
CALL insert_item_by_variant(1, 'SLIM', 37, 'COC', 17);
CALL insert_item_by_variant(1, 'SLIM', 37, 'DAI', 18);
CALL insert_item_by_variant(1, 'SLIM', 38, 'COC', 14);
CALL insert_item_by_variant(1, 'SLIM', 38, 'DAI', 7);
CALL insert_item_by_variant(1, 'SLIM', 39, 'COC', 11);
CALL insert_item_by_variant(1, 'SLIM', 39, 'DAI', 1);
CALL insert_item_by_variant(1, 'SLIM', 40, 'COC', 10);
CALL insert_item_by_variant(1, 'SLIM', 40, 'DAI', 11);
CALL insert_item_by_variant(1, 'SLIM', 41, 'COC', 11);
CALL insert_item_by_variant(1, 'SLIM', 41, 'DAI', 14);
CALL insert_item_by_variant(1, 'SLIM', 42, 'COC', 10);
CALL insert_item_by_variant(1, 'SLIM', 42, 'DAI', 10);
CALL insert_item_by_variant(1, 'SLIM', 43, 'COC', 9);
CALL insert_item_by_variant(1, 'SLIM', 43, 'DAI', 7);
-- SLIM Ngắn (Size 35-45, Cộc/Dài)
CALL insert_item_by_variant(1, 'SLIM Ngắn', 36, 'COC', 14);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 36, 'DAI', 16);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 37, 'COC', 15);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 37, 'DAI', 17);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 38, 'COC', 14);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 38, 'DAI', 7);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 39, 'COC', 11);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 39, 'DAI', 5);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 40, 'COC', 12);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 40, 'DAI', 10);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 41, 'COC', 11);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 41, 'DAI', 9);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 42, 'COC', 11);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 42, 'DAI', 7);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 43, 'COC', 10);
CALL insert_item_by_variant(1, 'SLIM Ngắn', 43, 'DAI', 10);

-- =====================================================
-- PHẦN 8: CONTRACT REPORTS (Dữ liệu mẫu báo cáo hợp đồng)
-- =====================================================
-- created_by = 4 (Thúy - PURCHASER, PRODUCTION)
INSERT INTO contract_reports (current_phase, unit_id, sales_person, expected_delivery_date, finalized_list_sent_date, finalized_list_received_date, delivery_method, extra_payment_date, extra_payment_amount, note, measurement_start, measurement_end, technician_name, measurement_received_date, measurement_handler, skip_measurement, production_handover_date, packing_return_date, tailor_start_date, tailor_expected_return, tailor_actual_return, actual_shipping_date, created_by, created_at) VALUES
-- 1. COMPLETED - Đã giao hàng hoàn tất
('COMPLETED', 1, 'Thúy', '2026-01-15', '2025-10-10', '2025-10-12', 'POST_OFFICE', '2026-01-12', 500000, 'Đã hoàn tất', '2025-10-01', '2025-10-03', 'Trần Thị B', '2025-10-08', 'Hương', FALSE, '2025-10-15', '2025-10-20', '2025-10-25', '2025-12-15', '2025-12-20', '2026-01-10', 4, '2025-09-20 08:00:00'),
-- 2. PRODUCTION_INPUT - Đang sản xuất, sắp đến hạn
('PRODUCTION_INPUT', 2, 'Thúy', '2026-03-15', '2025-12-20', '2025-12-22', NULL, NULL, 0, 'Đang chờ thợ triển khai', '2025-12-10', '2025-12-12', 'Nguyễn Văn D', '2025-12-18', 'Hương', FALSE, '2025-12-28', NULL, NULL, NULL, NULL, NULL, 4, '2025-12-01 09:00:00'),
-- 3. STOCKKEEPER_INPUT - Chờ giao hàng, trễ hạn
('STOCKKEEPER_INPUT', 3, 'Thúy', '2026-02-20', '2025-11-15', '2025-11-17', 'DIRECT', NULL, 0, 'Chờ giao hàng - đã trễ hạn', '2025-11-05', '2025-11-07', 'Trần Thị B', '2025-11-12', 'Hương', FALSE, '2025-11-20', '2025-11-25', '2025-12-01', '2026-01-30', '2026-02-10', NULL, 4, '2025-10-25 10:00:00'),
-- 4. PRODUCTION_INPUT - Bỏ qua số đo, chờ sản xuất
('PRODUCTION_INPUT', 4, 'Thúy', '2026-04-30', '2026-01-10', '2026-01-12', NULL, NULL, 0, 'Bỏ qua đo, dùng số đo cũ', NULL, NULL, NULL, NULL, NULL, TRUE, '2026-01-15', NULL, NULL, NULL, NULL, NULL, 4, '2026-01-05 14:00:00'),
-- 5. SALES_INPUT - Mới tạo
('SALES_INPUT', 5, 'Thúy', '2026-06-30', NULL, NULL, NULL, NULL, 0, 'Hợp đồng mới', NULL, NULL, NULL, NULL, NULL, FALSE, NULL, NULL, NULL, NULL, NULL, NULL, 4, '2026-03-01 08:30:00'),
-- 6. MEASUREMENT_INPUT - Đang đo khách
('MEASUREMENT_INPUT', 6, 'Thúy', '2026-05-15', NULL, NULL, NULL, NULL, 0, NULL, '2026-03-01', '2026-03-05', 'Nguyễn Văn D', NULL, NULL, FALSE, NULL, NULL, NULL, NULL, NULL, NULL, 4, '2026-02-20 11:00:00'),
-- 7. PRODUCTION_INPUT - Thợ trả trễ (đã nhập SX nhưng chưa xong)
('PRODUCTION_INPUT', 7, 'Thúy', '2026-04-10', '2025-12-25', '2025-12-27', NULL, NULL, 0, 'Thợ đang trễ hạn trả', '2025-12-15', '2025-12-17', 'Trần Thị B', '2025-12-22', 'Hương', FALSE, '2026-01-02', '2026-01-08', '2026-01-15', '2026-02-28', NULL, NULL, 4, '2025-12-10 09:00:00');

-- =====================================================
-- PHẦN 9: CLEANUP - XÓA PROCEDURE SAU KHI IMPORT
-- =====================================================
DROP PROCEDURE IF EXISTS insert_item_by_variant;

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
SELECT COUNT(*) AS total_contract_reports FROM contract_reports;

-- END OF FILE
