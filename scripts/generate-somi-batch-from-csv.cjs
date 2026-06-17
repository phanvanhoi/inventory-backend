/**
 * Batch: NHẬP LIỆU.csv → products (SP20+) + variants + tồn CÔNG TY
 * Block = 6 dòng: Tên SP | CỔ ĐIỂN header | Size | Cộc/Dài | Thực tế | Dự kiến
 */
const fs = require("fs");
const path = require("path");

const csvPath =
  process.argv[2] ||
  "c:\\Users\\RemoteUser\\Downloads\\NHẬP LIỆU.csv";
const startProductId = Number(process.argv[3] || 20);
const parentProductId = 2;

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

function parseCsvLines(filePath) {
  return fs
    .readFileSync(filePath, "utf8")
    .replace(/^\uFEFF/, "")
    .split(/\r?\n/)
    .filter((l) => l.trim());
}

function parseBlocks(lines) {
  const blocks = [];
  let i = 0;
  while (i < lines.length) {
    const name = lines[i].split(",")[0].replace(/^"|"$/g, "").trim();
    if (!name || name.startsWith("CỔ ĐIỂN") || name.startsWith("Size")) {
      throw new Error(`Unexpected line ${i + 1}: ${name.slice(0, 40)}`);
    }
    const header = lines[i + 1] || "";
    if (!header.includes("CỔ ĐIỂN")) {
      throw new Error(`Block "${name}": missing style header at line ${i + 2}`);
    }
    const rowActual = (lines[i + 4] || "").split(",");
    const rowExpected = (lines[i + 5] || "").split(",");
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
    if (col !== 88) throw new Error(`Block "${name}": expected 88 cols, got ${col}`);
    blocks.push({ name, items });
    i += 6;
  }
  return blocks;
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

function sqlVarPrefix(productId) {
  return `sp${productId}`;
}

function escapeSql(str) {
  return str.replace(/'/g, "''");
}

function inventorySeedSql(block, productId) {
  const { name, items } = block;
  const p = sqlVarPrefix(productId);
  const totalActual = items.reduce((s, i) => s + i.actual, 0);
  const adjustIn = items.filter((i) => i.diff > 0);
  const adjustOut = items.filter((i) => i.diff < 0);
  const actualUnion = unionRows(items, (i) => i.actual);
  const adjustInUnion = unionRows(adjustIn, (i) => i.diff);
  const adjustOutUnion = unionRows(adjustOut, (i) => -i.diff);
  const setShort = name.length > 60 ? `${name.slice(0, 57)}...` : name;

  let sql = `
-- SP${productId}: ${name}
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Tồn kho ban đầu - SP${productId} CÔNG TY 2026',
    '${escapeSql(name)}: tồn thực tế (${totalActual} chiếc)',
    'HANG_MAY_SAN',
    'EXECUTED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @${p}_actual_set_id = LAST_INSERT_ID();

INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, note, created_at, warehouse_id)
SELECT
    @${p}_actual_set_id,
    u.unit_id,
    ${productId},
    'IN',
    'EXECUTED',
    'Tồn thực tế SP${productId}',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
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
`;

  if (adjustIn.length || adjustOut.length) {
    sql += `
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP${productId} CÔNG TY 2026',
    '${escapeSql(setShort)}: ADJUST (${adjustIn.length} IN / ${adjustOut.length} OUT)',
    'HANG_MAY_SAN',
    'APPROVED',
    NULL,
    '2026-01-01 00:00:00',
    '2026-01-01 00:00:00'
);

SET @${p}_expected_set_id = LAST_INSERT_ID();
`;
  }

  if (adjustIn.length) {
    sql += `
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @${p}_expected_set_id,
    u.unit_id,
    ${productId},
    'ADJUST_IN',
    'APPROVED',
    '2026-06-30',
    'Dự kiến tăng SP${productId}',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
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
`;
  }

  if (adjustOut.length) {
    sql += `
INSERT INTO inventory_requests (set_id, unit_id, product_id, request_type, request_status, expected_date, note, created_at, warehouse_id)
SELECT
    @${p}_expected_set_id,
    u.unit_id,
    ${productId},
    'ADJUST_OUT',
    'APPROVED',
    '2026-06-30',
    'Dự kiến giảm SP${productId}',
    '2026-01-01 00:00:00',
    (SELECT warehouse_id FROM warehouses WHERE warehouse_name = 'CÔNG TY' LIMIT 1)
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
`;
  }

  return { sql, totalActual, adjustIn: adjustIn.length, adjustOut: adjustOut.length };
}

function main() {
  const lines = parseCsvLines(csvPath);
  const blocks = parseBlocks(lines);

  const productRows = blocks
    .map(
      (b, idx) =>
        `('${escapeSql(b.name)}', 'STRUCTURED', ${parentProductId}, 'Sơ mi nam 2026 - batch NHẬP LIỆU', '2026-01-01 00:00:00')`
    )
    .join(",\n");

  const variantClones = blocks
    .map((_, idx) => {
      const pid = startProductId + idx;
      return `-- SP${pid}\nINSERT INTO product_variants (product_id, style_id, size_id, length_type_id)\nSELECT ${pid}, style_id, size_id, length_type_id FROM product_variants WHERE product_id = 1;`;
    })
    .join("\n\n");

  const inventoryParts = [];
  const summary = [];
  blocks.forEach((block, idx) => {
    const productId = startProductId + idx;
    const { sql, totalActual, adjustIn, adjustOut } = inventorySeedSql(block, productId);
    inventoryParts.push(sql.trim());
    summary.push({ productId, name: block.name, totalActual, adjustIn, adjustOut });
  });

  const sql = `-- =====================================================
-- PHẦN 8d: SƠ MI NAM 2026 — BATCH NHẬP LIỆU (SP${startProductId}–SP${startProductId + blocks.length - 1})
-- Nguồn: ${path.basename(csvPath)} | ${blocks.length} sản phẩm | parent SP${parentProductId}
-- =====================================================

-- 8d-a: Master products
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
${productRows};

-- 8d-b: Variants (88 biến thể / SP, clone SP1)
${variantClones}

-- 8d-c: Tồn kho CÔNG TY
${inventoryParts.join("\n\n")}
`;

  const outPath = path.join(__dirname, "somi-batch-seed.sql");
  fs.writeFileSync(outPath, sql, "utf8");

  console.log("Blocks:", blocks.length);
  console.log("Product IDs:", startProductId, "-", startProductId + blocks.length - 1);
  summary.forEach((s) => {
    console.log(
      `  SP${s.productId}: actual=${s.totalActual} ADJ_IN=${s.adjustIn} ADJ_OUT=${s.adjustOut} | ${s.name.slice(0, 50)}`
    );
  });
  console.log("Written:", outPath);
}

main();
