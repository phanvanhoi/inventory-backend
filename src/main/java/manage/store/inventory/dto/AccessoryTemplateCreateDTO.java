package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public class AccessoryTemplateCreateDTO {

    private String name;
    private List<ItemDTO> items;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }

    public static class ItemDTO {
        private Long variantId;
        private String itemCode;
        private String itemName;
        private BigDecimal rate;
        private String unit;

        public Long getVariantId() { return variantId; }
        public void setVariantId(Long variantId) { this.variantId = variantId; }

        public String getItemCode() { return itemCode; }
        public void setItemCode(String itemCode) { this.itemCode = itemCode; }

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }
}
