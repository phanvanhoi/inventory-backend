package manage.store.inventory.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.PackingBatch;

@Repository
public interface PackingBatchRepository extends JpaRepository<PackingBatch, Long> {

    @Query("SELECT pb FROM PackingBatch pb " +
           "LEFT JOIN FETCH pb.packerUser " +
           "WHERE pb.order.orderId = :orderId " +
           "ORDER BY pb.createdAt DESC")
    List<PackingBatch> findByOrderId(@Param("orderId") Long orderId);

    // Alert: batches trễ giao hàng (actual_delivery_date null AND contract_delivery_date < today)
    @Query("SELECT pb FROM PackingBatch pb " +
           "LEFT JOIN FETCH pb.packerUser " +
           "LEFT JOIN FETCH pb.order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE pb.contractDeliveryDate IS NOT NULL " +
           "AND pb.contractDeliveryDate < :today " +
           "AND pb.actualDeliveryDate IS NULL " +
           "AND pb.status <> 'RETURNED' " +
           "ORDER BY pb.contractDeliveryDate ASC")
    List<PackingBatch> findOverdue(@Param("today") LocalDate today);
}
