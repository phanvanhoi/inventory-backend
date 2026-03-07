package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractReportListDTO {

    private Long reportId;
    private String currentPhase;

    // SALES fields
    private Long unitId;
    private String unitName;
    private String unitType;
    private Integer contractYear;
    private String salesPerson;
    private LocalDate expectedDeliveryDate;
    private LocalDate finalizedListSentDate;
    private LocalDate finalizedListReceivedDate;
    private String deliveryMethod;
    private LocalDate extraPaymentDate;
    private BigDecimal extraPaymentAmount;
    private String note;

    // MEASUREMENT fields
    private LocalDate measurementStart;
    private LocalDate measurementEnd;
    private String technicianName;
    private LocalDate measurementReceivedDate;
    private String measurementHandler;
    private Boolean skipMeasurement;
    private LocalDate productionHandoverDate;

    // PRODUCTION fields
    private LocalDate packingReturnDate;
    private LocalDate tailorStartDate;
    private LocalDate tailorExpectedReturn;
    private LocalDate tailorActualReturn;

    // STOCKKEEPER fields
    private LocalDate actualShippingDate;

    // Metadata
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Computed
    private Integer daysLate;
}
