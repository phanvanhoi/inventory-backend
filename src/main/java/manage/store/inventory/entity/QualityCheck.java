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
import manage.store.inventory.entity.enums.QualityCheckStatus;

@Entity
@Table(name = "quality_checks")
@Data
public class QualityCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "qc_id")
    private Long qcId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tailor_assignment_id")
    private TailorAssignment tailorAssignment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kcs_user_id")
    private User kcsUser;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "full_documents_received", nullable = false)
    private Boolean fullDocumentsReceived = false;

    @Column(name = "full_variants_received", nullable = false)
    private Boolean fullVariantsReceived = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QualityCheckStatus status = QualityCheckStatus.PENDING;

    @Column(name = "notes")
    private String notes;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
