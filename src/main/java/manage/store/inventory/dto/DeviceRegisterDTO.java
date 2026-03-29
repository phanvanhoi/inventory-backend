package manage.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceRegisterDTO {
    @NotBlank
    private String pushToken;
    private String platform;
}
