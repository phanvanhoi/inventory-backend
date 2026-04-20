package manage.store.inventory.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Guarantee;

@Repository
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {

    List<Guarantee> findByOrderOrderIdOrderByTypeAsc(Long orderId);

    // Alert: sắp hết hạn trong N ngày
    @Query("SELECT g FROM Guarantee g " +
           "LEFT JOIN FETCH g.order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE g.expiryDate IS NOT NULL " +
           "AND g.expiryDate BETWEEN :today AND :deadline " +
           "AND g.form <> 'NONE' " +
           "ORDER BY g.expiryDate ASC")
    List<Guarantee> findExpiringBetween(
            @Param("today") LocalDate today,
            @Param("deadline") LocalDate deadline);

    // Alert: đã hết hạn
    @Query("SELECT g FROM Guarantee g " +
           "LEFT JOIN FETCH g.order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE g.expiryDate IS NOT NULL " +
           "AND g.expiryDate < :today " +
           "AND g.form <> 'NONE' " +
           "ORDER BY g.expiryDate ASC")
    List<Guarantee> findExpired(@Param("today") LocalDate today);
}
