package manage.store.inventory.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "missing_items")
@Data
public class MissingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "missing_id")
    private Long missingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "packing_batch_id", nullable = false)
    private PackingBatch packingBatch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "missing_quantity", nullable = false)
    private Integer missingQuantity = 0;

    @Column(name = "missing_list_file_url")
    private String missingListFileUrl;

    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
