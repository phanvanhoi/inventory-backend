package manage.store.inventory.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import manage.store.inventory.dto.auth.AuthResponseDTO;
import manage.store.inventory.dto.auth.ChangePasswordDTO;
import manage.store.inventory.dto.auth.LoginRequestDTO;
import manage.store.inventory.dto.auth.RegisterRequestDTO;
import manage.store.inventory.security.CurrentUser;
import manage.store.inventory.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // User tự đổi mật khẩu (yêu cầu đã đăng nhập)
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordDTO request) {
        authService.changePassword(currentUser.getUserId(), request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
