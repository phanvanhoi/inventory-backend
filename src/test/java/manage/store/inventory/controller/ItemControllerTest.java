package manage.store.inventory.controller;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.dto.ItemCreateDTO;
import manage.store.inventory.dto.ItemDetailDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.ItemService;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc(addFilters = false)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ItemService itemService;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private JwtUtil jwtUtil;

    private ItemDetailDTO createMockDetail() {
        return new ItemDetailDTO() {
            @Override public Long getItemId() { return 1L; }
            @Override public Long getRequestId() { return 1L; }
            @Override public Long getVariantId() { return 10L; }
            @Override public String getStyleName() { return "Kiểu 1"; }
            @Override public String getSizeValue() { return "38"; }
            @Override public String getLengthCode() { return "COC"; }
            @Override public String getGender() { return "NAM"; }
            @Override public String getItemCode() { return null; }
            @Override public String getItemName() { return null; }
            @Override public String getUnit() { return null; }
            @Override public BigDecimal getQuantity() { return new BigDecimal("50"); }
        };
    }

    @Test
    @DisplayName("POST /api/items - tạo item")
    void createItem_returns200() throws Exception {
        when(itemService.createItem(any(ItemCreateDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestId\":1,\"variantId\":10,\"quantity\":50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("GET /api/items/{itemId} - lấy item")
    void getItemById_returns200() throws Exception {
        when(itemService.getItemById(1L)).thenReturn(createMockDetail());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizeValue").value("38"));
    }

    @Test
    @DisplayName("GET /api/items/request/{requestId} - lấy items theo request")
    void getItemsByRequestId_returns200() throws Exception {
        when(itemService.getItemsByRequestId(1L)).thenReturn(List.of(createMockDetail()));

        mockMvc.perform(get("/api/items/request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(50));
    }

    @Test
    @DisplayName("DELETE /api/items/{itemId} - xóa item")
    void deleteItem_returns204() throws Exception {
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }
}
