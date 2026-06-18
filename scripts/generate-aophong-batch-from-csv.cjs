/**
 * Batch: NHẬP ÁO PHÔNG 2026.csv → SP12 (update) + SP51+ (children SP4)
 * Block = 6 dòng: Tên | NAM/NỮ | Size headers | Cộc/Dài | Thực tế | Dự kiến
 * 40 cột = 10 size × 2 độ dài × 2 giới (NAM + NỮ)
 */
const fs = require("fs");
const path = require("path");

const csvPath =
  process.argv[2] ||
  "c:\\Users\\RemoteUser\\Downloads\\NHẬP ÁO PHÔNG 2026.csv";
const parentProductId = 4;

const SIZES = ["XS", "S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL"];
const SIZE_IDS = { XS: 12, S: 13, M: 14, L: 15, XL: 16, "2XL": 17, "3XL": 18, "4XL": 19, "5XL": 20, "6XL": 21 };
const LENGTH_IDS = { Cộc: 1, Dài: 2 };
const GENDERS = [
  { code: "NAM", colStart: 0 },
  { code: "NU", colStart: 20 },
];

function productIdForIndex(i) {
  return i === 0 ? 12 : 50 + i;
}

function parseQty(raw, { clampActualNegative = false } = {}) {
  const s = (raw || "").trim();
  if (!s) return 0;
  const n = Number(s.replace(",", "."));
  if (!Number.isFinite(n)) return 0;
  if (clampActualNegative && n < 0) return 0;
  return n;
}

function parseProductName(line) {
  const trimmed = line.trim();
  if (trimmed.startsWith('"')) {
    const end = trimmed.indexOf('"', 1);
    if (end > 0) return trimmed.slice(1, end).trim();
  }
  return trimmed.split(",")[0].trim();
}

function parseBlocks(lines) {
  const blocks = [];
  let i = 0;
  while (i < lines.length) {
    const name = parseProductName(lines[i]);
    const header = lines[i + 1] || "";
    if (!header.includes("NAM") || !header.includes("NỮ")) {
      throw new Error(`Block "${name}": missing NAM/NỮ at line ${i + 2}`);
    }
    const lengthRow = lines[i + 3] || "";
    if (!lengthRow.includes("Cộc") || !lengthRow.includes("Dài")) {
      throw new Error(`Block "${name}": missing Cộc/Dài row at line ${i + 4}`);
    }
    const rowActual = (lines[i + 4] || "").split(",");
    const rowExpected = (lines[i + 5] || "").split(",");
    const items = [];
    for (const g of GENDERS) {
      SIZES.forEach((size, sizeIdx) => {
        ["Cộc", "Dài"].forEach((length, lenIdx) => {
          const col = g.colStart + sizeIdx * 2 + lenIdx;
          const actual = parseQty(rowActual[col], { clampActualNegative: true });
          const expected = parseQty(rowExpected[col]);
          items.push({
            sizeId: SIZE_IDS[size],
            lengthId: LENGTH_IDS[length],
            gender: g.code,
            actual,
            expected,
            diff: expected - actual,
          });
        });
      });
    }
    if (items.length !== 40) throw new Error(`Block "${name}": expected 40 items, got ${items.length}`);
    blocks.push({ name, items, productId: productIdForIndex(blocks.length) });
    i += 6;
  }
  return blocks;
}

function unionRows(items, pick, { skipZero = false } = {}) {
  const rows = skipZero ? items.filter((i) => pick(i) !== 0) : items;
  return rows
    .map(
      (i) =>
        `    SELECT ${i.sizeId} AS size_id, ${i.lengthId} AS length_type_id, '${i.gender}' AS gender, ${pick(i)} AS qty`
    )
    .join("\n    UNION ALL\n");
}

function escapeSql(str) {
  return str.replace(/'/g, "''");
}

function inventorySeedSql(block) {
  const { name, items, productId } = block;
  const p = `ap${productId}`;
  const totalActual = items.reduce((s, i) => s + i.actual, 0);
  const adjustIn = items.filter((i) => i.diff > 0);
  const adjustOut = items.filter((i) => i.diff < 0);
  const actualUnion = unionRows(items, (i) => i.actual);
  const adjustInUnion = unionRows(adjustIn, (i) => i.diff);
  const adjustOutUnion = unionRows(adjustOut, (i) => -i.diff);

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
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;
`;

  if (adjustIn.length || adjustOut.length) {
    sql += `
INSERT INTO request_sets (set_name, description, category, status, created_by, created_at, submitted_at)
VALUES (
    'Dự kiến tồn - SP${productId} CÔNG TY 2026',
    '${escapeSql(name)}: ADJUST (${adjustIn.length} IN / ${adjustOut.length} OUT)',
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
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;
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
  AND pv.size_id = v.size_id
  AND pv.length_type_id = v.length_type_id
  AND pv.gender = v.gender;
`;
  }

  return { sql, totalActual, adjustIn: adjustIn.length, adjustOut: adjustOut.length };
}

function main() {
  const lines = fs
    .readFileSync(csvPath, "utf8")
    .replace(/^\uFEFF/, "")
    .split(/\r?\n/)
    .filter((l) => l.trim());

  const blocks = parseBlocks(lines);
  const newBlocks = blocks.slice(1);
  const sp12Name = blocks[0].name;

  const newProductRows = newBlocks
    .map((b) => `('${escapeSql(b.name)}', 'STRUCTURED', ${parentProductId}, 'Áo phông 2026 - batch NHẬP ÁO PHÔNG', '2026-01-01 00:00:00')`)
    .join(",\n");

  const variantClones = newBlocks
    .map((b) => {
      const pid = b.productId;
      return `-- SP${pid}\nINSERT INTO product_variants (product_id, size_id, length_type_id, gender)\nSELECT ${pid}, size_id, length_type_id, gender FROM product_variants WHERE product_id = 12;`;
    })
    .join("\n\n");

  const inventoryParts = blocks.map((b) => inventorySeedSql(b).sql.trim());

  const lastId = blocks[blocks.length - 1].productId;
  const sql = `-- =====================================================
-- PHẦN 8f: ÁO PHÔNG 2026 — BATCH NHẬP ÁO PHÔNG (SP12, SP51–SP${lastId})
-- Nguồn: ${path.basename(csvPath)} | ${blocks.length} sản phẩm | parent SP${parentProductId}
-- SP12: cập nhật tên + tồn mới | Thực tế âm → 0
-- =====================================================

-- 8f-a: Cập nhật tên SP12
UPDATE products SET product_name = '${escapeSql(sp12Name)}' WHERE product_id = 12;

-- 8f-b: Sản phẩm con mới (SP51+)
INSERT INTO products (product_name, variant_type, parent_product_id, note, created_at) VALUES
${newProductRows};

-- 8f-c: Variants (40 biến thể / SP = Size × Cộc/Dài × NAM/NỮ, clone SP12)
${variantClones}

-- 8f-d: Tồn kho CÔNG TY
${inventoryParts.join("\n\n")}
`;

  const outPath = path.join(__dirname, "aophong-batch-seed.sql");
  fs.writeFileSync(outPath, sql, "utf8");

  console.log("Blocks:", blocks.length);
  console.log("SP12 name:", sp12Name);
  blocks.forEach((b) => {
    const s = inventorySeedSql(b);
    console.log(
      `  SP${b.productId}: actual=${s.totalActual} ADJ_IN=${s.adjustIn} ADJ_OUT=${s.adjustOut} | ${b.name.slice(0, 55)}`
    );
  });
  console.log("Written:", outPath);
}

main();
