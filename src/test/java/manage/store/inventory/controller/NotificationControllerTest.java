package manage.store.inventory.controller;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.NotificationService;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private NotificationService notificationService;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/notifications - lấy tất cả")
    void getNotifications_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        when(notificationService.getNotifications(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/notifications/unread/count - đếm chưa đọc")
    void countUnread_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        when(notificationService.countUnread(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/notifications/unread/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/read - đánh dấu đã đọc")
    void markAsRead_returns200() throws Exception {
        mockMvc.perform(post("/api/notifications/1/read"))
                .andExpect(status().isOk());

        verify(notificationService).markAsRead(1L);
    }

    @Test
    @DisplayName("POST /api/notifications/read-all - đánh dấu tất cả đã đọc")
    void markAllAsRead_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);

        mockMvc.perform(post("/api/notifications/read-all"))
                .andExpect(status().isOk());

        verify(notificationService).markAllAsRead(1L);
    }
}
