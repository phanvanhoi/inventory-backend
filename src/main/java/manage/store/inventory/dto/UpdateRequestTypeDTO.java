package manage.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRequestTypeDTO {

    @NotBlank(message = "Request type không được để trống")
    private String requestType; // IN hoặc OUT
}
