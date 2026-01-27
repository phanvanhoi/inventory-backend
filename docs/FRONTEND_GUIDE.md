# Hướng dẫn Frontend - Hệ thống Quản lý Kho HangFashion

## 1. Hệ thống Role

### 1.1 Bảng phân quyền tổng quan

| Role | Mô tả | Tạo phiếu | Duyệt | Execute | Xem dự kiến |
|------|-------|-----------|-------|---------|-------------|
| **ADMIN** | Quản trị viên | ❌ | ✅ | ❌ | ✅ |
| **USER** | Người dùng thường | IN, OUT | ❌ | ❌ | ❌ |
| **PURCHASER** | Thu mua | IN, OUT, ADJUST_IN, ADJUST_OUT | ❌ | ❌ | ✅ |
| **STOCKKEEPER** | Kiểm kho | ❌ | ❌ | ✅ | ❌ |

### 1.2 Chi tiết từng Role

#### ADMIN
- **Không được tạo** request set
- Duyệt/Từ chối bộ phiếu PENDING
- Reset password cho user khác
- Xem tất cả dữ liệu (actual + expected)

#### USER
- Tạo phiếu **IN** (nhập kho thực tế)
- Tạo phiếu **OUT** (xuất kho thực tế)
- Chỉ xem được tồn kho thực tế (actualQuantity)

#### PURCHASER
- Tạo được **tất cả 4 loại** phiếu:
  - **IN**: Nhập kho thực tế
  - **OUT**: Xuất kho thực tế
  - **ADJUST_IN**: Điều chỉnh nhập dự kiến (hàng sẽ về)
  - **ADJUST_OUT**: Điều chỉnh xuất dự kiến (hàng sẽ xuất)
- Xem được cả tồn thực tế và dự kiến

#### STOCKKEEPER
- Không tạo phiếu
- Xem danh sách phiếu đã APPROVED
- Execute (xác nhận thực hiện) phiếu
- Chỉ xem được tồn kho thực tế

---

## 2. Các loại phiếu (Request Type)

| Type | Mô tả | Ai tạo | Ảnh hưởng | Bắt buộc expected_date |
|------|-------|--------|-----------|------------------------|
| **IN** | Nhập kho | USER, PURCHASER | actualQuantity (+) | ❌ |
| **OUT** | Xuất kho | USER, PURCHASER | actualQuantity (-) | ❌ |
| **ADJUST_IN** | Dự kiến nhập | PURCHASER | expectedQuantity (+) | ✅ |
| **ADJUST_OUT** | Dự kiến xuất | PURCHASER | expectedQuantity (-) | ✅ |

### 2.1 Validation khi tạo phiếu

```
OUT:        quantity <= actualQuantity (tồn thực tế)
ADJUST_OUT: quantity <= expectedQuantity tại expected_date
```

---

## 3. Luồng trạng thái Request Set

```
                    ┌─────────────────┐
                    │     PENDING     │  ← Mặc định khi tạo
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              │              ▼
    ┌─────────────────┐      │    ┌─────────────────┐
    │    APPROVED     │      │    │    REJECTED     │
    └────────┬────────┘      │    └─────────────────┘
             │               │
             ▼               │
    ┌─────────────────┐      │
    │    EXECUTED     │      │
    └─────────────────┘      │
```

| Status | Mô tả | Ai thực hiện |
|--------|-------|--------------|
| PENDING | Chờ duyệt | Tự động khi tạo |
| APPROVED | Đã duyệt, chờ thực hiện | ADMIN |
| REJECTED | Bị từ chối | ADMIN |
| EXECUTED | Đã thực hiện nhập/xuất kho | STOCKKEEPER |

---

## 4. API Tạo Request Set

### 4.1 Bước 1: Lấy tên gợi ý

```http
GET /api/request-sets/suggested-name
Authorization: Bearer <token>
```

**Response:**
```json
{
  "suggestedName": "ĐX 1 - Hương"
}
```

### 4.2 Bước 2: Lấy danh sách Units (đơn vị/khách hàng)

```http
GET /api/units
Authorization: Bearer <token>
```

**Response:**
```json
[
  { "unitId": 1, "unitName": "BĐ Hà Nội" },
  { "unitId": 2, "unitName": "BĐ Hải Phòng" },
  ...
]
```

### 4.3 Bước 3: Lấy danh sách Products

```http
GET /api/products
Authorization: Bearer <token>
```

**Response:**
```json
[
  {
    "productId": 1,
    "productName": "HDH22 - TRẮNG KEM NAM BƯU ĐIỆN",
    "note": "Sơ mi nam 2025"
  }
]
```

### 4.4 Bước 4: Lấy tồn kho hiện tại (để validate)

```http
GET /api/inventory/{productId}
Authorization: Bearer <token>
```

**Response:**
```json
{
  "productId": 1,
  "productName": "HDH22 - TRẮNG KEM NAM BƯU ĐIỆN",
  "canViewExpected": true,
  "data": [
    {
      "styleName": "CỔ ĐIỂN",
      "sizeValue": 38,
      "lengthCode": "COC",
      "actualQuantity": 10,
      "expectedQuantity": 15
    },
    ...
  ]
}
```

### 4.5 Bước 5: Tạo Request Set

```http
POST /api/request-sets
Authorization: Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "setName": "ĐX 1 - Hương",
  "description": "Xuất hàng cho BĐ Hà Nội",
  "requests": [
    {
      "unitId": 1,
      "productId": 1,
      "requestType": "OUT",
      "expectedDate": null,
      "note": "Ghi chú cho phiếu này",
      "items": [
        {
          "styleId": 1,
          "sizeValue": 38,
          "lengthCode": "COC",
          "quantity": 5
        },
        {
          "styleId": 1,
          "sizeValue": 39,
          "lengthCode": "DAI",
          "quantity": 3
        }
      ]
    }
  ]
}
```

**Response:**
```json
2
```
(Trả về setId)

---

## 5. Ví dụ tạo các loại phiếu

### 5.1 Phiếu OUT (Xuất kho thực tế)

```json
{
  "setName": "ĐX 1 - Hương",
  "description": "Xuất hàng cho BĐ Hà Nội",
  "requests": [
    {
      "unitId": 1,
      "productId": 1,
      "requestType": "OUT",
      "expectedDate": null,
      "items": [
        { "styleId": 1, "sizeValue": 38, "lengthCode": "COC", "quantity": 5 }
      ]
    }
  ]
}
```

### 5.2 Phiếu IN (Nhập kho thực tế)

```json
{
  "setName": "ĐX 2 - Hương",
  "description": "Nhập hàng từ nhà máy",
  "requests": [
    {
      "unitId": 52,
      "productId": 1,
      "requestType": "IN",
      "expectedDate": null,
      "items": [
        { "styleId": 1, "sizeValue": 38, "lengthCode": "COC", "quantity": 100 }
      ]
    }
  ]
}
```

### 5.3 Phiếu ADJUST_IN (Dự kiến nhập - chỉ PURCHASER)

```json
{
  "setName": "ĐX 3 - Nga",
  "description": "Dự kiến nhập hàng từ nhà máy ngày 15/02",
  "requests": [
    {
      "unitId": 52,
      "productId": 1,
      "requestType": "ADJUST_IN",
      "expectedDate": "2026-02-15",
      "items": [
        { "styleId": 1, "sizeValue": 38, "lengthCode": "COC", "quantity": 100 }
      ]
    }
  ]
}
```

### 5.4 Phiếu ADJUST_OUT (Dự kiến xuất - chỉ PURCHASER)

```json
{
  "setName": "ĐX 4 - Nga",
  "description": "Dự kiến xuất cho BĐ Hà Nội ngày 20/02",
  "requests": [
    {
      "unitId": 1,
      "productId": 1,
      "requestType": "ADJUST_OUT",
      "expectedDate": "2026-02-20",
      "items": [
        { "styleId": 1, "sizeValue": 38, "lengthCode": "COC", "quantity": 50 }
      ]
    }
  ]
}
```

---

## 6. Mapping styleId

| styleId | styleName |
|---------|-----------|
| 1 | CỔ ĐIỂN |
| 2 | CỔ ĐIỂN NGẮN |
| 3 | SLIM |
| 4 | SLIM Ngắn |

---

## 7. Mapping lengthCode

| lengthCode | Mô tả |
|------------|-------|
| COC | Cộc (tay ngắn) |
| DAI | Dài (tay dài) |

---

## 8. Size hợp lệ

```
35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45
```

---

## 9. Xử lý lỗi validation

### 9.1 Lỗi số lượng xuất vượt tồn kho

```json
{
  "error": "Số lượng xuất (10) vượt quá tồn kho thực tế (5) cho biến thể: size=38, length=COC"
}
```

### 9.2 Lỗi expected_date bắt buộc

```json
{
  "error": "Ngày dự kiến (expected_date) là bắt buộc cho phiếu ADJUST_IN"
}
```

### 9.3 Lỗi expected_date phải >= hôm nay

```json
{
  "error": "Ngày dự kiến phải >= hôm nay (2026-01-23)"
}
```

### 9.4 Lỗi role không có quyền

```json
{
  "error": "Access Denied"
}
```
(HTTP 403)

---

## 10. Flow UI đề xuất

### 10.1 Màn hình tạo Request Set

```
┌─────────────────────────────────────────────────────────────┐
│  Tạo bộ phiếu mới                                           │
├─────────────────────────────────────────────────────────────┤
│  Tên bộ phiếu: [ĐX 1 - Hương____________] (auto-fill)       │
│  Mô tả:        [_____________________________]              │
├─────────────────────────────────────────────────────────────┤
│  Phiếu #1                                           [+ Thêm]│
│  ┌───────────────────────────────────────────────────────┐  │
│  │ Loại phiếu: [▼ OUT        ]                           │  │
│  │ Đơn vị:     [▼ BĐ Hà Nội  ]                           │  │
│  │ Sản phẩm:   [▼ HDH22...   ]                           │  │
│  │ Ngày dự kiến: [____] (chỉ hiện nếu ADJUST_IN/OUT)     │  │
│  │                                                       │  │
│  │ ┌─────────────────────────────────────────────────┐   │  │
│  │ │ Style    │ Size │ Length │ Tồn kho │ Số lượng │   │  │
│  │ ├──────────┼──────┼────────┼─────────┼──────────┤   │  │
│  │ │ CỔ ĐIỂN  │  38  │  COC   │   10    │ [__5__]  │   │  │
│  │ │ CỔ ĐIỂN  │  39  │  DAI   │    8    │ [__3__]  │   │  │
│  │ └─────────────────────────────────────────────────┘   │  │
│  └───────────────────────────────────────────────────────┘  │
│                                                             │
│                              [Hủy]  [Tạo bộ phiếu]          │
└─────────────────────────────────────────────────────────────┘
```

### 10.2 Logic hiển thị theo Role

```javascript
// Kiểm tra role để hiển thị options
const user = getCurrentUser();
const isPurchaser = user.roles.includes('PURCHASER');

// Options cho dropdown "Loại phiếu"
const requestTypeOptions = isPurchaser
  ? ['IN', 'OUT', 'ADJUST_IN', 'ADJUST_OUT']
  : ['IN', 'OUT'];

// Hiển thị cột "Tồn dự kiến"
const showExpectedColumn = isPurchaser || user.roles.includes('ADMIN');

// Hiển thị field "Ngày dự kiến"
const showExpectedDate = ['ADJUST_IN', 'ADJUST_OUT'].includes(selectedType);
```

---

## 11. API khác

### 11.1 Lấy danh sách Request Sets

```http
GET /api/request-sets
GET /api/request-sets?status=PENDING
GET /api/request-sets?status=APPROVED
```

### 11.2 Lấy chi tiết Request Set

```http
GET /api/request-sets/{setId}
```

### 11.3 Duyệt phiếu (ADMIN)

```http
POST /api/request-sets/{setId}/approve
```

### 11.4 Từ chối phiếu (ADMIN)

```http
POST /api/request-sets/{setId}/reject
Content-Type: application/json

{
  "reason": "Lý do từ chối"
}
```

### 11.5 Execute phiếu (STOCKKEEPER)

```http
POST /api/request-sets/{setId}/execute
```

### 11.6 Xóa phiếu

```http
DELETE /api/request-sets/{setId}
```
