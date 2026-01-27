package manage.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectRequestDTO {

    private Long userId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
