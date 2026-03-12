package manage.store.inventory.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemCreateDTO {

    private Long requestId;
    // STRUCTURED variant lookup
    private Long styleId;
    private String sizeValue;
    private String lengthCode;
    private String gender;
    // ITEM_BASED variant lookup
    private Long variantId;

    private BigDecimal quantity;
}
