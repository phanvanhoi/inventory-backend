package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import manage.store.inventory.dto.ReceiptTimelineProjection;
import manage.store.inventory.entity.ReceiptRecord;

public interface ReceiptRecordRepository extends JpaRepository<ReceiptRecord, Long> {

    List<ReceiptRecord> findBySetIdOrderByReceivedAtDesc(Long setId);

    List<ReceiptRecord> findBySetId(Long setId);

    // G6, V24 — find receipts tied to a specific OrderItem (for Order detail view)
    @Query("SELECT rr FROM ReceiptRecord rr " +
           "LEFT JOIN FETCH rr.receivedBy " +
           "WHERE rr.orderItem.orderItemId = :orderItemId " +
           "ORDER BY rr.receivedAt DESC")
    List<ReceiptRecord> findByOrderItemId(@Param("orderItemId") Long orderItemId);

    /**
     * Dòng thời gian nhận hàng: mỗi row = 1 lần nhận, kèm tổng hợp số lượng
     */
    @Query(
            value = """
                SELECT
                    rr.receipt_id       AS receiptId,
                    rr.received_at      AS receivedAt,
                    u.full_name         AS receivedByName,
                    rr.note             AS note,
                    COUNT(DISTINCT CONCAT(ri.request_id, '-', ri.variant_id)) AS totalItems,
                    COALESCE(SUM(ri.received_quantity), 0)                    AS totalQuantity
                FROM receipt_records rr
                JOIN users u ON u.user_id = rr.received_by
                LEFT JOIN receipt_items ri ON ri.receipt_id = rr.receipt_id
                WHERE rr.set_id = :setId
                GROUP BY rr.receipt_id, rr.received_at, u.full_name, rr.note
                ORDER BY rr.received_at DESC
            """,
            nativeQuery = true
    )
    List<ReceiptTimelineProjection> findTimelineBySetId(@Param("setId") Long setId);
}
