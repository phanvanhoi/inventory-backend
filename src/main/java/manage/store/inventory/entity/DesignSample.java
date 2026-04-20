package manage.store.inventory.entity;

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
import manage.store.inventory.entity.enums.DesignSampleStatus;

@Entity
@Table(name = "design_samples")
@Data
public class DesignSample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "design_sample_id")
    private Long designSampleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(name = "sample_image_url")
    private String sampleImageUrl;

    @Column(name = "fabric_code")
    private String fabricCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_user_id")
    private User designerUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DesignSampleStatus status = DesignSampleStatus.DRAFT;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
