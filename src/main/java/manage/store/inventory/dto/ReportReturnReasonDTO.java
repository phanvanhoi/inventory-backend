package manage.store.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportReturnReasonDTO {

    @NotBlank(message = "Lý do trả lại không được để trống")
    private String reason;
}
