-- =====================================================
-- PHẦN 8b: TỒN KHO BAN ĐẦU HDH22 LÉ VÀNG VNPOST — CÔNG TY (Product 18)
-- Nguồn: HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (CÓ LÉ VÀNG, CÓ THÊU VNPOST).csv
-- HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (CÓ LÉ VÀNG, CÓ THÊU VNPOST)
-- Dòng 4 = thực tế (request set EXECUTED) | Dòng 5 = dự kiến (request set APPROVED riêng)
-- Tổng thực tế: 871 chiếc | ADJUST_IN: 33 dòng | ADJUST_OUT: 14 dòng
-- Ô dự kiến âm trong CSV: 0 ô (giữ nguyên)
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - HDH22 lé vàng VNPOST CÔNG TY 2026',
    'HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (CÓ LÉ VÀNG, CÓ THÊU VNPOST): tồn thực tế',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @hdh22_le_actual_set_id = LAST_INSERT_ID();
SET @hdh22_le_cong_ty_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1);

-- 8b-1: Tồn thực tế
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @hdh22_le_actual_set_id,
    u.unit_id,
    18,
    'IN',
    'EXECUTED',
    'Tồn thực tế HDH22 lé vàng VNPOST kho CÔNG TY',
    '2026-01-01 00:00:00',
    @hdh22_le_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @hdh22_le_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @hdh22_le_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 11 AS qty
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
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 12 AS qty
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
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 17 AS qty
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
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 18
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- 8b-2: Request set riêng cho điều chỉnh dự kiến (phải APPROVED, không EXECUTED)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - HDH22 lé vàng VNPOST CÔNG TY 2026',
    'Điều chỉnh dự kiến HDH22 lé vàng VNPOST (ADJUST_IN/OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @hdh22_le_expected_set_id = LAST_INSERT_ID();

-- 8b-3: Điều chỉnh dự kiến nhập (ADJUST_IN)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @hdh22_le_expected_set_id,
    u.unit_id,
    18,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng tồn HDH22 lé vàng VNPOST',
    '2026-01-01 00:00:00',
    @hdh22_le_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @hdh22_le_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @hdh22_le_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 35 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 29 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 29 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 10 AS qty
) v
JOIN product_variants pv ON pv.product_id = 18
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- 8b-4: Điều chỉnh dự kiến xuất (ADJUST_OUT)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @hdh22_le_expected_set_id,
    u.unit_id,
    18,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm tồn HDH22 lé vàng VNPOST',
    '2026-01-01 00:00:00',
    @hdh22_le_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @hdh22_le_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @hdh22_le_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 8 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 2 AS qty
) v
JOIN product_variants pv ON pv.product_id = 18
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

