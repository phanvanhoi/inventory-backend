package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    private String orderCode;

    // SALES fields
    private Long salesPersonUserId;
    private String salesPersonName;
    private String unitType;
    private Integer contractYear;

    private BigDecimal totalBeforeVat = BigDecimal.ZERO;
    private BigDecimal vatAmount = BigDecimal.ZERO;
    private BigDecimal totalAfterVat = BigDecimal.ZERO;

    private LocalDate expectedDeliveryDate;
    private LocalDate finalizedListSentDate;
    private LocalDate finalizedListReceivedDate;
    private String deliveryMethod;
    private LocalDate extraPaymentDate;
    private BigDecimal extraPaymentAmount;

    private String note;

    @Valid
    private List<OrderItemCreateDTO> items;
}
