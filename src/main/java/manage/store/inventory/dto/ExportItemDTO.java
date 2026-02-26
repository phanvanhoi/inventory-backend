package manage.store.inventory.dto;

public interface ExportItemDTO {
    Long getRequestId();
    String getUnitName();
    String getPositionCode();
    String getProductName();
    String getStyleName();
    Integer getSizeValue();
    String getLengthCode();
    Integer getQuantity();
}
