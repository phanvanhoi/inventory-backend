package manage.store.inventory.dto;

import java.math.BigDecimal;

public interface InventoryBalanceDTO {

    Long getVariantId();
    String getStyleName();
    String getSizeValue();
    String getLengthCode();
    String getGender();
    String getItemCode();
    String getItemName();
    String getUnit();
    BigDecimal getActualQuantity();
    BigDecimal getExpectedQuantity();
}
