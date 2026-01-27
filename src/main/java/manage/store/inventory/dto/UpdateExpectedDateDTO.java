package manage.store.inventory.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateExpectedDateDTO {

    @NotNull(message = "Ngày dự kiến mới không được để trống")
    private LocalDate newExpectedDate;

    // userId để xác thực quyền (optional, có thể dùng sau này)
    private Long userId;
}
