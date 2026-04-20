package manage.store.inventory.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import manage.store.inventory.entity.enums.TailorType;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "tailors")
@SQLRestriction("deleted_at IS NULL")
@Data
public class Tailor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tailor_id")
    private Long tailorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TailorType type;

    @Column(name = "phone")
    private String phone;

    @Column(name = "location")
    private String location;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "note")
    private String note;

    @Column(name = "seed_source")
    private String seedSource;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
