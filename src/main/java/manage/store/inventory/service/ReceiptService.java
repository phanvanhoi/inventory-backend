package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.ReceiptCreateDTO;
import manage.store.inventory.dto.ReceiptDetailDTO;
import manage.store.inventory.dto.SetReceiptProgressDTO;

public interface ReceiptService {

    // Ghi nhận nhận hàng (từng phần hoặc toàn bộ) - STOCKKEEPER
    // APPROVED → RECEIVING (lần đầu) hoặc giữ RECEIVING
    void recordReceipt(Long setId, ReceiptCreateDTO dto, Long userId);

    // Hoàn tất nhận hàng - STOCKKEEPER
    // RECEIVING → EXECUTED
    void completeReceipt(Long setId, Long userId);

    // Lấy danh sách các lần nhận hàng của bộ phiếu
    List<ReceiptDetailDTO> getReceipts(Long setId);

    // Lấy tiến độ nhận hàng (phân cấp: set → requests → items → history)
    SetReceiptProgressDTO getProgress(Long setId);
}
