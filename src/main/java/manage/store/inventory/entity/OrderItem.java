package manage.store.inventory.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "order_items")
@SQLRestriction("deleted_at IS NULL")
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long orderItemId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "qty_contract", nullable = false)
    private Integer qtyContract = 0;

    @Column(name = "qty_settlement")
    private Integer qtySettlement;

    @Column(name = "unit_price")
    private BigDecimal unitPrice = BigDecimal.ZERO;

    // Generated columns (read-only from app)
    @Column(name = "amount_contract", insertable = false, updatable = false)
    private BigDecimal amountContract;

    @Column(name = "amount_settlement", insertable = false, updatable = false)
    private BigDecimal amountSettlement;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "lark_legacy_id")
    private String larkLegacyId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
