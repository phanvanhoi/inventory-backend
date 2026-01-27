package manage.store.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExecuteRequestDTO {

    @NotNull(message = "User ID không được để trống")
    private Long userId;
}
