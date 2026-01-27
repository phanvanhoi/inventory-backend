-- =====================================================
-- IMPORT DATA TỪ CSV: SƠ MI NAM 2025 - HDH22
-- Lưu ý: Request Sets được gộp theo tên (unique)
-- Sử dụng INSERT IGNORE để bỏ qua nếu đã tồn tại
-- =====================================================

-- =====================================================
-- 1. MASTER DATA: STYLES (4 loại)
-- =====================================================
INSERT IGNORE INTO styles (style_name) VALUES
('CỔ ĐIỂN'),
('CỔ ĐIỂN NGẮN'),
('SLIM'),
('SLIM Ngắn');

-- =====================================================
-- 2. MASTER DATA: SIZES (11 sizes: 35-45)
-- =====================================================
INSERT IGNORE INTO sizes (size_value) VALUES
(35), (36), (37), (38), (39), (40), (41), (42), (43), (44), (45);

-- =====================================================
-- 3. MASTER DATA: LENGTH TYPES (2 loại)
-- =====================================================
INSERT IGNORE INTO length_types (code) VALUES
('Cộc'),
('Dài');

-- =====================================================
-- 4. PRODUCT VARIANTS (88 biến thể = 4 styles x 11 sizes x 2 lengths)
-- =====================================================
INSERT IGNORE INTO product_variants (style_id, size_id, length_type_id) VALUES
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

-- =====================================================
-- 5. USERS (người tạo request sets)
-- =====================================================
INSERT INTO users (username, full_name) VALUES
('thuy', 'Thúy'),
('nga', 'Nga'),
('huong', 'Hương'),
('thuong', 'Thương'),
('tung', 'Tùng');

-- =====================================================
-- 6. REQUEST SETS (Bộ phiếu - UNIQUE by name)
-- Gộp các dòng có cùng tên set lại
-- =====================================================
INSERT INTO request_sets (set_name, created_by, created_at) VALUES
-- set_id = 1: Nhập kho ban đầu (không có tên set trong CSV)
('Nhập kho ban đầu', NULL, '2025-06-20 00:00:00'),
-- set_id = 2: ĐX 48 - Thúy (2 requests: dòng 6,7)
('ĐX 48 - Thúy', 1, '2025-07-01 00:00:00'),
-- set_id = 3: ĐX 55 - Thúy (1 request: dòng 8)
('ĐX 55 - Thúy', 1, '2025-07-10 00:00:00'),
-- set_id = 4: ĐX 6 - Nga (2 requests: dòng 9,10)
('ĐX 6 - Nga', 2, '2025-07-05 00:00:00'),
-- set_id = 5: ĐX 7 - Nga (2 requests: dòng 11,12)
('ĐX 7 - Nga', 2, '2025-07-07 00:00:00'),
-- set_id = 6: Đơn đặt hàng 18 - Sài Đồng (1 request: dòng 13)
('Đơn đặt hàng 18 - Sài Đồng', NULL, '2025-07-23 00:00:00'),
-- set_id = 7: ĐX 60 - Thúy (4 requests: dòng 14,15,16,17)
('ĐX 60 - Thúy', 1, '2025-07-10 00:00:00'),
-- set_id = 8: ĐX 60 - Hương (1 request: dòng 18)
('ĐX 60 - Hương', 3, '2025-07-12 00:00:00'),
-- set_id = 9: ĐX 63 - Thúy (5 requests: dòng 19,20,21,22,23)
('ĐX 63 - Thúy', 1, '2025-07-19 00:00:00'),
-- set_id = 10: ĐX 65 - Thúy (1 request: dòng 24)
('ĐX 65 - Thúy', 1, '2025-07-29 00:00:00'),
-- set_id = 11: ĐX 71 - Thúy (9 requests: dòng 25,26,27,28,29,30,31,32,33)
('ĐX 71 - Thúy', 1, '2025-08-18 00:00:00'),
-- set_id = 12: ĐX 73 - Thúy (4 requests: dòng 34,35,36,37)
('ĐX 73 - Thúy', 1, '2025-08-17 00:00:00'),
-- set_id = 13: ĐX 66 - Hương (1 request: dòng 38)
('ĐX 66 - Hương', 3, '2025-08-06 00:00:00'),
-- set_id = 14: ĐX 67 - Hương (1 request: dòng 39)
('ĐX 67 - Hương', 3, '2025-08-12 00:00:00'),
-- set_id = 15: ĐX 64 - Thương (1 request: dòng 40)
('ĐX 64 - Thương', 4, '2025-08-16 00:00:00'),
-- set_id = 16: Đơn đặt hàng 26 - Sài Đồng (1 request: dòng 41)
('Đơn đặt hàng 26 - Sài Đồng', NULL, '2025-08-29 00:00:00'),
-- set_id = 17: ĐX 79 - Thúy (1 request: dòng 42)
('ĐX 79 - Thúy', 1, '2025-09-21 00:00:00'),
-- set_id = 18: ĐX 69 - Hương (1 request: dòng 43)
('ĐX 69 - Hương', 3, '2025-08-20 00:00:00'),
-- set_id = 19: ĐX 10 - Nga (2 requests: dòng 44,45)
('ĐX 10 - Nga', 2, '2025-08-22 00:00:00'),
-- set_id = 20: ĐX 81 - Thúy (2 requests: dòng 46,47)
('ĐX 81 - Thúy', 1, '2025-09-25 00:00:00'),
-- set_id = 21: ĐX 70 - Thương (1 request: dòng 48)
('ĐX 70 - Thương', 4, '2025-08-28 00:00:00'),
-- set_id = 22: ĐX 71 - Hương (1 request: dòng 49)
('ĐX 71 - Hương', 3, '2025-08-29 00:00:00'),
-- set_id = 23: ĐX 82 - Thúy (2 requests: dòng 50,51)
('ĐX 82 - Thúy', 1, '2025-09-25 00:00:00'),
-- set_id = 24: ĐX 12 - Nga (2 requests: dòng 52,53)
('ĐX 12 - Nga', 2, '2025-09-12 00:00:00'),
-- set_id = 25: ĐX 74 - Hương (1 request: dòng 54)
('ĐX 74 - Hương', 3, '2025-09-12 00:00:00'),
-- set_id = 26: ĐX 1 - Hương (3 requests: dòng 55,56,69)
('ĐX 1 - Hương', 3, '2025-09-17 00:00:00'),
-- set_id = 27: ĐX 86 - Thúy (3 requests: dòng 57,58,59)
('ĐX 86 - Thúy', 1, '2025-09-29 00:00:00'),
-- set_id = 28: ĐX 75 - Hương (1 request: dòng 60)
('ĐX 75 - Hương', 3, '2025-09-18 00:00:00'),
-- set_id = 29: ĐX 13 - Nga (2 requests: dòng 61,62)
('ĐX 13 - Nga', 2, '2025-09-19 00:00:00'),
-- set_id = 30: ĐX 90 - Thúy (1 request: dòng 63)
('ĐX 90 - Thúy', 1, '2025-10-20 00:00:00'),
-- set_id = 31: ĐX 14 - Nga (2 requests: dòng 64,65)
('ĐX 14 - Nga', 2, '2025-09-26 00:00:00'),
-- set_id = 32: ĐX 79 - Thương (1 request: dòng 66)
('ĐX 79 - Thương', 4, '2025-10-10 00:00:00'),
-- set_id = 33: ĐX 92 - Thúy (1 request: dòng 67)
('ĐX 92 - Thúy', 1, '2025-10-21 00:00:00'),
-- set_id = 34: ĐX 81 - Hương (1 request: dòng 68)
('ĐX 81 - Hương', 3, '2025-10-13 00:00:00'),
-- set_id = 35: ĐX 15 - Nga (2 requests: dòng 70,71)
('ĐX 15 - Nga', 2, '2025-10-15 00:00:00'),
-- set_id = 36: ĐX 16 - Nga (1 request: dòng 72)
('ĐX 16 - Nga', 2, '2025-10-22 00:00:00'),
-- set_id = 37: ĐX 98 - Thúy (1 request: dòng 73)
('ĐX 98 - Thúy', 1, '2025-11-04 00:00:00'),
-- set_id = 38: ĐX 83 - Hương (1 request: dòng 74)
('ĐX 83 - Hương', 3, '2025-10-29 00:00:00'),
-- set_id = 39: ĐX 84 - Hương (1 request: dòng 75)
('ĐX 84 - Hương', 3, '2025-10-30 00:00:00'),
-- set_id = 40: ĐX 101 - Thúy (2 requests: dòng 76,77)
('ĐX 101 - Thúy', 1, '2025-11-20 00:00:00'),
-- set_id = 41: Tạm loại vì ố (1 request: dòng 78 - không có tên set)
('Tạm loại vì ố', NULL, '2025-11-07 00:00:00'),
-- set_id = 42: ĐX 17 - Nga (2 requests: dòng 79,80)
('ĐX 17 - Nga', 2, '2025-11-06 00:00:00'),
-- set_id = 43: ĐX 87 - Hương (1 request: dòng 81)
('ĐX 87 - Hương', 3, '2025-11-07 00:00:00'),
-- set_id = 44: ĐX 91 - Thương (1 request: dòng 82)
('ĐX 91 - Thương', 4, '2025-11-10 00:00:00'),
-- set_id = 45: ĐX 1 - Tùng (1 request: dòng 83)
('ĐX 1 - Tùng', 5, '2025-11-12 00:00:00'),
-- set_id = 46: ĐX 104 - Thúy (1 request: dòng 84)
('ĐX 104 - Thúy', 1, '2025-12-06 00:00:00'),
-- set_id = 47: ĐX 88 - Hương (1 request: dòng 85)
('ĐX 88 - Hương', 3, '2025-11-20 00:00:00'),
-- set_id = 48: ĐX 106 - Thúy (3 requests: dòng 86,87,88)
('ĐX 106 - Thúy', 1, '2025-12-06 00:00:00'),
-- set_id = 49: ĐX 18 - Nga (2 requests: dòng 89,90)
('ĐX 18 - Nga', 2, '2025-12-08 00:00:00'),
-- set_id = 50: ĐX 107 - Thúy (1 request: dòng 91)
('ĐX 107 - Thúy', 1, '2025-12-20 00:00:00'),
-- set_id = 51: ĐX 94 - Hương (1 request: dòng 92)
('ĐX 94 - Hương', 3, '2025-12-12 00:00:00'),
-- set_id = 52: ĐX 97 - Hương (1 request: dòng 93)
('ĐX 97 - Hương', 3, '2025-12-20 00:00:00');

-- =====================================================
-- 7. INVENTORY REQUESTS
-- Mỗi dòng CSV = 1 request, tham chiếu đến set_id đúng
-- =====================================================

-- Request 1: Nhập kho ban đầu (dòng 5) - set_id = 1
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 1, u.unit_id, 'IN', NULL, '2025-06-20 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

-- Request 2-3: ĐX 48 - Thúy (dòng 6,7) - set_id = 2
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 2, u.unit_id, 'OUT', NULL, '2025-07-01 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Vĩnh Phúc';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 2, u.unit_id, 'OUT', NULL, '2025-07-01 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Hải Dương';

-- Request 4: ĐX 55 - Thúy (dòng 8) - set_id = 3
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 3, u.unit_id, 'OUT', NULL, '2025-07-10 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Bến Tre';

-- Request 5-6: ĐX 6 - Nga (dòng 9,10) - set_id = 4
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 4, u.unit_id, 'IN', NULL, '2025-07-05 00:00:00'
FROM units u WHERE u.unit_name = 'VT Hà Nội, TTKD Bắc Kạn, Cty DV Số';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 4, u.unit_id, 'OUT', NULL, '2025-07-05 00:00:00'
FROM units u WHERE u.unit_name = 'VT Hà Nội, TTKD Bắc Kạn, Cty DV Số';

-- Request 7-8: ĐX 7 - Nga (dòng 11,12) - set_id = 5
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 5, u.unit_id, 'IN', NULL, '2025-07-07 00:00:00'
FROM units u WHERE u.unit_name = 'VT An Giang, BĐ Kon Tum, Cty dịch vụ số';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 5, u.unit_id, 'OUT', NULL, '2025-07-07 00:00:00'
FROM units u WHERE u.unit_name = 'VT An Giang, BĐ Kon Tum, Cty dịch vụ số';

-- Request 9: Đơn đặt hàng 18 (dòng 13) - set_id = 6
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 6, u.unit_id, 'IN', NULL, '2025-07-23 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

-- Request 10-13: ĐX 60 - Thúy (dòng 14,15,16,17) - set_id = 7
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 7, u.unit_id, 'OUT', NULL, '2025-07-10 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Lào Cai';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 7, u.unit_id, 'OUT', NULL, '2025-07-10 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Hưng Yên';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 7, u.unit_id, 'OUT', NULL, '2025-07-10 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Đông Anh';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 7, u.unit_id, 'OUT', NULL, '2025-07-10 00:00:00'
FROM units u WHERE u.unit_name = 'Công ty Logistics';

-- Request 14: ĐX 60 - Hương (dòng 18) - set_id = 8
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 8, u.unit_id, 'OUT', NULL, '2025-07-12 00:00:00'
FROM units u WHERE u.unit_name = 'TTKD Hà Nội + BĐ Gia Lai';

-- Request 15-19: ĐX 63 - Thúy (dòng 19,20,21,22,23) - set_id = 9
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 9, u.unit_id, 'OUT', NULL, '2025-07-19 00:00:00'
FROM units u WHERE u.unit_name = 'BĐTT Sài Gòn - BĐ Hồ Chí Minh';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 9, u.unit_id, 'OUT', NULL, '2025-07-19 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Nam Định';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 9, u.unit_id, 'OUT', NULL, '2025-07-19 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Ninh Bình';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 9, u.unit_id, 'OUT', NULL, '2025-07-19 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Cầu Giấy';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 9, u.unit_id, 'OUT', NULL, '2025-07-19 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Lạng Sơn';

-- Request 20: ĐX 65 - Thúy (dòng 24) - set_id = 10
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 10, u.unit_id, 'OUT', NULL, '2025-07-29 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Quảng Bình';

-- Request 21-29: ĐX 71 - Thúy (dòng 25-33) - set_id = 11 (9 requests)
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-18 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Chương Mỹ';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-18 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Hoàn Kiếm';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-18 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Cần Thơ';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-18 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Long Biên';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-18 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Hà Đông';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Kho vận';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Nam Định';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Cầu Giấy';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 11, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Quảng Bình';

-- Request 30-33: ĐX 73 - Thúy (dòng 34-37) - set_id = 12 (4 requests)
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 12, u.unit_id, 'OUT', NULL, '2025-08-17 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Cà Mau';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 12, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Bình Phước';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 12, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Quảng Trị';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 12, u.unit_id, 'OUT', NULL, '2025-08-18 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Sơn La';

-- Request 34: ĐX 66 - Hương (dòng 38) - set_id = 13
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 13, u.unit_id, 'OUT', NULL, '2025-08-06 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Nghệ An';

-- Request 35: ĐX 67 - Hương (dòng 39) - set_id = 14
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 14, u.unit_id, 'OUT', NULL, '2025-08-12 00:00:00'
FROM units u WHERE u.unit_name = 'Tcty Bưu điện';

-- Request 36: ĐX 64 - Thương (dòng 40) - set_id = 15
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 15, u.unit_id, 'OUT', NULL, '2025-08-16 00:00:00'
FROM units u WHERE u.unit_name = 'Tcty Bưu điện';

-- Request 37: Đơn đặt hàng 26 (dòng 41) - set_id = 16
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 16, u.unit_id, 'IN', NULL, '2025-08-29 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

-- Request 38: ĐX 79 - Thúy (dòng 42) - set_id = 17
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 17, u.unit_id, 'OUT', NULL, '2025-09-21 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Tuyên Quang';

-- Request 39: ĐX 69 - Hương (dòng 43) - set_id = 18
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 18, u.unit_id, 'OUT', NULL, '2025-08-20 00:00:00'
FROM units u WHERE u.unit_name = 'Logistic, BĐ HCM, BĐ Tuyên Quang';

-- Request 40-41: ĐX 10 - Nga (dòng 44,45) - set_id = 19
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 19, u.unit_id, 'IN', NULL, '2025-08-22 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ lô lẻ của Thương';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 19, u.unit_id, 'OUT', NULL, '2025-08-22 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Vĩnh Phúc';

-- Request 42-43: ĐX 81 - Thúy (dòng 46,47) - set_id = 20
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 20, u.unit_id, 'OUT', NULL, '2025-09-25 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Yên Bái';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 20, u.unit_id, 'OUT', NULL, '2025-09-25 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Từ Liêm';

-- Request 44: ĐX 70 - Thương (dòng 48) - set_id = 21
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 21, u.unit_id, 'OUT', NULL, '2025-08-28 00:00:00'
FROM units u WHERE u.unit_name = 'Trường Định Công';

-- Request 45: ĐX 71 - Hương (dòng 49) - set_id = 22
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 22, u.unit_id, 'OUT', NULL, '2025-08-29 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Huế';

-- Request 46-47: ĐX 82 - Thúy (dòng 50,51) - set_id = 23
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 23, u.unit_id, 'OUT', NULL, '2025-09-25 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Phú Thọ';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 23, u.unit_id, 'OUT', NULL, '2025-09-25 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Bình Định';

-- Request 48-49: ĐX 12 - Nga (dòng 52,53) - set_id = 24
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 24, u.unit_id, 'OUT', NULL, '2025-09-12 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Hà Nội';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 24, u.unit_id, 'OUT', NULL, '2025-09-12 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Hà Nội, BĐ Vĩnh Phúc';

-- Request 50: ĐX 74 - Hương (dòng 54) - set_id = 25
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 25, u.unit_id, 'OUT', NULL, '2025-09-12 00:00:00'
FROM units u WHERE u.unit_name = 'Thùy';

-- Request 51-52: ĐX 1 - Hương (dòng 55,56) - set_id = 26
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 26, u.unit_id, 'IN', 'Hương may khách không mặc vừa trả nhập kho', '2025-09-17 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 26, u.unit_id, 'OUT', 'Áo trên kệ bị ố vàng. Tạm thời loại để giặt tẩy', '2025-09-18 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

-- Request 53-55: ĐX 86 - Thúy (dòng 57,58,59) - set_id = 27
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 27, u.unit_id, 'OUT', NULL, '2025-09-29 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Kiên Giang';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 27, u.unit_id, 'OUT', NULL, '2025-09-29 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Sơn Tây';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 27, u.unit_id, 'OUT', NULL, '2025-09-29 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Nam Định';

-- Request 56: ĐX 75 - Hương (dòng 60) - set_id = 28
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 28, u.unit_id, 'OUT', NULL, '2025-09-18 00:00:00'
FROM units u WHERE u.unit_name = 'Mẫu BĐ Huế - Hằng fashion';

-- Request 57-58: ĐX 13 - Nga (dòng 61,62) - set_id = 29
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 29, u.unit_id, 'IN', NULL, '2025-09-19 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Hà Nội';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 29, u.unit_id, 'OUT', NULL, '2025-09-19 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Hà Nội';

-- Request 59: ĐX 90 - Thúy (dòng 63) - set_id = 30
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 30, u.unit_id, 'OUT', NULL, '2025-10-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Thanh Trì';

-- Request 60-61: ĐX 14 - Nga (dòng 64,65) - set_id = 31
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 31, u.unit_id, 'IN', NULL, '2025-09-26 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Hải Phòng';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 31, u.unit_id, 'OUT', NULL, '2025-09-26 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Hà Nội, BĐ Hải Phòng';

-- Request 62: ĐX 79 - Thương (dòng 66) - set_id = 32
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 32, u.unit_id, 'OUT', NULL, '2025-10-10 00:00:00'
FROM units u WHERE u.unit_name = 'Khách lẻ sếp Hằng';

-- Request 63: ĐX 92 - Thúy (dòng 67) - set_id = 33
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 33, u.unit_id, 'OUT', NULL, '2025-10-21 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Đồng Nai';

-- Request 64: ĐX 81 - Hương (dòng 68) - set_id = 34
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 34, u.unit_id, 'OUT', NULL, '2025-10-13 00:00:00'
FROM units u WHERE u.unit_name = 'Đạt - con chị Liên Hương - Công đoàn Tct Bưu điện';

-- Request 65: ĐX 1 - Hương (dòng 69) - set_id = 26 (gộp vào set 26)
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 26, u.unit_id, 'IN', NULL, '2025-10-13 00:00:00'
FROM units u WHERE u.unit_name = 'Nhập lại kho đồ mượn đi đo và đồ khách trả lại';

-- Request 66-67: ĐX 15 - Nga (dòng 70,71) - set_id = 35
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 35, u.unit_id, 'IN', NULL, '2025-10-15 00:00:00'
FROM units u WHERE u.unit_name = 'Tổng Nét, VT Hòa Bình';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 35, u.unit_id, 'OUT', NULL, '2025-10-15 00:00:00'
FROM units u WHERE u.unit_name = 'Tổng Nét, VT Hòa Bình';

-- Request 68: ĐX 16 - Nga (dòng 72) - set_id = 36
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 36, u.unit_id, 'OUT', NULL, '2025-10-22 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Ninh Bình';

-- Request 69: ĐX 98 - Thúy (dòng 73) - set_id = 37
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 37, u.unit_id, 'OUT', NULL, '2025-11-04 00:00:00'
FROM units u WHERE u.unit_name = 'TCT Bưu điện';

-- Request 70: ĐX 83 - Hương (dòng 74) - set_id = 38
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 38, u.unit_id, 'OUT', NULL, '2025-10-29 00:00:00'
FROM units u WHERE u.unit_name = 'Toàn Huế đi đo';

-- Request 71: ĐX 84 - Hương (dòng 75) - set_id = 39
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 39, u.unit_id, 'OUT', NULL, '2025-10-30 00:00:00'
FROM units u WHERE u.unit_name = 'Tổng Nét';

-- Request 72-73: ĐX 101 - Thúy (dòng 76,77) - set_id = 40
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 40, u.unit_id, 'OUT', NULL, '2025-11-20 00:00:00'
FROM units u WHERE u.unit_name = 'Công ty Du lịch bưu điện';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 40, u.unit_id, 'OUT', NULL, '2025-11-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Đắk Lắk';

-- Request 74: Tạm loại vì ố (dòng 78) - set_id = 41
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 41, u.unit_id, 'OUT', NULL, '2025-11-07 00:00:00'
FROM units u WHERE u.unit_name = 'Tạm loại vì bị ố. Nếu tẩy được sẽ nhập lại';

-- Request 75-76: ĐX 17 - Nga (dòng 79,80) - set_id = 42
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 42, u.unit_id, 'IN', NULL, '2025-11-06 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Sơn La';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 42, u.unit_id, 'OUT', NULL, '2025-11-06 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Sơn La';

-- Request 77: ĐX 87 - Hương (dòng 81) - set_id = 43
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 43, u.unit_id, 'OUT', NULL, '2025-11-07 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Khánh Hòa';

-- Request 78: ĐX 91 - Thương (dòng 82) - set_id = 44
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 44, u.unit_id, 'OUT', NULL, '2025-11-10 00:00:00'
FROM units u WHERE u.unit_name = 'Lấy áo đi đo Than Thống Nhất đợt 3';

-- Request 79: ĐX 1 - Tùng (dòng 83) - set_id = 45
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 45, u.unit_id, 'IN', 'Tùng trả áo mẫu đi đo ĐX 91 - Thương', '2025-11-12 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

-- Request 80: ĐX 104 - Thúy (dòng 84) - set_id = 46
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 46, u.unit_id, 'OUT', NULL, '2025-12-06 00:00:00'
FROM units u WHERE u.unit_name = 'TT Đào tạo và Bồi dưỡng nghiệp vụ Bưu điện';

-- Request 81: ĐX 88 - Hương (dòng 85) - set_id = 47
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 47, u.unit_id, 'OUT', NULL, '2025-11-20 00:00:00'
FROM units u WHERE u.unit_name = 'Kho vận';

-- Request 82-84: ĐX 106 - Thúy (dòng 86,87,88) - set_id = 48
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 48, u.unit_id, 'OUT', NULL, '2025-12-06 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Thanh Trì - Đợt 2';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 48, u.unit_id, 'OUT', NULL, '2025-12-06 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Hà Tĩnh';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 48, u.unit_id, 'OUT', NULL, '2025-12-06 00:00:00'
FROM units u WHERE u.unit_name = 'Phát hành báo chí';

-- Request 85-86: ĐX 18 - Nga (dòng 89,90) - set_id = 49
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 49, u.unit_id, 'IN', 'BĐ Quảng Bình', '2025-12-08 00:00:00'
FROM units u WHERE u.unit_name = 'Kho';

INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 49, u.unit_id, 'OUT', NULL, '2025-12-08 00:00:00'
FROM units u WHERE u.unit_name = 'BĐ Quảng Bình';

-- Request 87: ĐX 107 - Thúy (dòng 91) - set_id = 50
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 50, u.unit_id, 'OUT', NULL, '2025-12-20 00:00:00'
FROM units u WHERE u.unit_name = 'Bưu điện Thái Nguyên';

-- Request 88: ĐX 94 - Hương (dòng 92) - set_id = 51
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 51, u.unit_id, 'OUT', NULL, '2025-12-12 00:00:00'
FROM units u WHERE u.unit_name = 'Khách lẻ, BĐ An Giang, BĐ Lạng Sơn, TCT Bưu điện, BĐ Hải Phòng';

-- Request 89: ĐX 97 - Hương (dòng 93) - set_id = 52
INSERT INTO inventory_requests (set_id, unit_id, request_type, note, created_at)
SELECT 52, u.unit_id, 'OUT', NULL, '2025-12-20 00:00:00'
FROM units u WHERE u.unit_name = 'TTKD Lai Châu, VT Thái Nguyên, TTKD Đồng Tháp, TTKD Tuyên Quang';
