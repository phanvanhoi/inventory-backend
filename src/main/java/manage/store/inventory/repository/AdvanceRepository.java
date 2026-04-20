package manage.store.inventory.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Advance;

@Repository
public interface AdvanceRepository extends JpaRepository<Advance, Long> {

    List<Advance> findByOrderOrderIdOrderByAdvanceDateDesc(Long orderId);

    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM Advance a WHERE a.order.orderId = :orderId")
    BigDecimal sumByOrderId(@Param("orderId") Long orderId);
}
