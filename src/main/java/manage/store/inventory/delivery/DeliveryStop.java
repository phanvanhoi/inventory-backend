package manage.store.inventory.delivery;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "delivery_stops")
@Data
public class DeliveryStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stop_id")
    private Long stopId;

    @Column(name = "stop_name", nullable = false)
    private String stopName;

    private String address;
    private Double latitude;
    private Double longitude;

    @Column(name = "is_shop", nullable = false)
    private boolean shop;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
