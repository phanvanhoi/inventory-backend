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

import manage.store.inventory.entity.Position;
import manage.store.inventory.repository.PositionRepository;
import manage.store.inventory.security.JwtUtil;

@WebMvcTest(PositionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PositionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private PositionRepository positionRepository;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/positions - lấy tất cả chức danh")
    void getAllPositions_returns200() throws Exception {
        Position p = new Position();
        p.setPositionId(1L);
        p.setPositionCode("GD");
        p.setPositionName("Giám đốc");
        when(positionRepository.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].positionCode").value("GD"));
    }

    @Test
    @DisplayName("GET /api/positions/{id} - tìm thấy")
    void getPositionById_found_returns200() throws Exception {
        Position p = new Position();
        p.setPositionId(1L);
        p.setPositionCode("GD");
        when(positionRepository.findById(1L)).thenReturn(Optional.of(p));

        mockMvc.perform(get("/api/positions/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/positions/{id} - không tìm thấy trả 404")
    void getPositionById_notFound_returns404() throws Exception {
        when(positionRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/positions/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/positions - tạo chức danh")
    void createPosition_returns200() throws Exception {
        Position saved = new Position();
        saved.setPositionId(1L);
        saved.setPositionCode("PGD");
        saved.setPositionName("Phó giám đốc");
        when(positionRepository.save(any(Position.class))).thenReturn(saved);

        mockMvc.perform(post("/api/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"positionCode\":\"PGD\",\"positionName\":\"Phó giám đốc\"}"))
                .andExpect(status().isOk());
    }
}
