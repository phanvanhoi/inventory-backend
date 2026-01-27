package manage.store.inventory.dto;

public interface ItemDetailDTO {

    Long getItemId();
    Long getRequestId();
    Long getVariantId();
    String getStyleName();
    Integer getSizeValue();
    String getLengthCode();
    Integer getQuantity();
}
