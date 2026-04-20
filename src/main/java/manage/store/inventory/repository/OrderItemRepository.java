package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi FROM OrderItem oi " +
           "LEFT JOIN FETCH oi.product " +
           "WHERE oi.order.orderId = :orderId " +
           "ORDER BY oi.orderItemId ASC")
    List<OrderItem> findByOrderId(@Param("orderId") Long orderId);

    long countByOrderOrderId(Long orderId);

    // G4/G7 helper: count ACTIVE (non-deleted) items for flag recompute logic
    @Query("SELECT COUNT(DISTINCT oi.orderItemId) FROM OrderItem oi " +
           "WHERE oi.order.orderId = :orderId AND oi.deletedAt IS NULL")
    long countActiveByOrderId(@Param("orderId") Long orderId);
}
