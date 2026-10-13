package manage.store.inventory.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "delivery_settings")
@Data
public class DeliverySetting {

    @Id
    @Column(name = "setting_id")
    private Byte settingId = 1;

    @Column(name = "distance_base_url")
    private String distanceBaseUrl;

    @Column(name = "detour_limit_meters", nullable = false)
    private int detourLimitMeters = 5000;

    @Column(name = "cost_per_km", nullable = false)
    private long costPerKm;
}
