package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import manage.store.inventory.entity.DesignDocument;

@Repository
public interface DesignDocumentRepository extends JpaRepository<DesignDocument, Long> {

    @Query("SELECT dd FROM DesignDocument dd " +
           "LEFT JOIN FETCH dd.uploadedByUser " +
           "WHERE dd.order.orderId = :orderId " +
           "ORDER BY dd.uploadedAt DESC")
    List<DesignDocument> findByOrderId(@Param("orderId") Long orderId);
}
