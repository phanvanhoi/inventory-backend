package manage.store.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBalanceViewDTO {

    private String styleName;
    private Integer sizeValue;
    private String lengthCode;
    private Integer actualQuantity;
    private Integer expectedQuantity; // null nếu user không có quyền xem
}
