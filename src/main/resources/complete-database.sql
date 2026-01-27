-- =====================================================
-- HANGFASHION INVENTORY MANAGEMENT SYSTEM
-- Complete Database Script
-- Version: 1.0
-- Description: Schema + Tables + Data Import
-- =====================================================

-- =====================================================
-- PHẦN 1: KHỞI TẠO DATABASE
-- =====================================================
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

-- 2.6 Bảng roles (Vai trò người dùng)
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
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'EXECUTED') NOT NULL DEFAULT 'PENDING',
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
    action ENUM('SUBMIT', 'APPROVE', 'REJECT', 'EXECUTE') NOT NULL,
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

-- 2.13 Bảng inventory_requests (Phiếu xuất/nhập kho)
-- expected_date: Ngày dự kiến (bắt buộc cho ADJUST_IN, ADJUST_OUT)
CREATE TABLE inventory_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    set_id BIGINT,
    unit_id BIGINT,
    product_id BIGINT,
    request_type ENUM('IN', 'OUT', 'ADJUST_IN', 'ADJUST_OUT') NOT NULL,
    expected_date DATE NULL,
    note TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (set_id) REFERENCES request_sets(set_id),
    FOREIGN KEY (unit_id) REFERENCES units(unit_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_request_set (set_id),
    INDEX idx_request_unit (unit_id),
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

-- =====================================================
-- PHẦN 3: MASTER DATA
-- =====================================================

-- 3.1 Roles (4 vai trò)
INSERT INTO roles (role_name, description) VALUES
('ADMIN', 'Quản trị viên - chỉ có quyền duyệt/từ chối bộ phiếu, không được tạo'),
('USER', 'Người dùng thông thường - chỉ tạo được phiếu IN/OUT (ảnh hưởng tồn kho thực tế)'),
('STOCKKEEPER', 'Kiểm kho - chỉ xem được bộ phiếu đã duyệt'),
('PURCHASER', 'Thu mua - tạo được cả 4 loại phiếu (ADJUST_IN/OUT ảnh hưởng dự kiến, IN/OUT ảnh hưởng thực tế)');

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

-- 3.5 Product Variants (88 biến thể = 4 styles x 11 sizes x 2 lengths)
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
('thuy', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Thúy'),
('thanh', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Thanh'),
('huong', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Hương'),
('thuong', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Thương'),
('khoa', '$2a$10$B2kATibQPqmIYfv3KHhVzuV7E4fEOVW.0uYMSzMdf4JNzunrpFZ.O', 'Khoa');

-- 3.9 User Roles (Gán role cho users)
-- user_id: 1=thuy, 2=nga, 3=huong, 4=thuong, 5=tung
-- role_id: 1=ADMIN, 2=USER, 3=STOCKKEEPER, 4=PURCHASER
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- thuy là ADMIN
(1, 2), -- thuy cũng có quyền USER
(2, 2), -- nga là USER
(2, 4), -- nga cũng có quyền PURCHASER
(3, 4), -- thanh cũng có quyền PURCHASER
(3, 2), -- huong là USER
(4, 2), -- thuong là USER
(5, 3); -- tung là STOCKKEEPER

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
-- PHẦN 8: CLEANUP - XÓA PROCEDURE SAU KHI IMPORT
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

-- END OF FILE
