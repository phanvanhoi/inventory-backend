package manage.store.inventory.dto;

import lombok.Data;

@Data
public class ItemCreateDTO {

    private Long requestId;
    private Long styleId;
    private Integer sizeValue;
    private String lengthCode;
    private Integer quantity;
}
