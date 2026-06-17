-- =====================================================
-- PHẦN 8c: TỒN KHO BAN ĐẦU KẺ GÂN NAM HLD — CÔNG TY (Product 19)
-- Nguồn: KẺ GÂN NAM HLD.csv
-- KẺ GÂN NAM HLD
-- Dòng 4 = thực tế (request set EXECUTED) | Dòng 5 = dự kiến (request set APPROVED riêng)
-- Tổng thực tế: 1518 chiếc | ADJUST_IN: 42 dòng | ADJUST_OUT: 13 dòng
-- Ô dự kiến âm trong CSV: 0 ô (giữ nguyên)
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - Kẻ gân nam HLD CÔNG TY 2026',
    'KẺ GÂN NAM HLD: tồn thực tế',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ke_gan_hld_actual_set_id = LAST_INSERT_ID();
SET @ke_gan_hld_cong_ty_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1);

-- 8c-1: Tồn thực tế
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @ke_gan_hld_actual_set_id,
    u.unit_id,
    19,
    'IN',
    'EXECUTED',
    'Tồn thực tế Kẻ gân nam HLD kho CÔNG TY',
    '2026-01-01 00:00:00',
    @ke_gan_hld_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ke_gan_hld_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ke_gan_hld_in_request_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 1 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 1 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 2 AS size_id, 2 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 27 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 33 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 42 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 40 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 44 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 37 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 43 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 27 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 33 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 1 AS style_id, 11 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 2 AS style_id, 2 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 28 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 18 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 9 AS size_id, 2 AS length_type_id, 15 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 2 AS style_id, 10 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 2 AS style_id, 11 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 1 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 2 AS size_id, 2 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 1 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 27 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 29 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 34 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 11 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 11 AS qty
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
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 2 AS length_type_id, 22 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 35 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 20 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 26 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 30 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 16 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 24 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 2 AS length_type_id, 19 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 10 AS size_id, 2 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 1 AS length_type_id, 0 AS qty
    UNION ALL
    SELECT 4 AS style_id, 11 AS size_id, 2 AS length_type_id, 0 AS qty
) v
JOIN product_variants pv ON pv.product_id = 19
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- 8c-2: Request set riêng cho điều chỉnh dự kiến (phải APPROVED, không EXECUTED)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - Kẻ gân nam HLD CÔNG TY 2026',
    'Điều chỉnh dự kiến Kẻ gân nam HLD (ADJUST_IN/OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @ke_gan_hld_expected_set_id = LAST_INSERT_ID();

-- 8c-3: Điều chỉnh dự kiến nhập (ADJUST_IN)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ke_gan_hld_expected_set_id,
    u.unit_id,
    19,
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng tồn Kẻ gân nam HLD',
    '2026-01-01 00:00:00',
    @ke_gan_hld_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ke_gan_hld_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ke_gan_hld_adjust_in_id, pv.variant_id, v.qty
FROM (
    SELECT 1 AS style_id, 3 AS size_id, 1 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 1 AS style_id, 3 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 1 AS length_type_id, 48 AS qty
    UNION ALL
    SELECT 1 AS style_id, 4 AS size_id, 2 AS length_type_id, 68 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 1 AS length_type_id, 34 AS qty
    UNION ALL
    SELECT 1 AS style_id, 5 AS size_id, 2 AS length_type_id, 54 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 1 AS style_id, 6 AS size_id, 2 AS length_type_id, 40 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 1 AS length_type_id, 27 AS qty
    UNION ALL
    SELECT 1 AS style_id, 7 AS size_id, 2 AS length_type_id, 42 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 1 AS style_id, 8 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 9 AS size_id, 2 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 1 AS length_type_id, 7 AS qty
    UNION ALL
    SELECT 1 AS style_id, 10 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 1 AS length_type_id, 13 AS qty
    UNION ALL
    SELECT 2 AS style_id, 3 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 2 AS style_id, 4 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 2 AS style_id, 5 AS size_id, 2 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 1 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 2 AS style_id, 6 AS size_id, 2 AS length_type_id, 25 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 2 AS style_id, 7 AS size_id, 2 AS length_type_id, 12 AS qty
    UNION ALL
    SELECT 3 AS style_id, 3 AS size_id, 2 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 1 AS length_type_id, 14 AS qty
    UNION ALL
    SELECT 3 AS style_id, 4 AS size_id, 2 AS length_type_id, 23 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 1 AS length_type_id, 9 AS qty
    UNION ALL
    SELECT 3 AS style_id, 5 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 3 AS style_id, 6 AS size_id, 2 AS length_type_id, 17 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 1 AS length_type_id, 10 AS qty
    UNION ALL
    SELECT 3 AS style_id, 7 AS size_id, 2 AS length_type_id, 21 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 1 AS length_type_id, 6 AS qty
    UNION ALL
    SELECT 3 AS style_id, 8 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 1 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 4 AS style_id, 5 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 6 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 1 AS length_type_id, 3 AS qty
) v
JOIN product_variants pv ON pv.product_id = 19
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

-- 8c-4: Điều chỉnh dự kiến xuất (ADJUST_OUT)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @ke_gan_hld_expected_set_id,
    u.unit_id,
    19,
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm tồn Kẻ gân nam HLD',
    '2026-01-01 00:00:00',
    @ke_gan_hld_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @ke_gan_hld_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @ke_gan_hld_adjust_out_id, pv.variant_id, v.qty
FROM (
    SELECT 2 AS style_id, 8 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 2 AS style_id, 8 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 9 AS size_id, 2 AS length_type_id, 3 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 1 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 3 AS style_id, 10 AS size_id, 2 AS length_type_id, 1 AS qty
    UNION ALL
    SELECT 4 AS style_id, 3 AS size_id, 1 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 4 AS size_id, 2 AS length_type_id, 4 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 1 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 7 AS size_id, 2 AS length_type_id, 2 AS qty
    UNION ALL
    SELECT 4 AS style_id, 8 AS size_id, 2 AS length_type_id, 5 AS qty
    UNION ALL
    SELECT 4 AS style_id, 9 AS size_id, 1 AS length_type_id, 1 AS qty
) v
JOIN product_variants pv ON pv.product_id = 19
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;

