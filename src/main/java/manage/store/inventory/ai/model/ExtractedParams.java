package manage.store.inventory.ai.model;

import lombok.Data;

@Data
public class ExtractedParams {
    private String unitName;
    private Long unitId;
    private String styleName;
    private Integer sizeValue;
    private String lengthCode;
    private Long productId;
    private Long variantId;
}
