package manage.store.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;
import manage.store.inventory.entity.enums.DeliveryStatus;
import manage.store.inventory.entity.enums.InvoiceStatus;
import manage.store.inventory.entity.enums.OrderStatus;

/**
 * Full-width row cho Dashboard table, ~36 columns mapping Excel "Báo cáo" sheet.
 * Aggregates từ nhiều tables (orders + customers + financial + packing + guarantees).
 */
@Data
public class DashboardRowDTO {
    // Order identity
    private Long orderId;
    private String orderCode;
    private String larkLegacyId;

    // Customer / Unit
    private Long customerId;
    private Long unitId;
    private String unitName;
    private String province;
    private String unitType;
    private Integer contractYear;
    private String customerType;

    // Order state
    private OrderStatus status;
    private String currentPhase;
    private String salesPersonName;

    // Measurement summary
    private LocalDate measurementStart;
    private LocalDate measurementEnd;
    private String measurementTakerName;
    private String measurementComposerName;
    private LocalDate measurementReceivedDate;
    private LocalDate measurementHandoverDate;     // V21 field

    // Delivery
    private DeliveryStatus latestDeliveryStatus;   // from most recent packing_batch
    private LocalDate contractDeliveryDate;        // from orders.expected_delivery_date or packing_batches.contract_delivery_date
    private LocalDate expectedDeliveryDate;
    private LocalDate actualDeliveryDate;

    // Invoice
    private InvoiceStatus invoiceStatus;           // ISSUED if any invoice with that status
    private LocalDate invoiceIssuedDate;
    private String invoiceNumber;

    // Financial
    private BigDecimal totalBeforeVat;
    private BigDecimal vatAmount;
    private BigDecimal totalAfterVat;
    private BigDecimal totalAdvance;
    private BigDecimal totalPaid;
    private BigDecimal remaining;

    // Advance / payment meta
    private LocalDate latestAdvanceDate;
    private String latestAdvanceBank;
    private LocalDate latestPaymentScheduledDate;
    private LocalDate latestPaymentActualDate;
    private String latestPaymentBank;

    // Guarantees (3 types, amount + expiry for each)
    private String biddingGuaranteeForm;           // NONE|BANK|CASH
    private BigDecimal biddingGuaranteeAmount;
    private LocalDate biddingGuaranteeExpiry;

    private String performanceGuaranteeForm;
    private BigDecimal performanceGuaranteeAmount;
    private LocalDate performanceGuaranteeExpiry;

    private String warrantyGuaranteeForm;
    private BigDecimal warrantyGuaranteeAmount;
    private LocalDate warrantyGuaranteeExpiry;

    // Missing + repair flags
    private Boolean hasMissing;
    private Boolean hasRepair;

    // Computed
    private Long daysLate;
}
