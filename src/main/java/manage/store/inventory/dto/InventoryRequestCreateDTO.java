package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class InventoryRequestCreateDTO {

    private Long unitId;
    private Long positionId; // Chức danh ID (optional)
    private Long productId;
    private Long warehouseId; // Kho (null = default warehouse)
    private String requestType; // IN, OUT, ADJUST_IN, ADJUST_OUT (tùy role)
    private LocalDate expectedDate; // Bắt buộc cho ADJUST_IN, ADJUST_OUT
    private String note;
    private String fabricMetadata; // JSON state cho fabric templates
    private List<ItemDTO> items;

    public InventoryRequestCreateDTO() {
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFabricMetadata() {
        return fabricMetadata;
    }

    public void setFabricMetadata(String fabricMetadata) {
        this.fabricMetadata = fabricMetadata;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public List<ItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemDTO> items) {
        this.items = items;
    }

    /* =====================================================
       INNER DTO — MỖI DÒNG = 1 SKU (CHỈ GỬI KHI quantity > 0)
       ===================================================== */
    public static class ItemDTO {

        // STRUCTURED variant lookup
        private Long styleId;
        private String sizeValue;
        private String lengthCode; // COC / DAI
        private String gender;     // NAM / NU

        // ITEM_BASED variant lookup
        private Long variantId;    // direct variant ID

        private BigDecimal quantity;

        // Fabric fields
        private String workerNote;       // Tên thợ (Mẫu 1)
        private String fabricNote;       // Ghi chú cây vải
        private Long employeeId;         // FK → unit_employees (Mẫu 2)
        private String garmentQuantity;  // "1D", "1Q" (Mẫu 2)

        public ItemDTO() {
        }

        public Long getStyleId() { return styleId; }
        public void setStyleId(Long styleId) { this.styleId = styleId; }

        public String getSizeValue() { return sizeValue; }
        public void setSizeValue(String sizeValue) { this.sizeValue = sizeValue; }

        public String getLengthCode() { return lengthCode; }
        public void setLengthCode(String lengthCode) { this.lengthCode = lengthCode; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }

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
    }
}
