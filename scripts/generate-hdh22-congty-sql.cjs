/**
 * Parse HDH22 matrix CSV → SQL seed (PHẦN 8a/8b)
 * Usage: node generate-hdh22-congty-sql.cjs <csvPath> [productId] [partLabel] [varPrefix] [outFile]
 * Row 4 = tồn thực tế (IN EXECUTED) | Row 5 = dự kiến (ADJUST, request set APPROVED riêng)
 */
const fs = require("fs");
const path = require("path");

const csvPath =
  process.argv[2] ||
  "c:\\Users\\RemoteUser\\Downloads\\HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU).csv";
const productId = Number(process.argv[3] || 1);
const partLabel = process.argv[4] || (productId === 1 ? "8a" : "8b");
const varPrefix = process.argv[5] || (productId === 1 ? "hdh22" : "hdh22_le");
const outFile = process.argv[6] || (productId === 1 ? "hdh22-seed.sql" : "hdh22-le-seed.sql");

const PRODUCT_LABELS = {
  1: "HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (KHÔNG LÉ, KHÔNG THÊU)",
  18: "HDH22 - TRẮNG KEM NAM BƯU ĐIỆN (CÓ LÉ VÀNG, CÓ THÊU VNPOST)",
};
const SET_SHORT = {
  1: "HDH22",
  18: "HDH22 lé vàng VNPOST",
};

const lines = fs
  .readFileSync(csvPath, "utf8")
  .replace(/^\uFEFF/, "")
  .split(/\r?\n/)
  .filter((l) => l.trim());

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
          styleId: STYLE_IDS[style],
          sizeId: SIZE_OFFSET[size],
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
  const productLabel = PRODUCT_LABELS[productId] || `Product ${productId}`;
  const setShort = SET_SHORT[productId] || `SP${productId}`;
  const csvName = path.basename(csvPath);

  const actualUnion = unionRows(items, (i) => i.actual);
  const adjustInUnion = unionRows(adjustIn, (i) => i.diff);
  const adjustOutUnion = unionRows(adjustOut, (i) => -i.diff);

  const p = varPrefix;
  const sql = `-- =====================================================
-- PHẦN ${partLabel}: TỒN KHO BAN ĐẦU ${setShort.toUpperCase()} — CÔNG TY (Product ${productId})
-- Nguồn: ${csvName}
-- ${productLabel}
-- Dòng 4 = thực tế (request set EXECUTED) | Dòng 5 = dự kiến (request set APPROVED riêng)
-- Tổng thực tế: ${totalActual} chiếc | ADJUST_IN: ${adjustIn.length} dòng | ADJUST_OUT: ${adjustOut.length} dòng
-- Ô dự kiến âm trong CSV: ${negativeExpected} ô (giữ nguyên)
-- =====================================================

INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - ${setShort} CÔNG TY 2026',
    '${productLabel}: tồn thực tế',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @${p}_actual_set_id = LAST_INSERT_ID();
SET @${p}_cong_ty_warehouse_id = (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1);

-- ${partLabel}-1: Tồn thực tế
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @${p}_actual_set_id,
    u.unit_id,
    ${productId},
    'IN',
    'EXECUTED',
    'Tồn thực tế ${setShort} kho CÔNG TY',
    '2026-01-01 00:00:00',
    @${p}_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @${p}_in_request_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @${p}_in_request_id, pv.variant_id, v.qty
FROM (
${actualUnion}
) v
JOIN product_variants pv ON pv.product_id = ${productId}
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
${
  adjustIn.length || adjustOut.length
    ? `
-- ${partLabel}-2: Request set riêng cho điều chỉnh dự kiến (phải APPROVED, không EXECUTED)
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - ${setShort} CÔNG TY 2026',
    'Điều chỉnh dự kiến ${setShort} (ADJUST_IN/OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @${p}_expected_set_id = LAST_INSERT_ID();
`
    : ""
}${
  adjustIn.length
    ? `
-- ${partLabel}-3: Điều chỉnh dự kiến nhập (ADJUST_IN)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @${p}_expected_set_id,
    u.unit_id,
    ${productId},
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng tồn ${setShort}',
    '2026-01-01 00:00:00',
    @${p}_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @${p}_adjust_in_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @${p}_adjust_in_id, pv.variant_id, v.qty
FROM (
${adjustInUnion}
) v
JOIN product_variants pv ON pv.product_id = ${productId}
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
`
    : ""
}${
  adjustOut.length
    ? `
-- ${partLabel}-4: Điều chỉnh dự kiến xuất (ADJUST_OUT)
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @${p}_expected_set_id,
    u.unit_id,
    ${productId},
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm tồn ${setShort}',
    '2026-01-01 00:00:00',
    @${p}_cong_ty_warehouse_id
FROM units u
WHERE u.unit_name = 'Kho'
LIMIT 1;

SET @${p}_adjust_out_id = LAST_INSERT_ID();

INSERT INTO inventory_request_items (request_id, variant_id, quantity)
SELECT @${p}_adjust_out_id, pv.variant_id, v.qty
FROM (
${adjustOutUnion}
) v
JOIN product_variants pv ON pv.product_id = ${productId}
  AND pv.style_id = v.style_id
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id;
`
    : ""
}
`;

  const outPath = path.join(__dirname, outFile);
  fs.writeFileSync(outPath, sql, "utf8");

  console.log("Product:", productId, partLabel);
  console.log("Items:", items.length);
  console.log("Total actual:", totalActual);
  console.log("ADJUST_IN lines:", adjustIn.length);
  console.log("ADJUST_OUT lines:", adjustOut.length);
  console.log("Negative expected cells:", negativeExpected);
  console.log("Written:", outPath);
}

main();
