package manage.store.inventory.delivery;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(name = "driver_day_plans", uniqueConstraints = @UniqueConstraint(name = "uk_plan_driver_date", columnNames = {"driver_id", "service_date"}))
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

    @Column(name = "start_stop_id")
    private Long startStopId;

    @Column(name = "passed_seq", nullable = false)
    private int passedSeq;

    @Column(name = "touched_open_stop", nullable = false)
    private boolean touchedOpenStop;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}
