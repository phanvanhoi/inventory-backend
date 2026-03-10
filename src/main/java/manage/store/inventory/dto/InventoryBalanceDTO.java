package manage.store.inventory.dto;

public interface InventoryBalanceDTO {

    Long getVariantId();
    String getStyleName();
    String getSizeValue();
    String getLengthCode();
    String getGender();
    String getItemCode();
    String getItemName();
    String getUnit();
    Integer getActualQuantity();
    Integer getExpectedQuantity();
}
