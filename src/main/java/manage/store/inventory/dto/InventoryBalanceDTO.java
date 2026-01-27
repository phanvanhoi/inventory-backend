package manage.store.inventory.dto;

public interface InventoryBalanceDTO {

    String getStyleName();
    Integer getSizeValue();
    String getLengthCode();
    Integer getActualQuantity();    // Tồn kho thực tế = IN - OUT
    Integer getExpectedQuantity();  // Tồn kho dự kiến = IN - OUT + ADJUST_IN - ADJUST_OUT
}
