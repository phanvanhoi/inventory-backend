package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import manage.store.inventory.entity.enums.PaymentStatus;

@Data
public class PaymentCreateDTO {

    @NotNull(message = "Số tiền không được trống")
    @DecimalMin(value = "0", message = "Số tiền không âm")
    private BigDecimal amount;

    private LocalDate scheduledDate;
    private LocalDate actualDate;
    private String bank;
    private PaymentStatus status = PaymentStatus.PENDING;
    private String note;
}
