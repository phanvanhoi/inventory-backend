package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import manage.store.inventory.dto.ReceiptEntryProjection;
import manage.store.inventory.entity.ReceiptItem;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {

    List<ReceiptItem> findByReceiptId(Long receiptId);

    /**
     * Tổng số lượng đã nhận theo request_id và variant_id
     * (gộp tất cả các lần nhận)
     */
    @Query(
            value = """
                SELECT COALESCE(SUM(ri.received_quantity), 0)
                FROM receipt_items ri
                JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
                WHERE rr.set_id = :setId
                  AND ri.request_id = :requestId
                  AND ri.variant_id = :variantId
            """,
            nativeQuery = true
    )
    Integer getTotalReceivedByRequestAndVariant(
            @Param("setId") Long setId,
            @Param("requestId") Long requestId,
            @Param("variantId") Long variantId
    );

    /**
     * Lấy tất cả receipt items theo set_id (gộp tất cả receipt records)
     */
    @Query(
            value = """
                SELECT ri.*
                FROM receipt_items ri
                JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
                WHERE rr.set_id = :setId
            """,
            nativeQuery = true
    )
    List<ReceiptItem> findAllBySetId(@Param("setId") Long setId);

    void deleteByReceiptId(Long receiptId);

    /**
     * Lịch sử nhận hàng cho 1 variant cụ thể trong 1 request
     * (mỗi row = 1 lần nhận)
     */
    @Query(
            value = """
                SELECT
                    ri.receipt_id       AS receiptId,
                    rr.received_at      AS receivedAt,
                    u.full_name         AS receivedByName,
                    ri.received_quantity AS receivedQuantity
                FROM receipt_items ri
                JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
                JOIN users u            ON u.user_id = rr.received_by
                WHERE rr.set_id = :setId
                  AND ri.request_id = :requestId
                  AND ri.variant_id = :variantId
                ORDER BY rr.received_at ASC
            """,
            nativeQuery = true
    )
    List<ReceiptEntryProjection> findReceiptHistoryByVariant(
            @Param("setId") Long setId,
            @Param("requestId") Long requestId,
            @Param("variantId") Long variantId
    );
}
