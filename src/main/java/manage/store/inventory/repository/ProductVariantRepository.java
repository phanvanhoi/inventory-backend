package manage.store.inventory.repository;

import manage.store.inventory.entity.ProductVariant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

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
        @Param("sizeValue") Integer sizeValue,
        @Param("lengthCode") String lengthCode
    );
}
