# Hướng dẫn FE: Cập nhật RequestSetCreate cho Multi-Product

## Tổng quan

Hiện tại `RequestSetCreate` + `RequestSetTableEditable` chỉ hỗ trợ đúng **1 loại STRUCTURED** (SP1 Sơ mi nam: style + size + length). Cần mở rộng để hỗ trợ tất cả 10 sản phẩm.

### Bảng sản phẩm

| SP  | Tên                  | variantType  | Dimensions             | Trạng thái FE     |
|-----|----------------------|-------------|------------------------|--------------------|
| SP1 | Sơ mi nam 2025       | STRUCTURED  | style + size + length  | ✅ Đã hoạt động    |
| SP2 | Sơ mi nam 2026       | STRUCTURED  | style + size + length  | ✅ Giống SP1       |
| SP3 | Áo khoác 2026        | STRUCTURED  | gender + size          | ❌ Cần làm mới     |
| SP4 | Áo phông 2026        | STRUCTURED  | gender + size + length | ❌ Cần làm mới     |
| SP5 | Áo len + Gile len    | STRUCTURED  | gender + size          | ❌ Giống SP3       |
| SP6 | Gile bảo hộ          | STRUCTURED  | gender + size          | ❌ Giống SP3       |
| SP7 | Bảo hộ lao động      | ITEM_BASED  | variantId              | ✅ Đã hoạt động    |
| SP8 | Nhập xuất vải        | ITEM_BASED  | variantId              | ✅ Đã hoạt động    |
| SP9 | Phụ kiện             | ITEM_BASED  | variantId              | ✅ Đã hoạt động    |
| SP10| Phụ liệu             | ITEM_BASED  | variantId              | ✅ Đã hoạt động    |

**Kết luận: Chỉ cần thêm 2 layout mới cho STRUCTURED:**
1. **Gender + Size** (SP3, SP5, SP6) — bảng đơn giản, không có COC/DAI
2. **Gender + Size + Length** (SP4) — bảng có COC/DAI, giống SP1 nhưng dùng gender thay style

---

## Backend API — Không đổi

`POST /api/request-sets` và `PUT /api/request-sets/{id}` giữ nguyên cấu trúc. BE tự resolve variant từ `ItemDTO` fields.

### Request body

```json
{
  "setName": "ĐX 5 - Hương",
  "description": "",
  "requests": [
    {
      "unitId": 3,
      "productId": 4,
      "positionId": 2,
      "requestType": "IN",
      "expectedDate": null,
      "note": "",
      "items": [
        // <-- format khác nhau tùy product, xem bên dưới
      ]
    }
  ]
}
```

### ItemDTO format theo loại product

#### 1. Style + Size + Length (SP1, SP2) — GIỐNG CŨ

```json
{
  "styleId": 1,
  "sizeValue": "38",
  "lengthCode": "COC",
  "gender": null,
  "variantId": null,
  "quantity": 10
}
```

#### 2. Gender + Size (SP3, SP5, SP6) — MỚI

```json
{
  "styleId": null,
  "sizeValue": "M",
  "lengthCode": null,
  "gender": "NAM",
  "variantId": null,
  "quantity": 15
}
```

#### 3. Gender + Size + Length (SP4) — MỚI

```json
{
  "styleId": null,
  "sizeValue": "M",
  "lengthCode": "COC",
  "gender": "NAM",
  "variantId": null,
  "quantity": 12
}
```

#### 4. ITEM_BASED (SP7-10) — GIỐNG CŨ

```json
{
  "styleId": null,
  "sizeValue": null,
  "lengthCode": null,
  "gender": null,
  "variantId": 245,
  "quantity": 50
}
```

> **Lưu ý:** Có thể bỏ các field `null`, BE chấp nhận missing fields.

---

## Phân tích code hiện tại

### `RequestSetCreate.jsx` — ĐÃ HỖ TRỢ ĐẦY ĐỦ ✅

File này **không cần sửa** vì:

1. **`createEmptyRequest()`** (line 41-109): Đã phân nhánh đúng cho tất cả dimensions
   - `hasStyle` → matrix `[styleName][size][lengthCode]`
   - `hasGender && hasLength` → matrix `[gender][size][lengthCode]`
   - `hasGender` → matrix `[gender][size]`
   - `ITEM_BASED` → itemData `[{variantId, quantity}]`

2. **`buildItems()`** (line 503-539): Đã dùng `buildRequestItems()` từ `variantHelpers.js` — hàm này xử lý đúng tất cả 4 trường hợp.

3. **`transformItemsToMatrixData()`** (line 163-216): Đã xử lý reverse mapping cho edit mode.

4. **`loadInventoryForProduct()`** (line 246-264): Đã cache `productMetaMap` với `{ variantType, dimensions, rawData }`.

### `RequestSetTableEditable.jsx` — CẦN SỬA ❌

**Đây là file chính cần thay đổi.** Vấn đề:

| Phần code          | Vấn đề                                                    |
|--------------------|------------------------------------------------------------|
| `structuredColumns` (line 314-439)  | Hardcode `STYLES`, `SIZES`, `LENGTHS` từ constants → chỉ đúng cho SP1,2 |
| `buildGroupRows()` (line 245-311)   | Hardcode `STYLES.find(s => s.id === styleId)` → không biết gender |
| `updateMatrixValue()` (line 114-139)| Hardcode `LENGTHS.forEach(l => ...)` → sai cho SP3,5,6 (không có length) |
| `createEmptyMatrix()` (line 160-167)| Hardcode `STYLES` + `SIZES` → chỉ đúng cho SP1 |
| `calcGroupTotals()` (line 468-482)  | Hardcode `SIZES` + giả định 3 cấp nested → sai cho gender+size (2 cấp) |
| `buildSummaryRow()` (line 442-466)  | Hardcode `SIZES.flatMap` + COC/DAI columns → sai cho SP3,5,6 |
| `getInventoryForStyle()` (line 53-63)| Chỉ lookup bằng `STYLES` → không biết gender |
| `getMaxQuantity()` (line 65-85)     | Chỉ dùng `inv.matrix[size][lengthCode]` → sai cho gender+size |

### `variantHelpers.js` — ĐÃ ĐẦY ĐỦ ✅

Các hàm `analyzeDimensions()`, `buildRequestItems()`, `getTabConfig()` đã xử lý đúng tất cả trường hợp.

### `constants/index.js` — ĐÃ CÓ ✅

```js
export const GENDERS = [
  { code: "NAM", label: "Nam" },
  { code: "NU", label: "Nữ" },
];
export const getGenderLabel = (code) => GENDERS.find(g => g.code === code)?.label || code;
```

---

## Chi tiết thay đổi trong `RequestSetTableEditable.jsx`

### Bước 1: Thêm import

```diff
-import { STYLES, SIZES, LENGTHS, getLengthLabel } from "../constants";
+import { STYLES, SIZES, LENGTHS, getLengthLabel, GENDERS, getGenderLabel } from "../constants";
+import { analyzeDimensions } from "../utils/variantHelpers";
```

### Bước 2: Thay đổi rendering logic chính

Hiện tại (line 566-584):
```jsx
{isItemBased ? (
  <ItemBasedGroupEditor ... />
) : (
  <Table ... columns={structuredColumns} ... />   // ← hardcoded SP1
)}
```

Cần đổi thành phân nhánh 4 loại:
```jsx
{isItemBased ? (
  <ItemBasedGroupEditor ... />
) : meta?.dimensions?.hasStyle ? (
  // SP1, SP2: Style + Size + Length — giữ nguyên code cũ
  <StyleBasedEditor group={group} ... />
) : meta?.dimensions?.hasGender && meta?.dimensions?.hasLength ? (
  // SP4: Gender + Size + Length
  <GenderLengthEditor group={group} ... />
) : meta?.dimensions?.hasGender ? (
  // SP3, SP5, SP6: Gender + Size (không có length)
  <GenderSizeEditor group={group} ... />
) : (
  // Fallback: legacy SP1 table
  <StyleBasedEditor group={group} ... />
)}
```

### Bước 3: Tạo GenderSizeEditor (SP3, SP5, SP6)

Layout bảng cho gender + size (**KHÔNG có COC/DAI**):

```
| STT | Đơn vị | Chức danh | Giới tính | XS | S | M | L | XL | 2XL | 3XL | 4XL | 5XL | 6XL | Tổng | Xóa |
|-----|--------|-----------|-----------|----|----|---|---|----|----|-----|-----|-----|-----|------|------|
|  1  | Kho    | GDV       | Nam       | 0  | 3  | 5 | 8 | 6  | 4   | 2   | 1   | 0   | 0   | 29   |  X   |
|     |        |           | Nữ        | 0  | 4  | 6 | 5 | 3  | 2   | 1   | 0   | 0   | 0   | 21   |      |
```

**Cấu trúc matrixData:**
```js
matrixData = {
  "NAM": { "XS": 0, "S": 3, "M": 5, "L": 8, ... },
  "NU":  { "XS": 0, "S": 4, "M": 6, "L": 5, ... }
}
```

**Component mẫu:**

```jsx
function GenderSizeEditor({ group, meta, updateRequest, removeRequest,
  units, loadingUnits, onAddUnit, positions, loadingPositions, onAddPosition,
  disabled, requestsCount, inventoryDataMap, requestType, requests
}) {
  const dimensions = meta?.dimensions;
  const sizes = dimensions?.sizes || [];    // ["XS","S","M","L","XL","2XL","3XL","4XL","5XL","6XL"]
  const genders = dimensions?.genders || []; // ["NAM","NU"]

  const updateGenderSizeValue = (requestId, gender, sizeValue, value) => {
    // Tương tự updateMatrixValue nhưng cho 2 cấp
    const req = requests.find(r => r.id === requestId);
    const newMatrix = { ...req.matrixData };
    if (!newMatrix[gender]) newMatrix[gender] = {};
    newMatrix[gender][sizeValue] = value || 0;
    onRequestsChange(requests.map(r => r.id === requestId ? { ...r, matrixData: newMatrix } : r));
  };

  // Build rows: mỗi request → N rows (1 per gender)
  const rows = [];
  group.requests.forEach((req, reqIdx) => {
    genders.forEach((gender, gIdx) => {
      let total = 0;
      sizes.forEach(s => { total += req.matrixData?.[gender]?.[s] || 0; });
      rows.push({
        key: `${req.id}_${gender}`,
        requestId: req.id,
        stt: reqIdx + 1,
        rowSpan: gIdx === 0 ? genders.length : 0,
        gender,
        genderLabel: getGenderLabel(gender),
        matrixData: req.matrixData?.[gender] || {},
        unitId: req.unitId,
        positionId: req.positionId,
        total,
      });
    });
  });

  const columns = [
    {
      title: "STT", dataIndex: "stt", width: 50, fixed: "left",
      onCell: (r) => ({ rowSpan: r.rowSpan }),
      render: (v, r) => r.rowSpan === 0 ? null : v,
    },
    {
      title: "Đơn vị", width: 180, fixed: "left",
      onCell: (r) => ({ rowSpan: r.rowSpan }),
      render: (_, r) => r.rowSpan === 0 ? null : (
        <Space.Compact style={{ width: "100%" }}>
          <Select size="small" style={{ width: "calc(100% - 28px)" }}
            options={units} loading={loadingUnits} value={r.unitId}
            onChange={(v) => updateRequest(r.requestId, "unitId", v)}
            showSearch optionFilterProp="label" disabled={disabled} />
          <Button size="small" icon={<PlusOutlined />}
            onClick={() => onAddUnit?.(r.requestId)} />
        </Space.Compact>
      ),
    },
    {
      title: "Chức danh", width: 250, fixed: "left",
      onCell: (r) => ({ rowSpan: r.rowSpan }),
      render: (_, r) => r.rowSpan === 0 ? null : (
        <Space.Compact style={{ width: "100%" }}>
          <Select size="small" style={{ width: "calc(100% - 28px)" }}
            value={r.positionId || undefined}
            onChange={(v) => updateRequest(r.requestId, "positionId", v)}
            options={positions} loading={loadingPositions}
            showSearch optionFilterProp="label" allowClear
            status={!r.positionId ? "error" : undefined} />
          <Button size="small" icon={<PlusOutlined />}
            onClick={() => onAddPosition?.(r.requestId)} />
        </Space.Compact>
      ),
    },
    {
      title: "Giới tính", dataIndex: "genderLabel", width: 90, fixed: "left",
    },
    // Size columns — MỖI SIZE = 1 CỘT (không có sub-column COC/DAI)
    ...sizes.map(size => ({
      title: size, width: 55, align: "center",
      render: (_, r) => (
        <InputNumber size="small" min={0} style={{ width: 48 }}
          value={r.matrixData[size] || null} placeholder="0" controls={false}
          onChange={(v) => updateGenderSizeValue(r.requestId, r.gender, size, v)}
          disabled={disabled} />
      ),
    })),
    {
      title: "Tổng", dataIndex: "total", width: 60, align: "center",
      render: (v) => <Text strong style={{ color: v > 0 ? tokens.colors.primary : tokens.colors.gray400 }}>{v}</Text>,
    },
    {
      title: "", width: 40, fixed: "right",
      onCell: (r) => ({ rowSpan: r.rowSpan }),
      render: (_, r) => r.rowSpan === 0 ? null : (
        <Popconfirm title="Xóa?" onConfirm={() => removeRequest(r.requestId)}>
          <Button type="text" danger size="small" icon={<DeleteOutlined />}
            disabled={requestsCount <= 1} />
        </Popconfirm>
      ),
    },
  ];

  return <Table bordered size="small" pagination={false}
    columns={columns} dataSource={rows} scroll={{ x: "max-content" }} />;
}
```

### Bước 4: Tạo GenderLengthEditor (SP4)

Layout bảng cho gender + size + length (**CÓ COC/DAI**, giống SP1 nhưng dùng gender):

```
| STT | Đơn vị | Chức danh | Giới tính |   XS    |    S    |    M    | ... | Tổng      | Xóa |
|     |        |           |           | Cộc Dài | Cộc Dài | Cộc Dài | ... | Cộc | Dài |      |
|-----|--------|-----------|-----------|---------|---------|---------|-----|-----------|------|
|  1  | Kho    | GDV       | Nam       |  0   0  |  2   1  |  5   3  | ... | 20  | 15  |  X   |
|     |        |           | Nữ        |  0   0  |  3   2  |  4   3  | ... | 18  | 12  |      |
```

**Cấu trúc matrixData:**
```js
matrixData = {
  "NAM": { "XS": { "COC": 0, "DAI": 0 }, "S": { "COC": 2, "DAI": 1 }, ... },
  "NU":  { "XS": { "COC": 0, "DAI": 0 }, "S": { "COC": 3, "DAI": 2 }, ... }
}
```

**Cấu trúc giống `structuredColumns` hiện tại** nhưng:
- Thay `STYLES` → `genders` (từ `dimensions.genders`)
- Thay `SIZES` → `sizes` (từ `dimensions.sizes`)
- Thay `LENGTHS` → `lengths` (từ `dimensions.lengths`)
- Cột "Kiểu dáng" → "Giới tính"
- Row key: `gender` thay vì `styleId`

### Bước 5: Refactor StyleBasedEditor (SP1, SP2)

Tách code STRUCTURED hiện tại thành component riêng `StyleBasedEditor`, **giữ nguyên 100% logic cũ**. Chỉ wrap lại cho gọn.

---

## Stock validation cho các layout mới

### Hiện tại (SP1 — style based)

`getMaxQuantity()` + `getInventoryForStyle()` dùng `STYLES` constant để lookup.

### Cần thêm cho SP3,5,6 (gender + size)

Inventory data từ API `GET /api/inventory/{productId}`:
```json
{
  "data": [
    { "variantId": 101, "sizeValue": "M", "gender": "NAM", "actualQuantity": 15, "expectedQuantity": 20 },
    { "variantId": 102, "sizeValue": "M", "gender": "NU",  "actualQuantity": 12, "expectedQuantity": 18 }
  ]
}
```

Hàm lookup tồn kho:
```js
const getMaxQtyGenderSize = (gender, sizeValue, requestId, productId) => {
  if (requestType === "IN" || requestType === "ADJUST_IN") return null;
  const rawData = productMetaMap[productId]?.rawData || [];
  const inv = rawData.find(d => d.gender === gender && d.sizeValue === sizeValue);
  if (!inv) return 0;
  let baseMax = requestType === "OUT" ? inv.actualQuantity : (inv.expectedQuantity ?? inv.actualQuantity);
  // Trừ đi số đã dùng bởi requests khác cùng product
  let usedByOthers = 0;
  requests.forEach(req => {
    if (req.id === requestId || req.productId !== productId) return;
    usedByOthers += req.matrixData?.[gender]?.[sizeValue] || 0;
  });
  return Math.max(0, baseMax - usedByOthers);
};
```

### Cần thêm cho SP4 (gender + size + length)

Tương tự nhưng thêm `lengthCode`:
```js
const getMaxQtyGenderLength = (gender, sizeValue, lengthCode, requestId, productId) => {
  const rawData = productMetaMap[productId]?.rawData || [];
  const inv = rawData.find(d => d.gender === gender && d.sizeValue === sizeValue && d.lengthCode === lengthCode);
  if (!inv) return 0;
  let baseMax = requestType === "OUT" ? inv.actualQuantity : (inv.expectedQuantity ?? inv.actualQuantity);
  let usedByOthers = 0;
  requests.forEach(req => {
    if (req.id === requestId || req.productId !== productId) return;
    usedByOthers += req.matrixData?.[gender]?.[sizeValue]?.[lengthCode] || 0;
  });
  return Math.max(0, baseMax - usedByOthers);
};
```

---

## Tổng hợp: calcGroupTotals cho từng layout

### SP1,2 (style + size + length) — giữ nguyên

```js
totals = { C: { 35: 10, 36: 15, ... }, D: { 35: 8, 36: 12, ... }, totalC: 110, totalD: 91 }
```

### SP3,5,6 (gender + size) — MỚI

```js
// Không có COC/DAI, chỉ có total per size
totals = { sizes: { XS: 5, S: 12, M: 20, ... }, grandTotal: 150 }
```

### SP4 (gender + size + length) — MỚI

```js
// Giống SP1 nhưng group by gender thay vì style
totals = { C: { XS: 3, S: 8, ... }, D: { XS: 2, S: 6, ... }, totalC: 85, totalD: 70 }
```

---

## Summary Row cho từng layout

### SP1,2: giữ nguyên `buildSummaryRow()` hiện tại

### SP3,5,6: summary đơn giản hơn

```
| TỔNG CỘNG                                    | XS | S  | M  | L  | XL | ... | Tổng |
|-----------------------------------------------|----|----|----|----|----|----|------|
|                                               | 5  | 12 | 20 | 15 | 10 | ... | 150  |
```

### SP4: giống SP1 layout (có COC/DAI sub-columns)

---

## validateAllQuantities() — Cần mở rộng

Hiện tại (line 541-599) chỉ validate cho ITEM_BASED và style-based STRUCTURED. Cần thêm 2 nhánh:

```js
// Sau block ITEM_BASED validation (line 564)...

// Gender + Size validation (SP3,5,6)
if (meta?.dimensions?.hasGender && !meta?.dimensions?.hasLength) {
  const rawData = meta?.rawData || [];
  for (const [gender, sizeMap] of Object.entries(req.matrixData || {})) {
    for (const [sizeValue, qty] of Object.entries(sizeMap || {})) {
      if (typeof qty !== "number" || qty <= 0) continue;
      const inv = rawData.find(d => d.gender === gender && d.sizeValue === sizeValue);
      if (!inv) continue;
      const available = requestType === "OUT" ? inv.actualQuantity : inv.expectedQuantity;
      if (qty > available) {
        return { valid: false, message: `${productName} - ${getGenderLabel(gender)} Size ${sizeValue}: Yêu cầu ${qty} > Tồn ${available}` };
      }
    }
  }
  continue;
}

// Gender + Size + Length validation (SP4)
if (meta?.dimensions?.hasGender && meta?.dimensions?.hasLength) {
  const rawData = meta?.rawData || [];
  for (const [gender, sizeMap] of Object.entries(req.matrixData || {})) {
    for (const [sizeValue, lengthMap] of Object.entries(sizeMap || {})) {
      if (typeof lengthMap !== "object") continue;
      for (const [lengthCode, qty] of Object.entries(lengthMap)) {
        if (typeof qty !== "number" || qty <= 0) continue;
        const inv = rawData.find(d => d.gender === gender && d.sizeValue === sizeValue && d.lengthCode === lengthCode);
        if (!inv) continue;
        const available = requestType === "OUT" ? inv.actualQuantity : inv.expectedQuantity;
        if (qty > available) {
          return { valid: false, message: `${productName} - ${getGenderLabel(gender)} Size ${sizeValue} ${getLengthLabel(lengthCode)}: Yêu cầu ${qty} > Tồn ${available}` };
        }
      }
    }
  }
  continue;
}
```

---

## buildInventoryData() — Cần tổng quát hóa

Hàm `buildInventoryData()` (line 226-243) hiện chỉ xử lý style-based. Cần sửa hoặc bỏ qua vì:

- `loadInventoryForProduct()` đã lưu `rawData` vào `productMetaMap`
- Các editor mới dùng `rawData` trực tiếp (array), không cần transform thành nested object
- Chỉ SP1,2 (style-based) mới cần `buildInventoryData()` cho `getInventoryForStyle()`

**Khuyến nghị:** Giữ `buildInventoryData()` cho backward compatibility với SP1, các layout mới dùng `productMetaMap[productId].rawData` trực tiếp.

---

## Thứ tự triển khai

1. **Thêm `GenderSizeEditor`** — component mới cho SP3, SP5, SP6
2. **Thêm `GenderLengthEditor`** — component mới cho SP4
3. **Tách `StyleBasedEditor`** — wrap code STRUCTURED cũ thành component riêng
4. **Sửa rendering branch** — phân nhánh 4 loại trong phần render chính
5. **Mở rộng `validateAllQuantities()`** — thêm 2 nhánh validation
6. **Mở rộng `calcGroupTotals()`** — tính tổng đúng cho từng layout
7. **Test** — tạo request set cho mỗi loại sản phẩm

---

## Ví dụ API request hoàn chỉnh

### Tạo đợt ĐX cho SP3 (Áo khoác — gender + size)

```json
POST /api/request-sets
{
  "setName": "ĐX 5 - Hương",
  "description": "Nhập áo khoác cho đợt đông 2026",
  "requests": [
    {
      "unitId": 3,
      "productId": 3,
      "positionId": 2,
      "requestType": "IN",
      "expectedDate": null,
      "note": "Kho Hà Nội",
      "items": [
        { "gender": "NAM", "sizeValue": "S",   "quantity": 5 },
        { "gender": "NAM", "sizeValue": "M",   "quantity": 10 },
        { "gender": "NAM", "sizeValue": "L",   "quantity": 15 },
        { "gender": "NAM", "sizeValue": "XL",  "quantity": 12 },
        { "gender": "NAM", "sizeValue": "2XL", "quantity": 8 },
        { "gender": "NU",  "sizeValue": "S",   "quantity": 8 },
        { "gender": "NU",  "sizeValue": "M",   "quantity": 12 },
        { "gender": "NU",  "sizeValue": "L",   "quantity": 10 },
        { "gender": "NU",  "sizeValue": "XL",  "quantity": 6 }
      ]
    }
  ]
}
```

### Tạo đợt ĐX cho SP4 (Áo phông — gender + size + length)

```json
POST /api/request-sets
{
  "setName": "ĐX 6 - Hương",
  "requests": [
    {
      "unitId": 3,
      "productId": 4,
      "positionId": 2,
      "requestType": "IN",
      "items": [
        { "gender": "NAM", "sizeValue": "M",  "lengthCode": "COC", "quantity": 8 },
        { "gender": "NAM", "sizeValue": "M",  "lengthCode": "DAI", "quantity": 6 },
        { "gender": "NAM", "sizeValue": "L",  "lengthCode": "COC", "quantity": 10 },
        { "gender": "NAM", "sizeValue": "L",  "lengthCode": "DAI", "quantity": 8 },
        { "gender": "NU",  "sizeValue": "S",  "lengthCode": "COC", "quantity": 5 },
        { "gender": "NU",  "sizeValue": "S",  "lengthCode": "DAI", "quantity": 4 },
        { "gender": "NU",  "sizeValue": "M",  "lengthCode": "COC", "quantity": 7 },
        { "gender": "NU",  "sizeValue": "M",  "lengthCode": "DAI", "quantity": 5 }
      ]
    }
  ]
}
```

### Tạo đợt ĐX cho SP10 (Phụ liệu — ITEM_BASED)

```json
POST /api/request-sets
{
  "setName": "ĐX 7 - Hương",
  "requests": [
    {
      "unitId": 3,
      "productId": 10,
      "positionId": 2,
      "requestType": "OUT",
      "items": [
        { "variantId": 500, "quantity": 100 },
        { "variantId": 501, "quantity": 200 },
        { "variantId": 520, "quantity": 50 }
      ]
    }
  ]
}
```
