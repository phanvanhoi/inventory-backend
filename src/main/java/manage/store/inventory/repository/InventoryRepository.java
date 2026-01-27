package manage.store.inventory.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import manage.store.inventory.dto.InventoryBalanceDTO;
import manage.store.inventory.dto.InventoryRequestHistoryDTO;
import manage.store.inventory.entity.InventoryRequestItem;

public interface InventoryRepository
        extends JpaRepository<InventoryRequestItem, Long> {

    /**
     * Lấy tồn kho tổng hợp theo biến thể cho một product cụ thể
     * - actualQuantity: Tồn kho thực tế = SUM(IN) - SUM(OUT) từ EXECUTED
     * - expectedQuantity: Tồn kho dự kiến = actualQuantity + SUM(ADJUST_IN) - SUM(ADJUST_OUT) từ PENDING/APPROVED/EXECUTED
     */
    @Query(
            value = """
        SELECT
          s.style_name      AS styleName,
          sz.size_value     AS sizeValue,
          lt.code           AS lengthCode,
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
          ), 0) AS actualQuantity,
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
          ), 0)
          +
          COALESCE((
            SELECT SUM(CASE
                WHEN r2.request_type = 'ADJUST_IN' THEN i2.quantity
                WHEN r2.request_type = 'ADJUST_OUT' THEN -i2.quantity
                ELSE 0
            END)
            FROM inventory_request_items i2
            JOIN inventory_requests r2 ON r2.request_id = i2.request_id
            JOIN request_sets rs2 ON rs2.set_id = r2.set_id
            WHERE i2.variant_id = pv.variant_id
              AND r2.product_id = :productId
              AND rs2.status IN ('PENDING', 'APPROVED', 'EXECUTED')
          ), 0) AS expectedQuantity
        FROM product_variants pv
        JOIN styles s                ON s.style_id = pv.style_id
        JOIN sizes sz                ON sz.size_id = pv.size_id
        JOIN length_types lt         ON lt.length_type_id = pv.length_type_id
        GROUP BY
          pv.variant_id,
          s.style_name,
          sz.size_value,
          lt.code
        HAVING actualQuantity <> 0 OR expectedQuantity <> 0
        ORDER BY
          s.style_name,
          sz.size_value,
          lt.code
      """,
            nativeQuery = true
    )
    List<InventoryBalanceDTO> getInventoryByProductId(@Param("productId") Long productId);

    /**
     * Lấy lịch sử các requests theo productId và styleName
     * Dành cho ADMIN, PURCHASER: Xem cả APPROVED và EXECUTED
     */
    @Query(
            value = """
        SELECT
            r.request_id AS requestId,
            rs.set_id AS setId,
            rs.set_name AS setName,
            rs.status AS setStatus,
            u.unit_name AS unitName,
            r.request_type AS requestType,
            sz.size_value AS sizeValue,
            lt.code AS lengthCode,
            i.quantity AS quantity,
            r.note AS note,
            r.created_at AS createdAt
        FROM inventory_request_items i
        JOIN inventory_requests r ON r.request_id = i.request_id
        JOIN request_sets rs ON rs.set_id = r.set_id
        JOIN units u ON u.unit_id = r.unit_id
        JOIN product_variants pv ON pv.variant_id = i.variant_id
        JOIN styles s ON s.style_id = pv.style_id
        JOIN sizes sz ON sz.size_id = pv.size_id
        JOIN length_types lt ON lt.length_type_id = pv.length_type_id
        WHERE r.product_id = :productId
          AND s.style_name = :styleName
          AND rs.status IN ('APPROVED', 'EXECUTED')
        ORDER BY r.created_at DESC, sz.size_value, lt.code
        """,
            nativeQuery = true
    )
    List<InventoryRequestHistoryDTO> getRequestHistoryByProductAndStyle(
            @Param("productId") Long productId,
            @Param("styleName") String styleName
    );

    /**
     * Lấy lịch sử các requests theo productId và styleName
     * Dành cho USER, STOCKKEEPER: Chỉ xem EXECUTED (đã thực hiện thực tế)
     */
    @Query(
            value = """
        SELECT
            r.request_id AS requestId,
            rs.set_id AS setId,
            rs.set_name AS setName,
            rs.status AS setStatus,
            u.unit_name AS unitName,
            r.request_type AS requestType,
            sz.size_value AS sizeValue,
            lt.code AS lengthCode,
            i.quantity AS quantity,
            r.note AS note,
            r.created_at AS createdAt
        FROM inventory_request_items i
        JOIN inventory_requests r ON r.request_id = i.request_id
        JOIN request_sets rs ON rs.set_id = r.set_id
        JOIN units u ON u.unit_id = r.unit_id
        JOIN product_variants pv ON pv.variant_id = i.variant_id
        JOIN styles s ON s.style_id = pv.style_id
        JOIN sizes sz ON sz.size_id = pv.size_id
        JOIN length_types lt ON lt.length_type_id = pv.length_type_id
        WHERE r.product_id = :productId
          AND s.style_name = :styleName
          AND rs.status = 'EXECUTED'
        ORDER BY r.created_at DESC, sz.size_value, lt.code
        """,
            nativeQuery = true
    )
    List<InventoryRequestHistoryDTO> getRequestHistoryByProductAndStyleExecutedOnly(
            @Param("productId") Long productId,
            @Param("styleName") String styleName
    );

    /**
     * Lấy tồn kho thực tế theo variant
     * Chỉ tính IN, OUT từ các request_sets đã được EXECUTED
     */
    @Query(
            value = """
        SELECT COALESCE(SUM(CASE
            WHEN r.request_type = 'IN' THEN i.quantity
            WHEN r.request_type = 'OUT' THEN -i.quantity
            ELSE 0
        END), 0)
        FROM inventory_request_items i
        JOIN inventory_requests r ON r.request_id = i.request_id
        JOIN request_sets rs ON rs.set_id = r.set_id
        WHERE i.variant_id = :variantId
          AND r.product_id = :productId
          AND rs.status = 'EXECUTED'
        """,
            nativeQuery = true
    )
    Integer getActualQuantityByVariant(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId
    );

    /**
     * Lấy tồn kho dự kiến tại một ngày cụ thể theo variant
     * Công thức: Tồn thực tế (EXECUTED) + SUM(ADJUST_IN có expected_date <= targetDate) - SUM(ADJUST_OUT có expected_date <= targetDate)
     * Tính cả PENDING, APPROVED và EXECUTED
     */
    @Query(
            value = """
        SELECT COALESCE(
            (SELECT SUM(CASE
                WHEN r.request_type = 'IN' THEN i.quantity
                WHEN r.request_type = 'OUT' THEN -i.quantity
                ELSE 0
            END)
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            WHERE i.variant_id = :variantId
              AND r.product_id = :productId
              AND rs.status = 'EXECUTED'), 0)
        +
        COALESCE(
            (SELECT SUM(CASE
                WHEN r.request_type = 'ADJUST_IN' THEN i.quantity
                WHEN r.request_type = 'ADJUST_OUT' THEN -i.quantity
                ELSE 0
            END)
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            WHERE i.variant_id = :variantId
              AND r.product_id = :productId
              AND r.expected_date <= :targetDate
              AND rs.status IN ('PENDING', 'APPROVED', 'EXECUTED')), 0)
        """,
            nativeQuery = true
    )
    Integer getExpectedQuantityByVariantAtDate(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("targetDate") LocalDate targetDate
    );

    /**
     * Kiểm tra có ADJUST_OUT nào phụ thuộc vào ADJUST_IN không
     * (ADJUST_OUT có expected_date >= ngày của ADJUST_IN)
     */
    @Query(
            value = """
        SELECT COUNT(*)
        FROM inventory_requests r
        JOIN request_sets rs ON rs.set_id = r.set_id
        WHERE r.product_id = :productId
          AND r.request_type = 'ADJUST_OUT'
          AND r.expected_date >= :fromDate
          AND rs.status IN ('PENDING', 'APPROVED', 'EXECUTED')
          AND r.request_id != :excludeRequestId
        """,
            nativeQuery = true
    )
    Integer countDependentAdjustOut(
            @Param("productId") Long productId,
            @Param("fromDate") LocalDate fromDate,
            @Param("excludeRequestId") Long excludeRequestId
    );
}
