package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.dto.ItemVariantCreateDTO;
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    List<Product> getAllProducts();
    Page<Product> getAllProducts(Pageable pageable);

    Product getProductById(Long id);

    List<Product> getChildProducts(Long parentId);

    boolean isParentProduct(Long productId);

    Product createChildProduct(Long parentId, String productName, String note);

    ProductVariant createItemVariant(Long productId, ItemVariantCreateDTO dto);

    void deleteProduct(Long id);
}
