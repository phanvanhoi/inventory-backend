package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.RepairRequest;

@Repository
public interface RepairRequestRepository extends JpaRepository<RepairRequest, Long> {

    @Query("SELECT r FROM RepairRequest r " +
           "LEFT JOIN FETCH r.receiverUser " +
           "LEFT JOIN FETCH r.returnHandlerUser " +
           "LEFT JOIN FETCH r.orderItem " +
           "WHERE r.orderItem.order.orderId = :orderId " +
           "ORDER BY r.createdAt DESC")
    List<RepairRequest> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT r FROM RepairRequest r " +
           "LEFT JOIN FETCH r.orderItem " +
           "WHERE r.orderItem.orderItemId = :itemId " +
           "ORDER BY r.createdAt DESC")
    List<RepairRequest> findByOrderItemId(@Param("itemId") Long itemId);

    // Active = chưa SHIPPED_BACK
    @Query("SELECT COUNT(r) FROM RepairRequest r " +
           "WHERE r.orderItem.order.orderId = :orderId AND r.status <> 'SHIPPED_BACK'")
    long countActiveByOrderId(@Param("orderId") Long orderId);
}
