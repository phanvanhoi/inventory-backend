-- =====================================================
-- PHẦN 8d: SƠ MI NAM 2026 — BATCH NHẬP LIỆU (SP20–SP32)
-- Nguồn: NHẬP LIỆU.csv | 13 sản phẩm | parent SP2
-- =====================================================

-- 8d-a: Master products
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
('HDH24S - KẺ NAM', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('BA0053 (THÊU VNPT TRẮNG - DÀI TAY THÊU MĂNG SÉC TAY', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('ÁO PHÒNG MÁY', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('TRẮNG TINH TTKD BA 0074 (CỘC TAY THÊU NGỰC - DÀI TAY THÊU MĂNG SÉC)', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('EMS', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('EMS ( ÁO TỒN CÁC NĂM TRƯỚC NÊN KHÁC CÂY VẢI)', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('TRẮNG KEM NAM TỔNG CÔNG TY BƯU ĐIỆN (KHÔNG LÉ', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('TRẮNG SM NAM MÃ K27 - TẬP ĐOÀN EVN (CỘC TAY THÊU NGỰC - DÀI TAY THÊU MĂNG SÉC)', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('XANH BT1443 (BƯU ĐIỆN TRUNG ƯƠNG)', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('NGÂN HÀNG INDOVINA (Vải BA0053 logo IVB cổ) - SLIM', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('MOBIPHONE (Vải BA0053 Phối cổ và măng sec)', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('THAN THỐNG NHẤT (Vải BA0053 logo LỘ TRÍ) - SLIM', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00'),
('Trắng TCT Bưu điện JP180249', 'STRUCTURED', 2, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00');

-- 8d-b: Variants (88 biến thể / SP, clone SP1)
-- SP20
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 20, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP21
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 21, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP22
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 22, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP23
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 23, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP24
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 24, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP25
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 25, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP26
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 26, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP27
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 27, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP28
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 28, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP29
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 29, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP30
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 30, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP31
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 31, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- SP32
INSERT INTO product_variants (product_id, style_id, size_id, length_type_id)
SELECT 32, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;

-- 8d-c: Tồn kho CÔNG TY
-- SP20: HDH24S - KẺ NAM
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP20 CÔNG TY 2026',
    'HDH24S - KẺ NAM: tồn thực tế (838 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp20_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp20_actual_set_id,
    u.unit_id,
    20,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP20',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp20_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp20_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 51 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 39 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 34 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 41 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 28 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 29 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 29 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 20
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP21: BA0053 (THÊU VNPT TRẮNG - DÀI TAY THÊU MĂNG SÉC TAY
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP21 CÔNG TY 2026',
    'BA0053 (THÊU VNPT TRẮNG - DÀI TAY THÊU MĂNG SÉC TAY: tồn thực tế (801 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp21_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp21_actual_set_id,
    u.unit_id,
    21,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP21',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp21_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp21_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 21
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP21 CÔNG TY 2026',
    'BA0053 (THÊU VNPT TRẮNG - DÀI TAY THÊU MĂNG SÉC TAY: ADJUST (0 IN / 29 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp21_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @sp21_expected_set_id,
    u.unit_id,
    21,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm SP21',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp21_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp21_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 21
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP22: ÁO PHÒNG MÁY
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP22 CÔNG TY 2026',
    'ÁO PHÒNG MÁY: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp22_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp22_actual_set_id,
    u.unit_id,
    22,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP22',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp22_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp22_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 22
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP23: TRẮNG TINH TTKD BA 0074 (CỘC TAY THÊU NGỰC - DÀI TAY THÊU MĂNG SÉC)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP23 CÔNG TY 2026',
    'TRẮNG TINH TTKD BA 0074 (CỘC TAY THÊU NGỰC - DÀI TAY THÊU MĂNG SÉC): tồn thực tế (230 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp23_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp23_actual_set_id,
    u.unit_id,
    23,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP23',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp23_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp23_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 23
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP23 CÔNG TY 2026',
    'TRẮNG TINH TTKD BA 0074 (CỘC TAY THÊU NGỰC - DÀI TAY THÊU...: ADJUST (0 IN / 2 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp23_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @sp23_expected_set_id,
    u.unit_id,
    23,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm SP23',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp23_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp23_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 23
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP24: EMS
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP24 CÔNG TY 2026',
    'EMS: tồn thực tế (16 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp24_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp24_actual_set_id,
    u.unit_id,
    24,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP24',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp24_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp24_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 24
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP25: EMS ( ÁO TỒN CÁC NĂM TRƯỚC NÊN KHÁC CÂY VẢI)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP25 CÔNG TY 2026',
    'EMS ( ÁO TỒN CÁC NĂM TRƯỚC NÊN KHÁC CÂY VẢI): tồn thực tế (15 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp25_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp25_actual_set_id,
    u.unit_id,
    25,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP25',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp25_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp25_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 25
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP26: TRẮNG KEM NAM TỔNG CÔNG TY BƯU ĐIỆN (KHÔNG LÉ
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP26 CÔNG TY 2026',
    'TRẮNG KEM NAM TỔNG CÔNG TY BƯU ĐIỆN (KHÔNG LÉ: tồn thực tế (0 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp26_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp26_actual_set_id,
    u.unit_id,
    26,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP26',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp26_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp26_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 26
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP27: TRẮNG SM NAM MÃ K27 - TẬP ĐOÀN EVN (CỘC TAY THÊU NGỰC - DÀI TAY THÊU MĂNG SÉC)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP27 CÔNG TY 2026',
    'TRẮNG SM NAM MÃ K27 - TẬP ĐOÀN EVN (CỘC TAY THÊU NGỰC - DÀI TAY THÊU MĂNG SÉC): tồn thực tế (36 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp27_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp27_actual_set_id,
    u.unit_id,
    27,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP27',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp27_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp27_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 27
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP28: XANH BT1443 (BƯU ĐIỆN TRUNG ƯƠNG)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP28 CÔNG TY 2026',
    'XANH BT1443 (BƯU ĐIỆN TRUNG ƯƠNG): tồn thực tế (85 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp28_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp28_actual_set_id,
    u.unit_id,
    28,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP28',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp28_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp28_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 28
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP29: NGÂN HÀNG INDOVINA (Vải BA0053 logo IVB cổ) - SLIM
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP29 CÔNG TY 2026',
    'NGÂN HÀNG INDOVINA (Vải BA0053 logo IVB cổ) - SLIM: tồn thực tế (8 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp29_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp29_actual_set_id,
    u.unit_id,
    29,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP29',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp29_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp29_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 29
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP30: MOBIPHONE (Vải BA0053 Phối cổ và măng sec)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP30 CÔNG TY 2026',
    'MOBIPHONE (Vải BA0053 Phối cổ và măng sec): tồn thực tế (15 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp30_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp30_actual_set_id,
    u.unit_id,
    30,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP30',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp30_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp30_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 30
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP31: THAN THỐNG NHẤT (Vải BA0053 logo LỘ TRÍ) - SLIM
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP31 CÔNG TY 2026',
    'THAN THỐNG NHẤT (Vải BA0053 logo LỘ TRÍ) - SLIM: tồn thực tế (84 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp31_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp31_actual_set_id,
    u.unit_id,
    31,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP31',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp31_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp31_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 31
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- SP32: Trắng TCT Bưu điện JP180249
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP32 CÔNG TY 2026',
    'Trắng TCT Bưu điện JP180249: tồn thực tế (14 chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp32_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @sp32_actual_set_id,
    u.unit_id,
    32,
    'IN',
    'EXECUTED',
    'Tồn thực tế SP32',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp32_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp32_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 32
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP32 CÔNG TY 2026',
    'Trắng TCT Bưu điện JP180249: ADJUST (1 IN / 0 OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @sp32_expected_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @sp32_expected_set_id,
    u.unit_id,
    32,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP32',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @sp32_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @sp32_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 2 AS qty
) v
JOIN product_variants pv ON pv.product_id = 32
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
