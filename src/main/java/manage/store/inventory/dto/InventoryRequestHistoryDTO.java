package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface InventoryRequestHistoryDTO {

    Long getRequestId();
    Long getSetId();
    String getSetName();
    String getSetStatus();
    String getUnitName();
    String getRequestType();
    Long getVariantId();
    String getStyleName();
    String getSizeValue();
    String getLengthCode();
    String getGender();
    String getItemCode();
    String getItemName();
    String getUnit();
    BigDecimal getQuantity();
    String getNote();
    LocalDateTime getCreatedAt();
    Long getCreatedBy();
    String getCreatedByName();
}
