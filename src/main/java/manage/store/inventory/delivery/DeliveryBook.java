package manage.store.inventory.delivery;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "delivery_books")
@Data
public class DeliveryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "pickup_stop_id", nullable = false)
    private Long pickupStopId;

    @Column(name = "dropoff_stop_id", nullable = false)
    private Long dropoffStopId;

    @Column(name = "service_date", nullable = false)
    private LocalDate serviceDate;

    private String note;

    @Column(name = "owner_name", length = 80)
    private String ownerName;

    @Column(name = "request_set_id")
    private Long requestSetId;

    @Column(nullable = false)
    private String status;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "sender_confirmed_at")
    private LocalDateTime senderConfirmedAt;

    @Column(name = "sender_confirmed_by")
    private Long senderConfirmedBy;

    @Column(name = "sender_on_behalf", nullable = false)
    private boolean senderOnBehalf;

    @Column(name = "receiver_confirmed_at")
    private LocalDateTime receiverConfirmedAt;

    @Column(name = "receiver_confirmed_by")
    private Long receiverConfirmedBy;

    @Column(name = "receiver_on_behalf", nullable = false)
    private boolean receiverOnBehalf;

    @Column(nullable = false)
    private boolean sudden;

    @Column(name = "sudden_confirmed", nullable = false)
    private boolean suddenConfirmed;
}
