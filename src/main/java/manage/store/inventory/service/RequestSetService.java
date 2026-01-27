package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.dto.RequestSetUpdateDTO;

public interface RequestSetService {

    // Tạo bộ phiếu mới
    Long createRequestSet(RequestSetCreateDTO dto, Long createdByUserId);

    // Lấy danh sách bộ phiếu theo role của user hiện tại
    // - USER/PURCHASER: chỉ xem của mình
    // - ADMIN: xem tất cả, sắp xếp theo tên người tạo
    // - STOCKKEEPER: xem tất cả (giữ nguyên)
    List<RequestSetListDTO> getAllRequestSets(Long userId);

    // Lấy danh sách bộ phiếu theo status (có phân quyền theo role)
    List<RequestSetListDTO> getRequestSetsByStatus(String status, Long userId);

    // Lấy danh sách bộ phiếu theo nhiều status (có phân quyền theo role)
    List<RequestSetListDTO> getRequestSetsByStatuses(List<String> statuses, Long userId);

    // Lấy chi tiết bộ phiếu (bao gồm các phiếu con)
    RequestSetDetailDTO getRequestSetDetail(Long setId);

    // Cập nhật bộ phiếu đã bị từ chối và tự động gửi duyệt lại
    // - Chỉ chủ phiếu mới được cập nhật
    // - Chỉ cập nhật được khi status = REJECTED
    // - Sau khi cập nhật, status tự động chuyển sang PENDING
    void updateRequestSet(Long setId, RequestSetUpdateDTO dto, Long userId);

    // Xóa bộ phiếu
    void deleteRequestSet(Long setId);

    // Xóa tất cả bộ phiếu
    void deleteAllRequestSets();

    // Submit bộ phiếu để chờ duyệt
    void submitForApproval(Long setId, Long userId);

    // Duyệt bộ phiếu (chỉ ADMIN)
    void approve(Long setId, Long userId);

    // Từ chối bộ phiếu (chỉ ADMIN)
    void reject(Long setId, Long userId, String reason);

    // Xác nhận đã thực hiện nhập/xuất kho (chỉ STOCKKEEPER)
    // Chuyển status từ APPROVED → EXECUTED
    void execute(Long setId, Long userId);

    // Lấy danh sách bộ phiếu đã duyệt (chờ STOCKKEEPER thực hiện)
    List<RequestSetListDTO> getApprovedRequestSets();
}
