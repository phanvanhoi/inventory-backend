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
import manage.store.inventory.entity.enums.TailorAssignmentStatus;
import manage.store.inventory.entity.enums.TailorType;

@Entity
@Table(name = "tailor_assignments")
@Data
public class TailorAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tailor_id", nullable = false)
    private Tailor tailor;

    @Column(name = "qty_assigned", nullable = false)
    private Integer qtyAssigned = 0;

    @Column(name = "qty_from_stock", nullable = false)
    private Integer qtyFromStock = 0;

    @Column(name = "qty_returned", nullable = false)
    private Integer qtyReturned = 0;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "tailor_type")
    private TailorType tailorType;

    @Column(name = "npl_proposal_url")
    private String nplProposalUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TailorAssignmentStatus status = TailorAssignmentStatus.PLANNED;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
