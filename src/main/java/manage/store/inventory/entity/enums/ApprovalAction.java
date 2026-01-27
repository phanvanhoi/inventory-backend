package manage.store.inventory.entity.enums;

public enum ApprovalAction {
    SUBMIT,     // Submit để chờ duyệt
    APPROVE,    // Admin duyệt
    REJECT,     // Admin từ chối
    EXECUTE     // Stockkeeper xác nhận đã thực hiện
}
