package manage.store.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_requests")
public class InventoryRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestId;

    private Long unitId;

    @Column(name = "position_id")
    private Long positionId;

    private Long productId;

    @Enumerated(EnumType.STRING)
    private RequestType requestType;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    private String note;
    private LocalDateTime createdAt;

    @Column(name = "set_id")
    private Long setId;

    public enum RequestType {
        IN,          // Nhập kho thực tế
        OUT,         // Xuất kho thực tế
        ADJUST_IN,   // Dự kiến nhập
        ADJUST_OUT   // Dự kiến xuất
    }

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }

    public Long getPositionId() { return positionId; }
    public void setPositionId(Long positionId) { this.positionId = positionId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public RequestType getRequestType() { return requestType; }
    public void setRequestType(RequestType requestType) { this.requestType = requestType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Long getSetId() { return setId; }
    public void setSetId(Long setId) { this.setId = setId; }

    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }
}
