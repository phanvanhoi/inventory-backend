package manage.store.inventory.repository;

import java.math.BigDecimal;
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
     * Hỗ trợ cả STRUCTURED (style/size/length/gender) và ITEM_BASED (item_code/item_name/unit)
     * - actualQuantity: SUM(IN/OUT) từ EXECUTED + SUM(receipt_items) từ RECEIVING
     * - expectedQuantity: EXECUTED(IN/OUT) + ADJUST(PENDING/APPROVED/RECEIVING)
     */
    @Query(
            value = """
        SELECT
          pv.variant_id     AS variantId,
          s.style_name      AS styleName,
          sz.size_value     AS sizeValue,
          lt.code           AS lengthCode,
          pv.gender         AS gender,
          pv.item_code      AS itemCode,
          pv.item_name      AS itemName,
          pv.unit           AS unit,
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
              AND (rs2.status = 'EXECUTED'
                OR (rs2.status = 'RECEIVING' AND r2.request_status = 'COMPLETED'))
          ), 0)
          +
          COALESCE((
            SELECT SUM(CASE
                WHEN r2.request_type IN ('IN', 'ADJUST_IN') THEN ri2.received_quantity
                WHEN r2.request_type IN ('OUT', 'ADJUST_OUT') THEN -ri2.received_quantity
                ELSE 0
            END)
            FROM receipt_items ri2
            JOIN receipt_records rr2 ON rr2.receipt_id = ri2.receipt_id
            JOIN inventory_requests r2 ON r2.request_id = ri2.request_id
            JOIN request_sets rs2 ON rs2.set_id = rr2.set_id
            WHERE ri2.variant_id = pv.variant_id
              AND r2.product_id = :productId
              AND rs2.status = 'RECEIVING'
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
              AND (rs2.status = 'EXECUTED'
                OR (rs2.status = 'RECEIVING' AND r2.request_status = 'COMPLETED'))
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
              AND rs2.status IN ('PENDING', 'APPROVED', 'RECEIVING')
          ), 0) AS expectedQuantity
        FROM product_variants pv
        LEFT JOIN styles s          ON s.style_id = pv.style_id
        LEFT JOIN sizes sz          ON sz.size_id = pv.size_id
        LEFT JOIN length_types lt   ON lt.length_type_id = pv.length_type_id
        WHERE pv.product_id = :productId
        GROUP BY
          pv.variant_id,
          s.style_name,
          sz.size_value,
          lt.code,
          pv.gender,
          pv.item_code,
          pv.item_name,
          pv.unit
        ORDER BY
          s.style_name,
          COALESCE(sz.size_order, 0),
          sz.size_value,
          lt.code,
          pv.gender,
          pv.item_code
      """,
            nativeQuery = true
    )
    List<InventoryBalanceDTO> getInventoryByProductId(@Param("productId") Long productId);

    /**
     * Lấy tồn kho theo product + warehouse
     * warehouseId = null → tất cả kho (tổng hợp), warehouseId != null → filter theo kho
     */
    @Query(
            value = """
        SELECT
          pv.variant_id     AS variantId,
          s.style_name      AS styleName,
          sz.size_value     AS sizeValue,
          lt.code           AS lengthCode,
          pv.gender         AS gender,
          pv.item_code      AS itemCode,
          pv.item_name      AS itemName,
          pv.unit           AS unit,
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
              AND r2.warehouse_id = :warehouseId
              AND (rs2.status = 'EXECUTED'
                OR (rs2.status = 'RECEIVING' AND r2.request_status = 'COMPLETED'))
          ), 0)
          +
          COALESCE((
            SELECT SUM(CASE
                WHEN r2.request_type IN ('IN', 'ADJUST_IN') THEN ri2.received_quantity
                WHEN r2.request_type IN ('OUT', 'ADJUST_OUT') THEN -ri2.received_quantity
                ELSE 0
            END)
            FROM receipt_items ri2
            JOIN receipt_records rr2 ON rr2.receipt_id = ri2.receipt_id
            JOIN inventory_requests r2 ON r2.request_id = ri2.request_id
            JOIN request_sets rs2 ON rs2.set_id = rr2.set_id
            WHERE ri2.variant_id = pv.variant_id
              AND r2.product_id = :productId
              AND r2.warehouse_id = :warehouseId
              AND rs2.status = 'RECEIVING'
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
              AND r2.warehouse_id = :warehouseId
              AND (rs2.status = 'EXECUTED'
                OR (rs2.status = 'RECEIVING' AND r2.request_status = 'COMPLETED'))
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
              AND r2.warehouse_id = :warehouseId
              AND rs2.status IN ('PENDING', 'APPROVED', 'RECEIVING')
          ), 0) AS expectedQuantity
        FROM product_variants pv
        LEFT JOIN styles s          ON s.style_id = pv.style_id
        LEFT JOIN sizes sz          ON sz.size_id = pv.size_id
        LEFT JOIN length_types lt   ON lt.length_type_id = pv.length_type_id
        WHERE pv.product_id = :productId
        GROUP BY
          pv.variant_id,
          s.style_name,
          sz.size_value,
          lt.code,
          pv.gender,
          pv.item_code,
          pv.item_name,
          pv.unit
        ORDER BY
          s.style_name,
          COALESCE(sz.size_order, 0),
          sz.size_value,
          lt.code,
          pv.gender,
          pv.item_code
      """,
            nativeQuery = true
    )
    List<InventoryBalanceDTO> getInventoryByProductIdAndWarehouse(
            @Param("productId") Long productId,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Lấy lịch sử các requests theo productId và filter value (tất cả kho)
     * Dành cho ADMIN, PURCHASER: Xem cả APPROVED, RECEIVING và EXECUTED
     * filterValue = styleName (STRUCTURED with style) hoặc gender (STRUCTURED with gender)
     */
    @Query(
            value = """
        SELECT * FROM (
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                r.request_type AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                i.quantity AS quantity,
                i.worker_note AS workerNote,
                i.fabric_note AS fabricNote,
                r.note AS note,
                r.created_at AS createdAt,
                rs.created_by AS createdBy,
                u.full_name AS createdByName
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u ON u.user_id = rs.created_by
            JOIN product_variants pv ON pv.variant_id = i.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND (rs.status = 'EXECUTED'
                OR (rs.status IN ('APPROVED', 'RECEIVING') AND r.request_status = 'COMPLETED'))
            UNION ALL
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                CONCAT('RECEIPT_', IF(r.request_type IN ('IN','ADJUST_IN'), 'IN', 'OUT')) AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                ri.received_quantity AS quantity,
                NULL AS workerNote,
                NULL AS fabricNote,
                rr.note AS note,
                rr.received_at AS createdAt,
                rr.received_by AS createdBy,
                u2.full_name AS createdByName
            FROM receipt_items ri
            JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
            JOIN inventory_requests r ON r.request_id = ri.request_id
            JOIN request_sets rs ON rs.set_id = rr.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u2 ON u2.user_id = rr.received_by
            JOIN product_variants pv ON pv.variant_id = ri.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND rs.status IN ('RECEIVING', 'EXECUTED')
        ) combined
        ORDER BY createdAt DESC, sizeValue, lengthCode
        """,
            nativeQuery = true
    )
    List<InventoryRequestHistoryDTO> getRequestHistoryByProductAndStyle(
            @Param("productId") Long productId,
            @Param("filterValue") String filterValue
    );

    /**
     * Lấy lịch sử các requests theo productId, filter value và warehouse
     * Dành cho ADMIN, PURCHASER: Xem cả APPROVED, RECEIVING và EXECUTED
     */
    @Query(
            value = """
        SELECT * FROM (
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                r.request_type AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                i.quantity AS quantity,
                i.worker_note AS workerNote,
                i.fabric_note AS fabricNote,
                r.note AS note,
                r.created_at AS createdAt,
                rs.created_by AS createdBy,
                u.full_name AS createdByName
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u ON u.user_id = rs.created_by
            JOIN product_variants pv ON pv.variant_id = i.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND r.warehouse_id = :warehouseId
              AND (rs.status = 'EXECUTED'
                OR (rs.status IN ('APPROVED', 'RECEIVING') AND r.request_status = 'COMPLETED'))
            UNION ALL
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                CONCAT('RECEIPT_', IF(r.request_type IN ('IN','ADJUST_IN'), 'IN', 'OUT')) AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                ri.received_quantity AS quantity,
                NULL AS workerNote,
                NULL AS fabricNote,
                rr.note AS note,
                rr.received_at AS createdAt,
                rr.received_by AS createdBy,
                u2.full_name AS createdByName
            FROM receipt_items ri
            JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
            JOIN inventory_requests r ON r.request_id = ri.request_id
            JOIN request_sets rs ON rs.set_id = rr.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u2 ON u2.user_id = rr.received_by
            JOIN product_variants pv ON pv.variant_id = ri.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND r.warehouse_id = :warehouseId
              AND rs.status IN ('RECEIVING', 'EXECUTED')
        ) combined
        ORDER BY createdAt DESC, sizeValue, lengthCode
        """,
            nativeQuery = true
    )
    List<InventoryRequestHistoryDTO> getRequestHistoryByProductAndStyleAndWarehouse(
            @Param("productId") Long productId,
            @Param("filterValue") String filterValue,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Lấy lịch sử các requests theo productId và filter value (tất cả kho)
     * Dành cho USER, STOCKKEEPER: Xem EXECUTED + RECEIVING
     */
    @Query(
            value = """
        SELECT * FROM (
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                r.request_type AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                i.quantity AS quantity,
                i.worker_note AS workerNote,
                i.fabric_note AS fabricNote,
                r.note AS note,
                r.created_at AS createdAt,
                rs.created_by AS createdBy,
                u.full_name AS createdByName
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u ON u.user_id = rs.created_by
            JOIN product_variants pv ON pv.variant_id = i.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND (rs.status = 'EXECUTED'
                OR (rs.status = 'RECEIVING' AND r.request_status = 'COMPLETED'))
            UNION ALL
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                CONCAT('RECEIPT_', IF(r.request_type IN ('IN','ADJUST_IN'), 'IN', 'OUT')) AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                ri.received_quantity AS quantity,
                NULL AS workerNote,
                NULL AS fabricNote,
                rr.note AS note,
                rr.received_at AS createdAt,
                rr.received_by AS createdBy,
                u2.full_name AS createdByName
            FROM receipt_items ri
            JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
            JOIN inventory_requests r ON r.request_id = ri.request_id
            JOIN request_sets rs ON rs.set_id = rr.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u2 ON u2.user_id = rr.received_by
            JOIN product_variants pv ON pv.variant_id = ri.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND rs.status IN ('RECEIVING', 'EXECUTED')
        ) combined
        ORDER BY createdAt DESC, sizeValue, lengthCode
        """,
            nativeQuery = true
    )
    List<InventoryRequestHistoryDTO> getRequestHistoryByProductAndStyleExecutedOnly(
            @Param("productId") Long productId,
            @Param("filterValue") String filterValue
    );

    /**
     * Lấy lịch sử các requests theo productId, filter value và warehouse
     * Dành cho USER, STOCKKEEPER: Xem EXECUTED + RECEIVING
     */
    @Query(
            value = """
        SELECT * FROM (
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                r.request_type AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                i.quantity AS quantity,
                i.worker_note AS workerNote,
                i.fabric_note AS fabricNote,
                r.note AS note,
                r.created_at AS createdAt,
                rs.created_by AS createdBy,
                u.full_name AS createdByName
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u ON u.user_id = rs.created_by
            JOIN product_variants pv ON pv.variant_id = i.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND r.warehouse_id = :warehouseId
              AND (rs.status = 'EXECUTED'
                OR (rs.status = 'RECEIVING' AND r.request_status = 'COMPLETED'))
            UNION ALL
            SELECT
                r.request_id AS requestId,
                rs.set_id AS setId,
                rs.set_name AS setName,
                rs.status AS setStatus,
                un.unit_name AS unitName,
                CONCAT('RECEIPT_', IF(r.request_type IN ('IN','ADJUST_IN'), 'IN', 'OUT')) AS requestType,
                pv.variant_id AS variantId,
                s.style_name AS styleName,
                sz.size_value AS sizeValue,
                lt.code AS lengthCode,
                pv.gender AS gender,
                pv.item_code AS itemCode,
                pv.item_name AS itemName,
                pv.unit AS unit,
                ri.received_quantity AS quantity,
                NULL AS workerNote,
                NULL AS fabricNote,
                rr.note AS note,
                rr.received_at AS createdAt,
                rr.received_by AS createdBy,
                u2.full_name AS createdByName
            FROM receipt_items ri
            JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
            JOIN inventory_requests r ON r.request_id = ri.request_id
            JOIN request_sets rs ON rs.set_id = rr.set_id
            LEFT JOIN units un ON un.unit_id = r.unit_id
            LEFT JOIN users u2 ON u2.user_id = rr.received_by
            JOIN product_variants pv ON pv.variant_id = ri.variant_id
            LEFT JOIN styles s ON s.style_id = pv.style_id
            LEFT JOIN sizes sz ON sz.size_id = pv.size_id
            LEFT JOIN length_types lt ON lt.length_type_id = pv.length_type_id
            WHERE r.product_id = :productId
              AND (s.style_name = :filterValue OR pv.gender = :filterValue OR :filterValue IS NULL)
              AND r.warehouse_id = :warehouseId
              AND rs.status IN ('RECEIVING', 'EXECUTED')
        ) combined
        ORDER BY createdAt DESC, sizeValue, lengthCode
        """,
            nativeQuery = true
    )
    List<InventoryRequestHistoryDTO> getRequestHistoryByProductAndStyleExecutedOnlyAndWarehouse(
            @Param("productId") Long productId,
            @Param("filterValue") String filterValue,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Lấy tồn kho thực tế theo variant
     * = SUM(IN/OUT) từ EXECUTED + SUM(receipt_items) từ RECEIVING
     */
    @Query(
            value = """
        SELECT
          COALESCE((
            SELECT SUM(CASE
                WHEN r.request_type = 'IN' THEN i.quantity
                WHEN r.request_type = 'OUT' THEN -i.quantity
                ELSE 0
            END)
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            WHERE i.variant_id = :variantId
              AND r.product_id = :productId
              AND (rs.status = 'EXECUTED'
                OR (rs.status = 'RECEIVING' AND r.request_status = 'COMPLETED'))
          ), 0)
          +
          COALESCE((
            SELECT SUM(CASE
                WHEN r.request_type IN ('IN', 'ADJUST_IN') THEN ri.received_quantity
                WHEN r.request_type IN ('OUT', 'ADJUST_OUT') THEN -ri.received_quantity
                ELSE 0
            END)
            FROM receipt_items ri
            JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
            JOIN inventory_requests r ON r.request_id = ri.request_id
            JOIN request_sets rs ON rs.set_id = rr.set_id
            WHERE ri.variant_id = :variantId
              AND r.product_id = :productId
              AND rs.status = 'RECEIVING'
          ), 0)
        """,
            nativeQuery = true
    )
    BigDecimal getActualQuantityByVariant(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId
    );

    /**
     * Lấy tồn kho thực tế theo variant + warehouse
     */
    @Query(
            value = """
        SELECT
          COALESCE((
            SELECT SUM(CASE
                WHEN r.request_type = 'IN' THEN i.quantity
                WHEN r.request_type = 'OUT' THEN -i.quantity
                ELSE 0
            END)
            FROM inventory_request_items i
            JOIN inventory_requests r ON r.request_id = i.request_id
            JOIN request_sets rs ON rs.set_id = r.set_id
            WHERE i.variant_id = :variantId
              AND r.product_id = :productId
              AND r.warehouse_id = :warehouseId
              AND (rs.status = 'EXECUTED'
                OR (rs.status = 'RECEIVING' AND r.request_status = 'COMPLETED'))
          ), 0)
          +
          COALESCE((
            SELECT SUM(CASE
                WHEN r.request_type IN ('IN', 'ADJUST_IN') THEN ri.received_quantity
                WHEN r.request_type IN ('OUT', 'ADJUST_OUT') THEN -ri.received_quantity
                ELSE 0
            END)
            FROM receipt_items ri
            JOIN receipt_records rr ON rr.receipt_id = ri.receipt_id
            JOIN inventory_requests r ON r.request_id = ri.request_id
            JOIN request_sets rs ON rs.set_id = rr.set_id
            WHERE ri.variant_id = :variantId
              AND r.product_id = :productId
              AND r.warehouse_id = :warehouseId
              AND rs.status = 'RECEIVING'
          ), 0)
        """,
            nativeQuery = true
    )
    BigDecimal getActualQuantityByVariantAndWarehouse(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Lấy tồn kho dự kiến tại một ngày cụ thể theo variant + warehouse
     */
    @Query(
            value = """
        SELECT
          COALESCE(
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
              AND r.warehouse_id = :warehouseId
              AND (rs.status = 'EXECUTED'
                OR (rs.status = 'RECEIVING' AND r.request_status = 'COMPLETED'))), 0)
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
              AND r.warehouse_id = :warehouseId
              AND r.expected_date <= :targetDate
              AND rs.status IN ('PENDING', 'APPROVED', 'RECEIVING')), 0)
        """,
            nativeQuery = true
    )
    BigDecimal getExpectedQuantityByVariantAtDateAndWarehouse(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("targetDate") LocalDate targetDate,
            @Param("warehouseId") Long warehouseId
    );

    /**
     * Lấy tồn kho dự kiến tại một ngày cụ thể theo variant
     * = EXECUTED(IN/OUT) + SUM(ADJUST có expected_date <= targetDate) từ PENDING/APPROVED/RECEIVING
     */
    @Query(
            value = """
        SELECT
          COALESCE(
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
              AND (rs.status = 'EXECUTED'
                OR (rs.status = 'RECEIVING' AND r.request_status = 'COMPLETED'))), 0)
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
              AND rs.status IN ('PENDING', 'APPROVED', 'RECEIVING')), 0)
        """,
            nativeQuery = true
    )
    BigDecimal getExpectedQuantityByVariantAtDate(
            @Param("productId") Long productId,
            @Param("variantId") Long variantId,
            @Param("targetDate") LocalDate targetDate
    );

    /**
     * Kiểm tra có ADJUST_OUT nào phụ thuộc vào ADJUST_IN không
     */
    @Query(
            value = """
        SELECT COUNT(*)
        FROM inventory_requests r
        JOIN request_sets rs ON rs.set_id = r.set_id
        WHERE r.product_id = :productId
          AND r.request_type = 'ADJUST_OUT'
          AND r.expected_date >= :fromDate
          AND rs.status IN ('PENDING', 'APPROVED', 'RECEIVING', 'EXECUTED')
          AND r.request_id != :excludeRequestId
        """,
            nativeQuery = true
    )
    Integer countDependentAdjustOut(
            @Param("productId") Long productId,
            @Param("fromDate") LocalDate fromDate,
            @Param("excludeRequestId") Long excludeRequestId
    );

    /**
     * Báo cáo xuất nhập tổng hợp theo sản phẩm, date range, warehouse
     */
    @Query(value = """
        SELECT
            p.product_id AS productId,
            p.product_name AS productName,
            COALESCE(SUM(CASE WHEN r.request_type IN ('IN', 'ADJUST_IN') THEN i.quantity ELSE 0 END), 0) AS totalIn,
            COALESCE(SUM(CASE WHEN r.request_type IN ('OUT', 'ADJUST_OUT') THEN i.quantity ELSE 0 END), 0) AS totalOut,
            COALESCE(SUM(CASE WHEN r.request_type IN ('IN', 'ADJUST_IN') THEN i.quantity
                              WHEN r.request_type IN ('OUT', 'ADJUST_OUT') THEN -i.quantity
                              ELSE 0 END), 0) AS netQuantity,
            COUNT(DISTINCT rs.set_id) AS transactionCount
        FROM inventory_request_items i
        JOIN inventory_requests r ON r.request_id = i.request_id
        JOIN request_sets rs ON rs.set_id = r.set_id
        JOIN products p ON p.product_id = r.product_id
        WHERE (rs.status = 'EXECUTED'
            OR (rs.status IN ('APPROVED', 'RECEIVING') AND r.request_status = 'COMPLETED'))
          AND (:fromDate IS NULL OR DATE(rs.created_at) >= :fromDate)
          AND (:toDate IS NULL OR DATE(rs.created_at) <= :toDate)
          AND (:warehouseId IS NULL OR r.warehouse_id = :warehouseId)
        GROUP BY p.product_id, p.product_name
        ORDER BY p.product_name
    """, nativeQuery = true)
    List<InventoryReportProjection> getInventoryReport(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("warehouseId") Long warehouseId
    );

    interface InventoryReportProjection {
        Long getProductId();
        String getProductName();
        BigDecimal getTotalIn();
        BigDecimal getTotalOut();
        BigDecimal getNetQuantity();
        Long getTransactionCount();
    }
}
