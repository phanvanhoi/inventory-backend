package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.RequestCompleteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.dto.EditAndReceiveDTO;
import manage.store.inventory.dto.RequestSetUpdateDTO;

public interface RequestSetService {

    // Tạo bộ phiếu mới
    Long createRequestSet(RequestSetCreateDTO dto, Long createdByUserId);

    // Lấy danh sách bộ phiếu theo role của user hiện tại
    // - USER/PURCHASER: chỉ xem của mình
    // - ADMIN: xem tất cả, sắp xếp theo tên người tạo
    // - STOCKKEEPER: xem tất cả (giữ nguyên)
    List<RequestSetListDTO> getAllRequestSets(Long userId);
    Page<RequestSetListDTO> getAllRequestSets(Long userId, Pageable pageable);

    // Lấy danh sách bộ phiếu theo status (có phân quyền theo role)
    List<RequestSetListDTO> getRequestSetsByStatus(String status, Long userId);
    Page<RequestSetListDTO> getRequestSetsByStatus(String status, Long userId, Pageable pageable);

    // Lấy danh sách bộ phiếu theo nhiều status (có phân quyền theo role)
    List<RequestSetListDTO> getRequestSetsByStatuses(List<String> statuses, Long userId);
    Page<RequestSetListDTO> getRequestSetsByStatuses(List<String> statuses, Long userId, Pageable pageable);

    // Lấy chi tiết bộ phiếu (bao gồm các phiếu con)
    RequestSetDetailDTO getRequestSetDetail(Long setId);

    // Cập nhật bộ phiếu đã bị từ chối và tự động gửi duyệt lại
    // - Chỉ chủ phiếu mới được cập nhật
    // - Chỉ cập nhật được khi status = REJECTED
    // - Sau khi cập nhật, status tự động chuyển sang PENDING
    void updateRequestSet(Long setId, RequestSetUpdateDTO dto, Long userId);

    // Xóa bộ phiếu (chủ phiếu hoặc ADMIN)
    void deleteRequestSet(Long setId, Long userId);

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
    // Bao gồm cả APPROVED và RECEIVING
    List<RequestSetListDTO> getApprovedRequestSets();

    // Sửa bộ phiếu đã duyệt (Case 2)
    // - STOCKKEEPER hoặc chủ phiếu
    // - Chỉ khi APPROVED (chưa có receipt)
    // - Status → PENDING (cần Admin duyệt lại)
    void editApprovedRequestSet(Long setId, RequestSetUpdateDTO dto, Long userId);

    // Sửa số lượng items và chuyển RECEIVING luôn (Case 4)
    // - Chỉ STOCKKEEPER
    // - Chỉ khi APPROVED (chưa có receipt)
    // - Chỉ sửa quantity của items đã có
    // - Bắt buộc nêu lý do
    // - Status → RECEIVING (không quay về PENDING)
    // - Thông báo Creator + tất cả ADMIN
    void editAndReceiveRequestSet(Long setId, EditAndReceiveDTO dto, Long userId);

    // Đánh dấu 1 request (kho) đã hoàn thành (multi-warehouse)
    // - Chỉ STOCKKEEPER
    // - Set phải APPROVED hoặc RECEIVING
    // - Kho đầu tiên COMPLETED → set chuyển RECEIVING
    // - Tất cả requests COMPLETED → set chuyển EXECUTED
    RequestCompleteResponseDTO completeRequest(Long requestId, Long userId);
}
