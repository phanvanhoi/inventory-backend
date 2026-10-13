package manage.store.inventory.delivery;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "driver_locations")
@Data
public class DriverLocation {

    @Id
    @Column(name = "driver_id")
    private Long driverId;

    private double latitude;
    private double longitude;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;
}
