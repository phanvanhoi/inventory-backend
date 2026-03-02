package manage.store.inventory.dto;

import java.time.LocalDateTime;

/**
 * Projection cho lịch sử nhận hàng của 1 variant cụ thể.
 * Dùng trong native query.
 */
public interface ReceiptEntryProjection {
    Long getReceiptId();
    LocalDateTime getReceivedAt();
    String getReceivedByName();
    Integer getReceivedQuantity();
}
