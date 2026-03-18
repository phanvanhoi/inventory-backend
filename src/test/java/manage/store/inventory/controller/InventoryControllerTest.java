package manage.store.inventory.controller;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.entity.Product;
import manage.store.inventory.entity.enums.VariantType;
import manage.store.inventory.repository.InventoryRepository;
import manage.store.inventory.repository.ProductRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private InventoryRepository inventoryRepository;
    @MockitoBean private ProductRepository productRepository;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/inventory - lấy tất cả tồn kho")
    void getAllInventory_returns200() throws Exception {
        when(currentUser.isAdmin()).thenReturn(true);
        when(productRepository.findLeafProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/inventory/{productId} - lấy tồn kho theo sản phẩm")
    void getInventoryByProduct_returns200() throws Exception {
        Product product = new Product();
        product.setProductId(1L);
        product.setProductName("Sơ mi");
        product.setVariantType(VariantType.STRUCTURED);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(currentUser.isAdmin()).thenReturn(true);
        when(inventoryRepository.getInventoryByProductId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/inventory/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Sơ mi"));
    }

    @Test
    @DisplayName("GET /api/inventory/{productId} - product không tồn tại trả 400")
    void getInventoryByProduct_notFound_returns400() throws Exception {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/99"))
                .andExpect(status().isBadRequest());
    }
}
