package manage.store.inventory.dto;

public class InventoryRequestItemDTO {

    private Long variantId;
    private String styleName;
    private Integer sizeValue;
    private String lengthCode; // COC / DAI
    private Integer quantity;

    public InventoryRequestItemDTO() {
    }

    public InventoryRequestItemDTO(
            Long variantId,
            String styleName,
            Integer sizeValue,
            String lengthCode,
            Integer quantity
    ) {
        this.variantId = variantId;
        this.styleName = styleName;
        this.sizeValue = sizeValue;
        this.lengthCode = lengthCode;
        this.quantity = quantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public Integer getSizeValue() {
        return sizeValue;
    }

    public void setSizeValue(Integer sizeValue) {
        this.sizeValue = sizeValue;
    }

    public String getLengthCode() {
        return lengthCode;
    }

    public void setLengthCode(String lengthCode) {
        this.lengthCode = lengthCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
