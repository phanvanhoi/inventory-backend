-- =====================================================
-- PHẦN 8f: ÁO PHÔNG 2026 — BATCH NHẬP ÁO PHÔNG (SP12, SP51–SP56)
-- Nguồn: NHẬP ÁO PHÔNG 2026.csv | 7 sản phẩm | parent SP4
-- SP12: cập nhật tên + tồn mới | Thực tế âm → 0
-- =====================================================

-- 8f-a: Cập nhật tên SP12
UPDATE products SET product_name = 'ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ' WHERE product_id = 12;

-- 8f-b: Sản phẩm con mới (SP51+)
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ (CÓ TÚI NGỰC)', 'STRUCTURED', 4, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00'),
('ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN', 'STRUCTURED', 4, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00'),
('ÁO PHÔNG TRẮNG VNPT', 'STRUCTURED', 4, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00'),
('ÁO PHÔNG XANH VNPT', 'STRUCTURED', 4, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00'),
('ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN MỚI 2025', 'STRUCTURED', 4, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00'),
('ÁO PHÔNG EMS (MỚI - IN EMS VIETNAM) MẪU 2025', 'STRUCTURED', 4, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00');

-- 8f-c: Variants (40 biến thể / SP = Size × Cộc/Dài × NAM/NỮ, clone SP12)
-- SP51
INSERT INTO product_variants (product_id, size_id, length_type_id, gender)
SELECT 51, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;

-- SP52
INSERT INTO product_variants (product_id, size_id, length_type_id, gender)
SELECT 52, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;

-- SP53
INSERT INTO product_variants (product_id, size_id, length_type_id, gender)
SELECT 53, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;

-- SP54
INSERT INTO product_variants (product_id, size_id, length_type_id, gender)
SELECT 54, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;

-- SP55
INSERT INTO product_variants (product_id, size_id, length_type_id, gender)
SELECT 55, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;

-- SP56
INSERT INTO product_variants (product_id, size_id, length_type_id, gender)
SELECT 56, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;

-- 8f-d: Tồn kho CÔNG TY
-- SP12: ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP12 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ: tồn thực tế (276 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap12_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap12_actual_set_id,
    u.unit_id,
    12,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP12',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap12_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap12_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 15 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 10 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 41 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 15 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 13 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 9 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 36 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 27 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 12 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 21 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 15 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 5 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 20 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 12
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP12 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ: ADJUST (17 IN / 1 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap12_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap12_expected_set_id,
    u.unit_id,
    12,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP12',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap12_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap12_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 150 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 30 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 500 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 80 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 502 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 80 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 250 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 50 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 70 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 40 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 30 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 70 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 50 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 70 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 30 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 12
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap12_expected_set_id,
    u.unit_id,
    12,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm SP12',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap12_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap12_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 12
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

-- SP51: ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ (CÓ TÚI NGỰC)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP51 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG PHỐI GHI) - BƯU TÁ (CÓ TÚI NGỰC): tồn thực tế (17 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap51_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap51_actual_set_id,
    u.unit_id,
    51,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP51',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap51_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap51_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 51
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

-- SP52: ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP52 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN: tồn thực tế (108 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap52_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap52_actual_set_id,
    u.unit_id,
    52,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP52',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap52_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap52_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 18 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 32 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 6 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 10 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 14 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 52
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP52 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN: ADJUST (5 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap52_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap52_expected_set_id,
    u.unit_id,
    52,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP52',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap52_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap52_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 2 AS qty
) v
JOIN product_variants pv ON pv.product_id = 52
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

-- SP53: ÁO PHÔNG TRẮNG VNPT
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP53 CÔNG TY 2026',
    'ÁO PHÔNG TRẮNG VNPT: tồn thực tế (54 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap53_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap53_actual_set_id,
    u.unit_id,
    53,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP53',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap53_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap53_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 6 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 15 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 53
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP53 CÔNG TY 2026',
    'ÁO PHÔNG TRẮNG VNPT: ADJUST (4 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap53_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap53_expected_set_id,
    u.unit_id,
    53,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP53',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap53_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap53_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 53
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

-- SP54: ÁO PHÔNG XANH VNPT
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP54 CÔNG TY 2026',
    'ÁO PHÔNG XANH VNPT: tồn thực tế (3 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap54_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap54_actual_set_id,
    u.unit_id,
    54,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP54',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap54_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap54_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 54
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP54 CÔNG TY 2026',
    'ÁO PHÔNG XANH VNPT: ADJUST (7 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap54_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap54_expected_set_id,
    u.unit_id,
    54,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP54',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap54_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap54_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 8 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 54
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

-- SP55: ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN MỚI 2025
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP55 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN MỚI 2025: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap55_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap55_actual_set_id,
    u.unit_id,
    55,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP55',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap55_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap55_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 55
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP55 CÔNG TY 2026',
    'ÁO PHÔNG BƯU ĐIỆN (VÀNG) - SỰ KIỆN MỚI 2025: ADJUST (7 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap55_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap55_expected_set_id,
    u.unit_id,
    55,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP55',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap55_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap55_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 13 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 11 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 43 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 24 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 5 AS qty
) v
JOIN product_variants pv ON pv.product_id = 55
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

-- SP56: ÁO PHÔNG EMS (MỚI - IN EMS VIETNAM) MẪU 2025
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP56 CÔNG TY 2026',
    'ÁO PHÔNG EMS (MỚI - IN EMS VIETNAM) MẪU 2025: tồn thực tế (48 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap56_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ap56_actual_set_id,
    u.unit_id,
    56,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP56',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap56_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap56_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 12 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 1 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 7 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 4 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 3 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 5 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 2 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NAM' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 12 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 13 AS size_id, 1 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 13 AS size_id, 2 AS length_type_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 14 AS size_id, 1 AS length_type_id, 'NU' AS gender, 4 AS qty
    UNION ALL
    SELECT 14 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 15 AS size_id, 1 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 15 AS size_id, 2 AS length_type_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 1 AS qty
    UNION ALL
    SELECT 17 AS size_id, 1 AS length_type_id, 'NU' AS gender, 6 AS qty
    UNION ALL
    SELECT 17 AS size_id, 2 AS length_type_id, 'NU' AS gender, 2 AS qty
    UNION ALL
    SELECT 18 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 18 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 19 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 20 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 1 AS length_type_id, 'NU' AS gender, 0 AS qty
    UNION ALL
    SELECT 21 AS size_id, 2 AS length_type_id, 'NU' AS gender, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 56
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP56 CÔNG TY 2026',
    'ÁO PHÔNG EMS (MỚI - IN EMS VIETNAM) MẪU 2025: ADJUST (2 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ap56_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ap56_expected_set_id,
    u.unit_id,
    56,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP56',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ap56_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ap56_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 16 AS size_id, 1 AS length_type_id, 'NU' AS gender, 3 AS qty
    UNION ALL
    SELECT 16 AS size_id, 2 AS length_type_id, 'NU' AS gender, 2 AS qty
) v
JOIN product_variants pv ON pv.product_id = 56
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;
