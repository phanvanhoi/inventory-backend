package manage.store.inventory.dto;

import java.time.LocalDateTime;

import lombok.Data;
import manage.store.inventory.entity.MissingItem;

@Data
public class MissingItemDTO {
    private Long missingId;
    private Long packingBatchId;
    private Long orderItemId;
    private String productName;
    private Integer missingQuantity;
    private String missingListFileUrl;
    private Boolean resolved;
    private String note;
    private LocalDateTime createdAt;

    public static MissingItemDTO from(MissingItem m) {
        if (m == null) return null;
        MissingItemDTO dto = new MissingItemDTO();
        dto.setMissingId(m.getMissingId());
        if (m.getPackingBatch() != null) dto.setPackingBatchId(m.getPackingBatch().getPackingBatchId());
        if (m.getOrderItem() != null) {
            dto.setOrderItemId(m.getOrderItem().getOrderItemId());
            dto.setProductName(m.getOrderItem().getProductName());
        }
        dto.setMissingQuantity(m.getMissingQuantity());
        dto.setMissingListFileUrl(m.getMissingListFileUrl());
        dto.setResolved(m.getResolved());
        dto.setNote(m.getNote());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
