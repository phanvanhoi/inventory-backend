package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.OrderHistory;
import manage.store.inventory.entity.enums.OrderHistoryAction;

@Data
public class OrderHistoryDTO {

    private Long historyId;
    private Long orderId;
    private Long changedBy;
    private String changedByName;
    private LocalDateTime changedAt;
    private OrderHistoryAction action;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String reason;

    public static OrderHistoryDTO from(OrderHistory h) {
        if (h == null) return null;
        OrderHistoryDTO dto = new OrderHistoryDTO();
        dto.setHistoryId(h.getHistoryId());
        if (h.getOrder() != null) dto.setOrderId(h.getOrder().getOrderId());
        if (h.getChangedByUser() != null) {
            dto.setChangedBy(h.getChangedByUser().getUserId());
            dto.setChangedByName(h.getChangedByUser().getFullName());
        }
        dto.setChangedAt(h.getChangedAt());
        dto.setAction(h.getAction());
        dto.setFieldName(h.getFieldName());
        dto.setOldValue(h.getOldValue());
        dto.setNewValue(h.getNewValue());
        dto.setReason(h.getReason());
        return dto;
    }
}
