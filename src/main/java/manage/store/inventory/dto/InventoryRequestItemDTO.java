package manage.store.inventory.dto;

public class InventoryRequestItemDTO {

    private Long variantId;
    private String styleName;
    private String sizeValue;
    private String lengthCode; // COC / DAI
    private String gender;
    private String itemCode;
    private String itemName;
    private String unit;
    private Integer quantity;

    public InventoryRequestItemDTO() {
    }

    // Constructor cho STRUCTURED variants (JPQL)
    public InventoryRequestItemDTO(
            Long variantId,
            String styleName,
            String sizeValue,
            String lengthCode,
            String gender,
            String itemCode,
            String itemName,
            String unit,
            Integer quantity
    ) {
        this.variantId = variantId;
        this.styleName = styleName;
        this.sizeValue = sizeValue;
        this.lengthCode = lengthCode;
        this.gender = gender;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
    }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }

    public String getSizeValue() { return sizeValue; }
    public void setSizeValue(String sizeValue) { this.sizeValue = sizeValue; }

    public String getLengthCode() { return lengthCode; }
    public void setLengthCode(String lengthCode) { this.lengthCode = lengthCode; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
