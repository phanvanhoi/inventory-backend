-- =====================================================
-- PHẦN 8a: TỒN KHO BAN ĐẦU HDH22 — CÔNG TY (Product 1)
-- Nguồn: HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU).csv
-- Dòng 4 = thực tế (request set EXECUTED) | Dòng 5 = dự kiến (request set APPROVED riêng)
-- ADJUST chỉ cộng vào tồn dự kiến khi request_set.status IN (PENDING, APPROVED, RECEIVING)
-- Tổng thực tế: 1094 chiếc | ADJUST_IN: 3 dòng | ADJUST_OUT: 43 dòng
-- Ô dự kiến âm trong CSV: 8 ô (giữ nguyên)
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - HDH22 CÔNG TY 2026',
    'HDH22 - TRẮNG KEM NAM BƯU ĐIỆN: tồn thực tế',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @hdh22_actual_set_id = LAST_INSERT_ID();
SET @hdh22_cong_ty_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1);

-- 8a-1: Tồn thực tế
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @hdh22_actual_set_id,
    u.unit_id,
    1,
    'IN',
    'EXECUTED',
    'Tồn thực tế HDH22 kho CÔNG TY',
    '2026-01-01 00:00:00',
    @hdh22_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @hdh22_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @hdh22_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 31 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 9 AS qty
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
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 15 AS qty
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
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 1
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- 8a-2: Request set riêng cho điều chỉnh dự kiến (phải APPROVED, không EXECUTED)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - HDH22 CÔNG TY 2026',
    'Điều chỉnh dự kiến HDH22 (ADJUST_IN/OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @hdh22_expected_set_id = LAST_INSERT_ID();

-- 8a-3: Điều chỉnh dự kiến nhập (ADJUST_IN)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @hdh22_expected_set_id,
    u.unit_id,
    1,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng tồn HDH22',
    '2026-01-01 00:00:00',
    @hdh22_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @hdh22_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @hdh22_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 1
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- 8a-4: Điều chỉnh dự kiến xuất (ADJUST_OUT)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @hdh22_expected_set_id,
    u.unit_id,
    1,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm tồn HDH22',
    '2026-01-01 00:00:00',
    @hdh22_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @hdh22_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @hdh22_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 35 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 38 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 42 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 10 AS qty
) v
JOIN product_variants pv ON pv.product_id = 1
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

