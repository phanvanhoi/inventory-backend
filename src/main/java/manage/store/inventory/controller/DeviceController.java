package manage.store.inventory.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import manage.store.inventory.entity.DeviceToken;
import manage.store.inventory.repository.DeviceTokenRepository;
import manage.store.inventory.security.UserPrincipal;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceTokenRepository deviceTokenRepository;

    public DeviceController(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public static class RegisterDTO {
        private String pushToken;
        private String platform;

        public String getPushToken() { return pushToken; }
        public void setPushToken(String pushToken) { this.pushToken = pushToken; }
        public String getPlatform() { return platform; }
        public void setPlatform(String platform) { this.platform = platform; }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Void> register(
            @RequestBody RegisterDTO dto,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        if (dto.getPushToken() == null || dto.getPushToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Upsert: delete old entry if exists, then insert
        if (deviceTokenRepository.existsByPushToken(dto.getPushToken())) {
            deviceTokenRepository.deleteByPushToken(dto.getPushToken());
        }

        DeviceToken token = new DeviceToken();
        token.setUserId(principal.getUserId());
        token.setPushToken(dto.getPushToken());
        token.setPlatform(dto.getPlatform() != null ? dto.getPlatform() : "UNKNOWN");
        token.setCreatedAt(LocalDateTime.now());
        deviceTokenRepository.save(token);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/unregister")
    @Transactional
    public ResponseEntity<Void> unregister(
            @RequestBody RegisterDTO dto
    ) {
        if (dto.getPushToken() != null) {
            deviceTokenRepository.deleteByPushToken(dto.getPushToken());
        }
        return ResponseEntity.ok().build();
    }
}
