package manage.store.inventory.delivery;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "delivery_trips")
@Data
public class DeliveryTrip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(name = "pickup_stop_id", nullable = false)
    private Long pickupStopId;

    @Column(name = "dropoff_stop_id", nullable = false)
    private Long dropoffStopId;

    @Column(nullable = false)
    private String status;
}
