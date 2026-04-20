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
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "orders")
@SQLRestriction("deleted_at IS NULL")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code", unique = true)
    private String orderCode;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_phase", nullable = false)
    private ReportPhase currentPhase = ReportPhase.SALES_INPUT;

    @ManyToOne
    @JoinColumn(name = "sales_person_user_id")
    private User salesPersonUser;

    @Column(name = "sales_person_name")
    private String salesPersonName;

    @Column(name = "unit_type")
    private String unitType;

    @Column(name = "contract_year")
    private Integer contractYear;

    @Column(name = "total_before_vat")
    private BigDecimal totalBeforeVat = BigDecimal.ZERO;

    @Column(name = "vat_amount")
    private BigDecimal vatAmount = BigDecimal.ZERO;

    @Column(name = "total_after_vat")
    private BigDecimal totalAfterVat = BigDecimal.ZERO;

    // === SALES phase ===
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

    // === MEASUREMENT phase ===
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

    // === MEASUREMENT detail (G3, V21) — 6 mốc ngày chi tiết ===
    @Column(name = "customer_registration_sent_date")
    private LocalDate customerRegistrationSentDate;

    @Column(name = "tech_book_return_date")
    private LocalDate techBookReturnDate;

    @Column(name = "measurement_received_from_tech_date")
    private LocalDate measurementReceivedFromTechDate;

    @Column(name = "list_sent_to_customer_date")
    private LocalDate listSentToCustomerDate;

    @Column(name = "list_finalized_date")
    private LocalDate listFinalizedDate;

    @Column(name = "measurement_handover_date_v2")
    private LocalDate measurementHandoverDateV2;

    @ManyToOne
    @JoinColumn(name = "measurement_taker_user_id")
    private User measurementTakerUser;

    @ManyToOne
    @JoinColumn(name = "measurement_composer_user_id")
    private User measurementComposerUser;

    // === Files (G3, V21) ===
    @Column(name = "contract_file_url")
    private String contractFileUrl;

    @Column(name = "handover_record_url")
    private String handoverRecordUrl;

    @Column(name = "liquidation_record_url")
    private String liquidationRecordUrl;

    @Column(name = "customer_measurement_file_url")
    private String customerMeasurementFileUrl;

    // === NPL proposal (G5, V23) — Bản đề xuất phụ liệu cấp đơn ===
    @Column(name = "npl_proposal_url")
    private String nplProposalUrl;

    // === PRODUCTION phase ===
    @Column(name = "tailor_start_date")
    private LocalDate tailorStartDate;

    @Column(name = "tailor_expected_return")
    private LocalDate tailorExpectedReturn;

    @Column(name = "tailor_actual_return")
    private LocalDate tailorActualReturn;

    @Column(name = "packing_return_date")
    private LocalDate packingReturnDate;

    // === STOCKKEEPER phase ===
    @Column(name = "actual_shipping_date")
    private LocalDate actualShippingDate;

    // === Flags ===
    @Column(name = "skip_design")
    private Boolean skipDesign = true;

    @Column(name = "design_ready")
    private Boolean designReady = false;

    @Column(name = "skip_kcs")
    private Boolean skipKcs = true;

    @Column(name = "qc_passed")
    private Boolean qcPassed = false;

    @Column(name = "has_repair")
    private Boolean hasRepair = false;

    @Column(name = "cancelled")
    private Boolean cancelled = false;

    @Column(name = "note")
    private String note;

    // === Migration tracking ===
    @Column(name = "legacy_report_id")
    private Long legacyReportId;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "lark_legacy_id")
    private String larkLegacyId;

    // === Metadata ===
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdByUser;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
