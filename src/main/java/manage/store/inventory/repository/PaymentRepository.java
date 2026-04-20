package manage.store.inventory.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Payment;
import manage.store.inventory.entity.enums.PaymentStatus;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderOrderIdOrderByScheduledDateAsc(Long orderId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
           "WHERE p.order.orderId = :orderId AND p.status IN ('PAID','CONFIRMED')")
    BigDecimal sumPaidByOrderId(@Param("orderId") Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    // Payment overdue: scheduled < today, status=PENDING
    @Query("SELECT p FROM Payment p " +
           "LEFT JOIN FETCH p.order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE p.status = 'PENDING' AND p.scheduledDate < :today " +
           "ORDER BY p.scheduledDate ASC")
    List<Payment> findOverdue(@Param("today") LocalDate today);
}
