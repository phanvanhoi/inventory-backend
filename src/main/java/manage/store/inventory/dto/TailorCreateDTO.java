package manage.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import manage.store.inventory.entity.enums.TailorType;

@Data
public class TailorCreateDTO {

    @NotBlank(message = "Tên thợ không được trống")
    private String name;

    private TailorType type;
    private String phone;
    private String location;
    private Boolean active = true;
    private String note;
}
