package manage.store.inventory.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.exception.BusinessException;
import manage.store.inventory.exception.ResourceNotFoundException;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.ProductVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
    }

    @Override
    public List<Product> getChildProducts(Long parentId) {
        productRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm cha không tồn tại"));
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
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm cha không tồn tại"));

        // Tìm sibling đầu tiên có variants để clone
        List<Product> siblings = productRepository.findByParentProductId(parentId);
        Product sibling = siblings.stream()
                .filter(s -> !variantRepository.findByProductId(s.getProductId()).isEmpty())
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "Không tìm thấy sản phẩm cùng cấp có biến thể để sao chép"));

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

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }
}
