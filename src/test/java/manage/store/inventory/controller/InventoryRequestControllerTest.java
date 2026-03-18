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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.dto.InventoryRequestCreateDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.InventoryRequestService;
import manage.store.inventory.service.RequestSetService;

@WebMvcTest(InventoryRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private InventoryRequestService requestService;
    @MockitoBean private RequestSetService requestSetService;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/requests - tạo request")
    void createRequest_returns200() throws Exception {
        when(requestService.createRequest(any(InventoryRequestCreateDTO.class))).thenReturn(1L);

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitId\":1,\"productId\":1,\"requestType\":\"IN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(1));
    }

    @Test
    @DisplayName("GET /api/requests - lấy tất cả")
    void getAllRequests_returns200() throws Exception {
        when(requestService.getAllRequests()).thenReturn(List.of());

        mockMvc.perform(get("/api/requests"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/requests/{id} - xóa request")
    void deleteRequest_returns204() throws Exception {
        mockMvc.perform(delete("/api/requests/1"))
                .andExpect(status().isNoContent());

        verify(requestService).deleteRequest(1L);
    }

    @Test
    @DisplayName("PATCH /api/requests/{id}/request-type - cập nhật type")
    void updateRequestType_returns200() throws Exception {
        mockMvc.perform(patch("/api/requests/1/request-type")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requestType\":\"IN\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/requests/{id}/dependent-adjust-out - đếm phụ thuộc")
    void getDependentAdjustOutCount_returns200() throws Exception {
        when(requestService.countDependentAdjustOut(1L)).thenReturn(3);

        mockMvc.perform(get("/api/requests/1/dependent-adjust-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dependentAdjustOutCount").value(3))
                .andExpect(jsonPath("$.canMoveDate").value(false));
    }
}
