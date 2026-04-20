package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdvanceCreateDTO {

    @NotNull(message = "Số tiền không được trống")
    @DecimalMin(value = "0", message = "Số tiền không âm")
    private BigDecimal amount;

    private LocalDate advanceDate;
    private String bank;
    private String note;
}
