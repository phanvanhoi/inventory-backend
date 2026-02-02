package manage.store.inventory.dto;

import java.time.LocalDateTime;

public interface InventoryRequestHistoryDTO {

    Long getRequestId();
    Long getSetId();
    String getSetName();
    String getSetStatus();
    String getUnitName();
    String getRequestType();
    Integer getSizeValue();
    String getLengthCode();
    Integer getQuantity();
    String getNote();
    LocalDateTime getCreatedAt();
    Long getCreatedBy();         // ID người tạo request set
    String getCreatedByName();   // Tên người tạo request set
}
