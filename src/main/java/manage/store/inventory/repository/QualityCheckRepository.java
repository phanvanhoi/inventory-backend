package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.QualityCheck;

@Repository
public interface QualityCheckRepository extends JpaRepository<QualityCheck, Long> {

    @Query("SELECT qc FROM QualityCheck qc " +
           "LEFT JOIN FETCH qc.kcsUser " +
           "LEFT JOIN FETCH qc.tailorAssignment ta " +
           "LEFT JOIN FETCH ta.tailor " +
           "WHERE qc.orderItem.orderItemId = :itemId " +
           "ORDER BY qc.createdAt DESC")
    List<QualityCheck> findByOrderItemId(@Param("itemId") Long itemId);

    @Query("SELECT qc FROM QualityCheck qc " +
           "LEFT JOIN FETCH qc.kcsUser " +
           "LEFT JOIN FETCH qc.orderItem oi " +
           "LEFT JOIN FETCH qc.tailorAssignment ta " +
           "LEFT JOIN FETCH ta.tailor " +
           "WHERE oi.order.orderId = :orderId " +
           "ORDER BY oi.orderItemId, qc.createdAt DESC")
    List<QualityCheck> findByOrderId(@Param("orderId") Long orderId);

    // Auto qc_passed flag logic:
    // Order được coi là qc_passed khi mỗi order_item có ít nhất 1 QC với status PASSED.
    @Query("SELECT COUNT(DISTINCT qc.orderItem.orderItemId) FROM QualityCheck qc " +
           "WHERE qc.orderItem.order.orderId = :orderId AND qc.status = 'PASSED'")
    long countPassedItems(@Param("orderId") Long orderId);
}
