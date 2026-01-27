# TECHNICAL SPECIFICATION
# AI WAREHOUSE ASSISTANT
## Giai đoạn 1 – Read-Only AI System

---

## 1. Mục tiêu hệ thống

Xây dựng **AI Assistant nội bộ** cho hệ thống quản lý kho, cho phép người dùng:
- Truy vấn dữ liệu tồn kho bằng **ngôn ngữ tự nhiên**
- Phân tích và giải thích dữ liệu tồn kho
- **KHÔNG** cho phép AI ghi hoặc thay đổi dữ liệu

### Nguyên tắc thiết kế

> **AI là một subsystem không đáng tin → phải bị kiểm soát, giới hạn và kiểm chứng**

---

## 2. Phạm vi (Scope)

### 2.1 In Scope – AI ĐƯỢC PHÉP

| Chức năng | Mô tả |
|-----------|-------|
| Truy vấn tồn kho | Theo đơn vị (unit), biến thể sản phẩm (style, size, length) |
| Phân tích dữ liệu | Tồn kho âm, biến động tồn kho |
| Dịch câu hỏi | Tiếng Việt → truy vấn dữ liệu có kiểm soát |
| Giải thích | Trả lời kèm nguồn dữ liệu (SQL result / view) |

### 2.2 Out of Scope – AI TUYỆT ĐỐI KHÔNG ĐƯỢC

| Hành vi bị cấm | Lý do |
|----------------|-------|
| Tạo / sửa / xóa `inventory_requests` | Thay đổi dữ liệu |
| Tạo / sửa / xóa `inventory_request_items` | Thay đổi dữ liệu |
| Điều chỉnh tồn kho | Thay đổi dữ liệu |
| Tự sinh hoặc tự tính số liệu | Rủi ro bịa dữ liệu |
| Quyết định quyền truy cập | Vượt quyền |
| Gọi trực tiếp database | SQL Injection |
| Trả lời khi không đủ dữ liệu | Suy đoán sai |

---

## 3. Use Cases chính (Giai đoạn 1)

### UC-01: Truy vấn tồn kho theo biến thể

**Input:**
```
"Kho Hà Nội còn bao nhiêu áo Slim size 40 dài?"
```

**Output:**
```json
{
  "answer": "Kho Hà Nội hiện còn 25 áo Slim size 40 dài",
  "data": {
    "unit_name": "Hà Nội",
    "style": "Slim",
    "size": 40,
    "length": "DAI",
    "balance": 25
  },
  "source": "inventory_balance",
  "query_time": "2026-01-19T10:30:00"
}
```

### UC-02: Phát hiện tồn kho âm

**Input:**
```
"Những biến thể nào đang âm kho ở đơn vị của tôi?"
```

**Output:**
```json
{
  "answer": "Có 3 biến thể đang âm kho tại đơn vị của bạn",
  "data": [
    { "variant": "Classic size 39 dài", "balance": -5 },
    { "variant": "Slim size 40 cộc", "balance": -2 },
    { "variant": "Slim Short size 41 dài", "balance": -1 }
  ],
  "source": "inventory_balance WHERE balance < 0",
  "unit_id": 1
}
```

### UC-03: Giải thích tồn kho

**Input:**
```
"Vì sao variant Classic size 39 dài bị âm kho?"
```

**Logic xử lý:**
1. Truy vấn `inventory_requests` + `inventory_request_items`
2. Tính toán: `SUM(IN) - SUM(OUT) + SUM(ADJUST)`
3. Liệt kê các giao dịch liên quan

**Output:**
```json
{
  "answer": "Variant Classic size 39 dài bị âm kho (-5) do xuất nhiều hơn nhập",
  "explanation": {
    "total_in": 10,
    "total_out": 15,
    "total_adjust": 0,
    "balance": -5
  },
  "transactions": [
    { "type": "IN", "quantity": 10, "date": "2026-01-10", "note": "Nhập hàng đợt 1" },
    { "type": "OUT", "quantity": 15, "date": "2026-01-15", "note": "Xuất cho khách A" }
  ],
  "source": "inventory_requests + inventory_request_items"
}
```

> **Lưu ý:** AI giải thích dựa trên dữ liệu, không đổ lỗi, không suy đoán ngoài dữ liệu

### UC-04: So sánh tồn kho giữa các đơn vị

**Input:**
```
"So sánh tồn kho giữa các đơn vị"
```

**Ràng buộc:**
- Chỉ trả về các đơn vị mà user có quyền xem
- `unit_id` lấy từ JWT / session

---

## 4. Rủi ro & Nguyên tắc kiểm soát

### R4.1 – AI bịa dữ liệu (Hallucination)

| Rủi ro | Giải pháp |
|--------|-----------|
| AI tự tính toán sai | AI chỉ dùng SQL result được backend cung cấp |
| AI ước lượng | Không cho AI "ước lượng", chỉ dùng dữ liệu thực |
| AI suy đoán | Bắt buộc trả lời "Không đủ dữ liệu" khi thiếu thông tin |

### R4.2 – Truy cập vượt quyền

| Rủi ro | Giải pháp |
|--------|-----------|
| AI truy cập unit khác | `unit_id` lấy từ JWT / session, không từ user input |
| AI tự quyết định quyền | AI không được quyết định quyền, backend kiểm soát |

### R4.3 – SQL Injection / Query nguy hiểm

| Rủi ro | Giải pháp |
|--------|-----------|
| AI sinh SQL trực tiếp | AI không sinh SQL, chỉ gọi predefined queries |
| Malicious input | Backend dùng query builder với parameterized queries |

---

## 5. Kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────────┐
│                         React UI                                │
│                    (Chat Interface)                             │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP POST /api/ai/query
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    AI Controller                                │
│                   (Spring Boot)                                 │
│  - Validate JWT/Session                                         │
│  - Extract unit_id from context                                 │
│  - Rate limiting                                                │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                Intent Classification                            │
│  - QUERY_BALANCE: Truy vấn tồn kho                             │
│  - QUERY_NEGATIVE: Tìm tồn kho âm                              │
│  - EXPLAIN_BALANCE: Giải thích tồn kho                         │
│  - COMPARE_UNITS: So sánh đơn vị                               │
│  - UNKNOWN: Không xác định                                      │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│               Query Mapping Layer                               │
│  - Map intent → predefined query                               │
│  - Extract parameters (style, size, length)                    │
│  - Inject unit_id from session (NOT from user)                 │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              Inventory Read Service                             │
│  - Execute predefined queries only                             │
│  - Return structured JSON                                       │
│  - NO write operations                                          │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Database                                   │
│  - inventory_balance (VIEW - read only)                        │
│  - inventory_requests (read only for AI)                       │
│  - inventory_request_items (read only for AI)                  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│               Structured Result (JSON)                          │
│  - Data từ database                                            │
│  - Metadata (query_time, source, unit_id)                      │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              LLM (Explanation Layer)                            │
│  - Nhận: Schema context + Data context                         │
│  - Sinh: Natural language explanation                          │
│  - KHÔNG: Truy cập DB, quyết định logic                        │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Response                                     │
│  - answer: Câu trả lời ngôn ngữ tự nhiên                       │
│  - data: Dữ liệu thực tế                                       │
│  - source: Nguồn dữ liệu                                       │
│  - query_time: Thời gian truy vấn                              │
└─────────────────────────────────────────────────────────────────┘
```

### Nguyên tắc kiến trúc

| Nguyên tắc | Mô tả |
|------------|-------|
| LLM không chạm DB | LLM chỉ nhận dữ liệu đã được backend xử lý |
| LLM không quyết định logic | Logic nằm ở Query Mapping Layer |
| LLM chỉ giải thích | Chuyển đổi structured data → natural language |

---

## 6. RAG Strategy

### 6.1 Schema Context (Static RAG)

Cung cấp cho AI khi khởi tạo conversation:

```yaml
schema_context:
  tables:
    - name: inventory_balance
      description: "View tính tồn kho theo đơn vị và biến thể"
      columns:
        - unit_id: "ID đơn vị"
        - variant_id: "ID biến thể sản phẩm"
        - balance: "Số lượng tồn kho (có thể âm)"

    - name: inventory_requests
      description: "Phiếu nhập/xuất/điều chỉnh kho"
      columns:
        - request_id: "ID phiếu"
        - unit_id: "ID đơn vị"
        - request_type: "IN (nhập) | OUT (xuất) | ADJUST (điều chỉnh)"
        - created_at: "Thời gian tạo"

    - name: inventory_request_items
      description: "Chi tiết từng dòng trong phiếu"
      columns:
        - variant_id: "ID biến thể"
        - quantity: "Số lượng (luôn dương)"

  business_rules:
    - "Tồn kho = SUM(IN) - SUM(OUT) + SUM(ADJUST)"
    - "Tồn kho có thể âm (cho phép backorder)"
    - "Mỗi đơn vị có tồn kho riêng biệt"

  variant_structure:
    - style: ["Classic", "Classic Short", "Slim", "Slim Short"]
    - size: [35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45]
    - length: ["COC (cộc)", "DAI (dài)"]
```

### 6.2 Data Context (Runtime RAG)

Cung cấp cho AI mỗi lần truy vấn:

```json
{
  "query_result": [
    { "variant": "Slim size 40 dài", "balance": 25 }
  ],
  "query_time": "2026-01-19T10:30:00",
  "unit_id": 1,
  "unit_name": "Kho Hà Nội",
  "query_source": "inventory_balance",
  "user_question": "Còn bao nhiêu áo Slim size 40 dài?"
}
```

> **Nguyên tắc:** AI chỉ được trả lời dựa trên context này, không được suy đoán

---

## 7. Quy tắc trả lời (Answer Policy)

### 7.1 AI PHẢI trả lời "Không đủ dữ liệu" khi:

| Tình huống | Ví dụ |
|------------|-------|
| Câu hỏi mơ hồ | "Còn bao nhiêu hàng?" (thiếu unit, variant) |
| Dữ liệu không tồn tại | Variant không có trong hệ thống |
| Không có quyền truy cập | User hỏi về unit không được phép |
| Câu hỏi ngoài phạm vi | "Dự báo doanh thu tháng sau" |

### 7.2 AI PHẢI:

- Nêu rõ giới hạn: *"Tôi chỉ có thể truy vấn dữ liệu tồn kho"*
- Không suy đoán: *"Dựa trên dữ liệu hiện có..."*
- Gợi ý câu hỏi cụ thể hơn nếu câu hỏi mơ hồ

### 7.3 Template trả lời

**Khi có đủ dữ liệu:**
```
[Câu trả lời tự nhiên]

📊 Dữ liệu:
- [Chi tiết dữ liệu]

📍 Nguồn: [tên bảng/view]
⏰ Thời gian: [query_time]
```

**Khi không đủ dữ liệu:**
```
Tôi không thể trả lời câu hỏi này vì [lý do].

💡 Bạn có thể hỏi cụ thể hơn, ví dụ:
- "Kho [tên đơn vị] còn bao nhiêu áo [style] size [số] [dài/cộc]?"
```

---

## 8. Audit & Logging (Bắt buộc)

### 8.1 Cấu trúc log

```json
{
  "log_id": "uuid",
  "timestamp": "2026-01-19T10:30:00",
  "user_id": 123,
  "unit_id": 1,
  "session_id": "abc123",

  "request": {
    "user_question": "Còn bao nhiêu áo Slim size 40 dài?",
    "detected_intent": "QUERY_BALANCE",
    "extracted_params": {
      "style": "Slim",
      "size": 40,
      "length": "DAI"
    }
  },

  "execution": {
    "query_used": "findBalanceByVariant",
    "query_params": { "unit_id": 1, "variant_id": 45 },
    "execution_time_ms": 23,
    "rows_returned": 1
  },

  "response": {
    "answer": "Kho Hà Nội hiện còn 25 áo Slim size 40 dài",
    "data_returned": true,
    "llm_model": "gpt-4",
    "llm_tokens_used": 150
  }
}
```

### 8.2 Mục đích logging

| Mục đích | Mô tả |
|----------|-------|
| Debug | Xác định lỗi khi AI trả lời sai |
| Compliance | Audit trail cho security |
| Analytics | Phân tích câu hỏi phổ biến |
| Improvement | Cải thiện intent classification |

---

## 9. API Specification

### 9.1 Query Endpoint

```
POST /api/ai/query
Authorization: Bearer <jwt_token>
Content-Type: application/json
```

**Request:**
```json
{
  "question": "Kho Hà Nội còn bao nhiêu áo Slim size 40 dài?",
  "conversation_id": "optional-for-context"
}
```

**Response (Success):**
```json
{
  "success": true,
  "answer": "Kho Hà Nội hiện còn 25 áo Slim size 40 dài",
  "data": {
    "unit_name": "Hà Nội",
    "style": "Slim",
    "size": 40,
    "length": "DAI",
    "balance": 25
  },
  "metadata": {
    "source": "inventory_balance",
    "query_time": "2026-01-19T10:30:00",
    "intent": "QUERY_BALANCE"
  }
}
```

**Response (Insufficient Data):**
```json
{
  "success": true,
  "answer": "Tôi không thể trả lời câu hỏi này vì thiếu thông tin về đơn vị kho.",
  "data": null,
  "metadata": {
    "intent": "UNKNOWN",
    "reason": "MISSING_UNIT"
  },
  "suggestions": [
    "Kho Hà Nội còn bao nhiêu áo Slim size 40 dài?",
    "Tồn kho tại đơn vị của tôi?"
  ]
}
```

### 9.2 Intent Types

| Intent | Mô tả | Required Params |
|--------|-------|-----------------|
| `QUERY_BALANCE` | Truy vấn tồn kho | unit (optional), variant (optional) |
| `QUERY_NEGATIVE` | Tìm tồn kho âm | unit (optional) |
| `EXPLAIN_BALANCE` | Giải thích tồn kho | unit, variant |
| `COMPARE_UNITS` | So sánh đơn vị | - |
| `UNKNOWN` | Không xác định | - |

---

## 10. Tiêu chí hoàn thành Giai đoạn 1

### 10.1 Functional Requirements

| # | Tiêu chí | Verification |
|---|----------|--------------|
| F1 | AI trả lời đúng dữ liệu từ database | Unit test với mock data |
| F2 | AI biết nói "không biết" khi thiếu dữ liệu | Test cases cho edge cases |
| F3 | AI không truy cập unit không có quyền | Security test |
| F4 | 4 use cases chính hoạt động | Integration test |

### 10.2 Non-Functional Requirements

| # | Tiêu chí | Target |
|---|----------|--------|
| NF1 | Response time | < 3 giây |
| NF2 | Uptime | 99% |
| NF3 | Concurrent users | 10 users |

### 10.3 Security Requirements

| # | Tiêu chí | Verification |
|---|----------|--------------|
| S1 | Không có đường nào để AI ghi DB | Code review + penetration test |
| S2 | unit_id từ JWT, không từ user input | Code review |
| S3 | Tất cả queries đều parameterized | Code review |
| S4 | Audit log đầy đủ | Log verification |

### 10.4 Demo Checklist

- [ ] Demo với dữ liệu thật
- [ ] Demo 4 use cases chính
- [ ] Demo trường hợp AI từ chối trả lời
- [ ] Demo audit log
- [ ] Giải thích được vì sao thiết kế như vậy

---

## 11. Implementation Roadmap

### Phase 1.1 - Foundation (Week 1-2)

- [ ] Setup AI Controller endpoint
- [ ] Implement Intent Classification (rule-based)
- [ ] Create predefined queries
- [ ] Setup logging infrastructure

### Phase 1.2 - Core Features (Week 3-4)

- [ ] Implement UC-01: Query Balance
- [ ] Implement UC-02: Negative Balance
- [ ] Implement UC-03: Explain Balance
- [ ] Implement UC-04: Compare Units

### Phase 1.3 - LLM Integration (Week 5-6)

- [ ] Setup LLM connection (OpenAI/Claude)
- [ ] Implement Schema Context (Static RAG)
- [ ] Implement Data Context (Runtime RAG)
- [ ] Answer generation với natural language

### Phase 1.4 - Testing & Polish (Week 7-8)

- [ ] Unit tests
- [ ] Integration tests
- [ ] Security tests
- [ ] Performance tuning
- [ ] Documentation

---

## 12. Định vị dự án (Portfolio)

**Tên dự án:**
> AI-powered Inventory Analysis System
> (Read-only, Guarded LLM Architecture)

**Vai trò:**
> Software Engineer – AI Systems / LLM Applications

**Highlights:**
- Thiết kế kiến trúc LLM an toàn với nguyên tắc "zero-trust AI"
- Implement RAG strategy cho domain-specific knowledge
- Xây dựng hệ thống audit logging cho AI compliance

---

## Appendix A: Predefined Queries

```java
// Query 1: Get balance by variant
@Query("""
    SELECT ib.balance, u.unit_name, s.style_name, sz.size_value, lt.length_code
    FROM inventory_balance ib
    JOIN units u ON u.unit_id = ib.unit_id
    JOIN product_variants pv ON pv.variant_id = ib.variant_id
    JOIN styles s ON s.style_id = pv.style_id
    JOIN sizes sz ON sz.size_id = pv.size_id
    JOIN length_types lt ON lt.length_id = pv.length_id
    WHERE ib.unit_id = :unitId
    AND (:styleId IS NULL OR pv.style_id = :styleId)
    AND (:sizeValue IS NULL OR sz.size_value = :sizeValue)
    AND (:lengthCode IS NULL OR lt.length_code = :lengthCode)
""")
List<BalanceDTO> findBalance(@Param("unitId") Long unitId, ...);

// Query 2: Get negative balances
@Query("""
    SELECT ...
    FROM inventory_balance ib
    WHERE ib.unit_id = :unitId AND ib.balance < 0
""")
List<BalanceDTO> findNegativeBalance(@Param("unitId") Long unitId);

// Query 3: Get transactions for explanation
@Query("""
    SELECT ir.request_type, iri.quantity, ir.created_at, ir.note
    FROM inventory_requests ir
    JOIN inventory_request_items iri ON iri.request_id = ir.request_id
    WHERE ir.unit_id = :unitId AND iri.variant_id = :variantId
    ORDER BY ir.created_at
""")
List<TransactionDTO> findTransactions(@Param("unitId") Long unitId, @Param("variantId") Long variantId);
```

---

## Appendix B: LLM System Prompt

```
You are an AI assistant for an inventory management system.

STRICT RULES:
1. You can ONLY answer based on the data provided in the context
2. You CANNOT make calculations or estimates
3. You CANNOT access data outside the provided context
4. If data is insufficient, say "Không đủ dữ liệu để trả lời"
5. Always cite the data source

CONTEXT:
- Schema: [schema_context]
- Data: [data_context]
- User's unit_id: [unit_id]

USER QUESTION: [question]

Respond in Vietnamese. Be concise and precise.
```

---

*Document Version: 1.0*
*Last Updated: 2026-01-19*
*Author: HangFashion Development Team*
