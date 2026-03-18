package manage.store.inventory.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.ProductService;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ProductService productService;
    @MockitoBean private ProductRepository productRepository;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/products - lấy tất cả sản phẩm")
    void getAllProducts_returns200() throws Exception {
        Product p = new Product();
        p.setProductId(1L);
        p.setProductName("Sơ mi");
        p.setVariantType(VariantType.STRUCTURED);
        when(productService.getAllProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Sơ mi"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - tìm thấy")
    void getProductById_found_returns200() throws Exception {
        Product p = new Product();
        p.setProductId(1L);
        p.setProductName("Sơ mi");
        p.setVariantType(VariantType.STRUCTURED);
        when(productService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Sơ mi"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - không tìm thấy trả 404")
    void getProductById_notFound_returns404() throws Exception {
        when(productService.getProductById(99L)).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/products/{id}/children - lấy sản phẩm con")
    void getChildProducts_returns200() throws Exception {
        Product child = new Product();
        child.setProductId(2L);
        child.setProductName("Sơ mi nam");
        child.setVariantType(VariantType.STRUCTURED);
        when(productService.getChildProducts(1L)).thenReturn(List.of(child));

        mockMvc.perform(get("/api/products/1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Sơ mi nam"));
    }

    @Test
    @DisplayName("GET /api/products/{id}/is-parent")
    void isParentProduct_returns200() throws Exception {
        when(productService.isParentProduct(1L)).thenReturn(true);

        mockMvc.perform(get("/api/products/1/is-parent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("POST /api/products/{parentId}/children - tạo sản phẩm con")
    void createChildProduct_returns200() throws Exception {
        Product child = new Product();
        child.setProductId(3L);
        child.setProductName("Sơ mi nam ngắn tay");
        child.setVariantType(VariantType.STRUCTURED);
        when(productService.createChildProduct(1L, "Sơ mi nam ngắn tay", "Note"))
                .thenReturn(child);

        mockMvc.perform(post("/api/products/1/children")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Sơ mi nam ngắn tay\",\"note\":\"Note\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Sơ mi nam ngắn tay"));
    }
}
