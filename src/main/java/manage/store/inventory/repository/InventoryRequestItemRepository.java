package manage.store.inventory.repository;

import manage.store.inventory.dto.InventoryRequestItemDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.entity.InventoryRequestItem;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRequestItemRepository
        extends JpaRepository<InventoryRequestItem, Long> {

    @Query("""
        SELECT new manage.store.inventory.dto.InventoryRequestItemDTO(
            s.styleName,
            sz.sizeValue,
            lt.code,
            i.quantity
        )
        FROM InventoryRequestItem i
        JOIN ProductVariant pv ON pv.variantId = i.variantId
        JOIN Style s           ON s.styleId = pv.styleId
        JOIN Size sz           ON sz.sizeId = pv.sizeId
        JOIN LengthType lt     ON lt.lengthTypeId = pv.lengthTypeId
        WHERE i.requestId = :requestId
        ORDER BY s.styleName, sz.sizeValue, lt.code
    """)
    List<InventoryRequestItemDTO> findItemsByRequestId(
            @Param("requestId") Long requestId
    );

    @Modifying
    @Query("DELETE FROM InventoryRequestItem i WHERE i.requestId = :requestId")
    void deleteByRequestId(@Param("requestId") Long requestId);

    @Query(
            value = """
                SELECT
                    i.item_id AS itemId,
                    i.request_id AS requestId,
                    i.variant_id AS variantId,
                    s.style_name AS styleName,
                    sz.size_value AS sizeValue,
                    lt.code AS lengthCode,
                    i.quantity AS quantity
                FROM inventory_request_items i
                JOIN product_variants pv ON pv.variant_id = i.variant_id
                JOIN styles s ON s.style_id = pv.style_id
                JOIN sizes sz ON sz.size_id = pv.size_id
                JOIN length_types lt ON lt.length_type_id = pv.length_type_id
                WHERE i.item_id = :itemId
            """,
            nativeQuery = true
    )
    Optional<ItemDetailDTO> findItemDetailById(@Param("itemId") Long itemId);

    @Query(
            value = """
                SELECT
                    i.item_id AS itemId,
                    i.request_id AS requestId,
                    i.variant_id AS variantId,
                    s.style_name AS styleName,
                    sz.size_value AS sizeValue,
                    lt.code AS lengthCode,
                    i.quantity AS quantity
                FROM inventory_request_items i
                JOIN product_variants pv ON pv.variant_id = i.variant_id
                JOIN styles s ON s.style_id = pv.style_id
                JOIN sizes sz ON sz.size_id = pv.size_id
                JOIN length_types lt ON lt.length_type_id = pv.length_type_id
                WHERE i.request_id = :requestId
                ORDER BY s.style_name, sz.size_value, lt.code
            """,
            nativeQuery = true
    )
    List<ItemDetailDTO> findItemDetailsByRequestId(@Param("requestId") Long requestId);
}
