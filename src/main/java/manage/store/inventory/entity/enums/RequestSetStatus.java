package manage.store.inventory.entity.enums;

public enum RequestSetStatus {
    DRAFT,      // Nháp (chưa submit)
    PENDING,    // Chờ duyệt
    APPROVED,   // Đã duyệt (chờ STOCKKEEPER thực hiện)
    REJECTED,   // Từ chối
    EXECUTED    // Đã thực hiện (STOCKKEEPER xác nhận nhập/xuất kho xong)
}
