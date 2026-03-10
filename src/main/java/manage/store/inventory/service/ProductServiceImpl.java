package manage.store.inventory.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.ProductVariantRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    @Override
    public List<Product> getChildProducts(Long parentId) {
        productRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent product not found: " + parentId));
        return productRepository.findByParentProductId(parentId);
    }

    @Override
    public boolean isParentProduct(Long productId) {
        return productRepository.existsByParentProductId(productId);
    }

    @Override
    @Transactional
    public Product createChildProduct(Long parentId, String productName, String note) {
        Product parent = productRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("Parent product not found: " + parentId));

        // Tìm sibling đầu tiên có variants để clone
        List<Product> siblings = productRepository.findByParentProductId(parentId);
        Product sibling = siblings.stream()
                .filter(s -> !variantRepository.findByProductId(s.getProductId()).isEmpty())
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No sibling with variants found for parent: " + parentId));

        // Tạo child product mới
        Product child = new Product();
        child.setProductName(productName);
        child.setVariantType(VariantType.STRUCTURED);
        child.setParentProductId(parentId);
        child.setNote(note);
        child = productRepository.save(child);

        // Clone variants từ sibling
        List<ProductVariant> siblingVariants = variantRepository.findByProductId(sibling.getProductId());
        for (ProductVariant sv : siblingVariants) {
            ProductVariant nv = new ProductVariant();
            nv.setProductId(child.getProductId());
            nv.setStyleId(sv.getStyleId());
            nv.setSizeId(sv.getSizeId());
            nv.setLengthTypeId(sv.getLengthTypeId());
            nv.setGender(sv.getGender());
            variantRepository.save(nv);
        }

        return child;
    }
}
