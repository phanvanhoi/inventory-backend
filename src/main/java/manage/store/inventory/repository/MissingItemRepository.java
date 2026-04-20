package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.MissingItem;

@Repository
public interface MissingItemRepository extends JpaRepository<MissingItem, Long> {

    @Query("SELECT mi FROM MissingItem mi " +
           "LEFT JOIN FETCH mi.orderItem " +
           "WHERE mi.packingBatch.packingBatchId = :batchId " +
           "ORDER BY mi.createdAt ASC")
    List<MissingItem> findByPackingBatchId(@Param("batchId") Long batchId);

    @Query("SELECT mi FROM MissingItem mi " +
           "LEFT JOIN FETCH mi.orderItem oi " +
           "LEFT JOIN FETCH mi.packingBatch pb " +
           "WHERE pb.order.orderId = :orderId AND mi.resolved = false " +
           "ORDER BY mi.createdAt DESC")
    List<MissingItem> findUnresolvedByOrderId(@Param("orderId") Long orderId);
}
