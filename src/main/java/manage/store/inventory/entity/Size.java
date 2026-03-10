package manage.store.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sizes")
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sizeId;

    private String sizeValue;

    private Integer sizeOrder;

    public Long getSizeId() { return sizeId; }
    public void setSizeId(Long sizeId) { this.sizeId = sizeId; }

    public String getSizeValue() { return sizeValue; }
    public void setSizeValue(String sizeValue) { this.sizeValue = sizeValue; }

    public Integer getSizeOrder() { return sizeOrder; }
    public void setSizeOrder(Integer sizeOrder) { this.sizeOrder = sizeOrder; }
}
