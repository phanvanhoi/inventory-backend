-- Seed data: 3 accessory templates + missing product_variants (product_id=10 = PHỤ LIỆU)
-- Created: 2026-03-28

-- ═══════════════════════════════════════════════════════════════════════════
-- 1. Đảm bảo tất cả variants tồn tại trong product_variants (product_id = 10)
--    INSERT IGNORE: bỏ qua nếu đã có (unique key: product_id + item_code)
-- ═══════════════════════════════════════════════════════════════════════════
INSERT IGNORE INTO product_variants (product_id, item_code, item_name, unit)
VALUES
    (10, 'KHOA3',   'Khóa quần xanh tươi',                        'chiếc'),
    (10, 'KHOA4',   'Khóa quần ghi',                              'chiếc'),
    (10, 'MEX5',    'Mex mè đen (Khổ 1m)',                        'mét'),
    (10, 'MEX6',    'Mex cạp quần nam',                           'mét'),
    (10, 'LOT10',   'Lót túi kate đen (Làm BH) (khổ 1.5m)',      'mét'),
    (10, 'LOT13',   'Lót túi kate trắng (cắt sẵn) - NT nam',     'bộ'),
    (10, 'NHAM1',   'Nhám dính ghi',                              'mét'),
    (10, 'NHAM3',   'Nhám dính xanh tươi',                       'mét'),
    (10, 'K3',      'Dây phản quang 2cm không in',               'mét'),
    (10, 'K4',      'Dây phản quang có in VNPT',                 'mét'),
    (10, 'K5',      'Chun 3F',                                    'mét'),
    (10, 'MAC1',    'Mác sơ mi nam Hằng',                        'chiếc'),
    (10, 'KHUY1',   'Khuy áo ngoài trời',                        'chiếc'),
    (10, 'TUIBONG1','Túi bóng kính',                             'chiếc');

-- ═══════════════════════════════════════════════════════════════════════════
-- 2. Insert 3 templates
-- ═══════════════════════════════════════════════════════════════════════════
INSERT INTO accessory_templates (name, created_by, created_at)
VALUES
    ('Quần BH nam',      NULL, NOW()),
    ('Áo budong nam NT', NULL, NOW()),
    ('Quần NT nam',      NULL, NOW());

-- ═══════════════════════════════════════════════════════════════════════════
-- 3. Insert template items (variant_id lookup by item_code)
-- ═══════════════════════════════════════════════════════════════════════════

-- ─── Template 1: Quần BH nam ───────────────────────────────────────────────
INSERT INTO accessory_template_items (template_id, variant_id, item_code, item_name, rate, unit, sort_order)
SELECT t.id, pv.variant_id, pv.item_code, v.item_name, v.rate, v.unit, v.sort_order
FROM accessory_templates t
JOIN (
    SELECT 'KHOA4' AS code, 'Khóa quần ghi'                      AS item_name, 1.0000 AS rate, 'chiếc' AS unit, 0 AS sort_order
    UNION ALL SELECT 'MEX5',  'Mex mè đen (Khổ 1m)',             0.0100, 'mét', 1
    UNION ALL SELECT 'MEX6',  'Mex cạp quần nam',                0.0350, 'mét', 2
    UNION ALL SELECT 'LOT10', 'Lót túi kate đen (Làm BH) (khổ 1.5m)', 0.3500, 'mét', 3
    UNION ALL SELECT 'NHAM1', 'Nhám dính ghi',                   0.0600, 'mét', 4
    UNION ALL SELECT 'K3',    'Dây phản quang 2cm không in',     0.3600, 'mét', 5
    UNION ALL SELECT 'K5',    'Chun 3F',                         0.1800, 'mét', 6
) v ON TRUE
JOIN product_variants pv ON pv.product_id = 10 AND pv.item_code = v.code
WHERE t.name = 'Quần BH nam';

-- ─── Template 2: Áo budong nam NT ──────────────────────────────────────────
INSERT INTO accessory_template_items (template_id, variant_id, item_code, item_name, rate, unit, sort_order)
SELECT t.id, pv.variant_id, pv.item_code, v.item_name, v.rate, v.unit, v.sort_order
FROM accessory_templates t
JOIN (
    SELECT 'MAC1'     AS code, 'Mác sơ mi nam Hằng'    AS item_name, 1.0000  AS rate, 'chiếc' AS unit, 0 AS sort_order
    UNION ALL SELECT 'KHUY1', 'Khuy áo ngoài trời',    19.0000, 'chiếc', 1
    UNION ALL SELECT 'K4',    'Dây phản quang có in VNPT', 1.1000, 'mét',   2
    UNION ALL SELECT 'TUIBONG1', 'Túi bóng kính',       1.0000, 'chiếc', 3
) v ON TRUE
JOIN product_variants pv ON pv.product_id = 10 AND pv.item_code = v.code
WHERE t.name = 'Áo budong nam NT';

-- ─── Template 3: Quần NT nam ────────────────────────────────────────────────
INSERT INTO accessory_template_items (template_id, variant_id, item_code, item_name, rate, unit, sort_order)
SELECT t.id, pv.variant_id, pv.item_code, v.item_name, v.rate, v.unit, v.sort_order
FROM accessory_templates t
JOIN (
    SELECT 'KHOA3' AS code, 'Khóa quần xanh tươi'                    AS item_name, 1.0000 AS rate, 'chiếc' AS unit, 0 AS sort_order
    UNION ALL SELECT 'MEX6',     'Mex cạp quần nam',                  0.0350, 'mét', 1
    UNION ALL SELECT 'MEX5',     'Mex mè đen',                        0.0100, 'mét', 2
    UNION ALL SELECT 'LOT13',    'Lót túi kate trắng (cắt sẵn) - NT nam', 1.0000, 'bộ', 3
    UNION ALL SELECT 'K4',       'Dây phản quang có in VNPT',         0.4000, 'mét', 4
    UNION ALL SELECT 'K5',       'Chun 3F',                           0.1800, 'mét', 5
    UNION ALL SELECT 'NHAM3',    'Nhám dính xanh tươi',               0.0600, 'mét', 6
) v ON TRUE
JOIN product_variants pv ON pv.product_id = 10 AND pv.item_code = v.code
WHERE t.name = 'Quần NT nam';
