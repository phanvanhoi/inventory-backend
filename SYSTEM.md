# HangFashion Inventory Management System

> Tài liệu hệ thống dành cho AI đọc hiểu. Cập nhật lần cuối: 2026-02-02.

## 1. Tổng quan

Hệ thống quản lý tồn kho cho HangFashion, xử lý nhập/xuất kho thực tế và dự kiến thông qua workflow duyệt phiếu. Sản phẩm chính là áo đồng phục bưu điện, phân loại theo **kiểu dáng (style)**, **kích cỡ (size 35-45)**, **độ dài (Cộc/Dài)**.

**Tech stack:** Spring Boot 4.0.1, Java 17, MySQL, JWT, Spring Security, JPA/Hibernate, Lombok, Maven.

**Frontend:** React (Vite) chạy tại `http://localhost:5173`.

---

## 2. Database Schema

```
┌─────────────────────────────────────────────────────────────┐
│                        15 TABLES                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  users ──M:N── roles          (qua bảng user_roles)        │
│    │                                                        │
│    ├── request_sets           (created_by, executed_by)     │
│    │     ├── approval_history (performed_by → users)        │
│    │     ├── notifications    (user → users)                │
│    │     └── inventory_requests                             │
│    │           ├── unit       (FK → units)                  │
│    │           ├── position   (FK → positions, nullable)    │
│    │           ├── product    (FK → products)               │
│    │           └── inventory_request_items                  │
│    │                 └── variant (FK → product_variants)    │
│    │                                                        │
│  product_variants ── style  (FK → styles)                   │
│                   ── size   (FK → sizes)                    │
│                   ── length (FK → length_types)             │
└─────────────────────────────────────────────────────────────┘
```

### Master Data

| Table | Data | Ghi chú |
|-------|------|---------|
| `roles` | ADMIN, USER, STOCKKEEPER, PURCHASER | 4 vai trò |
| `styles` | CỔ ĐIỂN, CỔ ĐIỂN NGẮN, SLIM, SLIM Ngắn | 4 kiểu dáng |
| `sizes` | 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45 | 11 kích cỡ |
| `length_types` | COC (Cộc), DAI (Dài) | 2 loại độ dài |
| `positions` | GDV (Giao dịch viên), VHX (Vận hành xưởng) | Chức danh |
| `product_variants` | 88 bản ghi = 4 styles × 11 sizes × 2 lengths | Tổ hợp biến thể |
| `units` | 72 bưu điện/khách hàng | Đơn vị |

### Bảng chính

#### `request_sets`
| Column | Type | Ghi chú |
|--------|------|---------|
| set_id | BIGINT PK | |
| set_name | VARCHAR(255) | VD: "ĐX 4 - Thúy" |
| description | TEXT | |
| status | ENUM | DRAFT, PENDING, APPROVED, REJECTED, EXECUTED |
| created_by | BIGINT FK → users | Người tạo |
| created_at | DATETIME | |
| submitted_at | DATETIME | Thời điểm gửi duyệt |
| executed_by | BIGINT FK → users | Người thực hiện (STOCKKEEPER) |
| executed_at | DATETIME | |

#### `inventory_requests`
| Column | Type | Ghi chú |
|--------|------|---------|
| request_id | BIGINT PK | |
| set_id | BIGINT FK → request_sets | |
| unit_id | BIGINT FK → units | Đơn vị (Bưu điện Kon Tum, ...) |
| position_id | BIGINT FK → positions | Chức danh (GDV, VHX), nullable |
| product_id | BIGINT FK → products | |
| request_type | ENUM | IN, OUT, ADJUST_IN, ADJUST_OUT |
| expected_date | DATE | Bắt buộc cho ADJUST_IN/OUT |
| note | TEXT | |
| created_at | DATETIME | |

#### `inventory_request_items`
| Column | Type | Ghi chú |
|--------|------|---------|
| item_id | BIGINT PK | |
| request_id | BIGINT FK | CASCADE DELETE |
| variant_id | BIGINT FK → product_variants | |
| quantity | INT | |

---

## 3. Roles & Permissions

```
ADMIN:
  ├── Duyệt/Từ chối bộ phiếu (PENDING → APPROVED/REJECTED)
  ├── KHÔNG được tạo bộ phiếu (nếu chỉ có role ADMIN)
  ├── Reset password user khác
  ├── Xem TẤT CẢ bộ phiếu (sắp xếp theo tên người tạo)
  └── Xem cả actualQuantity và expectedQuantity trong tồn kho

USER (role mặc định khi đăng ký):
  ├── Tạo phiếu IN/OUT (ảnh hưởng tồn kho thực tế)
  ├── Chỉ xem bộ phiếu CỦA MÌNH
  └── Chỉ xem actualQuantity

PURCHASER (Thu mua):
  ├── Tạo cả 4 loại phiếu (IN, OUT, ADJUST_IN, ADJUST_OUT)
  ├── Chỉ xem bộ phiếu CỦA MÌNH
  └── Xem cả actualQuantity và expectedQuantity

STOCKKEEPER (Kiểm kho):
  ├── KHÔNG tạo bộ phiếu
  ├── Xem TẤT CẢ bộ phiếu
  ├── Chỉ xem bộ phiếu APPROVED trong /approved
  ├── Thực hiện phiếu (APPROVED → EXECUTED)
  └── Chỉ xem actualQuantity
```

---

## 4. Workflow

```
                    ┌─────────┐
                    │  DRAFT  │ (optional, hiện tạo thẳng PENDING)
                    └────┬────┘
                         │ submitForApproval()
                    ┌────▼────┐
              ┌─────│ PENDING │─────┐
              │     └─────────┘     │
       approve()              reject()
              │                     │
        ┌─────▼─────┐       ┌──────▼──────┐
        │  APPROVED  │       │  REJECTED   │
        └─────┬──────┘       └──────┬──────┘
              │                     │
       execute()          updateRequestSet()
              │              (auto → PENDING)
        ┌─────▼─────┐              │
        │  EXECUTED  │       ┌─────▼─────┐
        └────────────┘       │  PENDING  │ (resubmit)
                             └───────────┘
```

### Request Types

| Type | Ảnh hưởng | Ai tạo được | expected_date |
|------|-----------|-------------|---------------|
| IN | +actualQuantity | USER, PURCHASER | Không bắt buộc |
| OUT | -actualQuantity | USER, PURCHASER | Không bắt buộc |
| ADJUST_IN | +expectedQuantity | PURCHASER only | Bắt buộc, >= today |
| ADJUST_OUT | -expectedQuantity | PURCHASER only | Bắt buộc, >= today |

### Inventory Calculations

```
actualQuantity = SUM(IN) - SUM(OUT)
                 [chỉ từ request_sets có status = 'EXECUTED']

expectedQuantity = actualQuantity
                 + SUM(ADJUST_IN) - SUM(ADJUST_OUT)
                 [ADJUST types từ status IN ('PENDING','APPROVED','EXECUTED')]
```

---

## 5. API Endpoints

### 5.1 Auth (`/api/auth`) - Public

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/login` | `{username, password}` | `{token, userId, username, fullName, roles}` |
| POST | `/register` | `{username, password, fullName, email}` | AuthResponseDTO |
| POST | `/change-password` | `{oldPassword, newPassword}` | 200 OK |

### 5.2 Users (`/api/users`)

| Method | Path | Auth | Response |
|--------|------|------|----------|
| GET | `/` | ADMIN | List\<UserDTO\> |
| GET | `/me` | Any | UserDTO (current user) |
| POST | `/{userId}/reset-password` | ADMIN | 200 OK |

### 5.3 Products (`/api/products`)

| Method | Path | Body | Response |
|--------|------|------|----------|
| GET | `/` | | List\<Product\> |
| GET | `/{id}` | | Product |
| POST | `/` | Product | Product |
| PUT | `/{id}` | Product | Product |
| DELETE | `/{id}` | | 204 |

### 5.4 Units (`/api/units`) - CRUD tương tự Products

### 5.5 Positions (`/api/positions`)

| Method | Path | Body | Response |
|--------|------|------|----------|
| GET | `/` | | List\<Position\> |
| GET | `/{id}` | | Position |
| POST | `/` | `{positionCode, positionName}` | Position |
| PUT | `/{id}` | `{positionCode, positionName}` | Position |
| DELETE | `/{id}` | | 204 |

### 5.6 Request Sets (`/api/request-sets`)

| Method | Path | Auth | Body/Params | Response |
|--------|------|------|-------------|----------|
| GET | `/suggested-name` | USER/PURCHASER | | `{suggestedName: "ĐX 4 - Thúy"}` |
| POST | `/` | USER/PURCHASER | RequestSetCreateDTO | setId |
| GET | `/` | Any | `?status=PENDING,APPROVED` | List\<RequestSetListDTO\> |
| GET | `/{setId}` | Any | | RequestSetDetailDTO |
| PUT | `/{setId}` | USER/PURCHASER (owner) | RequestSetUpdateDTO | 200 (chỉ REJECTED) |
| DELETE | `/{setId}` | | | 204 |
| DELETE | `/` | ADMIN | | 204 |
| POST | `/{setId}/submit` | USER/PURCHASER (owner) | | 200 |
| POST | `/{setId}/approve` | ADMIN | | 200 |
| POST | `/{setId}/reject` | ADMIN | `{reason}` | 200 |
| POST | `/{setId}/execute` | STOCKKEEPER | | 200 |
| GET | `/approved` | STOCKKEEPER | | List\<RequestSetListDTO\> |

### 5.7 Inventory Requests (`/api/requests`)

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/` | InventoryRequestCreateDTO | `{requestId}` |
| GET | `/` | | List\<InventoryRequestListDTO\> |
| GET | `/{id}` | | List\<InventoryRequestItemDTO\> |
| GET | `/{id}/detail` | | InventoryRequestDetailDTO |
| DELETE | `/{id}` | | 204 |
| DELETE | `/` | | 204 |
| PATCH | `/{id}/request-type` | `{requestType}` | 200 |
| PATCH | `/{id}/expected-date` | `{newExpectedDate}` | 200 |
| GET | `/{id}/dependent-adjust-out` | | `{count, canMoveDate}` |

### 5.8 Items (`/api/items`)

| Method | Path | Body | Response |
|--------|------|------|----------|
| POST | `/` | ItemCreateDTO | itemId |
| GET | `/{itemId}` | | ItemDetailDTO |
| GET | `/request/{requestId}` | | List\<ItemDetailDTO\> |
| PUT | `/{itemId}` | `{quantity}` | ItemDetailDTO |
| DELETE | `/{itemId}` | | 204 |

### 5.9 Inventory (`/api/inventory`)

| Method | Path | Auth | Response |
|--------|------|------|----------|
| GET | `/` | Any | List\<ProductInventoryViewDTO\> |
| GET | `/{productId}` | Any | ProductInventoryViewDTO |
| GET | `/{productId}/history?style=CỔ ĐIỂN` | Any | RequestHistoryMatrixDTO |

**Permission logic:**
- ADMIN/PURCHASER: xem cả `actualQuantity` và `expectedQuantity`
- USER/STOCKKEEPER: chỉ xem `actualQuantity` (expectedQuantity = null)
- History: ADMIN/PURCHASER xem APPROVED + EXECUTED; USER/STOCKKEEPER chỉ xem EXECUTED

### 5.10 Notifications (`/api/notifications`)

| Method | Path | Response |
|--------|------|----------|
| GET | `/` | List\<NotificationDTO\> |
| GET | `/unread` | List\<NotificationDTO\> |
| GET | `/unread/count` | Long |
| POST | `/{id}/read` | 200 |
| POST | `/read-all` | 200 |

---

## 6. Key DTOs

### Request body khi tạo Request Set

```json
{
  "setName": "ĐX 4 - Thúy",
  "description": "Xuất hàng cho BĐ Kon Tum",
  "requests": [
    {
      "unitId": 6,
      "positionCode": "GDV",        // String, resolve sang position_id trong service
      "productId": 1,
      "requestType": "OUT",
      "expectedDate": null,          // Bắt buộc nếu ADJUST_IN/ADJUST_OUT
      "note": "",
      "items": [
        {
          "styleId": 1,              // CỔ ĐIỂN
          "sizeValue": 38,
          "lengthCode": "COC",       // hoặc "DAI"
          "quantity": 10
        }
      ]
    }
  ]
}
```

### RequestSetListDTO (response list)

```json
{
  "setId": 5,
  "setName": "ĐX 4 - Thúy",
  "description": null,
  "status": "PENDING",
  "createdBy": 3,
  "createdByName": "Thúy",
  "createdAt": "2026-02-01T10:00:00",
  "submittedAt": "2026-02-01T10:00:00",
  "requestCount": 2,
  "requestTypes": "IN,OUT",
  "productNames": "HDH22 - TRẮNG KEM NAM",
  "positionCodes": "GDV,VHX",
  "earliestExpectedDate": null
}
```

### Inventory History Matrix (response `/api/inventory/{id}/history?style=`)

```json
{
  "productId": 1,
  "productName": "HDH22...",
  "styleName": "CỔ ĐIỂN",
  "sizeColumns": [35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45],
  "rows": [
    {
      "requestId": 1,
      "setId": 1,
      "setName": "ĐX 1",
      "setStatus": "EXECUTED",
      "unitName": "Bưu điện Kon Tum",
      "requestType": "OUT",
      "lengthCode": "COC",
      "note": null,
      "createdAt": "2026-01-27T10:00:00",
      "createdBy": 5,
      "createdByName": "Nguyễn Văn A",
      "sizes": {"35": 0, "36": 0, "37": 0, "38": 1, "39": 0, ...}
    }
  ]
}
```

---

## 7. Notification Logic

| Event | Ai nhận |
|-------|---------|
| Submit bộ phiếu | Tất cả ADMIN |
| Approve | Người tạo + Tất cả STOCKKEEPER |
| Reject | Người tạo (kèm lý do) |
| Execute | Người tạo |

---

## 8. Security

- **JWT Token:** Bearer token trong header `Authorization`
- **Token payload:** userId, fullName, roles[]
- **Expiration:** 24 giờ
- **Password:** BCrypt encoded
- **CORS:** Chỉ cho phép `http://localhost:5173`
- **Public endpoints:** `/api/auth/**`
- **Protected endpoints:** Tất cả còn lại cần JWT

---

## 9. Project Structure

```
src/main/java/manage/store/inventory/
├── config/
│   ├── SecurityConfig.java          # Spring Security + CORS
│   └── WebConfig.java               # CORS mapping
├── controller/
│   ├── AuthController.java          # /api/auth
│   ├── UserController.java          # /api/users
│   ├── ProductController.java       # /api/products
│   ├── UnitController.java          # /api/units
│   ├── PositionController.java      # /api/positions
│   ├── ItemController.java          # /api/items
│   ├── InventoryRequestController.java  # /api/requests
│   ├── InventoryController.java     # /api/inventory
│   ├── RequestSetController.java    # /api/request-sets
│   └── NotificationController.java  # /api/notifications
├── dto/                             # 20+ DTO classes
├── entity/
│   ├── User.java, Role.java
│   ├── Product.java, ProductVariant.java
│   ├── Style.java, Size.java, LengthType.java
│   ├── Unit.java, Position.java
│   ├── RequestSet.java
│   ├── InventoryRequest.java, InventoryRequestItem.java
│   ├── ApprovalHistory.java, Notification.java
│   └── enums/ (RequestSetStatus, ApprovalAction)
├── repository/                      # 12 JPA repositories
├── security/
│   ├── JwtUtil.java                 # Token generation/validation
│   ├── JwtAuthenticationFilter.java # Filter chain
│   ├── UserPrincipal.java           # Auth principal
│   └── CurrentUser.java             # Helper lấy user hiện tại
├── service/
│   ├── AuthService.java
│   ├── InventoryRequestService.java + Impl
│   ├── RequestSetService.java + Impl
│   ├── ItemService.java + Impl
│   └── NotificationService.java
└── InventoryApplication.java

src/main/resources/
├── application.yaml
├── application.yaml.example
└── complete-database.sql            # Full schema + seed data
```

---

## 10. Business Rules quan trọng

1. **Một user có thể có nhiều role.** Ví dụ: USER + PURCHASER. Logic kiểm tra quyền dùng `hasRole()`, `isAdmin()`, v.v.

2. **ADMIN không được tạo bộ phiếu** nếu chỉ có role ADMIN. Phải kèm USER hoặc PURCHASER.

3. **STOCKKEEPER không được tạo bộ phiếu** nếu chỉ có role STOCKKEEPER.

4. **Request type transition:** ADJUST_IN → IN, ADJUST_OUT → OUT (khi thực hiện thực tế). Không cho phép transition ngược hoặc chéo.

5. **ADJUST_OUT dependency:** Không thể dời ngày ADJUST_IN nếu có ADJUST_OUT phụ thuộc (expected_date >= ADJUST_IN date).

6. **Update rejected set:** Chỉ owner được sửa, chỉ khi status = REJECTED. Sau khi sửa tự động chuyển về PENDING.

7. **Role-based list filtering:**
   - USER/PURCHASER: `findAllSetsByCreatedBy(userId)`
   - ADMIN: `findAllSetsOrderByCreatorName()`
   - STOCKKEEPER: `findAllSets()`

8. **positionCode trong DTO là String** (VD: "GDV"), service tự resolve sang `position_id` qua `PositionRepository.findByPositionCode()`.

9. **History matrix:** Group by `requestId + lengthCode`, sizes là Map\<Integer, Integer\> (size → quantity). SIZE_COLUMNS cố định = [35..45].

10. **Suggested name format:** `"ĐX {count+1} - {fullName}"` — count là số bộ phiếu user đã tạo.
