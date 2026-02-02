package manage.store.inventory.dto;

import java.time.LocalDate;
import java.util.List;

public class InventoryRequestCreateDTO {

    private Long unitId;
    private String positionCode; // Chức danh: GDV, VHX, ... (optional)
    private Long productId;
    private String requestType; // IN, OUT, ADJUST_IN, ADJUST_OUT (tùy role)
    private LocalDate expectedDate; // Bắt buộc cho ADJUST_IN, ADJUST_OUT
    private String note;
    private List<ItemDTO> items;

    public InventoryRequestCreateDTO() {
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getPositionCode() {
        return positionCode;
    }

    public void setPositionCode(String positionCode) {
        this.positionCode = positionCode;
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

        private Long styleId;
        private Integer sizeValue;
        private String lengthCode; // COC / DAI
        private Integer quantity;

        public ItemDTO() {
        }

        public Long getStyleId() {
            return styleId;
        }

        public void setStyleId(Long styleId) {
            this.styleId = styleId;
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
}
