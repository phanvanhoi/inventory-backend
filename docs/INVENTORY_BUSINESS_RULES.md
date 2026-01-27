# Inventory Business Rules - HangFashion

## 1. Roles & Permissions

| Role | Tạo phiếu | Loại phiếu được tạo | Duyệt | Thực hiện | Xem tồn kho |
|------|-----------|---------------------|-------|-----------|-------------|
| **ADMIN** | Không | - | Có (trừ phiếu của mình) | Không | Thực tế + Dự kiến |
| **USER** | Có | IN, OUT | Không | Không | Chỉ Thực tế |
| **PURCHASER** | Có | IN, OUT, ADJUST_IN, ADJUST_OUT | Không | Không | Thực tế + Dự kiến |
| **STOCKKEEPER** | Không | - | Không | Có (xác nhận nhập/xuất kho) | Chỉ Thực tế (APPROVED/EXECUTED) |

## 2. Request Types & Impact

| Loại | Ảnh hưởng | Mô tả |
|------|-----------|-------|
| **IN** | Tồn thực tế (+) | Nhập kho thực tế |
| **OUT** | Tồn thực tế (-) | Xuất kho thực tế |
| **ADJUST_IN** | Tồn dự kiến (+) | Dự kiến nhập (cần expected_date) |
| **ADJUST_OUT** | Tồn dự kiến (-) | Dự kiến xuất (cần expected_date) |

## 3. Công thức tính tồn kho

### Tồn kho thực tế (Actual Quantity)
```
Tồn thực tế = SUM(IN đã EXECUTED) - SUM(OUT đã EXECUTED)
```
**Quan trọng:** Chỉ tính phiếu đã được STOCKKEEPER xác nhận thực hiện (EXECUTED)

### Tồn kho dự kiến tại ngày X (Expected Quantity at Date X)
```
Tồn dự kiến (ngày X) =
    Tồn thực tế hiện tại (EXECUTED)
    + SUM(ADJUST_IN có expected_date <= X và status IN ('PENDING', 'APPROVED', 'EXECUTED'))
    - SUM(ADJUST_OUT có expected_date <= X và status IN ('PENDING', 'APPROVED', 'EXECUTED'))
```

**Lưu ý:** Tính theo từng variant (style + size + length)

## 4. Validation khi tạo Request

### 4.1. USER tạo phiếu

| Loại | expected_date | Validate số lượng |
|------|---------------|-------------------|
| IN | Không cần | Không giới hạn |
| OUT | Không cần | Số lượng <= Tồn thực tế (theo variant) |

### 4.2. PURCHASER tạo phiếu

| Loại | expected_date | Validate số lượng |
|------|---------------|-------------------|
| IN | Không cần | Không giới hạn |
| OUT | Không cần | Số lượng <= Tồn thực tế (theo variant) |
| ADJUST_IN | **Bắt buộc >= today** | Không giới hạn |
| ADJUST_OUT | **Bắt buộc >= today** | Số lượng <= Tồn dự kiến tại expected_date (theo variant) |

## 5. Dời ngày expected_date

### 5.1. Điều kiện chung
- Chỉ **người tạo** mới được dời ngày
- Ngày mới phải **>= today**
- Sau khi dời → request_set chuyển về **PENDING**, cần Admin duyệt lại

### 5.2. Dời ADJUST_IN
- Phải dời hết các **ADJUST_OUT phụ thuộc** trước
- ADJUST_OUT phụ thuộc = những ADJUST_OUT có `expected_date >= ngày cũ của ADJUST_IN`
- Nếu còn ADJUST_OUT phụ thuộc → **Báo lỗi**, yêu cầu dời ADJUST_OUT trước

### 5.3. Dời ADJUST_OUT
- Ngày mới phải đảm bảo: **Tồn dự kiến tại ngày mới >= số lượng ADJUST_OUT**
- Nếu không đủ → **Báo lỗi**

## 6. Thứ tự xử lý cùng ngày

Khi cùng ngày có nhiều ADJUST_IN và ADJUST_OUT:
- Sắp xếp theo: `expected_date ASC`, sau đó `created_at ASC`

## 7. Request Set Status Flow

```
Tạo mới → PENDING → APPROVED (Admin duyệt) → EXECUTED (Stockkeeper xác nhận)
                  ↘ REJECTED (Admin từ chối)

REJECTED → PENDING (Submit lại)

APPROVED + Dời ngày → PENDING (Cần duyệt lại)
```

### Chi tiết các trạng thái:

| Status | Mô tả | Ai chuyển |
|--------|-------|-----------|
| **PENDING** | Chờ Admin duyệt | Tự động khi tạo mới |
| **APPROVED** | Đã duyệt, chờ Stockkeeper thực hiện | Admin |
| **REJECTED** | Bị từ chối | Admin |
| **EXECUTED** | Đã thực hiện nhập/xuất kho xong | Stockkeeper |

### Quan trọng về EXECUTED:
- **Tồn thực tế chỉ thay đổi khi status = EXECUTED**
- STOCKKEEPER là người duy nhất có thể chuyển APPROVED → EXECUTED
- Khi EXECUTED, tồn thực tế được cập nhật (IN/OUT có hiệu lực)

## 8. API Endpoints

### Approval Flow APIs

| API | Mô tả | Role |
|-----|-------|------|
| `POST /api/request-sets/{id}/submit` | Submit để chờ duyệt | USER, PURCHASER |
| `POST /api/request-sets/{id}/approve` | Duyệt phiếu | ADMIN |
| `POST /api/request-sets/{id}/reject` | Từ chối phiếu | ADMIN |
| `POST /api/request-sets/{id}/execute` | Xác nhận đã thực hiện | STOCKKEEPER |
| `GET /api/request-sets/approved` | Danh sách chờ thực hiện | STOCKKEEPER |

## 9. Ví dụ minh họa

### Ví dụ 1: Luồng hoàn chỉnh
```
1. USER tạo phiếu IN +10 → status: PENDING
2. ADMIN duyệt → status: APPROVED
   - Tồn thực tế: chưa thay đổi
   - STOCKKEEPER nhận thông báo
3. STOCKKEEPER thực hiện nhập kho thực tế
4. STOCKKEEPER xác nhận → status: EXECUTED
   - Tồn thực tế: +10
```

### Ví dụ 2: Tạo ADJUST_OUT
```
Tồn thực tế (EXECUTED): 5
ADJUST_IN +3 ngày 01/01/2026 (APPROVED)

Tồn dự kiến ngày 01/01/2026 = 5 + 3 = 8
Tồn dự kiến ngày 02/01/2026 = 5 + 3 = 8

→ PURCHASER có thể tạo ADJUST_OUT tối đa 8 vào ngày 01/01 hoặc sau
```

### Ví dụ 3: Dời ngày ADJUST_IN
```
Trạng thái ban đầu:
- Tồn thực tế: 5
- ADJUST_IN +3 ngày 01/01/2026 (APPROVED)
- ADJUST_OUT -6 ngày 01/01/2026 (APPROVED) - dựa vào tồn dự kiến 8

Muốn dời ADJUST_IN từ 01/01 → 05/01:
1. Hệ thống kiểm tra: có ADJUST_OUT ngày 01/01 phụ thuộc
2. Báo lỗi: "Không thể dời. Hãy dời ADJUST_OUT ngày 01/01 trước."
3. PURCHASER phải dời ADJUST_OUT sang ngày >= 05/01 trước
4. Sau đó mới dời được ADJUST_IN
```

---
*Cập nhật: 2026-01-23*
