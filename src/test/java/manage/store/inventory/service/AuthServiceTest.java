package manage.store.inventory.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import manage.store.inventory.dto.auth.AuthResponseDTO;
import manage.store.inventory.dto.auth.LoginRequestDTO;
import manage.store.inventory.dto.auth.RegisterRequestDTO;
import manage.store.inventory.entity.Role;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.RoleRepository;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setRoleId(1L);
        userRole.setRoleName("USER");

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded_password");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);
    }

    // ==================== LOGIN ====================

    @Test
    @DisplayName("Login thành công với credentials đúng")
    void login_success() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
        when(jwtUtil.generateToken(testUser)).thenReturn("jwt_token");

        AuthResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("testuser", response.getUsername());
        assertEquals("Test User", response.getFullName());
        assertTrue(response.getRoles().contains("USER"));
    }

    @Test
    @DisplayName("Login thất bại - username không tồn tại")
    void login_userNotFound_throwsException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("nonexistent");
        request.setPassword("password123");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));
        assertEquals("Sai tên đăng nhập hoặc mật khẩu", ex.getMessage());
    }

    @Test
    @DisplayName("Login thất bại - sai mật khẩu")
    void login_wrongPassword_throwsException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));
        assertEquals("Sai tên đăng nhập hoặc mật khẩu", ex.getMessage());
        verify(jwtUtil, never()).generateToken(any());
    }

    // ==================== REGISTER ====================

    @Test
    @DisplayName("Register thành công")
    void register_success() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setEmail("new@example.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded_new_password");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.of(userRole));

        User savedUser = new User();
        savedUser.setUserId(2L);
        savedUser.setUsername("newuser");
        savedUser.setFullName("New User");
        savedUser.setPassword("encoded_new_password");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        savedUser.setRoles(roles);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(savedUser)).thenReturn("new_jwt_token");

        AuthResponseDTO response = authService.register(request);

        assertNotNull(response);
        assertEquals("new_jwt_token", response.getToken());
        assertEquals("newuser", response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register thất bại - username đã tồn tại")
    void register_duplicateUsername_throwsException() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setFullName("Test");
        request.setEmail("test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(request));
        assertEquals("Username đã tồn tại", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Register thất bại - role USER không tồn tại")
    void register_roleNotFound_throwsException() {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setEmail("new@example.com");

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(roleRepository.findByRoleName("USER")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(request));
        assertEquals("Role USER không tồn tại", ex.getMessage());
    }

    // ==================== CHANGE PASSWORD ====================

    @Test
    @DisplayName("Đổi mật khẩu thành công")
    void changePassword_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpassword", "encoded_password")).thenReturn(true);
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded_new");

        authService.changePassword(1L, "oldpassword", "newpassword");

        verify(userRepository).save(testUser);
        assertEquals("encoded_new", testUser.getPassword());
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại - mật khẩu cũ sai")
    void changePassword_wrongOldPassword_throwsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongold", "encoded_password")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.changePassword(1L, "wrongold", "newpassword"));
        assertEquals("Mật khẩu cũ không đúng", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại - user không tồn tại")
    void changePassword_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.changePassword(99L, "old", "new"));
        assertEquals("User không tồn tại", ex.getMessage());
    }

    // ==================== RESET PASSWORD ====================

    @Test
    @DisplayName("Admin reset mật khẩu thành công")
    void resetPassword_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newpassword")).thenReturn("encoded_reset");

        authService.resetPassword(1L, "newpassword");

        verify(userRepository).save(testUser);
        assertEquals("encoded_reset", testUser.getPassword());
    }

    @Test
    @DisplayName("Reset mật khẩu thất bại - user không tồn tại")
    void resetPassword_userNotFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.resetPassword(99L, "newpassword"));
        assertEquals("User không tồn tại", ex.getMessage());
    }
}
