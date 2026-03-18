package manage.store.inventory.controller;

import java.util.List;
import java.util.Optional;

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

import manage.store.inventory.entity.Warehouse;
import manage.store.inventory.repository.WarehouseRepository;
import manage.store.inventory.security.JwtUtil;

@WebMvcTest(WarehouseController.class)
@AutoConfigureMockMvc(addFilters = false)
class WarehouseControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private WarehouseRepository warehouseRepository;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/warehouses - lấy tất cả kho")
    void getAll_returns200() throws Exception {
        when(warehouseRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/warehouses/default - lấy kho mặc định")
    void getDefault_found_returns200() throws Exception {
        Warehouse wh = new Warehouse();
        wh.setWarehouseId(1L);
        wh.setWarehouseName("Kho chính");
        wh.setIsDefault(true);
        when(warehouseRepository.findByIsDefaultTrue()).thenReturn(Optional.of(wh));

        mockMvc.perform(get("/api/warehouses/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.warehouseName").value("Kho chính"));
    }

    @Test
    @DisplayName("GET /api/warehouses/default - không có kho mặc định trả 400")
    void getDefault_notFound_returns400() throws Exception {
        when(warehouseRepository.findByIsDefaultTrue()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/warehouses/default"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/warehouses - tạo kho")
    void create_returns200() throws Exception {
        Warehouse saved = new Warehouse();
        saved.setWarehouseId(2L);
        saved.setWarehouseName("Kho phụ");
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);

        mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseName\":\"Kho phụ\",\"isDefault\":false}"))
                .andExpect(status().isOk());
    }
}
