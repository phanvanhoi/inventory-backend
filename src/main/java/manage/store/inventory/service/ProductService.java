package manage.store.inventory.service;

import java.util.List;

import manage.store.inventory.entity.Product;

public interface ProductService {

    List<Product> getAllProducts();

    Product getProductById(Long id);

    List<Product> getChildProducts(Long parentId);

    boolean isParentProduct(Long productId);

    Product createChildProduct(Long parentId, String productName, String note);
}
