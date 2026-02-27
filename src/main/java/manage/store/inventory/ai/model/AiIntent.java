package manage.store.inventory.ai.model;

public enum AiIntent {
    QUERY_BALANCE,      // UC-01: Truy vấn tồn kho
    QUERY_NEGATIVE,     // UC-02: Tìm tồn kho âm
    EXPLAIN_BALANCE,    // UC-03: Giải thích tồn kho
    COMPARE_UNITS,      // UC-04: So sánh đơn vị
    UNKNOWN             // Không xác định
}
