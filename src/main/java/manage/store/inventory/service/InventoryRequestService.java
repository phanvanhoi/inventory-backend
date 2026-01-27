package manage.store.inventory.service;

import manage.store.inventory.dto.InventoryRequestCreateDTO;
import manage.store.inventory.dto.InventoryRequestDetailDTO;
import manage.store.inventory.dto.InventoryRequestItemDTO;
import manage.store.inventory.dto.InventoryRequestListDTO;
import manage.store.inventory.dto.UpdateExpectedDateDTO;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRequestService {

    // ===== TẠO REQUEST =====
    Long createRequest(InventoryRequestCreateDTO dto);

    // ===== LẤY ITEMS (DẠNG LIST) =====
    List<InventoryRequestItemDTO> getRequestItems(Long requestId);

    // ===== LẤY HEADER + ITEMS (CHO UI) =====
    InventoryRequestDetailDTO getRequestDetail(Long requestId);

    // ===== LẤY DANH SÁCH TẤT CẢ REQUESTS =====
    List<InventoryRequestListDTO> getAllRequests();

    // ===== XÓA REQUEST =====
    void deleteRequest(Long requestId);

    // ===== XÓA TẤT CẢ REQUESTS =====
    void deleteAllRequests();

    // ===== CẬP NHẬT REQUEST TYPE =====
    // ADJUST_IN -> IN, ADJUST_OUT -> OUT
    void updateRequestType(Long requestId, String newRequestType);

    // ===== CẬP NHẬT NGÀY DỰ KIẾN =====
    // Chỉ áp dụng cho ADJUST_IN và ADJUST_OUT
    // Quy tắc: Muốn dời ADJUST_IN thì phải dời hết ADJUST_OUT phụ thuộc trước
    void updateExpectedDate(Long requestId, LocalDate newExpectedDate, Long userId);

    // ===== KIỂM TRA CÓ ADJUST_OUT PHỤ THUỘC =====
    // Trả về số lượng ADJUST_OUT có expected_date >= ngày của ADJUST_IN
    int countDependentAdjustOut(Long requestId);
}
