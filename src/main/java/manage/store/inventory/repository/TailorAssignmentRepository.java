package manage.store.inventory.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.TailorAssignment;

@Repository
public interface TailorAssignmentRepository extends JpaRepository<TailorAssignment, Long> {

    @Query("SELECT ta FROM TailorAssignment ta " +
           "LEFT JOIN FETCH ta.tailor " +
           "WHERE ta.orderItem.orderItemId = :itemId " +
           "ORDER BY ta.createdAt ASC")
    List<TailorAssignment> findByOrderItemId(@Param("itemId") Long itemId);

    @Query("SELECT ta FROM TailorAssignment ta " +
           "LEFT JOIN FETCH ta.tailor " +
           "LEFT JOIN FETCH ta.orderItem oi " +
           "WHERE oi.order.orderId = :orderId " +
           "ORDER BY oi.orderItemId, ta.createdAt ASC")
    List<TailorAssignment> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT ta FROM TailorAssignment ta " +
           "LEFT JOIN FETCH ta.tailor " +
           "LEFT JOIN FETCH ta.orderItem oi " +
           "LEFT JOIN FETCH oi.order " +
           "WHERE ta.tailor.tailorId = :tailorId " +
           "ORDER BY ta.appointmentDate DESC")
    List<TailorAssignment> findByTailorId(@Param("tailorId") Long tailorId);

    // Tổng qty_assigned cho 1 order_item → dùng để validate không vượt qty_contract
    @Query("SELECT COALESCE(SUM(ta.qtyAssigned), 0) FROM TailorAssignment ta " +
           "WHERE ta.orderItem.orderItemId = :itemId AND ta.assignmentId <> :excludeId")
    long sumQtyAssignedExcluding(@Param("itemId") Long itemId, @Param("excludeId") Long excludeId);

    // Alert: thợ trả trễ hạn
    @Query("SELECT ta FROM TailorAssignment ta " +
           "LEFT JOIN FETCH ta.tailor " +
           "LEFT JOIN FETCH ta.orderItem oi " +
           "LEFT JOIN FETCH oi.order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE ta.appointmentDate IS NOT NULL " +
           "AND ta.appointmentDate < :today " +
           "AND ta.status <> 'COMPLETED' " +
           "ORDER BY ta.appointmentDate ASC")
    List<TailorAssignment> findOverdue(@Param("today") LocalDate today);
}
