# Approval Flow Specification

## Tổng quan

Hệ thống phê duyệt cho Request Sets (bộ phiếu) trong ứng dụng quản lý kho HangFashion.

## 1. Roles (Vai trò)

| Role | Mô tả | Quyền hạn |
|------|-------|-----------|
| `ADMIN` | Quản trị viên | Duyệt/Từ chối bộ phiếu, tạo bộ phiếu, xem tất cả |
| `USER` | Người dùng thông thường | Tạo và submit bộ phiếu, xem bộ phiếu của mình |
| `STOCKKEEPER` | Kiểm kho | Chỉ xem được bộ phiếu đã được duyệt (APPROVED) |

### Users mẫu trong database:
| user_id | username | full_name | roles |
|---------|----------|-----------|-------|
| 1 | thuy | Thúy | ADMIN, USER |
| 2 | nga | Nga | USER |
| 3 | huong | Hương | USER |
| 4 | thuong | Thương | USER |
| 5 | tung | Tùng | STOCKKEEPER |

## 2. Request Set Status (Trạng thái bộ phiếu)

```
DRAFT ──submit──> PENDING ──approve──> APPROVED
                     │
                     └──reject───> REJECTED ──submit──> PENDING
```

| Status | Mô tả | Màu gợi ý |
|--------|-------|-----------|
| `DRAFT` | Nháp - chưa gửi duyệt | Gray |
| `PENDING` | Đang chờ duyệt | Yellow/Orange |
| `APPROVED` | Đã được duyệt | Green |
| `REJECTED` | Bị từ chối | Red |

## 3. API Endpoints

### 3.1 Request Sets

#### Lấy danh sách bộ phiếu
```
GET /api/request-sets
GET /api/request-sets?status=APPROVED
GET /api/request-sets?status=PENDING
GET /api/request-sets?status=DRAFT
GET /api/request-sets?status=REJECTED
```

**Response:**
```json
[
  {
    "setId": 1,
    "setName": "Nhập kho ban đầu",
    "description": null,
    "status": "APPROVED",
    "createdBy": null,
    "createdByName": null,
    "createdAt": "2025-06-20T00:00:00",
    "submittedAt": "2025-06-20T00:00:00",
    "requestCount": 1
  },
  {
    "setId": 2,
    "setName": "ĐX 48 - Thúy",
    "description": null,
    "status": "APPROVED",
    "createdBy": 1,
    "createdByName": "Thúy",
    "createdAt": "2025-07-01T00:00:00",
    "submittedAt": "2025-07-01T00:00:00",
    "requestCount": 2
  }
]
```

#### Lấy chi tiết bộ phiếu
```
GET /api/request-sets/{setId}
```

**Response:**
```json
{
  "setId": 2,
  "setName": "ĐX 48 - Thúy",
  "description": null,
  "status": "APPROVED",
  "createdBy": 1,
  "createdByName": "Thúy",
  "createdAt": "2025-07-01T00:00:00",
  "submittedAt": "2025-07-01T00:00:00",
  "requests": [...],
  "approvalHistory": [
    {
      "historyId": 1,
      "action": "SUBMIT",
      "performedBy": 1,
      "performedByName": "Thúy",
      "reason": null,
      "createdAt": "2025-07-01T08:00:00"
    },
    {
      "historyId": 2,
      "action": "APPROVE",
      "performedBy": 1,
      "performedByName": "Thúy",
      "reason": null,
      "createdAt": "2025-07-01T09:00:00"
    }
  ]
}
```

#### Submit bộ phiếu (gửi duyệt)
```
POST /api/request-sets/{setId}/submit
Content-Type: application/json

{
  "userId": 2
}
```

**Điều kiện:**
- Bộ phiếu phải ở trạng thái `DRAFT` hoặc `REJECTED`
- Bất kỳ user nào cũng có thể submit

**Response:** `200 OK`

**Errors:**
- `400` - Bộ phiếu không ở trạng thái DRAFT hoặc REJECTED

#### Duyệt bộ phiếu
```
POST /api/request-sets/{setId}/approve
Content-Type: application/json

{
  "userId": 1
}
```

**Điều kiện:**
- User phải có role `ADMIN`
- Bộ phiếu phải ở trạng thái `PENDING`
- Người duyệt KHÔNG được là người tạo bộ phiếu

**Response:** `200 OK`

**Errors:**
- `400` - User không phải ADMIN
- `400` - Bộ phiếu không ở trạng thái PENDING
- `400` - Không thể tự duyệt bộ phiếu của chính mình

#### Từ chối bộ phiếu
```
POST /api/request-sets/{setId}/reject
Content-Type: application/json

{
  "userId": 1,
  "reason": "Số lượng không chính xác, cần kiểm tra lại"
}
```

**Điều kiện:**
- User phải có role `ADMIN`
- Bộ phiếu phải ở trạng thái `PENDING`
- **Bắt buộc** phải có `reason` (lý do từ chối)

**Response:** `200 OK`

**Errors:**
- `400` - User không phải ADMIN
- `400` - Bộ phiếu không ở trạng thái PENDING
- `400` - Thiếu lý do từ chối

### 3.2 Notifications (Thông báo)

#### Lấy tất cả thông báo
```
GET /api/notifications?userId=1
```

**Response:**
```json
[
  {
    "notificationId": 1,
    "title": "Bộ phiếu chờ duyệt",
    "message": "Bộ phiếu 'ĐX 100 - Nga' đã được submit bởi Nga và đang chờ duyệt.",
    "isRead": false,
    "relatedSetId": 53,
    "createdAt": "2025-01-20T10:30:00"
  },
  {
    "notificationId": 2,
    "title": "Bộ phiếu đã được duyệt",
    "message": "Bộ phiếu 'ĐX 99 - Nga' đã được duyệt bởi Thúy.",
    "isRead": true,
    "relatedSetId": 52,
    "createdAt": "2025-01-19T15:00:00"
  }
]
```

#### Lấy thông báo chưa đọc
```
GET /api/notifications/unread?userId=1
```

#### Đếm số thông báo chưa đọc
```
GET /api/notifications/unread/count?userId=1
```

**Response:**
```json
5
```

#### Đánh dấu đã đọc
```
POST /api/notifications/{notificationId}/read
```

**Response:** `200 OK`

#### Đánh dấu tất cả đã đọc
```
POST /api/notifications/read-all?userId=1
```

**Response:** `200 OK`

## 4. Business Rules

### 4.1 Submit Flow
1. User tạo bộ phiếu (status = `DRAFT`)
2. User thêm các phiếu xuất/nhập vào bộ
3. User click "Submit" để gửi duyệt
4. Hệ thống:
   - Chuyển status sang `PENDING`
   - Ghi lại lịch sử (action = `SUBMIT`)
   - Gửi notification cho tất cả ADMIN (trừ người submit)

### 4.2 Approve Flow
1. ADMIN nhận notification về bộ phiếu chờ duyệt
2. ADMIN review bộ phiếu
3. ADMIN click "Approve"
4. Hệ thống:
   - Kiểm tra ADMIN không phải người tạo
   - Chuyển status sang `APPROVED`
   - Ghi lại lịch sử (action = `APPROVE`)
   - Gửi notification cho người tạo

### 4.3 Reject Flow
1. ADMIN review bộ phiếu
2. ADMIN click "Reject" và nhập lý do
3. Hệ thống:
   - Chuyển status sang `REJECTED`
   - Ghi lại lịch sử (action = `REJECT`, reason = "...")
   - Gửi notification cho người tạo kèm lý do

### 4.4 Re-submit Flow
1. User nhận notification bộ phiếu bị từ chối
2. User sửa lại bộ phiếu
3. User click "Submit" lại
4. Flow quay lại Submit Flow

### 4.5 Inventory Calculation
- **Quan trọng:** Tồn kho chỉ được tính từ các request_sets có status = `APPROVED`
- Các bộ phiếu DRAFT, PENDING, REJECTED không ảnh hưởng đến tồn kho

### 4.6 STOCKKEEPER Access
- Role STOCKKEEPER chỉ được xem các bộ phiếu có status = `APPROVED`
- Frontend cần filter danh sách khi user có role STOCKKEEPER

## 5. UI Components Suggestions

### 5.1 Status Badge
```jsx
const StatusBadge = ({ status }) => {
  const colors = {
    DRAFT: 'gray',
    PENDING: 'yellow',
    APPROVED: 'green',
    REJECTED: 'red'
  };

  const labels = {
    DRAFT: 'Nháp',
    PENDING: 'Chờ duyệt',
    APPROVED: 'Đã duyệt',
    REJECTED: 'Từ chối'
  };

  return <Badge color={colors[status]}>{labels[status]}</Badge>;
};
```

### 5.2 Action Buttons based on Status & Role
```jsx
const ActionButtons = ({ requestSet, currentUser }) => {
  const { status, createdBy } = requestSet;
  const isAdmin = currentUser.roles.includes('ADMIN');
  const isCreator = createdBy === currentUser.userId;

  return (
    <>
      {/* Submit button - show for DRAFT or REJECTED */}
      {(status === 'DRAFT' || status === 'REJECTED') && (
        <Button onClick={handleSubmit}>Submit</Button>
      )}

      {/* Approve/Reject - show for ADMIN on PENDING, not creator */}
      {status === 'PENDING' && isAdmin && !isCreator && (
        <>
          <Button color="green" onClick={handleApprove}>Duyệt</Button>
          <Button color="red" onClick={openRejectModal}>Từ chối</Button>
        </>
      )}
    </>
  );
};
```

### 5.3 Notification Bell
```jsx
const NotificationBell = ({ userId }) => {
  const [count, setCount] = useState(0);

  useEffect(() => {
    // Poll every 30 seconds
    const interval = setInterval(async () => {
      const res = await fetch(`/api/notifications/unread/count?userId=${userId}`);
      setCount(await res.json());
    }, 30000);

    return () => clearInterval(interval);
  }, [userId]);

  return (
    <Badge count={count}>
      <BellIcon onClick={openNotificationPanel} />
    </Badge>
  );
};
```

### 5.4 Approval History Timeline
```jsx
const ApprovalTimeline = ({ history }) => (
  <Timeline>
    {history.map(item => (
      <Timeline.Item
        key={item.historyId}
        color={item.action === 'APPROVE' ? 'green' : item.action === 'REJECT' ? 'red' : 'blue'}
      >
        <p><strong>{item.performedByName}</strong> - {formatAction(item.action)}</p>
        <p>{formatDate(item.createdAt)}</p>
        {item.reason && <p>Lý do: {item.reason}</p>}
      </Timeline.Item>
    ))}
  </Timeline>
);
```

### 5.5 Reject Modal
```jsx
const RejectModal = ({ visible, onConfirm, onCancel }) => {
  const [reason, setReason] = useState('');

  return (
    <Modal visible={visible} onCancel={onCancel}>
      <h3>Từ chối bộ phiếu</h3>
      <TextArea
        placeholder="Nhập lý do từ chối (bắt buộc)"
        value={reason}
        onChange={e => setReason(e.target.value)}
        required
      />
      <Button
        disabled={!reason.trim()}
        onClick={() => onConfirm(reason)}
      >
        Xác nhận từ chối
      </Button>
    </Modal>
  );
};
```

## 6. Data Models

### RequestSetListDTO
```typescript
interface RequestSetListDTO {
  setId: number;
  setName: string;
  description: string | null;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
  createdBy: number | null;
  createdByName: string | null;
  createdAt: string; // ISO datetime
  submittedAt: string | null;
  requestCount: number;
}
```

### RequestSetDetailDTO
```typescript
interface RequestSetDetailDTO {
  setId: number;
  setName: string;
  description: string | null;
  status: 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED';
  createdBy: number | null;
  createdByName: string | null;
  createdAt: string;
  submittedAt: string | null;
  requests: InventoryRequestDetailDTO[];
  approvalHistory: ApprovalHistoryDTO[];
}
```

### ApprovalHistoryDTO
```typescript
interface ApprovalHistoryDTO {
  historyId: number;
  action: 'SUBMIT' | 'APPROVE' | 'REJECT';
  performedBy: number;
  performedByName: string;
  reason: string | null;
  createdAt: string;
}
```

### NotificationDTO
```typescript
interface NotificationDTO {
  notificationId: number;
  title: string;
  message: string;
  isRead: boolean;
  relatedSetId: number | null;
  createdAt: string;
}
```

### User (extended)
```typescript
interface User {
  userId: number;
  username: string;
  fullName: string;
  email: string | null;
  createdAt: string;
  roles: Role[];
}

interface Role {
  roleId: number;
  roleName: 'ADMIN' | 'USER' | 'STOCKKEEPER';
  description: string;
}
```

## 7. Testing Scenarios

### Scenario 1: Normal Approval Flow
1. Login as `nga` (USER)
2. Create new request set
3. Add inventory requests
4. Submit for approval
5. Login as `thuy` (ADMIN)
6. Check notification
7. View pending request set
8. Approve it
9. Login as `nga`
10. Check notification about approval
11. Verify inventory updated

### Scenario 2: Rejection Flow
1. Login as `huong` (USER)
2. Create and submit request set
3. Login as `thuy` (ADMIN)
4. Reject with reason "Số lượng sai"
5. Login as `huong`
6. Check notification
7. Edit request set
8. Re-submit
9. Login as `thuy`
10. Approve

### Scenario 3: Self-Approval Prevention
1. Login as `thuy` (ADMIN + USER)
2. Create and submit request set
3. Try to approve own request → Should fail

### Scenario 4: STOCKKEEPER Access
1. Login as `tung` (STOCKKEEPER)
2. View request sets → Only see APPROVED
3. View inventory → Calculated from APPROVED only

## 8. Error Messages (Vietnamese)

| Error | Message |
|-------|---------|
| Not ADMIN | "Chỉ ADMIN mới có quyền duyệt bộ phiếu" |
| Not ADMIN (reject) | "Chỉ ADMIN mới có quyền từ chối bộ phiếu" |
| Self-approve | "Không thể tự duyệt bộ phiếu của chính mình" |
| Wrong status (submit) | "Chỉ có thể submit bộ phiếu ở trạng thái DRAFT hoặc REJECTED" |
| Wrong status (approve) | "Chỉ có thể duyệt bộ phiếu đang chờ duyệt (PENDING)" |
| Wrong status (reject) | "Chỉ có thể từ chối bộ phiếu đang chờ duyệt (PENDING)" |
| Missing reason | "Phải có lý do khi từ chối bộ phiếu" |
