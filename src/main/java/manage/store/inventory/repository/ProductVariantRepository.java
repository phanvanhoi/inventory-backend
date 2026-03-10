package manage.store.inventory.repository;

import manage.store.inventory.entity.ProductVariant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    /**
     * Lookup STRUCTURED variant: style + size + length (Sơ mi nam)
     */
    @Query("""
        SELECT pv FROM ProductVariant pv
        JOIN Size s ON s.sizeId = pv.sizeId
        JOIN LengthType lt ON lt.lengthTypeId = pv.lengthTypeId
        WHERE pv.productId = :productId
          AND pv.styleId = :styleId
          AND s.sizeValue = :sizeValue
          AND lt.code = :lengthCode
    """)
    Optional<ProductVariant> findStructuredVariantWithStyle(
        @Param("productId") Long productId,
        @Param("styleId") Long styleId,
        @Param("sizeValue") String sizeValue,
        @Param("lengthCode") String lengthCode
    );

    /**
     * Lookup STRUCTURED variant: size + gender + length (Áo phông)
     */
    @Query("""
        SELECT pv FROM ProductVariant pv
        JOIN Size s ON s.sizeId = pv.sizeId
        JOIN LengthType lt ON lt.lengthTypeId = pv.lengthTypeId
        WHERE pv.productId = :productId
          AND s.sizeValue = :sizeValue
          AND lt.code = :lengthCode
          AND pv.gender = :gender
    """)
    Optional<ProductVariant> findStructuredVariantWithGenderAndLength(
        @Param("productId") Long productId,
        @Param("sizeValue") String sizeValue,
        @Param("lengthCode") String lengthCode,
        @Param("gender") manage.store.inventory.entity.enums.Gender gender
    );

    /**
     * Lookup STRUCTURED variant: size + gender (Áo khoác, Áo len, Gile BH)
     */
    @Query("""
        SELECT pv FROM ProductVariant pv
        JOIN Size s ON s.sizeId = pv.sizeId
        WHERE pv.productId = :productId
          AND s.sizeValue = :sizeValue
          AND pv.gender = :gender
          AND pv.lengthTypeId IS NULL
    """)
    Optional<ProductVariant> findStructuredVariantWithGender(
        @Param("productId") Long productId,
        @Param("sizeValue") String sizeValue,
        @Param("gender") manage.store.inventory.entity.enums.Gender gender
    );

    /**
     * Lookup ITEM_BASED variant by item_code
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.productId = :productId AND pv.itemCode = :itemCode")
    Optional<ProductVariant> findByItemCode(
        @Param("productId") Long productId,
        @Param("itemCode") String itemCode
    );

    /**
     * Get all variants for a product (for building UI dropdowns, etc.)
     */
    List<ProductVariant> findByProductId(Long productId);

    /**
     * Backward-compatible: old findVariant for existing Sơ mi data
     */
    @Query("""
        SELECT pv FROM ProductVariant pv
        JOIN Size s ON s.sizeId = pv.sizeId
        JOIN LengthType lt ON lt.lengthTypeId = pv.lengthTypeId
        WHERE pv.styleId = :styleId
          AND s.sizeValue = :sizeValue
          AND lt.code = :lengthCode
    """)
    Optional<ProductVariant> findVariant(
        @Param("styleId") Long styleId,
        @Param("sizeValue") String sizeValue,
        @Param("lengthCode") String lengthCode
    );
}
