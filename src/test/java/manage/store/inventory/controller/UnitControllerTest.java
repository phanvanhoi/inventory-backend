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

import manage.store.inventory.entity.Unit;
import manage.store.inventory.repository.UnitRepository;
import manage.store.inventory.security.JwtUtil;

@WebMvcTest(UnitController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnitControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UnitRepository unitRepository;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/units - lấy tất cả đơn vị")
    void getAllUnits_returns200() throws Exception {
        Unit unit = new Unit();
        unit.setUnitId(1L);
        unit.setUnitName("Bưu điện HN");
        when(unitRepository.findAll()).thenReturn(List.of(unit));

        mockMvc.perform(get("/api/units"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unitName").value("Bưu điện HN"));
    }

    @Test
    @DisplayName("GET /api/units/{id} - tìm thấy")
    void getUnitById_found_returns200() throws Exception {
        Unit unit = new Unit();
        unit.setUnitId(1L);
        unit.setUnitName("Bưu điện HN");
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));

        mockMvc.perform(get("/api/units/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unitName").value("Bưu điện HN"));
    }

    @Test
    @DisplayName("GET /api/units/{id} - không tìm thấy trả 404")
    void getUnitById_notFound_returns404() throws Exception {
        when(unitRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/units/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/units - tạo đơn vị")
    void createUnit_returns200() throws Exception {
        Unit saved = new Unit();
        saved.setUnitId(1L);
        saved.setUnitName("Bưu điện mới");
        when(unitRepository.save(any(Unit.class))).thenReturn(saved);

        mockMvc.perform(post("/api/units")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitName\":\"Bưu điện mới\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/units/{id} - không tìm thấy trả 404")
    void deleteUnit_notFound_returns404() throws Exception {
        when(unitRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/units/99"))
                .andExpect(status().isNotFound());
    }
}
