package manage.store.inventory.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBalanceViewDTO {

    private Long variantId;
    private String styleName;
    private String sizeValue;
    private String lengthCode;
    private String gender;
    private String itemCode;
    private String itemName;
    private String unit;
    private BigDecimal actualQuantity;
    private BigDecimal expectedQuantity; // null nếu user không có quyền xem
}
