package manage.store.inventory.dto;

public interface ItemDetailDTO {

    Long getItemId();
    Long getRequestId();
    Long getVariantId();
    String getStyleName();
    String getSizeValue();
    String getLengthCode();
    String getGender();
    String getItemCode();
    String getItemName();
    String getUnit();
    Integer getQuantity();
}
