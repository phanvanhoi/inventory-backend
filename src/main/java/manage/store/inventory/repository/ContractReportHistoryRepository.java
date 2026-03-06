package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.ContractReportHistory;

@Repository
public interface ContractReportHistoryRepository extends JpaRepository<ContractReportHistory, Long> {

    @Query("SELECT h FROM ContractReportHistory h " +
           "LEFT JOIN FETCH h.changedByUser " +
           "WHERE h.reportId = :reportId " +
           "ORDER BY h.changedAt DESC")
    List<ContractReportHistory> findByReportIdWithUser(@Param("reportId") Long reportId);
}
