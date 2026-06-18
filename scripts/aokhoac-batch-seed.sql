-- =====================================================
-- PHẦN 8e: ÁO KHOÁC 2026 — BATCH NHẬP ÁO KHOÁC (SP11, SP33–SP50)
-- Nguồn: NHẬP ÁO KHOÁC 2026.csv | 19 sản phẩm | parent SP3
-- SP11: cập nhật tên + tồn mới | Thực tế âm trong CSV → coi = 0
-- =====================================================

-- 8e-a: Cập nhật tên SP11 (dữ liệu cũ thay bằng CSV)
UPDATE products SET product_name = 'ÁO KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG' WHERE product_id = 11;

-- 8e-b: Sản phẩm con mới (SP33+)
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('ÁO KHOÁC 2 LỚP VIỄN THÔNG NGOÀI TRỜI', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC 3 LỚP VIỄN THÔNG NGOÀI TRỜI', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC PHÒNG MÁY VNPT', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC GIÓ KHÔNG PHỐI CỔ LOGO VNPT THÊU', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC EMS 2 LỚP', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC EMS 3 LỚP', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('AK GIÓ NÉT 1 - LOGO VNPT THÊU (TÍM THAN)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC BÔNG BƯU ĐIỆN - GHI PHỐI VÀNG', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC GIÓ BAN KHAI THÁC MẠNG (MẪU NĂM 2025)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC GIÓ BO TAY, BO CỔ VT BẮC NINH (LÓT KẺ CARO MẪU 2025)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO ĐIỀU HÒA (VIỄN THÔNG)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC GIÓ BO TAY, BO CỔ VT THÁI NGUYÊN (LÓT TÍM THAN MẪU 2025)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC GIÓ BO TAY, BO CỔ KHÔNG THÊU', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('KHOÁC VIỄN THÔNG THÁI NGUYÊN (XANH TƯƠI)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC HABECO TÍM THAN (CẢ CŨ VÀ MỚI MÀU HƠI KHÁC NHAU)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO KHOÁC HABECO GHI (CẢ CŨ VÀ MỚI MÀU HƠI KHÁC NHAU)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG (1 LỚP)', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00'),
('ÁO RÉT THỦY ĐIỆN HÒA BÌNH', 'STRUCTURED', 3, 'Áo khoác 2026 - batch NHẬP ÁO KHOÁC', '2026-01-01 00:00:00');

-- 8e-c: Variants (20 biến thể / SP = Size XS-6XL × NAM/NỮ, clone SP11)
-- SP33
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 33, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP34
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 34, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP35
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 35, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP36
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 36, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP37
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 37, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP38
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 38, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP39
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 39, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP40
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 40, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP41
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 41, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP42
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 42, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP43
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 43, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP44
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 44, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP45
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 45, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP46
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 46, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP47
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 47, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP48
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 48, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP49
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 49, size_id, gender FROM product_variants WHERE product_id = 11;

-- SP50
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 50, size_id, gender FROM product_variants WHERE product_id = 11;

-- 8e-d: Tồn kho CÔNG TY
-- SP11: ÁO KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP11 CÔNG TY 2026',
    'ÁO KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG: tồn thực tế (84 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak11_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak11_actual_set_id,
    u.unit_id,
    11,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP11',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak11_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak11_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 15 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 13 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 8 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 23 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 11
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP11 CÔNG TY 2026',
    'ÁO KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG: ADJUST (13 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak11_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ak11_expected_set_id,
    u.unit_id,
    11,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP11',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak11_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak11_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 13 AS size_id, 'NAM' AS gender, 186 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 328 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 390 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 177 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 53 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 45 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 197 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 268 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 368 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 139 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 25 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 14 AS qty
) v
JOIN product_variants pv ON pv.product_id = 11
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP33: ÁO KHOÁC 2 LỚP VIỄN THÔNG NGOÀI TRỜI
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP33 CÔNG TY 2026',
    'ÁO KHOÁC 2 LỚP VIỄN THÔNG NGOÀI TRỜI: tồn thực tế (471 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak33_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak33_actual_set_id,
    u.unit_id,
    33,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP33',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak33_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak33_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 67 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 101 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 157 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 35 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 92 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 8 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 33
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP33 CÔNG TY 2026',
    'ÁO KHOÁC 2 LỚP VIỄN THÔNG NGOÀI TRỜI: ADJUST (7 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak33_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ak33_expected_set_id,
    u.unit_id,
    33,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP33',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak33_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak33_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 13 AS size_id, 'NAM' AS gender, 60 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 50 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 200 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 110 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 60 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 10 AS qty
) v
JOIN product_variants pv ON pv.product_id = 33
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP34: ÁO KHOÁC 3 LỚP VIỄN THÔNG NGOÀI TRỜI
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP34 CÔNG TY 2026',
    'ÁO KHOÁC 3 LỚP VIỄN THÔNG NGOÀI TRỜI: tồn thực tế (5 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak34_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak34_actual_set_id,
    u.unit_id,
    34,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP34',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak34_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak34_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 34
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP35: ÁO KHOÁC PHÒNG MÁY VNPT
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP35 CÔNG TY 2026',
    'ÁO KHOÁC PHÒNG MÁY VNPT: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak35_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak35_actual_set_id,
    u.unit_id,
    35,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP35',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak35_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak35_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 35
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP36: ÁO KHOÁC GIÓ KHÔNG PHỐI CỔ LOGO VNPT THÊU
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP36 CÔNG TY 2026',
    'ÁO KHOÁC GIÓ KHÔNG PHỐI CỔ LOGO VNPT THÊU: tồn thực tế (215 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak36_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak36_actual_set_id,
    u.unit_id,
    36,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP36',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak36_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak36_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 24 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 36 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 15 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 14 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 15 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 23 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 47 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 14 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 36
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP37: ÁO KHOÁC EMS 2 LỚP
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP37 CÔNG TY 2026',
    'ÁO KHOÁC EMS 2 LỚP: tồn thực tế (22 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak37_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak37_actual_set_id,
    u.unit_id,
    37,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP37',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak37_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak37_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 6 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 37
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP38: ÁO KHOÁC EMS 3 LỚP
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP38 CÔNG TY 2026',
    'ÁO KHOÁC EMS 3 LỚP: tồn thực tế (9 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak38_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak38_actual_set_id,
    u.unit_id,
    38,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP38',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak38_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak38_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 38
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP39: AK GIÓ NÉT 1 - LOGO VNPT THÊU (TÍM THAN)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP39 CÔNG TY 2026',
    'AK GIÓ NÉT 1 - LOGO VNPT THÊU (TÍM THAN): tồn thực tế (15 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak39_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak39_actual_set_id,
    u.unit_id,
    39,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP39',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak39_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak39_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 5 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 39
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP40: ÁO KHOÁC BÔNG BƯU ĐIỆN - GHI PHỐI VÀNG
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP40 CÔNG TY 2026',
    'ÁO KHOÁC BÔNG BƯU ĐIỆN - GHI PHỐI VÀNG: tồn thực tế (48 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak40_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak40_actual_set_id,
    u.unit_id,
    40,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP40',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak40_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak40_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 11 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 5 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 40
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP41: ÁO KHOÁC GIÓ BAN KHAI THÁC MẠNG (MẪU NĂM 2025)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP41 CÔNG TY 2026',
    'ÁO KHOÁC GIÓ BAN KHAI THÁC MẠNG (MẪU NĂM 2025): tồn thực tế (51 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak41_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak41_actual_set_id,
    u.unit_id,
    41,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP41',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak41_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak41_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 9 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 41
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP42: ÁO KHOÁC GIÓ BO TAY, BO CỔ VT BẮC NINH (LÓT KẺ CARO MẪU 2025)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP42 CÔNG TY 2026',
    'ÁO KHOÁC GIÓ BO TAY, BO CỔ VT BẮC NINH (LÓT KẺ CARO MẪU 2025): tồn thực tế (138 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak42_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak42_actual_set_id,
    u.unit_id,
    42,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP42',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak42_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak42_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 15 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 12 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 27 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 15 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 13 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 27 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 16 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 42
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP43: ÁO ĐIỀU HÒA (VIỄN THÔNG)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP43 CÔNG TY 2026',
    'ÁO ĐIỀU HÒA (VIỄN THÔNG): tồn thực tế (15 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak43_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak43_actual_set_id,
    u.unit_id,
    43,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP43',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak43_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak43_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 43
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP44: ÁO KHOÁC GIÓ BO TAY, BO CỔ VT THÁI NGUYÊN (LÓT TÍM THAN MẪU 2025)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP44 CÔNG TY 2026',
    'ÁO KHOÁC GIÓ BO TAY, BO CỔ VT THÁI NGUYÊN (LÓT TÍM THAN MẪU 2025): tồn thực tế (15 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak44_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak44_actual_set_id,
    u.unit_id,
    44,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP44',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak44_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak44_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 44
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP45: ÁO KHOÁC GIÓ BO TAY, BO CỔ KHÔNG THÊU
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP45 CÔNG TY 2026',
    'ÁO KHOÁC GIÓ BO TAY, BO CỔ KHÔNG THÊU: tồn thực tế (29 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak45_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak45_actual_set_id,
    u.unit_id,
    45,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP45',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak45_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak45_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 8 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 45
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP46: KHOÁC VIỄN THÔNG THÁI NGUYÊN (XANH TƯƠI)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP46 CÔNG TY 2026',
    'KHOÁC VIỄN THÔNG THÁI NGUYÊN (XANH TƯƠI): tồn thực tế (16 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak46_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak46_actual_set_id,
    u.unit_id,
    46,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP46',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak46_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak46_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 46
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP47: ÁO KHOÁC HABECO TÍM THAN (CẢ CŨ VÀ MỚI MÀU HƠI KHÁC NHAU)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP47 CÔNG TY 2026',
    'ÁO KHOÁC HABECO TÍM THAN (CẢ CŨ VÀ MỚI MÀU HƠI KHÁC NHAU): tồn thực tế (13 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak47_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak47_actual_set_id,
    u.unit_id,
    47,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP47',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak47_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak47_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 47
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP48: ÁO KHOÁC HABECO GHI (CẢ CŨ VÀ MỚI MÀU HƠI KHÁC NHAU)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP48 CÔNG TY 2026',
    'ÁO KHOÁC HABECO GHI (CẢ CŨ VÀ MỚI MÀU HƠI KHÁC NHAU): tồn thực tế (14 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak48_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak48_actual_set_id,
    u.unit_id,
    48,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP48',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak48_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak48_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 48
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP49: KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG (1 LỚP)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP49 CÔNG TY 2026',
    'KHOÁC BƯU ĐIỆN - GHI PHỐI VÀNG (1 LỚP): tồn thực tế (29 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak49_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak49_actual_set_id,
    u.unit_id,
    49,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP49',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak49_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak49_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 8 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 49
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP50: ÁO RÉT THỦY ĐIỆN HÒA BÌNH
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP50 CÔNG TY 2026',
    'ÁO RÉT THỦY ĐIỆN HÒA BÌNH: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak50_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ak50_actual_set_id,
    u.unit_id,
    50,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP50',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak50_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak50_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 50
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP50 CÔNG TY 2026',
    'ÁO RÉT THỦY ĐIỆN HÒA BÌNH: ADJUST (0 IN / 1 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ak50_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ak50_expected_set_id,
    u.unit_id,
    50,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm SP50',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ak50_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ak50_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 15 AS size_id, 'NAM' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 50
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;
