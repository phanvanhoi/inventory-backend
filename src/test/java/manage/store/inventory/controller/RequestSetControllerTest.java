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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.dto.RequestSetCreateDTO;
import manage.store.inventory.dto.RequestSetDetailDTO;
import manage.store.inventory.dto.RequestSetListDTO;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.RequestSetRepository;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.ExcelExportService;
import manage.store.inventory.service.ReceiptService;
import manage.store.inventory.service.RequestSetService;

@WebMvcTest(RequestSetController.class)
@AutoConfigureMockMvc(addFilters = false)
class RequestSetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private RequestSetService requestSetService;
    @MockitoBean private ReceiptService receiptService;
    @MockitoBean private RequestSetRepository requestSetRepository;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private ExcelExportService excelExportService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/request-sets - tạo bộ phiếu")
    void createRequestSet_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        when(requestSetService.createRequestSet(any(RequestSetCreateDTO.class), eq(1L))).thenReturn(1L);

        mockMvc.perform(post("/api/request-sets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"setName\":\"ĐX 1\",\"requests\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("GET /api/request-sets - lấy tất cả")
    void getAllRequestSets_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        when(requestSetService.getAllRequestSets(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/request-sets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/request-sets?status=APPROVED - lọc theo status")
    void getAllRequestSets_withStatus_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        when(requestSetService.getRequestSetsByStatus("APPROVED", 1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/request-sets").param("status", "APPROVED"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/request-sets?status=APPROVED,EXECUTED - nhiều status")
    void getAllRequestSets_withMultipleStatuses_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        when(requestSetService.getRequestSetsByStatuses(any(), eq(1L))).thenReturn(List.of());

        mockMvc.perform(get("/api/request-sets").param("status", "APPROVED,EXECUTED"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/request-sets/{setId} - chi tiết")
    void getRequestSetDetail_returns200() throws Exception {
        RequestSetDetailDTO detail = new RequestSetDetailDTO(1L, "Test", null, "PENDING",
                1L, "User", null, null, List.of(), List.of());
        when(requestSetService.getRequestSetDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/request-sets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setName").value("Test"));
    }

    @Test
    @DisplayName("POST /api/request-sets/{setId}/approve - duyệt phiếu")
    void approve_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);

        mockMvc.perform(post("/api/request-sets/1/approve"))
                .andExpect(status().isOk());

        verify(requestSetService).approve(1L, 1L);
    }

    @Test
    @DisplayName("POST /api/request-sets/{setId}/reject - từ chối phiếu")
    void reject_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);

        mockMvc.perform(post("/api/request-sets/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"Số lượng sai\"}"))
                .andExpect(status().isOk());

        verify(requestSetService).reject(1L, 1L, "Số lượng sai");
    }

    @Test
    @DisplayName("POST /api/request-sets/{setId}/execute - thực hiện phiếu")
    void execute_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(3L);

        mockMvc.perform(post("/api/request-sets/1/execute"))
                .andExpect(status().isOk());

        verify(requestSetService).execute(1L, 3L);
    }

    @Test
    @DisplayName("DELETE /api/request-sets/{setId} - xóa phiếu")
    void deleteRequestSet_returns204() throws Exception {
        mockMvc.perform(delete("/api/request-sets/1"))
                .andExpect(status().isNoContent());

        verify(requestSetService).deleteRequestSet(1L, null);
    }

    @Test
    @DisplayName("GET /api/request-sets/{setId}/export - xuất Excel")
    void exportRequestSet_returns200() throws Exception {
        when(excelExportService.exportRequestSet(1L)).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/api/request-sets/1/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("POST /api/request-sets/{setId}/receive - nhận hàng")
    void recordReceipt_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(3L);

        mockMvc.perform(post("/api/request-sets/1/receive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"Lần 1\",\"items\":[{\"requestId\":10,\"variantId\":100,\"receivedQuantity\":20}]}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/request-sets/{setId}/complete - hoàn tất nhận hàng")
    void completeReceipt_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(3L);

        mockMvc.perform(post("/api/request-sets/1/complete"))
                .andExpect(status().isOk());

        verify(receiptService).completeReceipt(1L, 3L);
    }

    @Test
    @DisplayName("GET /api/request-sets/suggested-name - tên gợi ý")
    void getSuggestedName_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        User user = new User();
        user.setUserId(1L);
        user.setFullName("Test User");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestSetRepository.countByCreatedByUserId(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/request-sets/suggested-name"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedName").value("ĐX 6 - Test User"));
    }
}
