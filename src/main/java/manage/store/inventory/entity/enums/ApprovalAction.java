package manage.store.inventory.entity.enums;

public enum ApprovalAction {
    SUBMIT,     // Submit để chờ duyệt
    APPROVE,    // Admin duyệt
    REJECT,     // Admin từ chối
    EXECUTE,    // Stockkeeper xác nhận đã thực hiện (full match - Case 1)
    RECEIVE,    // Stockkeeper ghi nhận nhận hàng từng phần (Case 3)
    COMPLETE,   // Stockkeeper hoàn tất nhận hàng từng phần (Case 3)
    EDIT,              // Stockkeeper/chủ phiếu sửa phiếu đã duyệt (Case 2)
    EDIT_AND_RECEIVE   // Stockkeeper sửa số lượng và chuyển RECEIVING luôn (Case 4)
}
