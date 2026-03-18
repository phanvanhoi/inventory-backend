package manage.store.inventory.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.ProductVariant;
import manage.store.inventory.entity.enums.Gender;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.repository.ProductVariantRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product parentProduct;
    private Product childProduct;

    @BeforeEach
    void setUp() {
        parentProduct = new Product();
        parentProduct.setProductId(1L);
        parentProduct.setProductName("Sơ mi");
        parentProduct.setVariantType(VariantType.STRUCTURED);

        childProduct = new Product();
        childProduct.setProductId(2L);
        childProduct.setProductName("Sơ mi nam dài tay");
        childProduct.setVariantType(VariantType.STRUCTURED);
        childProduct.setParentProductId(1L);
    }

    // ==================== GET ALL ====================

    @Test
    @DisplayName("Lấy tất cả sản phẩm")
    void getAllProducts_returnsList() {
        when(productRepository.findAll()).thenReturn(List.of(parentProduct, childProduct));

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    // ==================== GET BY ID ====================

    @Test
    @DisplayName("Lấy sản phẩm theo ID thành công")
    void getProductById_found() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(parentProduct));

        Product result = productService.getProductById(1L);

        assertEquals("Sơ mi", result.getProductName());
    }

    @Test
    @DisplayName("Lấy sản phẩm theo ID - không tìm thấy")
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.getProductById(99L));
        assertTrue(ex.getMessage().contains("Product not found: 99"));
    }

    // ==================== GET CHILD PRODUCTS ====================

    @Test
    @DisplayName("Lấy danh sách sản phẩm con")
    void getChildProducts_returnsList() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(parentProduct));
        when(productRepository.findByParentProductId(1L)).thenReturn(List.of(childProduct));

        List<Product> result = productService.getChildProducts(1L);

        assertEquals(1, result.size());
        assertEquals("Sơ mi nam dài tay", result.get(0).getProductName());
    }

    @Test
    @DisplayName("Lấy sản phẩm con - parent không tồn tại")
    void getChildProducts_parentNotFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.getChildProducts(99L));
        assertTrue(ex.getMessage().contains("Parent product not found: 99"));
    }

    // ==================== IS PARENT PRODUCT ====================

    @Test
    @DisplayName("Kiểm tra là sản phẩm cha - có con")
    void isParentProduct_hasChildren_returnsTrue() {
        when(productRepository.existsByParentProductId(1L)).thenReturn(true);

        assertTrue(productService.isParentProduct(1L));
    }

    @Test
    @DisplayName("Kiểm tra là sản phẩm cha - không có con")
    void isParentProduct_noChildren_returnsFalse() {
        when(productRepository.existsByParentProductId(2L)).thenReturn(false);

        assertFalse(productService.isParentProduct(2L));
    }

    // ==================== CREATE CHILD PRODUCT ====================

    @Test
    @DisplayName("Tạo sản phẩm con với clone variants từ sibling")
    void createChildProduct_clonesVariantsFromSibling() {
        // Sibling có variants
        Product sibling = new Product();
        sibling.setProductId(2L);
        sibling.setProductName("Sơ mi nam dài tay");

        ProductVariant v1 = new ProductVariant();
        v1.setVariantId(10L);
        v1.setProductId(2L);
        v1.setStyleId(1L);
        v1.setSizeId(1L);
        v1.setLengthTypeId(1L);
        v1.setGender(Gender.NAM);

        ProductVariant v2 = new ProductVariant();
        v2.setVariantId(11L);
        v2.setProductId(2L);
        v2.setStyleId(1L);
        v2.setSizeId(2L);
        v2.setLengthTypeId(1L);
        v2.setGender(Gender.NAM);

        when(productRepository.findById(1L)).thenReturn(Optional.of(parentProduct));
        when(productRepository.findByParentProductId(1L)).thenReturn(List.of(sibling));
        when(variantRepository.findByProductId(2L)).thenReturn(List.of(v1, v2));

        Product newChild = new Product();
        newChild.setProductId(3L);
        newChild.setProductName("Sơ mi nam ngắn tay");
        newChild.setVariantType(VariantType.STRUCTURED);
        newChild.setParentProductId(1L);
        when(productRepository.save(any(Product.class))).thenReturn(newChild);

        Product result = productService.createChildProduct(1L, "Sơ mi nam ngắn tay", "Note");

        assertEquals(3L, result.getProductId());
        assertEquals("Sơ mi nam ngắn tay", result.getProductName());
        // Verify 2 variants were cloned
        verify(variantRepository, times(2)).save(any(ProductVariant.class));
    }

    @Test
    @DisplayName("Tạo sản phẩm con - không tìm thấy sibling có variants")
    void createChildProduct_noSiblingWithVariants_throwsException() {
        Product siblingNoVariants = new Product();
        siblingNoVariants.setProductId(2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(parentProduct));
        when(productRepository.findByParentProductId(1L)).thenReturn(List.of(siblingNoVariants));
        when(variantRepository.findByProductId(2L)).thenReturn(Collections.emptyList());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.createChildProduct(1L, "New", "Note"));
        assertTrue(ex.getMessage().contains("No sibling with variants found"));
    }

    @Test
    @DisplayName("Tạo sản phẩm con - parent không tồn tại")
    void createChildProduct_parentNotFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.createChildProduct(99L, "New", "Note"));
        assertTrue(ex.getMessage().contains("Parent product not found: 99"));
    }
}
