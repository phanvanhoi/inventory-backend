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
@Table(name = "driver_day_plans")
@Data
public class DriverDayPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "return_to_shop", nullable = false)
    private boolean returnToShop = true;

    @Column(name = "total_meters", nullable = false)
    private int totalMeters;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}
