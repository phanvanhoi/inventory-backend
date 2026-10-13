package manage.store.inventory.delivery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "vietmap_api_key")
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String vietmapApiKey;

    @Transient
    private boolean vietmapReady;

    @JsonIgnore
    public String getVietmapApiKey() {
        return vietmapApiKey;
    }

    @JsonProperty("vietmapApiKey")
    public void setVietmapApiKey(String vietmapApiKey) {
        this.vietmapApiKey = vietmapApiKey;
    }
}
