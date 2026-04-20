package manage.store.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MissingItemCreateDTO {

    @NotNull(message = "Mặt hàng không được trống")
    private Long orderItemId;

    @NotNull(message = "Số lượng thiếu không được trống")
    @Min(value = 0, message = "Số lượng không âm")
    private Integer missingQuantity;

    private String missingListFileUrl;
    private Boolean resolved;
    private String note;
}
