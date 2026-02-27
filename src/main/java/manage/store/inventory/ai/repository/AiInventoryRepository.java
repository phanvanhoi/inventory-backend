package manage.store.inventory.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import manage.store.inventory.entity.InventoryRequestItem;

public interface AiInventoryRepository extends JpaRepository<InventoryRequestItem, Long> {

    // ==================== Projection Interfaces ====================

    interface AiBalanceProjection {
        Long getVariantId();
        String getStyleName();
        Integer getSizeValue();
        String getLengthCode();
        Integer getActualQuantity();
    }

    interface AiTransactionProjection {
        String getRequestType();
        Integer getQuantity();
        String getCreatedAt();
        String getNote();
        String getSetName();
        String getSetStatus();
    }

    interface AiUnitComparisonProjection {
        Long getUnitId();
        String getUnitName();
        Integer getTotalBalance();
        Integer getVariantCount();
    }

    // ==================== UC-01: Query Balance by Unit ====================

    /**
     * Truy vấn tồn kho theo đơn vị, có thể filter theo style/size/length.
     * Chỉ tính từ request_sets đã EXECUTED (tồn kho thực tế).
     */
    @Query(value = """
            SELECT
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                COALESCE((
                    SELECT SUM(CASE
                        WHEN r2.request_type = 'IN' THEN i2.quantity
                        WHEN r2.request_type = 'OUT' THEN -i2.quantity
                        ELSE 0
                    END)
                    FROM inventory_request_items i2
                    JOIN inventory_requests r2 ON r2.request_id = i2.request_id
                    JOIN request_sets rs2 ON rs2.set_id = r2.set_id
                    WHERE i2.variant_id = pv.variant_id
                      AND r2.product_id = :productId
                      AND r2.unit_id = :unitId
                      AND rs2.status = 'EXECUTED'
                ), 0) AS actualQuantity
            FROM product_variants pv
            JOIN styles s ON s.style_id = pv.style_id
            JOIN sizes sz ON sz.size_id = pv.size_id
            JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE (:styleName IS NULL OR s.style_name = :styleName)
              AND (:sizeValue IS NULL OR sz.size_value = :sizeValue)
              AND (:lengthCode IS NULL OR lt.code = :lengthCode)
            HAVING actualQuantity <> 0
            ORDER BY s.style_name, sz.size_value, lt.code
            """, nativeQuery = true)
    List<AiBalanceProjection> findBalanceByUnit(
            @Param("productId") Long productId,
            @Param("unitId") Long unitId,
            @Param("styleName") String styleName,
            @Param("sizeValue") Integer sizeValue,
            @Param("lengthCode") String lengthCode
    );

    // ==================== UC-02: Query Negative Balance ====================

    /**
     * Tìm biến thể có tồn kho âm tại một đơn vị cụ thể.
     */
    @Query(value = """
            SELECT
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                COALESCE((
                    SELECT SUM(CASE
                        WHEN r2.request_type = 'IN' THEN i2.quantity
                        WHEN r2.request_type = 'OUT' THEN -i2.quantity
                        ELSE 0
                    END)
                    FROM inventory_request_items i2
                    JOIN inventory_requests r2 ON r2.request_id = i2.request_id
                    JOIN request_sets rs2 ON rs2.set_id = r2.set_id
                    WHERE i2.variant_id = pv.variant_id
                      AND r2.product_id = :productId
                      AND r2.unit_id = :unitId
                      AND rs2.status = 'EXECUTED'
                ), 0) AS actualQuantity
            FROM product_variants pv
            JOIN styles s ON s.style_id = pv.style_id
            JOIN sizes sz ON sz.size_id = pv.size_id
            JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            HAVING actualQuantity < 0
            ORDER BY actualQuantity ASC
            """, nativeQuery = true)
    List<AiBalanceProjection> findNegativeBalanceByUnit(
            @Param("productId") Long productId,
            @Param("unitId") Long unitId
    );

    /**
     * Tìm biến thể có tồn kho âm trên toàn hệ thống (không filter unit).
     */
    @Query(value = """
            SELECT
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                COALESCE((
                    SELECT SUM(CASE
                        WHEN r2.request_type = 'IN' THEN i2.quantity
                        WHEN r2.request_type = 'OUT' THEN -i2.quantity
                        ELSE 0
                    END)
                    FROM inventory_request_items i2
                    JOIN inventory_requests r2 ON r2.request_id = i2.request_id
                    JOIN request_sets rs2 ON rs2.set_id = r2.set_id
                    WHERE i2.variant_id = pv.variant_id
                      AND r2.product_id = :productId
                      AND rs2.status = 'EXECUTED'
                ), 0) AS actualQuantity
            FROM product_variants pv
            JOIN styles s ON s.style_id = pv.style_id
            JOIN sizes sz ON sz.size_id = pv.size_id
            JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            HAVING actualQuantity < 0
            ORDER BY actualQuantity ASC
            """, nativeQuery = true)
    List<AiBalanceProjection> findNegativeBalanceGlobal(
            @Param("productId") Long productId
    );

    // ==================== UC-03: Explain Balance ====================

    /**
     * Lấy lịch sử giao dịch của một biến thể tại một đơn vị.
     * Dùng để giải thích vì sao tồn kho ở mức hiện tại.
     */
    @Query(value = """
            SELECT
                r.request_type AS requestType,
                i.quantity AS quantity,
                r.created_at AS createdAt,
                r.note AS note,
                rs.set_name AS setName,
                rs.status AS setStatus
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            WHERE r.unit_id = :unitId
              AND r.product_id = :productId
              AND i.variant_id = :variantId
              AND rs.status = 'EXECUTED'
            ORDER BY r.created_at ASC
            """, nativeQuery = true)
    List<AiTransactionProjection> findTransactionsByUnitAndVariant(
            @Param("productId") Long productId,
            @Param("unitId") Long unitId,
            @Param("variantId") Long variantId
    );

    // ==================== UC-04: Compare Units ====================

    /**
     * So sánh tồn kho tổng hợp giữa các đơn vị cho một product.
     */
    @Query(value = """
            SELECT
                u.unit_id AS unitId,
                u.unit_name AS unitName,
                COALESCE(SUM(CASE
                    WHEN r.request_type = 'IN' THEN i.quantity
                    WHEN r.request_type = 'OUT' THEN -i.quantity
                    ELSE 0
                END), 0) AS totalBalance,
                COUNT(DISTINCT i.variant_id) AS variantCount
            FROM units u
            LEFT JOIN inventory_requests r ON r.unit_id = u.unit_id AND r.product_id = :productId
            LEFT JOIN request_sets rs ON rs.set_id = r.set_id AND rs.status = 'EXECUTED'
            LEFT JOIN inventory_request_items i ON i.request_id = r.request_id AND rs.set_id IS NOT NULL
            GROUP BY u.unit_id, u.unit_name
            HAVING totalBalance <> 0
            ORDER BY totalBalance DESC
            """, nativeQuery = true)
    List<AiUnitComparisonProjection> findBalanceSummaryByAllUnits(
            @Param("productId") Long productId
    );
}
