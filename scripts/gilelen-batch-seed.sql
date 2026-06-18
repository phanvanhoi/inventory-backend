-- =====================================================
-- PHẦN 8g: ÁO LEN + GILE LEN 2026 — BATCH NHẬP ÁO LEN + GILE LEN (SP13, SP57–SP74)
-- Nguồn: NHẬP ÁO LEN + GILE LEN 2026.csv | 19 sản phẩm | parent SP5
-- SP13: cập nhật tên + tồn mới | Thực tế âm → 0
-- =====================================================

-- 8g-a: Cập nhật tên SP13
UPDATE products SET product_name = 'GILE LEN VNPT' WHERE product_id = 13;

-- 8g-b: Sản phẩm con mới (SP57+)
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('ÁO LEN DÀI TAY VNPT', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (MẪU THÊU MỚI) - LOGO VÀNG - CHỮ VÀNG', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (LOGO VÀNG - CHỮ VÀNG)', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (MẪU THÊU CŨ) LOGO VÀNG + CHỮ XANH', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (MẪU THÊU CŨ) LOGO VÀNG + CHỮ XANH', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI - LOGO VÀNG - CHỮ VÀNG', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (KHÔNG THÊU)', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN EMS - CỔ CÓ VIỀN CAM (MẪU CHUẨN) - CHỮ EMS MÀU CAM', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY EMS CỔ CÓ VIỀN CAM (CÓ THÊU MẪU MỚI) - CHỮ EMS MÀU CAM', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY TÍM THAN - KHÔNG VIỀN - KHÔNG THÊU', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN EMS - CỔ CÓ VIỀN CAM (MẪU THÊU CŨ) Logo thêu màu vàng', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('GILE LEN EMS - CỔ KHÔNG VIỀN (MẪU THÊU CŨ) - Chữ EMS màu xanh', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (KHÔNG THÊU)', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (KHÔNG THÊU)', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY EMS - CỔ CÓ VIỀN CAM - THÊU CŨ', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY EMS - CỔ KHÔNG VIỀN CAM - THÊU CŨ - CHỮ EMS MÀU XANH', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00'),
('ÁO LEN DÀI TAY EMS - CỔ CÓ VIỀN KHÔNG THÊU', 'STRUCTURED', 5, 'Áo len + Gile len 2026 - batch NHẬP ÁO LEN + GILE LEN', '2026-01-01 00:00:00');

-- 8g-c: Variants (20 biến thể / SP = Size XS-6XL × NAM/NỮ, clone SP13)
-- SP57
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 57, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP58
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 58, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP59
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 59, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP60
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 60, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP61
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 61, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP62
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 62, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP63
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 63, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP64
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 64, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP65
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 65, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP66
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 66, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP67
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 67, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP68
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 68, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP69
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 69, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP70
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 70, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP71
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 71, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP72
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 72, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP73
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 73, size_id, gender FROM product_variants WHERE product_id = 13;

-- SP74
INSERT INTO product_variants (product_id, size_id, gender)
SELECT 74, size_id, gender FROM product_variants WHERE product_id = 13;

-- 8g-d: Tồn kho CÔNG TY
-- SP13: GILE LEN VNPT
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP13 CÔNG TY 2026',
    'GILE LEN VNPT: tồn thực tế (647 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl13_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl13_actual_set_id,
    u.unit_id,
    13,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP13',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl13_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl13_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 7 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 42 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 124 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 115 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 13 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 14 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 34 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 155 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 83 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 25 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 14 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 11 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 13
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP57: ÁO LEN DÀI TAY VNPT
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP57 CÔNG TY 2026',
    'ÁO LEN DÀI TAY VNPT: tồn thực tế (89 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl57_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl57_actual_set_id,
    u.unit_id,
    57,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP57',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl57_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl57_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 18 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 12 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 8 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 9 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 9 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 15 AS qty
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
JOIN product_variants pv ON pv.product_id = 57
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP58: GILE LEN BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (MẪU THÊU MỚI) - LOGO VÀNG - CHỮ VÀNG
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP58 CÔNG TY 2026',
    'GILE LEN BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (MẪU THÊU MỚI) - LOGO VÀNG - CHỮ VÀNG: tồn thực tế (26 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl58_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl58_actual_set_id,
    u.unit_id,
    58,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP58',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl58_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl58_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 8 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 3 AS qty
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
    SELECT 16 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 58
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP59: GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (LOGO VÀNG - CHỮ VÀNG)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP59 CÔNG TY 2026',
    'GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (LOGO VÀNG - CHỮ VÀNG): tồn thực tế (39 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl59_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl59_actual_set_id,
    u.unit_id,
    59,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP59',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl59_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl59_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 6 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 1 AS qty
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
    SELECT 14 AS size_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 12 AS qty
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
JOIN product_variants pv ON pv.product_id = 59
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP60: GILE LEN BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (MẪU THÊU CŨ) LOGO VÀNG + CHỮ XANH
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP60 CÔNG TY 2026',
    'GILE LEN BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (MẪU THÊU CŨ) LOGO VÀNG + CHỮ XANH: tồn thực tế (41 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl60_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl60_actual_set_id,
    u.unit_id,
    60,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP60',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl60_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl60_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 20 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 10 AS qty
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
    SELECT 13 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 2 AS qty
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
JOIN product_variants pv ON pv.product_id = 60
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP61: GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (MẪU THÊU CŨ) LOGO VÀNG + CHỮ XANH
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP61 CÔNG TY 2026',
    'GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (MẪU THÊU CŨ) LOGO VÀNG + CHỮ XANH: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl61_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl61_actual_set_id,
    u.unit_id,
    61,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP61',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl61_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl61_in_request_id, pv.variant_id, v.qty
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
JOIN product_variants pv ON pv.product_id = 61
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP62: ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI - LOGO VÀNG - CHỮ VÀNG
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP62 CÔNG TY 2026',
    'ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI - LOGO VÀNG - CHỮ VÀNG: tồn thực tế (10 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl62_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl62_actual_set_id,
    u.unit_id,
    62,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP62',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl62_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl62_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 0 AS qty
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
JOIN product_variants pv ON pv.product_id = 62
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP62 CÔNG TY 2026',
    'ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI - LOGO VÀNG - CHỮ VÀNG: ADJUST (4 IN / 5 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl62_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @gl62_expected_set_id,
    u.unit_id,
    62,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP62',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl62_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl62_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 14 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 62
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @gl62_expected_set_id,
    u.unit_id,
    62,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm SP62',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl62_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl62_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 15 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 62
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP63: ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP63 CÔNG TY 2026',
    'ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (CÓ THÊU VNPOST) MẪU MỚI: tồn thực tế (114 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl63_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl63_actual_set_id,
    u.unit_id,
    63,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP63',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl63_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl63_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 12 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 13 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 26 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 14 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 10 AS qty
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
JOIN product_variants pv ON pv.product_id = 63
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP64: GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (KHÔNG THÊU)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP64 CÔNG TY 2026',
    'GILE LEN BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (KHÔNG THÊU): tồn thực tế (246 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl64_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl64_actual_set_id,
    u.unit_id,
    64,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP64',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl64_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl64_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 46 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 12 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 37 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 101 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 25 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 64
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP65: GILE LEN EMS - CỔ CÓ VIỀN CAM (MẪU CHUẨN) - CHỮ EMS MÀU CAM
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP65 CÔNG TY 2026',
    'GILE LEN EMS - CỔ CÓ VIỀN CAM (MẪU CHUẨN) - CHỮ EMS MÀU CAM: tồn thực tế (82 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl65_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl65_actual_set_id,
    u.unit_id,
    65,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP65',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl65_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl65_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 9 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 9 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 17 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 19 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 65
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP66: ÁO LEN DÀI TAY EMS CỔ CÓ VIỀN CAM (CÓ THÊU MẪU MỚI) - CHỮ EMS MÀU CAM
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP66 CÔNG TY 2026',
    'ÁO LEN DÀI TAY EMS CỔ CÓ VIỀN CAM (CÓ THÊU MẪU MỚI) - CHỮ EMS MÀU CAM: tồn thực tế (39 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl66_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl66_actual_set_id,
    u.unit_id,
    66,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP66',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl66_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl66_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 17 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 7 AS qty
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
    SELECT 13 AS size_id, 'NU' AS gender, 5 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 3 AS qty
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
JOIN product_variants pv ON pv.product_id = 66
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP67: ÁO LEN DÀI TAY TÍM THAN - KHÔNG VIỀN - KHÔNG THÊU
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP67 CÔNG TY 2026',
    'ÁO LEN DÀI TAY TÍM THAN - KHÔNG VIỀN - KHÔNG THÊU: tồn thực tế (54 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl67_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl67_actual_set_id,
    u.unit_id,
    67,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP67',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl67_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl67_in_request_id, pv.variant_id, v.qty
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
    SELECT 13 AS size_id, 'NU' AS gender, 35 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 18 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 1 AS qty
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
JOIN product_variants pv ON pv.product_id = 67
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP68: GILE LEN EMS - CỔ CÓ VIỀN CAM (MẪU THÊU CŨ) Logo thêu màu vàng
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP68 CÔNG TY 2026',
    'GILE LEN EMS - CỔ CÓ VIỀN CAM (MẪU THÊU CŨ) Logo thêu màu vàng: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl68_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl68_actual_set_id,
    u.unit_id,
    68,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP68',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl68_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl68_in_request_id, pv.variant_id, v.qty
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
JOIN product_variants pv ON pv.product_id = 68
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP69: GILE LEN EMS - CỔ KHÔNG VIỀN (MẪU THÊU CŨ) - Chữ EMS màu xanh
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP69 CÔNG TY 2026',
    'GILE LEN EMS - CỔ KHÔNG VIỀN (MẪU THÊU CŨ) - Chữ EMS màu xanh: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl69_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl69_actual_set_id,
    u.unit_id,
    69,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP69',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl69_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl69_in_request_id, pv.variant_id, v.qty
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
JOIN product_variants pv ON pv.product_id = 69
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP70: ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (KHÔNG THÊU)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP70 CÔNG TY 2026',
    'ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ KHÔNG VIỀN VÀNG (KHÔNG THÊU): tồn thực tế (40 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl70_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl70_actual_set_id,
    u.unit_id,
    70,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP70',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl70_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl70_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 7 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
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
    SELECT 13 AS size_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 9 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 10 AS qty
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
JOIN product_variants pv ON pv.product_id = 70
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP71: ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (KHÔNG THÊU)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP71 CÔNG TY 2026',
    'ÁO LEN DÀI TAY BƯU ĐIỆN - CỔ CÓ VIỀN VÀNG (KHÔNG THÊU): tồn thực tế (54 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl71_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl71_actual_set_id,
    u.unit_id,
    71,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP71',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl71_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl71_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 'NAM' AS gender, 6 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NAM' AS gender, 13 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 17 AS size_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 'NAM' AS gender, 3 AS qty
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
    SELECT 15 AS size_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 16 AS size_id, 'NU' AS gender, 3 AS qty
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
JOIN product_variants pv ON pv.product_id = 71
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP72: ÁO LEN DÀI TAY EMS - CỔ CÓ VIỀN CAM - THÊU CŨ
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP72 CÔNG TY 2026',
    'ÁO LEN DÀI TAY EMS - CỔ CÓ VIỀN CAM - THÊU CŨ: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl72_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl72_actual_set_id,
    u.unit_id,
    72,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP72',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl72_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl72_in_request_id, pv.variant_id, v.qty
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
JOIN product_variants pv ON pv.product_id = 72
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP73: ÁO LEN DÀI TAY EMS - CỔ KHÔNG VIỀN CAM - THÊU CŨ - CHỮ EMS MÀU XANH
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP73 CÔNG TY 2026',
    'ÁO LEN DÀI TAY EMS - CỔ KHÔNG VIỀN CAM - THÊU CŨ - CHỮ EMS MÀU XANH: tồn thực tế (1 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl73_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl73_actual_set_id,
    u.unit_id,
    73,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP73',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl73_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl73_in_request_id, pv.variant_id, v.qty
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
    SELECT 16 AS size_id, 'NU' AS gender, 1 AS qty
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
JOIN product_variants pv ON pv.product_id = 73
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;

-- SP74: ÁO LEN DÀI TAY EMS - CỔ CÓ VIỀN KHÔNG THÊU
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP74 CÔNG TY 2026',
    'ÁO LEN DÀI TAY EMS - CỔ CÓ VIỀN KHÔNG THÊU: tồn thực tế (23 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @gl74_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @gl74_actual_set_id,
    u.unit_id,
    74,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP74',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @gl74_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @gl74_in_request_id, pv.variant_id, v.qty
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
    SELECT 13 AS size_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 14 AS size_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 15 AS size_id, 'NU' AS gender, 5 AS qty
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
JOIN product_variants pv ON pv.product_id = 74
  AND pv.size_id = v.size_id
  AND pv.gender = v.gender;
