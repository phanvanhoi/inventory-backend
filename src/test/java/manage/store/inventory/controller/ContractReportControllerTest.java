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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.dto.ContractReportCreateDTO;
import manage.store.inventory.dto.ContractReportDashboardDTO;
import manage.store.inventory.dto.ContractReportListDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.security.UserPrincipal;
import manage.store.inventory.service.ContractReportService;

@WebMvcTest(ContractReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContractReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private ContractReportService reportService;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/reports - tạo báo cáo")
    void createReport_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        UserPrincipal principal = new UserPrincipal(1L, "sales", List.of("SALES"));
        when(currentUser.get()).thenReturn(principal);
        when(reportService.createReport(any(ContractReportCreateDTO.class), eq(1L))).thenReturn(1L);

        mockMvc.perform(post("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitId\":1,\"salesPerson\":\"Nguyễn A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    @DisplayName("GET /api/reports - lấy tất cả")
    void getAllReports_returns200() throws Exception {
        when(reportService.getAllReports()).thenReturn(List.of());

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/{id} - lấy theo ID")
    void getReportById_returns200() throws Exception {
        ContractReportListDTO dto = new ContractReportListDTO();
        dto.setReportId(1L);
        dto.setCurrentPhase("SALES_INPUT");
        when(reportService.getReportById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPhase").value("SALES_INPUT"));
    }

    @Test
    @DisplayName("DELETE /api/reports/{id} - xóa báo cáo")
    void deleteReport_returns204() throws Exception {
        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isNoContent());

        verify(reportService).deleteReport(1L);
    }

    @Test
    @DisplayName("POST /api/reports/{id}/advance - chuyển phase")
    void advancePhase_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        UserPrincipal principal = new UserPrincipal(1L, "sales", List.of("SALES"));
        when(currentUser.get()).thenReturn(principal);

        mockMvc.perform(post("/api/reports/1/advance"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/{id}/history - lịch sử")
    void getHistory_returns200() throws Exception {
        when(reportService.getHistory(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/1/history"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/alerts - cảnh báo")
    void getAlerts_returns200() throws Exception {
        when(reportService.getAlerts()).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/alerts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/dashboard - dashboard")
    void getDashboard_returns200() throws Exception {
        ContractReportDashboardDTO dashboard = new ContractReportDashboardDTO(
                10L, 5L, 2L, 1L, 1L, 1L, List.of(), List.of(), List.of());
        when(reportService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/reports/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10));
    }
}
