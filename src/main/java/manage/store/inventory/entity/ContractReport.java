package manage.store.inventory.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import manage.store.inventory.entity.enums.ReportPhase;

@Entity
@Table(name = "contract_reports")
@Data
public class ContractReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false)
    private ReportPhase currentPhase = ReportPhase.SALES_INPUT;

    // === SALES fields ===
    @ManyToOne
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "sales_person")
    private String salesPerson;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "finalized_list_sent_date")
    private LocalDate finalizedListSentDate;

    @Column(name = "finalized_list_received_date")
    private LocalDate finalizedListReceivedDate;

    @Column(name = "delivery_method")
    private String deliveryMethod;

    @Column(name = "extra_payment_date")
    private LocalDate extraPaymentDate;

    @Column(name = "extra_payment_amount")
    private BigDecimal extraPaymentAmount = BigDecimal.ZERO;

    @Column(name = "note")
    private String note;

    // === MEASUREMENT fields ===
    @Column(name = "measurement_start")
    private LocalDate measurementStart;

    @Column(name = "measurement_end")
    private LocalDate measurementEnd;

    @Column(name = "technician_name")
    private String technicianName;

    @Column(name = "measurement_received_date")
    private LocalDate measurementReceivedDate;

    @Column(name = "measurement_handler")
    private String measurementHandler;

    @Column(name = "skip_measurement")
    private Boolean skipMeasurement = false;

    @Column(name = "production_handover_date")
    private LocalDate productionHandoverDate;

    // === PRODUCTION fields ===
    @Column(name = "packing_return_date")
    private LocalDate packingReturnDate;

    @Column(name = "tailor_start_date")
    private LocalDate tailorStartDate;

    @Column(name = "tailor_expected_return")
    private LocalDate tailorExpectedReturn;

    @Column(name = "tailor_actual_return")
    private LocalDate tailorActualReturn;

    // === STOCKKEEPER fields ===
    @Column(name = "actual_shipping_date")
    private LocalDate actualShippingDate;

    // === Metadata ===
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
