package manage.store.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import manage.store.inventory.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByParentProductId(Long parentProductId);

    @Query("SELECT p FROM Product p WHERE p.parentProductId IS NULL")
    List<Product> findTopLevelProducts();

    boolean existsByParentProductId(Long parentProductId);

    @Query("""
        SELECT p FROM Product p
        WHERE p.productId NOT IN (
            SELECT DISTINCT c.parentProductId FROM Product c
            WHERE c.parentProductId IS NOT NULL
        )
    """)
    List<Product> findLeafProducts();
}
