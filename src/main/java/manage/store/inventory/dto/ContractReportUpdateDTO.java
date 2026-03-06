package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContractReportUpdateDTO {

    @NotNull(message = "Đơn vị không được để trống")
    private Long unitId;

    private String salesPerson;
    private LocalDate expectedDeliveryDate;

    // So do
    private LocalDate measurementStart;
    private LocalDate measurementEnd;
    private String technicianName;
    private LocalDate measurementReceivedDate;
    private String measurementHandler;
    private Boolean skipMeasurement;

    // DS Chot
    private LocalDate finalizedListSentDate;
    private LocalDate finalizedListReceivedDate;

    // San xuat
    private LocalDate productionHandoverDate;
    private LocalDate packingReturnDate;
    private LocalDate tailorStartDate;
    private LocalDate tailorExpectedReturn;
    private LocalDate tailorActualReturn;

    // Giao hang
    private LocalDate actualShippingDate;
    private String deliveryMethod;

    // Thanh toan
    private LocalDate extraPaymentDate;
    private BigDecimal extraPaymentAmount;

    private String note;
}
