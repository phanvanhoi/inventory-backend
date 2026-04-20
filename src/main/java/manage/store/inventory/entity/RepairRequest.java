package manage.store.inventory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import manage.store.inventory.entity.enums.LogisticsMethod;
import manage.store.inventory.entity.enums.RepairStatus;

@Entity
@Table(name = "repair_requests")
@Data
public class RepairRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "repair_id")
    private Long repairId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_batch_id")
    private PackingBatch packingBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private User receiverUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "receive_method")
    private LogisticsMethod receiveMethod;

    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    @Column(name = "qty_repair", nullable = false)
    private Integer qtyRepair = 0;

    @Column(name = "repair_details", columnDefinition = "TEXT")
    private String repairDetails;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_method")
    private LogisticsMethod returnMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_handler_user_id")
    private User returnHandlerUser;

    @Column(name = "parent_batches", columnDefinition = "TEXT")
    private String parentBatches;

    @Column(name = "reason_for_return", columnDefinition = "TEXT")
    private String reasonForReturn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RepairStatus status = RepairStatus.RECEIVED;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
