package manage.store.inventory.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.UserDTO;
import manage.store.inventory.dto.auth.ResetPasswordDTO;
import manage.store.inventory.entity.Role;
import manage.store.inventory.entity.User;
import manage.store.inventory.repository.UserRepository;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.AuthService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AuthService authService;

    public UserController(UserRepository userRepository, CurrentUser currentUser, AuthService authService) {
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.authService = authService;
    }

    // Lấy danh sách users (ADMIN only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Lấy thông tin user hiện tại
    @GetMapping("/me")
    public UserDTO getCurrentUser() {
        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toDTO(user);
    }

    // ADMIN reset mật khẩu cho user khác
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody ResetPasswordDTO request) {
        authService.resetPassword(userId, request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    private UserDTO toDTO(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        return new UserDTO(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getCreatedAt(),
                roles
        );
    }
}
