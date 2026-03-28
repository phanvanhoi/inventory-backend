package manage.store.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_request_items")
public class InventoryRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    private Long requestId;
    private Long variantId;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "worker_note")
    private String workerNote;

    @Column(name = "fabric_note")
    private String fabricNote;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "garment_quantity")
    private String garmentQuantity;

    @Column(precision = 10, scale = 4)
    private BigDecimal rate;

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getVariantId() { return variantId; }
    public void setVariantId(Long variantId) { this.variantId = variantId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getWorkerNote() { return workerNote; }
    public void setWorkerNote(String workerNote) { this.workerNote = workerNote; }

    public String getFabricNote() { return fabricNote; }
    public void setFabricNote(String fabricNote) { this.fabricNote = fabricNote; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getGarmentQuantity() { return garmentQuantity; }
    public void setGarmentQuantity(String garmentQuantity) { this.garmentQuantity = garmentQuantity; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
}
