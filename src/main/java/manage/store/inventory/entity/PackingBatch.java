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
import manage.store.inventory.entity.enums.DeliveryStatus;
import manage.store.inventory.entity.enums.PackingBatchStatus;

@Entity
@Table(name = "packing_batches")
@Data
public class PackingBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "packing_batch_id")
    private Long packingBatchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packer_user_id")
    private User packerUser;

    @Column(name = "documents_received_date")
    private LocalDate documentsReceivedDate;

    @Column(name = "packing_started_date")
    private LocalDate packingStartedDate;

    @Column(name = "packing_completed_date")
    private LocalDate packingCompletedDate;

    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;

    @Column(name = "contract_delivery_date")
    private LocalDate contractDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", nullable = false)
    private DeliveryStatus deliveryStatus = DeliveryStatus.NOT_DELIVERED;

    @Column(name = "tick_file_url")
    private String tickFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PackingBatchStatus status = PackingBatchStatus.PREPARING;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
