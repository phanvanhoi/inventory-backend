package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class OrderUpdateDTO {

    // All fields optional — only update if not null

    private Long salesPersonUserId;
    private String salesPersonName;
    private String unitType;
    private Integer contractYear;
    private String orderCode;

    private BigDecimal totalBeforeVat;
    private BigDecimal vatAmount;
    private BigDecimal totalAfterVat;

    // SALES
    private LocalDate expectedDeliveryDate;
    private LocalDate finalizedListSentDate;
    private LocalDate finalizedListReceivedDate;
    private String deliveryMethod;
    private LocalDate extraPaymentDate;
    private BigDecimal extraPaymentAmount;

    // MEASUREMENT
    private LocalDate measurementStart;
    private LocalDate measurementEnd;
    private String technicianName;
    private LocalDate measurementReceivedDate;
    private String measurementHandler;
    private Boolean skipMeasurement;
    private LocalDate productionHandoverDate;

    // PRODUCTION
    private LocalDate tailorStartDate;
    private LocalDate tailorExpectedReturn;
    private LocalDate tailorActualReturn;
    private LocalDate packingReturnDate;

    // STOCKKEEPER
    private LocalDate actualShippingDate;

    // Flags
    private Boolean skipDesign;
    private Boolean designReady;
    private Boolean skipKcs;
    private Boolean qcPassed;
    private Boolean hasRepair;

    private String note;
}
