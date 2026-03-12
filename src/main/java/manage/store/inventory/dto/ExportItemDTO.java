package manage.store.inventory.dto;

import java.math.BigDecimal;

public interface ExportItemDTO {
    Long getRequestId();
    String getUnitName();
    String getPositionCode();
    String getProductName();
    String getStyleName();
    String getSizeValue();
    String getLengthCode();
    String getGender();
    String getItemCode();
    String getItemName();
    String getUnit();
    BigDecimal getQuantity();
}
