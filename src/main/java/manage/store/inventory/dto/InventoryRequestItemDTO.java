package manage.store.inventory.dto;

import java.math.BigDecimal;

public class InventoryRequestItemDTO {

    private Long itemId;
    private Long variantId;
    private String styleName;
    private String sizeValue;
    private String lengthCode; // COC / DAI
    private String gender;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal quantity;

    // Fabric fields
    private String workerNote;
    private String fabricNote;
    private Long employeeId;
    private String garmentQuantity;

    // Employee info (joined from unit_employees + positions)
    private String employeeName;
    private String positionCode;

    public InventoryRequestItemDTO() {
    }

    // Constructor cho JPQL query (backward compatible — Integer quantity auto-converts)
    public InventoryRequestItemDTO(
            Long itemId,
            Long variantId,
            String styleName,
            String sizeValue,
            String lengthCode,
            String gender,
            String itemCode,
            String itemName,
            String unit,
            BigDecimal quantity
    ) {
        this.itemId = itemId;
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

    // Full constructor with fabric fields
    public InventoryRequestItemDTO(
            Long itemId,
            Long variantId,
            String styleName,
            String sizeValue,
            String lengthCode,
            String gender,
            String itemCode,
            String itemName,
            String unit,
            BigDecimal quantity,
            String workerNote,
            String fabricNote,
            Long employeeId,
            String garmentQuantity,
            String employeeName,
            String positionCode
    ) {
        this.itemId = itemId;
        this.variantId = variantId;
        this.styleName = styleName;
        this.sizeValue = sizeValue;
        this.lengthCode = lengthCode;
        this.gender = gender;
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.unit = unit;
        this.quantity = quantity;
        this.workerNote = workerNote;
        this.fabricNote = fabricNote;
        this.employeeId = employeeId;
        this.garmentQuantity = garmentQuantity;
        this.employeeName = employeeName;
        this.positionCode = positionCode;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

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

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getWorkerNote() { return workerNote; }
    public void setWorkerNote(String workerNote) { this.workerNote = workerNote; }

    public String getFabricNote() { return fabricNote; }
    public void setFabricNote(String fabricNote) { this.fabricNote = fabricNote; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getGarmentQuantity() { return garmentQuantity; }
    public void setGarmentQuantity(String garmentQuantity) { this.garmentQuantity = garmentQuantity; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getPositionCode() { return positionCode; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }
}
