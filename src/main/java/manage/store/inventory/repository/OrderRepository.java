package manage.store.inventory.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.Order;
import manage.store.inventory.entity.enums.OrderStatus;
import manage.store.inventory.entity.enums.ReportPhase;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "LEFT JOIN FETCH o.salesPersonUser " +
           "LEFT JOIN FETCH o.createdByUser " +
           "ORDER BY o.createdAt DESC")
    List<Order> findAllWithRelations();

    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "LEFT JOIN FETCH o.salesPersonUser " +
           "LEFT JOIN FETCH o.createdByUser " +
           "WHERE o.orderId = :id")
    Optional<Order> findByIdWithRelations(@Param("id") Long id);

    Optional<Order> findByLegacyReportId(Long legacyReportId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCurrentPhase(ReportPhase phase);

    List<Order> findByCustomerCustomerId(Long customerId);

    List<Order> findBySalesPersonUserUserId(Long userId);

    // Hop dong tre han
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE o.expectedDeliveryDate IS NOT NULL " +
           "AND o.expectedDeliveryDate < :today " +
           "AND o.actualShippingDate IS NULL " +
           "AND o.cancelled = false " +
           "ORDER BY o.expectedDeliveryDate ASC")
    List<Order> findLateDeliveries(@Param("today") LocalDate today);

    // Hop dong sap den han
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE o.expectedDeliveryDate IS NOT NULL " +
           "AND o.expectedDeliveryDate BETWEEN :today AND :deadline " +
           "AND o.actualShippingDate IS NULL " +
           "AND o.cancelled = false " +
           "ORDER BY o.expectedDeliveryDate ASC")
    List<Order> findUpcomingDeliveries(
            @Param("today") LocalDate today,
            @Param("deadline") LocalDate deadline);

    // Tho tra tre
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.customer c " +
           "LEFT JOIN FETCH c.unit " +
           "WHERE o.tailorExpectedReturn IS NOT NULL " +
           "AND o.tailorExpectedReturn < :today " +
           "AND o.tailorActualReturn IS NULL " +
           "AND o.cancelled = false " +
           "ORDER BY o.tailorExpectedReturn ASC")
    List<Order> findLateTailorReturns(@Param("today") LocalDate today);

    // Dashboard counts by phase
    @Query("SELECT " +
           "COUNT(o), " +
           "SUM(CASE WHEN o.currentPhase = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.currentPhase = 'SALES_INPUT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.currentPhase = 'MEASUREMENT_INPUT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.currentPhase = 'PRODUCTION_INPUT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN o.currentPhase = 'STOCKKEEPER_INPUT' THEN 1 ELSE 0 END) " +
           "FROM Order o WHERE o.cancelled = false")
    Object[] getDashboardCounts();

    long countBySeedSource(String seedSource);

    // Order code generator: MAX sequence for year prefix "ORD-YYYY-"
    // Extract numeric suffix after prefix, cast to INT, find MAX.
    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(order_code, LENGTH(:prefix) + 1) AS UNSIGNED)), 0) " +
                   "FROM orders WHERE order_code LIKE CONCAT(:prefix, '%') AND deleted_at IS NULL",
           nativeQuery = true)
    Long findMaxSequenceByYearPrefix(@Param("prefix") String prefix);
}
