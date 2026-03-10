# Frontend Migration Guide: Product Variants Redesign

## Mục lục

1. [Tổng quan thay đổi](#1-tổng-quan-thay-đổi)
2. [Dữ liệu sản phẩm mới](#2-dữ-liệu-sản-phẩm-mới)
3. [API Response thay đổi](#3-api-response-thay-đổi)
4. [Thay đổi constants/index.js](#4-thay-đổi-constantsindexjs)
5. [Thay đổi API client](#5-thay-đổi-api-client)
6. [Cập nhật trang Tồn kho (InventoryList)](#6-cập-nhật-trang-tồn-kho-inventorylist)
7. [Cập nhật trang Chi tiết tồn kho (InventoryDetail)](#7-cập-nhật-trang-chi-tiết-tồn-kho-inventorydetail)
8. [Cập nhật form Tạo phiếu (RequestSetCreate)](#8-cập-nhật-form-tạo-phiếu-requestsetcreate)
9. [Cập nhật hiển thị phiếu (RequestSetDetail)](#9-cập-nhật-hiển-thị-phiếu-requestsetdetail)
10. [Cập nhật nhận hàng (ReceiveModal)](#10-cập-nhật-nhận-hàng-receivemodal)
11. [Tóm tắt file cần sửa](#11-tóm-tắt-file-cần-sửa)

---

## 1. Tổng quan thay đổi

### Trước (chỉ Sơ mi)
- 1 loại sản phẩm duy nhất
- Variant = tổ hợp **Style × Size (số) × Length (Cộc/Dài)**
- Hardcode: `STYLES`, `SIZES = [35..45]`, `LENGTHS = [COC, DAI]`
- Mọi UI đều render dạng **matrix** (bảng kích cỡ)

### Sau (10 sản phẩm)
- 2 loại variant type:
  - **STRUCTURED**: variant = tổ hợp các dimensions (style/size/length/gender)
  - **ITEM_BASED**: variant = mã hàng riêng (item_code + item_name + unit)
- Size bây giờ là **String** (`"35"`, `"XS"`, `"M"`, ...)
- Không còn hardcode — **mọi thứ đều dynamic từ API**

### 10 sản phẩm

| # | Tên | variantType | Dimensions | Ghi chú |
|---|-----|-------------|------------|---------|
| 1 | Sơ mi nam 2025 | STRUCTURED | style + size(35-45) + length(Cộc/Dài) | Giữ nguyên logic cũ |
| 2 | Sơ mi nam 2026 | STRUCTURED | style + size(35-45) + length(Cộc/Dài) | Giống SP1 |
| 3 | Áo khoác | STRUCTURED | size(XS-6XL) + gender(NAM/NU) | Không có style, length |
| 4 | Áo phông | STRUCTURED | size(XS-6XL) + gender(NAM/NU) + length(Cộc/Dài) | Không có style |
| 5 | Áo len + Gile | STRUCTURED | size(XS-6XL) + gender(NAM/NU) | Giống SP3 |
| 6 | Gile BH | STRUCTURED | size(XS-6XL) + gender(NAM/NU) | Giống SP3 |
| 7 | BH lao động | ITEM_BASED | item_code + item_name + unit | 18 mã |
| 8 | Vải | ITEM_BASED | item_code + item_name + unit | 31 mã |
| 9 | Phụ kiện | ITEM_BASED | item_code + item_name + unit | 47 mã |
| 10 | Phụ liệu | ITEM_BASED | item_code + item_name + unit | 258 mã |

---

## 2. Dữ liệu sản phẩm mới

### Product entity (từ GET /api/products)
```json
{
  "productId": 1,
  "productName": "SƠ MI NAM 2025",
  "variantType": "STRUCTURED",   // ← MỚI: "STRUCTURED" hoặc "ITEM_BASED"
  "note": "88 biến thể",
  "createdAt": "2025-01-01T00:00:00"
}
```

### Variant dimensions theo từng sản phẩm

**STRUCTURED products — các dimensions nullable:**

| Field | SP1,2 (Sơ mi) | SP3 (Áo khoác) | SP4 (Áo phông) | SP5,6 (Áo len, Gile) |
|-------|---------------|-----------------|-----------------|----------------------|
| styleName | ✅ "Cổ điển", "Slim"... | ❌ null | ❌ null | ❌ null |
| sizeValue | ✅ "35"-"45" | ✅ "XS"-"6XL" | ✅ "XS"-"6XL" | ✅ "XS"-"6XL" |
| lengthCode | ✅ "COC"/"DAI" | ❌ null | ✅ "COC"/"DAI" | ❌ null |
| gender | ❌ null | ✅ "NAM"/"NU" | ✅ "NAM"/"NU" | ✅ "NAM"/"NU" |

**ITEM_BASED products — các fields:**

| Field | Mô tả | Ví dụ |
|-------|--------|-------|
| itemCode | Mã hàng unique | "KHOA1", "PK9", "B7" |
| itemName | Tên hàng | "Khóa quần tím than" |
| unit | Đơn vị tính | "chiếc", "mét", "bộ", "gói" |

---

## 3. API Response thay đổi

### 3.1. GET /api/inventory — Danh sách tồn kho

**Response: `List<ProductInventoryViewDTO>`**

```json
[
  {
    "productId": 1,
    "productName": "SƠ MI NAM 2025",
    "variantType": "STRUCTURED",        // ← MỚI
    "note": "88 biến thể",
    "createdAt": "2025-01-01T00:00:00",
    "canViewExpected": true,
    "data": [
      {
        "variantId": 101,                // ← MỚI
        "styleName": "Cổ điển",          // null nếu không có style
        "sizeValue": "35",               // ← ĐỔI: String thay vì Integer
        "lengthCode": "COC",             // null nếu không có length
        "gender": null,                  // ← MỚI: "NAM"/"NU" hoặc null
        "itemCode": null,                // ← MỚI: cho ITEM_BASED
        "itemName": null,                // ← MỚI: cho ITEM_BASED
        "unit": null,                    // ← MỚI: cho ITEM_BASED
        "actualQuantity": 150,
        "expectedQuantity": 200
      }
    ]
  },
  {
    "productId": 7,
    "productName": "BH LAO ĐỘNG",
    "variantType": "ITEM_BASED",         // ← ITEM_BASED
    "note": "18 mã",
    "createdAt": "2026-01-01T00:00:00",
    "canViewExpected": true,
    "data": [
      {
        "variantId": 301,
        "styleName": null,               // luôn null
        "sizeValue": null,               // luôn null
        "lengthCode": null,              // luôn null
        "gender": null,                  // luôn null
        "itemCode": "GIAY-38",           // ← có giá trị
        "itemName": "Giày BH size 38",   // ← có giá trị
        "unit": "đôi",                   // ← có giá trị
        "actualQuantity": 50,
        "expectedQuantity": 100
      }
    ]
  }
]
```

### 3.2. GET /api/inventory/{productId}/history?filter={value}

**⚠️ BREAKING CHANGE: query param `style` → `filter`**

- Với sản phẩm có **style** (SP1,2): `filter=Cổ điển` hoặc `filter=Slim`
- Với sản phẩm có **gender** (SP3,4,5,6): `filter=NAM` hoặc `filter=NU`
- Với **ITEM_BASED** (SP7-10): không truyền `filter` (hoặc `filter=null`)

**Response: `RequestHistoryMatrixDTO`**

```json
// STRUCTURED product (Sơ mi) — giống cũ nhưng sizeColumns là String[]
{
  "productId": 1,
  "productName": "SƠ MI NAM 2025",
  "variantType": "STRUCTURED",          // ← MỚI
  "filterValue": "Cổ điển",             // ← ĐỔI TÊN: từ "styleName"
  "sizeColumns": ["35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45"],  // ← String[]
  "rows": [
    {
      "requestId": 1,
      "setId": 1,
      "setName": "Đợt 01 - 01/2025",
      "setStatus": "EXECUTED",
      "unitName": "BĐ Hà Nội",
      "requestType": "IN",
      "lengthCode": "COC",
      "note": null,
      "createdAt": "2025-01-15T10:00:00",
      "createdBy": 2,
      "createdByName": "Nguyễn Văn A",
      "sizes": { "35": 10, "36": 15, "37": 20, "38": 25, ... },  // ← key là String
      // Các field ITEM_BASED = null
      "variantId": null,
      "itemCode": null,
      "itemName": null,
      "unit": null,
      "quantity": null
    }
  ]
}

// STRUCTURED product (Áo khoác) — filter by gender
{
  "productId": 3,
  "productName": "ÁO KHOÁC 2025",
  "variantType": "STRUCTURED",
  "filterValue": "NAM",                  // filter = gender
  "sizeColumns": ["XS", "S", "M", "L", "XL", "2XL", "3XL", "4XL", "5XL", "6XL"],
  "rows": [
    {
      "requestId": 5,
      "setId": 3,
      ...
      "lengthCode": null,                // không có length → mỗi row = 1 request
      "sizes": { "XS": 5, "S": 10, "M": 20, ... },
      ...
    }
  ]
}

// ITEM_BASED product — flat list, KHÔNG có matrix
{
  "productId": 7,
  "productName": "BH LAO ĐỘNG",
  "variantType": "ITEM_BASED",
  "filterValue": null,
  "sizeColumns": [],                     // ← rỗng
  "rows": [
    {
      "requestId": 10,
      "setId": 5,
      "setName": "Đợt 05 - 03/2025",
      "setStatus": "EXECUTED",
      "unitName": "BĐ Hà Nội",
      "requestType": "IN",
      "lengthCode": null,
      "note": null,
      "createdAt": "2025-03-01T10:00:00",
      "createdBy": 2,
      "createdByName": "Nguyễn Văn A",
      "sizes": null,                     // ← null cho ITEM_BASED
      "variantId": 301,                  // ← có giá trị
      "itemCode": "GIAY-38",             // ← có giá trị
      "itemName": "Giày BH size 38",     // ← có giá trị
      "unit": "đôi",                     // ← có giá trị
      "quantity": 100                    // ← có giá trị (số lượng đơn lẻ)
    }
  ]
}
```

### 3.3. GET /api/requests/{id} — Items của 1 request

**Response: `List<InventoryRequestItemDTO>`**

```json
// STRUCTURED
[
  {
    "variantId": 101,           // ← MỚI
    "styleName": "Cổ điển",     // null nếu sản phẩm không có style
    "sizeValue": "35",          // ← String
    "lengthCode": "COC",        // null nếu không có length
    "gender": null,             // ← MỚI
    "itemCode": null,           // ← MỚI
    "itemName": null,           // ← MỚI
    "unit": null,               // ← MỚI
    "quantity": 10
  }
]

// ITEM_BASED
[
  {
    "variantId": 301,
    "styleName": null,
    "sizeValue": null,
    "lengthCode": null,
    "gender": null,
    "itemCode": "GIAY-38",
    "itemName": "Giày BH size 38",
    "unit": "đôi",
    "quantity": 50
  }
]
```

### 3.4. POST /api/requests — Tạo request (request body thay đổi)

```json
// STRUCTURED — Sơ mi (style + size + length)
{
  "unitId": 1,
  "positionId": null,
  "productId": 1,
  "requestType": "IN",
  "note": "",
  "items": [
    {
      "styleId": 1,              // có cho Sơ mi
      "sizeValue": "35",         // ← String
      "lengthCode": "COC",
      "gender": null,
      "variantId": null,
      "quantity": 10
    }
  ]
}

// STRUCTURED — Áo khoác (size + gender, KHÔNG có style/length)
{
  "unitId": 1,
  "productId": 3,
  "requestType": "IN",
  "items": [
    {
      "styleId": null,
      "sizeValue": "M",          // ← chữ
      "lengthCode": null,
      "gender": "NAM",           // ← MỚI
      "variantId": null,
      "quantity": 5
    }
  ]
}

// ITEM_BASED — chỉ cần variantId + quantity
{
  "unitId": 1,
  "productId": 7,
  "requestType": "IN",
  "items": [
    {
      "styleId": null,
      "sizeValue": null,
      "lengthCode": null,
      "gender": null,
      "variantId": 301,           // ← truyền trực tiếp variantId
      "quantity": 50
    }
  ]
}
```

### 3.5. GET /api/request-sets/{setId}/progress

**SetReceiptProgressDTO.ItemProgress — thêm fields mới:**

```json
{
  "variantId": 301,
  "styleName": null,
  "sizeValue": null,              // ← String hoặc null
  "lengthCode": null,
  "gender": "NAM",                // ← MỚI
  "itemCode": "GIAY-38",          // ← MỚI
  "itemName": "Giày BH size 38",  // ← MỚI
  "unit": "đôi",                  // ← MỚI
  "proposedQuantity": 100,
  "totalReceived": 60,
  "remainingQuantity": 40,
  "percentage": 60.0,
  "receiptHistory": [...]
}
```

### 3.6. GET /api/request-sets/{setId}/receipts

**ReceiptDetailDTO.ReceiptItemDetailDTO — thêm fields mới:**

```json
{
  "receiptItemId": 1,
  "requestId": 10,
  "variantId": 301,
  "styleName": null,
  "sizeValue": null,               // ← String hoặc null
  "lengthCode": null,
  "gender": "NAM",                 // ← MỚI
  "itemCode": "GIAY-38",           // ← MỚI
  "itemName": "Giày BH size 38",   // ← MỚI
  "unit": "đôi",                   // ← MỚI
  "receivedQuantity": 30
}
```

---

## 4. Thay đổi constants/index.js

### Xóa hardcode STYLES, SIZES, LENGTHS (không dùng làm source-of-truth nữa)

```javascript
// ❌ XÓA hoặc deprecate — không dùng để render dynamic UI
// export const STYLES = [...]
// export const SIZES = [35, 36, ...]
// export const LENGTHS = [...]

// ✅ Giữ lại LENGTHS chỉ để mapping label (vẫn hữu ích)
export const LENGTHS = [
  { code: "COC", label: "Cộc" },
  { code: "DAI", label: "Dài" },
];

// ✅ MỚI: Gender labels
export const GENDERS = [
  { code: "NAM", label: "Nam" },
  { code: "NU", label: "Nữ" },
];

export const getGenderLabel = (code) => {
  const g = GENDERS.find((g) => g.code === code);
  return g?.label || code;
};

// ✅ MỚI: Variant type labels
export const VARIANT_TYPES = {
  STRUCTURED: "Biến thể có cấu trúc",
  ITEM_BASED: "Mã hàng riêng lẻ",
};
```

### Giữ nguyên (không đổi)
- `REQUEST_TYPES`, `REQUEST_SET_STATUSES`, `APPROVAL_ACTIONS`
- `USER_ROLES`, `REQUEST_TYPES_BY_ROLE`, `ROLES_CAN_VIEW_EXPECTED`
- Tất cả contract report constants

---

## 5. Thay đổi API client

### requestApi.js

```javascript
// ❌ CŨ:
export const getInventoryHistory = (productId, style) => {
  return axiosClient.get(`/inventory/${productId}/history`, {
    params: { style },
  });
};

// ✅ MỚI: đổi param "style" → "filter"
export const getInventoryHistory = (productId, filter) => {
  return axiosClient.get(`/inventory/${productId}/history`, {
    params: { filter },
  });
};

// ✅ MỚI: API lấy danh sách variants của 1 product (cần cho form tạo phiếu ITEM_BASED)
export const getProductVariants = (productId) => {
  return axiosClient.get(`/products/${productId}/variants`);
};
// → Backend cần thêm endpoint này (hoặc FE extract từ inventory data)
```

> **Lưu ý**: Hiện tại backend chưa có endpoint riêng `GET /products/{id}/variants`.
> FE có thể tạm dùng `getInventoryByProduct(productId)` rồi extract danh sách variants từ `data[]`.

---

## 6. Cập nhật trang Tồn kho (InventoryList)

### File: `src/pages/InventoryList.jsx`

### 6.1. Thay đổi hiển thị product card/row

**Thêm badge variant type:**
```jsx
// Mỗi product card hiển thị thêm tag variant type
<Tag color={product.variantType === 'STRUCTURED' ? 'blue' : 'green'}>
  {product.variantType === 'STRUCTURED' ? 'Cấu trúc' : 'Mã hàng'}
</Tag>
```

### 6.2. Thay đổi tính tổng tồn kho

**Cũ**: Sum tất cả `actualQuantity` (đồng nhất — đều là "chiếc áo")
**Mới**: ITEM_BASED products có đơn vị khác nhau (mét, chiếc, bộ...) → **không nên sum chung**

```javascript
// Với STRUCTURED: sum actualQuantity như cũ
// Với ITEM_BASED: hiển thị số lượng variants thay vì tổng số lượng
const getProductSummary = (product) => {
  if (product.variantType === 'STRUCTURED') {
    return {
      label: 'Tổng TT',
      value: product.data.reduce((sum, d) => sum + d.actualQuantity, 0),
    };
  }
  // ITEM_BASED: đếm số mã hàng có tồn > 0
  const activeItems = product.data.filter(d => d.actualQuantity > 0).length;
  return {
    label: 'Mã hàng có tồn',
    value: `${activeItems}/${product.data.length}`,
  };
};
```

### 6.3. Table view cho ITEM_BASED

Khi render ở chế độ Table, ITEM_BASED products cần columns khác:

```jsx
// STRUCTURED: giữ matrix columns (Style, Size, Cộc/Dài)
// ITEM_BASED: flat table
const itemBasedColumns = [
  { title: 'Mã hàng', dataIndex: 'itemCode', width: 120 },
  { title: 'Tên hàng', dataIndex: 'itemName', ellipsis: true },
  { title: 'ĐVT', dataIndex: 'unit', width: 80 },
  { title: 'TT', dataIndex: 'actualQuantity', width: 80, align: 'right' },
  // Chỉ hiện nếu canViewExpected
  ...(canViewExpected ? [{
    title: 'DK', dataIndex: 'expectedQuantity', width: 80, align: 'right',
  }] : []),
];
```

---

## 7. Cập nhật trang Chi tiết tồn kho (InventoryDetail)

### File: `src/pages/InventoryDetail.jsx`

Đây là file cần thay đổi nhiều nhất. Hiện tại toàn bộ logic hardcode cho Sơ mi (style tabs + size 35-45 matrix). Cần **branch theo variantType**.

### 7.1. Phân nhánh rendering chính

```jsx
export default function InventoryDetail() {
  // ... load data giữ nguyên ...

  if (!product) return <ErrorResult .../>;

  // ← PHÂN NHÁNH Ở ĐÂY
  if (product.variantType === 'ITEM_BASED') {
    return <ItemBasedInventoryDetail product={product} showExpected={showExpected} />;
  }

  // STRUCTURED: tiếp tục logic cũ nhưng DYNAMIC
  return <StructuredInventoryDetail product={product} showExpected={showExpected} />;
}
```

### 7.2. STRUCTURED: Xử lý dynamic dimensions

**Hiện tại** hardcode `SIZES.forEach(size => ...)` — cần dynamic:

```javascript
// ✅ Extract dimensions từ product.data
const analyzeDimensions = (data) => {
  const hasStyle = data.some(d => d.styleName != null);
  const hasLength = data.some(d => d.lengthCode != null);
  const hasGender = data.some(d => d.gender != null);

  // Extract unique values
  const styles = [...new Set(data.map(d => d.styleName).filter(Boolean))];
  const sizes = [...new Set(data.map(d => d.sizeValue).filter(Boolean))];
  const lengths = [...new Set(data.map(d => d.lengthCode).filter(Boolean))];
  const genders = [...new Set(data.map(d => d.gender).filter(Boolean))];

  return { hasStyle, hasLength, hasGender, styles, sizes, lengths, genders };
};
```

### 7.3. STRUCTURED với Style (SP1, SP2 — Sơ mi)

**Tabs**: theo styleName (Cổ điển, Slim, ...)
**Matrix**: Size columns (35-45) × Length rows (Cộc/Dài)
**History filter**: `filter=<styleName>`

→ **Gần như giữ nguyên logic cũ**, chỉ thay:
- `SIZES` hardcode → `dimensions.sizes` dynamic
- `sizeValue` so sánh dùng String thay Integer

```jsx
// ❌ CŨ
SIZES.forEach((size) => {
  cols.push({ title: `Size ${size}`, ... });
});

// ✅ MỚI
dimensions.sizes.forEach((size) => {
  cols.push({ title: `Size ${size}`, ... });
});
```

### 7.4. STRUCTURED với Gender (SP3, SP5, SP6 — Áo khoác, Áo len, Gile)

**Tabs**: theo gender ("NAM", "NU")
**Matrix**: Size columns (XS-6XL), KHÔNG CÓ Cộc/Dài sub-columns
**History filter**: `filter=NAM` hoặc `filter=NU`

```jsx
// Khi hasGender && !hasLength:
// → Tab = gender (NAM/NU)
// → Mỗi tab = 1 row "Thực tế" + 1 row "Dự kiến"
// → Columns = dynamic sizes, KHÔNG có sub-columns Cộc/Dài

const genderColumns = [
  { title: '', dataIndex: 'rowLabel', fixed: 'left', width: 100 },
  ...dimensions.sizes.map(size => ({
    title: size,
    dataIndex: `s_${size}`,
    align: 'center',
    width: 55,
    render: (val, record) => renderQty(val, record),
  })),
  { title: 'Tổng', dataIndex: 'total', align: 'center', width: 60 },
];
```

**Build data cho gender tab:**
```javascript
const buildGenderTableData = (gender) => {
  const items = product.data.filter(d => d.gender === gender);
  const actualRow = { key: 'actual', rowLabel: 'Thực tế', total: 0 };
  const expectedRow = { key: 'expected', rowLabel: 'Dự kiến', total: 0 };

  items.forEach(item => {
    actualRow[`s_${item.sizeValue}`] = item.actualQuantity;
    actualRow.total += item.actualQuantity;
    expectedRow[`s_${item.sizeValue}`] = item.expectedQuantity;
    expectedRow.total += item.expectedQuantity;
  });

  return showExpected ? [actualRow, expectedRow] : [actualRow];
};
```

### 7.5. STRUCTURED với Gender + Length (SP4 — Áo phông)

**Tabs**: theo gender ("NAM", "NU")
**Matrix**: Size columns (XS-6XL) × Length sub-columns (Cộc/Dài)
**History filter**: `filter=NAM` hoặc `filter=NU`

→ Giống Sơ mi nhưng tab = gender thay vì style

### 7.6. ITEM_BASED (SP7-10)

**Hoàn toàn khác — KHÔNG có matrix, hiển thị bảng phẳng**

```jsx
function ItemBasedInventoryDetail({ product, showExpected }) {
  const [historyData, setHistoryData] = useState(null);
  const [historyLoading, setHistoryLoading] = useState(false);

  // Load history (không cần filter)
  useEffect(() => {
    getInventoryHistory(product.productId, null)
      .then(res => setHistoryData(res.data))
      .catch(() => setHistoryData({ rows: [] }));
  }, [product.productId]);

  // ── Bảng tồn kho ──
  const inventoryColumns = [
    {
      title: 'Mã hàng',
      dataIndex: 'itemCode',
      width: 120,
      fixed: 'left',
      sorter: (a, b) => a.itemCode.localeCompare(b.itemCode),
    },
    {
      title: 'Tên hàng',
      dataIndex: 'itemName',
      ellipsis: true,
    },
    {
      title: 'ĐVT',
      dataIndex: 'unit',
      width: 80,
    },
    {
      title: 'Thực tế',
      dataIndex: 'actualQuantity',
      width: 100,
      align: 'right',
      sorter: (a, b) => a.actualQuantity - b.actualQuantity,
      render: (val) => (
        <Text strong style={{ color: tokens.colors.dataActual }}>
          {val}
        </Text>
      ),
    },
    ...(showExpected ? [{
      title: 'Dự kiến',
      dataIndex: 'expectedQuantity',
      width: 100,
      align: 'right',
      render: (val) => (
        <Text strong style={{ color: tokens.colors.dataExpected }}>
          {val}
        </Text>
      ),
    }] : []),
  ];

  // ── Bảng lịch sử ──
  const historyColumns = [
    {
      title: 'Đợt',
      dataIndex: 'setName',
      width: 150,
      render: (name, record) => (
        <a onClick={() => window.open(`/request-sets/${record.setId}`, '_blank')}>
          {name}
        </a>
      ),
    },
    { title: 'Đơn vị', dataIndex: 'unitName', width: 150, ellipsis: true },
    {
      title: 'Loại',
      dataIndex: 'requestType',
      width: 80,
      render: (type) => {
        const config = REQUEST_TYPE_CONFIG[type];
        return <Tag color={config?.color}>{config?.label}</Tag>;
      },
    },
    { title: 'Mã hàng', dataIndex: 'itemCode', width: 100 },
    { title: 'Tên hàng', dataIndex: 'itemName', ellipsis: true },
    { title: 'ĐVT', dataIndex: 'unit', width: 70 },
    {
      title: 'SL',
      dataIndex: 'quantity',
      width: 80,
      align: 'right',
      render: (val, record) => {
        const isOut = ['OUT', 'ADJUST_OUT', 'RECEIPT_OUT'].includes(record.requestType);
        return (
          <Text strong style={{ color: isOut ? tokens.colors.dataNegative : tokens.colors.dataPositive }}>
            {isOut ? `-${val}` : `+${val}`}
          </Text>
        );
      },
    },
    {
      title: 'Ngày',
      dataIndex: 'createdAt',
      width: 100,
      render: (val) => new Date(val).toLocaleDateString('vi-VN'),
    },
  ];

  return (
    <div>
      {/* Header */}
      <Card title="Tồn kho" size="small">
        <Table
          columns={inventoryColumns}
          dataSource={product.data.map(d => ({ ...d, key: d.variantId }))}
          pagination={{ pageSize: 50, showSizeChanger: true }}
          size="small"
          scroll={{ x: 600 }}
        />
      </Card>

      {/* Lịch sử */}
      <Card title="Lịch sử" size="small" style={{ marginTop: 16 }}>
        <Table
          columns={historyColumns}
          dataSource={historyData?.rows?.map((r, i) => ({ ...r, key: i })) || []}
          loading={historyLoading}
          pagination={{ pageSize: 20 }}
          size="small"
        />
      </Card>
    </div>
  );
}
```

### 7.7. Quyết định hiển thị tabs

```javascript
// Logic chọn tab dimension
const getTabConfig = (product, dimensions) => {
  if (product.variantType === 'ITEM_BASED') {
    return null; // không có tabs
  }
  if (dimensions.hasStyle) {
    // SP1, SP2: tabs = style names
    return {
      tabKey: 'style',
      tabs: dimensions.styles.map(s => ({ key: s, label: s })),
      historyFilter: (tab) => tab, // filter = styleName
    };
  }
  if (dimensions.hasGender) {
    // SP3, SP4, SP5, SP6: tabs = gender
    return {
      tabKey: 'gender',
      tabs: dimensions.genders.map(g => ({
        key: g,
        label: g === 'NAM' ? 'Nam' : 'Nữ',
      })),
      historyFilter: (tab) => tab, // filter = gender code
    };
  }
  // Fallback: 1 tab duy nhất
  return { tabKey: 'all', tabs: [{ key: 'all', label: 'Tất cả' }], historyFilter: () => null };
};
```

---

## 8. Cập nhật form Tạo phiếu (RequestSetCreate)

### Files:
- `src/pages/RequestSetCreate.jsx`
- `src/components/RequestSetTableEditable.jsx`

### 8.1. Phân nhánh UI theo variantType khi chọn product

Khi user chọn product trong form, cần phân nhánh:

```javascript
const handleProductChange = async (productId, requestIndex) => {
  const product = products.find(p => p.productId === productId);

  if (product.variantType === 'ITEM_BASED') {
    // Hiển thị Item Picker UI
    switchToItemBasedEditor(requestIndex, product);
  } else {
    // Hiển thị Matrix Editor (cũ nhưng dynamic)
    switchToStructuredEditor(requestIndex, product);
  }
};
```

### 8.2. STRUCTURED Editor — Dynamic dimensions

**Hiện tại**: hardcode Style dropdown + Size 35-45 columns + Cộc/Dài rows
**Mới**: Dimensions phụ thuộc product

```javascript
// Load inventory data để biết dimensions
const loadProductDimensions = async (productId) => {
  const res = await getInventoryByProduct(productId);
  const data = res.data.data; // inventory balance list
  return analyzeDimensions(data);
};
```

**SP1,2 (Sơ mi) — giữ nguyên UI cũ:**
- Style selector (dropdown chọn kiểu dáng)
- Matrix: columns = dynamic sizes từ data, sub-columns = Cộc/Dài

**SP3,5,6 (Áo khoác, Áo len, Gile) — Size × Gender grid:**
- Gender selector (chọn NAM hoặc NU) hoặc hiển thị 2 hàng
- Matrix: columns = sizes (XS..6XL), KHÔNG có sub-columns

```jsx
// Render cho product có gender, KHÔNG có length
// Option A: Gender là 1 cột cố định bên trái, mỗi row = 1 gender
// Option B: 2 bảng riêng cho NAM và NU

// Đề xuất Option A:
// | Gender | XS | S  | M  | L  | XL | 2XL | 3XL | 4XL | Tổng |
// |--------|----|----|----|----|----|----|-----|-----|------|
// | NAM    | [] | [] | [] | [] | [] | [] | []  | []  |  0   |
// | NỮ     | [] | [] | [] | [] | [] | [] | []  | []  |  0   |
```

**SP4 (Áo phông) — Size × Gender × Length grid:**
```
// | Gender | XS        | S         | M         | ... | Tổng      |
// |        | Cộc | Dài | Cộc | Dài | Cộc | Dài |     | Cộc | Dài |
// |--------|-----|-----|-----|-----|-----|-----|-----|-----|------|
// | NAM    | []  | []  | []  | []  | []  | []  |     |  0  |  0   |
// | NỮ     | []  | []  | []  | []  | []  | []  |     |  0  |  0   |
```

### 8.3. matrixData format thay đổi

**Hiện tại:**
```javascript
matrixData = {
  [styleId]: {
    [size]: { COC: qty, DAI: qty }
  }
}
```

**Mới — STRUCTURED with style (SP1,2):**
```javascript
matrixData = {
  [styleId]: {
    [sizeValue]: { COC: qty, DAI: qty }  // sizeValue là String "35"
  }
}
```

**Mới — STRUCTURED with gender, no length (SP3,5,6):**
```javascript
matrixData = {
  [gender]: {   // "NAM" hoặc "NU"
    [sizeValue]: qty   // không có sub-key length
  }
}
```

**Mới — STRUCTURED with gender + length (SP4):**
```javascript
matrixData = {
  [gender]: {
    [sizeValue]: { COC: qty, DAI: qty }
  }
}
```

### 8.4. ITEM_BASED Editor — Danh sách mã hàng

**UI hoàn toàn mới — thay matrix bằng bảng nhập:**

```jsx
function ItemBasedEditor({ product, inventoryData, value, onChange }) {
  // value = [{ variantId, quantity }]
  // inventoryData = product's inventory balance data

  const [searchText, setSearchText] = useState('');

  // Filter variants by search
  const filteredVariants = inventoryData.filter(v =>
    v.itemCode.toLowerCase().includes(searchText.toLowerCase()) ||
    v.itemName.toLowerCase().includes(searchText.toLowerCase())
  );

  return (
    <div>
      <Input.Search
        placeholder="Tìm mã hàng hoặc tên..."
        onChange={e => setSearchText(e.target.value)}
        style={{ marginBottom: 8, width: 300 }}
      />
      <Table
        columns={[
          { title: 'Mã', dataIndex: 'itemCode', width: 100 },
          { title: 'Tên hàng', dataIndex: 'itemName', ellipsis: true },
          { title: 'ĐVT', dataIndex: 'unit', width: 70 },
          {
            title: 'Tồn TT',
            dataIndex: 'actualQuantity',
            width: 80,
            align: 'right',
          },
          {
            title: 'Số lượng',
            dataIndex: 'quantity',
            width: 100,
            render: (_, record) => (
              <InputNumber
                min={0}
                value={getItemQuantity(record.variantId)}
                onChange={qty => updateItemQuantity(record.variantId, qty)}
                size="small"
                style={{ width: '100%' }}
              />
            ),
          },
        ]}
        dataSource={filteredVariants.map(v => ({ ...v, key: v.variantId }))}
        pagination={{ pageSize: 20, showSizeChanger: true }}
        size="small"
        scroll={{ y: 400 }}
      />
    </div>
  );
}
```

### 8.5. Convert form data → API request body

```javascript
const buildRequestItems = (request, product, dimensions) => {
  if (product.variantType === 'ITEM_BASED') {
    // ITEM_BASED: mỗi item = { variantId, quantity }
    return request.itemData
      .filter(item => item.quantity > 0)
      .map(item => ({
        variantId: item.variantId,
        quantity: item.quantity,
        // Tất cả structured fields = null
        styleId: null,
        sizeValue: null,
        lengthCode: null,
        gender: null,
      }));
  }

  // STRUCTURED with style (SP1,2)
  if (dimensions.hasStyle) {
    const items = [];
    Object.entries(request.matrixData).forEach(([styleId, sizeMap]) => {
      Object.entries(sizeMap).forEach(([sizeValue, lengthMap]) => {
        Object.entries(lengthMap).forEach(([lengthCode, qty]) => {
          if (qty > 0) {
            items.push({
              styleId: Number(styleId),
              sizeValue,         // String
              lengthCode,
              gender: null,
              variantId: null,
              quantity: qty,
            });
          }
        });
      });
    });
    return items;
  }

  // STRUCTURED with gender + length (SP4)
  if (dimensions.hasGender && dimensions.hasLength) {
    const items = [];
    Object.entries(request.matrixData).forEach(([gender, sizeMap]) => {
      Object.entries(sizeMap).forEach(([sizeValue, lengthMap]) => {
        Object.entries(lengthMap).forEach(([lengthCode, qty]) => {
          if (qty > 0) {
            items.push({
              styleId: null,
              sizeValue,
              lengthCode,
              gender,
              variantId: null,
              quantity: qty,
            });
          }
        });
      });
    });
    return items;
  }

  // STRUCTURED with gender only (SP3,5,6)
  if (dimensions.hasGender) {
    const items = [];
    Object.entries(request.matrixData).forEach(([gender, sizeMap]) => {
      Object.entries(sizeMap).forEach(([sizeValue, qty]) => {
        if (qty > 0) {
          items.push({
            styleId: null,
            sizeValue,
            lengthCode: null,
            gender,
            variantId: null,
            quantity: qty,
          });
        }
      });
    });
    return items;
  }

  return [];
};
```

### 8.6. Inventory validation (OUT/ADJUST_OUT)

**STRUCTURED**: Logic giữ nguyên nhưng dùng dynamic sizes
**ITEM_BASED**: So sánh trực tiếp quantity vs actualQuantity per variantId

```javascript
// ITEM_BASED validation
const validateItemBasedStock = (itemData, inventoryData, requestType) => {
  if (!['OUT', 'ADJUST_OUT'].includes(requestType)) return [];

  const errors = [];
  itemData.forEach(item => {
    const inv = inventoryData.find(d => d.variantId === item.variantId);
    const available = requestType === 'OUT' ? inv?.actualQuantity : inv?.expectedQuantity;
    if (item.quantity > available) {
      errors.push(`${inv.itemCode}: yêu cầu ${item.quantity}, tồn ${available}`);
    }
  });
  return errors;
};
```

---

## 9. Cập nhật hiển thị phiếu (RequestSetDetail)

### Files:
- `src/pages/RequestSetDetail.jsx`
- `src/components/RequestSetTableReadonly.jsx`

### 9.1. Hiển thị request items theo variantType

Khi hiển thị chi tiết phiếu đã tạo, cần branch:

```jsx
// Trong RequestSetTableReadonly hoặc RequestSetDetail
const renderRequestItems = (request, items) => {
  // Nhận diện variant type từ items
  const isItemBased = items.some(item => item.itemCode != null);

  if (isItemBased) {
    return <ItemBasedRequestTable items={items} />;
  }

  // STRUCTURED: phân nhánh tiếp
  const hasStyle = items.some(item => item.styleName != null);
  const hasLength = items.some(item => item.lengthCode != null);
  const hasGender = items.some(item => item.gender != null);

  // Render matrix phù hợp
  return <StructuredRequestMatrix items={items} hasStyle={hasStyle} hasLength={hasLength} hasGender={hasGender} />;
};
```

### 9.2. ITEM_BASED request display

```jsx
function ItemBasedRequestTable({ items }) {
  const columns = [
    { title: 'Mã hàng', dataIndex: 'itemCode', width: 120 },
    { title: 'Tên hàng', dataIndex: 'itemName', ellipsis: true },
    { title: 'ĐVT', dataIndex: 'unit', width: 80 },
    {
      title: 'Số lượng',
      dataIndex: 'quantity',
      width: 100,
      align: 'right',
      render: (val) => <Text strong>{val}</Text>,
    },
  ];

  return (
    <Table
      columns={columns}
      dataSource={items.map(i => ({ ...i, key: i.variantId }))}
      pagination={false}
      size="small"
    />
  );
}
```

### 9.3. STRUCTURED với Gender display

```jsx
// Khi items có gender → pivot thành matrix theo gender
function GenderRequestMatrix({ items, hasLength }) {
  const genders = [...new Set(items.map(i => i.gender))];
  const sizes = [...new Set(items.map(i => i.sizeValue))];

  if (hasLength) {
    // Gender × Size × Length matrix (giống Sơ mi matrix)
    // Rows = gender (NAM/NU)
    // Columns = sizes with Cộc/Dài sub-columns
  } else {
    // Gender × Size matrix (no sub-columns)
    // Rows = gender (NAM/NU)
    // Columns = sizes
  }
}
```

---

## 10. Cập nhật nhận hàng (ReceiveModal)

### File: `src/components/ReceiveModal.jsx`

### 10.1. Hiển thị variant info đầy đủ

Khi hiển thị danh sách items để nhập số lượng nhận:

```jsx
// ❌ CŨ: chỉ hiện styleName + sizeValue + lengthCode
// ✅ MỚI: hiện đầy đủ theo variant type

const getVariantLabel = (item) => {
  // ITEM_BASED
  if (item.itemCode) {
    return `${item.itemCode} - ${item.itemName} (${item.unit})`;
  }
  // STRUCTURED
  const parts = [];
  if (item.styleName) parts.push(item.styleName);
  if (item.gender) parts.push(item.gender === 'NAM' ? 'Nam' : 'Nữ');
  if (item.sizeValue) parts.push(`Size ${item.sizeValue}`);
  if (item.lengthCode) parts.push(item.lengthCode === 'COC' ? 'Cộc' : 'Dài');
  return parts.join(' - ');
};

// Sử dụng trong ReceiveModal columns:
{
  title: 'Biến thể',
  render: (_, record) => getVariantLabel(record),
}
```

### 10.2. Progress display (SetReceiptProgressDTO)

Tương tự, cập nhật label cho ItemProgress:

```jsx
// Trong progress table
const renderProgressItem = (item) => {
  if (item.itemCode) {
    // ITEM_BASED
    return (
      <div>
        <Text strong>{item.itemCode}</Text>
        <Text type="secondary" style={{ marginLeft: 8 }}>{item.itemName}</Text>
        <Tag style={{ marginLeft: 8 }}>{item.unit}</Tag>
      </div>
    );
  }
  // STRUCTURED
  return (
    <div>
      {item.styleName && <Text>{item.styleName}</Text>}
      {item.gender && <Tag>{item.gender === 'NAM' ? 'Nam' : 'Nữ'}</Tag>}
      <Text> Size {item.sizeValue}</Text>
      {item.lengthCode && <Text> {item.lengthCode === 'COC' ? 'Cộc' : 'Dài'}</Text>}
    </div>
  );
};
```

---

## 11. Tóm tắt file cần sửa

### Mức độ thay đổi

| File | Mức | Mô tả |
|------|-----|-------|
| `constants/index.js` | 🟡 Nhỏ | Xóa STYLES/SIZES hardcode, thêm GENDERS, VARIANT_TYPES |
| `api/requestApi.js` | 🟡 Nhỏ | Đổi param `style`→`filter` trong `getInventoryHistory()` |
| `pages/InventoryList.jsx` | 🟠 Trung bình | Thêm variant type badge, xử lý ITEM_BASED summary |
| **`pages/InventoryDetail.jsx`** | 🔴 **Lớn** | Refactor toàn bộ: branch STRUCTURED/ITEM_BASED, dynamic dimensions |
| **`pages/RequestSetCreate.jsx`** | 🔴 **Lớn** | Thêm ITEM_BASED editor, dynamic matrix cho STRUCTURED |
| **`components/RequestSetTableEditable.jsx`** | 🔴 **Lớn** | Dynamic columns, gender support, ITEM_BASED mode |
| `components/RequestSetTableReadonly.jsx` | 🟠 Trung bình | Branch hiển thị theo variant type |
| `components/ReceiveModal.jsx` | 🟡 Nhỏ | Cập nhật variant label display |
| `pages/RequestSetDetail.jsx` | 🟡 Nhỏ | Truyền variant type info xuống components |

### Thứ tự triển khai đề xuất

1. **constants + API** — nền tảng, ảnh hưởng mọi nơi
2. **InventoryList** — đơn giản nhất, verify API response mới hoạt động
3. **InventoryDetail** — phức tạp nhất, nên làm từng phần:
   - 3a. ITEM_BASED view (hoàn toàn mới, tách component riêng)
   - 3b. STRUCTURED dynamic (refactor matrix columns)
   - 3c. Gender tabs (thay thế style tabs cho SP3-6)
4. **RequestSetCreate + TableEditable** — form nhập liệu:
   - 4a. ITEM_BASED editor (component mới)
   - 4b. Dynamic STRUCTURED editor
5. **RequestSetDetail + TableReadonly** — readonly, dễ hơn
6. **ReceiveModal** — cập nhật labels

### Helper function nên tạo (file mới `src/utils/variantHelpers.js`)

```javascript
/**
 * Phân tích dimensions từ danh sách inventory items
 */
export const analyzeDimensions = (data) => { ... };

/**
 * Lấy label hiển thị cho 1 variant
 */
export const getVariantLabel = (item) => { ... };

/**
 * Quyết định tab config (style/gender/none) cho InventoryDetail
 */
export const getTabConfig = (product, dimensions) => { ... };

/**
 * Convert form matrixData/itemData → API items array
 */
export const buildRequestItems = (request, product, dimensions) => { ... };

/**
 * Validate stock cho OUT/ADJUST_OUT
 */
export const validateStock = (items, inventoryData, requestType, variantType) => { ... };
```

---

## Lưu ý quan trọng

### Backward compatibility
- SP1, SP2 (Sơ mi) giữ nguyên cấu trúc cũ — nếu code detect `hasStyle && hasLength && !hasGender` thì render y chang UI cũ
- Không cần migration data — API tự trả format mới

### Không hardcode
- **KHÔNG** hardcode danh sách sizes, styles, genders ở FE
- Tất cả lấy từ API response (`product.data` → extract unique values)
- Ngoại lệ: `LENGTHS` (COC/DAI) và `GENDERS` (NAM/NU) có thể giữ làm label mapping

### sizeValue là String
- Mọi nơi so sánh size phải dùng String: `"35"` không phải `35`
- Map key trong `matrixData`, `sizes` object đều là String
- Sắp xếp: API đã sort theo `size_order` → FE giữ nguyên thứ tự từ API

### Performance
- SP10 (Phụ liệu) có 258 variants → table cần pagination
- Inventory list load ALL products → nên thêm loading lazy hoặc virtualization nếu chậm

### Nhận diện variant type từ items (khi không có product info)
```javascript
// Khi chỉ có items mà không có product object:
const isItemBased = items.some(i => i.itemCode != null);
const hasStyle = items.some(i => i.styleName != null);
const hasGender = items.some(i => i.gender != null);
const hasLength = items.some(i => i.lengthCode != null);
```
