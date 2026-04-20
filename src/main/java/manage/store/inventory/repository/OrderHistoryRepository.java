package manage.store.inventory.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.OrderHistory;

@Repository
public interface OrderHistoryRepository extends JpaRepository<OrderHistory, Long> {

    @Query("SELECT oh FROM OrderHistory oh " +
           "LEFT JOIN FETCH oh.changedByUser " +
           "WHERE oh.order.orderId = :orderId " +
           "ORDER BY oh.changedAt DESC")
    List<OrderHistory> findByOrderId(@Param("orderId") Long orderId);

    // Dashboard "ai làm gì gần đây" — G0-2 decision
    @Query("SELECT oh FROM OrderHistory oh " +
           "LEFT JOIN FETCH oh.changedByUser " +
           "LEFT JOIN FETCH oh.order " +
           "ORDER BY oh.changedAt DESC")
    List<OrderHistory> findRecentActivity();

    // Cron purge: xoa record cu hon 3 thang (G0-2 decision)
    @Modifying
    @Query("DELETE FROM OrderHistory oh WHERE oh.changedAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
