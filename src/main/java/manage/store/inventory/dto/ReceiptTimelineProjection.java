package manage.store.inventory.dto;

import java.time.LocalDateTime;

/**
 * Projection cho dòng thời gian nhận hàng (tổng hợp mỗi lần nhận).
 * Dùng trong native query.
 */
public interface ReceiptTimelineProjection {
    Long getReceiptId();
    LocalDateTime getReceivedAt();
    String getReceivedByName();
    String getNote();
    Integer getTotalItems();
    java.math.BigDecimal getTotalQuantity();
}
