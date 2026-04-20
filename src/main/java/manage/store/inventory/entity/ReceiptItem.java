package manage.store.inventory.entity;

import java.math.BigDecimal;

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
@Table(name = "receipt_items")
@Data
public class ReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_item_id")
    private Long receiptItemId;

    @Column(name = "receipt_id", nullable = false)
    private Long receiptId;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "received_quantity", nullable = false)
    private BigDecimal receivedQuantity;

    // G6, V24 — Optional link to TailorAssignment (nullable for legacy rows)
    // LAZY: tránh N+1 khi list receipt_items mà không cần expand
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tailor_assignment_id")
    private TailorAssignment tailorAssignment;
}
