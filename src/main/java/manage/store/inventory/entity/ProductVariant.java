package manage.store.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long variantId;

    private Long styleId;
    private Long sizeId;
    private Long lengthTypeId;

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public Long getStyleId() { return styleId; }
    public void setStyleId(Long styleId) { this.styleId = styleId; }

    public Long getSizeId() { return sizeId; }
    public void setSizeId(Long sizeId) { this.sizeId = sizeId; }

    public Long getLengthTypeId() { return lengthTypeId; }
    public void setLengthTypeId(Long lengthTypeId) { this.lengthTypeId = lengthTypeId; }
}
