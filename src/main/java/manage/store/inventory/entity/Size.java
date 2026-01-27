package manage.store.inventory.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sizes")
public class Size {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sizeId;

    private Integer sizeValue;

    public Long getSizeId() { return sizeId; }
    public void setSizeId(Long sizeId) { this.sizeId = sizeId; }

    public Integer getSizeValue() { return sizeValue; }
    public void setSizeValue(Integer sizeValue) { this.sizeValue = sizeValue; }
}
