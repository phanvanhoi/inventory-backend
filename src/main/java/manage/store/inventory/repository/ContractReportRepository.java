package manage.store.inventory.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.ContractReport;
import manage.store.inventory.entity.enums.ReportPhase;

@Repository
public interface ContractReportRepository extends JpaRepository<ContractReport, Long> {

    @Query("SELECT cr FROM ContractReport cr " +
           "LEFT JOIN FETCH cr.unit " +
           "LEFT JOIN FETCH cr.createdByUser " +
           "ORDER BY cr.createdAt DESC")
    List<ContractReport> findAllWithRelations();

    @Query("SELECT cr FROM ContractReport cr " +
           "LEFT JOIN FETCH cr.unit " +
           "LEFT JOIN FETCH cr.createdByUser " +
           "WHERE cr.reportId = :id")
    ContractReport findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT cr FROM ContractReport cr " +
           "LEFT JOIN FETCH cr.unit " +
           "LEFT JOIN FETCH cr.createdByUser " +
           "WHERE cr.currentPhase = :phase " +
           "ORDER BY cr.createdAt DESC")
    List<ContractReport> findByPhase(@Param("phase") ReportPhase phase);

    // Hop dong tre han
    @Query("SELECT cr FROM ContractReport cr " +
           "LEFT JOIN FETCH cr.unit " +
           "WHERE cr.expectedDeliveryDate IS NOT NULL " +
           "AND cr.expectedDeliveryDate < :today " +
           "AND cr.actualShippingDate IS NULL " +
           "ORDER BY cr.expectedDeliveryDate ASC")
    List<ContractReport> findLateDeliveries(@Param("today") LocalDate today);

    // Hop dong sap den han
    @Query("SELECT cr FROM ContractReport cr " +
           "LEFT JOIN FETCH cr.unit " +
           "WHERE cr.expectedDeliveryDate IS NOT NULL " +
           "AND cr.expectedDeliveryDate BETWEEN :today AND :deadline " +
           "AND cr.actualShippingDate IS NULL " +
           "ORDER BY cr.expectedDeliveryDate ASC")
    List<ContractReport> findUpcomingDeliveries(
            @Param("today") LocalDate today,
            @Param("deadline") LocalDate deadline);

    // Tho tra tre
    @Query("SELECT cr FROM ContractReport cr " +
           "LEFT JOIN FETCH cr.unit " +
           "WHERE cr.tailorExpectedReturn IS NOT NULL " +
           "AND cr.tailorExpectedReturn < :today " +
           "AND cr.tailorActualReturn IS NULL " +
           "ORDER BY cr.tailorExpectedReturn ASC")
    List<ContractReport> findLateTailorReturns(@Param("today") LocalDate today);

    // Dashboard counts by phase
    @Query("SELECT " +
           "COUNT(cr), " +
           "SUM(CASE WHEN cr.currentPhase = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN cr.currentPhase = 'SALES_INPUT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN cr.currentPhase = 'MEASUREMENT_INPUT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN cr.currentPhase = 'PRODUCTION_INPUT' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN cr.currentPhase = 'STOCKKEEPER_INPUT' THEN 1 ELSE 0 END) " +
           "FROM ContractReport cr")
    Object[] getDashboardCounts();
}
