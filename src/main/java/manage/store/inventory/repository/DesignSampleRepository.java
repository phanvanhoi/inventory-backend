package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.DesignSample;

@Repository
public interface DesignSampleRepository extends JpaRepository<DesignSample, Long> {

    @Query("SELECT ds FROM DesignSample ds " +
           "LEFT JOIN FETCH ds.designerUser " +
           "WHERE ds.orderItem.orderItemId = :itemId " +
           "ORDER BY ds.createdAt DESC")
    List<DesignSample> findByOrderItemId(@Param("itemId") Long itemId);

    @Query("SELECT ds FROM DesignSample ds " +
           "LEFT JOIN FETCH ds.orderItem oi " +
           "LEFT JOIN FETCH ds.designerUser " +
           "WHERE oi.order.orderId = :orderId " +
           "ORDER BY oi.orderItemId, ds.createdAt DESC")
    List<DesignSample> findByOrderId(@Param("orderId") Long orderId);

    // For auto design_ready flag:
    // Đếm tổng items và items có ít nhất 1 APPROVED sample.
    @Query("SELECT COUNT(DISTINCT oi.orderItemId) FROM OrderItem oi " +
           "WHERE oi.order.orderId = :orderId AND oi.deletedAt IS NULL")
    long countOrderItems(@Param("orderId") Long orderId);

    @Query("SELECT COUNT(DISTINCT ds.orderItem.orderItemId) FROM DesignSample ds " +
           "WHERE ds.orderItem.order.orderId = :orderId AND ds.status = 'APPROVED'")
    long countApprovedItems(@Param("orderId") Long orderId);
}
