package manage.store.inventory.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import manage.store.inventory.dto.InventoryRequestHeaderDTO;
import manage.store.inventory.dto.InventoryRequestListDTO;
import manage.store.inventory.entity.InventoryRequest;

public interface InventoryRequestRepository
        extends JpaRepository<InventoryRequest, Long> {

    @Query(
            value = """
        SELECT
          r.request_id      AS requestId,
          u.unit_name       AS unitName,
          p.product_name    AS productName,
          r.request_type    AS requestType,
          r.expected_date   AS expectedDate,
          r.note            AS note,
          r.created_at      AS createdAt
        FROM inventory_requests r
        JOIN units u ON u.unit_id = r.unit_id
        LEFT JOIN products p ON p.product_id = r.product_id
        WHERE r.request_id = :requestId
      """,
            nativeQuery = true
    )
    Optional<InventoryRequestHeaderDTO> findHeaderByRequestId(
            @Param("requestId") Long requestId
    );

    @Query(
            value = """
    SELECT
      r.request_id   AS requestId,
      u.unit_name    AS unitName,
      p.product_name AS productName,
      r.request_type AS requestType,
      r.expected_date AS expectedDate,
      r.note         AS note,
      r.created_at   AS createdAt
    FROM inventory_requests r
    JOIN units u ON u.unit_id = r.unit_id
    LEFT JOIN products p ON p.product_id = r.product_id
    ORDER BY r.created_at DESC
  """,
            nativeQuery = true
    )
    List<InventoryRequestListDTO> findAllRequests();

    List<InventoryRequest> findBySetId(Long setId);
}
