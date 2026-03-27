package manage.store.inventory.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.access.prepost.PreAuthorize;

import manage.store.inventory.dto.ChildProductCreateDTO;
import manage.store.inventory.dto.ItemVariantCreateDTO;
import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductRepository productRepository;

    public ProductController(ProductService productService,
                             ProductRepository productRepository) {
        this.productService = productService;
        this.productRepository = productRepository;
    }

    // Lấy tất cả sản phẩm (có phân trang)
    @GetMapping
    public Page<Product> getAllProducts(
            @PageableDefault(size = 20, sort = "productId") Pageable pageable
    ) {
        return productService.getAllProducts(pageable);
    }

    // Lấy sản phẩm theo ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // Lấy danh sách child products của parent
    @GetMapping("/{id}/children")
    public ResponseEntity<List<Product>> getChildProducts(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getChildProducts(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Kiểm tra product có phải parent không
    @GetMapping("/{id}/is-parent")
    public ResponseEntity<Boolean> isParentProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.isParentProduct(id));
    }

    // Tạo child product (clone variants từ sibling)
    @PostMapping("/{parentId}/children")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('PURCHASER')")
    public ResponseEntity<Product> createChildProduct(
            @PathVariable Long parentId,
            @RequestBody ChildProductCreateDTO dto) {
        try {
            Product child = productService.createChildProduct(
                    parentId, dto.getProductName(), dto.getNote());
            return ResponseEntity.ok(child);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Thêm mã hàng mới vào sản phẩm ITEM_BASED
    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('PURCHASER')")
    public ResponseEntity<ProductVariant> createItemVariant(
            @PathVariable Long productId,
            @RequestBody ItemVariantCreateDTO dto) {
        return ResponseEntity.ok(productService.createItemVariant(productId, dto));
    }

    // Cập nhật sản phẩm
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    product.setProductName(productDetails.getProductName());
                    product.setNote(productDetails.getNote());
                    product.setMinStock(productDetails.getMinStock());
                    return ResponseEntity.ok(productRepository.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa sản phẩm (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
