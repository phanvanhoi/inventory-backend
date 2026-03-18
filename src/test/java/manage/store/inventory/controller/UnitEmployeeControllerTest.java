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

import manage.store.inventory.dto.UnitEmployeeCreateDTO;
import manage.store.inventory.dto.UnitEmployeeDTO;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.UnitEmployeeService;

@WebMvcTest(UnitEmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class UnitEmployeeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UnitEmployeeService employeeService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/units/{unitId}/employees - lấy nhân viên")
    void getEmployees_returns200() throws Exception {
        when(employeeService.getEmployeesByUnit(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/units/1/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/units/{unitId}/employees - tạo nhân viên")
    void createEmployee_returns200() throws Exception {
        UnitEmployeeDTO dto = new UnitEmployeeDTO(1L, 1L, "Unit", "Nguyễn A", null, null, null);
        when(employeeService.createEmployee(eq(1L), any(UnitEmployeeCreateDTO.class))).thenReturn(dto);

        mockMvc.perform(post("/api/units/1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Nguyễn A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nguyễn A"));
    }

    @Test
    @DisplayName("DELETE /api/units/{unitId}/employees/{id} - xóa nhân viên")
    void deleteEmployee_returns204() throws Exception {
        mockMvc.perform(delete("/api/units/1/employees/1"))
                .andExpect(status().isNoContent());

        verify(employeeService).deleteEmployee(1L, 1L);
    }
}
