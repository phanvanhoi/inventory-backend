package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import manage.store.inventory.entity.enums.GuaranteeForm;
import manage.store.inventory.entity.enums.GuaranteeType;

@Data
public class GuaranteeCreateDTO {

    @NotNull(message = "Loại bảo lãnh không được trống")
    private GuaranteeType type;

    private GuaranteeForm form = GuaranteeForm.NONE;
    private BigDecimal amount;
    private LocalDate expiryDate;
    private String bank;
    private String note;
}
