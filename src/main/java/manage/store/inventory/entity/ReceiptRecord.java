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
@Table(name = "receipt_records")
@Data
public class ReceiptRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "set_id", nullable = false)
    private Long setId;

    @ManyToOne
    @JoinColumn(name = "received_by", nullable = false)
    private User receivedBy;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "note")
    private String note;

    // G6, V24 — Optional link to OrderItem (Lark integration, nullable for legacy rows)
    // LAZY: receipt list queries không cần load OrderItem trừ khi caller expand
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;
}
