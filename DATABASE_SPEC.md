# Database Specification: Inventory Management System

## Tổng quan

Database `inventory_db` là hệ thống quản lý kho hàng cho sản phẩm thời trang (áo sơ mi), hỗ trợ theo dõi nhập/xuất/điều chỉnh tồn kho theo đơn vị (bưu điện) với các biến thể sản phẩm (kiểu dáng, size, chiều dài).

---

## Schema

### 1. `units` - Đơn vị/Chi nhánh

| Column    | Type         | Constraints          | Description          |
|-----------|--------------|----------------------|----------------------|
| unit_id   | BIGINT       | PK, AUTO_INCREMENT   | ID đơn vị            |
| unit_name | VARCHAR(255) | UNIQUE, NOT NULL     | Tên đơn vị/chi nhánh |

---

### 2. `users` - Người dùng

| Column     | Type         | Constraints          | Description      |
|------------|--------------|----------------------|------------------|
| user_id    | BIGINT       | PK, AUTO_INCREMENT   | ID người dùng    |
| username   | VARCHAR(100) | UNIQUE, NOT NULL     | Tên đăng nhập    |
| full_name  | VARCHAR(255) | NOT NULL             | Họ tên đầy đủ    |
| email      | VARCHAR(255) | NULL                 | Email            |
| created_at | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

### 3. `products` - Sản phẩm

| Column       | Type         | Constraints        | Description    |
|--------------|--------------|-------------------|----------------|
| product_id   | BIGINT       | PK, AUTO_INCREMENT | ID sản phẩm    |
| product_name | VARCHAR(255) | NOT NULL           | Tên sản phẩm   |

---

### 5. `styles` - Kiểu dáng

| Column     | Type         | Constraints          | Description   |
|------------|--------------|----------------------|---------------|
| style_id   | BIGINT       | PK, AUTO_INCREMENT   | ID kiểu dáng  |
| style_name | VARCHAR(100) | UNIQUE, NOT NULL     | Tên kiểu dáng |

**Giá trị mẫu:** Cổ điển, Cổ điển ngắn, Slim, Slim ngắn

---

### 6. `sizes` - Kích cỡ

| Column     | Type   | Constraints          | Description |
|------------|--------|----------------------|-------------|
| size_id    | BIGINT | PK, AUTO_INCREMENT   | ID size     |
| size_value | INT    | UNIQUE, NOT NULL     | Giá trị size |

**Giá trị mẫu:** 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45

---

### 7. `length_types` - Loại chiều dài

| Column         | Type              | Constraints          | Description       |
|----------------|-------------------|----------------------|-------------------|
| length_type_id | BIGINT            | PK, AUTO_INCREMENT   | ID loại chiều dài |
| code           | ENUM('COC','DAI') | UNIQUE, NOT NULL     | Mã loại           |

**Giá trị:**
- `COC` - Cộc (ngắn)
- `DAI` - Dài

---

### 8. `product_variants` - Biến thể sản phẩm

| Column         | Type   | Constraints                              | Description       |
|----------------|--------|------------------------------------------|-------------------|
| variant_id     | BIGINT | PK, AUTO_INCREMENT                       | ID biến thể       |
| style_id       | BIGINT | FK → styles, NOT NULL                    | Kiểu dáng         |
| size_id        | BIGINT | FK → sizes, NOT NULL                     | Kích cỡ           |
| length_type_id | BIGINT | FK → length_types, NOT NULL              | Loại chiều dài    |

**Constraints:**
- UNIQUE KEY `uk_variant` (style_id, size_id, length_type_id)

**Tổng số biến thể:** 4 styles × 11 sizes × 2 lengths = **88 variants**

---

### 9. `request_sets` - Bộ phiếu

| Column      | Type         | Constraints            | Description      |
|-------------|--------------|------------------------|------------------|
| set_id      | BIGINT       | PK, AUTO_INCREMENT     | ID bộ phiếu      |
| set_name    | VARCHAR(255) | NOT NULL               | Tên bộ phiếu     |
| description | VARCHAR(255) | NULL                   | Mô tả            |
| created_by  | BIGINT       | FK → users, NULL       | Người tạo        |
| created_at  | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

**Mục đích:** Nhóm nhiều phiếu nhập/xuất/điều chỉnh thành một bộ, ví dụ: "ĐX-THÚY" chứa nhiều phiếu xuất cho khách hàng Thúy.

---

### 10. `inventory_requests` - Phiếu yêu cầu kho

| Column       | Type                        | Constraints            | Description        |
|--------------|-----------------------------|------------------------|--------------------|
| request_id   | BIGINT                      | PK, AUTO_INCREMENT     | ID phiếu           |
| set_id       | BIGINT                      | FK → request_sets, NULL| Bộ phiếu (nếu có)  |
| unit_id      | BIGINT                      | FK → units, NOT NULL   | Đơn vị thực hiện   |
| product_id   | BIGINT                      | FK → products, NOT NULL| Sản phẩm           |
| request_type | ENUM('IN','OUT','ADJUST_IN','ADJUST_OUT') | NOT NULL | Loại phiếu |
| note         | VARCHAR(255)                | NULL                   | Ghi chú            |
| created_at   | TIMESTAMP                   | DEFAULT CURRENT_TIMESTAMP | Thời gian tạo   |

**Loại phiếu:**
- `IN` - Nhập kho thực tế
- `OUT` - Xuất kho thực tế
- `ADJUST_IN` - Dự kiến nhập
- `ADJUST_OUT` - Dự kiến xuất

---

### 11. `inventory_request_items` - Chi tiết phiếu

| Column     | Type   | Constraints                              | Description     |
|------------|--------|------------------------------------------|-----------------|
| item_id    | BIGINT | PK, AUTO_INCREMENT                       | ID dòng         |
| request_id | BIGINT | FK → inventory_requests, NOT NULL        | Phiếu cha       |
| variant_id | BIGINT | FK → product_variants, NOT NULL          | Biến thể SP     |
| quantity   | INT    | NOT NULL, CHECK (quantity > 0)           | Số lượng        |

---

## Công thức tính tồn kho

### Tồn kho thực tế (Actual Quantity)

```
actualQuantity = SUM(IN) - SUM(OUT)
```

Chỉ tính các giao dịch nhập/xuất thực tế đã xảy ra.

### Tồn kho dự kiến (Expected Quantity)

```
expectedQuantity = SUM(IN) - SUM(OUT) + SUM(ADJUST_IN) - SUM(ADJUST_OUT)
```

Bao gồm cả các giao dịch dự kiến sẽ xảy ra trong tương lai.

**Ví dụ:**
- Tồn kho thực tế: 100 (đã nhập 150, đã xuất 50)
- Dự kiến nhập thêm: 30
- Dự kiến xuất: 20
- Tồn kho dự kiến: 100 + 30 - 20 = 110

---

## Entity Relationship Diagram

```
                    ┌──────────────┐
                    │ request_sets │
                    └──────┬───────┘
                           │ 1:N (optional)
                           ▼
┌─────────┐     ┌──────────────────┐     ┌─────────────────┐
│  units  │────<│ inventory_       │>────│    products     │
└─────────┘     │ requests         │     └─────────────────┘
                └────────┬─────────┘
                         │
                         │ 1:N
                         ▼
              ┌────────────────────┐
              │inventory_request_  │
              │      items         │
              └────────┬───────────┘
                       │
                       │ N:1
                       ▼
              ┌─────────────────┐
              │product_variants │
              └───┬───┬───┬─────┘
                  │   │   │
         ┌────────┘   │   └────────┐
         ▼            ▼            ▼
    ┌────────┐   ┌────────┐  ┌──────────────┐
    │ styles │   │ sizes  │  │ length_types │
    └────────┘   └────────┘  └──────────────┘
```

---

## Business Rules

1. **Biến thể sản phẩm** là tổ hợp duy nhất của (style, size, length_type)
2. **Quantity** phải > 0 trong mọi phiếu
3. **Tồn kho** có thể âm (nợ kho)
4. Mỗi phiếu thuộc về **một đơn vị** và **một sản phẩm**
5. Một phiếu có thể chứa **nhiều biến thể** khác nhau
6. **Bộ phiếu** (request_set) có thể chứa nhiều phiếu từ các đơn vị khác nhau
7. Một phiếu có thể thuộc hoặc không thuộc bộ phiếu nào (set_id nullable)

---

## Character Set

- **Charset:** utf8mb4
- **Collation:** utf8mb4_unicode_ci

Hỗ trợ đầy đủ tiếng Việt có dấu và emoji.
