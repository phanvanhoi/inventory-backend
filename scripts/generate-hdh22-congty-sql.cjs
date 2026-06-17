/**
 * Parse HDH22 matrix CSV → SQL seed (PHẦN 8a)
 * Row 4 = tồn thực tế (IN EXECUTED)
 * Row 5 = tồn dự kiến (có thể âm) → ADJUST_IN/OUT APPROVED where diff != 0
 */
const fs = require("fs");
const path = require("path");

const csvPath =
  process.argv[2] ||
  "c:\\Users\\RemoteUser\\Downloads\\HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU).csv";

const lines = fs
  .readFileSync(csvPath, "utf8")
  .replace(/^\uFEFF/, "")
  .split(/\r?\n/)
  .filter((l) => l.trim());

const rowStyles = lines[0].split(",");
const rowSizes = lines[1].split(",");
const rowLengths = lines[2].split(",");
const rowActual = lines[3].split(",");
const rowExpected = lines[4].split(",");

const STYLES = ["CỔ ĐIỂN", "CỔ ĐIỂN NGẮN", "SLIM", "SLIM Ngắn"];
const STYLE_IDS = { "CỔ ĐIỂN": 1, "CỔ ĐIỂN NGẮN": 2, SLIM: 3, "SLIM Ngắn": 4 };
const SIZE_OFFSET = { 35: 1, 36: 2, 37: 3, 38: 4, 39: 5, 40: 6, 41: 7, 42: 8, 43: 9, 44: 10, 45: 11 };
const LENGTH_IDS = { Cộc: 1, Dài: 2 };

function parseQty(raw) {
  const s = (raw || "").trim();
  if (!s) return 0;
  const n = Number(s.replace(",", "."));
  return Number.isFinite(n) ? n : 0;
}

function parseMatrix() {
  const items = [];
  let col = 0;
  for (const style of STYLES) {
    for (let size = 35; size <= 45; size++) {
      for (const length of ["Cộc", "Dài"]) {
        const actual = parseQty(rowActual[col]);
        const expected = parseQty(rowExpected[col]);
        items.push({
          style,
          styleId: STYLE_IDS[style],
          size,
          sizeId: SIZE_OFFSET[size],
          length,
          lengthId: LENGTH_IDS[length],
          actual,
          expected,
          diff: expected - actual,
        });
        col++;
      }
    }
  }
  if (col !== 88) throw new Error(`Expected 88 columns, got ${col}`);
  return items;
}

function unionRows(items, pick, { skipZero = false } = {}) {
  const rows = skipZero ? items.filter((i) => pick(i) !== 0) : items;
  return rows
    .map(
      (i) =>
        `    SELECT ${i.styleId} AS style_id, ${i.sizeId} AS size_id, ${i.lengthId} AS length_type_id, ${pick(i)} AS qty`
    )
    .join("\n    UNION ALL\n");
}

function main() {
  const items = parseMatrix();
  const totalActual = items.reduce((s, i) => s + i.actual, 0);
  const adjustIn = items.filter((i) => i.diff > 0);
  const adjustOut = items.filter((i) => i.diff < 0);
  const negativeExpected = items.filter((_, idx) => parseQty(rowExpected[idx]) < 0).length;

  const actualUnion = unionRows(items, (i) => i.actual);
  const adjustInUnion = unionRows(adjustIn, (i) => i.diff);
  const adjustOutUnion = unionRows(adjustOut, (i) => -i.diff);

  const sql = `-- =====================================================
-- PHẦN 8a: TỒN KHO BAN ĐẦU HDH22 — CÔNG TY (Product 1)
-- Nguồn: HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU).csv
-- Dòng 4 = thực tế (IN EXECUTED) | Dòng 5 = dự kiến (ADJUST APPROVED, có thể âm)
-- Tổng thực tế: ${totalActual} chiếc | ADJUST_IN: ${adjustIn.length} dòng | ADJUST_OUT: ${adjustOut.length} dòng
-- Ô dự kiến âm trong CSV: ${negativeExpected} ô (giữ nguyên)
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - HDH22 CÔNG TY 2026',
    'HDH22 - TRẮNG KEM NAM BƯU ĐIỆN: thực tế + điều chỉnh dự kiến',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @hdh22_set_id = LAST_INSERT_ID();
SET @hdh22_cong_ty_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1);

-- 8a-1: Tồn thực tế
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @hdh22_set_id,
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
${actualUnion}
) v
JOIN product_variants pv ON pv.product_id = 1
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
${
  adjustIn.length
    ? `
-- 8a-2: Điều chỉnh dự kiến nhập (ADJUST_IN)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @hdh22_set_id,
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
${adjustInUnion}
) v
JOIN product_variants pv ON pv.product_id = 1
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
`
    : ""
}${
  adjustOut.length
    ? `
-- 8a-3: Điều chỉnh dự kiến xuất (ADJUST_OUT)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @hdh22_set_id,
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
${adjustOutUnion}
) v
JOIN product_variants pv ON pv.product_id = 1
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
`
    : ""
}
`;

  const outPath = path.join(__dirname, "hdh22-seed.sql");
  fs.writeFileSync(outPath, sql, "utf8");

  console.log("Items:", items.length);
  console.log("Total actual:", totalActual);
  console.log("ADJUST_IN lines:", adjustIn.length);
  console.log("ADJUST_OUT lines:", adjustOut.length);
  console.log("Negative expected cells:", negativeExpected);
  console.log("Written:", outPath);
}

main();
