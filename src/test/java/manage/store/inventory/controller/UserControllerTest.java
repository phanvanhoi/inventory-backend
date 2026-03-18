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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import manage.store.inventory.entity.User;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.security.JwtUtil;
import manage.store.inventory.service.AuthService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private CurrentUser currentUser;
    @MockitoBean private AuthService authService;
    @MockitoBean private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/users - lấy tất cả users")
    void getAllUsers_returns200() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/me - lấy user hiện tại")
    void getCurrentUser_returns200() throws Exception {
        when(currentUser.getUserId()).thenReturn(1L);
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        user.setFullName("Test User");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/users/{id}/reset-password - reset mật khẩu")
    void resetPassword_returns200() throws Exception {
        mockMvc.perform(post("/api/users/1/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newPassword\":\"newpass123\"}"))
                .andExpect(status().isOk());
    }
}
